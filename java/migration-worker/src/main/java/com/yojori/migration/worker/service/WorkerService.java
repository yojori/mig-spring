package com.yojori.migration.worker.service;

import com.yojori.migration.worker.client.WorkerClient;
import com.yojori.migration.worker.model.MigrationSchema;
import com.yojori.migration.worker.model.WorkerStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Service;

import java.util.UUID;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.springframework.beans.factory.InitializingBean;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class WorkerService implements CommandLineRunner, InitializingBean {

    @Autowired
    private WorkerClient workerClient;

    @Autowired
    private MigrationExecutor migrationExecutor;

    @org.springframework.beans.factory.annotation.Value("${worker.id.suffix:001}")
    private String workerIdSuffix;

    @org.springframework.beans.factory.annotation.Value("${worker.id:}")
    private String configuredWorkerId;

    private String workerId;

    @Override
    public void afterPropertiesSet() throws Exception {
        if (configuredWorkerId != null && !configuredWorkerId.trim().isEmpty()) {
            this.workerId = configuredWorkerId;
        } else {
            try {
                String ip = java.net.InetAddress.getLocalHost().getHostAddress();
                this.workerId = ip + "-" + workerIdSuffix;
            } catch (Exception e) {
                this.workerId = "UNKNOWN-" + workerIdSuffix + "-" + UUID.randomUUID().toString();
            }
        }
    }

    private ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();

    @Override
    public void run(String... args) throws Exception {
        log.info("Worker Started with ID: " + workerId);
        startPolling();
    }

    private void startPolling() {
        scheduler.scheduleWithFixedDelay(() -> {
            try {
                String taskId = workerClient.allocateTask(workerId);
                if (taskId != null) {
                    processTask(taskId);
                }
            } catch (Exception e) {
                log.error("Polling error: " + e.getMessage());
            }
        }, 5, 5, TimeUnit.SECONDS);
    }

    private void processTask(String taskId) {
        log.info("Processing Task: " + taskId);

        // Update Status: RUNNING
        WorkerStatus status = new WorkerStatus();
        status.setWorkerId(workerId);
        status.setTaskId(taskId);
        status.setStatus("RUNNING");
        workerClient.updateStatus(status);

        try {
            // Get Config
            MigrationSchema schema = workerClient.getTaskConfig(taskId);

            // Execute Logic
            migrationExecutor.execute(schema);

            // Update Status: COMPLETED
            status.setStatus("COMPLETED");
            status.setProcessedCount(100L); // TODO: Return count from executor
            workerClient.updateStatus(status);

        } catch (Exception e) {
            status.setStatus("FAILED");
            status.setMessage(e.getMessage());
            workerClient.updateStatus(status);
            log.error("Task failed", e);
        }
    }
}
