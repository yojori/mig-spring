package com.yojori.migration.controller.model;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = false)
public class DBConnMaster extends Search {
    private String master_code;
    private String character_set;
    private String db_type;
    private String db_alias;

    // Exteded fields for Worker connection
    private String driverClass;
    private String jdbcUrl;
    private String username;
    private String password;
}
