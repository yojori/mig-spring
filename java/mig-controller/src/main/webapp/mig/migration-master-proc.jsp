<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ page import="com.yojori.util.*" %>
<%@ page import="com.yojori.manager.*" %>
<%@ page import="com.yojori.migration.controller.model.*" %>
<%@ page import="java.util.*" %>
<%@include file="/mig/session-admin-check.jsp" %>
<%
    MigrationMaster master = (MigrationMaster) RequestUtils.getBean(request, MigrationMaster.class);
	MigrationMasterManager manager = new MigrationMasterManager(); 

    if ("update".equals(master.getMode())) {
    	master.setUpdate_date(new Date());
    	
        manager.update(master);	
    } else {
    	master.setMaster_code(Config.getOrdNoSequence("MM"));
    	
    	master.setCreate_date(new Date());
    	master.setUpdate_date(new Date());
    	
    	manager.insert(master);
    }
    
    response.sendRedirect("/mig/migration-master-list.jsp");
%>