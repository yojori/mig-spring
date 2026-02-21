package c.y.mig.db.query;

public interface InterfaceQuery {

    public abstract String toQuery();

    public abstract InterfaceQuery addFrom(String tableName);

    public abstract InterfaceQuery addWhere(String where);

    public abstract InterfaceQuery addField(String field);

    public abstract InterfaceQuery addOrder(String order);

    public abstract InterfaceQuery addInnerJoin(String table, String on);

    public abstract InterfaceQuery addLeftJoin(String table, String on);

    public abstract InterfaceQuery addRightJoin(String table, String on);

    public abstract InterfaceQuery addFullJoin(String table, String on);
}
