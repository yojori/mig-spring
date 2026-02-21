package c.y.mig.manager;

import c.y.mig.db.DBManager;
import c.y.mig.db.query.Insert;
import c.y.mig.db.query.Select;
import c.y.mig.db.query.Update;
import c.y.mig.model.MigrationList;
import c.y.mig.model.InsertTable;
import c.y.mig.model.InsertSql;
import c.y.mig.model.InsertColumn;
import c.y.mig.model.SelectColumn;
import c.y.mig.model.DBConnMaster;
import c.y.mig.util.StringUtil;
import c.y.mig.util.Config;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Date;

public class MigrationListManager extends Manager {

    private static final Logger log = LoggerFactory.getLogger(MigrationListManager.class);


    // Removed legacy threading logic (NormalMigration, ThreadTableMigration)
    // Controller only handles CRUD and Task Queueing.

    private void buildListQuery(MigrationList master) {

        Select sql = new Select();

        sql.addField("A.mig_list_seq");
        sql.addField("A.mig_master");
        sql.addField("A.mig_name");
        sql.addField("A.mig_type");
        sql.addField("A.thread_use_yn");
        sql.addField("A.thread_count");
        sql.addField("A.page_count_per_thread");
        sql.addField("A.sql_string");
        sql.addField("A.ordering");
        sql.addField("A.execute_yn");

        sql.addField("A.source_db_alias");
        sql.addField("A.target_db_alias");

        sql.addField("B.db_type as source_db_type");
        sql.addField("C.db_type as target_db_type");

        sql.addField("A.create_date");
        sql.addField("A.update_date");

        sql.addField("A.display_yn");

        sql.addFrom(MIGRATION_LIST + " A");
        sql.addFrom(DB_MASTER + " B");
        sql.addFrom(DB_MASTER + " C");

        sql.addWhere("A.source_db_alias = B.master_code");
        sql.addWhere("A.target_db_alias = C.master_code");

        sql.addWhere("mig_master = ? ", master.getMig_master());

        if (!StringUtil.empty(master.getDisplay_yn())) {
            sql.addWhere("display_yn = ?", master.getDisplay_yn());
        }

        if (!StringUtil.empty(master.getExecute_yn())) {
            sql.addWhere("execute_yn = ?", master.getExecute_yn());
        }

        sql.addOrder("A.ordering desc");

        setListQuery(sql);
    }

    public MigrationList findReadyTask() {
        MigrationList search = new MigrationList();
        search.setDisplay_yn("Y");
        search.setExecute_yn("Y");
        List<MigrationList> candidates = getList(search, Integer.MAX_VALUE); // Get all candidates

        if (candidates != null && !candidates.isEmpty()) {
            return candidates.get(0);
        }
        return null;
    }

    private void buildCountQuery(MigrationList master) {

        Select sql = new Select();

        sql.addField("COUNT(mig_list_seq)");
        sql.addFrom(MIGRATION_LIST);

        if (!StringUtil.empty(master.getDisplay_yn())) {
            sql.addWhere("display_yn = ?", master.getDisplay_yn());
        }

        setCountQuery(sql);
    }

    public int findMax(MigrationList master) {
        int max = 0;
        Connection con = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;

        try {
            con = DBManager.getConnection();
            Select sql = new Select();
            sql.addField("MAX(ordering)");
            sql.addFrom(MIGRATION_LIST);
            sql.addWhere("mig_master = ?", master.getMig_master());

            stmt = con.prepareStatement(sql.toQuery());
            setParameter(sql, stmt);
            rs = stmt.executeQuery();

            if (rs.next()) {
                max = rs.getInt(1);
            }
        } catch (SQLException e) {
            log.error(e.toString(), e);
        } catch (Exception e) {
            log.error(e.toString(), e);
        } finally {
            DBManager.close(rs, stmt, con);
        }
        return max;
    }

    public List<MigrationList> getList(MigrationList master, int PAGE_GUBUN) {

        List<MigrationList> list = new ArrayList<MigrationList>();

        Connection con = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;

        try {
            con = DBManager.getConnection();

            setForm(master);
            setPageGubun(PAGE_GUBUN);
            buildCountQuery(master);
            buildListQuery(master);

            if (getPageGubun() == InterfaceManager.PAGE) {
                stmt = con.prepareStatement(getCountQuery().toQuery());
                setParameter(getCountQuery(), stmt);
                rs = stmt.executeQuery();
                if (rs.next())
                    master.setTotalCount(rs.getInt(1));
                DBManager.close(rs, stmt, null);
            }

            if (master.getPageSize() > 0) {
                stmt = con.prepareStatement(getListQueryString());

                setParameter(getListQuery(), stmt, getPageGubun());

                rs = stmt.executeQuery();
                int i = 0;
                MigrationList entity = null;
                while (rs.next()) {
                    entity = new MigrationList();
                    entity.setListIndex(
                            ((master.getTotalCount() - ((master.getCurrentPage() - 1) * master.getPageSize())) - i));
                    entity.setMig_list_seq(rs.getString("mig_list_seq"));
                    entity.setMig_master(rs.getString("mig_master"));
                    entity.setMig_name(rs.getString("mig_name"));
                    entity.setMig_type(rs.getString("mig_type"));
                    entity.setThread_use_yn(rs.getString("thread_use_yn"));
                    entity.setThread_count(rs.getInt("thread_count"));
                    entity.setPage_count_per_thread(rs.getInt("page_count_per_thread"));
                    entity.setSql_string(rs.getString("sql_string"));
                    entity.setOrdering(rs.getInt("ordering"));
                    entity.setExecute_yn(rs.getString("execute_yn"));
                    entity.setSource_db_alias(rs.getString("source_db_alias"));
                    entity.setTarget_db_alias(rs.getString("target_db_alias"));
                    entity.setSource_db_type(rs.getString("source_db_type"));
                    entity.setTarget_db_type(rs.getString("target_db_type"));
                    entity.setCreate_date(rs.getDate("create_date"));
                    entity.setUpdate_date(rs.getDate("update_date"));
                    entity.setDisplay_yn(rs.getString("display_yn"));
                    list.add(entity);
                    i++;
                }
            }
        } catch (SQLException e) {
            log.error(e.toString(), e);
        } catch (Exception e) {
            log.error(e.toString(), e);
        } finally {
            DBManager.close(rs, stmt, con);
        }

        return list;
    }

    public MigrationList find(MigrationList master) {
        Connection con = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;

        try {
            con = DBManager.getConnection();
            Select select = new Select();
            select.addField("A.*");
            select.addField("B.db_type as source_db_type");
            select.addField("C.db_type as target_db_type");
            select.addFrom(MIGRATION_LIST + " A");
            select.addFrom(DB_MASTER + " B");
            select.addFrom(DB_MASTER + " C");

            select.addWhere("A.source_db_alias = B.master_code");
            select.addWhere("A.target_db_alias = C.master_code");
            select.addWhere("mig_list_seq = ?", master.getMig_list_seq());

            stmt = con.prepareStatement(select.toQuery());
            setParameter(select, stmt);
            rs = stmt.executeQuery();

            if (rs.next()) {
                master.setMig_list_seq(rs.getString("mig_list_seq"));
                master.setMig_master(rs.getString("mig_master"));
                master.setThread_use_yn(rs.getString("thread_use_yn"));
                master.setThread_count(rs.getInt("thread_count"));
                master.setPage_count_per_thread(rs.getInt("page_count_per_thread"));
                master.setSql_string(rs.getString("sql_string"));
                master.setOrdering(rs.getInt("ordering"));
                master.setExecute_yn(rs.getString("execute_yn"));
                master.setMig_name(rs.getString("mig_name"));
                master.setMig_type(rs.getString("mig_type"));
                master.setSource_db_alias(rs.getString("source_db_alias"));
                master.setTarget_db_alias(rs.getString("target_db_alias"));
                master.setSource_db_type(rs.getString("source_db_type"));
                master.setTarget_db_type(rs.getString("target_db_type"));
                master.setCreate_date(rs.getDate("create_date"));
                master.setUpdate_date(rs.getDate("update_date"));
                master.setDisplay_yn(rs.getString("display_yn"));
            } else {
                master = null;
            }
        } catch (SQLException e) {
            log.error(e.toString(), e);
        } catch (Exception e) {
            log.error(e.toString(), e);
        } finally {
            DBManager.close(rs, stmt, con);
        }
        return master;
    }

    public int insert(MigrationList master) {
        int rtn = 0;
        Connection con = null;
        PreparedStatement stmt = null;

        try {
            Insert sql = new Insert();
            sql.addField("mig_list_seq", master.getMig_list_seq());
            sql.addField("mig_master", master.getMig_master());
            sql.addField("thread_use_yn", master.getThread_use_yn());
            sql.addField("thread_count", master.getThread_count());
            sql.addField("page_count_per_thread", master.getPage_count_per_thread());
            sql.addField("sql_string", master.getSql_string());
            sql.addField("execute_yn", master.getExecute_yn());
            sql.addField("ordering", master.getOrdering());
            sql.addField("mig_name", master.getMig_name());
            sql.addField("mig_type", master.getMig_type());
            sql.addField("source_db_alias", master.getSource_db_alias());
            sql.addField("target_db_alias", master.getTarget_db_alias());
            sql.addField("create_date", master.getCreate_date());
            sql.addField("update_date", master.getUpdate_date());
            sql.addField("display_yn", master.getDisplay_yn());
            sql.addFrom(MIGRATION_LIST);

            con = DBManager.getConnection();
            stmt = con.prepareStatement(sql.toQuery());
            setParameter(sql, stmt);
            rtn = stmt.executeUpdate();

        } catch (SQLException e) {
            log.error(e.toString(), e);
        } catch (Exception e) {
            log.error(e.toString(), e);
        } finally {
            DBManager.close(null, stmt, con);
        }
        return rtn;
    }

    public int update(MigrationList master) {
        int rtn = 0;
        Connection con = null;
        PreparedStatement stmt = null;

        try {
            Update sql = new Update();
            sql.addField("mig_master", master.getMig_master());
            sql.addField("thread_use_yn", master.getThread_use_yn());
            sql.addField("thread_count", master.getThread_count());
            sql.addField("page_count_per_thread", master.getPage_count_per_thread());
            sql.addField("sql_string", master.getSql_string());
            sql.addField("execute_yn", master.getExecute_yn());
            sql.addField("ordering", master.getOrdering());
            sql.addField("mig_name", master.getMig_name());
            sql.addField("mig_type", master.getMig_type());
            sql.addField("source_db_alias", master.getSource_db_alias());
            sql.addField("target_db_alias", master.getTarget_db_alias());
            sql.addField("update_date", master.getUpdate_date());
            sql.addField("display_yn", master.getDisplay_yn());
            sql.addFrom(MIGRATION_LIST);
            sql.addWhere("mig_list_seq = ", master.getMig_list_seq());

            con = DBManager.getConnection();
            stmt = con.prepareStatement(sql.toQuery());
            setParameter(sql, stmt);
            rtn = stmt.executeUpdate();

        } catch (SQLException e) {
            log.error(e.toString(), e);
        } catch (Exception e) {
            log.error(e.toString(), e);
        } finally {
            DBManager.close(null, stmt, con);
        }
        return rtn;
    }

    // Simplified/Restored tableCreate for legacy support
    public void tableCreate(List<InsertTable> list) {
        long startTime = System.currentTimeMillis();
        log.info("tableCreate Start");

        Connection sourceConn = null;
        Connection targetConn = null;
        PreparedStatement sourcePstmt = null;
        PreparedStatement targetPstmt = null;
        ResultSet sourceRs = null;

        try {
            if (list == null || list.isEmpty()) return;

            MigrationList ml = new MigrationList();
            ml.setMig_list_seq(list.get(0).getMig_list_seq());
            ml = find(ml);

            if (ml == null) {
                log.error("MigrationList not found for seq: " + list.get(0).getMig_list_seq());
                return;
            }

            DBConnMasterManager dbm = new DBConnMasterManager();
            DBConnMaster sourceMasterKey = new DBConnMaster();
            sourceMasterKey.setMaster_code(ml.getSource_db_alias());
            DBConnMaster sourceMaster = dbm.find(sourceMasterKey);

            DBConnMaster targetMasterKey = new DBConnMaster();
            targetMasterKey.setMaster_code(ml.getTarget_db_alias());
            DBConnMaster targetMaster = dbm.find(targetMasterKey);

            sourceConn = DBManager.getConnection(sourceMaster);
            targetConn = DBManager.getConnection(targetMaster);

            for (InsertTable insertTable : list) {
                Select select = new Select();
                select.addField(" * ");
                select.addFrom(insertTable.getSource_table());

                if (insertTable.getSource_pk() != null && insertTable.getSource_pk().length() > 0) {
                    select.addOrder(insertTable.getSource_pk());
                }

                String sql = getRownum1Sql(select.toQuery(), ml.getSource_db_type());

                log.info("\n select sql : " + sql);
                sourcePstmt = sourceConn.prepareStatement(sql);
                sourceRs = sourcePstmt.executeQuery();

                ResultSetMetaData rsmd = sourceRs.getMetaData();
                int columnCount = rsmd.getColumnCount();
                StringBuilder sb = new StringBuilder(1024);

                if (columnCount > 0) {
                    sb.append("Create table ").append(insertTable.getTarget_table()).append(" ( \n");
                }

                Map<String, String> typeMap = getTypeMap(ml.getTarget_db_type()); // Need to implement or access type mapping

                for (int index = 1; index <= columnCount; index++) {
                    if (index > 1) sb.append("\n, ");
                    String columnName = rsmd.getColumnLabel(index);
                    int columnTypeInt = rsmd.getColumnType(index);
                    int precision = rsmd.getPrecision(index);
                    int display_size = rsmd.getColumnDisplaySize(index);
                    int scale = rsmd.getScale(index);

                    if (precision > display_size) precision = display_size;

                    // Oracle specific adjustment
                    if("oracle".equals(ml.getSource_db_type())) {
                       if(columnTypeInt == 93 && precision == 0 && scale == 0) columnTypeInt = 91;
                    } else if("maria".equals(ml.getSource_db_type())) {
                       if(precision == 65535) { columnTypeInt = 2005; precision = 0; scale = 0; }
                       else if(columnTypeInt == 93) { precision = 0; scale = 0; }
                       else if(columnTypeInt == 91) { precision = 0; scale = 0; }
                    } else if("mssql".equals(ml.getSource_db_type())) {
                       if(columnTypeInt == 91) { precision = 0; scale = 0; }
                       else if(columnTypeInt == 93) { precision = 0; scale = 0; }
                    }

                    String columnType = typeMap.get(columnTypeInt + "_" + ml.getTarget_db_type());
                    if(columnType == null) columnType = "VARCHAR(255) /*UNKNOWN*/"; // Fallback

                    sb.append(columnName).append(" ").append(columnType);

                    if (precision > 0) {
                        if (scale == 0) sb.append("( ").append(precision).append(" )");
                        else sb.append("( ").append(precision).append(",").append(scale).append(" )");
                    } else if (precision == 0 && scale > 0) {
                        sb.append("( ").append(scale).append(" )");
                    }
                }
                sb.append("\n ) ");
                log.info("\n" + sb.toString() + "\n");

                try {
                    targetPstmt = targetConn.prepareStatement(sb.toString());
                    targetPstmt.execute();
                    targetPstmt.close();
                } catch (Exception e) {
                   log.error("Failed to create table: " + insertTable.getTarget_table() + " - " + e.getMessage());
                }
                
                sourceRs.close();
                sourcePstmt.close();
            }

        } catch (Exception e) {
            log.error(e.toString(), e);
        } finally {
            DBManager.close(sourceRs, sourcePstmt, sourceConn);
            DBManager.close(null, targetPstmt, targetConn);
        }
    }

    private String getRownum1Sql(String sql_string, String dbType) {
        String rtn = "";
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
    
    // Simplistic Type Map based on legacy Manager.java (Hardcoded for now as quick fix)
    private Map<String, String> getTypeMap(String dbType) {
        java.util.Map<String, String> typeMap = new java.util.HashMap<>();
        // Populate standard types (truncated list for brevity, focusing on common types)
        typeMap.put("12_oracle", "VARCHAR2"); typeMap.put("12_mysql", "VARCHAR"); typeMap.put("12_maria", "VARCHAR"); typeMap.put("12_mssql", "VARCHAR");
        typeMap.put("4_oracle", "NUMBER"); typeMap.put("4_mysql", "INT"); typeMap.put("4_maria", "INT"); typeMap.put("4_mssql", "INT");
        typeMap.put("-5_oracle", "NUMBER"); typeMap.put("-5_mysql", "BIGINT"); typeMap.put("-5_maria", "BIGINT"); typeMap.put("-5_mssql", "BIGINT");
        typeMap.put("93_oracle", "TIMESTAMP"); typeMap.put("93_mysql", "TIMESTAMP"); typeMap.put("93_maria", "TIMESTAMP"); typeMap.put("93_mssql", "DATETIME");
        typeMap.put("91_oracle", "DATE"); typeMap.put("91_mysql", "DATE"); typeMap.put("91_maria", "DATE"); typeMap.put("91_mssql", "DATE");
        
        // PostgreSQL mappings
        typeMap.put("12_postgresql", "VARCHAR");
        typeMap.put("4_postgresql", "INTEGER");
        typeMap.put("-5_postgresql", "BIGINT");
        typeMap.put("93_postgresql", "TIMESTAMP");
        typeMap.put("91_postgresql", "DATE");
        // Add more as needed or move to Manager.java properly if this list is incomplete for user's DB
        return typeMap;
    }

    public void autoRegisterColumns(MigrationList ml) {
        log.info("Auto-registering columns for: " + ml.getMig_list_seq() + " (" + ml.getMig_type() + ")");
        
        Connection sourceConn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;

        try {
            DBConnMasterManager dbm_local = new DBConnMasterManager();
            DBConnMaster sourceMasterKey = new DBConnMaster();
            sourceMasterKey.setMaster_code(ml.getSource_db_alias());
            DBConnMaster sourceMaster = dbm_local.find(sourceMasterKey);
            sourceConn = DBManager.getConnection(sourceMaster);
            
            String baseQuery = "";
            if ("TABLE".equals(ml.getMig_type())) {
                // For TABLE type, we need to find the InsertTable entry to know the source table
                InsertTableManager itm = new InsertTableManager();
                InsertTable itSearch = new InsertTable();
                itSearch.setMig_list_seq(ml.getMig_list_seq());
                List<InsertTable> itList = itm.getList(itSearch, InterfaceManager.LIST);
                if (itList != null && !itList.isEmpty()) {
                    baseQuery = "SELECT * FROM " + itList.get(0).getSource_table();
                } else {
                    log.warn("No InsertTable found for TABLE type migration: " + ml.getMig_list_seq());
                    return;
                }
            } else {
                baseQuery = ml.getSql_string();
            }

            if (StringUtil.empty(baseQuery)) {
                log.warn("Base query is empty for autoRegisterColumns. Task: " + ml.getMig_list_seq());
                return;
            }

            String sql = getRownum1Sql(baseQuery, ml.getSource_db_type());
            log.info("Executing metadata query: " + sql);
            
            stmt = sourceConn.prepareStatement(sql);
            rs = stmt.executeQuery();
            ResultSetMetaData meta = rs.getMetaData();
            int columnCount = meta.getColumnCount();

            // 1. Register Select Columns
            SelectColumnManager scm = new SelectColumnManager();
            // Optional: Clear existing columns? Usually okay for new registrations.
            
            for (int i = 1; i <= columnCount; i++) {
                SelectColumn col = new SelectColumn();
                col.setColumn_seq(Config.getOrdNoSequence("SC"));
                col.setMig_list_seq(ml.getMig_list_seq());
                col.setColumn_name(meta.getColumnLabel(i).toUpperCase());
                col.setColumn_type(meta.getColumnTypeName(i));
                col.setCreate_date(new Date());
                col.setUpdate_date(new Date());
                col.setOrdering(i * 10);
                scm.insert(col);
            }

            // 2. Register Insert Sql & Columns (One per TABLE or SQL type by default)
            InsertSqlManager ism = new InsertSqlManager();
            InsertColumnManager icm = new InsertColumnManager();
            
            String insertTableName = "";
            if ("TABLE".equals(ml.getMig_type())) {
                 InsertTableManager itm = new InsertTableManager();
                 InsertTable itSearch = new InsertTable();
                 itSearch.setMig_list_seq(ml.getMig_list_seq());
                 List<InsertTable> itList = itm.getList(itSearch, InterfaceManager.LIST);
                 if (itList != null && !itList.isEmpty()) {
                     insertTableName = itList.get(0).getTarget_table();
                 }
            }
            
            if (StringUtil.empty(insertTableName)) {
                insertTableName = "TARGET_TABLE_PLACEHOLDER";
            }

            InsertSql is = new InsertSql();
            String insertSqlSeq = Config.getOrdNoSequence("IS");
            is.setInsert_sql_seq(insertSqlSeq);
            is.setMig_list_seq(ml.getMig_list_seq());
            is.setInsert_type("INSERT");
            is.setInsert_table(insertTableName);
            is.setOrdering(10);
            is.setCreate_date(new Date());
            is.setUpdate_date(new Date());
            is.setTruncate_yn("N");
            ism.insert(is);

            for (int i = 1; i <= columnCount; i++) {
                InsertColumn col = new InsertColumn();
                col.setInsert_column_seq(Config.getOrdNoSequence("IC"));
                col.setInsert_sql_seq(insertSqlSeq);
                col.setColumn_name(meta.getColumnLabel(i).toUpperCase());
                col.setColumn_type(meta.getColumnTypeName(i));
                col.setInsert_data(meta.getColumnLabel(i).toUpperCase());
                col.setInsert_value("");
                col.setCreate_date(new Date());
                col.setUpdate_date(new Date());
                icm.insert(col);
            }

        } catch (Exception e) {
            log.error("Failed autoRegisterColumns for " + ml.getMig_list_seq(), e);
        } finally {
            DBManager.close(rs, stmt, sourceConn);
        }
    }

    public List<String> bulkInsertTableTasks(MigrationList master, String sourceTableArea, String sourcePkArea, String targetTableArea, String truncateYn) {
        List<String> createdSeqs = new ArrayList<String>();
        if (StringUtil.empty(sourceTableArea)) return createdSeqs;

        String[] source_table = sourceTableArea.split("\r\n");
        String[] source_pk = (sourcePkArea != null) ? sourcePkArea.split("\r\n") : new String[0];
        String[] target_table = (targetTableArea != null) ? targetTableArea.split("\r\n") : new String[0];

        int threadCount = (master.getThread_count() <= 0) ? 1 : master.getThread_count();

        for (int i = 0; i < source_table.length; i++) {
            String sTable = source_table[i].trim();
            if (sTable.isEmpty()) continue;

            String tTable = (i < target_table.length && !target_table[i].trim().isEmpty()) ? target_table[i].trim() : sTable;
            String sPk = (i < source_pk.length) ? source_pk[i].trim() : "";

            for (int t = 0; t < threadCount; t++) {
                MigrationList ml = new MigrationList();
                // Copy properties from master
                ml.setMig_master(master.getMig_master());
                ml.setSource_db_alias(master.getSource_db_alias());
                ml.setTarget_db_alias(master.getTarget_db_alias());
                ml.setMig_type(master.getMig_type());
                ml.setThread_use_yn(threadCount > 1 ? "Y" : "N");
                ml.setThread_count(threadCount);
                ml.setPage_count_per_thread(master.getPage_count_per_thread());
                ml.setExecute_yn(master.getExecute_yn());
                ml.setDisplay_yn(master.getDisplay_yn());
                ml.setOrdering(master.getOrdering());
                
                String taskName = sTable;
                if (threadCount > 1) {
                    taskName += " (" + (t + 1) + "/" + threadCount + ")";
                    ml.setParam_string("thread_count:" + threadCount + ";thread_idx:" + t);
                }
                ml.setMig_name(taskName);
                String newSeq = Config.getOrdNoSequence("ML");
                ml.setMig_list_seq(newSeq);
                ml.setCreate_date(new Date());
                ml.setUpdate_date(new Date());
                
                this.insert(ml);
                
                // Create InsertTable entry
                InsertTableManager itm = new InsertTableManager();
                InsertTable it = new InsertTable();
                it.setInsert_table_seq(Config.getOrdNoSequence("IT"));
                it.setMig_list_seq(ml.getMig_list_seq());
                it.setSource_table(sTable);
                it.setSource_pk(sPk);
                it.setTarget_table(tTable);
                it.setTruncate_yn((truncateYn != null && "Y".equals(truncateYn)) ? "Y" : "N");
                it.setCreate_date(new Date());
                it.setUpdate_date(new Date());
                itm.insert(it);
                
                // Auto-register columns
                this.autoRegisterColumns(ml);
                
                createdSeqs.add(newSeq);
            }
        }
        return createdSeqs;
    }
}
