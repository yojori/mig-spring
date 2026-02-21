package c.y.mig.db.query;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Delete extends Query {
    private static final Logger log = LoggerFactory.getLogger(Delete.class);

    @Override
    public String toQuery() {
        String sql = "DELETE FROM " + getFromClause() + " WHERE " + getWhereClause();

        log.info(sql);

        return sql;
    }
}
