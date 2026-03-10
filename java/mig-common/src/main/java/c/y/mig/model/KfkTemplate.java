package c.y.mig.model;

public class KfkTemplate extends Search {
    private String template_id;
    private String template_name;
    private String template_type; // SOURCE/TARGET
    private String connector_class;
    private String description;
    private String use_yn;
    private int ordering;

    public String getTemplate_id() { return template_id; }
    public void setTemplate_id(String template_id) { this.template_id = template_id; }

    public String getTemplate_name() { return template_name; }
    public void setTemplate_name(String template_name) { this.template_name = template_name; }

    public String getTemplate_type() { return template_type; }
    public void setTemplate_type(String template_type) { this.template_type = template_type; }

    public String getConnector_class() { return connector_class; }
    public void setConnector_class(String connector_class) { this.connector_class = connector_class; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getUse_yn() { return use_yn; }
    public void setUse_yn(String use_yn) { this.use_yn = use_yn; }

    public int getOrdering() { return ordering; }
    public void setOrdering(int ordering) { this.ordering = ordering; }
}
