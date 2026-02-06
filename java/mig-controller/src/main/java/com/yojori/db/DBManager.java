package com.yojori.db;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import lombok.extern.slf4j.Slf4j;
import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

@Component
@Slf4j
public class DBManager {

    private static DataSource dataSource;

    @Autowired
    public void setDataSource(DataSource dataSource) {
        log.info("DBManager.setDataSource called with: " + dataSource);
        DBManager.dataSource = dataSource;
    }

    @jakarta.annotation.PostConstruct
    public void init() {
        log.info("DBManager initialized. DataSource is: " + dataSource);
    }

    public static Connection getConnection() throws SQLException {
        if (dataSource == null) {
            log.error("DBManager.getConnection() - DataSource is NULL!");
            throw new SQLException("DataSource not initialized");
        }
        return dataSource.getConnection();
    }

    // Legacy helper for simple close
    public static void close(ResultSet rs, Statement stmt, Connection con) {
        try {
            if (rs != null)
                rs.close();
        } catch (Exception e) {
        }
        try {
            if (stmt != null)
                stmt.close();
        } catch (Exception e) {
        }
        try {
            if (con != null)
                con.close();
        } catch (Exception e) {
        }
    }

    // For Migration logic that requests specific DB alias connection
    // For Migration logic that requests specific DB alias connection
    public static Connection getMIGConnection(String dbAlias) throws SQLException {
        if (dbAlias == null || dbAlias.isEmpty()) {
            return getConnection();
        }

        // Use DBConnMasterManager to find connection details
        // Note: Instantiating Manager manually since this is a static helper method context
        // and DBConnMasterManager primarily uses static DBManager.getConnection()
        try {
            com.yojori.manager.DBConnMasterManager manager = new com.yojori.manager.DBConnMasterManager();
            com.yojori.migration.controller.model.DBConnMaster master = new com.yojori.migration.controller.model.DBConnMaster();
            master.setMaster_code(dbAlias);
            master = manager.find(master);

            if (master != null) {
                log.info("Attempting connection for " + dbAlias);
                log.info("Driver: " + master.getDriverClass());
                log.info("URL: " + master.getJdbcUrl());
                log.info("User: " + master.getUsername());
                
                // Load driver
                if (master.getDriverClass() != null && !master.getDriverClass().isEmpty()) {
                    try {
                        Class.forName(master.getDriverClass());
                    } catch (ClassNotFoundException e) {
                        log.error("Driver class not found: " + master.getDriverClass(), e);
                        throw e;
                    }
                }
                
                // Return connection
                return java.sql.DriverManager.getConnection(
                    master.getJdbcUrl(), 
                    master.getUsername(), 
                    master.getPassword()
                );
            } else {
                log.warn("DB Alias not found in DB_MASTER: " + dbAlias + ". Returning default connection.");
            }
        } catch (Exception e) {
            log.error("Failed to get connection for alias: " + dbAlias, e);
            throw new SQLException("Failed to get connection for alias: " + dbAlias, e);
        }

        return getConnection();
    }
}
