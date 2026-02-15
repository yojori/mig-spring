package com.yojori.db.query;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class Update extends Query {
    private static final Logger log = LoggerFactory.getLogger(Update.class);

    public String toQuery() {

        StringBuilder sql = new StringBuilder();

        sql.append("\n")
           .append("UPDATE \n")
           .append(getFromClause())
           .append(" SET \n");

        int i = 0;

        for (; i < getField().size(); i++) {
            if (i == 0) {
                if (getInsertField().get(i) instanceof Dummy) {
                    sql.append(getField().get(i)).append(" \n");
                } else {
                    sql.append(getField().get(i)).append(" = ? \n");
                }
            } else {
                if (getInsertField().get(i) instanceof Dummy) {
                    sql.append(" , ").append(getField().get(i)).append(" \n");
                } else {
                    sql.append(" , ").append(getField().get(i)).append(" = ? \n");
                }
            }
        }

        sql.append(getWhereClause());

        log.info(sql.toString());

        return sql.toString();
    }

    public StringBuilder getWhereClause() {
        StringBuilder sql = new StringBuilder().append("Where \n");

        int i = 0;
        for (String field : getWhere()) {
            if (i == 0)
                sql.append(field).append(" ? \n");
            else
                sql.append(" AND ").append(field).append(" ? \n");
            i++;
        }

        return sql;
    }
}
