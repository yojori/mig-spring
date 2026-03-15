<%@ page language="java" contentType="application/json; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ page import="c.y.mig.manager.*" %>
<%@ page import="c.y.mig.model.*" %>
<%@ page import="com.fasterxml.jackson.databind.ObjectMapper" %>
<%@ page import="java.util.*" %>
<%@include file="/mig/session-admin-check.jsp" %>
<%
    String mode = request.getParameter("mode");
    String mig_list_seq = request.getParameter("mig_list_seq");
    
    KafkaConnectManager connectManager = (KafkaConnectManager) RequestUtils.getBean(request, KafkaConnectManager.class);
    if (connectManager == null) connectManager = new KafkaConnectManager(); // Fallback if not managed

    KfkMigListManager listManager = new KfkMigListManager();
    KfkMigParamManager paramManager = new KfkMigParamManager();
    KfkMigList migList = listManager.getRecord(mig_list_seq);

    Map<String, Object> result = new HashMap<>();
    ObjectMapper mapper = new ObjectMapper();

    try {
        if ("status".equals(mode)) {
            if (migList != null) {
                Map<String, String> statusMap = new HashMap<>();
                if (migList.getSource_connector() != null && !migList.getSource_connector().isEmpty()) {
                    statusMap.put("source", connectManager.getStatus(mig_list_seq + "-source"));
                }
                if (migList.getSink_connector() != null && !migList.getSink_connector().isEmpty()) {
                    statusMap.put("sink", connectManager.getStatus(mig_list_seq + "-sink"));
                }
                result.put("status", statusMap);
            }
            result.put("success", true);
        } else if ("start".equals(mode)) {
            boolean success = true;
            if (migList != null) {
                List<KfkMigParam> params = paramManager.getList(mig_list_seq);
                
                // Build configs
                Map<String, String> commonConfig = new HashMap<>();
                Map<String, String> sourceConfig = new HashMap<>();
                Map<String, String> sinkConfig = new HashMap<>();
                
                for (KfkMigParam p : params) {
                    if ("COMMON".equals(p.getConnector_type())) commonConfig.put(p.getParam_key(), p.getParam_value());
                    else if ("SOURCE".equals(p.getConnector_type())) sourceConfig.put(p.getParam_key(), p.getParam_value());
                    else if ("SINK".equals(p.getConnector_type())) sinkConfig.put(p.getParam_key(), p.getParam_value());
                }
                
                if (migList.getSource_connector() != null && !migList.getSource_connector().isEmpty()) {
                    Map<String, String> fullSource = new HashMap<>(commonConfig);
                    fullSource.putAll(sourceConfig);
                    fullSource.put("connector.class", migList.getSource_connector());
                    success &= connectManager.createConnector(mig_list_seq + "-source", mapper.writeValueAsString(fullSource));
                }
                if (migList.getSink_connector() != null && !migList.getSink_connector().isEmpty()) {
                    Map<String, String> fullSink = new HashMap<>(commonConfig);
                    fullSink.putAll(sinkConfig);
                    fullSink.put("connector.class", migList.getSink_connector());
                    success &= connectManager.createConnector(mig_list_seq + "-sink", mapper.writeValueAsString(fullSink));
                }
            }
            result.put("success", success);
        } else if ("pause".equals(mode)) {
            boolean success = true;
            success &= connectManager.pauseConnector(mig_list_seq + "-source");
            success &= connectManager.pauseConnector(mig_list_seq + "-sink");
            result.put("success", success);
        } else if ("resume".equals(mode)) {
            boolean success = true;
            success &= connectManager.resumeConnector(mig_list_seq + "-source");
            success &= connectManager.resumeConnector(mig_list_seq + "-sink");
            result.put("success", success);
        } else if ("stop".equals(mode)) {
            boolean success = true;
            success &= connectManager.deleteConnector(mig_list_seq + "-source");
            success &= connectManager.deleteConnector(mig_list_seq + "-sink");
            result.put("success", success);
        }
    } catch (Exception e) {
        result.put("success", false);
        result.put("message", e.getMessage());
    }

    out.print(mapper.writeValueAsString(result));
%>
