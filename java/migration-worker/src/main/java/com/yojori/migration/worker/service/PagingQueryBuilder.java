package com.yojori.migration.worker.service;

import com.yojori.db.query.Select;
import com.yojori.migration.worker.model.IndexValue;
import com.yojori.migration.worker.model.Search;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.StringReader;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Date;
import java.util.List;
import java.util.Map;

@Slf4j
@Component
public class PagingQueryBuilder {

    public String buildPagingQuery(String sql, String dbType, Search pageConfig) {
        String query = "";

        if ("mysql".equalsIgnoreCase(dbType)) {
            query = sql + " limit ?, ? ";
        } else if ("maria".equalsIgnoreCase(dbType)) {
            query = sql + " limit ? OFFSET ? ";
        } else if ("mssql".equalsIgnoreCase(dbType)) {
            // Basic OFFSET/FETCH for modern MSSQL
            query = "\n" + sql + "\n OFFSET ? ROWS \n FETCH NEXT ? ROWS ONLY ";
        } else if ("oracle".equalsIgnoreCase(dbType)) {
            query = "\n	select 	yy.rnum rnum, yy.* 	FROM (	" +
                    "\n	select rownum rnum, xx.*  FROM (	" +
                    "\n	" + sql + "	" +
                    "\n		) xx where rownum <= ? " +
                    "\n ) yy WHERE rnum > ? ";
        } else {
            // Default fall back
            query = sql;
        }
        return query;
    }

    public void setPagingParams(PreparedStatement pstmt, int startParamIndex, Search form, String dbType)
            throws SQLException {
        int i = startParamIndex;
        if ("mysql".equalsIgnoreCase(dbType)) {
            pstmt.setInt(i + 1, (form.getCurrentPage() - 1) * form.getPageSize());
            pstmt.setInt(i + 2, form.getPageSize());
        } else if ("maria".equalsIgnoreCase(dbType)) {
            pstmt.setInt(i + 1, form.getPageSize());
            pstmt.setInt(i + 2, (form.getCurrentPage() - 1) * form.getPageSize());
        } else if ("oracle".equalsIgnoreCase(dbType)) {
            pstmt.setInt(i + 1, (form.getCurrentPage() * form.getPageSize()));
            pstmt.setInt(i + 2, (form.getCurrentPage() - 1) * form.getPageSize());
        } else if ("mssql".equalsIgnoreCase(dbType)) {
            pstmt.setInt(i + 1, (form.getCurrentPage() - 1) * form.getPageSize());
            pstmt.setInt(i + 2, form.getPageSize());
        }
    }

    public int setParameters(PreparedStatement pstmt, List<Object> params) throws SQLException {
        int index = 0;
        for (Object parameter : params) {
            if (parameter == null) {
                pstmt.setString(++index, null);
            } else if (parameter instanceof String) {
                pstmt.setString(++index, (String) parameter);
            } else if (parameter instanceof Integer) {
                pstmt.setInt(++index, (Integer) parameter);
            } else if (parameter instanceof Long) {
                pstmt.setLong(++index, (Long) parameter);
            } else if (parameter instanceof Double) {
                pstmt.setDouble(++index, (Double) parameter);
            } else if (parameter instanceof Date) {
                pstmt.setTimestamp(++index, new java.sql.Timestamp(((Date) parameter).getTime()));
            } else {
                pstmt.setObject(++index, parameter);
            }
        }
        return index;
    }
}
