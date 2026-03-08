<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ page import="c.y.mig.util.*" %>
<%@ page import="c.y.mig.manager.*" %>
<%@ page import="c.y.mig.model.*" %>
<%@ page import="java.util.*" %>
<%@include file="/mig/session-admin-check.jsp" %>
<%
    String workSeqStr = RequestUtils.getStringParameter(request, "work_seq");
    if (workSeqStr == null || workSeqStr.isEmpty()) {
        out.println("<script>alert('Invalid Work Sequence'); window.close();</script>");
        return;
    }

    WorkDetail search = new WorkDetail();
    search.setWork_seq(Integer.parseInt(workSeqStr));

    WorkDetailManager manager = new WorkDetailManager();
    List<WorkDetail> list = manager.findList(search);

    request.setAttribute("list", list);
    request.setAttribute("work_seq", workSeqStr);

    request.getRequestDispatcher("./migration-work-detail-fwd.jsp").forward(request, response);
%>
