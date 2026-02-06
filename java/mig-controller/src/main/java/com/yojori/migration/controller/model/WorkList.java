package com.yojori.migration.controller.model;

import lombok.Data;
import lombok.EqualsAndHashCode;
import java.util.Date;

@Data
@EqualsAndHashCode(callSuper = false)
public class WorkList extends Search {
    private String work_seq;
    private String mig_list_seq;
    private String worker_id;
    private String status;
    private Date start_date;
    private Date end_date;
    private String result_msg;
    private Date create_date;
    
    // Status Constants
    public static final String STATUS_READY = "READY";
    public static final String STATUS_RUNNING = "RUNNING";
    public static final String STATUS_DONE = "DONE";
    public static final String STATUS_FAIL = "FAIL";
    
    // Join Fields (Optional, for display)
    private String mig_name;
    
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
}
