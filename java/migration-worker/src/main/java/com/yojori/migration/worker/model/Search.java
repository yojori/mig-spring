package com.yojori.migration.worker.model;

import lombok.Data;
import java.util.Date;

@Data
public class Search {

    private int listIndex;
    private String mode;
    private String searchKey;
    private String searchWord;
    private String searchCheck;
    private int currentPage = 1;
    private int totalCount;
    private int pageSize = 10;
    private String pageGubun;
    private Date create_date;
    private Date update_date;
    private String regUser;
    private Date regDate;
    private String updUser;
    private Date updDate;
    private Date modDate;
    private String returnUrl;

    public int getTotalPage() {
        return (int) ((totalCount - 1) / pageSize) + 1;
    }
}
