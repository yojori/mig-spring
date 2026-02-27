<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ page import="c.y.mig.util.*" %>
<%@ page import="c.y.mig.manager.*" %>
<%@ page import="c.y.mig.model.*" %>
<%@ page import="c.y.mig.service.*" %>
<%@ page import="java.util.*" %>
<%@include file="/mig/session-admin-check.jsp" %>
<%
    MigrationList master = (MigrationList) RequestUtils.getBean(request, MigrationList.class);
    MigrationListManager manager = new MigrationListManager();
    MigrationRegistrationService registrationService = new MigrationRegistrationService();
    MigrationMetadataService metadataService = new MigrationMetadataService();

    String sourceTableArea = request.getParameter("source_table_area");
    String sourcePkArea = request.getParameter("source_pk_area");
    String targetTableArea = request.getParameter("target_table_area");
    String truncateYnArea = request.getParameter("truncate_yn"); // Changed from truncate_yn_area
    String targetStrategy = request.getParameter("target_strategy");

    boolean isBulk = !StringUtil.empty(sourceTableArea);
    String mode = master.getMode();
    if (StringUtil.empty(mode)) mode = "insert"; // Default to insert if missing
    
    // For DDL type, we save the table list in sql_string if it's a new registration or update
    if ("DDL".equals(master.getMig_type()) && !StringUtil.empty(sourceTableArea)) {
        master.setSql_string(sourceTableArea);
    }

    // 1. Bulk Table Registration (Create multiple MigrationList entries)
    if ("insert".equals(mode) && isBulk && ("TABLE".equals(master.getMig_type()) || "DDL".equals(master.getMig_type()) || "JAVA".equals(master.getMig_type()))) {
        List<String> seqs = registrationService.bulkInsertTableTasks(master, sourceTableArea, sourcePkArea, targetTableArea, truncateYnArea, targetStrategy);
        if (seqs != null && !seqs.isEmpty()) {
            master.setMig_list_seq(seqs.get(0)); // Set first one for redirection
        }
    }
    // 2. Standard Single Registration or Update
    else {
        if ("update".equals(mode)) {
            master.setUpdate_date(new Date());
            manager.update(master);

            // Update truncate_yn, table names, and pk in detail tables
            if ("NORMAL".equals(master.getMig_type())) {
                InsertSqlManager ism = new InsertSqlManager();
                InsertSql search = new InsertSql();
                search.setMig_list_seq(master.getMig_list_seq());
                List<InsertSql> list = ism.getList(search, 0);
                for (InsertSql is : list) {
                    is.setTruncate_yn(master.getTruncate_yn());
                    is.setInsert_table(master.getTarget_table());
                    is.setPk_column(master.getSource_pk());
                    is.setUpdate_date(new Date());
                    ism.update(is);
                }
            } else if ("TABLE".equals(master.getMig_type()) || "DDL".equals(master.getMig_type())) {
                InsertTableManager itm = new InsertTableManager();
                InsertTable search = new InsertTable();
                search.setMig_list_seq(master.getMig_list_seq());
                List<InsertTable> list = itm.getList(search, 0);
                for (InsertTable it : list) {
                    it.setTruncate_yn(master.getTruncate_yn());
                    it.setSource_table(master.getSource_table());
                    it.setTarget_table(master.getTarget_table());
                    it.setSource_pk(master.getSource_pk());
                    it.setUpdate_date(new Date());
                    itm.update(it);
                }
                
                // For TABLE/DDL, sql_string should be kept in sync with source_table if changed
                if (!StringUtil.empty(master.getSource_table())) {
                    master.setSql_string(master.getSource_table());
                    manager.update(master); // Re-save master with updated sql_string
                }
            }
        } else {
            master.setMig_list_seq(Config.getOrdNoSequence("ML"));
            master.setCreate_date(new Date());
            master.setUpdate_date(new Date());
            manager.insert(master);

            // Auto register columns for single insert (SQL type etc.)
            metadataService.autoRegisterColumns(master);
        }
    }
%>
<script>
    if (window.opener && !window.opener.closed) {
        opener.location.href = "/mig/migration-list.jsp?mig_master=<%=master.getMig_master()%>";
        self.close();
    } else {
        location.href = "/mig/migration-list.jsp?mig_master=<%=master.getMig_master()%>";
    }
</script>

