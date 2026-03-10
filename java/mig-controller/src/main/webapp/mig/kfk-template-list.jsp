<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ page import="c.y.mig.util.*" %>
<%@ page import="c.y.mig.manager.*" %>
<%@ page import="c.y.mig.model.*" %>
<%@ page import="java.util.*" %>
<%@include file="/mig/session-admin-check.jsp" %>
<%
    KfkTemplate search = (KfkTemplate) RequestUtils.getBean(request, KfkTemplate.class);
    KfkTemplateManager manager = new KfkTemplateManager();
    List<KfkTemplate> list = manager.getList(search);

    request.setAttribute("search", search);
    request.setAttribute("list", list);

    request.getRequestDispatcher("./kfk-template-list-fwd.jsp").forward(request, response);
%>
