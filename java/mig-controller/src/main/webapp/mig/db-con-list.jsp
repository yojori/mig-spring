<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ page import="c.y.mig.util.*" %>
<%@ page import="c.y.mig.manager.*" %>
<%@ page import="c.y.mig.model.*" %>
<%@ page import="java.util.*" %>
<%@include file="/mig/session-admin-check.jsp" %>
<%
    DBConnMaster master = (DBConnMaster) RequestUtils.getBean(request, DBConnMaster.class);
    DBConnMasterManager manager = new DBConnMasterManager();
    List<DBConnMaster> list = manager.getList(master, InterfaceManager.LIST);

    request.setAttribute("search", master);
    request.setAttribute("list", list);

    request.getRequestDispatcher("./db-con-list-fwd.jsp").forward(request, response);
%>