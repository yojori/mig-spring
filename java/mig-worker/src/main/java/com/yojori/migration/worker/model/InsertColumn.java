package com.yojori.migration.worker.model;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = false)
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
    
    // Transient field to hold bind columns for SQL_FUNC
    private transient java.util.List<String> sqlFuncBindCols;
}
