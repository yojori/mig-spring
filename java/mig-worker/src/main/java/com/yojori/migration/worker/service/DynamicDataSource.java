package com.yojori.migration.worker.service;

import com.yojori.migration.worker.model.DBConnMaster;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.springframework.stereotype.Component;

import jakarta.annotation.PreDestroy;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class DynamicDataSource {

    private final Map<String, HikariDataSource> dataSourceCache = new ConcurrentHashMap<>();

    public Connection getConnection(DBConnMaster dbConfig) throws SQLException {
        if (dbConfig == null || dbConfig.getJdbcUrl() == null) {
            throw new SQLException("Database configuration is missing or invalid");
        }

        String cacheKey = dbConfig.getJdbcUrl() + "|" + dbConfig.getUsername();
        
        HikariDataSource ds = dataSourceCache.computeIfAbsent(cacheKey, k -> createDataSource(dbConfig));
        
        return ds.getConnection();
    }

    private HikariDataSource createDataSource(DBConnMaster dbConfig) {
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl(dbConfig.getJdbcUrl());
        config.setUsername(dbConfig.getUsername());
        config.setPassword(dbConfig.getPassword());
        
        // Pool Settings
        config.setMaximumPoolSize(30); // Requested by User
        config.setMinimumIdle(5);
        config.setIdleTimeout(600000); // 10 min
        config.setConnectionTimeout(30000); // 30 sec
        config.setMaxLifetime(1800000); // 30 min
        
        // Optional: Driver specific optimizations
        // config.addDataSourceProperty("cachePrepStmts", "true");
        // config.addDataSourceProperty("prepStmtCacheSize", "250");
        // config.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");

        return new HikariDataSource(config);
    }
    
    @PreDestroy
    public void cleanup() {
        for (HikariDataSource ds : dataSourceCache.values()) {
            if (ds != null && !ds.isClosed()) {
                ds.close();
            }
        }
        dataSourceCache.clear();
    }

    public void initializePools(java.util.List<DBConnMaster> connections) {
        if (connections == null) return;
        for (DBConnMaster db : connections) {
             try {
                 if (db.getJdbcUrl() != null) {
                     String cacheKey = db.getJdbcUrl() + "|" + db.getUsername();
                     dataSourceCache.computeIfAbsent(cacheKey, k -> createDataSource(db));
                     // log.info("Initialized pool for: " + db.getJdbcUrl());
                 }
             } catch (Exception e) {
                 // log.error("Failed to init pool for: " + db.getJdbcUrl());
             }
        }
    }
}
