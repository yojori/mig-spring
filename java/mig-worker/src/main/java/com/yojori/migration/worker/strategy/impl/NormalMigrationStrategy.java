package com.yojori.migration.worker.strategy.impl;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Component;

import com.yojori.db.query.Select;
import com.yojori.migration.worker.strategy.AbstractMigrationStrategy;
import com.yojori.migration.worker.strategy.ProgressListener;
import com.yojori.model.InsertSql;
import com.yojori.model.InsertTable;
import com.yojori.model.MigrationList;
import com.yojori.model.MigrationSchema;
import com.yojori.util.StringUtil;

@Component("NORMAL")
public class NormalMigrationStrategy extends AbstractMigrationStrategy {


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
            // 1. Prepare Source connection & Query
            sourceConn = dynamicDataSource.getConnection(schema.getSource());
            
            String sqlSource = "";
            List<InsertTable> tables = schema.getInsertTableList();
            
            if (tables != null && !tables.isEmpty()) {
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
            
            sourcePstmt = sourceConn.prepareStatement(sqlSource, ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
            sourcePstmt.setFetchSize(1000);
            
            sourceRs = sourcePstmt.executeQuery();
            ResultSetMetaData meta = sourceRs.getMetaData();
            int colCount = meta.getColumnCount();

            // 2. Prepare Target connection & Statements
            targetConn = dynamicDataSource.getConnection(schema.getTarget());
            targetConn.setAutoCommit(false);
            
            List<InsertSql> sqlList = schema.getInsertSqlList();
            if (sqlList == null || sqlList.isEmpty()) {
                log.warn("No Target Insert SQLs defined!");
                return;
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
                Map<String, Object> row = new HashMap<>();
                for (int i = 1; i <= colCount; i++) {
                    String colName = meta.getColumnName(i).toUpperCase();
                    row.put(colName, sourceRs.getObject(i));
                }

                for (int i = 0; i < sqlList.size(); i++) {
                    if (targetPstmts[i] == null) continue;
                    setTargetParams(targetPstmts[i], sqlList.get(i), schema.getInsertColumnList(), row);
                    targetPstmts[i].addBatch();
                }

                rowCount++;

                if (rowCount % batchSize == 0) {
                    executeBatch(targetPstmts);
                    targetConn.commit();
                    totalInserted += rowCount;
                    log.info("Processed {} rows...", totalInserted);
                    if (listener != null) listener.onProgress(totalRead, totalInserted);
                    rowCount = 0;
                }
            }

            if (rowCount > 0) {
                executeBatch(targetPstmts);
                targetConn.commit();
                totalInserted += rowCount;
            }

            log.info("Total Processed Rows: {}", totalInserted);
            if (listener != null) listener.onProgress(totalRead, totalInserted);

        } catch (Exception e) {
            log.error("Migration failed", e);
            if (targetConn != null) {
                try { targetConn.rollback(); } catch (SQLException ex) {}
            }
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
