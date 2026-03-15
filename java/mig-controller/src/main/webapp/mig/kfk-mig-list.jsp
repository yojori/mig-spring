<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ page import="c.y.mig.util.*" %>
<%@ page import="c.y.mig.manager.*" %>
<%@ page import="c.y.mig.model.*" %>
<%@ page import="java.util.*" %>
<%@include file="/mig/session-admin-check.jsp" %>
<%
    String mig_master = request.getParameter("mig_master");

    KfkMigListManager manager = new KfkMigListManager();
    List<KfkMigList> list = manager.getList(mig_master);

    request.setAttribute("mig_master", mig_master);
    request.setAttribute("list", list);

    request.getRequestDispatcher("./kfk-mig-list-fwd.jsp").forward(request, response);
%>
