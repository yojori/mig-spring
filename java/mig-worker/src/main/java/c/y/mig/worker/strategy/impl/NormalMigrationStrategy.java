package c.y.mig.worker.strategy.impl;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Component;

import c.y.mig.model.InsertColumn;
import c.y.mig.model.InsertSql;
import c.y.mig.model.InsertTable;
import c.y.mig.model.MigrationList;
import c.y.mig.model.MigrationSchema;
import c.y.mig.util.StringUtil;
import c.y.mig.worker.strategy.AbstractMigrationStrategy;
import c.y.mig.worker.strategy.ProgressListener;

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
            
            String sqlSource = workList.getSql_string();
            String pkCol = workList.getSource_pk();
            
            // Legacy fallbacks
            if (StringUtil.empty(sqlSource)) {
                List<InsertTable> tables = schema.getInsertTableList();
                if (tables != null && !tables.isEmpty()) {
                    InsertTable it = tables.get(0);
                    sqlSource = "SELECT * FROM " + it.getSource_table();
                    if (StringUtil.empty(pkCol)) pkCol = it.getSource_pk();
                }
            }

            if (!StringUtil.empty(pkCol)) {
                String upperSql = sqlSource.toUpperCase();
                if (!upperSql.contains("ORDER BY") && !upperSql.contains("GROUP BY")) {
                    sqlSource += " ORDER BY " + pkCol;
                }
            }

            if (StringUtil.empty(sqlSource)) {
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
            
            // Determine Target Info (Preferred: workList, Fallback: sqlList)
            String targetTable = workList.getTarget_table();
            if (StringUtil.empty(targetTable)) {
                List<InsertSql> sqlList = schema.getInsertSqlList();
                if (sqlList != null && !sqlList.isEmpty()) {
                    targetTable = sqlList.get(0).getInsert_table();
                }
            }

            if (StringUtil.empty(targetTable)) {
                log.warn("No Target Table defined!");
                return;
            }

            List<InsertColumn> allColumns = schema.getInsertColumnList();
            String query = buildTargetQuery(workList, allColumns, schema.getTarget().getDb_type());
            log.info("Target SQL: {}", query);
            
            targetPstmts = new PreparedStatement[1];
            if (query != null) {
                targetPstmts[0] = targetConn.prepareStatement(query);
            }

            // 3. Iterate and Batch Insert
            int rowCount = 0;
            int batchSize = 1000; 
            int totalInserted = 0;
            long totalRead = 0;

            // Cache Column Names to avoid metadata lookup in loop
            String[] colNames = new String[colCount];
            for (int i = 1; i <= colCount; i++) {
                colNames[i-1] = meta.getColumnName(i).toUpperCase();
            }

            long writeTimeTotal = 0;
            long readTimeTotal = 0;
            long lastMark = System.currentTimeMillis();

            while (true) {
                long startRead = System.currentTimeMillis();
                if (!sourceRs.next()) break;
                readTimeTotal += (System.currentTimeMillis() - startRead);

                totalRead++;
                Map<String, Object> row = new HashMap<>();
                for (int i = 1; i <= colNames.length; i++) {
                    row.put(colNames[i-1], sourceRs.getObject(i));
                }

                if (targetPstmts[0] != null) {
                    setTargetParams(targetPstmts[0], workList, allColumns, row);
                    targetPstmts[0].addBatch();
                }

                rowCount++;

                if (rowCount % batchSize == 0) {
                    long startWrite = System.currentTimeMillis();
                    executeBatch(targetPstmts);
                    targetConn.commit();
                    long writeDuration = (System.currentTimeMillis() - startWrite);
                    writeTimeTotal += writeDuration;

                    totalInserted += rowCount;
                    long elapsed = System.currentTimeMillis() - lastMark;
                    log.info("Processed {} rows... (Current Batch - ReadTime: {}ms, WriteTime: {}ms, Total: {}ms)", 
                            totalInserted, readTimeTotal, writeDuration, elapsed);
                    
                    // listener.onProgress removed within batch loop to reduce load
                    
                    // Reset batch specific read timer if you want, or keep cumulative
                    readTimeTotal = 0; 
                    lastMark = System.currentTimeMillis();
                    rowCount = 0;
                }
            }

            if (rowCount > 0) {
                long startWrite = System.currentTimeMillis();
                executeBatch(targetPstmts);
                targetConn.commit();
                writeTimeTotal += (System.currentTimeMillis() - startWrite);
                totalInserted += rowCount;
            }

            log.info("Total Processed Rows: {}, Total Write Time: {}ms, Total Read Time: {}ms", totalInserted, writeTimeTotal, readTimeTotal);
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
