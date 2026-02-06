package com.yojori.util;

import org.springframework.stereotype.Component;
import java.util.HashMap;
import java.util.Map;

// Simplified Config to satisfy legacy dependencies using it static way.
// In Spring Boot, we should use @Value or Environment, but for legacy porting we wrap it.
public class Config {

    // Legacy static accessor
    private static Config instance = new Config();
    private static Map<String, String> properties = new HashMap<>();

    public static Config getConfig() {
        return instance;
    }

    public String getString(String key) {
        return properties.getOrDefault(key, "");
    }



    public static String getOrdNoSequence(String prefix) {
        return prefix + "-" + Long.toString(System.currentTimeMillis(), 36);
    }

}