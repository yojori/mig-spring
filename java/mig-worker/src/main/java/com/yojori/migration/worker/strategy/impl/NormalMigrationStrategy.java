package com.yojori.migration.worker.strategy.impl;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.yojori.db.query.Select;
import com.yojori.migration.worker.model.InsertSql;
import com.yojori.migration.worker.model.InsertTable;
import com.yojori.migration.worker.model.MigrationList;
import com.yojori.migration.worker.model.MigrationSchema;
import com.yojori.migration.worker.service.PagingQueryBuilder;
import com.yojori.migration.worker.strategy.AbstractMigrationStrategy;
import com.yojori.migration.worker.strategy.ProgressListener;
import com.yojori.util.StringUtil;

@Component("NORMAL")
public class NormalMigrationStrategy extends AbstractMigrationStrategy {

    @Autowired
    private PagingQueryBuilder pagingQueryBuilder;

    @Override
    public void execute(MigrationSchema schema, MigrationList workList, ProgressListener listener) throws Exception {
        logStart(workList.getMig_name());
        log.info("Starting Migration [Streaming Mode V1]");

        Connection sourceConn = null;
        Connection targetConn = null;
        PreparedStatement sourcePstmt = null;
        PreparedStatement[] targetPstmts = null;
        ResultSet sourceRs = null;

        try {
            // 1. Prepare Source Connection & Query
            sourceConn = dynamicDataSource.getConnection(schema.getSource());
            
            // Streaming mode often requires specific Statement options (e.g., fetchSize)
            // MySQL: Integer.MIN_VALUE, Postgres: setFetchSize(n) with autoCommit false
            
            String sqlSource = "";
            List<InsertTable> tables = schema.getInsertTableList();
            
            if (tables != null && !tables.isEmpty()) {
                // Use First Table
                InsertTable insertT = tables.get(0);
                Select select = new Select();
                select.addField(" * ");
                select.addFrom(insertT.getSource_table());
                if (!StringUtil.empty(insertT.getSource_pk())) {
                    select.addOrder(insertT.getSource_pk());
                }
                sqlSource = select.toQuery();
            } else if (!StringUtil.empty(workList.getSql_string())) {
                sqlSource = workList.getSql_string();
            } else {
                log.warn("No Source defined (Table or SQL)!");
                return;
            }

            log.info("Source SQL: {}", sqlSource);
            
            // Prepare Statement with Streaming options if possible
            sourcePstmt = sourceConn.prepareStatement(sqlSource, ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
            sourcePstmt.setFetchSize(1000); // Default fetch size
            
            sourceRs = sourcePstmt.executeQuery();
            ResultSetMetaData meta = sourceRs.getMetaData();
            int colCount = meta.getColumnCount();

            // 2. Prepare Target Connection & Statements
            targetConn = dynamicDataSource.getConnection(schema.getTarget());
            targetConn.setAutoCommit(false);

            List<InsertSql> sqlList = schema.getInsertSqlList();
            if (sqlList == null || sqlList.isEmpty()) {
                log.warn("No Target Insert SQLs defined!");
                return; 
            }
            
            // Truncate Logic
            // Truncate Logic
            if (tables != null && !tables.isEmpty()) {
                 log.info("Checking Truncate for Tables: {}", tables.size());
                 for (InsertTable t : tables) {
                     String truncYn = StringUtil.nvl(t.getTruncate_yn());
                     log.info("Table: {}, TruncateYN: [{}] (Raw: {})", t.getTarget_table(), truncYn, t.getTruncate_yn());
                     if ("Y".equalsIgnoreCase(truncYn)) {
                         log.info("Truncating Table: {}", t.getTarget_table());
                         executeTruncate(targetConn, t.getTarget_table());
                     }
                 }
            } else {
                log.info("Checking Truncate for SQLs: {}", (sqlList != null ? sqlList.size() : 0));
                // SQL String Mode uses InsertSql configuration
                if (sqlList != null) {
                    for (InsertSql s : sqlList) {
                        log.info("SQL Table: {}, TruncateYN: [{}]", s.getInsert_table(), s.getTruncate_yn());
                        if ("Y".equalsIgnoreCase(StringUtil.nvl(s.getTruncate_yn()))) {
                            log.info("Truncating Table: {}", s.getInsert_table());
                            executeTruncate(targetConn, s.getInsert_table());
                        }
                    }
                }
            }
            
            // Explicitly commit Truncate (DDL might be transactional)
            if (tables != null && !tables.isEmpty() || (sqlList != null && !sqlList.isEmpty())) {
                 log.info("Committing Truncate transaction...");
                 targetConn.commit();
            }

            targetPstmts = new PreparedStatement[sqlList.size()];
            for (int i = 0; i < sqlList.size(); i++) {
                InsertSql iSql = sqlList.get(i);
                String query = buildTargetQuery(iSql, schema.getInsertColumnList());
                log.debug("Target SQL [{}]: {}", i, query);
                if (query != null) {
                    targetPstmts[i] = targetConn.prepareStatement(query);
                }
            }

            // 3. Iterate and Batch Insert
            int rowCount = 0;
            int batchSize = 1000; 
            int totalInserted = 0;
            long totalRead = 0;

            while (sourceRs.next()) {
                totalRead++;
                // Map row data
                Map<String, Object> row = new HashMap<>();
                for (int i = 1; i <= colCount; i++) {
                    String colName = meta.getColumnName(i).toUpperCase();
                    row.put(colName, sourceRs.getObject(i));
                }

                // Add to batch
                for (int i = 0; i < sqlList.size(); i++) {
                    if (targetPstmts[i] == null) continue;
                    setTargetParams(targetPstmts[i], sqlList.get(i), schema.getInsertColumnList(), row);
                    targetPstmts[i].addBatch();
                }

                rowCount++;

                // Execute Batch
                if (rowCount % batchSize == 0) {
                    if (listener != null) listener.onProgress(totalRead, totalInserted);
                    executeBatch(targetPstmts);
                    targetConn.commit();
                    totalInserted += rowCount; // Simplified count tracking
                    log.info("Processed {} rows...", totalInserted);
                    if (listener != null) listener.onProgress(totalRead, totalInserted);
                    rowCount = 0; // Reset checking counter, though logical total increases
                    // Actually let's keep totalInserted accurate
                     // Reset batch counter
                }
            }

            // Final Batch
            if (rowCount > 0) {
                executeBatch(targetPstmts);
                targetConn.commit();
                totalInserted += rowCount;
            }

            log.info("Total Processed Rows: {}", totalInserted);
            if (listener != null) listener.onProgress(totalRead, totalInserted);

        } catch (Exception e) {
            log.error("Migration failed", e);
            try { if (targetConn != null) targetConn.rollback(); } catch (SQLException ex) {}
            throw e;
        } finally {
            closeResources(sourceRs, sourcePstmt, sourceConn);
            if (targetPstmts != null) {
                for (PreparedStatement ps : targetPstmts) closeResources(null, ps, null);
            }
            closeResources(null, null, targetConn);
        }
        
        logEnd(workList.getMig_name(), System.currentTimeMillis());
    }
    

}
