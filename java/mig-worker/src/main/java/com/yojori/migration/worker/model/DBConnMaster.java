package com.yojori.migration.worker.model;

public class DBConnMaster extends Search {
    private String master_code;
    private String character_set;
    private String db_type;

    // Exteded fields for Worker connection
    private String jdbcUrl;
    private String username;
    private String password;

    public String getMaster_code() {
        return master_code;
    }

    public void setMaster_code(String master_code) {
        this.master_code = master_code;
    }

    public String getCharacter_set() {
        return character_set;
    }

    public void setCharacter_set(String character_set) {
        this.character_set = character_set;
    }

    public String getDb_type() {
        return db_type;
    }

    public void setDb_type(String db_type) {
        this.db_type = db_type;
    }

    public String getJdbcUrl() {
        return jdbcUrl;
    }

    public void setJdbcUrl(String jdbcUrl) {
        this.jdbcUrl = jdbcUrl;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
