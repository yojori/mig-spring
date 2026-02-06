<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ page import="com.yojori.util.*" %>
<%@ page import="com.yojori.manager.*" %>
<%@ page import="com.yojori.migration.controller.model.*" %>
<%@ page import="java.util.*" %>
<%@include file="/mig/session-admin-check.jsp" %>
<%  
    SelectColumn search = (SelectColumn) RequestUtils.getBean(request, SelectColumn.class);

    SelectColumnManager manager = new SelectColumnManager();
    
    String rtn = "";
    
    if ("auto".equals(search.getMode())) {
    	rtn = manager.autoInsert(search);
    }
    
    if (rtn != null && rtn.length() > 0) {
    	out.println(rtn);
    	return;
    }
    
    List<SelectColumn> list = manager.getList(search, InterfaceManager.LIST);
    
    request.setAttribute("search", search);
    request.setAttribute("list", list);
    
    request.getRequestDispatcher("./select-column-list-fwd.jsp").forward(request, response);
%>