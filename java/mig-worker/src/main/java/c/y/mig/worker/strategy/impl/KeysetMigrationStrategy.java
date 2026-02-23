package c.y.mig.worker.strategy.impl;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.springframework.stereotype.Component;

import c.y.mig.db.query.Select;
import c.y.mig.model.DBConnMaster;
import c.y.mig.model.InsertColumn;
import c.y.mig.model.InsertSql;
import c.y.mig.model.InsertTable;
import c.y.mig.model.MigrationList;
import c.y.mig.model.MigrationSchema;
import c.y.mig.util.StringUtil;
import c.y.mig.worker.strategy.AbstractMigrationStrategy;
import c.y.mig.worker.strategy.ProgressListener;

@Component("THREAD_IDX")
public class KeysetMigrationStrategy extends AbstractMigrationStrategy {

    @Override
    public void execute(MigrationSchema schema, MigrationList workList, ProgressListener listener) throws Exception {
        logStart(workList.getMig_name());
        log.info("Starting Migration [THREAD_IDX Strategy]");

        // 1. 유효성 검사 및 테이블 정보 획득
        List<InsertTable> tables = schema.getInsertTableList();
        String tableName = null;
        String pkCol = null;

        if (tables != null && !tables.isEmpty()) {
            InsertTable sourceTableObj = tables.get(0);
            tableName = sourceTableObj.getSource_table();
            pkCol = sourceTableObj.getSource_pk();
        } else {
            String sqlSource = workList.getSql_string();
            if (!StringUtil.empty(sqlSource)) {
                String trimmed = sqlSource.trim();
                if (trimmed.toUpperCase().startsWith("SELECT")) {
                     java.util.regex.Pattern p = java.util.regex.Pattern.compile("(?i)^SELECT\\s+\\*\\s+FROM\\s+([a-zA-Z0-9_.]+)$");
                     java.util.regex.Matcher m = p.matcher(trimmed);
                     if (m.find()) {
                         tableName = m.group(1);
                     } else {
                         tableName = "(" + trimmed + ") T_SOURCE";
                     }
                } else {
                    tableName = trimmed;
                }
                
                List<InsertSql> sqlList = schema.getInsertSqlList();
                if (sqlList != null && !sqlList.isEmpty()) {
                    pkCol = sqlList.get(0).getPk_column();
                }
            }
        }

        if (StringUtil.empty(tableName)) {
            throw new Exception("KEYSET migration requires a Source Table.");
        }
        
        if (StringUtil.empty(pkCol)) {
            throw new Exception("KEYSET migration requires a Primary Key (PK) to be defined.");
        }

        String[] pkCols = pkCol.split(",");
        for(int i=0; i<pkCols.length; i++) pkCols[i] = pkCols[i].trim();


        // 3. Fetch Chunk Keys (from Rownum Strategy)
        int fetchSize = workList.getPage_count_per_thread();
        if (fetchSize <= 0) fetchSize = 1000;
        
        // Parse Min/Max if available
        Map<String, String> params = parseParams(workList.getParam_string());
        String minPk = params.get("MIN_PK");
        String maxPk = params.get("MAX_PK");

        log.info("Fetching Chunk Keys... Min: {}, Max: {}", minPk, maxPk);
        List<Map<String, Object>> chunkKeys = fetchChunkKeys(schema.getSource(), tableName, pkCols, fetchSize, minPk, maxPk);
        
        if (chunkKeys.isEmpty()) {
             log.warn("No data found in source table.");
             return;
        }

        log.info("Fetched {} chunk keys for processing.", chunkKeys.size());
        
        // 4. Parallel Execution
        int threadCount = workList.getThread_count();
        if (threadCount <= 0) threadCount = 1;
        
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        AtomicInteger totalProcessed = new AtomicInteger(0);
        AtomicInteger totalRead = new AtomicInteger(0);
        java.util.Timer progressTimer = new java.util.Timer(true);
        progressTimer.scheduleAtFixedRate(new java.util.TimerTask() {
            @Override
            public void run() {
                if (listener != null) listener.onProgress(totalRead.get(), totalProcessed.get());
            }
        }, 1000, 3000);
        
        final String finalTableName = tableName;

        // 5. Pre-build Target SQL templates & Pre-filter Column lists (Reuse for all chunks)
        List<InsertSql> sqlList = schema.getInsertSqlList();
        List<String> targetQueries = new ArrayList<>();
        List<List<InsertColumn>> filteredColumnsList = new ArrayList<>();

        if (sqlList != null) {
            List<InsertColumn> allColumns = schema.getInsertColumnList();
            for (InsertSql sql : sqlList) {
                List<InsertColumn> filtered = new ArrayList<>();
                for (InsertColumn col : allColumns) {
                    if (sql.getInsert_sql_seq().equals(col.getInsert_sql_seq())) {
                        filtered.add(col);
                    }
                }
                filteredColumnsList.add(filtered);
                targetQueries.add(buildTargetQuery(sql, filtered, schema.getTarget().getDb_type()));
            }
        }

        try {
            for (int i = 0; i < chunkKeys.size(); i++) {
                final int chunkIndex = i;
                final Map<String, Object> startKey = chunkKeys.get(i);
                
                final int chunkLimit = fetchSize;
                executor.submit(() -> {
                    try {
                        processChunk(chunkIndex, startKey, pkCols, finalTableName, schema, chunkLimit, totalProcessed, totalRead, targetQueries, filteredColumnsList);
                    } catch (Exception e) {
                        log.error("Chunk " + chunkIndex + " failed", e);
                    }
                });
            }
            
            executor.shutdown();
            if (!executor.awaitTermination(24, TimeUnit.HOURS)) {
                executor.shutdownNow();
            }
        } catch (Exception e) {
            log.error("Migration execution failed", e);
            executor.shutdownNow();
            throw e;
        } finally {
            if (progressTimer != null) progressTimer.cancel();
        }

        log.info("Total Processed Rows: {}", totalProcessed.get());
        if (listener != null) listener.onProgress(totalRead.get(), totalProcessed.get());
        logEnd(workList.getMig_name(), System.currentTimeMillis());
    }

    // Helper to parse Params
    private Map<String, String> parseParams(String paramStr) {
        Map<String, String> map = new HashMap<>();
        if(StringUtil.empty(paramStr)) return map;
        String[] parts = paramStr.split(";");
        for(String part : parts) {
            String[] kv = part.split("=");
            if(kv.length == 2) map.put(kv[0].trim(), kv[1].trim());
        }
        return map;
    }

    private List<Map<String, Object>> fetchChunkKeys(DBConnMaster sourceDs, String tableName, String[] pkCols, int chunkSize, String minPk, String maxPk) throws SQLException {
        List<Map<String, Object>> keys = new ArrayList<>();
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        
        try {
            conn = dynamicDataSource.getConnection(sourceDs);
            
            String pkList = String.join(", ", pkCols);
            String orderBy = String.join(", ", pkCols);
            
            // Parse Composite PK Params (Assume comma separated)
            String[] minPks = (minPk != null) ? minPk.split(",") : null;
            String[] maxPks = (maxPk != null) ? maxPk.split(",") : null;
            
            // Generate Query
            StringBuilder sql = new StringBuilder();
            sql.append("WITH numbered_rows AS ( ");
            sql.append(" SELECT ").append(pkList);
            sql.append(", ROW_NUMBER() OVER (ORDER BY ").append(orderBy).append(") as rn ");
            sql.append(" FROM ").append(tableName);
            
            // Add WHERE for Min/Max
            sql.append(" WHERE 1=1 "); 
            
            if (minPks != null && minPks.length == pkCols.length) {
                if (pkCols.length == 1) {
                    sql.append(" AND ").append(pkCols[0]).append(" >= ?");
                } else {
                    // Tuple Comparison: (A, B) >= (?, ?)
                    sql.append(" AND (").append(pkList).append(") >= (");
                    for(int i=0; i<pkCols.length; i++) {
                         sql.append("?");
                         if (i < pkCols.length - 1) sql.append(", ");
                    }
                    sql.append(")");
                }
            }
            
            if (maxPks != null && maxPks.length == pkCols.length) {
                 if (pkCols.length == 1) {
                    sql.append(" AND ").append(pkCols[0]).append(" <= ?");
                } else {
                    // Tuple Comparison: (A, B) <= (?, ?)
                    sql.append(" AND (").append(pkList).append(") <= (");
                    for(int i=0; i<pkCols.length; i++) {
                         sql.append("?");
                         if (i < pkCols.length - 1) sql.append(", ");
                    }
                    sql.append(")");
                }
            }
            
            sql.append(" ) ");
            sql.append(" SELECT ").append(pkList).append(" FROM numbered_rows ");
            
            String dbType = sourceDs.getDb_type();
            if (dbType != null && (dbType.toLowerCase().contains("mssql") || dbType.toLowerCase().contains("sqlserver"))) {
                sql.append(" WHERE rn = 1 OR (rn - 1) % ? = 0 "); 
            } else {
                sql.append(" WHERE rn = 1 OR MOD(rn - 1, ?) = 0 "); 
            }
            sql.append(" ORDER BY rn ");

            log.info("Fetching Keys Query ({}): {}", dbType, sql.toString());

            pstmt = conn.prepareStatement(sql.toString());
            
            int idx = 1;
            
            // Bind MIN params
            if (minPks != null && minPks.length == pkCols.length) {
                for(String val : minPks) pstmt.setObject(idx++, val.trim());
            }
            
            // Bind MAX params
            if (maxPks != null && maxPks.length == pkCols.length) {
                for(String val : maxPks) pstmt.setObject(idx++, val.trim());
            }
            
            pstmt.setInt(idx++, chunkSize);
            
            rs = pstmt.executeQuery();
            ResultSetMetaData meta = rs.getMetaData();
            int colCount = meta.getColumnCount();
            
            while (rs.next()) {
                Map<String, Object> keyMap = new HashMap<>();
                for (int i = 1; i <= colCount; i++) {
                    String colName = meta.getColumnName(i).toUpperCase();
                    keyMap.put(colName, rs.getObject(i));
                }
                keys.add(keyMap);
            }
        } finally {
            closeResources(rs, pstmt, conn);
        }
        return keys;
    }

    private void processChunk(int chunkIndex, Map<String, Object> startKey, String[] pkCols, String tableName, MigrationSchema schema, int limit, AtomicInteger totalProcessed, AtomicInteger totalRead, List<String> targetQueries, List<List<InsertColumn>> filteredColumnsList) {
        Connection sourceConn = null;
        Connection targetConn = null;
        PreparedStatement sourcePstmt = null;
        PreparedStatement[] targetPstmts = null;
        ResultSet sourceRs = null;

        try {
            targetConn = dynamicDataSource.getConnection(schema.getTarget());
            targetConn.setAutoCommit(false);
            
            targetPstmts = new PreparedStatement[targetQueries.size()];
            for (int i = 0; i < targetQueries.size(); i++) {
                String query = targetQueries.get(i);
                if (query != null) targetPstmts[i] = targetConn.prepareStatement(query);
            }

            sourceConn = dynamicDataSource.getConnection(schema.getSource());
            
            Select select = new Select();
            select.addField(" * ");
            select.addFrom(tableName);
            
            // Seek Criteria (Keyset Pagination)
            String sourceDbType = schema.getSource().getDb_type();
            addSeekCriteria(select, pkCols, startKey, sourceDbType);
            
            for(String col : pkCols) select.addOrder(col);
            
            // LIMIT N
            String dbType = schema.getSource().getDb_type();
            String query = applyLimit(select.toQuery(), limit, dbType);
            
            log.info("Chunk [{}] Query: {}", chunkIndex, query);
            log.info("pk key {}", java.util.Arrays.toString(pkCols));
            
            sourcePstmt = sourceConn.prepareStatement(query);
            bindSeekParams(sourcePstmt, pkCols, startKey, 1, sourceDbType);
            sourcePstmt.setFetchSize(limit);
            
            sourceRs = sourcePstmt.executeQuery();
            ResultSetMetaData meta = sourceRs.getMetaData();
            int colCount = meta.getColumnCount();

            // [Optimization] Cache column names outside row loop
            String[] colNames = new String[colCount];
            for (int i = 1; i <= colCount; i++) {
                colNames[i-1] = meta.getColumnName(i).toUpperCase();
            }

            long startFetchRowTime = System.currentTimeMillis();
            int rowCount = 0;
            List<InsertSql> sqlList = schema.getInsertSqlList();
            while (sourceRs.next()) {
                Map<String, Object> row = new HashMap<>();
                for (int i = 1; i <= colNames.length; i++) {
                    row.put(colNames[i-1], sourceRs.getObject(i));
                }

                for (int i = 0; i < targetQueries.size(); i++) {
                    if (targetPstmts[i] == null) continue;
                    setTargetParams(targetPstmts[i], sqlList.get(i), filteredColumnsList.get(i), row);
                    targetPstmts[i].addBatch();
                }
                rowCount++;
            }
            long endFetchTime = System.currentTimeMillis();
            long fetchDuration = endFetchTime - startFetchRowTime;
            
            if (rowCount > 0) {
                totalRead.addAndGet(rowCount);
                long startWrite = System.currentTimeMillis();
                executeBatch(targetPstmts);
                targetConn.commit();
                long writeDuration = System.currentTimeMillis() - startWrite;
                totalProcessed.addAndGet(rowCount);
                
                log.info("Chunk [{}] Finished. Rows: {} - Read(Fetch): {}ms, Write(Batch): {}ms, Total: {}ms", 
                        chunkIndex, rowCount, fetchDuration, writeDuration, (System.currentTimeMillis() - startFetchRowTime));
            } else {
                log.info("Chunk [{}] Finished. No rows found.", chunkIndex);
            }

        } catch (Exception e) {
            log.error("Error in Chunk " + chunkIndex, e);
            try { if (targetConn != null) targetConn.rollback(); } catch (SQLException ex) {}
        } finally {
            closeResources(sourceRs, sourcePstmt, sourceConn);
            if (targetPstmts != null) {
                for (PreparedStatement ps : targetPstmts) closeResources(null, ps, null);
            }
            closeResources(null, null, targetConn);
        }
    }

    
}
