package com.yojori.db.query;

import lombok.extern.slf4j.Slf4j;
import java.util.Iterator;

@Slf4j
public class Update extends Query {

    public String toQuery() {

        StringBuffer sql = new StringBuffer();

        sql
                .append("\n")
                .append("UPDATE \n")
                .append(getFromClause())
                .append(" SET \n");

        int i = 0;

        for (; i < getField().size(); i++) {
            if (i == 0) {
                if (getInsertField().get(i) instanceof Dummy) {
                    sql
                            .append(getField().get(i) + " \n");
                } else {
                    sql
                            .append(getField().get(i) + " = ? \n");
                }
            } else {
                if (getInsertField().get(i) instanceof Dummy) {
                    sql
                            .append(" , " + getField().get(i) + " \n");
                } else {
                    sql
                            .append(" , " + getField().get(i) + " = ? \n");
                }
            }
        }

        sql
                .append(getWhereClause());

        log.info(sql.toString());

        return sql.toString();
    }

    public StringBuffer getWhereClause() {
        StringBuffer sql = new StringBuffer()
                .append("Where \n");

        int i = 0;

        Iterator<String> iter = getWhere().iterator();

        while (iter.hasNext()) {
            String field = (String) iter.next();

            if (i == 0)
                sql.append(field + " ? \n");
            else
                sql.append(" AND " + field + " ? \n");
            i++;
        }

        return sql;
    }
}
