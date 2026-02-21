package com.yojori.migration.controller.service;

import com.yojori.model.MigrationSchema;
import com.yojori.model.WorkerStatus;

public interface TaskService {
    String allocateTask(String workerId);

    MigrationSchema getTaskConfig(String taskId);

    void updateStatus(WorkerStatus status);

    void createChildTask(com.yojori.model.MigrationList childTask);

    java.util.List<com.yojori.model.DBConnMaster> getAllDBConnections();
}
