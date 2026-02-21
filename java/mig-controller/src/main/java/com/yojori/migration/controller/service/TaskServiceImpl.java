package com.yojori.migration.controller.service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.yojori.manager.MigrationListManager; // Added import
import com.yojori.manager.WorkListManager;
import com.yojori.model.DBConnMaster;
import com.yojori.model.InsertColumn;
import com.yojori.model.InsertSql;
import com.yojori.model.InsertTable;
import com.yojori.model.MigrationList;
import com.yojori.model.MigrationMaster;
import com.yojori.model.MigrationSchema;
import com.yojori.model.WorkList;
import com.yojori.model.WorkerStatus;

@Service
public class TaskServiceImpl implements TaskService {

    private static final Logger log = LoggerFactory.getLogger(TaskServiceImpl.class);

    @Override
    public String allocateTask(String workerId) {
        WorkListManager workManager = new WorkListManager();
        WorkList search = new WorkList();
        search.setStatus(WorkList.STATUS_READY);
        search.setOrderBy("A.work_seq ASC"); // FIFO: Process oldest task first
        
        // Simple polling: Get list of READY tasks and pick first one
        // Ideally we should use a "findAndLock" or atomic update, but for now:
        List<WorkList> list = workManager.getList(search, com.yojori.manager.InterfaceManager.LIST);
        
        if (list != null && !list.isEmpty()) {
            WorkList work = list.get(0);
            
            // Optimistic locking / Update status to RUNNING
            work.setStatus(WorkList.STATUS_RUNNING);
            work.setWorker_id(workerId);
            work.setStart_date(new java.util.Date());
            
            if (workManager.updateStatus(work) > 0) {
                // Return work_seq as the Task ID for the worker
                return work.getWork_seq();
            }
        }
        return null;
    }

    @Override
    public MigrationSchema getTaskConfig(String taskId) {
        // taskId is work_seq
        WorkListManager workManager = new WorkListManager();
        WorkList workSearch = new WorkList();
        workSearch.setWork_seq(taskId);
        WorkList work = workManager.find(workSearch);
        
        if (work == null) {
            throw new RuntimeException("Work not found for ID: " + taskId);
        }
        
        String migListSeq = work.getMig_list_seq();
        
        MigrationSchema schema = new MigrationSchema();

        MigrationListManager listManager = new MigrationListManager();
        MigrationList migList = new MigrationList();
        migList.setMig_list_seq(migListSeq);
        migList = listManager.find(migList);
        
        if (migList == null) {
             throw new RuntimeException("Migration List not found for seq: " + migListSeq);
        }
        
        schema.setMigList(new ArrayList<>(Collections.singletonList(migList)));

        // Populate Source/Target DB Config
        if (migList.getSource_db_alias() != null) {
            com.yojori.manager.DBConnMasterManager dbManager = new com.yojori.manager.DBConnMasterManager();
            DBConnMaster sourceKey = new DBConnMaster();
            sourceKey.setMaster_code(migList.getSource_db_alias());
            schema.setSource(dbManager.find(sourceKey));
        }
        
        if (migList.getTarget_db_alias() != null) {
            com.yojori.manager.DBConnMasterManager dbManager = new com.yojori.manager.DBConnMasterManager();
            DBConnMaster targetKey = new DBConnMaster();
            targetKey.setMaster_code(migList.getTarget_db_alias());
            schema.setTarget(dbManager.find(targetKey));
        }

        com.yojori.manager.MigrationMasterManager masterManager = new com.yojori.manager.MigrationMasterManager();
        MigrationMaster master = new MigrationMaster();
        master.setMaster_code(migList.getMig_master());
        master = masterManager.find(master);
        schema.setMaster(master);

        // Inject params from WorkList (execution instance) to MigrationList (definition)
        // so the Worker sees the specific parameters for this task instance.
        if (work.getParam_string() != null && !work.getParam_string().isEmpty()) {
            migList.setParam_string(work.getParam_string());
        }

        com.yojori.manager.InsertTableManager tableManager = new com.yojori.manager.InsertTableManager();
        InsertTable tableSearch = new InsertTable();
        tableSearch.setMig_list_seq(migListSeq);
        tableSearch.setPageSize(99999);
        List<InsertTable> tList = tableManager.getList(tableSearch, com.yojori.manager.InterfaceManager.LIST);
        log.info("DEBUG: TaskServiceImpl fetched InsertTableList size: " + (tList != null ? tList.size() : "null") + " for migListSeq: " + migListSeq);
        schema.setInsertTableList(tList);

        com.yojori.manager.InsertSqlManager sqlManager = new com.yojori.manager.InsertSqlManager();
        InsertSql sqlSearch = new InsertSql();
        sqlSearch.setMig_list_seq(migListSeq);
        schema.setInsertSqlList(sqlManager.getList(sqlSearch, com.yojori.manager.InterfaceManager.LIST));

        com.yojori.manager.InsertColumnManager colManager = new com.yojori.manager.InsertColumnManager();
        InsertColumn colSearch = new InsertColumn();
        colSearch.setMig_list_seq(migListSeq);
        schema.setInsertColumnList(colManager.getList(colSearch, com.yojori.manager.InterfaceManager.LIST));

        return schema;
    }

    @Override
    public void updateStatus(WorkerStatus status) {
        WorkListManager workManager = new WorkListManager();
        WorkList work = new WorkList();
        work.setWork_seq(status.getTaskId()); // TaskId is work_seq
        
        work.setStatus(status.getStatus());
        if (WorkList.STATUS_DONE.equals(status.getStatus()) || "COMPLETED".equals(status.getStatus())) {
             work.setStatus(WorkList.STATUS_DONE);
             work.setEnd_date(new java.util.Date());
        } else if (WorkList.STATUS_FAIL.equals(status.getStatus()) || "FAILED".equals(status.getStatus())) {
             work.setStatus(WorkList.STATUS_FAIL);
             work.setEnd_date(new java.util.Date());
        }
        
        work.setResult_msg(status.getMessage());
        work.setRead_count(status.getReadCount());
        work.setProc_count(status.getProcessedCount());
        workManager.updateStatus(work);
    }

    @Override
    public void createChildTask(MigrationList childTask) {
        // Refactored: Do NOT create new MIGRATION_LIST entry for child tasks.
        // Instead, reuse parent's mig_list_seq and store execution params in WORK_LIST.
        
        // Auto-create WorkList item if execution is requested (e.g. for THREAD_MULTI child tasks)
        if ("Y".equals(childTask.getExecute_yn())) {
            
             // 1. Generate new Work Sequence
             String workSeq = com.yojori.util.Config.getOrdNoSequence("WL");
             
             WorkList work = new WorkList();
             // work.setWork_seq(workSeq); // Handled by DB
             work.setMig_list_seq(childTask.getMig_list_seq()); // Reuse Parent ID
             work.setStatus(WorkList.STATUS_READY);
             work.setCreate_date(new java.util.Date());
             
             // Save Execution Params
             work.setParam_string(childTask.getParam_string());
             
             // Insert into WORK_LIST
             WorkListManager workManager = new WorkListManager();
             
             workManager.insert(work);
             
             log.info("Auto-created WorkList entry [{}] for Child Task [{}] with params [{}]", workSeq, childTask.getMig_name(), childTask.getParam_string());
        }
    }
    @Override
    public List<DBConnMaster> getAllDBConnections() {
        com.yojori.manager.DBConnMasterManager dbManager = new com.yojori.manager.DBConnMasterManager();
        DBConnMaster search = new DBConnMaster();
        search.setPageSize(9999); // Fetch all
        return dbManager.getList(search, com.yojori.manager.InterfaceManager.LIST);
    }
}
