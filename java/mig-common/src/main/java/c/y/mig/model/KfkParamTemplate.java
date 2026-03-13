package c.y.mig.model;

import java.util.Date;

public class KfkParamTemplate extends Search {
    private String id;
    private String param_name;
    private String input_method;
    private String param_explain;
    private String param_key;
    private String par_class_id;
    private int dp_level;
    private int dp_order;
    private String hidden_yn;
    private String group_cd;
    private String par_param_id;
    private String par_column_key;
    private String column_type;
    private String required_yn;
    private String default_value;
    private Date create_date;
    private Date update_date;

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getParam_name() { return param_name; }
    public void setParam_name(String param_name) { this.param_name = param_name; }

    public String getInput_method() { return input_method; }
    public void setInput_method(String input_method) { this.input_method = input_method; }

    public String getParam_explain() { return param_explain; }
    public void setParam_explain(String param_explain) { this.param_explain = param_explain; }

    public String getParam_key() { return param_key; }
    public void setParam_key(String param_key) { this.param_key = param_key; }

    public String getPar_class_id() { return par_class_id; }
    public void setPar_class_id(String par_class_id) { this.par_class_id = par_class_id; }

    public int getDp_level() { return dp_level; }
    public void setDp_level(int dp_level) { this.dp_level = dp_level; }

    public int getDp_order() { return dp_order; }
    public void setDp_order(int dp_order) { this.dp_order = dp_order; }

    public String getHidden_yn() { return hidden_yn; }
    public void setHidden_yn(String hidden_yn) { this.hidden_yn = hidden_yn; }

    public String getGroup_cd() { return group_cd; }
    public void setGroup_cd(String group_cd) { this.group_cd = group_cd; }

    public String getPar_param_id() { return par_param_id; }
    public void setPar_param_id(String par_param_id) { this.par_param_id = par_param_id; }

    public String getPar_column_key() { return par_column_key; }
    public void setPar_column_key(String par_column_key) { this.par_column_key = par_column_key; }

    public String getColumn_type() { return column_type; }
    public void setColumn_type(String column_type) { this.column_type = column_type; }

    public String getRequired_yn() { return required_yn; }
    public void setRequired_yn(String required_yn) { this.required_yn = required_yn; }

    public String getDefault_value() { return default_value; }
    public void setDefault_value(String default_value) { this.default_value = default_value; }

    public Date getCreate_date() { return create_date; }
    public void setCreate_date(Date create_date) { this.create_date = create_date; }

    public Date getUpdate_date() { return update_date; }
    public void setUpdate_date(Date update_date) { this.update_date = update_date; }
}
