package com.yojori.db.query;




public class Select extends Query {
    // private static final Logger log = LoggerFactory.getLogger(Select.class);

    public StringBuilder getSelectClause() {
        StringBuilder sql = new StringBuilder();
        int i = 0;
        for (String field : getField()) {
            if (i > 0)
                sql.append(", ");
            sql.append(field);
            i++;
        }
        return sql;
    }

    public StringBuilder getOrderClause() {
        StringBuilder sql = new StringBuilder();
        int i = 0;
        for (String field : getOrder()) {
            if (i > 0)
                sql.append(", ");
            sql.append(field);
            i++;
        }
        return sql;
    }

    public String toQuery() {
        StringBuilder sql = new StringBuilder();

        sql.append("\nSELECT ")
           .append(getSelectClause())
           .append("\n FROM ")
           .append(getFromClause());

        if (!getWhere().isEmpty()) {
            sql.append("\n WHERE ")
               .append(getWhereClause());
        }

        if (!getOrder().isEmpty()) {
            sql.append("\n Order By ")
               .append(getOrderClause());
        }

        // log.info(sql.toString());

        return sql.toString();
    }
}
