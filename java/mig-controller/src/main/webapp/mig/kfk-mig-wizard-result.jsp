 Lucia are you there?
<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ page import="java.util.*,c.y.mig.manager.*,c.y.mig.model.*,c.y.mig.util.*" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@include file="/mig/session-admin-check.jsp"%>
<%
    String mig_master = request.getParameter("mig_master");
    String mig_list_seq = request.getParameter("mig_list_seq");

    KfkMigListManager kfkListManager = new KfkMigListManager();
    KfkMigList record = kfkListManager.getRecord(mig_list_seq);
    
    if (record == null) {
        out.println("<script>alert('Invalid Access'); window.close();</script>");
        return;
    }

    String source_connector = record.getSource_connector();
    String sink_connector = record.getSink_connector();
    String registration_type = record.getRegistration_type();

    KfkParamTemplateManager tmManager = new KfkParamTemplateManager();
    List<KfkParamTemplate> sourcePreviewParams = tmManager.getAllLevelsList(source_connector);
    List<KfkParamTemplate> sinkPreviewParams = tmManager.getAllLevelsList(sink_connector);
    
    sourcePreviewParams = deduplicateParams(sourcePreviewParams);
    sinkPreviewParams = deduplicateParams(sinkPreviewParams);

    KfkMigParamManager paramManager = new KfkMigParamManager();
%>
<%!
    private List<KfkParamTemplate> deduplicateParams(List<KfkParamTemplate> input) {
        if (input == null || input.isEmpty()) return input;
        Map<String, KfkParamTemplate> map = new LinkedHashMap<String, KfkParamTemplate>();
        for (KfkParamTemplate t : input) {
            if (!map.containsKey(t.getParam_key())) {
                map.put(t.getParam_key(), t);
            }
        }
        return new ArrayList<KfkParamTemplate>(map.values());
    }
%>
<%
    KfkMigParamManager paramManagerInstance = new KfkMigParamManager();
    List<KfkMigParam> savedList = paramManagerInstance.getList(mig_list_seq);
    Map<String, String> paramMap = new HashMap<String, String>();
    for (KfkMigParam p : savedList) {
        String prefix = StringUtil.nvl(p.getConnector_type(), "COMMON") + "__";
        paramMap.put(prefix + p.getParam_key(), p.getParam_value());
    }

    // Fetch API URLs from CodeManager
    String source_connector_url = "";
    String sink_connector_url = "";
    if (source_connector != null && !source_connector.isEmpty()) {
        for (Map<String, String> opt : CodeManager.getCodeList("KFK-0041")) {
            if (source_connector.equals(opt.get("value"))) {
                source_connector_url = opt.get("ec1");
                break;
            }
        }
    }
    if (sink_connector != null && !sink_connector.isEmpty()) {
        for (Map<String, String> opt : CodeManager.getCodeList("KFK-0042")) {
            if (sink_connector.equals(opt.get("value"))) {
                sink_connector_url = opt.get("ec1");
                break;
            }
        }
    }

    request.setAttribute("record", record);
    request.setAttribute("sourcePreviewParams", sourcePreviewParams);
    request.setAttribute("sinkPreviewParams", sinkPreviewParams);
    request.setAttribute("savedParam", paramMap);
    request.setAttribute("source_connector_url", source_connector_url);
    request.setAttribute("sink_connector_url", sink_connector_url);
%>
<!DOCTYPE html>
<html lang="ko">
<head>
    <meta charset="UTF-8">
    <title>저장 완료 - Configuration Preview</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/css/bootstrap.min.css" rel="stylesheet">
    <link href="https://cdn.jsdelivr.net/npm/bootstrap-icons@1.10.5/font/bootstrap-icons.css" rel="stylesheet">
    <style>
        body { background-color: #f8f9fa; padding: 20px; }
        .result-container { max-width: 800px; margin: 0 auto; background: white; border-radius: 12px; box-shadow: 0 5px 20px rgba(0,0,0,0.05); overflow: hidden; }
        .result-header { background: #198754; color: white; padding: 20px; text-align: center; }
        .result-body { padding: 30px; }
        .auto-close-timer { text-align: center; color: #6c757d; font-size: 0.9rem; margin-top: 20px; }
        pre { font-size: 0.8rem; border-radius: 8px; }
    </style>
    <script>
        window.onload = function() {
            if (window.opener) {
                window.opener.location.reload();
            }
            
            var seconds = 5;
            var timerSpan = document.getElementById('timer');
            var countdown = setInterval(function() {
                seconds--;
                if (timerSpan) timerSpan.innerText = seconds;
                if (seconds <= 0) {
                    clearInterval(countdown);
                    window.close();
                }
            }, 1000);
        };

        function copyToClipboard(id) {
            var text = document.getElementById(id).innerText;
            navigator.clipboard.writeText(text).then(function() {
                alert("클립보드에 복사되었습니다.");
            });
        }
    </script>
</head>
<body>
    <div class="result-container">
        <div class="result-header">
            <h4 class="mb-0"><i class="bi bi-check-circle-fill me-2"></i>이관 설정 저장 완료</h4>
        </div>
        <div class="result-body">
            <p class="text-center mb-4 text-muted">설정이 정상적으로 저장되었습니다. 아래의 Connector 구성을 확인하세요.</p>
            
            <div class="card bg-dark text-light p-4 shadow-sm">
                <c:if test="${record.registration_type ne 'SINK_ONLY'}">
                    <div class="mb-4">
                        <div class="d-flex justify-content-between align-items-center mb-2">
                            <span class="text-success fw-bold">[ SOURCE CONNECTOR ]</span>
                            <button type="button" class="btn btn-outline-light btn-sm" onclick="copyToClipboard('sourceCurl')">Copy</button>
                        </div>
                        <pre id="sourceCurl" class="m-0 text-info" style="white-space: pre-wrap;">curl -X POST ${source_connector_url} -H "Content-Type: application/json" -d '{
  "name": "jdbc_source_${record.mig_list_seq}_${record.mig_name}",
  "config": {
    "connector.class": "${record.source_connector}",
    <c:forEach items="${sourcePreviewParams}" var="item" varStatus="vs">
    <c:set var="preKey" value="SOURCE__${item.param_key}" />
    "${item.param_key}": "${savedParam[preKey] != null ? savedParam[preKey] : item.default_value}"<c:if test="${!vs.last}">,</c:if>
    </c:forEach>
  }
}'</pre>
                    </div>
                </c:if>

                <c:if test="${record.registration_type ne 'SOURCE_ONLY'}">
                    <div class="mb-0">
                        <div class="d-flex justify-content-between align-items-center mb-2">
                            <span class="text-info fw-bold">[ SINK CONNECTOR ]</span>
                            <button type="button" class="btn btn-outline-light btn-sm" onclick="copyToClipboard('sinkCurl')">Copy</button>
                        </div>
                        <pre id="sinkCurl" class="m-0 text-warning" style="white-space: pre-wrap;">curl -X POST ${sink_connector_url} -H "Content-Type: application/json" -d '{
  "name": "jdbc_sink_${record.mig_list_seq}_${record.mig_name}",
  "config": {
    "connector.class": "${record.sink_connector}",
    <c:forEach items="${sinkPreviewParams}" var="item" varStatus="vs">
    <c:set var="preKey" value="SINK__${item.param_key}" />
    "${item.param_key}": "${savedParam[preKey] != null ? savedParam[preKey] : item.default_value}"<c:if test="${!vs.last}">,</c:if>
    </c:forEach>
  }
}'</pre>
                    </div>
                </c:if>
            </div>
            
            <div class="auto-close-timer">
                이 창은 <span id="timer" class="fw-bold text-danger">5</span>초 후에 자동으로 닫힙니다.
                <div class="mt-3">
                    <button type="button" class="btn btn-sm btn-secondary" onclick="window.close()">지금 닫기</button>
                </div>
            </div>
        </div>
    </div>
</body>
</html>
