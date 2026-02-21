<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ page import="c.y.mig.util.*" %>
<%@ page import="c.y.mig.manager.*" %>
<%@ page import="c.y.mig.model.*" %>
<%@ page import="java.util.*" %>
<%@include file="/mig/session-admin-check.jsp" %>
<%
    MigrationMaster master = (MigrationMaster) RequestUtils.getBean(request, MigrationMaster.class);

    // 검색 기본값을 표시로
    if (request.getParameter("display_yn") == null) {
        master.setDisplay_yn("Y");
    }

    MigrationMasterManager manager = new MigrationMasterManager();
    List<MigrationMaster> list = manager.getList(master, InterfaceManager.LIST);

    request.setAttribute("search", master);
    request.setAttribute("list", list);

    request.getRequestDispatcher("./migration-master-list-fwd.jsp").forward(request, response);
%>