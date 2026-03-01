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

import c.y.mig.model.DBConnMaster;
import c.y.mig.model.InsertSql;
import c.y.mig.model.InsertTable;
import c.y.mig.model.MigrationList;
import c.y.mig.model.MigrationSchema;
import c.y.mig.util.StringUtil;
import c.y.mig.worker.strategy.AbstractMigrationStrategy;
import c.y.mig.worker.strategy.ProgressListener;

@Component("THREAD_MULTI_ROWNUM") // Deprecated
public class ZXX_RownumMigrationStrategy extends AbstractMigrationStrategy {

    @Override
    public void execute(MigrationSchema schema, MigrationList workList, ProgressListener listener) throws Exception {
        logStart(workList.getMig_name());

        // 1. 유효성 검사 및 테이블 정보 획득
        String tableName = null;
        String pkCol = workList.getSource_pk();

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
        }
        
        // Legacy fallbacks
        if (StringUtil.empty(tableName)) {
            List<InsertTable> tables = schema.getInsertTableList();
            if (tables != null && !tables.isEmpty()) {
                tableName = tables.get(0).getSource_table();
                if (StringUtil.empty(pkCol)) pkCol = tables.get(0).getSource_pk();
            }
        }

        if (StringUtil.empty(pkCol)) {
            List<InsertSql> sqlList = schema.getInsertSqlList();
            if (sqlList != null && !sqlList.isEmpty()) {
                pkCol = sqlList.get(0).getPk_column();
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

        // 3. Thread configuration
        int threadCount = workList.getThread_count();
        if (threadCount <= 0) threadCount = 1;

        // Truncate is now handled by Abstract prepare()
        // prepareTargetTable(schema);

        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        AtomicInteger totalProcessed = new AtomicInteger(0);
        AtomicInteger totalRead = new AtomicInteger(0);
        // progressTimer removed to reduce database load
        
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
            // progressTimer cleanup removed
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
            
            List<c.y.mig.model.InsertColumn> allColumns = schema.getInsertColumnList();
            String targetQuery = buildTargetQuery(workList, allColumns, schema.getTarget().getDb_type());
            
            if (targetQuery != null) {
                targetPstmts = new PreparedStatement[1];
                targetPstmts[0] = targetConn.prepareStatement(targetQuery);
            } else {
                // Legacy fallback
                List<InsertSql> sqlList = schema.getInsertSqlList();
                if (sqlList != null) {
                    targetPstmts = new PreparedStatement[sqlList.size()];
                    for (int i = 0; i < sqlList.size(); i++) {
                        String query = buildTargetQuery(sqlList.get(i), allColumns, schema.getTarget().getDb_type());
                        if (query != null) {
                            targetPstmts[i] = targetConn.prepareStatement(query);
                        }
                    }
                }
            }

            // 2. Prepare Source Query (Range based on Keys)
            sourceConn = dynamicDataSource.getConnection(schema.getSource());
            
            StringBuilder whereClause = new StringBuilder();
            List<Object> params = new ArrayList<>();
            
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
            
            sourcePstmt = sourceConn.prepareStatement(sqlSource);
            
            // Set Params
            int idx = 1;
            for(Object p : params) sourcePstmt.setObject(idx++, p);
            
            sourceRs = sourcePstmt.executeQuery();
            ResultSetMetaData meta = sourceRs.getMetaData();
            int colCount = meta.getColumnCount();
            
            // Optimization: Cache column names
            String[] colNames = new String[colCount];
            for (int i = 1; i <= colCount; i++) {
                colNames[i-1] = meta.getColumnName(i).toUpperCase();
            }

            int rowCount = 0;
            boolean isOneToOne = (targetQuery != null);
            List<InsertSql> sqlList = schema.getInsertSqlList();

            while (sourceRs.next()) {
                Map<String, Object> row = new HashMap<>();
                for (int i = 0; i < colCount; i++) {
                    row.put(colNames[i], sourceRs.getObject(i + 1));
                }

                if (isOneToOne) {
                    if (targetPstmts != null && targetPstmts[0] != null) {
                        setTargetParams(targetPstmts[0], workList, allColumns, row);
                        targetPstmts[0].addBatch();
                    }
                } else if (targetPstmts != null) {
                    for (int i = 0; i < targetPstmts.length; i++) {
                        if (targetPstmts[i] == null) continue;
                        setTargetParams(targetPstmts[i], sqlList.get(i), allColumns, row);
                        targetPstmts[i].addBatch();
                    }
                }
                rowCount++;
            }
            
            totalRead.addAndGet(rowCount);

            if (rowCount > 0 && targetPstmts != null) {
                executeBatch(targetPstmts);
                targetConn.commit();
            }
            
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
}
