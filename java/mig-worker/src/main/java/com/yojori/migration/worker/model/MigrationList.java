package com.yojori.migration.worker.model;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = false)
public class MigrationList extends Search {
    private String mig_list_seq;
    private String mig_master;
    private String mig_name;
    private String thread_use_yn;
    private int thread_count;
    private int page_count_per_thread;
    private String sql_string;
    private int ordering;
    private String execute_yn;
    private String mig_type;
    private String source_db_alias;
    private String target_db_alias;
    private String source_db_type;
    private String target_db_type;
    private String display_yn;
    private String param_string;
}
