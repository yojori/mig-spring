<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ page import="c.y.mig.util.*" %>
<%@ page import="c.y.mig.manager.*" %>
<%@ page import="c.y.mig.model.*" %>
<%@ page import="java.util.*" %>
<%@include file="/mig/session-admin-check.jsp" %>
<%
    String mode = StringUtil.defaultIfEmpty(request.getParameter("mode"), "insert");
    String template_id = request.getParameter("template_id");

    KfkTemplate master = new KfkTemplate();
    master.setMode(mode);

    if ("update".equals(mode) && !StringUtil.empty(template_id)) {
        KfkTemplateManager manager = new KfkTemplateManager();
        master = manager.find(template_id);
        if (master != null) {
            master.setMode(mode);
        } else {
            master = new KfkTemplate();
            master.setMode("insert");
        }
    }

    request.setAttribute("master", master);
    request.getRequestDispatcher("./kfk-template-write-fwd.jsp").forward(request, response);
%>
