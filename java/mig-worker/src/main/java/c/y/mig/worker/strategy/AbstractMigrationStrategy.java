package c.y.mig.worker.strategy;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import c.y.mig.model.InsertColumn;
import c.y.mig.model.InsertSql;
import c.y.mig.model.InsertTable;
import c.y.mig.model.MigrationList;
import c.y.mig.model.MigrationSchema;
import c.y.mig.worker.service.DynamicDataSource;

public abstract class AbstractMigrationStrategy implements MigrationStrategy {
    
    protected final Logger log = LoggerFactory.getLogger(this.getClass());

    @Autowired
    protected DynamicDataSource dynamicDataSource; // To connect to Source/Target

    @Override
    public abstract void execute(MigrationSchema schema, MigrationList workList, ProgressListener listener) throws Exception;

    @Override
    public void prepare(MigrationSchema schema, MigrationList workList) throws Exception {
        // 0. Skip for DDL type (handled in execute)
        if ("DDL".equalsIgnoreCase(c.y.mig.util.StringUtil.nvl(workList.getMig_type()))) {
            log.info("[PREPARE] DDL type detected. Skipping Truncate (Drop will be handled in execute).");
            return;
        }

        // 1. Detect Child Task (Chunk)
        // If param_string contains chunk-specific info, we skip truncation.
        String param = c.y.mig.util.StringUtil.nvl(workList.getParam_string());
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
                    if ("Y".equalsIgnoreCase(c.y.mig.util.StringUtil.nvl(t.getTruncate_yn()))) {
                        String tableName = t.getTarget_table();
                        if (tableName != null && !truncatedTables.contains(tableName)) {
                            log.info("[PREPARE] Truncating Table (from Tables): {}", tableName);
                            executeTruncate(targetConn, quoteIdentifier(tableName, schema.getTarget().getDb_type()));
                            truncatedTables.add(tableName);
                        }
                    }
                }
            }
            
            // Check SQL List
            java.util.List<InsertSql> sqlList = schema.getInsertSqlList();
            if (sqlList != null && !sqlList.isEmpty()) {
                for (InsertSql s : sqlList) {
                    if ("Y".equalsIgnoreCase(c.y.mig.util.StringUtil.nvl(s.getTruncate_yn()))) {
                        String tableName = s.getInsert_table();
                        if (tableName != null && !truncatedTables.contains(tableName)) {
                            log.info("[PREPARE] Truncating Table (from SQLs): {}", tableName);
                            executeTruncate(targetConn, quoteIdentifier(tableName, schema.getTarget().getDb_type()));
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


    protected String buildTargetQuery(InsertSql iSql, java.util.List<InsertColumn> columns, String targetDbType) {
        String tableName = quoteIdentifier(iSql.getInsert_table(), targetDbType);

        String resultSql = null;
        if ("INSERT".equalsIgnoreCase(iSql.getInsert_type())) {
            c.y.mig.db.query.Insert insert = new c.y.mig.db.query.Insert();
            insert.addFrom(tableName);
            for (InsertColumn col : columns) {
                String colName = quoteIdentifier(col.getColumn_name(), targetDbType);
                if ("SQL_FUNC".equals(col.getInsert_data())) {
                    String funcStr = col.getInsert_value();
                    if (funcStr == null) funcStr = "";
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
                    insert.addField(colName, sb.toString());
                } else {
                    insert.addField(colName, "?");
                }
            }
            resultSql = insert.toQuery();
        } else if ("UPDATE".equalsIgnoreCase(iSql.getInsert_type())) {
            c.y.mig.db.query.Update update = new c.y.mig.db.query.Update();
            update.addFrom(tableName);
            for (InsertColumn col : columns) {
                String colName = quoteIdentifier(col.getColumn_name(), targetDbType);
                if (col.getColumn_name().equalsIgnoreCase(iSql.getPk_column())) {
                    update.addWhere(colName + " = ", "?");
                } else {
                    if ("SQL_FUNC".equals(col.getInsert_data())) {
                         String funcStr = col.getInsert_value();
                         if (funcStr == null) funcStr = "";
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
                         update.addField(colName, sb.toString());
                    } else {
                        update.addField(colName, "?");
                    }
                }
            }
            resultSql = update.toQuery();
        }
        
        if (resultSql != null) {
            log.info("[TARGET_QUERY] Standardized SQL for {}: {}", targetDbType, resultSql.replace("\n", " ").replaceAll("\\s+", " "));
        }
        return resultSql;
    }

    protected String quoteIdentifier(String id, String dbType) {
        if (id == null || id.trim().isEmpty()) return id;
        
        // Handle cases where the ID might already be quoted partially or fully
        String trimmed = id.trim();
        if (trimmed.startsWith("(") || trimmed.contains(" ") || 
            (trimmed.startsWith("[") && trimmed.endsWith("]")) || 
            (trimmed.startsWith("\"") && trimmed.endsWith("\"")) || 
            (trimmed.startsWith("`") && trimmed.endsWith("`"))) {
            return id;
        }

        // Schema.Table format support
        if (id.contains(".") && !id.contains("[") && !id.contains("\"")) {
            String[] parts = id.split("\\.");
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < parts.length; i++) {
                sb.append(quoteIdentifier(parts[i], dbType));
                if (i < parts.length - 1) sb.append(".");
            }
            return sb.toString();
        }

        if (dbType != null) {
            String lowerType = dbType.toLowerCase();
            if (lowerType.contains("mssql") || lowerType.contains("sqlserver")) {
                return "[" + id + "]";
            }
            if (lowerType.contains("mariadb") || lowerType.contains("mysql") || lowerType.contains("maria")) {
                return "`" + id + "`";
            }
        }
        // PostgreSQL, Oracle, Tibero, etc. (ANSI SQL)
        return "\"" + id + "\"";
    }

    protected void setTargetParams(java.sql.PreparedStatement pstmt, InsertSql iSql, java.util.List<InsertColumn> columns, java.util.Map<String, Object> row) throws Exception {
        int pIdx = 1;
        for (InsertColumn col : columns) {
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
                val = c.y.mig.util.Config.getUUID();
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

    protected void addSeekCriteria(c.y.mig.db.query.Select select, String[] pkCols, java.util.Map<String, Object> lastRow, String dbType) {
        if (lastRow == null || lastRow.isEmpty()) return;
        boolean isMssql = (dbType != null && (dbType.toLowerCase().contains("mssql") || dbType.toLowerCase().contains("sqlserver")));
        
        if (!isMssql && pkCols.length > 1) {
             // Standard Row Constructor: (A, B) >= (?, ?)
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
        } else {
            // Expansion for MSSQL or Single PK
            // (A > ?) OR (A = ? AND B >= ?)
            StringBuilder sb = new StringBuilder();
            if (pkCols.length == 1) {
                sb.append(pkCols[0]).append(" >= ?");
            } else {
                sb.append("(");
                for (int i = 0; i < pkCols.length; i++) {
                    if (i > 0) sb.append(" OR ");
                    sb.append("(");
                    for (int j = 0; j < i; j++) {
                        sb.append(pkCols[j]).append(" = ? AND ");
                    }
                    if (i == pkCols.length - 1) {
                        sb.append(pkCols[i]).append(" >= ?");
                    } else {
                        sb.append(pkCols[i]).append(" > ?");
                    }
                    sb.append(")");
                }
                sb.append(")");
            }
            select.addWhere(sb.toString(), null);
        }
    }

    protected int bindSeekParams(java.sql.PreparedStatement pstmt, String[] pkCols, java.util.Map<String, Object> lastRow, int startIdx, String dbType) throws java.sql.SQLException {
        boolean isMssql = (dbType != null && (dbType.toLowerCase().contains("mssql") || dbType.toLowerCase().contains("sqlserver")));
        int currentParamIdx = startIdx;

        if (!isMssql && pkCols.length > 1) {
            for (String col : pkCols) {
                pstmt.setObject(currentParamIdx++, lastRow.get(col.toUpperCase()));
            }
        } else {
            for (int i = 0; i < pkCols.length; i++) {
                for (int j = 0; j <= i; j++) {
                    pstmt.setObject(currentParamIdx++, lastRow.get(pkCols[j].toUpperCase()));
                }
            }
        }
        return currentParamIdx;
    }

    protected String applyLimit(String baseQuery, int limit, String dbType) {
        if (c.y.mig.util.StringUtil.empty(dbType)) return baseQuery + " LIMIT " + limit;
        String upperType = dbType.toUpperCase();
        if (upperType.contains("ORACLE")) {
            return "SELECT * FROM ( " + baseQuery + " ) WHERE ROWNUM <= " + limit;
        } else if (upperType.contains("MSSQL") || upperType.contains("SQLSERVER")) {
            return "SELECT TOP " + limit + " * FROM ( " + baseQuery + " ) SUB_LIMIT";
        } else {
            return baseQuery + " LIMIT " + limit;
        }
    }
}
