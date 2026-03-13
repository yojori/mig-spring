package com.srm.mig.kafka.transform;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Unit test for SpelMessageTransformer to verify dynamic transformation capabilities.
 */
public class SpelMessageTransformerTest {

    private SpelMessageTransformer transformer;

    @BeforeEach
    public void setup() {
        this.transformer = new SpelMessageTransformer();
    }

    @Test
    public void testDefaultOneToOneMapping() {
        // Arrange
        Map<String, Object> sourcePayload = new HashMap<>();
        sourcePayload.put("ID", 100);
        sourcePayload.put("NAME", "Pocari");
        
        // Empty mapping rules
        Map<String, Object> mappingRules = new HashMap<>();

        // Act
        Map<String, Object> targetPayload = transformer.transform(sourcePayload, mappingRules);

        // Assert: Verify it defaults to 1:1
        Assertions.assertEquals(sourcePayload.size(), targetPayload.size());
        Assertions.assertEquals(100, targetPayload.get("ID"));
        Assertions.assertEquals("Pocari", targetPayload.get("NAME"));
        
        System.out.println("[Default 1:1] Target: " + targetPayload);
    }

    @Test
    public void testDynamicTransformation() {
        // 1. Arrange
        Map<String, Object> sourcePayload = new HashMap<>();
        sourcePayload.put("USER_ID", "USR999");
        sourcePayload.put("FIRST_NAME", "Gildong");
        sourcePayload.put("LAST_NAME", "Hong");
        sourcePayload.put("USER_AGE", 25);

        // 2. Arrange: Transformation Rules
        Map<String, Object> mappingRules = new HashMap<>();
        List<Map<String, Object>> mappingsList = new ArrayList<>();

        mappingsList.add(createMapping("id", "USER_ID", null));
        mappingsList.add(createMapping("full_name", null, "#FIRST_NAME + ' ' + #LAST_NAME"));
        mappingsList.add(createMapping("is_adult", null, "#USER_AGE >= 20 ? 'Y' : 'N'"));

        mappingRules.put("mappings", mappingsList);

        // 3. Act
        Map<String, Object> targetPayload = transformer.transform(sourcePayload, mappingRules);

        // 4. Assert
        Assertions.assertEquals("USR999", targetPayload.get("id"));
        Assertions.assertEquals("Gildong Hong", targetPayload.get("full_name"));
        Assertions.assertEquals("Y", targetPayload.get("is_adult"));
        
        System.out.println("[Transformation] Target: " + targetPayload);
    }

    private Map<String, Object> createMapping(String target, String source, String expression) {
        Map<String, Object> mapping = new HashMap<>();
        mapping.put("target", target);
        if (source != null) mapping.put("source", source);
        if (expression != null) mapping.put("expression", expression);
        return mapping;
    }
}
