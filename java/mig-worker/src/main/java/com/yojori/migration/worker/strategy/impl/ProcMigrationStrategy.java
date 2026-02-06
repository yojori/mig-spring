package com.yojori.migration.worker.strategy.impl;

import com.yojori.migration.worker.model.MigrationList;
import com.yojori.migration.worker.model.MigrationSchema;
import com.yojori.migration.worker.strategy.AbstractMigrationStrategy;
import com.yojori.migration.worker.strategy.ProgressListener;
import org.springframework.stereotype.Component;

@Component("PROC")
public class ProcMigrationStrategy extends AbstractMigrationStrategy {
    @Override
    public void execute(MigrationSchema schema, MigrationList workList, ProgressListener listener) throws Exception {
        logStart(workList.getMig_name());
        // Logic for PROC migration
        log.info("Executing PROC migration for: " + workList.getMig_list_seq());
        logEnd(workList.getMig_name(), System.currentTimeMillis());
    }
}
