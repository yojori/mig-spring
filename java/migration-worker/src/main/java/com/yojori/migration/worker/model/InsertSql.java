package com.yojori.migration.worker.model;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = false)
public class InsertSql extends Search {
    private String insert_sql_seq;
    private String mig_list_seq;
    private String insert_type;
    private String insert_table;
    private String pk_column;
    private String truncate_yn;
}
