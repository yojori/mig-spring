<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ page import="com.yojori.util.*" %>
<%@ page import="com.yojori.manager.*" %>
<%@ page import="com.yojori.migration.controller.model.*" %>
<%@ page import="java.util.*" %>
<%@include file="/mig/session-admin-check.jsp" %>
<%
    MigrationList master = (MigrationList) RequestUtils.getBean(request, MigrationList.class);
    MigrationListManager manager = new MigrationListManager(); 

    if ("update".equals(master.getMode())) {
    	master.setUpdate_date(new Date());
    	
        manager.update(master);	
    } else {
    	master.setMig_list_seq(Config.getOrdNoSequence("ML"));
    	
    	master.setCreate_date(new Date());
    	master.setUpdate_date(new Date());
    	
    	manager.insert(master);
    }
    
    // Process Table Bulk Insert if type is TABLE or JAVA
    if ("TABLE".equals(master.getMig_type()) || "JAVA".equals(master.getMig_type())) {
        String source_table_str = request.getParameter("source_table_area");
        String source_pk_str = request.getParameter("source_pk_area");
        String target_table_str = request.getParameter("target_table_area");
        String truncate_yn = request.getParameter("truncate_yn_area");
        
        if (source_table_str != null && !source_table_str.trim().isEmpty()) {
            InsertTableManager itManager = new InsertTableManager();
            String[] source_table = source_table_str.split("\r\n");
            // Handle PK split gracefully even if lengths mismatch or empty
            String[] source_pk = (source_pk_str != null) ? source_pk_str.split("\r\n") : new String[0];
            String[] target_table = (target_table_str != null) ? target_table_str.split("\r\n") : new String[0];
            
            for (int i = 0; i < source_table.length; i++) {
                String sTable = source_table[i].trim();
                if (sTable.isEmpty()) continue;
                
                InsertTable entity = new InsertTable();
                entity.setMig_list_seq(master.getMig_list_seq());
                entity.setSource_table(sTable);
                
                if (i < source_pk.length) entity.setSource_pk(source_pk[i].trim());
                if (i < target_table.length) entity.setTarget_table(target_table[i].trim());
                else entity.setTarget_table(sTable); // Default to source table name if target not specified
                
                entity.setTruncate_yn((truncate_yn != null && "Y".equals(truncate_yn)) ? "Y" : "N");
                entity.setInsert_table_seq(Config.getOrdNoSequence("IT"));
                entity.setCreate_date(new Date());
                entity.setUpdate_date(new Date());
                
                itManager.insert(entity);
            }
        }
    }
%>
<script>
	opener.location.href = "/mig/migration-list.jsp?mig_master=<%=master.getMig_master()%>";		
<%
	if("TABLE".equals(master.getMig_type()))
	{
%>
	location.href = "/mig/insert-table-write.jsp?mig_list_seq=<%=master.getMig_list_seq()%>";
<%
	}
	else if("JAVA".equals(master.getMig_type()))
	{
%>
	location.href = "/mig/insert-sql-list.jsp?mig_list_seq=<%=master.getMig_list_seq()%>";
<%
	}
	else
	{
%>
	location.href = "/mig/select-column-list.jsp?mig_list_seq=<%=master.getMig_list_seq()%>";
<%
	}
%>
</script>