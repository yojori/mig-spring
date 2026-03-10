package c.y.mig.model;

public class KfkMapping extends Search {
    private String mig_list_seq;
    private String mapping_name;
    private String transformation_json; // Holds the SpEL or mapping rules
    private String description;

    public String getMig_list_seq() { return mig_list_seq; }
    public void setMig_list_seq(String mig_list_seq) { this.mig_list_seq = mig_list_seq; }

    public String getMapping_name() { return mapping_name; }
    public void setMapping_name(String mapping_name) { this.mapping_name = mapping_name; }

    public String getTransformation_json() { return transformation_json; }
    public void setTransformation_json(String transformation_json) { this.transformation_json = transformation_json; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
}
