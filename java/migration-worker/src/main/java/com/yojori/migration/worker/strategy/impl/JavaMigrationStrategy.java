package com.yojori.migration.worker.strategy.impl;

import com.yojori.migration.worker.model.MigrationList;
import com.yojori.migration.worker.model.MigrationSchema;
import com.yojori.migration.worker.strategy.AbstractMigrationStrategy;
import org.springframework.stereotype.Component;

@Component("JAVA")
public class JavaMigrationStrategy extends AbstractMigrationStrategy {
    @Override
    public void execute(MigrationSchema schema, MigrationList workList) throws Exception {
        logStart(workList.getMig_name());
        // Logic for JAVA migration (Reflection call)
        log.info("Executing JAVA migration for: " + workList.getMig_list_seq());
        logEnd(workList.getMig_name(), System.currentTimeMillis());
    }
}
