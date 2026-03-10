package com.srm.mig.kafka.transform;

import org.springframework.expression.EvaluationContext;
import org.springframework.expression.Expression;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * A PoC implementation of MessageTransformer using Spring Expression Language (SpEL).
 * This class demonstrates how external UI configuration (JSON mapping rules)
 * can dynamically transform real-time Kafka payloads without hard-coded mapping logic.
 */
public class SpelMessageTransformer implements MessageTransformer {

    private final ExpressionParser parser = new SpelExpressionParser();

    @Override
    @SuppressWarnings("unchecked")
    public Map<String, Object> transform(Map<String, Object> sourcePayload, Map<String, Object> mappingRules) {
        if (sourcePayload == null) {
            return new HashMap<>();
        }

        Map<String, Object> targetPayload = new HashMap<>();

        // The EvaluationContext exposes the sourcePayload as variables for SpEL expressions.
        // E.g., if sourcePayload has {"FIRST_NAME": "Gil"}, we can use #FIRST_NAME in SpEL.
        EvaluationContext context = new StandardEvaluationContext();
        for (Map.Entry<String, Object> entry : sourcePayload.entrySet()) {
            context.setVariable(entry.getKey(), entry.getValue());
        }

        // Expected mappingRules format (matching what UI would save in KFK_MAPPING):
        // {
        //   "mappings": [
        //     { "target": "id", "source": "USER_ID" }, // Direct mapping
        //     { "target": "full_name", "expression": "#FIRST_NAME + ' ' + #LAST_NAME" }, // SpEL mapping
        //     { "target": "is_adult", "expression": "#AGE >= 20 ? 'Y' : 'N'" } // Conditional SpEL
        //   ]
        // }
        List<Map<String, Object>> mappings = (List<Map<String, Object>>) mappingRules.get("mappings");

        if (mappings != null) {
            for (Map<String, Object> rule : mappings) {
                String targetField = (String) rule.get("target");

                if (rule.containsKey("expression")) {
                    // Evaluate complex SpEL expressions
                    String spelExpression = (String) rule.get("expression");
                    Expression exp = parser.parseExpression(spelExpression);
                    Object value = exp.getValue(context);
                    targetPayload.put(targetField, value);
                } else if (rule.containsKey("source")) {
                    // Simple direct 1:1 mapping mapping
                    String sourceField = (String) rule.get("source");
                    Object value = sourcePayload.get(sourceField);
                    targetPayload.put(targetField, value);
                } else if (rule.containsKey("default")) {
                   // Default constant value if neither source nor expression is provided
                    targetPayload.put(targetField, rule.get("default"));
                }
            }
        }

        return targetPayload;
    }
}
