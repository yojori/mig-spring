package com.yojori.migration.worker.client;

import com.yojori.migration.worker.model.MigrationSchema;
import com.yojori.migration.worker.model.WorkerStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class WorkerClient {

    @Autowired
    private RestTemplate restTemplate;

    @Value("${migration.controller.url:http://localhost:8100/api/worker}")
    private String controllerUrl;

    public String allocateTask(String workerId) {
        try {
            return restTemplate.postForObject(controllerUrl + "/allocate?workerId=" + workerId, null, String.class);
        } catch (Exception e) {
            log.error("Failed to allocate task: " + e.getMessage());
            return null;
        }
    }

    public MigrationSchema getTaskConfig(String taskId) {
        return restTemplate.getForObject(controllerUrl + "/task/" + taskId + "/config", MigrationSchema.class);
    }

    public void updateStatus(WorkerStatus status) {
        try {
            restTemplate.postForObject(controllerUrl + "/status", status, Void.class);
        } catch (Exception e) {
            log.error("Failed to update status: " + e.getMessage());
        }
    }

    public void createChildTask(com.yojori.migration.worker.model.MigrationList childTask) {
        try {
            restTemplate.postForObject(controllerUrl + "/child-task", childTask, Void.class);
        } catch (Exception e) {
            log.error("Failed to create child task: " + e.getMessage());
            throw new RuntimeException("Failed to create child task", e);
        }
    }
}
