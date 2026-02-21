package c.y.mig.worker.strategy.impl;

import org.springframework.stereotype.Component;

import c.y.mig.model.MigrationList;
import c.y.mig.model.MigrationSchema;
import c.y.mig.worker.strategy.AbstractMigrationStrategy;
import c.y.mig.worker.strategy.ProgressListener;

@Component("TABLE")
public class TableMigrationStrategy extends AbstractMigrationStrategy {
    @Override
    public void execute(MigrationSchema schema, MigrationList workList, ProgressListener listener) throws Exception {
        logStart(workList.getMig_name());
        // Logic for TABLE migration
        log.info("Executing TABLE migration for: " + workList.getMig_list_seq());
        logEnd(workList.getMig_name(), System.currentTimeMillis());
    }
}
