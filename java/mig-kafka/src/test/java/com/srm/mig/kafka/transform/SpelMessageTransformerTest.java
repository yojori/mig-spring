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
    public void testDynamicTransformation() {
        // 1. Arrange: Setup incoming Source Payload (e.g., from Source DB via Kafka)
        Map<String, Object> sourcePayload = new HashMap<>();
        sourcePayload.put("USER_ID", "USR999");
        sourcePayload.put("FIRST_NAME", "Gildong");
        sourcePayload.put("LAST_NAME", "Hong");
        sourcePayload.put("USER_AGE", 25);
        sourcePayload.put("STATUS_CODE", "A");

        // 2. Arrange: Setup Mapping Rules (e.g., from Controller DB UI 'KFK_MAPPING')
        Map<String, Object> mappingRules = new HashMap<>();
        List<Map<String, Object>> mappingsList = new ArrayList<>();

        // Rule A: Direct Mapping
        Map<String, Object> mapping1 = new HashMap<>();
        mapping1.put("target", "id");
        mapping1.put("source", "USER_ID");
        mappingsList.add(mapping1);

        // Rule B: SpEL Expression (String Concatenation)
        Map<String, Object> mapping2 = new HashMap<>();
        mapping2.put("target", "full_name");
        mapping2.put("expression", "#FIRST_NAME + ' ' + #LAST_NAME");
        mappingsList.add(mapping2);

        // Rule C: SpEL Expression (Conditional Logic/Type Conversion)
        Map<String, Object> mapping3 = new HashMap<>();
        mapping3.put("target", "is_adult");
        mapping3.put("expression", "#USER_AGE >= 20 ? 'Y' : 'N'");
        mappingsList.add(mapping3);

        // Rule D: SpEL Expression (Code Mapping Logic)
        Map<String, Object> mapping4 = new HashMap<>();
        mapping4.put("target", "status_desc");
        mapping4.put("expression", "#STATUS_CODE == 'A' ? 'Active' : 'Inactive'");
        mappingsList.add(mapping4);

        mappingRules.put("mappings", mappingsList);

        // 3. Act: Transform the payload
        Map<String, Object> targetPayload = transformer.transform(sourcePayload, mappingRules);

        // 4. Assert: Verify the dynamically generated Target Payload
        Assertions.assertEquals("USR999", targetPayload.get("id"));
        Assertions.assertEquals("Gildong Hong", targetPayload.get("full_name"));
        Assertions.assertEquals("Y", targetPayload.get("is_adult"));
        Assertions.assertEquals("Active", targetPayload.get("status_desc"));
        
        System.out.println("Source Payload: " + sourcePayload);
        System.out.println("Mapping Rules : " + mappingRules);
        System.out.println("Target Payload: " + targetPayload);
    }
}
