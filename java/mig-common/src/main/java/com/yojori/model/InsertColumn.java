package com.yojori.model;

public class InsertColumn extends Search {

    private String mig_list_seq;

    private String insert_column_seq;
    private String insert_sql_seq;
    private String column_name;
    private String column_type;

    private String insert_data;
    private String insert_value;

    private String insert_type;
    private String insert_table;

    // Alias for legacy code compatibility
    public void setColumn_seq(String seq) {
        this.insert_column_seq = seq;
    }

    public String getMig_list_seq() {
        return mig_list_seq;
    }

    public void setMig_list_seq(String mig_list_seq) {
        this.mig_list_seq = mig_list_seq;
    }

    public String getInsert_column_seq() {
        return insert_column_seq;
    }

    public void setInsert_column_seq(String insert_column_seq) {
        this.insert_column_seq = insert_column_seq;
    }

    public String getInsert_sql_seq() {
        return insert_sql_seq;
    }

    public void setInsert_sql_seq(String insert_sql_seq) {
        this.insert_sql_seq = insert_sql_seq;
    }

    public String getColumn_name() {
        return column_name;
    }

    public void setColumn_name(String column_name) {
        this.column_name = column_name;
    }

    public String getColumn_type() {
        return column_type;
    }

    public void setColumn_type(String column_type) {
        this.column_type = column_type;
    }

    public String getInsert_data() {
        return insert_data;
    }

    public void setInsert_data(String insert_data) {
        this.insert_data = insert_data;
    }

    public String getInsert_value() {
        return insert_value;
    }

    public void setInsert_value(String insert_value) {
        this.insert_value = insert_value;
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

    private java.util.List<String> sqlFuncBindCols;

    public java.util.List<String> getSqlFuncBindCols() {
        return sqlFuncBindCols;
    }

    public void setSqlFuncBindCols(java.util.List<String> sqlFuncBindCols) {
        this.sqlFuncBindCols = sqlFuncBindCols;
    }

    public void setInsert_table(String insert_table) {
        this.insert_table = insert_table;
    }
}
