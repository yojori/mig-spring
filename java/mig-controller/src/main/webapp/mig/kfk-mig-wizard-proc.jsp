<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ page import="c.y.mig.manager.*" %>
<%@ page import="c.y.mig.model.*" %>
<%@ page import="c.y.mig.util.*" %>
<%@ page import="java.util.*" %>
<%@include file="/mig/session-admin-check.jsp" %>
<%
    String mig_master = request.getParameter("mig_master");
    String mig_list_seq = request.getParameter("mig_list_seq");
    String registration_type = request.getParameter("registration_type");
    String source_connector = request.getParameter("source_connector");
    String sink_connector = request.getParameter("sink_connector");
    
    // Get MigrationList info
    MigrationListManager migListManager = new MigrationListManager();
    MigrationList searchModel = new MigrationList();
    searchModel.setMig_list_seq(mig_list_seq);
    MigrationList migListInfo = migListManager.find(searchModel);
    
    // 1. Insert into KFK_MIG_LIST
    KfkMigList list = new KfkMigList();
    list.setMig_list_seq(mig_list_seq);
    list.setMig_master(mig_master);
    list.setRegistration_type(registration_type);
    if (migListInfo != null) {
        list.setMig_name(migListInfo.getMig_name());
    }
    
    // Set connectors based on registration type
    if ("SOURCE_ONLY".equals(registration_type) || "BOTH".equals(registration_type)) {
        list.setSource_connector(source_connector);
    }
    if ("SINK_ONLY".equals(registration_type) || "BOTH".equals(registration_type)) {
        list.setSink_connector(sink_connector);
    }
    list.setUse_yn("Y");

    KfkMigListManager kfkListManager = new KfkMigListManager();
    kfkListManager.insert(list);
    
    // Clean up existing parameters for this list before re-saving
    kfkListManager.deleteParams(mig_list_seq);
    
    // Pre-cache parameter templates to identify connector_type
    KfkParamTemplateManager tm = new KfkParamTemplateManager();
    Map<String, String> keyToTypeMap = new HashMap<String, String>();
    Map<String, KfkParamTemplate> tplMap = new HashMap<String, KfkParamTemplate>();
    
    KfkMigList migListInfo = kfkListManager.getRecord(mig_list_seq);
    if (source_connector == null || source_connector.isEmpty()) {
        source_connector = (migListInfo != null) ? migListInfo.getSource_connector() : "";
    }
    if (sink_connector == null || sink_connector.isEmpty()) {
        sink_connector = (migListInfo != null) ? migListInfo.getSink_connector() : "";
    }

    // Level 0 params
    for (KfkParamTemplate t : tm.getList(0, null)) {
        tplMap.put("COMMON:" + t.getParam_key(), t);
    }
    // Source params
    if (source_connector != null && !source_connector.isEmpty()) {
        for (KfkParamTemplate t : tm.getAllLevelsList(source_connector)) {
            tplMap.put(source_connector + ":" + t.getParam_key(), t);
        }
    }
    // Sink params
    if (sink_connector != null && !sink_connector.isEmpty()) {
        for (KfkParamTemplate t : tm.getAllLevelsList(sink_connector)) {
            tplMap.put(sink_connector + ":" + t.getParam_key(), t);
        }
    }
    
    // 2. Insert into KFK_MIG_PARAM
    Enumeration<String> paramNames = request.getParameterNames();
    while (paramNames.hasMoreElements()) {
        String fullKey = paramNames.nextElement();
        if ("mig_master".equals(fullKey) || "mig_name".equals(fullKey) || "level".equals(fullKey) || "mig_list_seq".equals(fullKey) || "registration_type".equals(fullKey)) {
            continue;
        }
        
        String val = request.getParameter(fullKey);
        if (val != null && !val.isEmpty()) {
            String cType = "COMMON";
            String realKey = fullKey;
            
            if (fullKey.startsWith("SOURCE__")) {
                cType = "SOURCE";
                realKey = fullKey.substring(8);
            } else if (fullKey.startsWith("SINK__")) {
                cType = "SINK";
                realKey = fullKey.substring(6);
            } else if (fullKey.startsWith("COMMON__")) {
                cType = "COMMON";
                realKey = fullKey.substring(8);
            }
            
            // Try specific connector template first
            String connector = "SOURCE".equals(cType) ? source_connector : ("SINK".equals(cType) ? sink_connector : null);
            String cacheKey = (connector != null ? connector : "COMMON") + ":" + realKey;
            KfkParamTemplate tpl = tplMap.get(cacheKey);
            
            // Fallback to searching by key if not found with specific connector
            if (tpl == null) {
                tpl = tplMap.get("COMMON:" + realKey);
            }
            
            int dpLevel = (tpl != null) ? tpl.getDp_level() : 0;
            int dpOrder = (tpl != null) ? tpl.getDp_order() : 999;
            
            kfkListManager.saveParams(mig_list_seq, cType, realKey, val, dpLevel, dpOrder);
        }
    }
%>
<script>
    location.href = "kfk-mig-wizard-result.jsp?mig_master=<%=mig_master%>&mig_list_seq=<%=mig_list_seq%>";
</script>
