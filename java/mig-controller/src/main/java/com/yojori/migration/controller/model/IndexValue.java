package com.yojori.migration.controller.model;

public class IndexValue extends Search {

    private String index_value_seq; // PK
    private String mig_list_seq; // FK mig_list_seq

    private String index_value; // id >= 'sldksdfs' and 2nd >= 'sldksd' and 3rd >= '12232932' 등이 입력

    private int thread_number; // 현재 Thread
    private int current_page; // 호출하고 있는 페이지
    private int while_loop = 0; // 반복 횟수

    public String getIndex_value_seq() {
        return index_value_seq;
    }

    public void setIndex_value_seq(String index_value_seq) {
        this.index_value_seq = index_value_seq;
    }

    public String getMig_list_seq() {
        return mig_list_seq;
    }

    public void setMig_list_seq(String mig_list_seq) {
        this.mig_list_seq = mig_list_seq;
    }

    public String getIndex_value() {
        return index_value;
    }

    public void setIndex_value(String index_value) {
        this.index_value = index_value;
    }

    public int getThread_number() {
        return thread_number;
    }

    public void setThread_number(int thread_number) {
        this.thread_number = thread_number;
    }

    public int getCurrent_page() {
        return current_page;
    }

    public void setCurrent_page(int current_page) {
        this.current_page = current_page;
    }

    public int getWhile_loop() {
        return while_loop;
    }

    public void setWhile_loop(int while_loop) {
        this.while_loop = while_loop;
    }
}
