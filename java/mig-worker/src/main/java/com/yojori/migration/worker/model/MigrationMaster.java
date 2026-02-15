package com.yojori.migration.worker.model;

public class MigrationMaster extends Search {
    private String master_code;
    private String master_name;
    private String display_yn;
    private int ordering;

    public String getMaster_code() {
        return master_code;
    }

    public void setMaster_code(String master_code) {
        this.master_code = master_code;
    }

    public String getMaster_name() {
        return master_name;
    }

    public void setMaster_name(String master_name) {
        this.master_name = master_name;
    }

    public String getDisplay_yn() {
        return display_yn;
    }

    public void setDisplay_yn(String display_yn) {
        this.display_yn = display_yn;
    }

    public int getOrdering() {
        return ordering;
    }

    public void setOrdering(int ordering) {
        this.ordering = ordering;
    }
}
