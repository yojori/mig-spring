package com.yojori.migration.worker.strategy.impl;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;
import com.yojori.migration.worker.strategy.impl.KeysetMigrationStrategy;

public class ZXX_KeysetQueryTest {

    @Test
    public void testApplyLimit() {
        KeysetMigrationStrategy strategy = new KeysetMigrationStrategy();
        String baseQuery = "SELECT * FROM users ORDER BY id";
        int limit = 100;

        // 1. MySQL / Postgres (Default)
        assertEquals("SELECT * FROM users ORDER BY id LIMIT 100", 
            strategy.applyLimit(baseQuery, limit, "MYSQL"));
        
        assertEquals("SELECT * FROM users ORDER BY id LIMIT 100", 
            strategy.applyLimit(baseQuery, limit, "POSTGRESQL"));

        // 2. Oracle
        assertEquals("SELECT * FROM ( SELECT * FROM users ORDER BY id ) WHERE ROWNUM <= 100", 
            strategy.applyLimit(baseQuery, limit, "ORACLE"));

        // 3. MSSQL
        String mssqlExpected = "SELECT TOP 100 * FROM users ORDER BY id";
        assertEquals(mssqlExpected, 
            strategy.applyLimit(baseQuery, limit, "MSSQL"));
        
        // MSSQL Case Insensitive
        String lowerQuery = "select * from users order by id";
        String mssqlLowerExpected = "SELECT TOP 100 * from users order by id"; // Case preserved except SELECT
        // My impl replaces "SELECT" with "SELECT TOP N".
        // Base: "select * ..." -> replaceFirst("(?i)SELECT", ...) -> "SELECT TOP 100 * ..."
        assertEquals("SELECT TOP 100 * from users order by id", 
            strategy.applyLimit(lowerQuery, limit, "MSSQL"));

        // MSSQL Complex (CTE/WITH)
        String complexQuery = "WITH cte AS (SELECT * FROM foo) SELECT * FROM cte";
        String complexExpected = "SELECT TOP 100 * FROM ( WITH cte AS (SELECT * FROM foo) SELECT * FROM cte ) SUB";
        assertEquals(complexExpected, 
            strategy.applyLimit(complexQuery, limit, "MSSQL"));
    }
}
