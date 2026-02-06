package com.yojori.db.query;

public interface InterfaceQuery {

    public abstract String toQuery();

    public abstract void addFrom(String tableName);

    public abstract void addWhere(String where);

    public abstract void addField(String field);

    public abstract void addOrder(String order);
}
