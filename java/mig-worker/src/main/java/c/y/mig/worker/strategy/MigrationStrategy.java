package c.y.mig.worker.strategy;

import c.y.mig.model.MigrationList;
import c.y.mig.model.MigrationSchema;

public interface MigrationStrategy {
    void prepare(MigrationSchema schema, MigrationList workList) throws Exception;
    void execute(MigrationSchema schema, MigrationList workList, ProgressListener listener) throws Exception;

}
