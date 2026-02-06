<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ page import="com.yojori.util.*" %>
<%@ page import="com.yojori.manager.*" %>
<%@ page import="com.yojori.migration.controller.model.*" %>
<%@ page import="java.util.*" %>
<%@include file="/mig/session-admin-check.jsp" %>
<%  
    InsertColumn search = (InsertColumn) RequestUtils.getBean(request, InsertColumn.class);
    InsertColumnManager manager = new InsertColumnManager();    
    SelectColumnManager sm = new SelectColumnManager();
    
    if ("insertAll".equals(search.getMode())) {
    	String[] insert_column_seq = request.getParameterValues("insert_column_seq");
    	String[] insert_data = request.getParameterValues("insert_data");
    	String[] insert_value = request.getParameterValues("insert_value");
    	
    	for (int i = 0; i < insert_column_seq.length; i++) {
    		InsertColumn insertC = new InsertColumn();
    		
    		insertC.setInsert_column_seq(insert_column_seq[i]);
    		insertC.setInsert_data(insert_data[i]);
    		insertC.setInsert_value(insert_value[i]);
    		insertC.setUpdate_date(new Date());
    		
    		manager.update(insertC);
    	}    	
    } else if ("deleteColumn".equals(search.getMode())) {
    	String deleteColumnSeq = request.getParameter("deleteColumnSeq");    	
    	search.setInsert_column_seq(deleteColumnSeq);    	
    	manager.delete(search);
    	search.setInsert_column_seq("");      
    }
    	
    List<InsertColumn> list = manager.getList(search, InterfaceManager.LIST);
    
    SelectColumn column = new SelectColumn();    
    column.setMig_list_seq(search.getMig_list_seq());
    
    List<SelectColumn> sList = sm.getList(column, InterfaceManager.LIST);
    
    SelectColumn column1 = new SelectColumn();
    SelectColumn column2 = new SelectColumn();
    SelectColumn column3 = new SelectColumn();
    SelectColumn column4 = new SelectColumn();
    
    column1.setColumn_name("CURRENT_DATE");
    column1.setColumn_type("DATE");
    
    column2.setColumn_name("KEY_IN_NUM");
    column2.setColumn_type("NUMBER");
    
    column3.setColumn_name("KEY_IN_VAR");
    column3.setColumn_type("VARCHAR");
    
    column4.setColumn_name("UUID");
    column4.setColumn_type("VARCHAR");
    
    SelectColumn column5 = new SelectColumn();
    column5.setColumn_name("SQL_FUNC");
    column5.setColumn_type("VARCHAR");
    
    sList.add(column1);
    sList.add(column2);
    sList.add(column3);
    sList.add(column4);
    sList.add(column5);
    
    request.setAttribute("search", search);
    request.setAttribute("list", list);
    request.setAttribute("sList", sList);
   
    request.getRequestDispatcher("./insert-column-list-fwd.jsp").forward(request, response);
%>
