package c.y.mig.controller.service;

import c.y.mig.model.MigrationSchema;
import c.y.mig.model.WorkerStatus;

public interface TaskService {
    String allocateTask(String workerId);

    MigrationSchema getTaskConfig(String taskId);

    void updateStatus(WorkerStatus status);

    void createChildTask(c.y.mig.model.MigrationList childTask);

    java.util.List<c.y.mig.model.DBConnMaster> getAllDBConnections();
}
