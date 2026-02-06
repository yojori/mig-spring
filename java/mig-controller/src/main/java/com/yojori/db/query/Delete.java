package com.yojori.db.query;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class Delete extends Query {

    @Override
    public String toQuery() {
        StringBuilder sql = new StringBuilder()
                .append("DELETE FROM ")
                .append(getFromClause())
                .append(" WHERE ")
                .append(getWhereClause());

        log.info(sql.toString());

        return sql.toString();
    }
}
