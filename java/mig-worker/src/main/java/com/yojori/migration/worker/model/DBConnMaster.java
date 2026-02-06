package com.yojori.migration.worker.model;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = false)
public class DBConnMaster extends Search {
    private String master_code;
    private String character_set;
    private String db_type;

    // Exteded fields for Worker connection
    private String jdbcUrl;
    private String username;
    private String password;
}
