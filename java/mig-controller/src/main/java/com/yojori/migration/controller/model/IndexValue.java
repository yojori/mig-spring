package com.yojori.migration.controller.model;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = false)
public class IndexValue extends Search {

    private String index_value_seq; // PK
    private String mig_list_seq; // FK mig_list_seq

    private String index_value; // id >= 'sldksdfs' and 2nd >= 'sldksd' and 3rd >= '12232932' 등이 입력

    private int thread_number; // 현재 Thread
    private int current_page; // 호출하고 있는 페이지
    private int while_loop; // 반복 횟수
}
