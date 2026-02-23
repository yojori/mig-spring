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
    String truncateYnArea = request.getParameter("truncate_yn_area");
    String targetStrategy = request.getParameter("target_strategy");

    boolean isBulk = !StringUtil.empty(sourceTableArea);
    String mode = master.getMode();
    if (StringUtil.empty(mode)) mode = "insert"; // Default to insert if missing

    // 1. Bulk Table Registration (Create multiple MigrationList entries)
    if ("insert".equals(mode) && isBulk && ("TABLE".equals(master.getMig_type()) || "JAVA".equals(master.getMig_type()))) {
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
    opener.location.href = "/mig/migration-list.jsp?mig_master=<%=master.getMig_master()%>";
<%
    if (isBulk) {
%>
        self.close();
<%
    } else {
        if ("TABLE".equals(master.getMig_type())) {
%>
            location.href = "/mig/insert-table-write.jsp?mig_list_seq=<%=master.getMig_list_seq()%>";
<%
        } else if ("JAVA".equals(master.getMig_type())) {
%>
            location.href = "/mig/insert-sql-list.jsp?mig_list_seq=<%=master.getMig_list_seq()%>";
<%
        } else {
%>
            location.href = "/mig/select-column-list.jsp?mig_list_seq=<%=master.getMig_list_seq()%>";
<%
        }
    }
%>
</script>