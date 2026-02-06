package com.yojori.migration.controller.service;

import com.yojori.migration.controller.model.MigrationSchema;
import com.yojori.migration.controller.model.WorkerStatus;

public interface TaskService {
    String allocateTask(String workerId);

    MigrationSchema getTaskConfig(String taskId);

    void updateStatus(WorkerStatus status);

    void createChildTask(com.yojori.migration.controller.model.MigrationList childTask);
}
