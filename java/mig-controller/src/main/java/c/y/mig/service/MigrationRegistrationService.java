package c.y.mig.service;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;



import c.y.mig.manager.DBConnMasterManager;
import c.y.mig.manager.MigrationListManager;
import c.y.mig.model.DBConnMaster;
import c.y.mig.model.MigrationList;
import c.y.mig.util.Config;
import c.y.mig.util.StringUtil;

/**
 * Service for handling Migration task registration workflows.
 */
public class MigrationRegistrationService {
    
    private final MigrationListManager migrationListManager = new MigrationListManager();
    private final MigrationMetadataService metadataService = new MigrationMetadataService();

    /**
     * Handles bulk registration of tables as separate MigrationList entries.
     */
    public List<String> bulkInsertTableTasks(MigrationList master, String sourceTableArea, String sourcePkArea, String targetTableArea, String truncateYn, String targetStrategy) {
        List<String> createdSeqs = new ArrayList<String>();
        if (StringUtil.empty(sourceTableArea)) return createdSeqs;

        String[] source_table = sourceTableArea.split("\\r?\\n");
        String[] source_pk = (sourcePkArea != null) ? sourcePkArea.split("\\r?\\n") : new String[0];
        String[] target_table = (targetTableArea != null) ? targetTableArea.split("\\r?\\n") : new String[0];

        int threadCount = (master.getThread_count() <= 0) ? 1 : master.getThread_count();
        String finalStrategy = (targetStrategy != null && !targetStrategy.isEmpty()) ? targetStrategy : "NORMAL";
        
            // Base sequence for formatted IDs
            String baseSeq = Config.getOrdNoSequence("ML");
            int baseOrdering = master.getOrdering();
            
            // Pre-fetch DB types if missing from master
            DBConnMasterManager dbm = new DBConnMasterManager();
            String sType = master.getSource_db_type();
            String tType = master.getTarget_db_type();
            
            if (StringUtil.empty(sType) && !StringUtil.empty(master.getSource_db_alias())) {
                DBConnMaster sKey = new DBConnMaster();
                sKey.setMaster_code(master.getSource_db_alias());
                DBConnMaster sDB = dbm.find(sKey);
                if (sDB != null) sType = sDB.getDb_type();
            }
            if (StringUtil.empty(tType) && !StringUtil.empty(master.getTarget_db_alias())) {
                DBConnMaster tKey = new DBConnMaster();
                tKey.setMaster_code(master.getTarget_db_alias());
                DBConnMaster tDB = dbm.find(tKey);
                if (tDB != null) tType = tDB.getDb_type();
            }

            for (int i = 0; i < source_table.length; i++) {
                String sTable = source_table[i].trim();
                if (sTable.isEmpty()) continue;

                String tTable = (i < target_table.length && !target_table[i].trim().isEmpty()) ? target_table[i].trim() : sTable;
                String sPk = (i < source_pk.length) ? source_pk[i].trim() : "";

                MigrationList ml = new MigrationList();
                // Copy properties from master
                ml.setMig_master(master.getMig_master());
                ml.setSource_db_alias(master.getSource_db_alias());
                ml.setTarget_db_alias(master.getTarget_db_alias());
                ml.setSource_db_type(sType);
                ml.setTarget_db_type(tType);
                ml.setMig_type(finalStrategy);
                ml.setThread_use_yn(finalStrategy.contains("THREAD") ? "Y" : "N");
                ml.setThread_count(threadCount);
                ml.setPage_count_per_thread(master.getPage_count_per_thread());
                ml.setExecute_yn(master.getExecute_yn());
                ml.setDisplay_yn(master.getDisplay_yn());
                
                // Populate logic
                ml.setSql_string("SELECT * FROM " + sTable);
                ml.setOrdering(baseOrdering + (i * 10));
                
                // Set mig_name to Target Table (as per user guidance/logic)
                ml.setMig_name(tTable);
                
                // Store parameters (PK, Truncate) in param_string as InsertTable is removed
                StringBuilder params = new StringBuilder();
                if (!sPk.isEmpty()) params.append("PK=").append(sPk).append(";");
                if (truncateYn != null && "Y".equals(truncateYn)) params.append("TRUNCATE=Y;");
                
                String paramStr = params.toString();
                ml.setParam_string(paramStr);
                
                // System.out.println("DEBUG: BulkInsert - Table=" + sTable + ", truncateYnRaw=" + truncateYn + ", finalParams=" + paramStr);
                
                // Formatted ID
                String formattedIdx = String.format("%04d", i + 1);
                String newSeq = baseSeq + "-" + formattedIdx;
                
                ml.setMig_list_seq(newSeq);
                ml.setCreate_date(new Date());
                ml.setUpdate_date(new Date());
                
                migrationListManager.insert(ml);
                
                // Delegate column registration to Metadata Service (Now handles InsertTable redundancy)
                metadataService.autoRegisterColumns(ml);
                
                createdSeqs.add(newSeq);
            }
        return createdSeqs;
    }
}
