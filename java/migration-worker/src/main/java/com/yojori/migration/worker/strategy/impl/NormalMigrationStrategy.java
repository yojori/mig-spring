package com.yojori.migration.worker.strategy.impl;

import com.yojori.db.query.Insert;
import com.yojori.db.query.Select;
import com.yojori.db.query.Update;
import com.yojori.migration.worker.model.*;
import com.yojori.migration.worker.service.PagingQueryBuilder;
import com.yojori.migration.worker.strategy.AbstractMigrationStrategy;
import com.yojori.util.Config;
import com.yojori.util.StringUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component("NORMAL")
public class NormalMigrationStrategy extends AbstractMigrationStrategy {

    @Autowired
    private PagingQueryBuilder pagingQueryBuilder;

    @Override
    public void execute(MigrationSchema schema, MigrationList workList) throws Exception {
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
            if (tables != null && !tables.isEmpty()) {
                 for (InsertTable t : tables) {
                     if ("Y".equalsIgnoreCase(t.getTruncate_yn())) {
                         log.info("Truncating Table: {}", t.getTarget_table());
                         executeTruncate(targetConn, t.getTarget_table());
                     }
                 }
            } else {
                // SQL String Mode uses InsertSql configuration
                for (InsertSql s : sqlList) {
                    if ("Y".equalsIgnoreCase(s.getTruncate_yn())) {
                        log.info("Truncating Table: {}", s.getInsert_table());
                        executeTruncate(targetConn, s.getInsert_table());
                    }
                }
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

            while (sourceRs.next()) {
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
                    executeBatch(targetPstmts);
                    targetConn.commit();
                    totalInserted += rowCount; // Simplified count tracking
                    log.info("Processed {} rows...", totalInserted);
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
