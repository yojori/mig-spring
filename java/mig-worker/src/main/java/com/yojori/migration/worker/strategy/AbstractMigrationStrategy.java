package com.yojori.migration.worker.strategy;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.yojori.migration.worker.model.MigrationList;
import com.yojori.migration.worker.model.MigrationSchema;
import com.yojori.migration.worker.service.DynamicDataSource;

public abstract class AbstractMigrationStrategy implements MigrationStrategy {
    
    protected final Logger log = LoggerFactory.getLogger(this.getClass());

    @Autowired
    protected DynamicDataSource dynamicDataSource; // To connect to Source/Target

    @Override
    public abstract void execute(MigrationSchema schema, MigrationList workList, ProgressListener listener) throws Exception;

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

    protected String buildTargetQuery(com.yojori.migration.worker.model.InsertSql iSql, java.util.List<com.yojori.migration.worker.model.InsertColumn> columns) {
        if ("INSERT".equalsIgnoreCase(iSql.getInsert_type())) {
            com.yojori.db.query.Insert insert = new com.yojori.db.query.Insert();
            insert.addFrom(iSql.getInsert_table());
            for (com.yojori.migration.worker.model.InsertColumn col : columns) {
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
            for (com.yojori.migration.worker.model.InsertColumn col : columns) {
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

    protected void setTargetParams(java.sql.PreparedStatement pstmt, com.yojori.migration.worker.model.InsertSql iSql, java.util.List<com.yojori.migration.worker.model.InsertColumn> columns, java.util.Map<String, Object> row) throws Exception {
        int pIdx = 1;
        for (com.yojori.migration.worker.model.InsertColumn col : columns) {
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
