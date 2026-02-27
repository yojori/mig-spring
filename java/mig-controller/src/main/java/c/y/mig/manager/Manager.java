package c.y.mig.manager;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Date;
import java.util.List;

import c.y.mig.db.query.Delete;
import c.y.mig.db.query.Dummy;
import c.y.mig.db.query.Insert;
import c.y.mig.db.query.Select;
import c.y.mig.db.query.Update;
import java.util.HashMap;
import java.util.Map;
import c.y.mig.model.Search;

public abstract class Manager implements InterfaceManager {
    
    protected String getRownum1Sql(String sql_string, String dbType) {
        String rtn = sql_string;
        if (dbType == null) dbType = "mysql";
        
        if ("mysql".equals(dbType)) {
            rtn = sql_string + " Limit 0, 1";
        } else if ("maria".equals(dbType)) {
            rtn = sql_string + " Limit 1 OFFSET 0";
        } else if ("mssql".equals(dbType)) {
            String temp = sql_string.toUpperCase();
            int idx = temp.lastIndexOf("ORDER BY");
            if (idx > 0) {
                rtn = "SELECT TOP 1 A.* FROM ( " + sql_string.substring(0, idx) + " ) A";
            } else {
                rtn = "SELECT TOP 1 A.* FROM ( " + sql_string + " ) A";
            }
        } else if ("oracle".equals(dbType)) {
            rtn = "SELECT * FROM ( " + sql_string + " ) WHERE  ROWNUM = 1";
        } else if ("postgresql".equals(dbType)) {
            rtn = sql_string + " Limit 1 OFFSET 0";
        }
        return rtn;
    }

    protected Map<String, String> getTypeMap(String dbType) {
        Map<String, String> typeMap = new HashMap<>();
        typeMap.put("12_oracle", "VARCHAR2"); typeMap.put("12_mysql", "VARCHAR"); typeMap.put("12_maria", "VARCHAR"); typeMap.put("12_mssql", "VARCHAR");
        typeMap.put("4_oracle", "NUMBER"); typeMap.put("4_mysql", "INT"); typeMap.put("4_maria", "INT"); typeMap.put("4_mssql", "INT");
        typeMap.put("-5_oracle", "NUMBER"); typeMap.put("-5_mysql", "BIGINT"); typeMap.put("-5_maria", "BIGINT"); typeMap.put("-5_mssql", "BIGINT");
        typeMap.put("93_oracle", "TIMESTAMP"); typeMap.put("93_mysql", "TIMESTAMP"); typeMap.put("93_maria", "TIMESTAMP"); typeMap.put("93_mssql", "DATETIME");
        typeMap.put("91_oracle", "DATE"); typeMap.put("91_mysql", "DATE"); typeMap.put("91_maria", "DATE"); typeMap.put("91_mssql", "DATE");
        typeMap.put("12_postgresql", "VARCHAR");
        typeMap.put("4_postgresql", "INTEGER");
        typeMap.put("-5_postgresql", "BIGINT");
        typeMap.put("93_postgresql", "TIMESTAMP");
        typeMap.put("91_postgresql", "DATE");
        return typeMap;
    }

    public final String DB_MASTER = "ZXXMIG_DB_MASTER";
    public final String INSERT_COLUMN = "ZXXMIG_INSERT_COLUMN";
    public final String INSERT_SQL = "ZXXMIG_INSERT_SQL";
    public final String INSERT_TABLE = "ZXXMIG_INSERT_TABLE";
    public final String MIGRATION_LIST = "ZXXMIG_MIGRATION_LIST";
    public final String MIGRATION_MASTER = "ZXXMIG_MIGRATION_MASTER";
    public final String SELECT_COLUMN = "ZXXMIG_SELECT_COLUMN";
    public final String RESULT_HD = "ZXXMIG_RESULT_HD";
    public final String RESULT_DT = "ZXXMIG_RESULT_DT";
    public final String WORK_LIST = "ZXXMIG_WORK_LIST";

    private Search form;
    private int pageGubun;
    private Select countQuery;
    private Select listQuery;

    public int getPageGubun() {
        return pageGubun;
    }

    public void setPageGubun(int pageGubun) {
        this.pageGubun = pageGubun;
    }

    public Select getCountQuery() {
        return countQuery;
    }

    public void setCountQuery(Select countQuery) {
        this.countQuery = countQuery;
    }

    public void setListQuery(Select listQuery) {
        this.listQuery = listQuery;
        if (this.countQuery != null) {
            this.countQuery.setWhereField(listQuery.getWhereField());
            this.countQuery.setWhere(listQuery.getWhere());
        }
    }

    public Select getListQuery() {
        return this.listQuery;
    }

    public Search getForm() {
        return form;
    }

    public void setForm(Search form) {
        this.form = form;
    }

    // Parameter setting helpers
    public void setParameter(Delete delete, PreparedStatement pstmt) throws SQLException {
        setParameter(delete.getWhereField(), pstmt);
    }

    public void setParameter(Select select, PreparedStatement pstmt) throws SQLException {
        setParameter(select.getWhereField(), pstmt);
    }

    public void setParameter(Select select, PreparedStatement pstmt, int gubun) throws SQLException {
        setParameter(select.getWhereField(), pstmt, gubun, "mysql"); // Default to mysql for internal DB
    }

    public void setParameter(Insert insert, PreparedStatement pstmt) throws SQLException {
        setParameterValue(insert.getInsertField(), pstmt, 0, 0);
    }

    public void setParameter(Update update, PreparedStatement pstmt) throws SQLException {
        int i = setParameterValue(update.getInsertField(), pstmt, 0, 0);
        setParameterValue(update.getWhereField(), pstmt, 0, i);
    }

    public void setParameter(List<Object> param, PreparedStatement pstmt) throws SQLException {
        setParameterValue(param, pstmt, 0, 0);
    }

    // Simplified parameter setting
    public void setParameter(List<Object> param, PreparedStatement pstmt, int gubun, String dbType)
            throws SQLException {
        int i = setParameterValue(param, pstmt, 0, 0); // Always start from 0 for internal

        // Internal DB Paging (assuming MariaDB/MySQL for Controller DB)
        if (getPageGubun() == PAGE) {
            pstmt.setInt(i + 1, (form.getCurrentPage() - 1) * form.getPageSize());
            pstmt.setInt(i + 2, form.getPageSize());
        }
    }

    private int setParameterValue(List<Object> param, PreparedStatement pstmt, int i, int index) throws SQLException {
        for (int j = i; j < param.size(); j++) {
            Object parameter = (Object) param.get(j);
            if (parameter instanceof Dummy)
                continue;

            // log.info("index : " + (index + 1) + ", value : " + (parameter == null ?
            // "null" : parameter.toString()));

            if (parameter == null)
                pstmt.setString(++index, null);
            else if (parameter instanceof String)
                pstmt.setString(++index, (String) parameter);
            else if (parameter instanceof Integer)
                pstmt.setInt(++index, ((Integer) parameter).intValue());
            else if (parameter instanceof Date)
                pstmt.setTimestamp(++index, new java.sql.Timestamp(((Date) parameter).getTime()));
            else
                pstmt.setObject(++index, parameter);

            i++;
        }
        return i;
    }

    public String getListQueryString() {
        // Assume MySQL/MariaDB for the internal Controller DB
        return getListQueryString("mysql");
    }

    public String getListQueryString(String dbType) {
        String query = listQuery.toQuery();
        if (pageGubun == InterfaceManager.PAGE || pageGubun == InterfaceManager.TOP) {
            query += " limit ?, ? ";
        }
        return query;
    }
}
