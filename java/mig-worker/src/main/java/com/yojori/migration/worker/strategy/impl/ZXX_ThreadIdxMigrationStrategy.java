package com.yojori.migration.worker.strategy.impl;

import java.math.BigInteger;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
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
import com.yojori.model.InsertSql;
import com.yojori.model.InsertTable;
import com.yojori.model.MigrationList;
import com.yojori.model.MigrationSchema;
import com.yojori.util.StringUtil;

@Component("THREAD_IDX_LEGACY")
public class ZXX_ThreadIdxMigrationStrategy extends AbstractMigrationStrategy {

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
            String sqlSource = workList.getSql_string();
            if (!StringUtil.empty(sqlSource)) {
                tableName = "(" + sqlSource + ") T_SOURCE";
                List<InsertSql> sqlList = schema.getInsertSqlList();
                if (sqlList != null && !sqlList.isEmpty()) {
                    pkCol = sqlList.get(0).getPk_column();
                }
            }
        }

        if (StringUtil.empty(tableName)) {
            throw new Exception("THREAD_IDX migration requires a Source Table or SQL configuration.");
        }
        
        if (StringUtil.empty(pkCol)) {
            throw new Exception("THREAD_IDX migration requires a Primary Key (PK) to be defined (in Source Table or Insert Sql).");
        }
        
        // 2. PK의 최소값(Min)과 최대값(Max) 조회
        Object minVal = null;
        Object maxVal = null;
        boolean isNumeric = true;
        
        // PARAM_STRING parsing if available (from THREAD_MULTI dispatcher)
        String params = workList.getParam_string();
        if (!StringUtil.empty(params) && params.contains("MIN_PK=") && params.contains("MAX_PK=")) {
            log.info("Using provided PK Range from params: {}", params);
            try {
                String[] tokens = params.split(";");
                for (String token : tokens) {
                    if (token.startsWith("MIN_PK=")) minVal = token.substring("MIN_PK=".length());
                    if (token.startsWith("MAX_PK=")) maxVal = token.substring("MAX_PK=".length());
                }
            } catch (Exception e) {
                log.warn("Failed to parse params, falling back to DB query: {}", e.getMessage());
                minVal = null; maxVal = null;
            }
        }

        Connection sourceConn = null;
        Statement stmt = null;
        ResultSet rs = null;
        
        if (minVal == null || maxVal == null) {
            try {
                sourceConn = dynamicDataSource.getConnection(schema.getSource());
                stmt = sourceConn.createStatement();
                String minMaxSql = "SELECT MIN(" + pkCol + "), MAX(" + pkCol + ") FROM " + tableName;
                log.info("Fetching Min/Max PK: {}", minMaxSql);
                rs = stmt.executeQuery(minMaxSql);
                if (rs.next()) {
                    minVal = rs.getObject(1);
                    maxVal = rs.getObject(2);
                }
            } finally {
                closeResources(rs, stmt, sourceConn);
            }
        }

        if (minVal == null || maxVal == null) {
             log.warn("Table appears empty or Min/Max is null.");
             return;
        }

        // Check Type
        BigInteger minBi, maxBi;
        if (minVal instanceof Number) {
            minBi = BigInteger.valueOf(((Number) minVal).longValue());
            maxBi = BigInteger.valueOf(((Number) maxVal).longValue());
        } else {
            isNumeric = false; // String (UUID assumption)
            minBi = uuidToBigInt(minVal.toString());
            maxBi = uuidToBigInt(maxVal.toString());
        }
        
        log.info("PK Range (BigInt): {} ~ {}", minBi, maxBi);
        
        if (maxBi.compareTo(minBi) < 0) {
             log.warn("Invalid range (Max < Min).");
             return;
        }

        // 3. Thread configuration
        int threadCount = workList.getThread_count();
        if (threadCount <= 0) threadCount = 1;

        // Truncate is now handled by Abstract prepare() call in Executor
        // prepareTargetTable(schema);

        // 4. 범위 계산 및 작업 제출
        BigInteger totalRows = maxBi.subtract(minBi).add(BigInteger.ONE);
        BigInteger rangePerThread = totalRows.divide(BigInteger.valueOf(threadCount));
        if (totalRows.mod(BigInteger.valueOf(threadCount)).compareTo(BigInteger.ZERO) > 0) {
            rangePerThread = rangePerThread.add(BigInteger.ONE);
        }

        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        AtomicInteger totalProcessed = new AtomicInteger(0);
        
        final String finalTableName = tableName;
        final String finalPkCol = pkCol;
        final boolean finalIsNumeric = isNumeric;
        final BigInteger finalMinBi = minBi;
        final BigInteger finalMaxBi = maxBi;
        final BigInteger finalRange = rangePerThread;
        
        try {
            for (int i = 0; i < threadCount; i++) {
                final BigInteger startBi = finalMinBi.add(finalRange.multiply(BigInteger.valueOf(i)));
                BigInteger endCalc = startBi.add(finalRange).subtract(BigInteger.ONE);
                final BigInteger endBi = (endCalc.compareTo(finalMaxBi) > 0) ? finalMaxBi : endCalc;
                
                if (startBi.compareTo(finalMaxBi) > 0) break;

                final int threadNum = i;
                executor.submit(() -> {
                    try {
                        processRange(threadNum, startBi, endBi, finalPkCol, finalTableName, schema, workList, totalProcessed, finalIsNumeric);
                    } catch (Exception e) {
                        log.error("Thread " + threadNum + " failed", e);
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
        }

        log.info("Total Processed Rows: {}", totalProcessed.get());
        logEnd(workList.getMig_name(), System.currentTimeMillis());
    }
    

    private void processRange(int threadNum, BigInteger startBi, BigInteger endBi, String pkCol, String tableName, MigrationSchema schema, MigrationList workList, AtomicInteger totalProcessed, boolean isNumeric) {
        Connection sourceConn = null;
        Connection targetConn = null;
        PreparedStatement sourcePstmt = null;
        PreparedStatement[] targetPstmts = null;
        ResultSet sourceRs = null;
        
        // long threadStartTime = System.currentTimeMillis(); // Unused

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

            // 2. Prepare Source Connection & Query
            sourceConn = dynamicDataSource.getConnection(schema.getSource());
            
            Select select = new Select();
            select.addField(" * ");
            select.addFrom(tableName);
            select.addWhere(pkCol + " >= ?", null); // Placeholders only
            select.addWhere(pkCol + " <= ?", null);
            select.addOrder(pkCol);
            
            String sqlSource = select.toQuery();
            
            // Debugging Query & Params
            log.info("Thread[{}] Range Query: {}", threadNum, sqlSource);
            if (isNumeric) {
                 log.info("Thread[{}] Range Params: 1=[{}], 2=[{}]", threadNum, startBi, endBi);
            } else {
                 log.info("Thread[{}] Range Params: 1=[{}], 2=[{}]", threadNum, bigIntToUuid(startBi), bigIntToUuid(endBi));
            }

            sourcePstmt = sourceConn.prepareStatement(sqlSource);
            
            // Parameter Binding
            if (isNumeric) {
                sourcePstmt.setLong(1, startBi.longValue());
                sourcePstmt.setLong(2, endBi.longValue());
            } else {
                // Formatting UUID
                sourcePstmt.setString(1, bigIntToUuid(startBi));
                sourcePstmt.setString(2, bigIntToUuid(endBi));
            }
            
            int fetchSize = workList.getPage_count_per_thread();
            if (fetchSize <= 0) fetchSize = 1000;
            sourcePstmt.setFetchSize(fetchSize);
            
            sourceRs = sourcePstmt.executeQuery();
            ResultSetMetaData meta = sourceRs.getMetaData();
            int colCount = meta.getColumnCount();
            
            int rowCount = 0;
            int batchSize = fetchSize; 
            
            long stepStartTime = System.currentTimeMillis();

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

                if (rowCount % batchSize == 0) {
                    executeBatch(targetPstmts);
                    targetConn.commit();
                    
                    totalProcessed.addAndGet(batchSize);
                    
                    long stepEndTime = System.currentTimeMillis();
                    double stepSeconds = (stepEndTime - stepStartTime) / 1000.0;
                    
                    // Log sparsely
                    log.info("Thread {} processed {} rows (Batch: {}s)", threadNum, rowCount, String.format("%.3f", stepSeconds));
                    
                    stepStartTime = System.currentTimeMillis();
                }
            }
            
            if (rowCount % batchSize != 0) {
                executeBatch(targetPstmts);
                targetConn.commit();
                totalProcessed.addAndGet(rowCount % batchSize);
            }
            
            log.info("Thread {} Finished. Total Rows: {}", threadNum, rowCount);

        } catch (Exception e) {
            log.error("Error in thread " + threadNum, e);
            try { if (targetConn != null) targetConn.rollback(); } catch (SQLException ex) {}
        } finally {
            if (targetPstmts != null) {
                for (PreparedStatement ps : targetPstmts) closeResources(null, ps, null);
            }
            closeResources(sourceRs, sourcePstmt, sourceConn);
            closeResources(null, null, targetConn);
        }
    }
    
    // Helper: UUID String (with hyphens) -> BigInteger
    private BigInteger uuidToBigInt(String uuidInfo) {
        if (uuidInfo == null) return BigInteger.ZERO;
        String hex = uuidInfo.replace("-", "").trim();
        if (hex.isEmpty()) return BigInteger.ZERO;
        try {
            return new BigInteger(hex, 16);
        } catch (NumberFormatException e) {
            log.error("Failed to parse UUID: " + uuidInfo, e);
            return BigInteger.ZERO;
        }
    }
    
    // Helper: BigInteger -> UUID String (with hyphens)
    private String bigIntToUuid(BigInteger val) {
        // Must be 32 hex chars
        String hex = String.format("%032x", val);
        // Insert hyphens: 8-4-4-4-12
        return new StringBuilder(hex)
                .insert(8, "-")
                .insert(13, "-")
                .insert(18, "-")
                .insert(23, "-")
                .toString();
    }
}
