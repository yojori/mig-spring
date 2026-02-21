package com.yojori.migration.worker.service;

import java.util.UUID;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Service;

import com.yojori.migration.worker.client.WorkerClient;
import com.yojori.model.MigrationSchema;
import com.yojori.model.WorkerStatus;

@Service
public class WorkerService implements CommandLineRunner, InitializingBean {
    private static final Logger log = LoggerFactory.getLogger(WorkerService.class);

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

    @Autowired
    private DynamicDataSource dynamicDataSource;

    @Override
    public void run(String... args) throws Exception {
        log.info("Worker Started with ID: " + workerId);
        
        // Init DB Pools
        try {
            java.util.List<com.yojori.model.DBConnMaster> conns = workerClient.getDBConnections();
            dynamicDataSource.initializePools(conns);
            log.info("Initialized {} DB connection pools.", (conns != null ? conns.size() : 0));
        } catch (Exception e) {
             log.error("Failed to initialize DB pools: " + e.getMessage());
        }

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

            // Execute Logic with Listener
            migrationExecutor.execute(schema, (read, proc) -> {
                status.setReadCount(read);
                status.setProcessedCount(proc);
                status.setStatus("RUNNING"); // Ensure status is RUNNING
                try {
                    workerClient.updateStatus(status);
                } catch (Exception e) {
                    log.error("Failed to update progress: " + e.getMessage());
                }
            });

            // Update Status: COMPLETED
            status.setStatus("COMPLETED");
            // status.setProcessedCount(100L); // Processed count already updated by listener
            workerClient.updateStatus(status);

        } catch (Exception e) {
            status.setStatus("FAILED");
            status.setMessage(e.getMessage());
            workerClient.updateStatus(status);
            log.error("Task failed", e);
        }
    }
}
