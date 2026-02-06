package com.yojori.migration.worker.model;

import lombok.Data;

@Data
public class WorkerStatus {
    private String workerId;
    private String taskId;
    private String status; // START, RUNNING, COMPLETED, FAILED
    private String message;
    private long processedCount;
}
