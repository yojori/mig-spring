package c.y.mig.model;

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
    
    private String source_db_name;
    private String target_db_name;

    private String display_yn;
    
    private String source_table;
    private String source_pk;
    private String target_table;
    private String truncate_yn;
    private String insert_type; // INSERT/UPDATE
    
    private String param_string; // Used for passing dynamic params (e.g. MIN/MAX PK)

    public String getMig_list_seq() { return mig_list_seq; }
    public void setMig_list_seq(String mig_list_seq) { this.mig_list_seq = mig_list_seq; }

    public String getMig_master() { return mig_master; }
    public void setMig_master(String mig_master) { this.mig_master = mig_master; }

    public String getMig_name() { return mig_name; }
    public void setMig_name(String mig_name) { this.mig_name = mig_name; }

    public String getThread_use_yn() { return thread_use_yn; }
    public void setThread_use_yn(String thread_use_yn) { this.thread_use_yn = thread_use_yn; }

    public int getThread_count() { return thread_count; }
    public void setThread_count(int thread_count) { this.thread_count = thread_count; }

    public int getPage_count_per_thread() { return page_count_per_thread; }
    public void setPage_count_per_thread(int page_count_per_thread) { this.page_count_per_thread = page_count_per_thread; }

    public String getSql_string() { return sql_string; }
    public void setSql_string(String sql_string) { this.sql_string = sql_string; }

    public int getOrdering() { return ordering; }
    public void setOrdering(int ordering) { this.ordering = ordering; }

    public String getExecute_yn() { return execute_yn; }
    public void setExecute_yn(String execute_yn) { this.execute_yn = execute_yn; }

    public String getMig_type() { return mig_type; }
    public void setMig_type(String mig_type) { this.mig_type = mig_type; }

    public String getSource_db_alias() { return source_db_alias; }
    public void setSource_db_alias(String source_db_alias) { this.source_db_alias = source_db_alias; }

    public String getTarget_db_alias() { return target_db_alias; }
    public void setTarget_db_alias(String target_db_alias) { this.target_db_alias = target_db_alias; }

    public String getSource_db_type() { return source_db_type; }
    public void setSource_db_type(String source_db_type) { this.source_db_type = source_db_type; }

    public String getTarget_db_type() { return target_db_type; }
    public void setTarget_db_type(String target_db_type) { this.target_db_type = target_db_type; }

    public String getDisplay_yn() { return display_yn; }
    public void setDisplay_yn(String display_yn) { this.display_yn = display_yn; }

    public String getSource_table() { return source_table; }
    public void setSource_table(String source_table) { this.source_table = source_table; }

    public String getSource_pk() { return source_pk; }
    public void setSource_pk(String source_pk) { this.source_pk = source_pk; }

    public String getTarget_table() { return target_table; }
    public void setTarget_table(String target_table) { this.target_table = target_table; }

    public String getTruncate_yn() { return truncate_yn; }
    public void setTruncate_yn(String truncate_yn) { this.truncate_yn = truncate_yn; }

    public String getInsert_type() { return insert_type; }
    public void setInsert_type(String insert_type) { this.insert_type = insert_type; }

    public String getParam_string() { return param_string; }
    public void setParam_string(String param_string) { this.param_string = param_string; }

    public String getSource_db_name() { return source_db_name; }
    public void setSource_db_name(String source_db_name) { this.source_db_name = source_db_name; }

    public String getTarget_db_name() { return target_db_name; }
    public void setTarget_db_name(String target_db_name) { this.target_db_name = target_db_name; }
}
