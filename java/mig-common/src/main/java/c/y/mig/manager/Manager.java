package c.y.mig.manager;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;

import c.y.mig.db.query.Delete;
import c.y.mig.db.query.Dummy;
import c.y.mig.db.query.Insert;
import c.y.mig.db.query.Select;
import c.y.mig.db.query.Update;
import c.y.mig.model.Search;

public abstract class Manager implements InterfaceManager {

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
        if (getPageGubun() == PAGE && form != null) {
            pstmt.setInt(i + 1, (form.getCurrentPage() - 1) * form.getPageSize());
            pstmt.setInt(i + 2, form.getPageSize());
        }
    }

    private int setParameterValue(List<Object> param, PreparedStatement pstmt, int i, int index) throws SQLException {
        for (int j = i; j < param.size(); j++) {
            Object parameter = (Object) param.get(j);
            if (parameter instanceof Dummy)
                continue;

            if (parameter == null)
                pstmt.setString(++index, null);
            else if (parameter instanceof String)
                pstmt.setString(++index, (String) parameter);
            else if (parameter instanceof Integer)
                pstmt.setInt(++index, ((Integer) parameter).intValue());
            else if (parameter instanceof java.util.Date)
                pstmt.setTimestamp(++index, new java.sql.Timestamp(((java.util.Date) parameter).getTime()));
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
