package com.srm.mig.kafka.transform;

import java.util.Map;

/**
 * Interface defining the contract for transforming a Source message to a Target message.
 * This abstracts away the specific transformation engine (e.g., SpEL, pure JSON manipulation).
 */
public interface MessageTransformer {

    /**
     * Transforms the source payload based on the provided mapping rules.
     *
     * @param sourcePayload The incoming data from the Source DB (e.g., as a Map representing JSON).
     * @param mappingRules  The rules defining how to construct the target payload.
     * @return The transformed Target payload ready to be UPSERTed.
     */
    Map<String, Object> transform(Map<String, Object> sourcePayload, Map<String, Object> mappingRules);
}
