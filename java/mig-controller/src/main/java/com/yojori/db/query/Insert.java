package com.yojori.db.query;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class Insert extends Query {

    public String toQuery() {

        StringBuilder sql = new StringBuilder(1024);
        sql.append("\nINSERT INTO ")
           .append(getFromClause())
           .append("\n ( ");

        int listSize = getField().size();
        for (int i = 0; i < listSize; i++) {
            if (i == 0) {
                sql.append(getField().get(i));
            } else {
                sql.append(", ").append(getField().get(i));
            }
        }

        sql.append("\n	) values ( ");

        Object o = null;
        for (int i = 0; i < listSize; i++) {
            o = getInsertField().get(i);

            if (o instanceof Dummy) {
                if (i == 0) {
                    sql.append(((Dummy) o).getValue());
                } else {
                    sql.append(", ").append(((Dummy) o).getValue());
                }
            } else {
                if (i == 0) {
                    sql.append(" ? ");
                } else {
                    sql.append(", ? ");
                }
            }
        }

        sql.append("	) \n");

        log.info(sql.toString());

        return sql.toString();
    }
}
