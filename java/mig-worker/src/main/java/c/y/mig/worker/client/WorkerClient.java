package c.y.mig.worker.client;

import c.y.mig.model.MigrationSchema;
import c.y.mig.model.WorkerStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Component
public class WorkerClient {
    private static final Logger log = LoggerFactory.getLogger(WorkerClient.class);

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

    public void createChildTask(c.y.mig.model.MigrationList childTask) {
        try {
            restTemplate.postForObject(controllerUrl + "/child-task", childTask, Void.class);
        } catch (Exception e) {
            log.error("Failed to create child task: " + e.getMessage());
            throw new RuntimeException("Failed to create child task", e);
        }
    }

    public java.util.List<c.y.mig.model.DBConnMaster> getDBConnections() {
        try {
            org.springframework.core.ParameterizedTypeReference<java.util.List<c.y.mig.model.DBConnMaster>> responseType =
                    new org.springframework.core.ParameterizedTypeReference<java.util.List<c.y.mig.model.DBConnMaster>>() {};
            
            org.springframework.http.ResponseEntity<java.util.List<c.y.mig.model.DBConnMaster>> response =
                    restTemplate.exchange(controllerUrl + "/db-connections", org.springframework.http.HttpMethod.GET, null, responseType);
            
            return response.getBody();
        } catch (Exception e) {
            log.error("Failed to get DB connections: " + e.getMessage());
            return new java.util.ArrayList<>();
        }
    }
}
