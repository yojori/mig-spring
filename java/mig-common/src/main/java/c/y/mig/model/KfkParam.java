package c.y.mig.model;

public class KfkParam extends Search {
    private String template_id;
    private String param_key;
    private String param_value;
    private String description;
    private int ordering;

    public String getTemplate_id() { return template_id; }
    public void setTemplate_id(String template_id) { this.template_id = template_id; }

    public String getParam_key() { return param_key; }
    public void setParam_key(String param_key) { this.param_key = param_key; }

    public String getParam_value() { return param_value; }
    public void setParam_value(String param_value) { this.param_value = param_value; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public int getOrdering() { return ordering; }
    public void setOrdering(int ordering) { this.ordering = ordering; }
}
