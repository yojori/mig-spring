<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ page import="c.y.mig.util.*" %>
<%@ page import="c.y.mig.manager.*" %>
<%@ page import="c.y.mig.model.*" %>
<%@ page import="java.util.*" %>
<%@include file="/mig/session-admin-check.jsp" %>
<%
    InsertSql search = (InsertSql) RequestUtils.getBean(request, InsertSql.class);
    InsertSqlManager manager = new InsertSqlManager();

    if ("insertAll".equals(search.getMode())) {
        String[] insert_sql_seq = request.getParameterValues("insert_sql_seq");
        String[] insert_type = request.getParameterValues("insert_type");
        String[] insert_table = request.getParameterValues("insert_table");
        String[] pk_column = request.getParameterValues("pk_column");
        String[] truncate_yn = request.getParameterValues("truncate_yn");

        // System.out.println("DEBUG: InsertSql Update - seq_len=" + (insert_sql_seq != null ? insert_sql_seq.length : 0) + " , trunc_len=" + (truncate_yn != null ? truncate_yn.length : 0));

        if (insert_sql_seq != null) {
            java.sql.Connection sourceConn = null;
            try {
                // Fetch MigrationList to get DB Info for PK lookup
                MigrationListManager initialMigManager = new MigrationListManager();
                MigrationList initialMigList = new MigrationList();
                initialMigList.setMig_list_seq(search.getMig_list_seq());
                initialMigList = initialMigManager.find(initialMigList);
                
                String sourceDbType = null;
                boolean isThreadType = false;
                if (initialMigList != null) {
                    isThreadType = "THREAD".equals(initialMigList.getMig_type()) || "THREAD_IDX".equals(initialMigList.getMig_type());
                    if (initialMigList.getSource_db_alias() != null) {
                        c.y.mig.manager.DBConnMasterManager dbm = new c.y.mig.manager.DBConnMasterManager();
                        c.y.mig.model.DBConnMaster dbSearch = new c.y.mig.model.DBConnMaster();
                        dbSearch.setMaster_code(initialMigList.getSource_db_alias());
                        c.y.mig.model.DBConnMaster sourceMaster = dbm.find(dbSearch);
                        if (sourceMaster != null) {
                            try {
                                sourceConn = c.y.mig.db.DBManager.getConnection(sourceMaster);
                                sourceDbType = sourceMaster.getDb_type();
                            } catch (Exception e) {}
                        }
                    }
                }
                c.y.mig.service.MigrationMetadataService metaService = new c.y.mig.service.MigrationMetadataService();

                for (int i = 0; i < insert_sql_seq.length; i++) {
                    String pk = pk_column[i];
                    if (isThreadType && c.y.mig.util.StringUtil.empty(pk) && sourceConn != null && !c.y.mig.util.StringUtil.empty(insert_table[i])) {
                        pk = metaService.fetchPrimaryKey(sourceConn, insert_table[i], sourceDbType);
                        if (pk == null) pk = "";
                        pk_column[i] = pk;
                    }

                    InsertSql entity = new InsertSql();
                    entity.setMig_list_seq(search.getMig_list_seq());
                    entity.setInsert_type(insert_type[i]);
                    entity.setInsert_table(insert_table[i]);
                    entity.setPk_column(pk);
                    entity.setTruncate_yn("N");

                    if (truncate_yn != null && i < truncate_yn.length && "Y".equals(truncate_yn[i])) {
                        entity.setTruncate_yn("Y");
                    }

                    if (insert_table[i] == null || insert_table[i].length() == 0) {
                        continue;
                    } else if (insert_sql_seq[i] == null || insert_sql_seq[i].length() == 0) {
                        // insert
                        entity.setInsert_sql_seq(Config.getOrdNoSequence("IS"));
                        entity.setCreate_date(new Date());
                        entity.setUpdate_date(new Date());
                        manager.insert(entity);
                    } else if (insert_sql_seq[i] != null && insert_sql_seq[i].length() > 0) {
                        // update
                        entity.setInsert_sql_seq(insert_sql_seq[i]);
                        entity.setUpdate_date(new Date());
                        manager.update(entity);
                    }
                }

                // Sync with first valid entry in MigrationList
                for (int i = 0; i < insert_table.length; i++) {
                    if (insert_table[i] != null && insert_table[i].length() > 0) {
                        MigrationListManager migManager = new MigrationListManager();
                        MigrationList migList = new MigrationList();
                        migList.setMig_list_seq(search.getMig_list_seq());
                        migList = migManager.find(migList);
                        if (migList != null) {
                            migList.setTarget_table(insert_table[i]);
                            migList.setSource_pk(pk_column[i]);
                            migList.setInsert_type(insert_type[i]);
                            migList.setTruncate_yn( (truncate_yn != null && i < truncate_yn.length && "Y".equals(truncate_yn[i])) ? "Y" : "N" );
                            migList.setUpdate_date(new Date());
                            migManager.update(migList);
                        }
                        break;
                    }
                }
            } finally {
                c.y.mig.db.DBManager.close(null, null, sourceConn);
            }
        }
    } else if ("autoInsert".equals(search.getMode())) {
        if (request.getParameter("auto_insert_sql_seq") != null) {
            search.setInsert_sql_seq(request.getParameter("auto_insert_sql_seq"));
            search.setInsert_table(request.getParameter("auto_insert_table"));

            String rtn = manager.autoInsert(search);

            if (rtn != null && rtn.length() > 0) {
                out.println(rtn);
                return;
            }
        }
    } else if ("goDelete".equals(search.getMode())) {
        if (request.getParameter("auto_insert_sql_seq") != null) {
            search.setInsert_sql_seq(request.getParameter("auto_insert_sql_seq"));
            manager.goDelete(search);
        }
    }

    List<InsertSql> list = manager.getList(search, InterfaceManager.LIST);

    request.setAttribute("search", search);
    request.setAttribute("list", list);

    MigrationListManager migManager = new MigrationListManager();
    MigrationList migList = new MigrationList();
    migList.setMig_list_seq(search.getMig_list_seq());
    migList = migManager.find(migList);
    request.setAttribute("migList", migList);

    request.getRequestDispatcher("./insert-sql-list-fwd.jsp").forward(request, response);
%>
