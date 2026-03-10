<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ page import="c.y.mig.util.*" %>
<%@ page import="c.y.mig.manager.*" %>
<%@ page import="c.y.mig.model.*" %>
<%@include file="/mig/session-admin-check.jsp" %>
<%
    KfkTemplate master = (KfkTemplate) RequestUtils.getBean(request, KfkTemplate.class);
    KfkTemplateManager manager = new KfkTemplateManager();
    
    String mode = master.getMode();
    if (StringUtil.empty(mode)) mode = "insert";

    if ("update".equals(mode)) {
        manager.update(master);
    } else {
        manager.insert(master);
    }
%>
<script>
    location.href = "/mig/kfk-template-list.jsp";
</script>
