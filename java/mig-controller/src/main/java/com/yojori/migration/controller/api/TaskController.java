package com.yojori.migration.controller.api;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.yojori.migration.controller.service.TaskService;
import com.yojori.model.MigrationSchema;
import com.yojori.model.WorkerStatus;

@RestController
@RequestMapping("/api/worker")
public class TaskController {

    private static final Logger log = LoggerFactory.getLogger(TaskController.class);

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
    public void createChildTask(@RequestBody com.yojori.model.MigrationList childTask) {
        taskService.createChildTask(childTask);
    }

    @GetMapping("/db-connections")
    public java.util.List<com.yojori.model.DBConnMaster> getDBConnections() {
        return taskService.getAllDBConnections();
    }
}
