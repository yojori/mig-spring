package com.yojori.migration.worker.strategy.impl;

import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;

import com.yojori.migration.worker.strategy.ProgressListener;
import com.yojori.model.MigrationList;
import com.yojori.model.MigrationSchema;

public class JavaMigrationStrategyTest {

    public static boolean wasCalled = false;

    public JavaMigrationStrategyTest() {
        // Public no-arg constructor required for instantiation by JavaMigrationStrategy
    }

    public void targetMethod(MigrationList list) {
        System.out.println("Target method called with list: " + list);
        wasCalled = true;
    }

    @Test
    public void testExecute() throws Exception {
        JavaMigrationStrategy strategy = new JavaMigrationStrategy();
        // Mock WorkerClient
        com.yojori.migration.worker.client.WorkerClient mockClient = org.mockito.Mockito.mock(com.yojori.migration.worker.client.WorkerClient.class);
        java.lang.reflect.Field clientField = JavaMigrationStrategy.class.getDeclaredField("workerClient");
        clientField.setAccessible(true);
        clientField.set(strategy, mockClient);

        MigrationList workList = new MigrationList();
        workList.setMig_name("TestJavaMig");
        // Parameter string pointing to this class and method
        workList.setParam_string("com.yojori.migration.worker.strategy.impl.JavaMigrationStrategyTest#targetMethod");

        MigrationSchema schema = new MigrationSchema();
        ProgressListener listener = (long readCount, long procCount) -> {
        };

        wasCalled = false;
        strategy.execute(schema, workList, listener);

        assertTrue(wasCalled, "The target method should have been called via reflection (param_string).");
    }

    @Test
    public void testExecuteWithInsertSql() throws Exception {
        JavaMigrationStrategy strategy = new JavaMigrationStrategy();
        // Mock WorkerClient
        com.yojori.migration.worker.client.WorkerClient mockClient = org.mockito.Mockito.mock(com.yojori.migration.worker.client.WorkerClient.class);
        java.lang.reflect.Field clientField = JavaMigrationStrategy.class.getDeclaredField("workerClient");
        clientField.setAccessible(true);
        clientField.set(strategy, mockClient);

        MigrationList workList = new MigrationList();
        workList.setMig_name("TestJavaMigSQL");
        
        // Setup schema with InsertSql
        MigrationSchema schema = new MigrationSchema();
        com.yojori.model.InsertSql insertSql = new com.yojori.model.InsertSql();
        insertSql.setInsert_table("com.yojori.migration.worker.strategy.impl.JavaMigrationStrategyTest");
        insertSql.setPk_column("targetMethod");
        
        java.util.List<com.yojori.model.InsertSql> sqlList = new java.util.ArrayList<>();
        sqlList.add(insertSql);
        schema.setInsertSqlList(sqlList);

        ProgressListener listener = (long readCount, long procCount) -> {
        };

        wasCalled = false;
        strategy.execute(schema, workList, listener);

        assertTrue(wasCalled, "The target method should have been called via reflection (InsertSql).");
    }
}
