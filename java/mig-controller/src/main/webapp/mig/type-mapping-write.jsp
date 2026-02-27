<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ page import="c.y.mig.util.*" %>
<%@ page import="c.y.mig.model.*" %>
<%@ page import="c.y.mig.manager.TypeMappingManager" %>
<%@include file="/mig/session-admin-check.jsp" %>
<%
    String mappingSeq = request.getParameter("mapping_seq");
    String mode = request.getParameter("mode");
    
    TypeMapping entry = new TypeMapping();
    if ("update".equals(mode) && mappingSeq != null) {
        TypeMappingManager manager = new TypeMappingManager();
        entry = manager.find(mappingSeq);
    } else {
        entry.setUse_yn("Y");
        entry.setPriority(10);
    }
    
    request.setAttribute("entry", entry);
    request.setAttribute("mode", mode);
    
    request.getRequestDispatcher("./type-mapping-write-fwd.jsp").forward(request, response);
%>
