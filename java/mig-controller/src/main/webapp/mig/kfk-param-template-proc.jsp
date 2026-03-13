<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ page import="c.y.mig.util.*" %>
<%@ page import="c.y.mig.manager.*" %>
<%@ page import="c.y.mig.model.*" %>
<%@include file="/mig/session-admin-check.jsp" %>
<%
    KfkParamTemplate master = (KfkParamTemplate) RequestUtils.getBean(request, KfkParamTemplate.class);
    KfkParamTemplateManager manager = new KfkParamTemplateManager();
    
    String mode = StringUtil.nvl(master.getMode(), "insert");

    if ("delete".equals(mode)) {
        // manager.delete(master.getId()); 
        // Need to implement delete in manager if needed, for now let's just do update/insert
    } else if ("update".equals(mode)) {
        manager.update(master);
    } else {
        manager.insert(master);
    }
%>
<script>
    location.href = "/mig/kfk-param-template-list.jsp";
</script>
