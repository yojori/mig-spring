<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ page import="c.y.mig.util.*" %>
<%@ page import="c.y.mig.manager.*" %>
<%@ page import="c.y.mig.model.*" %>
<%@ page import="c.y.mig.service.*" %>
<%@ page import="java.util.*" %>
<%@include file="/mig/session-admin-check.jsp" %>
<%
    MigrationList master = (MigrationList) RequestUtils.getBean(request, MigrationList.class);
    MigrationListManager manager = new MigrationListManager();
    MigrationRegistrationService registrationService = new MigrationRegistrationService();
    MigrationMetadataService metadataService = new MigrationMetadataService();

    String sourceTableArea = request.getParameter("source_table_area");
    String sourcePkArea = request.getParameter("source_pk_area");
    String targetTableArea = request.getParameter("target_table_area");
    String truncateYnArea = request.getParameter("truncate_yn"); // Changed from truncate_yn_area
    String targetStrategy = request.getParameter("target_strategy");

    boolean isBulk = !StringUtil.empty(sourceTableArea);
    String mode = master.getMode();
    if (StringUtil.empty(mode)) mode = "insert"; // Default to insert if missing
    
    // For DDL type, we save the table list in sql_string if it's a new registration or update
    if ("DDL".equals(master.getMig_type()) && !StringUtil.empty(sourceTableArea)) {
        master.setSql_string(sourceTableArea);
    }

    // 1. Bulk Table Registration (Create multiple MigrationList entries)
    if ("insert".equals(mode) && isBulk && ("TABLE".equals(master.getMig_type()) || "DDL".equals(master.getMig_type()) || "JAVA".equals(master.getMig_type()))) {
        List<String> seqs = registrationService.bulkInsertTableTasks(master, sourceTableArea, sourcePkArea, targetTableArea, truncateYnArea, targetStrategy);
        if (seqs != null && !seqs.isEmpty()) {
            master.setMig_list_seq(seqs.get(0)); // Set first one for redirection
        }
    }
    // 2. Standard Single Registration or Update
    else {
        // Helper to fetch PK if empty for NORMAL/THREAD/KAFKA types
        if (("NORMAL".equals(master.getMig_type()) || "THREAD".equals(master.getMig_type()) || "THREAD_IDX".equals(master.getMig_type()) || "KAFKA".equals(master.getMig_type())) && StringUtil.empty(master.getSource_pk()) && !StringUtil.empty(master.getTarget_table())) {
            if (!StringUtil.empty(master.getSource_db_alias())) {
                c.y.mig.manager.DBConnMasterManager dbm = new c.y.mig.manager.DBConnMasterManager();
                c.y.mig.model.DBConnMaster dbSearch = new c.y.mig.model.DBConnMaster();
                dbSearch.setMaster_code(master.getSource_db_alias());
                c.y.mig.model.DBConnMaster sourceMaster = dbm.find(dbSearch);
                if (sourceMaster != null) {
                    java.sql.Connection sourceConn = null;
                    try {
                        sourceConn = c.y.mig.db.DBManager.getConnection(sourceMaster);
                        String fetchedPk = metadataService.fetchPrimaryKey(sourceConn, master.getTarget_table(), sourceMaster.getDb_type());
                        if (!StringUtil.empty(fetchedPk)) {
                            master.setSource_pk(fetchedPk);
                        }
                    } catch (Exception e) {
                        // Ignore, PK will remain empty
                    } finally {
                        c.y.mig.db.DBManager.close(null, null, sourceConn);
                    }
                }
            }
        }

        if ("update".equals(mode)) {
            master.setUpdate_date(new Date());
            
            // For TABLE/DDL, ensure sql_string matches source_table for compatibility
            if (StringUtil.empty(master.getSql_string()) && !StringUtil.empty(master.getSource_table())) {
                master.setSql_string(master.getSource_table());
            }
            
            manager.update(master);
            
            // Handle KAFKA mapping update
            if ("KAFKA".equals(master.getMig_type()) && !StringUtil.empty(master.getSql_string())) {
                c.y.mig.manager.KfkMappingManager mappingManager = new c.y.mig.manager.KfkMappingManager();
                c.y.mig.model.KfkMapping mapping = mappingManager.find(master.getMig_list_seq());
                if (mapping != null) {
                    mapping.setTransformation_json(master.getSql_string());
                    mappingManager.update(mapping);
                } else {
                    mapping = new c.y.mig.model.KfkMapping();
                    mapping.setMig_list_seq(master.getMig_list_seq());
                    mapping.setMapping_name("Mapping_" + master.getMig_list_seq());
                    mapping.setTransformation_json(master.getSql_string());
                    mappingManager.insert(mapping);
                }
            }
            
            // Note: Legacy detail table sync (InsertSql, InsertTable) removed as metadata is now 1:1
        } else {
            master.setMig_list_seq(Config.getOrdNoSequence("ML"));
            master.setCreate_date(new Date());
            master.setUpdate_date(new Date());
            
            // For TABLE/DDL, ensure sql_string matches source_table
            if (StringUtil.empty(master.getSql_string()) && !StringUtil.empty(master.getSource_table())) {
                master.setSql_string(master.getSource_table());
            }
            
            manager.insert(master);

            // Handle KAFKA mapping insert
            if ("KAFKA".equals(master.getMig_type()) && !StringUtil.empty(master.getSql_string())) {
                c.y.mig.manager.KfkMappingManager mappingManager = new c.y.mig.manager.KfkMappingManager();
                c.y.mig.model.KfkMapping mapping = new c.y.mig.model.KfkMapping();
                mapping.setMig_list_seq(master.getMig_list_seq());
                mapping.setMapping_name("Mapping_" + master.getMig_list_seq());
                mapping.setTransformation_json(master.getSql_string());
                mappingManager.insert(mapping);
            }

            // Auto register columns for single insert (SQL type etc.)
            metadataService.autoRegisterColumns(master);
        }
    }
%>
<script>
    if (window.opener && !window.opener.closed) {
        opener.location.href = "/mig/migration-list.jsp?mig_master=<%=master.getMig_master()%>";
        self.close();
    } else {
        location.href = "/mig/migration-list.jsp?mig_master=<%=master.getMig_master()%>";
    }
</script>

