package com.yojori.db.query;

import java.util.Iterator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Select extends Query {
    private static final Logger log = LoggerFactory.getLogger(Select.class);

    public StringBuffer getSelectClause() {
        StringBuffer sql = new StringBuffer();
        int i = 0;
        Iterator<String> iter = getField().iterator();

        while (iter.hasNext()) {
            String field = (String) iter.next();

            if (i > 0)
                sql.append(", ");

            sql.append(field);
            i++;
        }

        return sql;
    }

    public StringBuffer getOrderClause() {
        StringBuffer sql = new StringBuffer();
        int i = 0;
        Iterator<String> iter = getOrder().iterator();

        while (iter.hasNext()) {
            String field = (String) iter.next();

            if (i > 0)
                sql.append(", ");

            sql.append(field);
            i++;
        }

        return sql;

    }

    public String toQuery() {
        StringBuffer sql = new StringBuffer();

        sql
                .append("\nSELECT ")
                .append(getSelectClause())
                .append("\n FROM ")
                .append(getFromClause());

        if (getWhere().size() > 0) {
            sql
                    .append("\n WHERE ")
                    .append(getWhereClause());
        }

        if (getOrder().size() > 0) {
            sql
                    .append("\n Order By ")
                    .append(getOrderClause());
        }

        log.info(sql.toString());

        return sql.toString();
    }
}
