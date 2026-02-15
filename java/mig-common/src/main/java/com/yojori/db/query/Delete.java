package com.yojori.db.query;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Delete extends Query {
    private static final Logger log = LoggerFactory.getLogger(Delete.class);

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
