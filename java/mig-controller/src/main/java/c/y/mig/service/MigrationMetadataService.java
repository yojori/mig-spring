package c.y.mig.service;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import c.y.mig.model.InsertTable;
import c.y.mig.manager.InsertTableManager;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import c.y.mig.db.DBManager;
import c.y.mig.manager.DBConnMasterManager;
import c.y.mig.manager.InsertColumnManager;
import c.y.mig.manager.InsertSqlManager;
import c.y.mig.manager.SelectColumnManager;
import c.y.mig.model.DBConnMaster;
import c.y.mig.model.InsertColumn;
import c.y.mig.model.InsertSql;
import c.y.mig.model.MigrationList;
import c.y.mig.model.SelectColumn;
import c.y.mig.util.StringUtil;

/**
 * Service for handling DB Metadata retrieval and automated column registration.
 */
public class MigrationMetadataService {

    private static final Logger log = LoggerFactory.getLogger(MigrationMetadataService.class);

    /**
     * Automatically registers source and target columns for a migration task.
     */
    public void autoRegisterColumns(MigrationList ml) {
        log.info("Auto-registering columns for: " + ml.getMig_list_seq() + " (" + ml.getMig_type() + ")");
        
        Connection sourceConn = null;
        Connection targetConn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;

        try {
            DBConnMasterManager dbm_local = new DBConnMasterManager();
            
            // 1. Get Source Connection
            DBConnMaster sourceMasterKey = new DBConnMaster();
            sourceMasterKey.setMaster_code(ml.getSource_db_alias());
            DBConnMaster sourceMaster = dbm_local.find(sourceMasterKey);
            sourceConn = DBManager.getConnection(sourceMaster);
            
            // 2. Get Target Connection
            DBConnMaster targetMasterKey = new DBConnMaster();
            targetMasterKey.setMaster_code(ml.getTarget_db_alias());
            DBConnMaster targetMaster = dbm_local.find(targetMasterKey);
            
            String sourceDbType = (sourceMaster != null) ? sourceMaster.getDb_type() : ml.getSource_db_type();
            String targetDbType = (targetMaster != null) ? targetMaster.getDb_type() : ml.getTarget_db_type();

            log.info("Source DB Alias: {}, Type: {}", ml.getSource_db_alias(), sourceDbType);
            log.info("Target DB Alias: {}, Type: {}", ml.getTarget_db_alias(), targetDbType);

            try {
                targetConn = DBManager.getConnection(targetMaster);
            } catch (Exception e) {
                log.warn("Could not connect to target DB for metadata: " + ml.getTarget_db_alias());
            }

            String sourceBaseQuery = "";
            String targetTableName = "";
            
            if (!StringUtil.empty(ml.getSql_string())) {
                sourceBaseQuery = ml.getSql_string();
                // Target table is stored in mig_name (set during bulk registration)
                targetTableName = !StringUtil.empty(ml.getMig_name()) ? ml.getMig_name() : "TARGET_TABLE_PLACEHOLDER";
                log.info("Using sql_string: [{}], Target (from mig_name): {}", sourceBaseQuery, targetTableName);
            }

            if (StringUtil.empty(sourceBaseQuery)) {
                log.warn("No source query found for autoRegisterColumns. Task: " + ml.getMig_list_seq());
                return;
            }

            if (sourceConn == null) {
                log.error("Source connection is null for Task: {}. Alias: {}", ml.getMig_list_seq(), ml.getSource_db_alias());
                // Continue to IS creation if possible, but columns won't be mapped.
            }

            // 3. Fetch Source Metadata
            ResultSetMetaData sourceMeta = null;
            int sourceColCount = 0;
            if (sourceConn != null) {
                try {
                    String sourceSql = getRownum1Sql(sourceBaseQuery, sourceDbType);
                    log.info("Executing source metadata fetch: [{}]", sourceSql);
                    stmt = sourceConn.prepareStatement(sourceSql);
                    rs = stmt.executeQuery();
                    sourceMeta = rs.getMetaData();
                    sourceColCount = sourceMeta.getColumnCount();
                    log.info("Successfully fetched source metadata. Columns: {}", sourceColCount);
                } catch (Exception e) {
                    log.warn("Source metadata fetch skipped or failed for: {}. (Reason: {})", sourceBaseQuery, e.getMessage());
                }
            } else {
                log.warn("Skipping source metadata fetch due to missing connection. Task: {}", ml.getMig_list_seq());
            }

                // 4. Register Select Columns (Only if metadata exists)
                if (sourceMeta != null) {
                    SelectColumnManager scm = new SelectColumnManager();
                    String baseID = Long.toString(System.currentTimeMillis(), 36);
                    for (int i = 1; i <= sourceColCount; i++) {
                        SelectColumn col = new SelectColumn();
                        col.setColumn_seq("SC-" + baseID + "-" + String.format("%03d", i));
                    col.setMig_list_seq(ml.getMig_list_seq());
                    col.setColumn_name(sourceMeta.getColumnLabel(i).toUpperCase());
                    col.setColumn_type(sourceMeta.getColumnTypeName(i));
                    col.setCreate_date(new Date());
                    col.setUpdate_date(new Date());
                    col.setOrdering(i * 10);
                    scm.insert(col);
                }
            }

            // 5. Fetch Target Metadata (Isolated) - Skip for DDL as target table won't exist yet
            List<String> targetCols = new ArrayList<>();
            if (targetConn != null && !StringUtil.empty(targetTableName) && !"DDL".equals(ml.getMig_type())) {
                try {
                    String targetSql = getRownum1Sql("SELECT * FROM " + targetTableName, targetDbType);
                    log.info("Executing target metadata fetch: [{}]", targetSql);
                    PreparedStatement tStmt = targetConn.prepareStatement(targetSql);
                    ResultSet tRs = tStmt.executeQuery();
                    ResultSetMetaData targetMeta = tRs.getMetaData();
                    for (int i = 1; i <= targetMeta.getColumnCount(); i++) {
                        targetCols.add(targetMeta.getColumnLabel(i).toUpperCase());
                    }
                    DBManager.close(tRs, tStmt, null);
                    log.info("Successfully fetched target metadata for: {}", targetTableName);
                } catch (Exception e) {
                    log.info("Target metadata fetch skipped for table: {}. (Table may not exist yet or is DDL type)", targetTableName);
                }
            }

            // 6. Register Insert Sql & Columns
            InsertSqlManager ism = new InsertSqlManager();
            InsertColumnManager icm = new InsertColumnManager();
            
            InsertSql is = new InsertSql();
            String baseID = Long.toString(System.currentTimeMillis(), 36);
            String insertSqlSeq = "IS-" + baseID + "-01";
            is.setInsert_sql_seq(insertSqlSeq);
            is.setMig_list_seq(ml.getMig_list_seq());
            is.setInsert_type("INSERT");
            is.setInsert_table(targetTableName);
            
            // Populate PK and Truncate directly from ml (no more param_string)
            String pk = ml.getSource_pk();
            if (StringUtil.empty(pk) && sourceConn != null && ("TABLE".equals(ml.getMig_type()) || "DDL".equals(ml.getMig_type()))) {
                pk = fetchPrimaryKey(sourceConn, sourceBaseQuery, sourceDbType);
            }
            String trunc = ml.getTruncate_yn() != null ? ml.getTruncate_yn() : "N";
            
            log.info("Mapping InsertSql: PK={}, Truncate={}", pk, trunc);
            
            is.setPk_column(pk);
            is.setTruncate_yn(trunc);
            
            is.setOrdering(10);
            is.setCreate_date(new Date());
            is.setUpdate_date(new Date());
            ism.insert(is);

            // 7. Register Insert Table for TABLE/DDL types
            if ("TABLE".equals(ml.getMig_type()) || "DDL".equals(ml.getMig_type())) {
                InsertTableManager itm = new InsertTableManager();
                InsertTable it = new InsertTable();
                it.setMig_list_seq(ml.getMig_list_seq());
                it.setSource_table(ml.getSql_string()); // For TABLE/DDL, sql_string is the source table
                it.setTarget_table(targetTableName);
                it.setSource_pk(pk);
                it.setTruncate_yn(trunc);
                it.setCreate_date(new Date());
                it.setUpdate_date(new Date());
                itm.insert(it);
                log.info("Registered InsertTable for Task: {}", ml.getMig_list_seq());
            }

            // Register columns only if source metadata was successful
            if (sourceMeta != null) {
                String baseID_SC = Long.toString(System.currentTimeMillis(), 36);
                for (int i = 1; i <= sourceColCount; i++) {
                    String colName = sourceMeta.getColumnLabel(i).toUpperCase();
                    
                    InsertColumn ic = new InsertColumn();
                    ic.setInsert_column_seq("IC-" + baseID_SC + "-" + String.format("%03d", i));
                    ic.setInsert_sql_seq(insertSqlSeq);
                    ic.setColumn_name(colName);
                    ic.setColumn_type(sourceMeta.getColumnTypeName(i));
                    
                    if (targetCols.contains(colName)) {
                        ic.setInsert_data(colName);
                    }
                    
                    ic.setCreate_date(new Date());
                    ic.setUpdate_date(new Date());
                    icm.insert(ic);
                }
            } else {
                log.warn("InsertColumns NOT registered due to missing source metadata. Task: {}", ml.getMig_list_seq());
            }

        } catch (SQLException e) {
            log.error("Global SQLException in autoRegisterColumns: " + e.getMessage(), e);
        } catch (Exception e) {
            log.error("Global Exception in autoRegisterColumns: " + e.getMessage(), e);
        } finally {
            DBManager.close(rs, stmt, sourceConn);
            DBManager.close(null, null, targetConn);
        }
    }

    /**
     * Helper to wrap query with rownum=1 equivalent based on DB type.
     */
    public String getRownum1Sql(String sql_string, String dbType) {
        String rtn = sql_string;
        if (dbType == null) dbType = "mysql";
        
        if ("mysql".equals(dbType)) {
            rtn = sql_string + " Limit 0, 1";
        } else if ("maria".equals(dbType)) {
            rtn = sql_string + " Limit 1 OFFSET 0";
        } else if ("mssql".equals(dbType)) {
            String temp = sql_string.toUpperCase();
            int idx = temp.lastIndexOf("ORDER BY");
            if (idx > 0) {
                rtn = "SELECT TOP 1 A.* FROM ( " + sql_string.substring(0, idx) + " ) A";
            } else {
                rtn = "SELECT TOP 1 A.* FROM ( " + sql_string + " ) A";
            }
        } else if ("oracle".equals(dbType)) {
            rtn = "SELECT * FROM ( " + sql_string + " ) WHERE  ROWNUM = 1";
        } else if ("postgresql".equals(dbType)) {
            rtn = sql_string + " Limit 1 OFFSET 0";
        }
        return rtn;
    }


    /**
     * Helper to fetch Primary Key column names from database metadata.
     */
    private String fetchPrimaryKey(Connection conn, String fullTableName, String dbType) {
        if (conn == null || StringUtil.empty(fullTableName)) return null;
        
        String catalog = null;
        String schema = null;
        String table = fullTableName;
        
        if (fullTableName.contains(".")) {
            String[] parts = fullTableName.split("\\.");
            if (parts.length == 2) {
                schema = parts[0];
                table = parts[1];
            } else if (parts.length == 3) {
                catalog = parts[0];
                schema = parts[1];
                table = parts[2];
            }
        }
        
        // Remove quotes if present
        table = table.replace("\"", "").replace("[", "").replace("]", "").replace("`", "");
        if (schema != null) schema = schema.replace("\"", "").replace("[", "").replace("]", "").replace("`", "");
        
        List<String> pks = new ArrayList<>();
        ResultSet rs = null;
        try {
            // Some DBs need exact casing (Oracle usually UPPER, etc.)
            rs = conn.getMetaData().getPrimaryKeys(catalog, schema, table);
            while (rs.next()) {
                pks.add(rs.getString("COLUMN_NAME"));
            }
            
            if (pks.isEmpty()) {
                rs = conn.getMetaData().getPrimaryKeys(catalog, (schema != null ? schema.toUpperCase() : null), table.toUpperCase());
                while (rs.next()) {
                    pks.add(rs.getString("COLUMN_NAME"));
                }
            }
        } catch (SQLException e) {
            log.warn("Failed to fetch primary keys for {}: {}", fullTableName, e.getMessage());
        } finally {
            DBManager.close(rs, null, null);
        }
        
        return pks.isEmpty() ? null : String.join(",", pks);
    }
}
