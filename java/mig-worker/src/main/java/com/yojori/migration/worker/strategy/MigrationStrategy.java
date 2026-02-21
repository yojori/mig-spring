package com.yojori.migration.worker.strategy;

import com.yojori.model.MigrationList;
import com.yojori.model.MigrationSchema; // Assuming this model needs to be created or imported from controller model if shared

public interface MigrationStrategy {
    void prepare(MigrationSchema schema, MigrationList workList) throws Exception;
    void execute(MigrationSchema schema, MigrationList workList, ProgressListener listener) throws Exception;

}
