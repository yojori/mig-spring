package c.y.mig.model;


public class TypeMapping extends Search {
    private String mapping_seq;
    private String src_db_type;
    private String src_type_name;
    private String tgt_db_type;
    private String tgt_type_name;
    private int priority;
    private String use_yn;

    public String getMapping_seq() {
        return mapping_seq;
    }

    public void setMapping_seq(String mapping_seq) {
        this.mapping_seq = mapping_seq;
    }

    public String getSrc_db_type() {
        return src_db_type;
    }

    public void setSrc_db_type(String src_db_type) {
        this.src_db_type = src_db_type;
    }

    public String getSrc_type_name() {
        return src_type_name;
    }

    public void setSrc_type_name(String src_type_name) {
        this.src_type_name = src_type_name;
    }

    public String getTgt_db_type() {
        return tgt_db_type;
    }

    public void setTgt_db_type(String tgt_db_type) {
        this.tgt_db_type = tgt_db_type;
    }

    public String getTgt_type_name() {
        return tgt_type_name;
    }

    public void setTgt_type_name(String tgt_type_name) {
        this.tgt_type_name = tgt_type_name;
    }

    public int getPriority() {
        return priority;
    }

    public void setPriority(int priority) {
        this.priority = priority;
    }

    public String getUse_yn() {
        return use_yn;
    }

    public void setUse_yn(String use_yn) {
        this.use_yn = use_yn;
    }
}
