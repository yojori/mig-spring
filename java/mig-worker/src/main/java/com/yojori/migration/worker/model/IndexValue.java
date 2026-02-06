package com.yojori.migration.worker.model;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = false)
public class IndexValue extends Search {
    private String index_value_seq;
    private String mig_list_seq;
    private String index_value;
    private int thread_number;
    private int current_page;
    private int while_loop;
}
