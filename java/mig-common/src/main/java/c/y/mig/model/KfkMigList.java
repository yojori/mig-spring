package c.y.mig.model;

import java.util.Date;

public class KfkMigList extends Search {
    private String mig_list_seq;
    private String mig_master;
    private String mig_name;
    private String registration_type;
    private String source_connector;
    private String sink_connector;
    private String use_yn;
    private String source_db_alias;
    private String target_db_alias;
    private Date create_date;
    private Date update_date;

    public String getMig_list_seq() { return mig_list_seq; }
    public void setMig_list_seq(String mig_list_seq) { this.mig_list_seq = mig_list_seq; }

    public String getMig_master() { return mig_master; }
    public void setMig_master(String mig_master) { this.mig_master = mig_master; }

    public String getMig_name() { return mig_name; }
    public void setMig_name(String mig_name) { this.mig_name = mig_name; }

    public String getRegistration_type() { return registration_type; }
    public void setRegistration_type(String registration_type) { this.registration_type = registration_type; }

    public String getSource_connector() { return source_connector; }
    public void setSource_connector(String source_connector) { this.source_connector = source_connector; }

    public String getSink_connector() { return sink_connector; }
    public void setSink_connector(String sink_connector) { this.sink_connector = sink_connector; }

    public String getUse_yn() { return use_yn; }
    public void setUse_yn(String use_yn) { this.use_yn = use_yn; }

    public String getSource_db_alias() { return source_db_alias; }
    public void setSource_db_alias(String source_db_alias) { this.source_db_alias = source_db_alias; }

    public String getTarget_db_alias() { return target_db_alias; }
    public void setTarget_db_alias(String target_db_alias) { this.target_db_alias = target_db_alias; }

    public Date getCreate_date() { return create_date; }
    public void setCreate_date(Date create_date) { this.create_date = create_date; }

    public Date getUpdate_date() { return update_date; }
    public void setUpdate_date(Date update_date) { this.update_date = update_date; }
}
