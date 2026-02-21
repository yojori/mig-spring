package c.y.mig.worker.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import c.y.mig.worker.strategy.MigrationFactory;
import c.y.mig.worker.strategy.MigrationStrategy;
import c.y.mig.worker.strategy.ProgressListener;
import c.y.mig.model.MigrationList;
import c.y.mig.model.MigrationSchema;

@Service
public class MigrationExecutor {
    private static final Logger log = LoggerFactory.getLogger(MigrationExecutor.class);

    @Autowired
    private MigrationFactory migrationFactory;

    public void execute(MigrationSchema schema, ProgressListener listener) throws Exception {
        log.info("Starting Migration Execution for Master: {}", schema.getMaster().getMaster_code());

        if (schema.getMigList() == null || schema.getMigList().isEmpty()) {
            log.warn("No migration list found in schema.");
            return;
        }

        for (MigrationList task : schema.getMigList()) {
            String migType = task.getMig_type();
            log.info("Executing Strategy for Type: {}", migType);
            
            MigrationStrategy strategy = migrationFactory.getStrategy(migType);

            if (strategy != null) {
                strategy.prepare(schema, task);
                strategy.execute(schema, task, listener);
            } else {
                throw new UnsupportedOperationException("No strategy found for type: " + migType);
            }
        }
        
        log.info("Migration Execution Completed.");
    }
}
