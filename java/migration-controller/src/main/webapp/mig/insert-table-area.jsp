<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ page import="com.yojori.util.*" %>
<%@ page import="com.yojori.manager.*" %>
<%@ page import="com.yojori.migration.controller.model.*" %>
<%@ page import="java.util.*" %>
<%@include file="/mig/session-admin-check.jsp" %>
<%  
    InsertTable search = (InsertTable) RequestUtils.getBean(request, InsertTable.class);
    InsertTableManager manager = new InsertTableManager();
    
    if ("insertAll".equals(search.getMode())) {
        String source_table_str = request.getParameter("source_table");
        String source_pk_str = request.getParameter("source_pk");
        String target_table_str = request.getParameter("target_table");    	
    	
    	String truncate_yn = request.getParameter("truncate_yn");    	
    	
    	String[] source_table = source_table_str.split("\r\n");
    	String[] source_pk = source_pk_str.split("\r\n");
    	String[] target_table = target_table_str.split("\r\n");    	
    	
    	for (int i = 0; i < source_table.length; i++) {
    	    InsertTable entity = new InsertTable();
    	    
    	    entity.setMig_list_seq(search.getMig_list_seq());
    	    entity.setSource_table(source_table[i]);
    	    entity.setSource_pk(source_pk[i]);    	    
    	    entity.setTarget_table(target_table[i]);    	    
    	    
    	    if (entity.getSource_table() == null || "".equals(entity.getSource_table())) {
    	    	continue;
    	    }
    	    
    	    if (entity.getSource_pk() == null || "".equals(entity.getSource_pk())) {
                continue;
            }
            
    	    if (entity.getTarget_table() == null || "".equals(entity.getTarget_table())) {
                continue;
            }
            
            entity.setTruncate_yn(truncate_yn);
    	    	entity.setInsert_table_seq(Config.getOrdNoSequence("IT"));
   	    	
   	    	entity.setCreate_date(new Date());
   	    	entity.setUpdate_date(new Date());
   	    	
   	        manager.insert(entity);
    	}
    }
    
    request.setAttribute("search", search);
   
    request.getRequestDispatcher("./insert-table-area-fwd.jsp").forward(request, response);
%>
