package com.yojori.model;

import java.util.List;
import java.util.Map;
import java.util.HashMap;

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

    public DBConnMaster getSource() {
        return source;
    }

    public void setSource(DBConnMaster source) {
        this.source = source;
    }

    public DBConnMaster getTarget() {
        return target;
    }

    public void setTarget(DBConnMaster target) {
        this.target = target;
    }

    public String getResult_hd_seq() {
        return result_hd_seq;
    }

    public void setResult_hd_seq(String result_hd_seq) {
        this.result_hd_seq = result_hd_seq;
    }

    public MigrationMaster getMaster() {
        return master;
    }

    public void setMaster(MigrationMaster master) {
        this.master = master;
    }

    public List<MigrationList> getMigList() {
        return migList;
    }

    public void setMigList(List<MigrationList> migList) {
        this.migList = migList;
    }

    public List<InsertTable> getInsertTableList() {
        return insertTableList;
    }

    public void setInsertTableList(List<InsertTable> insertTableList) {
        this.insertTableList = insertTableList;
    }

    public List<InsertSql> getInsertSqlList() {
        return insertSqlList;
    }

    public void setInsertSqlList(List<InsertSql> insertSqlList) {
        this.insertSqlList = insertSqlList;
    }

    public List<InsertColumn> getInsertColumnList() {
        return insertColumnList;
    }

    public void setInsertColumnList(List<InsertColumn> insertColumnList) {
        this.insertColumnList = insertColumnList;
    }

    public Map<Integer, IndexValue> getIndexMap() {
        return indexMap;
    }

    public void setIndexMap(Map<Integer, IndexValue> indexMap) {
        this.indexMap = indexMap;
    }
}
