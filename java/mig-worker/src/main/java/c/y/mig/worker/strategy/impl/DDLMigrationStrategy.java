package c.y.mig.worker.strategy.impl;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Component;

import c.y.mig.model.MigrationList;
import c.y.mig.model.MigrationSchema;
import c.y.mig.util.StringUtil;
import c.y.mig.worker.strategy.AbstractMigrationStrategy;
import c.y.mig.worker.strategy.ProgressListener;

/**
 * Strategy for automated table creation (DDL) from Source to Target.
 * Expects "SOURCE_TABLE=tableName" in workList.getParam_string().
 */
@Component("DDL")
public class DDLMigrationStrategy extends AbstractMigrationStrategy {

    @org.springframework.beans.factory.annotation.Autowired
    private c.y.mig.worker.client.WorkerClient workerClient;

    private List<c.y.mig.model.TypeMapping> cachedMappings = null;

    @Override
    public void execute(MigrationSchema schema, MigrationList workList, ProgressListener listener) throws Exception {
        // Fetch mappings once per execution
        try {
            cachedMappings = workerClient.getTypeMappings();
            log.info("[DDL] Loaded {} dynamic type mappings from controller.", (cachedMappings != null ? cachedMappings.size() : 0));
        } catch (Exception e) {
            log.warn("[DDL] Failed to load dynamic mappings, falling back to hardcoded logic: {}", e.getMessage());
            cachedMappings = null;
        }
        
        String param = StringUtil.nvl(workList.getParam_string());
        String sourceTable = workList.getSource_table();
        
        if (sourceTable == null || sourceTable.isEmpty()) {
            String sql = workList.getSql_string();
            if (sql != null) {
                String upperSql = sql.toUpperCase();
                if (upperSql.contains("SELECT ") && upperSql.contains("FROM ")) {
                    // Extract from SQL (Legacy or manual entry)
                    int fromIdx = upperSql.lastIndexOf("FROM ");
                    if (fromIdx != -1) {
                        String afterFrom = sql.substring(fromIdx + 5).trim();
                        sourceTable = afterFrom.split("\\s+")[0];
                        log.info("[DDL] Extracted source table [{}] from SQL string.", sourceTable);
                    }
                } else {
                    // Direct table name (New simplified logic)
                    sourceTable = sql.trim();
                }
            }
        }

        if (sourceTable == null || sourceTable.trim().isEmpty()) {
            throw new IllegalArgumentException("Source table name is missing in DDL task.");
        }

        log.info("[DDL] Starting Table Creation for: {}", sourceTable);
        
        Connection sourceConn = null;
        Connection targetConn = null;
        
        try {
            sourceConn = dynamicDataSource.getConnection(schema.getSource());
            targetConn = dynamicDataSource.getConnection(schema.getTarget());
            
            String targetDbType = schema.getTarget().getDb_type().toLowerCase();
            
            String schemaName = null;
            String tableName = sourceTable;
            if (sourceTable.contains(".")) {
                String[] parts = sourceTable.split("\\.");
                schemaName = parts[0];
                tableName = parts[1];
            }

            String targetSchema = null;
            if (param.contains("TARGET_SCHEMA=")) {
                targetSchema = param.split("TARGET_SCHEMA=")[1].split(";")[0].trim();
            }

            String targetTable = workList.getTarget_table();
            boolean isMssql = targetDbType.contains("mssql") || targetDbType.contains("sqlserver");
            boolean isMysql = targetDbType.contains("maria") || targetDbType.contains("mysql");

            if (StringUtil.empty(targetTable)) {
                targetTable = tableName;
                // Schema re-attachment logic (only if targetTable wasn't explicit)
                if (targetSchema != null && !targetSchema.isEmpty()) {
                    targetTable = targetSchema + "." + tableName;
                } else if (schemaName != null) {
                    if (!isMssql && !isMysql) {
                        targetTable = schemaName + "." + tableName;
                    }
                }
            } else if (isMssql || isMysql) {
                // Clean up source schema if it was automatically carried over (e.g. from bulk registration)
                if (schemaName != null && targetTable.startsWith(schemaName + ".")) {
                    String stripped = targetTable.substring(schemaName.length() + 1);
                    if (targetSchema != null && !targetSchema.isEmpty()) {
                        targetTable = targetSchema + "." + stripped;
                    } else {
                        targetTable = stripped;
                    }
                    log.info("[DDL] Adjusted target table [{}] for MSSQL/MySQL (stripped source schema: {}).", targetTable, schemaName);
                }
            }
            
            log.info("[DDL] Target Table Name set to: [{}] for DB Type: [{}]", targetTable, targetDbType);

            // Handle Drop logic if truncate_yn=Y for DDL
            if ("Y".equals(workList.getTruncate_yn())) {
                log.info("[DDL] DROP detected (truncate_yn=Y). Dropping table if exists: {}", targetTable);
                String dropSql = "DROP TABLE IF EXISTS " + quoteIdentifier(targetTable, targetDbType);
                
                if (targetDbType.contains("oracle")) {
                    // Oracle doesn't support IF EXISTS easily in a simple statement
                    dropSql = "BEGIN EXECUTE IMMEDIATE 'DROP TABLE " + quoteIdentifier(targetTable, targetDbType) + "'; EXCEPTION WHEN OTHERS THEN NULL; END;";
                } else if (targetDbType.contains("mssql") || targetDbType.contains("sqlserver")) {
                    // More compatible syntax for various MSSQL versions
                    dropSql = "IF OBJECT_ID('" + targetTable.replace("'", "''") + "', 'U') IS NOT NULL DROP TABLE " + quoteIdentifier(targetTable, targetDbType);
                }
                
                Statement dropStmt = targetConn.createStatement();
                try {
                    dropStmt.execute(dropSql);
                    log.info("[DDL] Successfully dropped (or skipped) table: {}", targetTable);
                } catch (Exception e) {
                    log.warn("[DDL] Drop attempt failed (non-critical): {}", e.getMessage());
                } finally {
                    dropStmt.close();
                }
            }

            DatabaseMetaData metaData = sourceConn.getMetaData();

            log.info("[DDL] Fetching columns for Schema: [{}], Table: [{}]", schemaName, tableName);
            ResultSet rs = metaData.getColumns(null, schemaName, tableName, null);
            List<ColumnInfo> columns = new ArrayList<>();
            
            // Try different combinations if empty
            if (!rs.isBeforeFirst()) {
                rs.close();
                log.info("[DDL] Columns empty, trying as Catalog: [{}], Table: [{}]", schemaName, tableName);
                rs = metaData.getColumns(schemaName, null, tableName, null);
            }
            
            if (!rs.isBeforeFirst()) {
                rs.close();
                log.info("[DDL] Columns empty, trying Uppercase Schema: [{}], Table: [{}]", schemaName != null ? schemaName.toUpperCase() : null, tableName.toUpperCase());
                rs = metaData.getColumns(null, schemaName != null ? schemaName.toUpperCase() : null, tableName.toUpperCase(), null);
            }
            
            if (!rs.isBeforeFirst()) {
                rs.close();
                log.info("[DDL] Columns empty, trying Uppercase Catalog: [{}], Table: [{}]", schemaName != null ? schemaName.toUpperCase() : null, tableName.toUpperCase());
                rs = metaData.getColumns(schemaName != null ? schemaName.toUpperCase() : null, null, tableName.toUpperCase(), null);
            }

            while (rs.next()) {
                ColumnInfo col = new ColumnInfo();
                col.name = rs.getString("COLUMN_NAME");
                col.type = rs.getInt("DATA_TYPE");
                col.typeName = rs.getString("TYPE_NAME");
                col.size = rs.getInt("COLUMN_SIZE");
                col.decimalDigits = rs.getInt("DECIMAL_DIGITS");
                col.nullable = rs.getInt("NULLABLE") == DatabaseMetaData.columnNullable;
                columns.add(col);
            }
            rs.close();

            if (columns.isEmpty()) {
                throw new SQLException("Table not found or no columns retrieved: " + sourceTable);
            }

            log.info("[DDL] Fetching Primary Keys for Schema: [{}], Table: [{}]", schemaName, tableName);
            ResultSet pkRs = metaData.getPrimaryKeys(null, schemaName, tableName);
            if (!pkRs.isBeforeFirst()) {
                pkRs.close();
                pkRs = metaData.getPrimaryKeys(schemaName, null, tableName);
            }
            if (!pkRs.isBeforeFirst()) {
                pkRs.close();
                pkRs = metaData.getPrimaryKeys(null, schemaName != null ? schemaName.toUpperCase() : null, tableName.toUpperCase());
            }
            if (!pkRs.isBeforeFirst()) {
                pkRs.close();
                pkRs = metaData.getPrimaryKeys(schemaName != null ? schemaName.toUpperCase() : null, null, tableName.toUpperCase());
            }
            List<String> pks = new ArrayList<>();
            while (pkRs.next()) {
                pks.add(pkRs.getString("COLUMN_NAME"));
            }
            pkRs.close();

            String ddl = generateCreateSQL(schema, targetTable, columns, pks);
            log.info("[DDL] Generated SQL ({}): {}", targetDbType, ddl);

            Statement stmt = targetConn.createStatement();
            try {
                stmt.execute(ddl);
                log.info("[DDL] Table created successfully: {}", targetTable);
            } catch (SQLException e) {
                if (e.getMessage().contains("already exists") || e.getMessage().contains("이미 존재")) {
                     log.warn("[DDL] Table already exists, skipping: {}", targetTable);
                } else {
                    throw e;
                }
            } finally {
                stmt.close();
            }

            if (listener != null) listener.onProgress(1, 1);

        } finally {
            closeResources(null, null, sourceConn);
            closeResources(null, null, targetConn);
        }
    }

    private String generateCreateSQL(MigrationSchema schema, String table, List<ColumnInfo> columns, List<String> pks) {
        String dbType = schema.getTarget().getDb_type();
        StringBuilder sb = new StringBuilder();
        sb.append("CREATE TABLE ").append(quoteIdentifier(table, dbType)).append(" (\n");

        for (int i = 0; i < columns.size(); i++) {
            ColumnInfo col = columns.get(i);
            sb.append("  ").append(quoteIdentifier(col.name, dbType)).append(" ");
            sb.append(mapDataType(schema, col));
            
            if (!col.nullable) {
                sb.append(" NOT NULL");
            }
            
            if (i < columns.size() - 1 || !pks.isEmpty()) {
                sb.append(",");
            }
            sb.append("\n");
        }

        if (!pks.isEmpty()) {
            sb.append("  PRIMARY KEY (");
            for (int i = 0; i < pks.size(); i++) {
                sb.append(quoteIdentifier(pks.get(i), dbType));
                if (i < pks.size() - 1) sb.append(", ");
            }
            sb.append(")\n");
        }

        sb.append(")");
        return sb.toString();
    }

    private String mapDataType(MigrationSchema schema, ColumnInfo col) {
        String srcDbType = schema.getSource().getDb_type().toLowerCase();
        String tgtDbType = schema.getTarget().getDb_type().toLowerCase();
        String typeName = col.typeName.toUpperCase();
        int jdbcType = col.type;
        int size = col.size;
        int scale = col.decimalDigits;

        // 1. Try Dynamic Mapping from DB
        if (cachedMappings != null) {
            for (c.y.mig.model.TypeMapping m : cachedMappings) {
                if (srcDbType.contains(m.getSrc_db_type().toLowerCase()) &&
                    tgtDbType.contains(m.getTgt_db_type().toLowerCase()) &&
                    typeName.equals(m.getSrc_type_name().toUpperCase())) {
                    
                    String mapped = m.getTgt_type_name();
                    if (mapped.contains("(") || mapped.contains(" ")) return mapped;

                    // Handle types that only take a single precision parameter (fsp) like DATETIME(6), TIMESTAMP(6), TIME(6)
                    String upperMapped = mapped.toUpperCase();
                    if (upperMapped.startsWith("DATETIME") || upperMapped.startsWith("TIMESTAMP") || upperMapped.startsWith("TIME")) {
                        if (scale > 0 && scale <= 7) return mapped + "(" + scale + ")";
                        return mapped;
                    }

                    if (size > 0) {
                        if (scale > 0) return mapped + "(" + size + "," + scale + ")";
                        return mapped + "(" + size + ")";
                    } else if (mapped.equalsIgnoreCase("DECIMAL") || mapped.equalsIgnoreCase("NUMERIC")) {
                        // Default precision for DECIMAL if size is 0
                        return mapped + "(18" + (scale > 0 ? "," + scale : "") + ")";
                    }
                    return mapped;
                }
            }
        }

        // 2. Hardcoded Fallbacks (Postgres)
        if (tgtDbType.contains("postgresql")) {
            if (typeName.contains("VARCHAR2") || typeName.equals("NVARCHAR2")) return "VARCHAR(" + (size > 0 ? size : 255) + ")";
            if (typeName.equals("NUMBER")) {
                if (scale > 0) return "NUMERIC(" + size + "," + scale + ")";
                if (size > 9) return "BIGINT";
                return "INTEGER";
            }
            if (typeName.equals("CLOB")) return "TEXT";
            if (typeName.equals("BLOB")) return "BYTEA";
            if (typeName.equals("DATE") || typeName.contains("TIMESTAMP")) return "TIMESTAMP";

            switch (jdbcType) {
                case Types.VARCHAR: case Types.CHAR: case Types.NVARCHAR: return "VARCHAR(" + (size > 0 ? size : 255) + ")";
                case Types.NUMERIC: case Types.DECIMAL: 
                    int p = (size > 0 ? size : 18);
                    return "NUMERIC(" + p + (scale > 0 ? "," + scale : "") + ")";
                case Types.INTEGER: case Types.SMALLINT: return "INTEGER";
                case Types.BIGINT: return "BIGINT";
                case Types.TIMESTAMP: case Types.DATE: return "TIMESTAMP";
                case Types.CLOB: case Types.LONGVARCHAR: return "TEXT";
                case Types.BLOB: case Types.VARBINARY: case Types.BINARY: return "BYTEA";
            }
        } 
        // 3. Hardcoded Fallbacks (MariaDB/MySQL)
        else if (tgtDbType.contains("maria") || tgtDbType.contains("mysql")) {
            if (size > 16383) return "LONGTEXT"; // MariaDB row size limit for VARCHAR
            
            if (typeName.contains("VARCHAR2") || typeName.equals("NVARCHAR2")) return "VARCHAR(" + (size > 0 ? size : 255) + ")";
            if (typeName.equals("NUMBER")) {
                if (scale > 0) return "DECIMAL(" + (size > 65 ? 65 : size) + "," + scale + ")";
                if (size > 9) return "BIGINT";
                return "INT";
            }
            if (typeName.equals("CLOB")) return "LONGTEXT";
            if (typeName.equals("BLOB")) return "LONGBLOB";
            if (typeName.equals("DATE") || typeName.contains("TIMESTAMP")) return "DATETIME";

            switch (jdbcType) {
                case Types.VARCHAR: case Types.CHAR: case Types.NVARCHAR: return "VARCHAR(" + (size > 0 ? size : 255) + ")";
                case Types.NUMERIC: case Types.DECIMAL: case Types.DOUBLE: case Types.FLOAT:
                    int mp = (size > 0 ? (size > 65 ? 65 : size) : 18);
                    return "DECIMAL(" + mp + (scale > 0 ? "," + scale : "") + ")";
                case Types.INTEGER: case Types.SMALLINT: return "INT";
                case Types.BIGINT: return "BIGINT";
                case Types.TIMESTAMP: case Types.DATE: return "DATETIME";
                case Types.CLOB: case Types.LONGVARCHAR: return "LONGTEXT";
                case Types.BLOB: case Types.VARBINARY: case Types.BINARY: return "LONGBLOB";
            }
        }
        // 4. Hardcoded Fallbacks (MSSQL)
        else if (tgtDbType.contains("mssql") || tgtDbType.contains("sqlserver")) {
            if (typeName.contains("VARCHAR2") || typeName.equals("NVARCHAR2")) return (typeName.startsWith("N") ? "NVARCHAR(" : "VARCHAR(") + (size > 0 ? (size > 8000 ? "MAX" : size) : 255) + ")";
            if (typeName.equals("NUMBER")) {
                if (scale > 0) return "DECIMAL(" + (size > 38 ? 38 : size) + "," + scale + ")";
                if (size > 9) return "BIGINT";
                return "INT";
            }
            if (typeName.equals("CLOB")) return "VARCHAR(MAX)";
            if (typeName.equals("BLOB")) return "VARBINARY(MAX)";
            if (typeName.equals("DATE") || typeName.contains("TIMESTAMP")) return "DATETIME2";

            switch (jdbcType) {
                case Types.VARCHAR: case Types.CHAR: case Types.NVARCHAR: 
                    return (jdbcType == Types.NVARCHAR ? "NVARCHAR(" : "VARCHAR(") + (size > 0 ? (size > 8000 ? "MAX" : size) : 255) + ")";
                case Types.NUMERIC: case Types.DECIMAL: 
                    int msp = (size > 0 ? (size > 38 ? 38 : size) : 18);
                    return "DECIMAL(" + msp + (scale > 0 ? "," + scale : "") + ")";
                case Types.INTEGER: case Types.SMALLINT: return "INT";
                case Types.BIGINT: return "BIGINT";
                case Types.TIMESTAMP: case Types.DATE: return "DATETIME2";
                case Types.CLOB: case Types.LONGVARCHAR: return "VARCHAR(MAX)";
                case Types.BLOB: case Types.VARBINARY: case Types.BINARY: return "VARBINARY(MAX)";
            }
        }
        
        if (size > 0) {
            if (scale > 0) return typeName + "(" + size + "," + scale + ")";
            return typeName + "(" + size + ")";
        }
        return typeName;
    }

    private static class ColumnInfo {
        String name;
        int type;
        String typeName;
        int size;
        int decimalDigits;
        boolean nullable;
    }
}
