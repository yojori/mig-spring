<%@ page language="java" pageEncoding="UTF-8" %>
<%@ page import="c.y.mig.util.*" %>
<%@ page import="c.y.mig.manager.*" %>
<%@ page import="c.y.mig.model.*" %>
<%@ page import="java.util.*" %>
<%@include file="/mig/session-admin-check.jsp" %>
<%
    response.setContentType("application/json; charset=UTF-8");
    request.setCharacterEncoding("UTF-8");
    String mode = request.getParameter("mode");
    String migListSeq = request.getParameter("mig_list_seq");
    String migName = request.getParameter("mig_name");

    Map<String, Object> result = new HashMap<>();
    
    try {
        if ("register".equals(mode)) {
            if (migListSeq != null && !migListSeq.isEmpty()) {
                WorkListManager manager = new WorkListManager();
                WorkList work = new WorkList();
                work.setMig_list_seq(migListSeq);
                work.setStatus(WorkList.STATUS_READY);
                work.setCreate_date(new Date());
                int rtn = manager.insert(work);
                
                if (rtn > 0) {
                    result.put("success", true);
                    result.put("message", "작업 등록 완료 [" + migName + "]");
                } else {
                    result.put("success", false);
                    result.put("message", "DB 등록 실패 (결과: " + rtn + ")");
                }
            } else {
                result.put("success", false);
                result.put("message", "mig_list_seq is missing");
            }
        } else {
            result.put("success", false);
            result.put("message", "Invalid mode: " + mode);
        }
    } catch (Exception e) {
        result.put("success", false);
        result.put("message", "Error: " + e.getMessage());
        e.printStackTrace();
    }

    // Convert to simple JSON manually to avoid adding dependency if not present
    // Or use a utility if available.
    StringBuilder json = new StringBuilder();
    json.append("{");
    json.append("\"success\":").append(result.get("success")).append(",");
    json.append("\"message\":\"").append(String.valueOf(result.get("message")).replace("\"", "\\\"")).append("\"");
    json.append("}");
    
    out.print(json.toString());
%>
