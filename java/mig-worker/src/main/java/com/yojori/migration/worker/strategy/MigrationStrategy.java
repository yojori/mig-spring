package com.yojori.migration.worker.strategy;

import com.yojori.migration.worker.model.MigrationSchema;
import com.yojori.migration.worker.model.MigrationList; // Assuming this model needs to be created or imported from controller model if shared

public interface MigrationStrategy {
    void execute(MigrationSchema schema, MigrationList workList, ProgressListener listener) throws Exception;
}
