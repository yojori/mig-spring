<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ page import="c.y.mig.util.*" %>
<%@ page import="c.y.mig.manager.*" %>
<%@ page import="c.y.mig.model.*" %>
<%@ page import="java.util.*" %>
<%@include file="/mig/session-admin-check.jsp" %>
<%
    MigrationList master = (MigrationList) RequestUtils.getBean(request, MigrationList.class);
    MigrationListManager manager = new MigrationListManager();

    String sourceTableArea = request.getParameter("source_table_area");
    String sourcePkArea = request.getParameter("source_pk_area");
    String targetTableArea = request.getParameter("target_table_area");
    String truncateYnArea = request.getParameter("truncate_yn_area");

    // 1. Bulk Table Registration (Create multiple MigrationList entries)
    if ("insert".equals(master.getMode()) && "TABLE".equals(master.getMig_type()) && !StringUtil.empty(sourceTableArea)) {
        List<String> seqs = manager.bulkInsertTableTasks(master, sourceTableArea, sourcePkArea, targetTableArea, truncateYnArea);
        if (seqs != null && !seqs.isEmpty()) {
            master.setMig_list_seq(seqs.get(0)); // Set first one for redirection
        }
    }
    // 2. Standard Single Registration or Update
    else {
        if ("update".equals(master.getMode())) {
            master.setUpdate_date(new Date());
            manager.update(master);
        } else {
            master.setMig_list_seq(Config.getOrdNoSequence("ML"));
            master.setCreate_date(new Date());
            master.setUpdate_date(new Date());
            manager.insert(master);

            // Auto register columns for single insert (SQL type etc.)
            manager.autoRegisterColumns(master);
        }

        // 3. Handle InsertTable bulk creation for JAVA type (Legacy logic preserved)
        if ("JAVA".equals(master.getMig_type()) && !StringUtil.empty(sourceTableArea)) {
            InsertTableManager itm = new InsertTableManager();
            String[] sourceArr = sourceTableArea.split("\r\n");
            String[] pkArr = (sourcePkArea != null) ? sourcePkArea.split("\r\n") : new String[0];
            String[] targetArr = (targetTableArea != null) ? targetTableArea.split("\r\n") : new String[0];

            for (int i = 0; i < sourceArr.length; i++) {
                String sTable = sourceArr[i].trim();
                if (sTable.isEmpty()) continue;
                InsertTable entity = new InsertTable();
                entity.setMig_list_seq(master.getMig_list_seq());
                entity.setSource_table(sTable);
                if (i < pkArr.length) entity.setSource_pk(pkArr[i].trim());
                if (i < targetArr.length) entity.setTarget_table(targetArr[i].trim());
                else entity.setTarget_table(sTable);
                entity.setTruncate_yn("Y".equals(truncateYnArea) ? "Y" : "N");
                entity.setInsert_table_seq(Config.getOrdNoSequence("IT"));
                entity.setCreate_date(new Date());
                entity.setUpdate_date(new Date());
                itm.insert(entity);
            }
        }
    }
%>
<script>
    opener.location.href = "/mig/migration-list.jsp?mig_master=<%=master.getMig_master()%>";
<%
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
%>
</script>