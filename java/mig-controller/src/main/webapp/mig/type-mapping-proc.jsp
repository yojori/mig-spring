<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ page import="c.y.mig.util.*" %>
<%@ page import="c.y.mig.model.*" %>
<%@ page import="c.y.mig.manager.TypeMappingManager" %>
<%@include file="/mig/session-admin-check.jsp" %>
<%
    String mode = request.getParameter("mode");
    String mappingSeq = request.getParameter("mapping_seq");
    
    TypeMappingManager manager = new TypeMappingManager();
    
    if ("update".equals(mode)) {
        TypeMapping entry = (TypeMapping) RequestUtils.getBean(request, TypeMapping.class);
        manager.update(entry);
%>
    <script>
        alert("수정되었습니다.");
        opener.location.reload();
        window.close();
    </script>
<%
    } else if ("delete".equals(mode)) {
        manager.delete(mappingSeq);
        response.sendRedirect("type-mapping-list.jsp");
    } else {
        TypeMapping entry = (TypeMapping) RequestUtils.getBean(request, TypeMapping.class);
        manager.insert(entry);
%>
    <script>
        alert("등록되었습니다.");
        opener.location.reload();
        window.close();
    </script>
<%
    }
%>
