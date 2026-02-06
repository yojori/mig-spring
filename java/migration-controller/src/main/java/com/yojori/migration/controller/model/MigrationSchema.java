package com.yojori.migration.controller.model;

import lombok.Data;
import lombok.EqualsAndHashCode;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

@Data
@EqualsAndHashCode(callSuper = false)
public class MigrationSchema extends Search {

    // Enabled connection info for Worker
    private DBConnMaster source;
    private DBConnMaster target;

    // Log 등록을 위한 Key
    private String result_hd_seq;

    private MigrationMaster master;

    private List<MigrationList> migList;

    private List<InsertTable> insertTableList;

    private List<InsertSql> insertSqlList;

    private List<InsertColumn> insertColumnList;

    private Map<Integer, IndexValue> indexMap = new HashMap<Integer, IndexValue>();
}
