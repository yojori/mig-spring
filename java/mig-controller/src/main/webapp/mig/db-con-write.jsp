<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ page import="c.y.mig.util.*" %>
<%@ page import="c.y.mig.manager.*" %>
<%@ page import="c.y.mig.model.*" %>
<%@ page import="java.util.*" %>
<%@include file="/mig/session-admin-check.jsp" %>
<%  
    DBConnMaster master = (DBConnMaster) RequestUtils.getBean(request, DBConnMaster.class);
   
    DBConnMasterManager manager = new DBConnMasterManager();  

    if ("update".equals(master.getMode()) && master.getMaster_code() != null) {
        master = manager.find(master);
    }
        
    request.setAttribute("master", master);
   
    request.getRequestDispatcher("./db-con-write-fwd.jsp").forward(request, response);
%>
