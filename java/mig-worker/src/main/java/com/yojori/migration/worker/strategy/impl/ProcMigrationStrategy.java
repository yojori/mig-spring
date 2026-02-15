package com.yojori.migration.worker.strategy.impl;

import org.springframework.stereotype.Component;

import com.yojori.migration.worker.model.MigrationList;
import com.yojori.migration.worker.model.MigrationSchema;
import com.yojori.migration.worker.strategy.AbstractMigrationStrategy;
import com.yojori.migration.worker.strategy.ProgressListener;

@Component("PROC")
public class ProcMigrationStrategy extends AbstractMigrationStrategy {
    @Override
    public void execute(MigrationSchema schema, MigrationList workList, ProgressListener listener) throws Exception {
        logStart(workList.getMig_name());
        log.info("Starting Procedure Migration: {}", workList.getMig_name());

        java.sql.Connection targetConn = null;
        java.sql.Statement stmt = null;

        try {
            // 1. Connect to Target DB
            targetConn = dynamicDataSource.getConnection(schema.getTarget());
            targetConn.setAutoCommit(false); // Procedures might handle their own tx or we commit at end

            String sql = workList.getSql_string();
            if (com.yojori.util.StringUtil.empty(sql)) {
                log.error("No SQL/Procedure defined for migration: {}", workList.getMig_list_seq());
                return;
            }
            
            sql = sql.trim();
            // detailed check if it is a call or raw sql
            // simple heuristic: if it doesn't start with known keywords, assume procedure name
            String upperSql = sql.toUpperCase();
            if (!upperSql.startsWith("CALL") && !upperSql.startsWith("SELECT") && 
                !upperSql.startsWith("INSERT") && !upperSql.startsWith("UPDATE") && 
                !upperSql.startsWith("DELETE") && !upperSql.startsWith("TRUNCATE") &&
                !upperSql.startsWith("BEGIN")) {
                
                // Smart quoting for hyphens
                if (sql.contains("-")) {
                    String[] parts = sql.split("\\.");
                    StringBuilder sb = new StringBuilder();
                    for (int i = 0; i < parts.length; i++) {
                        String part = parts[i].trim();
                        // If part has hyphen and not quoted, quote it.
                        if (part.contains("-") && !part.startsWith("\"")) {
                            part = "\"" + part + "\"";
                        }
                        if (i > 0) sb.append(".");
                        sb.append(part);
                    }
                    sql = sb.toString();
                }

                if (!sql.endsWith(")")) {
                    sql = sql + "()";
                }
                sql = "CALL " + sql;
            }

            log.info("Executing SQL: {}", sql);

            // 2. Execute SQL
            stmt = targetConn.createStatement();
            stmt.execute(sql);
            
            // 3. Commit
            targetConn.commit();
            log.info("Procedure executed successfully.");

        } catch (Exception e) {
            log.error("Procedure Migration failed", e);
            if (targetConn != null) {
                try { targetConn.rollback(); } catch (java.sql.SQLException ex) {}
            }
            throw e;
        } finally {
            closeResources(null, stmt, targetConn);
        }

        logEnd(workList.getMig_name(), System.currentTimeMillis());
    }
}
