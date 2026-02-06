package com.yojori.migration.worker.strategy;

import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class MigrationFactory {

    @Autowired
    private Map<String, MigrationStrategy> strategyMap;

    public MigrationStrategy getStrategy(String migType) {
        if (migType == null) return null;
        
        // Strategy names are defined in @Component("NAME")
        return strategyMap.get(migType);
    }
}
