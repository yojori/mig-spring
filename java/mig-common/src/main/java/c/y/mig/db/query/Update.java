package c.y.mig.db.query;

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

        if (!getWhere().isEmpty()) {
            sql.append("\n WHERE ")
               .append(getWhereClause());
        }

        log.info(sql.toString());

        return sql.toString();
    }
}
