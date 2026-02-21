package c.y.mig.worker.strategy.impl;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import c.y.mig.worker.client.WorkerClient;
import c.y.mig.worker.strategy.AbstractMigrationStrategy;
import c.y.mig.worker.strategy.ProgressListener;
import c.y.mig.model.DBConnMaster;
import c.y.mig.model.InsertSql;
import c.y.mig.model.InsertTable;
import c.y.mig.model.MigrationList;
import c.y.mig.model.MigrationSchema;

@Component("JAVA")
public class JavaMigrationStrategy extends AbstractMigrationStrategy {
    @Autowired
    private WorkerClient workerClient;

    @Override
    public void execute(MigrationSchema schema, MigrationList workList, ProgressListener listener) throws Exception {
        log.info("!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
        log.info("!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
        log.info("[JAVA_STRATEGY] Starting execute: " + workList.getMig_name());
        log.info("!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
        log.info("!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
        logStart(workList.getMig_name());

        // 0. Extract Partitioning/Method Config
        String sourceClassWithMethod = null;
        String pkCol = null;
        
        // Try to parse from Param String first (for Child Tasks)
        Map<String, String> paramMap = parseParamString(workList.getParam_string());
        if (paramMap.containsKey("TARGET_CLASS") && paramMap.containsKey("TARGET_METHOD")) {
            sourceClassWithMethod = paramMap.get("TARGET_CLASS") + "." + paramMap.get("TARGET_METHOD");
            pkCol = paramMap.get("PK_COL");
            log.info("[JAVA_STRATEGY] Found config in Param String: {} (PK: {})", sourceClassWithMethod, pkCol);
        }

        if (sourceClassWithMethod == null && schema.getInsertSqlList() != null && !schema.getInsertSqlList().isEmpty()) {
            InsertSql config = schema.getInsertSqlList().get(0);
            sourceClassWithMethod = config.getInsert_table(); // "com.package.Class.method"
            pkCol = config.getPk_column(); // "ID" or "ID1,ID2"
            log.info("[JAVA_STRATEGY] Found config in InsertSql (Step 3): {} (PK: {})", sourceClassWithMethod, pkCol);
        }

        // 1. Dispatcher Logic (Parent Task)
        boolean isChildTask = paramMap.containsKey("KEYSET_START");
        
        if (!isChildTask && sourceClassWithMethod != null && "Y".equals(workList.getThread_use_yn()) && workList.getThread_count() > 1) {
            log.info("[JAVA_STRATEGY] Executing Master Logic: Keyset Partitioning by {} for Method: {}", pkCol, sourceClassWithMethod);
            if (executeKeysetPartitioning(schema, workList, pkCol, sourceClassWithMethod)) {
                log.info("[JAVA_STRATEGY] Master Logic Finished (Dispatcher)");
                return; 
            }
            log.info("[JAVA_STRATEGY] Keyset Partitioning failed. Falling back to single-threaded execution.");
        }

        // 2. Worker Logic (Child Task or Single Thread)
        String className = null;
        String methodName = null;

        if (sourceClassWithMethod != null) {
            int lastDotIndex = sourceClassWithMethod.lastIndexOf('.');
            if (lastDotIndex > 0) {
                className = sourceClassWithMethod.substring(0, lastDotIndex);
                methodName = sourceClassWithMethod.substring(lastDotIndex + 1);
            } else {
                className = sourceClassWithMethod;
                methodName = "goMigration";
            }
        }

        if (workList.getSql_string() != null && !workList.getSql_string().trim().isEmpty()) {
            if (className != null && methodName != null) {
                 executeBatch(schema, workList, pkCol, className, methodName);
            } else {
                 log.warn("[JAVA_STRATEGY] Class/Method not configured correctly. Cannot execute Batch.");
            }
        } else {
             log.info("[JAVA_STRATEGY] No SQL defined. Running in Legacy Mode.");
             executeLegacyMethods(schema, workList);
        }
        
        logEnd(workList.getMig_name(), System.currentTimeMillis());
    }

    private boolean executeKeysetPartitioning(MigrationSchema schema, MigrationList workList, String pkCol, String sourceClassWithMethod) throws Exception {
        if (c.y.mig.util.StringUtil.empty(pkCol)) return false;

        String tableName = null;
        String sqlSource = workList.getSql_string();
        if (sqlSource != null) {
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

        if (c.y.mig.util.StringUtil.empty(tableName)) return false;

        String[] pkCols = pkCol.split(",");
        for(int i=0; i<pkCols.length; i++) pkCols[i] = pkCols[i].trim();

        int chunkSize = workList.getPage_count_per_thread();
        if (chunkSize <= 0) chunkSize = 1000;
        
        log.info("[JAVA_STRATEGY] Fetching Keyset split points for table: {}, chunkSize: {}", tableName, chunkSize);
        List<Map<String, Object>> pivotKeys = fetchPivotKeys(schema.getSource(), tableName, pkCols, chunkSize);
        
        if (pivotKeys.isEmpty()) {
             log.warn("[JAVA_STRATEGY] No data found for partitioning.");
             return false;
        }

        String cls = null;
        String mth = null;
        if (sourceClassWithMethod != null && sourceClassWithMethod.contains(".")) {
            int lastDot = sourceClassWithMethod.lastIndexOf(".");
            cls = sourceClassWithMethod.substring(0, lastDot);
            mth = sourceClassWithMethod.substring(lastDot + 1);
        }

        for (int i = 0; i < pivotKeys.size(); i++) {
            Map<String, Object> startKey = pivotKeys.get(i);
            StringBuilder param = new StringBuilder("KEYSET_START:");
            for (int k = 0; k < pkCols.length; k++) {
                param.append(pkCols[k]).append("=").append(startKey.get(pkCols[k].toUpperCase()));
                if (k < pkCols.length - 1) param.append(",");
            }
            if (cls != null) param.append("#TARGET_CLASS:").append(cls);
            if (mth != null) param.append("#TARGET_METHOD:").append(mth);
            if (pkCol != null) param.append("#PK_COL:").append(pkCol);
            
            MigrationList childTask = new MigrationList();
            childTask.setMig_master(workList.getMig_list_seq());
            childTask.setMig_name(workList.getMig_name() + " - Chunk " + (i+1));
            childTask.setMig_type(workList.getMig_type());
            childTask.setSource_db_alias(workList.getSource_db_alias());
            childTask.setTarget_db_alias(workList.getTarget_db_alias());
            childTask.setSql_string(workList.getSql_string());
            childTask.setThread_use_yn("N");
            childTask.setParam_string(param.toString());
            
            log.info("[JAVA_STRATEGY] Creating Child Task {}: Param={}", i+1, param.toString());
            workerClient.createChildTask(childTask);
        }
        return true;
    }

    private List<Map<String, Object>> fetchPivotKeys(DBConnMaster sourceDs, String tableName, String[] pkCols, int chunkSize) throws SQLException {
        List<Map<String, Object>> keys = new ArrayList<>();
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
            
            String dbType = sourceDs.getDb_type() != null ? sourceDs.getDb_type().toUpperCase() : "";
            if (dbType.contains("MSSQL") || dbType.contains("SQLSERVER") || dbType.contains("POSTGRE")) {
                sql.append(" WHERE rn = 1 OR (rn - 1) % ? = 0 ");
            } else {
                sql.append(" WHERE rn = 1 OR MOD(rn - 1, ?) = 0 ");
            }
            sql.append(" ORDER BY rn ");

            pstmt = conn.prepareStatement(sql.toString());
            pstmt.setInt(1, chunkSize);
            
            rs = pstmt.executeQuery();
            java.sql.ResultSetMetaData meta = rs.getMetaData();
            int colCount = meta.getColumnCount();
            
            while (rs.next()) {
                Map<String, Object> keyMap = new java.util.HashMap<>();
                for (int i = 1; i <= colCount; i++) {
                    keyMap.put(meta.getColumnName(i).toUpperCase(), rs.getObject(i));
                }
                keys.add(keyMap);
            }
        } finally {
            closeResources(rs, pstmt, conn);
        }
        return keys;
    }
    
    private void executeBatch(MigrationSchema schema, MigrationList workList, String pkCol, String className, String methodName) throws Exception {
         Connection conn = null;
         PreparedStatement pstmt = null; 
         ResultSet rs = null;
         
         try {
             conn = getConnection(workList.getSource_db_alias());
             String sql = workList.getSql_string();
             String finalSql = sql;
             
             Map<String, String> allParams = parseParamString(workList.getParam_string());
             Map<String, String> keysetValues = new java.util.HashMap<>();
             
             if (allParams.containsKey("KEYSET_START")) {
                 String content = allParams.get("KEYSET_START");
                 String[] pairs = content.split(",");
                 for(String pair : pairs) {
                     String[] kv = pair.split("=");
                     if(kv.length == 2) keysetValues.put(kv[0].toUpperCase(), kv[1]);
                 }
             }
             
             String[] pkCols = (pkCol != null) ? pkCol.split(",") : new String[0];
             for(int i=0; i<pkCols.length; i++) pkCols[i] = pkCols[i].trim();

             if (!keysetValues.isEmpty() && pkCols.length > 0) {
                 StringBuilder seekSql = new StringBuilder();
                 seekSql.append("SELECT * FROM (").append(sql).append(") T WHERE (");
                 for(int i=0; i<pkCols.length; i++) {
                     seekSql.append(pkCols[i]);
                     if(i < pkCols.length - 1) seekSql.append(", ");
                 }
                 seekSql.append(") >= (");
                 for(int i=0; i<pkCols.length; i++) {
                     seekSql.append("?");
                     if(i < pkCols.length - 1) seekSql.append(", ");
                 }
                 seekSql.append(") ");
                 seekSql.append(" ORDER BY ");
                 for(int i=0; i<pkCols.length; i++) {
                     seekSql.append(pkCols[i]);
                     if(i < pkCols.length - 1) seekSql.append(", ");
                 }
                 int limit = workList.getPage_count_per_thread();
                 if (limit <= 0) limit = 1000;
                 finalSql = applyLimit(seekSql.toString(), limit, workList.getSource_db_type());
             }
             
             log.info("[JAVA_STRATEGY] Executing Keyset Batch SQL: {}", finalSql);
             pstmt = conn.prepareStatement(finalSql);
             
             if (!keysetValues.isEmpty() && pkCols.length > 0) {
                 for(int i=0; i<pkCols.length; i++) {
                     Object val = keysetValues.get(pkCols[i].toUpperCase());
                     log.debug("[JAVA_STRATEGY] Binding Param {}: {}={}", i+1, pkCols[i], val);
                     pstmt.setObject(i+1, val);
                 }
             }
             
             rs = pstmt.executeQuery();
             java.util.List<Map<String, Object>> dataList = new java.util.ArrayList<>();
             int processBatchSize = 1000; 
             java.sql.ResultSetMetaData md = rs.getMetaData();
             int colCount = md.getColumnCount();
             
             int rowCounter = 0;
             while (rs.next()) {
                 Map<String, Object> row = new java.util.HashMap<>();
                 for (int i = 1; i <= colCount; i++) {
                     row.put(md.getColumnLabel(i), rs.getObject(i));
                 }
                 dataList.add(row);
                 rowCounter++;
                 
                 if (dataList.size() >= processBatchSize) {
                     log.info("[JAVA_STRATEGY] Invoking {} with {} rows (Total read: {})", methodName, dataList.size(), rowCounter);
                     executeMethod(className, methodName, schema, workList, null, dataList);
                     dataList.clear();
                 }
             }
             if (!dataList.isEmpty()) {
                 log.info("[JAVA_STRATEGY] Invoking final batch of {} with {} rows (Total read: {})", methodName, dataList.size(), rowCounter);
                 executeMethod(className, methodName, schema, workList, null, dataList);
             } else if (rowCounter == 0) {
                 log.warn("[JAVA_STRATEGY] No rows found for this chunk!");
             }
         } catch (Exception e) {
             log.error("[JAVA_STRATEGY] Error in executeBatch", e);
             throw e;
         } finally {
             closeResources(rs, pstmt, conn);
         }
    }

    protected String applyLimit(String baseQuery, int limit, String dbType) {
        if (c.y.mig.util.StringUtil.empty(dbType)) return baseQuery + " LIMIT " + limit;
        String upperType = dbType.toUpperCase();
        if (upperType.contains("ORACLE")) {
            return "SELECT * FROM ( " + baseQuery + " ) WHERE ROWNUM <= " + limit;
        } else if (upperType.contains("MSSQL") || upperType.contains("SQLSERVER")) {
            if (baseQuery.trim().toUpperCase().startsWith("SELECT")) {
                 return baseQuery.replaceFirst("(?i)SELECT", "SELECT TOP " + limit);
            }
            return "SELECT TOP " + limit + " * FROM ( " + baseQuery + " ) SUB";
        } else {
            return baseQuery + " LIMIT " + limit;
        }
    }
    
    private Connection getConnection(String dbAlias) throws SQLException {
        if (dbAlias == null) throw new SQLException("DB Alias is null");
        java.util.List<c.y.mig.model.DBConnMaster> connections = workerClient.getDBConnections();
        if (connections != null) {
            for (c.y.mig.model.DBConnMaster db : connections) {
                if (dbAlias.equals(db.getMaster_code())) return dynamicDataSource.getConnection(db);
            }
        }
        throw new SQLException("DB Connection not found for alias: " + dbAlias);
    }

    private void executeLegacyMethods(MigrationSchema schema, MigrationList workList) throws Exception {
        if (schema.getInsertSqlList() != null && !schema.getInsertSqlList().isEmpty()) {
            for(InsertSql insertSql : schema.getInsertSqlList()) {
                 executeMethod(insertSql.getInsert_table(), insertSql.getPk_column(), schema, workList, insertSql, null);
            }
        } else if (workList.getParam_string() != null && !workList.getParam_string().isEmpty()) {
             String[] parts = workList.getParam_string().split("#");
             if (parts.length == 2) {
                 executeMethod(parts[0].trim(), parts[1].trim(), schema, workList, null, null);
             }
        }

        if (schema.getInsertTableList() != null && !schema.getInsertTableList().isEmpty()) {
            for(InsertTable insertTable : schema.getInsertTableList()) {
                String className = insertTable.getSource_table();
                String methodName = insertTable.getSource_pk();
                if (className != null && !className.isEmpty() && methodName != null && !methodName.isEmpty()) {
                     executeMethod(className, methodName, schema, workList, insertTable, null);
                }
            }
        }
    }

    private void executeMethod(String className, String methodName, MigrationSchema schema, MigrationList workList, Object context, List<Map<String, Object>> dataList) throws Exception {
        if (className == null || className.isEmpty() || methodName == null || methodName.isEmpty()) return;
        log.info("!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
        log.info("[JAVA_STRATEGY] ATTEMPTING TO INVOKE: " + className + "." + methodName + " (DataRows: " + (dataList != null ? dataList.size() : "NULL") + ")");
        log.info("!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
        try {
            Class<?> clazz = Class.forName(className);
            Object instance = clazz.getDeclaredConstructor().newInstance();
            java.lang.reflect.Method method = null;
            boolean executed = false;
            if (dataList != null) {
                try {
                    method = clazz.getMethod(methodName, List.class);
                    method.invoke(instance, dataList);
                    executed = true;
                } catch (NoSuchMethodException e) { /* ignore */ }
            }
            if (!executed) {
                try {
                    method = clazz.getMethod(methodName, MigrationSchema.class, MigrationList.class, Object.class);
                    method.invoke(instance, schema, workList, context);
                    executed = true;
                } catch (NoSuchMethodException e) { /* ignore */ }
            }
            if (!executed) {
                try {
                    method = clazz.getMethod(methodName, MigrationSchema.class, MigrationList.class);
                    method.invoke(instance, schema, workList);
                    executed = true;
                } catch (NoSuchMethodException e) { /* ignore */ }
            }
            if (!executed) {
                try {
                    method = clazz.getMethod(methodName, MigrationList.class);
                    method.invoke(instance, workList);
                    executed = true;
                } catch (NoSuchMethodException e) { /* ignore */ }
            }
            if (!executed && context != null) {
                try {
                    method = clazz.getMethod(methodName, Object.class);
                    method.invoke(instance, context);
                    executed = true;
                } catch (NoSuchMethodException e) { /* ignore */ }
            }
            if (!executed) {
                 try {
                    method = clazz.getMethod(methodName);
                    method.invoke(instance);
                    executed = true;
                } catch (NoSuchMethodException e) { /* ignore */ }
            }
            if (!executed) throw new NoSuchMethodException("No suitable method found for " + methodName + " in " + className);
        } catch (Exception e) {
            log.error("[JAVA_STRATEGY] Error executing java method: " + className + "#" + methodName, e);
            throw e;
        }
    }

    private Map<String, String> parseParamString(String paramStr) {
        Map<String, String> map = new java.util.HashMap<>();
        if (paramStr == null || paramStr.isEmpty()) return map;
        String[] sections = paramStr.split("#");
        for (String section : sections) {
            int colonIdx = section.indexOf(":");
            if (colonIdx > 0) {
                String key = section.substring(0, colonIdx).trim();
                String val = section.substring(colonIdx + 1).trim();
                map.put(key, val);
            }
        }
        return map;
    }
}
