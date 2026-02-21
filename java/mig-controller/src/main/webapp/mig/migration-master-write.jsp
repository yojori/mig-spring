<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ page import="c.y.mig.util.*" %>
<%@ page import="c.y.mig.manager.*" %>
<%@ page import="c.y.mig.model.*" %>
<%@ page import="java.util.*" %>
<%@include file="/mig/session-admin-check.jsp" %>
<%
    MigrationMaster master = (MigrationMaster) RequestUtils.getBean(request, MigrationMaster.class);
    MigrationMasterManager manager = new MigrationMasterManager();

    if ("update".equals(master.getMode()) && master.getMaster_code() != null) {
        master = manager.find(master);
    }

    request.setAttribute("master", master);
    request.getRequestDispatcher("./migration-master-write-fwd.jsp").forward(request, response);
%>