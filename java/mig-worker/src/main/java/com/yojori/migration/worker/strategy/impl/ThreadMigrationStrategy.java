package com.yojori.migration.worker.strategy.impl;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.yojori.db.query.Select;
import com.yojori.migration.worker.model.InsertSql;
import com.yojori.migration.worker.model.InsertTable;
import com.yojori.migration.worker.model.MigrationList;
import com.yojori.migration.worker.model.MigrationSchema;
import com.yojori.migration.worker.model.Search;
import com.yojori.migration.worker.service.PagingQueryBuilder;
import com.yojori.migration.worker.strategy.AbstractMigrationStrategy;
import com.yojori.migration.worker.strategy.ProgressListener;
import com.yojori.util.StringUtil;

@Component("THREAD")
public class ThreadMigrationStrategy extends AbstractMigrationStrategy {

    @Autowired
    private PagingQueryBuilder pagingQueryBuilder;

    @Override
    public void execute(MigrationSchema schema, MigrationList workList, ProgressListener listener) throws Exception {
        logStart(workList.getMig_name());
        
        final int threadCount = (workList.getThread_count() <= 0) ? 1 : workList.getThread_count();
        final int pageSize = (workList.getPage_count_per_thread() <= 0) ? 1000 : workList.getPage_count_per_thread();
        
        log.info("Starting Threaded Migration. Threads: {}, PageSize: {}", threadCount, pageSize);

        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        AtomicInteger totalProcessed = new AtomicInteger(0);
        AtomicInteger totalRead = new AtomicInteger(0);
        java.util.Timer progressTimer = new java.util.Timer(true);
        progressTimer.scheduleAtFixedRate(new java.util.TimerTask() {
            @Override
            public void run() {
                if (listener != null) listener.onProgress(totalRead.get(), totalProcessed.get());
            }
        }, 1000, 3000);

        try {
            // Truncate if needed (Single threaded before workers start)
            prepareTargetTable(schema);

            for (int i = 0; i < threadCount; i++) {
                final int threadNum = i;
                executor.submit(() -> {
                    try {
                        processThread(threadNum, threadCount, pageSize, schema, workList, totalProcessed, totalRead);
                    } catch (Exception e) {
                        log.error("Thread " + threadNum + " failed", e);
                    }
                });
            }
            
            executor.shutdown();
            // Wait for all threads to finish
            if (!executor.awaitTermination(24, TimeUnit.HOURS)) { // Long timeout for migration
                executor.shutdownNow();
            }

        } catch (Exception e) {
            log.error("Migration execution failed", e);
            executor.shutdownNow();
            throw e;
        } finally {
            if (progressTimer != null) progressTimer.cancel();
        }

        log.info("Total Processed Rows across all threads: {}", totalProcessed.get());
        if (listener != null) listener.onProgress(totalRead.get(), totalProcessed.get());
        logEnd(workList.getMig_name(), System.currentTimeMillis());
    }
    
    private void prepareTargetTable(MigrationSchema schema) throws SQLException {
        Connection targetConn = null;
        try {
             targetConn = dynamicDataSource.getConnection(schema.getTarget());
             List<InsertTable> tables = schema.getInsertTableList();
             if (tables != null && !tables.isEmpty()) {
                 for (InsertTable t : tables) {
                     if ("Y".equalsIgnoreCase(StringUtil.nvl(t.getTruncate_yn()))) {
                         log.info("Truncating Table: {}", t.getTarget_table());
                         executeTruncate(targetConn, t.getTarget_table());
                     }
                 }
             } else {
                 if (schema.getInsertSqlList() != null) {
                     for (InsertSql s : schema.getInsertSqlList()) {
                        if ("Y".equalsIgnoreCase(StringUtil.nvl(s.getTruncate_yn()))) {
                             log.info("Truncating Table: {}", s.getInsert_table());
                             executeTruncate(targetConn, s.getInsert_table());
                        }
                     }
                 }
             }
             // Explicitly commit
             if (!targetConn.getAutoCommit()) {
                 targetConn.commit();
             } else {
                 // Even if auto-commit is true, some drivers might need a nudge or we leave it.
                 // But if we want to be sure execution happened.
                 // Actually, if auto-commit is true, executeUpdate commits.
             }
        } finally {
            closeResources(null, null, targetConn);
        }
    }

    private void processThread(int threadNum, int threadCount, int pageSize, MigrationSchema schema, MigrationList workList, AtomicInteger totalProcessed, AtomicInteger totalRead) {
        Connection sourceConn = null;
        Connection targetConn = null;
        PreparedStatement sourcePstmt = null;
        PreparedStatement[] targetPstmts = null;
        ResultSet sourceRs = null;
        
        int currentPage = threadNum + 1; // 1-based page index
        int loopParams = 0; // equivalent to 'whileLoop' in legacy

        long threadStartTime = System.currentTimeMillis();

        try {
            // 1. Prepare Target Connection (Per thread)
            targetConn = dynamicDataSource.getConnection(schema.getTarget());
            targetConn.setAutoCommit(false);
            
            List<InsertSql> sqlList = schema.getInsertSqlList();
            targetPstmts = new PreparedStatement[sqlList.size()];
            for (int i = 0; i < sqlList.size(); i++) {
                String query = buildTargetQuery(sqlList.get(i), schema.getInsertColumnList());
                if (query != null) {
                    targetPstmts[i] = targetConn.prepareStatement(query);
                }
            }

            // 2. Open Source Connection (Reuse for the thread)
            sourceConn = dynamicDataSource.getConnection(schema.getSource());

            // 3. Loop until no data
            while (true) {
                long stepStartTime = System.currentTimeMillis();

                // Loop setup
                String sqlSource = buildSourceQuery(schema, workList);
                
                Search form = new Search();
                form.setCurrentPage(currentPage);
                form.setPageSize(pageSize);

                // Use PagingQueryBuilder
                String pagingQuery = pagingQueryBuilder.buildPagingQuery(sqlSource, workList.getSource_db_type(), form);

                log.info("Thread[{}] Paging Query: {}", threadNum, pagingQuery);

                String dbType = workList.getSource_db_type();
                int p1 = 0;
                int p2 = 0;
                if ("oracle".equalsIgnoreCase(dbType)) {
                    p1 = (form.getCurrentPage() * form.getPageSize());
                    p2 = ((form.getCurrentPage() - 1) * form.getPageSize());
                } else if ("mysql".equalsIgnoreCase(dbType) || "mssql".equalsIgnoreCase(dbType)) {
                    p1 = ((form.getCurrentPage() - 1) * form.getPageSize());
                    p2 = form.getPageSize();
                } else if ("maria".equalsIgnoreCase(dbType)) {
                    p1 = form.getPageSize();
                    p2 = ((form.getCurrentPage() - 1) * form.getPageSize());
                }

                log.info("Thread[{}] Paging Params: 1=[{}], 2=[{}]", threadNum, p1, p2);
                
                sourcePstmt = sourceConn.prepareStatement(pagingQuery);
                pagingQueryBuilder.setPagingParams(sourcePstmt, 0, form, workList.getSource_db_type());
                
                // Note: Not setting fetchSize might be okay if we are paging, but valid optimization
                sourcePstmt.setFetchSize(pageSize); 
                
                sourceRs = sourcePstmt.executeQuery();
                ResultSetMetaData meta = sourceRs.getMetaData();
                int colCount = meta.getColumnCount();
                
                int rowCount = 0;
                while (sourceRs.next()) {
                    // Map row data
                    Map<String, Object> row = new HashMap<>();
                    for (int i = 1; i <= colCount; i++) {
                        String colName = meta.getColumnName(i).toUpperCase();
                        row.put(colName, sourceRs.getObject(i));
                    }

                    // Add to batch
                    for (int i = 0; i < sqlList.size(); i++) {
                        if (targetPstmts[i] == null) continue;
                        setTargetParams(targetPstmts[i], sqlList.get(i), schema.getInsertColumnList(), row);
                        targetPstmts[i].addBatch();
                    }
                    rowCount++;
                }
                
                // Close source resources early for this page (Keep Connection Open)
                try { if (sourceRs != null) sourceRs.close(); } catch (Exception e) {}
                try { if (sourcePstmt != null) sourcePstmt.close(); } catch (Exception e) {}
                sourceRs = null; sourcePstmt = null;

                if (rowCount == 0) {
                    break; // No more data
                }

                totalRead.addAndGet(rowCount);

                // Execute Batch
                executeBatch(targetPstmts);
                targetConn.commit();
                
                totalProcessed.addAndGet(rowCount);
                
                long stepEndTime = System.currentTimeMillis();
                double stepSeconds = (stepEndTime - stepStartTime) / 1000.0;
                double totalSeconds = (stepEndTime - threadStartTime) / 1000.0;
                
                log.info("Thread {} processed Page {} (Rows: {}) - One select, insert time: {} seconds, Total elapsed: {} seconds", 
                        threadNum, currentPage, rowCount, String.format("%.3f", stepSeconds), String.format("%.3f", totalSeconds));

                // Next Page for this thread
                loopParams++;
                currentPage = (threadCount * loopParams) + threadNum + 1;
            }

        } catch (Exception e) {
            log.error("Error in thread " + threadNum, e);
            try { if (targetConn != null) targetConn.rollback(); } catch (SQLException ex) {}
        } finally {
            if (targetPstmts != null) {
                for (PreparedStatement ps : targetPstmts) closeResources(null, ps, null);
            }
            closeResources(sourceRs, sourcePstmt, sourceConn); // Ensure source is closed if error
            closeResources(null, null, targetConn);
        }
    }
    
    private String buildSourceQuery(MigrationSchema schema, MigrationList workList) {
        List<InsertTable> tables = schema.getInsertTableList();
        if (tables != null && !tables.isEmpty()) {
            InsertTable insertT = tables.get(0);
            Select select = new Select();
            select.addField(" * ");
            select.addFrom(insertT.getSource_table());
            if (!StringUtil.empty(insertT.getSource_pk())) {
                select.addOrder(insertT.getSource_pk()); // Important for consistent paging
            }
            return select.toQuery();
        } else {
             return workList.getSql_string();
        }
    }
}
