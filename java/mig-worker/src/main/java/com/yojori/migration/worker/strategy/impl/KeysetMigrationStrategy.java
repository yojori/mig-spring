package com.yojori.migration.worker.strategy.impl;

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

import com.yojori.db.query.Select;
import com.yojori.migration.worker.strategy.AbstractMigrationStrategy;
import com.yojori.migration.worker.strategy.ProgressListener;
import com.yojori.model.DBConnMaster;
import com.yojori.model.InsertSql;
import com.yojori.model.InsertTable;
import com.yojori.model.MigrationList;
import com.yojori.model.MigrationSchema;
import com.yojori.util.StringUtil;

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
        
        try {
            for (int i = 0; i < chunkKeys.size(); i++) {
                final int chunkIndex = i;
                final Map<String, Object> startKey = chunkKeys.get(i);
                final Map<String, Object> nextKey = (i < chunkKeys.size() - 1) ? chunkKeys.get(i + 1) : null;
                
                final int chunkLimit = fetchSize; // Still needed for LIMIT, though Range helps
                executor.submit(() -> {
                    try {
                        // Pass nextKey to strictly bound the chunk if we want, but Keyset Strategy
                        // usually just seeks and LIMITs. Range bounding helps safety.
                        // For now, keeping original processChunk logic but we could optimize.
                        processChunk(chunkIndex, startKey, pkCols, finalTableName, schema, chunkLimit, totalProcessed, totalRead);
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

    /*
    private List<Map<String, Object>> fetchChunkKeys(DBConnMaster sourceDs, String tableName, String[] pkCols, int chunkSize) throws SQLException {
        // Overload validation: Check current thread context params or pass them in?
        // Ideally we pass them in. Refactoring signature next.
        // For minimal change, we'll parse from workList in execute and change signature here.
        return new ArrayList<>(); 
    }
    */
    
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
            sql.append(" WHERE rn = 1 OR MOD(rn, ?) = 1 "); 
            sql.append(" ORDER BY rn ");

            log.info("Fetching Keys Query: {}", sql.toString());

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

    private void processChunk(int chunkIndex, Map<String, Object> startKey, String[] pkCols, String tableName, MigrationSchema schema, int limit, AtomicInteger totalProcessed, AtomicInteger totalRead) {
        Connection sourceConn = null;
        Connection targetConn = null;
        PreparedStatement sourcePstmt = null;
        PreparedStatement[] targetPstmts = null;
        ResultSet sourceRs = null;

        try {
            targetConn = dynamicDataSource.getConnection(schema.getTarget());
            targetConn.setAutoCommit(false);
            
            List<InsertSql> sqlList = schema.getInsertSqlList();
            targetPstmts = new PreparedStatement[sqlList.size()];
            for (int i = 0; i < sqlList.size(); i++) {
                String query = buildTargetQuery(sqlList.get(i), schema.getInsertColumnList());
                if (query != null) targetPstmts[i] = targetConn.prepareStatement(query);
            }

            sourceConn = dynamicDataSource.getConnection(schema.getSource());
            
            Select select = new Select();
            select.addField(" * ");
            select.addFrom(tableName);
            
            // WHERE (PK) >= (StartKey)
            addSeekCriteria(select, pkCols, startKey);
            
            for(String col : pkCols) select.addOrder(col);
            
            // LIMIT N
            String dbType = schema.getSource().getDb_type();
            String query = applyLimit(select.toQuery(), limit, dbType);
            
            log.info("Chunk [{}] Query: {}", chunkIndex, query);
            log.info("pk key {}", pkCols.toString());
            
            sourcePstmt = sourceConn.prepareStatement(query);
            bindSeekParams(sourcePstmt, pkCols, startKey, 1);
            sourcePstmt.setFetchSize(limit);
            
            sourceRs = sourcePstmt.executeQuery();
            ResultSetMetaData meta = sourceRs.getMetaData();
            int colCount = meta.getColumnCount();
            
            int rowCount = 0;
            while (sourceRs.next()) {
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
            }
            
            if (rowCount > 0) {
                totalRead.addAndGet(rowCount);
                executeBatch(targetPstmts);
                targetConn.commit();
                totalProcessed.addAndGet(rowCount);
            }
            
            log.info("Chunk [{}] Finished. Rows: {}", chunkIndex, rowCount);

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

    
    // Tuple Comparison: (A, B) >= (?, ?)
    // note: >= because we are taking the EXACT start key of the chunk
    private void addSeekCriteria(Select select, String[] pkCols, Map<String, Object> lastRow) {
        StringBuilder sb = new StringBuilder();
        sb.append(" (");
        for (int i = 0; i < pkCols.length; i++) {
            sb.append(pkCols[i]);
            if (i < pkCols.length - 1) sb.append(", ");
        }
        sb.append(") >= (");
        for (int i = 0; i < pkCols.length; i++) {
            sb.append("?");
            if (i < pkCols.length - 1) sb.append(", ");
        }
        sb.append(") ");
        select.addWhere(sb.toString(), null);
    }

    private int bindSeekParams(PreparedStatement pstmt, String[] pkCols, Map<String, Object> lastRow, int startIdx) throws SQLException {
        int currentParamIdx = startIdx;
        for (String col : pkCols) {
            pstmt.setObject(currentParamIdx++, lastRow.get(col.toUpperCase()));
        }
        return currentParamIdx;
    }

    protected String applyLimit(String baseQuery, int limit, String dbType) {
        if (StringUtil.empty(dbType)) return baseQuery + " LIMIT " + limit;
        
        String upperType = dbType.toUpperCase();
        if (upperType.contains("ORACLE")) {
            return "SELECT * FROM ( " + baseQuery + " ) WHERE ROWNUM <= " + limit;
        } else if (upperType.contains("MSSQL") || upperType.contains("SQLSERVER")) {
            // Check if SELECT is at the start (simple replacement)
            String upperQuery = baseQuery.trim().toUpperCase();
            if (upperQuery.startsWith("SELECT")) {
                 return baseQuery.replaceFirst("(?i)SELECT", "SELECT TOP " + limit);
            }
            // Fallback if not starting with SELECT (e.g. WITH ...) -> Subquery
            return "SELECT TOP " + limit + " * FROM ( " + baseQuery + " ) SUB";
        } else {
            // MySQL, MariaDB, PostgreSQL, etc.
            return baseQuery + " LIMIT " + limit;
        }
    }
}
