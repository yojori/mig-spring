package com.yojori.migration.worker.model;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = false)
public class InsertTable extends Search {
    private String insert_table_seq;
    private String source_table;
    private String source_pk;
    private String target_table;
    private String truncate_yn;
    private String mig_list_seq;
}
