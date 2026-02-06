package com.yojori.migration.worker.model;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = false)
public class MigrationMaster extends Search {
    private String master_code;
    private String master_name;
    private String display_yn;
    private int ordering;
}
