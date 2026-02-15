package com.yojori.migration.controller.model;




public class InsertTable extends Search {

    private String insert_table_seq;
    private String source_table;
    private String source_pk;
    private String target_table;
    private String truncate_yn;
    private String mig_list_seq;

    public String getInsert_table_seq() { return insert_table_seq; }
    public void setInsert_table_seq(String insert_table_seq) { this.insert_table_seq = insert_table_seq; }

    public String getSource_table() { return source_table; }
    public void setSource_table(String source_table) { this.source_table = source_table; }

    public String getSource_pk() { return source_pk; }
    public void setSource_pk(String source_pk) { this.source_pk = source_pk; }

    public String getTarget_table() { return target_table; }
    public void setTarget_table(String target_table) { this.target_table = target_table; }

    public String getTruncate_yn() { return truncate_yn; }
    public void setTruncate_yn(String truncate_yn) { this.truncate_yn = truncate_yn; }

    public String getMig_list_seq() { return mig_list_seq; }
    public void setMig_list_seq(String mig_list_seq) { this.mig_list_seq = mig_list_seq; }
}
