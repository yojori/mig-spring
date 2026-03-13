<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ page import="c.y.mig.manager.*" %>
<%@ page import="c.y.mig.model.*" %>
<%@ page import="c.y.mig.util.*" %>
<%@ page import="java.util.*" %>
<%@include file="/mig/session-admin-check.jsp" %>
<%
    String mode = StringUtil.nvl(request.getParameter("mode"), "insert");
    String id = request.getParameter("id");

    KfkParamTemplate master = new KfkParamTemplate();
    master.setMode(mode);

    if ("update".equals(mode) && !StringUtil.empty(id)) {
        KfkParamTemplateManager manager = new KfkParamTemplateManager();
        master = manager.find(id);
        if (master != null) {
            master.setMode(mode);
        } else {
            master = new KfkParamTemplate();
            master.setMode("insert");
        }
    }

    request.setAttribute("master", master);
    request.getRequestDispatcher("./kfk-param-template-write-fwd.jsp").forward(request, response);
%>
