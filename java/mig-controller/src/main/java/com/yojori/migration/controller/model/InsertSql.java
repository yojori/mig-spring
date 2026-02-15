package com.yojori.migration.controller.model;

public class InsertSql extends Search {

    private String insert_sql_seq;
    private String mig_list_seq;

    private String insert_type;
    private String insert_table;
    private String pk_column;

    private String truncate_yn;
    private int ordering;

    public String getInsert_sql_seq() {
        return insert_sql_seq;
    }

    public void setInsert_sql_seq(String insert_sql_seq) {
        this.insert_sql_seq = insert_sql_seq;
    }

    public String getMig_list_seq() {
        return mig_list_seq;
    }

    public void setMig_list_seq(String mig_list_seq) {
        this.mig_list_seq = mig_list_seq;
    }

    public String getInsert_type() {
        return insert_type;
    }

    public void setInsert_type(String insert_type) {
        this.insert_type = insert_type;
    }

    public String getInsert_table() {
        return insert_table;
    }

    public void setInsert_table(String insert_table) {
        this.insert_table = insert_table;
    }

    public String getPk_column() {
        return pk_column;
    }

    public void setPk_column(String pk_column) {
        this.pk_column = pk_column;
    }

    public String getTruncate_yn() {
        return truncate_yn;
    }

    public void setTruncate_yn(String truncate_yn) {
        this.truncate_yn = truncate_yn;
    }

    public int getOrdering() {
        return ordering;
    }

    public void setOrdering(int ordering) {
        this.ordering = ordering;
    }
}
