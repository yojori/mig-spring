package com.yojori.migration.worker.strategy.impl;

import com.yojori.db.query.Select;
import com.yojori.migration.worker.model.*;
import com.yojori.migration.worker.strategy.AbstractMigrationStrategy;
import com.yojori.util.StringUtil;
import org.springframework.stereotype.Component;

import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import com.yojori.migration.worker.strategy.ProgressListener;

@Component("THREAD_MULTI_ROWNUM") // Deprecated
public class ZXX_RownumMigrationStrategy extends AbstractMigrationStrategy {

    @Override
    public void execute(MigrationSchema schema, MigrationList workList, ProgressListener listener) throws Exception {
        logStart(workList.getMig_name());

        // 1. 유효성 검사 및 테이블 정보 획득
        List<InsertTable> tables = schema.getInsertTableList();
        String tableName = null;
        String pkCol = null;

        if (tables != null && !tables.isEmpty()) {
            InsertTable sourceTableObj = tables.get(0);
            tableName = sourceTableObj.getSource_table();
            pkCol = sourceTableObj.getSource_pk();
        } else {
            // 대체 로직: InsertTable이 없는 경우 SQL 문자열(sql_string)을 소스로 사용
            // 단, ROWNUM 전략은 테이블명과 PK 컬럼명이 필수적임.
            // 필요하다면 sql_string 파싱 로직 추가 가능하나, 현재는 설정값을 권장.
            String sqlSource = workList.getSql_string();
            if (!StringUtil.empty(sqlSource)) {
                String trimmed = sqlSource.trim();
                if (trimmed.toUpperCase().startsWith("SELECT")) {
                    // Try to unwrap simple "SELECT * FROM table" to avoid subquery nesting
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
                 // PK는 InsertSql에서 추론 시도
                 List<InsertSql> sqlList = schema.getInsertSqlList();
                if (sqlList != null && !sqlList.isEmpty()) {
                    pkCol = sqlList.get(0).getPk_column();
                }
            }
        }

        if (StringUtil.empty(tableName)) {
            throw new Exception("ROWNUM migration requires a Source Table.");
        }
        
        if (StringUtil.empty(pkCol)) {
            throw new Exception("ROWNUM migration requires a Primary Key (PK) to be defined.");
        }
        
        // PK 컬럼 분리 (Composite Key 지원)
        String[] pkCols = pkCol.split(",");
        for(int i=0; i<pkCols.length; i++) pkCols[i] = pkCols[i].trim();
        
        // 2. Chunk Key 조회 (ROW_NUMBER 기반)
        int chunkSize = workList.getPage_count_per_thread();
        if (chunkSize <= 0) chunkSize = 1000;
        
        List<Map<String, Object>> chunkKeys = fetchChunkKeys(schema.getSource(), tableName, pkCols, chunkSize);
        
        if (chunkKeys.isEmpty()) {
             log.warn("No data found in source table.");
             return;
        }

        log.info("Fetched {} chunk keys for processing.", chunkKeys.size());

        // 3. 스레드 설정
        int threadCount = workList.getThread_count();
        if (threadCount <= 0) threadCount = 1;

        prepareTargetTable(schema);

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
        
        try {
            for (int i = 0; i < chunkKeys.size(); i++) {
                final int chunkIndex = i;
                final Map<String, Object> startKey = chunkKeys.get(i);
                final Map<String, Object> nextKey = (i + 1 < chunkKeys.size()) ? chunkKeys.get(i + 1) : null;
                
                final String finalTableName = tableName;
                final String[] finalPkCols = pkCols;

                executor.submit(() -> {
                    try {
                        processChunk(chunkIndex, startKey, nextKey, finalPkCols, finalTableName, schema, workList, totalProcessed, totalRead);
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

    private List<Map<String, Object>> fetchChunkKeys(DBConnMaster sourceDs, String tableName, String[] pkCols, int chunkSize) throws SQLException {
        List<Map<String, Object>> keys = new ArrayList<>();
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        
        try {
            conn = dynamicDataSource.getConnection(sourceDs);
            
            String pkList = String.join(", ", pkCols);
            String orderBy = String.join(", ", pkCols);
            
            // Generate Query
            StringBuilder sql = new StringBuilder();
            sql.append("WITH numbered_rows AS ( ");
            sql.append(" SELECT ").append(pkList);
            sql.append(", ROW_NUMBER() OVER (ORDER BY ").append(orderBy).append(") as rn ");
            sql.append(" FROM ").append(tableName);
            sql.append(" ) ");
            sql.append(" SELECT ").append(pkList).append(" FROM numbered_rows ");
            sql.append(" WHERE rn = 1 OR MOD(rn, ?) = 1 "); // Oracle/Postgres style MOD
            // If MySQL: WHERE rn = 1 OR rn % ? = 1
            // We might need DB type check here later, assuming Oracle/Compatible for now based on user query.
            sql.append(" ORDER BY rn ");

            log.info("Fetching Keys Query: {}", sql.toString());

            pstmt = conn.prepareStatement(sql.toString());
            pstmt.setInt(1, chunkSize);
            
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

    private void processChunk(int chunkIndex, Map<String, Object> startKey, Map<String, Object> nextKey, String[] pkCols, String tableName, MigrationSchema schema, MigrationList workList, AtomicInteger totalProcessed, AtomicInteger totalRead) {
        Connection sourceConn = null;
        Connection targetConn = null;
        PreparedStatement sourcePstmt = null;
        PreparedStatement[] targetPstmts = null;
        ResultSet sourceRs = null;

        try {
            // 1. Prepare Target Connection
            targetConn = dynamicDataSource.getConnection(schema.getTarget());
            targetConn.setAutoCommit(false);
            
            List<InsertSql> sqlList = schema.getInsertSqlList();
            targetPstmts = new PreparedStatement[sqlList.size()];
            for (int i = 0; i < sqlList.size(); i++) {
                String query = buildTargetQuery(sqlList.get(i), schema.getInsertColumnList());
                if (query != null) {
                    targetPstmts[i] = targetConn.prepareStatement(query);
                }
            }

            // 2. Prepare Source Query (Range based on Keys)
            sourceConn = dynamicDataSource.getConnection(schema.getSource());
            
            StringBuilder whereClause = new StringBuilder();
            List<Object> params = new ArrayList<>();
            
            // (PK1, PK2) >= (Val1, Val2) logic is complex in standard SQL if not supported directly.
            // Simplified: Assuming standard SQL row comparison is supported: (a,b) >= (c,d)
            // Or use explicit expansion: (a > c) OR (a = c AND b >= d)
            // Ideally target DB supports tuple comparison. Oracle/Postgres/MySQL do.
            
            String pkList = String.join(", ", pkCols);
            String bindParams = "";
            for(int i=0; i<pkCols.length; i++) bindParams += (i==0 ? "?" : ",?");
            
            whereClause.append(" (").append(pkList).append(") >= (").append(bindParams).append(") ");
            for(String col : pkCols) params.add(startKey.get(col.toUpperCase()));
            
            
            // Range Logic: Use Upper Bound
            if (nextKey != null) {
                whereClause.append(" AND (").append(pkList).append(") < (").append(bindParams).append(") ");
                for(String col : pkCols) params.add(nextKey.get(col.toUpperCase()));
            }

            // Remove ORDER BY for performance (Range ensures data integrity)
            String sqlSource = "SELECT * FROM " + tableName + " WHERE " + whereClause.toString();
            
            log.info("Chunk [{}] Query: {}", chunkIndex, sqlSource);
            // log params if needed
            
            sourcePstmt = sourceConn.prepareStatement(sqlSource);
            
            // Set Params
            int idx = 1;
            for(Object p : params) sourcePstmt.setObject(idx++, p);
            
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
            
            totalRead.addAndGet(rowCount);

            executeBatch(targetPstmts);
            targetConn.commit();
            
            totalProcessed.addAndGet(rowCount);
            log.info("Chunk [{}] Finished. Rows: {}", chunkIndex, rowCount);

        } catch (Exception e) {
            log.error("Error in Chunk " + chunkIndex, e);
            try { if (targetConn != null) targetConn.rollback(); } catch (SQLException ex) {}
        } finally {
            if (targetPstmts != null) {
                for (PreparedStatement ps : targetPstmts) closeResources(null, ps, null);
            }
            closeResources(sourceRs, sourcePstmt, sourceConn);
            closeResources(null, null, targetConn);
        }
    }
    
    // Duplicate helper just to be safe or Refactor to Abstract if possible. 
    // Assuming prepareTargetTable is same as others.
    private void prepareTargetTable(MigrationSchema schema) throws SQLException {
        Connection targetConn = null;
        try {
             targetConn = dynamicDataSource.getConnection(schema.getTarget());
             List<InsertTable> tables = schema.getInsertTableList();
             if (tables != null && !tables.isEmpty()) {
                 for (InsertTable t : tables) {
                     if ("Y".equalsIgnoreCase(t.getTruncate_yn())) {
                         log.info("Truncating Table: {}", t.getTarget_table());
                         executeTruncate(targetConn, t.getTarget_table());
                     }
                 }
             } else {
                 if (schema.getInsertSqlList() != null) {
                     for (InsertSql s : schema.getInsertSqlList()) {
                        if ("Y".equalsIgnoreCase(s.getTruncate_yn())) {
                             log.info("Truncating Table: {}", s.getInsert_table());
                             executeTruncate(targetConn, s.getInsert_table());
                        }
                     }
                 }
             }
        } finally {
            closeResources(null, null, targetConn);
        }
    }
}
