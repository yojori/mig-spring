package com.yojori.db.query;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public abstract class Query implements InterfaceQuery {

    private List<String> listField;
    private List<String> listFrom;
    private List<String> listWhere;
    private List<String> listOrder;

    private List<Object> insertField;
    private List<Object> whereField;

    public Query() {
        listField = new ArrayList<String>();
        listFrom = new ArrayList<String>();
        listWhere = new ArrayList<String>();
        listOrder = new ArrayList<String>();

        insertField = new ArrayList<Object>();
        whereField = new ArrayList<Object>();
    }

    public List<String> getOrder() {
        return this.listOrder;
    }

    public List<String> getField() {
        return this.listField;
    }

    public List<String> getFrom() {
        return this.listFrom;
    }

    public List<String> getWhere() {
        return this.listWhere;
    }

    public void setWhere(List<String> where) {
        this.listWhere = where;
    }

    public List<Object> getWhereField() {
        return this.whereField;
    }

    public void setWhereField(List<Object> whereField) {
        this.whereField = whereField;
    }

    public List<Object> getInsertField() {
        return this.insertField;
    }

    public void addField(String field) {
        listField.add(field);
    }

    public void addField(String field, Object value) {
        listField.add(field);
        insertField.add(value);
    }

    public void addField(String field, Object dummy, Object value) {
        listField.add(field);
        insertField.add(dummy);
        insertField.add(value);
    }

    public void addFrom(String tableName) {
        listFrom.add(tableName);
    }

    public void addWhere(String where) {
        listWhere.add(where);
    }

    public void addWhere(String where, Object value) {
        listWhere.add(where);
        whereField.add(value);
    }

    public void addWhere(String where, Object dummy, Object value) {
        listWhere.add(where);
        whereField.add(dummy);
        whereField.add(value);
    }

    public void addWhere(Object value) {
        whereField.add(value);
    }

    public void addOrder(String order) {
        listOrder.add(order);
    }

    public abstract String toQuery();

    public StringBuffer getFromClause() {
        StringBuffer sql = new StringBuffer();
        int i = 0;
        Iterator<String> iter = getFrom().iterator();

        while (iter.hasNext()) {
            String field = (String) iter.next();

            if (i > 0)
                sql.append(", ");

            sql.append(field);
            i++;
        }

        return sql;
    }

    public StringBuffer getWhereClause() {
        StringBuffer sql = new StringBuffer();
        int i = 0;
        Iterator<String> iter = getWhere().iterator();

        while (iter.hasNext()) {
            String field = (String) iter.next();

            if (i == 0) {
                sql.append(field);
            } else if (field.indexOf("Or") >= 0) {
                sql.append(" " + field);
            } else {
                sql.append(" AND " + field);
            }
            i++;
        }

        return sql;
    }
}
