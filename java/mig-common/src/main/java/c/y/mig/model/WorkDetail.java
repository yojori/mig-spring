package c.y.mig.model;

import java.util.Date;

public class WorkDetail {
    private int detail_seq;
    private int work_seq;
    private int thread_idx;
    private int paging_idx;
    private String query_params;
    private int read_cnt;
    private int read_ms;
    private int proc_cnt;
    private int proc_ms;
    private String status;
    private String err_msg;
    private Date create_date;

    public int getDetail_seq() { return detail_seq; }
    public void setDetail_seq(int detail_seq) { this.detail_seq = detail_seq; }

    public int getWork_seq() { return work_seq; }
    public void setWork_seq(int work_seq) { this.work_seq = work_seq; }

    public int getThread_idx() { return thread_idx; }
    public void setThread_idx(int thread_idx) { this.thread_idx = thread_idx; }

    public int getPaging_idx() { return paging_idx; }
    public void setPaging_idx(int paging_idx) { this.paging_idx = paging_idx; }

    public String getQuery_params() { return query_params; }
    public void setQuery_params(String query_params) { this.query_params = query_params; }

    public int getRead_cnt() { return read_cnt; }
    public void setRead_cnt(int read_cnt) { this.read_cnt = read_cnt; }

    public int getRead_ms() { return read_ms; }
    public void setRead_ms(int read_ms) { this.read_ms = read_ms; }

    public int getProc_cnt() { return proc_cnt; }
    public void setProc_cnt(int proc_cnt) { this.proc_cnt = proc_cnt; }

    public int getProc_ms() { return proc_ms; }
    public void setProc_ms(int proc_ms) { this.proc_ms = proc_ms; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getErr_msg() { return err_msg; }
    public void setErr_msg(String err_msg) { this.err_msg = err_msg; }

    public Date getCreate_date() { return create_date; }
    public void setCreate_date(Date create_date) { this.create_date = create_date; }
}
