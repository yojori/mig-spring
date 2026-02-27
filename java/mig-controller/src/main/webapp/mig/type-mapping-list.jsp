<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ page import="c.y.mig.util.*" %>
<%@ page import="c.y.mig.model.*" %>
<%@ page import="c.y.mig.manager.TypeMappingManager" %>
<%@ page import="java.util.*" %>
<%@include file="/mig/session-admin-check.jsp" %>
<%
    TypeMapping search = (TypeMapping) RequestUtils.getBean(request, TypeMapping.class);
    
    TypeMappingManager manager = new TypeMappingManager();
    List<TypeMapping> list = manager.getList(search);
    
    request.setAttribute("search", search);
    request.setAttribute("list", list);
    
    request.getRequestDispatcher("./type-mapping-list-fwd.jsp").forward(request, response);
%>
