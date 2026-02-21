package c.y.mig.model;

import java.util.Date;

public class WorkList extends Search {
    private String work_seq;
    private String mig_list_seq;
    private String worker_id;
    private String status;
    private Date start_date;
    private Date end_date;
    private String result_msg;
    private long read_count;
    private long proc_count;
    private Date create_date;
    
    // Status Constants
    public static final String STATUS_READY = "READY";
    public static final String STATUS_RUNNING = "RUNNING";
    public static final String STATUS_DONE = "DONE";
    public static final String STATUS_FAIL = "FAIL";
    
    // Join Fields (Optional, for display)
    private String mig_name;
    private String mig_master;
    
    private String param_string; // Execution parameters (e.g. PK range)

    public String getDurationStr() {
        if (start_date == null || end_date == null) {
            return "";
        }
        long diff = end_date.getTime() - start_date.getTime();
        if (diff < 0) diff = 0;
        long seconds = diff / 1000;
        long h = seconds / 3600;
        long m = (seconds % 3600) / 60;
        long s = seconds % 60;
        return String.format("(%02d:%02d:%02d)", h, m, s);
    }

    public String getWork_seq() { return work_seq; }
    public void setWork_seq(String work_seq) { this.work_seq = work_seq; }

    public String getMig_list_seq() { return mig_list_seq; }
    public void setMig_list_seq(String mig_list_seq) { this.mig_list_seq = mig_list_seq; }

    public String getWorker_id() { return worker_id; }
    public void setWorker_id(String worker_id) { this.worker_id = worker_id; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public Date getStart_date() { return start_date; }
    public void setStart_date(Date start_date) { this.start_date = start_date; }

    public Date getEnd_date() { return end_date; }
    public void setEnd_date(Date end_date) { this.end_date = end_date; }

    public String getResult_msg() { return result_msg; }
    public void setResult_msg(String result_msg) { this.result_msg = result_msg; }

    public long getRead_count() { return read_count; }
    public void setRead_count(long read_count) { this.read_count = read_count; }

    public long getProc_count() { return proc_count; }
    public void setProc_count(long proc_count) { this.proc_count = proc_count; }

    public Date getCreate_date() { return create_date; }
    public void setCreate_date(Date create_date) { this.create_date = create_date; }

    public String getMig_name() { return mig_name; }
    public void setMig_name(String mig_name) { this.mig_name = mig_name; }

    public String getMig_master() { return mig_master; }
    public void setMig_master(String mig_master) { this.mig_master = mig_master; }

    private String orderBy;
    public String getOrderBy() { return orderBy; }
    public void setOrderBy(String orderBy) { this.orderBy = orderBy; }

    public String getParam_string() { return param_string; }
    public void setParam_string(String param_string) { this.param_string = param_string; }
}
