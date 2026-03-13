<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ page import="c.y.mig.manager.*" %>
<%@ page import="c.y.mig.model.*" %>
<%@ page import="c.y.mig.util.*" %>
<%@ page import="java.util.*" %>
<%@include file="/mig/session-admin-check.jsp" %>
<%
    String mig_master = request.getParameter("mig_master");
    String mig_list_seq = request.getParameter("mig_list_seq");
    int level = StringUtil.nvl(request.getParameter("level"), 0);
    
    // In Level 0, we don't have connectors yet.
    // In Level 1+, we need the source_connector and sink_connector to filter params.
    String source_connector = request.getParameter("source_connector"); 
    String sink_connector = request.getParameter("sink_connector"); 

    KfkParamTemplateManager manager = new KfkParamTemplateManager();
    List<KfkParamTemplate> params = new ArrayList<KfkParamTemplate>();
    List<KfkParamTemplate> sourceParams = new ArrayList<KfkParamTemplate>();
    List<KfkParamTemplate> sinkParams = new ArrayList<KfkParamTemplate>();

    List<KfkParamTemplate> sourcePreviewParams = new ArrayList<KfkParamTemplate>();
    List<KfkParamTemplate> sinkPreviewParams = new ArrayList<KfkParamTemplate>();

    if (level == 0) {
        params = manager.getList(level, null);
    } else {
        sourceParams = manager.getList(level, source_connector);
        sinkParams = manager.getList(level, sink_connector);
        
        if (level == 3) {
            sourcePreviewParams = manager.getAllLevelsList(source_connector);
            sinkPreviewParams = manager.getAllLevelsList(sink_connector);
        }
    }

    // Deduplicate lists by param_key while maintaining order
    params = deduplicateParams(params);
    sourceParams = deduplicateParams(sourceParams);
    sinkParams = deduplicateParams(sinkParams);
    sourcePreviewParams = deduplicateParams(sourcePreviewParams);
    sinkPreviewParams = deduplicateParams(sinkPreviewParams);

    // Helper method for deduplication
%>
<%!
    private List<KfkParamTemplate> deduplicateParams(List<KfkParamTemplate> input) {
        if (input == null || input.isEmpty()) return input;
        Map<String, KfkParamTemplate> map = new LinkedHashMap<String, KfkParamTemplate>();
        for (KfkParamTemplate t : input) {
            // If already exists, we might want to prefer the one with higher level or just the first one.
            // Usually, later levels or more specific ones are preferred if they overlap, 
            // but for preview, any one is fine as long as it's unique.
            if (!map.containsKey(t.getParam_key())) {
                map.put(t.getParam_key(), t);
            }
        }
        return new ArrayList<KfkParamTemplate>(map.values());
    }
%>
<%

    // Fetch master info for header
    MigrationMasterManager masterManager = new MigrationMasterManager();
    MigrationMaster paramMaster = new MigrationMaster();
    paramMaster.setMaster_code(mig_master);
    MigrationMaster masterInfo = masterManager.find(paramMaster);

    // Fetch migration list info for target DB
    MigrationListManager migListManager = new MigrationListManager();
    MigrationList paramList = new MigrationList();
    paramList.setMig_list_seq(mig_list_seq);
    MigrationList migListInfo = migListManager.find(paramList);
    
    // Fetch DB credentials using aliases
    DBConnMasterManager dbConnManager = new DBConnMasterManager();
    DBConnMaster sourceDb = null;
    DBConnMaster targetDb = null;
    if (migListInfo != null) {
        if (migListInfo.getSource_db_alias() != null) {
            DBConnMaster sParam = new DBConnMaster();
            sParam.setMaster_code(migListInfo.getSource_db_alias());
            sourceDb = dbConnManager.find(sParam);
        }
        if (migListInfo.getTarget_db_alias() != null) {
            DBConnMaster tParam = new DBConnMaster();
            tParam.setMaster_code(migListInfo.getTarget_db_alias());
            targetDb = dbConnManager.find(tParam);
        }
    }
    
    // Fetch saved metadata if it exists
    KfkMigList existingRecord = null;
    if (mig_list_seq != null && !mig_list_seq.isEmpty()) {
        KfkMigListManager kfkMigListManager = new KfkMigListManager();
        existingRecord = kfkMigListManager.getRecord(mig_list_seq);
        if (existingRecord != null) {
            if (source_connector == null || source_connector.isEmpty()) {
                source_connector = existingRecord.getSource_connector();
            }
            if (sink_connector == null || sink_connector.isEmpty()) {
                sink_connector = existingRecord.getSink_connector();
            }
        }
    }

    // Fetch API URLs from CodeManager for Level 3 curl generation
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

    // Fetch saved parameters if they exist
    Map<String, String> paramMap = new HashMap<String, String>();
    if (mig_list_seq != null && !mig_list_seq.isEmpty()) {
        KfkMigParamManager kfkParamManager = new KfkMigParamManager();
        List<KfkMigParam> savedParams = kfkParamManager.getList(mig_list_seq);
        for (KfkMigParam p : savedParams) {
            String prefix = StringUtil.nvl(p.getConnector_type(), "COMMON") + "__";
            paramMap.put(prefix + p.getParam_key(), p.getParam_value());
        }
    }
    
    // Ensure registration_type is in the map if it's in the master record but not in params
    if (existingRecord != null && existingRecord.getRegistration_type() != null) {
        if (!paramMap.containsKey("registration_type") || paramMap.get("registration_type") == null) {
            paramMap.put("registration_type", existingRecord.getRegistration_type());
        }
    }

    request.setAttribute("params", params);
    request.setAttribute("sourceParams", sourceParams);
    request.setAttribute("sinkParams", sinkParams);
    request.setAttribute("sourcePreviewParams", sourcePreviewParams);
    request.setAttribute("sinkPreviewParams", sinkPreviewParams);
    request.setAttribute("source_connector", source_connector);
    request.setAttribute("sink_connector", sink_connector);
    request.setAttribute("sourceDb", sourceDb);
    request.setAttribute("targetDb", targetDb);
    request.setAttribute("savedParam", paramMap);
    request.setAttribute("mig_master", mig_master);
    request.setAttribute("mig_list_seq", mig_list_seq);
    request.setAttribute("migListInfo", migListInfo);
    request.setAttribute("mig_list_seq", mig_list_seq);
    request.setAttribute("source_connector_url", source_connector_url);
    request.setAttribute("sink_connector_url", sink_connector_url);
    request.setAttribute("level", level);
    request.setAttribute("masterInfo", masterInfo);
    
    request.getRequestDispatcher("./kfk-mig-wizard-fwd.jsp").forward(request, response);
%>
