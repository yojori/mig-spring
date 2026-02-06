package com.yojori.migration.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@SpringBootTest
public class DataGenTest {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    public void generateBigData() {
        String tableName = "ZXXMIG_TEST_BIG";
        
        // 1. Create Table
        jdbcTemplate.execute("DROP TABLE IF EXISTS " + tableName);
        jdbcTemplate.execute("CREATE TABLE " + tableName + " (" +
                "SEQ INT AUTO_INCREMENT PRIMARY KEY, " +
                "TITLE VARCHAR(200), " +
                "CONTENT TEXT, " +
                "REG_DATE DATETIME, " +
                "DUMMY1 VARCHAR(100), " +
                "DUMMY2 VARCHAR(100) " +
                ") ENGINE=InnoDB");

        log.info("Table " + tableName + " created.");

        // 2. Insert Data
        int totalRecords = 3000000;
        int batchSize = 5000;
        
        String sql = "INSERT INTO " + tableName + " (TITLE, CONTENT, REG_DATE, DUMMY1, DUMMY2) VALUES (?, ?, NOW(), ?, ?)";

        for (int i = 0; i < totalRecords; i += batchSize) {
            final int currentBatchStart = i;
            final int currentBatchSize = Math.min(batchSize, totalRecords - i);

            jdbcTemplate.batchUpdate(sql, new BatchPreparedStatementSetter() {
                @Override
                public void setValues(PreparedStatement ps, int j) throws SQLException {
                    int seq = currentBatchStart + j + 1;
                    ps.setString(1, "Title " + seq);
                    ps.setString(2, "Content for record " + seq + ". This is some dummy text to fill up space.");
                    ps.setString(3, "DummyData1_" + seq);
                    ps.setString(4, "DummyData2_" + seq);
                }

                @Override
                public int getBatchSize() {
                    return currentBatchSize;
                }
            });
            
            if ((i + batchSize) % 100000 == 0) {
                log.info("Inserted " + (i + batchSize) + " records...");
            }
        }
        
        log.info("Finished generating " + totalRecords + " records in " + tableName);
    }
}
