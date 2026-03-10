package c.y.mig.model;

public class KfkOffset extends Search {
    private String mig_list_seq;
    private String topic_name;
    private int partition_id;
    private long current_offset;
    private String last_timestamp;
    private String last_pk;
    private String consumer_group;

    public String getMig_list_seq() { return mig_list_seq; }
    public void setMig_list_seq(String mig_list_seq) { this.mig_list_seq = mig_list_seq; }

    public String getTopic_name() { return topic_name; }
    public void setTopic_name(String topic_name) { this.topic_name = topic_name; }

    public int getPartition_id() { return partition_id; }
    public void setPartition_id(int partition_id) { this.partition_id = partition_id; }

    public long getCurrent_offset() { return current_offset; }
    public void setCurrent_offset(long current_offset) { this.current_offset = current_offset; }

    public String getLast_timestamp() { return last_timestamp; }
    public void setLast_timestamp(String last_timestamp) { this.last_timestamp = last_timestamp; }

    public String getLast_pk() { return last_pk; }
    public void setLast_pk(String last_pk) { this.last_pk = last_pk; }

    public String getConsumer_group() { return consumer_group; }
    public void setConsumer_group(String consumer_group) { this.consumer_group = consumer_group; }
}
