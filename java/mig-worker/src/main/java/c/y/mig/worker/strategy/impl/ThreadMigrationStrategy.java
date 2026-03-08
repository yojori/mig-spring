package c.y.mig.worker.strategy.impl;

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

import c.y.mig.model.InsertSql;
import c.y.mig.model.InsertTable;
import c.y.mig.model.MigrationList;
import c.y.mig.model.MigrationSchema;
import c.y.mig.model.Search;
import c.y.mig.util.StringUtil;
import c.y.mig.worker.service.PagingQueryBuilder;
import c.y.mig.worker.strategy.AbstractMigrationStrategy;
import c.y.mig.worker.strategy.ProgressListener;

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
        // progressTimer removed to reduce database load

        try {
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
            // progressTimer cleanup removed
        }

        log.info("Total Processed Rows across all threads: {}", totalProcessed.get());
        if (listener != null) listener.onProgress(totalRead.get(), totalProcessed.get());
        logEnd(workList.getMig_name(), System.currentTimeMillis());
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
            
            // Determine Target Info
            String targetTable = workList.getTarget_table();
            if (StringUtil.empty(targetTable)) {
                List<InsertSql> sqlList = schema.getInsertSqlList();
                if (sqlList != null && !sqlList.isEmpty()) {
                    targetTable = sqlList.get(0).getInsert_table();
                }
            }

            List<c.y.mig.model.InsertColumn> allColumns = schema.getInsertColumnList();
            String query = buildTargetQuery(workList, allColumns, schema.getTarget().getDb_type());
            
            targetPstmts = new PreparedStatement[1];
            if (query != null) {
                targetPstmts[0] = targetConn.prepareStatement(query);
            }

            // 2. Open Source Connection (Reuse for the thread)
            sourceConn = dynamicDataSource.getConnection(schema.getSource());

            // 3. Loop until no data
            while (true) {
                long stepStartTime = System.currentTimeMillis();

                // Loop setup
                String sqlSource = workList.getSql_string();
                String pkCol = workList.getSource_pk();
                
                // Fallback
                if (StringUtil.empty(sqlSource)) {
                    List<InsertTable> tables = schema.getInsertTableList();
                    if (tables != null && !tables.isEmpty()) {
                        sqlSource = "SELECT * FROM " + tables.get(0).getSource_table();
                        if (StringUtil.empty(pkCol)) pkCol = tables.get(0).getSource_pk();
                    }
                }

                if (!StringUtil.empty(pkCol)) {
                    String upperSql = sqlSource.toUpperCase();
                    if (!upperSql.contains("ORDER BY") && !upperSql.contains("GROUP BY")) {
                        sqlSource += " ORDER BY " + pkCol;
                    }
                }
                
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
                } else if ("maria".equalsIgnoreCase(dbType) || "mariadb".equalsIgnoreCase(dbType)) {
                    p1 = form.getPageSize();
                    p2 = ((form.getCurrentPage() - 1) * form.getPageSize());
                } else if ("postgresql".equalsIgnoreCase(dbType) || "postgres".equalsIgnoreCase(dbType)) {
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
                
                // [Optimization] Cache column names outside row loop
                String[] colNames = new String[colCount];
                for (int i = 1; i <= colCount; i++) {
                    colNames[i-1] = meta.getColumnName(i).toUpperCase();
                }

                int rowCount = 0;
                while (sourceRs.next()) {
                    // Map row data
                    Map<String, Object> row = new HashMap<>();
                    for (int i = 1; i <= colNames.length; i++) {
                        row.put(colNames[i-1], sourceRs.getObject(i));
                    }

                    // Add to batch
                    if (targetPstmts[0] != null) {
                        setTargetParams(targetPstmts[0], workList, allColumns, row);
                        targetPstmts[0].addBatch();
                    }
                    rowCount++;
                }
                long endFetchTime = System.currentTimeMillis();
                long fetchDuration = endFetchTime - stepStartTime;
                
                // Close source resources early for this page (Keep Connection Open)
                try { if (sourceRs != null) sourceRs.close(); } catch (Exception e) {}
                try { if (sourcePstmt != null) sourcePstmt.close(); } catch (Exception e) {}
                sourceRs = null; sourcePstmt = null;

                if (rowCount == 0) {
                    break; // No more data
                }

                totalRead.addAndGet(rowCount);

                // Execute Batch
                long startWrite = System.currentTimeMillis();
                executeBatch(targetPstmts);
                targetConn.commit();
                long writeDuration = System.currentTimeMillis() - startWrite;
                
                totalProcessed.addAndGet(rowCount);
                
                long stepEndTime = System.currentTimeMillis();
                double totalSeconds = (stepEndTime - threadStartTime) / 1000.0;
                
                log.info("Thread {} processed Page {} (Rows: {}) - Read(Fetch): {}ms, Write(Batch): {}ms, Total: {}ms, Total elapsed: {} seconds", 
                        threadNum, currentPage, rowCount, fetchDuration, writeDuration, (stepEndTime - stepStartTime), String.format("%.3f", totalSeconds));

                // Logging Detail
                saveWorkDetail(schema.getWork_seq(), threadNum, currentPage, pagingQuery, 
                               rowCount, (int)fetchDuration, rowCount, (int)writeDuration, "SUCCESS", null);

                // Next Page for this thread
                loopParams++;
                currentPage = (threadCount * loopParams) + threadNum + 1;
            }

        } catch (Exception e) {
            log.error("Error in thread " + threadNum, e);
            saveWorkDetail(schema.getWork_seq(), threadNum, currentPage, "ERROR", 
                           0, 0, 0, 0, "FAIL", e.getMessage());
            try { if (targetConn != null) targetConn.rollback(); } catch (SQLException ex) {}
        } finally {
            if (targetPstmts != null) {
                for (PreparedStatement ps : targetPstmts) closeResources(null, ps, null);
            }
            closeResources(sourceRs, sourcePstmt, sourceConn); // Ensure source is closed if error
            closeResources(null, null, targetConn);
        }
    }
}
