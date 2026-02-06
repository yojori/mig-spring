<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ page import="com.yojori.util.*" %>
<%@ page import="com.yojori.manager.*" %>
<%@ page import="com.yojori.migration.controller.model.DBConnMaster" %>
<%@ page import="java.util.*" %>
<%@include file="/mig/session-admin-check.jsp" %>
<%
    DBConnMaster master = (DBConnMaster)RequestUtils.getBean(request, DBConnMaster.class);
    DBConnMasterManager manager = new DBConnMasterManager(); 

    if("update".equals(master.getMode()))
    {
    	master.setUpdate_date(new Date());
    	
        manager.update(master);	
    }
    else
    {
    	master.setCreate_date(new Date());
    	master.setUpdate_date(new Date());
    	
    	manager.insert(master);
    }
    
    response.sendRedirect("/mig/db-con-list.jsp");
%>
