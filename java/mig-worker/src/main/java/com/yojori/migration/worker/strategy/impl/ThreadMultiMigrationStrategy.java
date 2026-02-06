package com.yojori.migration.worker.strategy.impl;

import com.yojori.migration.worker.client.WorkerClient;
import com.yojori.migration.worker.model.*;
import com.yojori.migration.worker.strategy.AbstractMigrationStrategy;
import com.yojori.util.Config;
import com.yojori.util.StringUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.math.BigInteger;
import java.sql.*;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.ArrayList;
// import java.util.HashMap;

@Component("THREAD_MULTI")
public class ThreadMultiMigrationStrategy extends AbstractMigrationStrategy {

    @Autowired
    private WorkerClient workerClient;
    
    @Autowired
    private KeysetMigrationStrategy keysetMigrationStrategy;

    @Override
    public void execute(MigrationSchema schema, MigrationList workList) throws Exception {
        logStart("THREAD_MULTI_DISPATCHER: " + workList.getMig_name());
        
        // 0. Check if this is a Child Task (Executor) or Parent Task (Dispatcher)
        if (workList.getParam_string() != null && 
           (workList.getParam_string().contains("MIN_PK") || workList.getParam_string().contains("MAX_PK"))) {
            
            log.info("Executing Child Task Logic (Delegating to Keyset Strategy) with params: {}", workList.getParam_string());
            
            // Delegate to KeysetMigrationStrategy (Bean)
            keysetMigrationStrategy.execute(schema, workList);
            return;
        }

        // 1. Validate Source Table & PK
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

        if (StringUtil.empty(tableName) || StringUtil.empty(pkCol)) {
            throw new Exception("THREAD_MULTI requires Source Table/SQL and PK.");
        }

        String[] pkCols = pkCol.split(",");
        for(int i=0; i<pkCols.length; i++) pkCols[i] = pkCols[i].trim();

        // 2. Fetch All Chunk Keys
        int fetchSize = workList.getPage_count_per_thread();
        if (fetchSize <= 0) fetchSize = 1000;
        
        log.info("Fetching All Chunk Keys for Splitting... (Size: {})", fetchSize);
        List<Map<String, Object>> allKeys = fetchChunkKeys(schema.getSource(), tableName, pkCols, fetchSize);
        
        if (allKeys.isEmpty()) {
             log.warn("No data found in source table.");
             return;
        }

        log.info("Total Keys Fetched: {}", allKeys.size());

        // 3. Calculate Partitions
        int splitCount = workList.getThread_count();
        if (splitCount <= 0) splitCount = 1;

        int totalKeys = allKeys.size();
        
        // If total keys is small, adjust split count
        if (totalKeys < splitCount) {
            splitCount = totalKeys;
        }
        
        int keysPerSplit = totalKeys / splitCount;
        int remainder = totalKeys % splitCount;
        
        log.info("Dispatching {} tasks for {} keys (Keys/Task: approx {})", splitCount, totalKeys, keysPerSplit);
        
        int currentIdx = 0;

        for (int i = 0; i < splitCount; i++) {
            int myCount = keysPerSplit + (i < remainder ? 1 : 0);
            int startIdx = currentIdx;
            int endIdx = currentIdx + myCount - 1;
            
            // Safety
            if (startIdx >= totalKeys) break;
            
            Map<String, Object> startMap = allKeys.get(startIdx);
            Map<String, Object> endMap = allKeys.get(endIdx);
             
            // Extract PK string (Composite implementation)
            List<String> minPkList = new ArrayList<>();
            List<String> maxPkList = new ArrayList<>();
            
            for (String col : pkCols) {
                minPkList.add(String.valueOf(startMap.get(col.toUpperCase())));
                maxPkList.add(String.valueOf(endMap.get(col.toUpperCase())));
            }
            
            String minPk = String.join(",", minPkList);
            String maxPk = String.join(",", maxPkList);
            
            // For parameter string
            String paramStr = "MIN_PK=" + minPk + ";MAX_PK=" + maxPk;
            
            MigrationList childTask = new MigrationList();
            childTask.setMig_master(workList.getMig_master());
            // childTask.setMig_list_seq(com.yojori.util.Config.getOrdNoSequence("ML")); // OLD: New ID
             childTask.setMig_list_seq(workList.getMig_list_seq()); // NEW: Reuse Parent ID !!!
            
            childTask.setMig_name(workList.getMig_name() + "_Worker_" + (i+1));
            // childTask.setMig_type("THREAD_IDX"); // OLD: Change Type
            childTask.setMig_type(workList.getMig_type()); // NEW: Keep THREAD_MULTI (Parent matches)
            
            childTask.setThread_use_yn("Y");
            childTask.setThread_count(workList.getThread_count()); 
            childTask.setPage_count_per_thread(workList.getPage_count_per_thread());
            childTask.setOrdering(workList.getOrdering() + (i+1));
            childTask.setExecute_yn("Y"); 
            childTask.setDisplay_yn("Y");
            childTask.setSource_db_alias(workList.getSource_db_alias());
            childTask.setTarget_db_alias(workList.getTarget_db_alias());
            childTask.setSource_db_type(workList.getSource_db_type());
            childTask.setTarget_db_type(workList.getTarget_db_type());
            childTask.setSql_string(workList.getSql_string());
            childTask.setParam_string(paramStr); // Set Filtering Params
            childTask.setCreate_date(new Date());
            childTask.setUpdate_date(new Date());
            
            workerClient.createChildTask(childTask);
            log.info("Created Child Task [{}]: Range {} ~ {}", childTask.getMig_name(), minPk, maxPk);
            
            currentIdx += myCount;
        }

        logEnd(workList.getMig_name(), System.currentTimeMillis());
    }
    
    // Duplicated from KeysetMigrationStrategy due to private access
    private List<Map<String, Object>> fetchChunkKeys(com.yojori.migration.worker.model.DBConnMaster sourceDs, String tableName, String[] pkCols, int chunkSize) throws SQLException {
        java.util.List<java.util.Map<String, Object>> keys = new java.util.ArrayList<>();
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        
        try {
            conn = dynamicDataSource.getConnection(sourceDs);
            
            String pkList = String.join(", ", pkCols);
            String orderBy = String.join(", ", pkCols);
            
            StringBuilder sql = new StringBuilder();
            sql.append("WITH numbered_rows AS ( ");
            sql.append(" SELECT ").append(pkList);
            sql.append(", ROW_NUMBER() OVER (ORDER BY ").append(orderBy).append(") as rn ");
            sql.append(" FROM ").append(tableName);
            sql.append(" ) ");
            sql.append(" SELECT ").append(pkList).append(" FROM numbered_rows ");
            sql.append(" WHERE rn = 1 OR MOD(rn, ?) = 1 "); 
            sql.append(" ORDER BY rn ");

            pstmt = conn.prepareStatement(sql.toString());
            pstmt.setInt(1, chunkSize);
            
            rs = pstmt.executeQuery();
            ResultSetMetaData meta = rs.getMetaData();
            int colCount = meta.getColumnCount();
            
            while (rs.next()) {
                java.util.Map<String, Object> keyMap = new java.util.HashMap<>();
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
    
    // Remove unused helper methods from previous version if any checks exist
    private BigInteger uuidToBigInt(String uuidInfo) { return BigInteger.ZERO; }
    private String bigIntToUuid(BigInteger val) { return ""; }

}
