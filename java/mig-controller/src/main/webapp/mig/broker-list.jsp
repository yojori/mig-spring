<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ page import="c.y.mig.manager.*" %>
<%@ page import="java.util.*" %>
<%@include file="/mig/session-admin-check.jsp" %>
<%
    List<Map<String, String>> brokerList = CodeManager.getCodeList("KFK-0030");
    request.setAttribute("brokerList", brokerList);
    request.getRequestDispatcher("./broker-list-fwd.jsp").forward(request, response);
%>
