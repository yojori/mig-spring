<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ page import="c.y.mig.util.*" %>
<%@ page import="c.y.mig.manager.*" %>
<%@ page import="c.y.mig.model.*" %>
<%@ page import="java.util.*" %>
<%@include file="/mig/session-admin-check.jsp" %>
<%
    WorkList search = (WorkList) RequestUtils.getBean(request, WorkList.class);

    // Default Paging
    if (search.getPageSize() == 0) {
        search.setPageSize(15);
    }

    WorkListManager manager = new WorkListManager();
    List<WorkList> list = manager.getList(search, InterfaceManager.PAGE);

    request.setAttribute("search", search);
    request.setAttribute("list", list);

    request.getRequestDispatcher("./migration-work-list-fwd.jsp").forward(request, response);
%>