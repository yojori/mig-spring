package com.yojori.migration.worker.strategy;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.yojori.migration.worker.service.DynamicDataSource;
import com.yojori.model.InsertColumn;
import com.yojori.model.InsertSql;
import com.yojori.model.InsertTable;
import com.yojori.model.MigrationList;
import com.yojori.model.MigrationSchema;

public abstract class AbstractMigrationStrategy implements MigrationStrategy {
    
    protected final Logger log = LoggerFactory.getLogger(this.getClass());

    @Autowired
    protected DynamicDataSource dynamicDataSource; // To connect to Source/Target

    @Override
    public abstract void execute(MigrationSchema schema, MigrationList workList, ProgressListener listener) throws Exception;

    @Override
    public void prepare(MigrationSchema schema, MigrationList workList) throws Exception {
        // 1. Detect Child Task (Chunk)
        // If param_string contains chunk-specific info, we skip truncation.
        String param = com.yojori.util.StringUtil.nvl(workList.getParam_string());
        if (param.contains("MIN_PK") || param.contains("KEYSET_START") || param.contains("MAX_PK")) {
            log.info("[PREPARE] Skipping Truncate for Child Task (Chunk): {}", workList.getMig_name());
            return;
        }

        // 2. Perform Truncate (Master Task)
        java.sql.Connection targetConn = null;
        try {
            targetConn = dynamicDataSource.getConnection(schema.getTarget());
            java.util.Set<String> truncatedTables = new java.util.HashSet<>();
            
            // Check Tables List
            java.util.List<InsertTable> tables = schema.getInsertTableList();
            if (tables != null && !tables.isEmpty()) {
                for (InsertTable t : tables) {
                    if ("Y".equalsIgnoreCase(com.yojori.util.StringUtil.nvl(t.getTruncate_yn()))) {
                        String tableName = t.getTarget_table();
                        if (tableName != null && !truncatedTables.contains(tableName)) {
                            log.info("[PREPARE] Truncating Table (from Tables): {}", tableName);
                            executeTruncate(targetConn, tableName);
                            truncatedTables.add(tableName);
                        }
                    }
                }
            }
            
            // Check SQL List
            java.util.List<InsertSql> sqlList = schema.getInsertSqlList();
            if (sqlList != null && !sqlList.isEmpty()) {
                for (InsertSql s : sqlList) {
                    if ("Y".equalsIgnoreCase(com.yojori.util.StringUtil.nvl(s.getTruncate_yn()))) {
                        String tableName = s.getInsert_table();
                        if (tableName != null && !truncatedTables.contains(tableName)) {
                            log.info("[PREPARE] Truncating Table (from SQLs): {}", tableName);
                            executeTruncate(targetConn, tableName);
                            truncatedTables.add(tableName);
                        }
                    }
                }
            }
            
            // Explicitly commit truncate
            if (!targetConn.getAutoCommit()) {
                targetConn.commit();
            }
        } finally {
            closeResources(null, null, targetConn);
        }
    }

    // Common methods can be added here, e.g., tableTruncate, logging helper.
    protected void logStart(String migName) {
        log.info("Starting Migration: " + migName);
    }

    protected void logEnd(String migName, long startTime) {
        log.info("Finished Migration: " + migName + ", Elapsed: " + (System.currentTimeMillis() - startTime) + "ms");
    }

    protected void executeBatch(java.sql.PreparedStatement[] pstmts) throws java.sql.SQLException {
        for (java.sql.PreparedStatement ps : pstmts) {
            if (ps != null) ps.executeBatch();
        }
    }

    protected String buildTargetQuery(InsertSql iSql, java.util.List<InsertColumn> columns) {
        if ("INSERT".equalsIgnoreCase(iSql.getInsert_type())) {
            com.yojori.db.query.Insert insert = new com.yojori.db.query.Insert();
            insert.addFrom(iSql.getInsert_table());
            for (InsertColumn col : columns) {
                if (col.getInsert_sql_seq().equals(iSql.getInsert_sql_seq())) {
                    if ("SQL_FUNC".equals(col.getInsert_data())) {
                        String funcStr = col.getInsert_value();
                        if (funcStr == null) funcStr = "";
                        // Parse {COL} pattern
                        java.util.regex.Pattern p = java.util.regex.Pattern.compile("\\{([^}]+)\\}");
                        java.util.regex.Matcher m = p.matcher(funcStr);
                        java.util.List<String> bindCols = new java.util.ArrayList<>();
                        StringBuffer sb = new StringBuffer();
                        while (m.find()) {
                            bindCols.add(m.group(1));
                            m.appendReplacement(sb, "?");
                        }
                        m.appendTail(sb);
                        col.setSqlFuncBindCols(bindCols);
                        insert.addField(col.getColumn_name(), sb.toString());
                    } else {
                        insert.addField(col.getColumn_name(), "?");
                    }
                }
            }
            return insert.toQuery();
        } else if ("UPDATE".equalsIgnoreCase(iSql.getInsert_type())) {
            com.yojori.db.query.Update update = new com.yojori.db.query.Update();
            update.addFrom(iSql.getInsert_table());
            for (InsertColumn col : columns) {
                if (col.getInsert_sql_seq().equals(iSql.getInsert_sql_seq())) {
                    if (col.getColumn_name().equalsIgnoreCase(iSql.getPk_column())) {
                        update.addWhere(col.getColumn_name() + " = ", "?");
                    } else {
                        if ("SQL_FUNC".equals(col.getInsert_data())) {
                             String funcStr = col.getInsert_value();
                             if (funcStr == null) funcStr = "";
                             // Parse {COL} pattern
                             java.util.regex.Pattern p = java.util.regex.Pattern.compile("\\{([^}]+)\\}");
                             java.util.regex.Matcher m = p.matcher(funcStr);
                             java.util.List<String> bindCols = new java.util.ArrayList<>();
                             StringBuffer sb = new StringBuffer();
                             while (m.find()) {
                                 bindCols.add(m.group(1));
                                 m.appendReplacement(sb, "?");
                             }
                             m.appendTail(sb);
                             col.setSqlFuncBindCols(bindCols);
                             update.addField(col.getColumn_name(), sb.toString());
                        } else {
                            update.addField(col.getColumn_name(), "?");
                        }
                    }
                }
            }
            return update.toQuery();
        }
        return null;
    }

    protected void setTargetParams(java.sql.PreparedStatement pstmt, InsertSql iSql, java.util.List<InsertColumn> columns, java.util.Map<String, Object> row) throws Exception {
        int pIdx = 1;
        for (InsertColumn col : columns) {
            if (col.getInsert_sql_seq().equals(iSql.getInsert_sql_seq())) {
                Object val = null;
                if ("SQL_FUNC".equals(col.getInsert_data())) {
                    if (col.getSqlFuncBindCols() != null) {
                        for (String bindCol : col.getSqlFuncBindCols()) {
                            Object bindVal = row.get(bindCol); 
                            pstmt.setObject(pIdx++, bindVal);
                        }
                    }
                    continue; // Skip standard binding
                } else if ("CURRENT_DATE".equals(col.getInsert_data())) {
                    val = new java.sql.Date(System.currentTimeMillis());
                } else if ("UUID".equals(col.getInsert_data())) {
                    val = com.yojori.util.Config.getUUID();
                } else if ("KEY_IN_VAR".equals(col.getInsert_data())) {
                    val = col.getInsert_value();
                } else if ("KEY_IN_NUM".equals(col.getInsert_data())) {
                    val = Integer.parseInt(col.getInsert_value());
                } else {
                    val = row.get(col.getInsert_data()); 
                }
                pstmt.setObject(pIdx++, val);
            }
        }
    }

    protected void executeTruncate(java.sql.Connection conn, String tableName) throws java.sql.SQLException {
        java.sql.Statement stmt = null;
        try {
            stmt = conn.createStatement();
            stmt.executeUpdate("TRUNCATE TABLE " + tableName);
        } catch (java.sql.SQLException e) {
            log.error("Truncate failed for table: " + tableName, e);
            throw e;
        } finally {
            closeResources(null, stmt, null);
        }
    }

    protected void closeResources(java.sql.ResultSet rs, java.sql.Statement stmt, java.sql.Connection con) {
        try { if (rs != null) rs.close(); } catch (Exception e) {}
        try { if (stmt != null) stmt.close(); } catch (Exception e) {}
        try { if (con != null) con.close(); } catch (Exception e) {}
    }
}
