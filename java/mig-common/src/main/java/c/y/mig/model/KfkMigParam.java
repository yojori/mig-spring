package c.y.mig.model;

public class KfkMigParam {
    private int param_seq;
    private String mig_list_seq;
    private String param_key;
    private String param_value;
    private String connector_type;
    private int dp_level;
    private int dp_order;
    private java.util.Date create_date;
    private java.util.Date update_date;

    public int getParam_seq() { return param_seq; }
    public void setParam_seq(int param_seq) { this.param_seq = param_seq; }

    public String getMig_list_seq() { return mig_list_seq; }
    public void setMig_list_seq(String mig_list_seq) { this.mig_list_seq = mig_list_seq; }

    public String getConnector_type() { return connector_type; }
    public void setConnector_type(String connector_type) { this.connector_type = connector_type; }

    public String getParam_key() { return param_key; }
    public void setParam_key(String param_key) { this.param_key = param_key; }

    public String getParam_value() { return param_value; }
    public void setParam_value(String param_value) { this.param_value = param_value; }

    public int getDp_level() { return dp_level; }
    public void setDp_level(int dp_level) { this.dp_level = dp_level; }

    public int getDp_order() { return dp_order; }
    public void setDp_order(int dp_order) { this.dp_order = dp_order; }

    public java.util.Date getCreate_date() { return create_date; }
    public void setCreate_date(java.util.Date create_date) { this.create_date = create_date; }

    public java.util.Date getUpdate_date() { return update_date; }
    public void setUpdate_date(java.util.Date update_date) { this.update_date = update_date; }
}
