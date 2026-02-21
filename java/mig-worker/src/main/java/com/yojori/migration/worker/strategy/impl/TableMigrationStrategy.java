package com.yojori.migration.worker.strategy.impl;

import org.springframework.stereotype.Component;

import com.yojori.migration.worker.strategy.AbstractMigrationStrategy;
import com.yojori.migration.worker.strategy.ProgressListener;
import com.yojori.model.MigrationList;
import com.yojori.model.MigrationSchema;

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
