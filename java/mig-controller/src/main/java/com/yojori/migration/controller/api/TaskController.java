package com.yojori.migration.controller.api;

import com.yojori.migration.controller.model.MigrationSchema;
import com.yojori.migration.controller.model.WorkerStatus;
import com.yojori.migration.controller.service.TaskService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/worker")
public class TaskController {

    @Autowired
    private TaskService taskService;

    @PostMapping("/allocate")
    public String allocate(@RequestParam String workerId) {
        String taskId = taskService.allocateTask(workerId);
        if (taskId == null) {
            log.info("No Work to do, for worker: {}", workerId);
        }
        else{
            log.info("Request from worker: {}, Assigned work: {}", workerId, taskId);
        }
        return taskId;
    }

    @GetMapping("/task/{taskId}/config")
    public MigrationSchema getConfig(@PathVariable String taskId) {
        return taskService.getTaskConfig(taskId);
    }

    @PostMapping("/status")
    public void status(@RequestBody WorkerStatus status) {
        taskService.updateStatus(status);
    }

    @PostMapping("/child-task")
    public void createChildTask(@RequestBody com.yojori.migration.controller.model.MigrationList childTask) {
        taskService.createChildTask(childTask);
    }

    @GetMapping("/db-connections")
    public java.util.List<com.yojori.migration.controller.model.DBConnMaster> getDBConnections() {
        return taskService.getAllDBConnections();
    }
}
