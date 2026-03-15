package c.y.mig.manager;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;

@Component
public class KafkaConnectManager {

    private static final Logger log = LoggerFactory.getLogger(KafkaConnectManager.class);
    private static final ObjectMapper mapper = new ObjectMapper();
    private static final HttpClient client = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(5))
            .build();

    private String getEndpoint() {
        List<Map<String, String>> codes = CodeManager.getCodeList("KFK_CONNECT_ENDPOINT");
        if (codes != null && !codes.isEmpty()) {
            return codes.get(0).get("value");
        }
        return "http://localhost:8083"; // Fallback
    }

    public String getStatus(String connectorName) {
        try {
            String url = getEndpoint() + "/connectors/" + connectorName + "/status";
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .GET()
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200) {
                Map<String, Object> map = mapper.readValue(response.body(), Map.class);
                Map<String, Object> connector = (Map<String, Object>) map.get("connector");
                if (connector != null) {
                    return (String) connector.get("state");
                }
            } else if (response.statusCode() == 404) {
                return "NOT_FOUND";
            }
        } catch (Exception e) {
            log.error("Error getting status for " + connectorName + ": " + e.getMessage());
        }
        return "UNKNOWN";
    }

    public boolean createConnector(String name, String configJson) {
        try {
            String url = getEndpoint() + "/connectors";
            // Kafka Connect POST expects { "name": "...", "config": { ... } }
            Map<String, Object> payload = Map.of(
                "name", name,
                "config", mapper.readValue(configJson, Map.class)
            );
            
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(mapper.writeValueAsString(payload)))
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            return response.statusCode() == 201 || response.statusCode() == 409; // 409 means already exists
        } catch (Exception e) {
            log.error("Error creating connector " + name + ": " + e.getMessage());
        }
        return false;
    }

    public boolean pauseConnector(String connectorName) {
        return sendAction(connectorName, "pause");
    }

    public boolean resumeConnector(String connectorName) {
        return sendAction(connectorName, "resume");
    }

    public boolean deleteConnector(String connectorName) {
        try {
            String url = getEndpoint() + "/connectors/" + connectorName;
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .DELETE()
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            return response.statusCode() == 204 || response.statusCode() == 404;
        } catch (Exception e) {
            log.error("Error deleting connector " + connectorName + ": " + e.getMessage());
        }
        return false;
    }

    private boolean sendAction(String connectorName, String action) {
        try {
            String url = getEndpoint() + "/connectors/" + connectorName + "/" + action;
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .PUT(HttpRequest.BodyPublishers.noBody())
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            return response.statusCode() == 202;
        } catch (Exception e) {
            log.error("Error performing " + action + " on " + connectorName + ": " + e.getMessage());
        }
        return false;
    }
}
