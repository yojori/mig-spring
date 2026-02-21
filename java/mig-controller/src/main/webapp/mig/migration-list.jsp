<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ page import="c.y.mig.util.*" %>
<%@ page import="c.y.mig.model.*" %>
<%@ page import="c.y.mig.manager.MigrationListManager" %>
<%@ page import="c.y.mig.manager.MigrationMasterManager" %>
<%@ page import="c.y.mig.manager.InterfaceManager" %>
<%@ page import="java.util.*" %>
<%@include file="/mig/session-admin-check.jsp" %>
<%
    MigrationList search = (MigrationList) RequestUtils.getBean(request, MigrationList.class);

    // 검색
    if (request.getParameter("display_yn") == null) {
        search.setDisplay_yn("Y");
    }

    MigrationMaster master = new MigrationMaster();
    master.setMaster_code(search.getMig_master());

    MigrationListManager manager = new MigrationListManager();
    MigrationMasterManager manager1 = new MigrationMasterManager();
    List<MigrationList> list = manager.getList(search, InterfaceManager.LIST);

    master = manager1.find(master);

    request.setAttribute("search", search);
    request.setAttribute("master", master);
    request.setAttribute("list", list);

    request.getRequestDispatcher("./migration-list-fwd.jsp").forward(request, response);
%>