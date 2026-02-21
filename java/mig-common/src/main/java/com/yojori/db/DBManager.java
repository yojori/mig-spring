package com.yojori.db;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import javax.sql.DataSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.yojori.model.DBConnMaster;

@Component
public class DBManager {

    private static final Logger log = LoggerFactory.getLogger(DBManager.class);

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

    /**
     * Get a connection to a specific database using DBConnMaster configuration.
     * This is used for dynamic migration connections.
     */
    public static Connection getConnection(DBConnMaster master) throws SQLException {
        if (master == null) {
            return getConnection();
        }

        log.info("Attempting connection for code: " + master.getMaster_code());
        
        // Load driver
        if (master.getDriverClass() != null && !master.getDriverClass().isEmpty()) {
            try {
                Class.forName(master.getDriverClass());
            } catch (ClassNotFoundException e) {
                log.error("Driver class not found: " + master.getDriverClass(), e);
                throw new SQLException("Driver class not found", e);
            }
        }
        
        // Return connection
        return java.sql.DriverManager.getConnection(
            master.getJdbcUrl(), 
            master.getUsername(), 
            master.getPassword()
        );
    }
}
