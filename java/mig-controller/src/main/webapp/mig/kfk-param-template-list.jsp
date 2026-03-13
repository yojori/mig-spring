<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ page import="c.y.mig.manager.*" %>
<%@ page import="c.y.mig.model.*" %>
<%@ page import="c.y.mig.util.*" %>
<%@ page import="java.util.*" %>
<%@ page import="c.y.mig.manager.KfkParamTemplateManager" %>
<%@include file="/mig/session-admin-check.jsp" %>
<%
    int dp_level = StringUtil.nvl(request.getParameter("dp_level"), -1);
    String par_class_id = request.getParameter("par_class_id");

    KfkParamTemplateManager manager = new KfkParamTemplateManager();
    // Fetching all for the management list, or filtered if needed
    // For now, let's fetch by level if provided, else all (need a getListAll in manager)
    List<KfkParamTemplate> list = manager.getList(dp_level, par_class_id); 
    
    request.setAttribute("list", list);
    request.getRequestDispatcher("./kfk-param-template-list-fwd.jsp").forward(request, response);
%>
