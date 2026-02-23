package c.y.mig.db.query;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Insert extends Query {
    private static final Logger log = LoggerFactory.getLogger(Insert.class);

    public String toQuery() {

        StringBuilder sql = new StringBuilder(1024);
        sql.append("INSERT INTO ")
           .append(getFromClause())
           .append(" (");

        int listSize = getField().size();
        for (int i = 0; i < listSize; i++) {
            if (i == 0) {
                sql.append(getField().get(i));
            } else {
                sql.append(", ").append(getField().get(i));
            }
        }

        sql.append(") VALUES (");

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
                    sql.append("?");
                } else {
                    sql.append(", ?");
                }
            }
        }

        sql.append(")");

        log.info(sql.toString());

        return sql.toString();
    }
}
