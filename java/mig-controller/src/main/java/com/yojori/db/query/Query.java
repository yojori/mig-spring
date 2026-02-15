package com.yojori.db.query;

import java.util.ArrayList;
import java.util.List;

public abstract class Query implements InterfaceQuery {

    private List<String> field = new ArrayList<>();
    private List<String> from = new ArrayList<>();
    private List<String> where = new ArrayList<>();
    private List<String> order = new ArrayList<>();
    private List<String> joins = new ArrayList<>();

    private List<Object> insertField = new ArrayList<>();
    private List<Object> whereField = new ArrayList<>();

    public Query() {
    }

    public Query addField(String field) {
        this.field.add(field);
        return this;
    }

    public Query addField(String field, Object value) {
        this.field.add(field);
        this.insertField.add(value);
        return this;
    }

    public Query addField(String field, Object dummy, Object value) {
        this.field.add(field);
        this.insertField.add(dummy);
        this.insertField.add(value);
        return this;
    }

    public Query addFrom(String tableName) {
        this.from.add(tableName);
        return this;
    }

    public Query addWhere(String where) {
        this.where.add(where);
        return this;
    }

    public Query addWhere(String where, Object value) {
        this.where.add(where);
        this.whereField.add(value);
        return this;
    }

    public Query addWhere(String where, Object dummy, Object value) {
        this.where.add(where);
        this.whereField.add(dummy);
        this.whereField.add(value);
        return this;
    }

    public Query addWhere(Object value) {
        this.whereField.add(value);
        return this;
    }

    public Query addOrder(String order) {
        this.order.add(order);
        return this;
    }

    public Query addInnerJoin(String table, String on) {
        addJoin("INNER JOIN", table, on);
        return this;
    }

    public Query addLeftJoin(String table, String on) {
        addJoin("LEFT OUTER JOIN", table, on);
        return this;
    }

    public Query addRightJoin(String table, String on) {
        addJoin("RIGHT OUTER JOIN", table, on);
        return this;
    }

    public Query addFullJoin(String table, String on) {
        addJoin("FULL OUTER JOIN", table, on);
        return this;
    }

    private void addJoin(String joinType, String table, String on) {
        StringBuilder join = new StringBuilder();
        join.append(joinType).append(" ").append(table);
        if (on != null && !on.trim().isEmpty()) {
            join.append(" ON ").append(on);
        }
        this.joins.add(join.toString());
    }

    public abstract String toQuery();

    public StringBuilder getFromClause() {
        StringBuilder sql = new StringBuilder();
        int i = 0;
        for (String field : from) {
            if (i > 0)
                sql.append(", ");
            sql.append(field);
            i++;
        }
        
        // ANSI Joins
        for (String join : joins) {
            sql.append(" ").append(join);
        }
        
        return sql;
    }

    public StringBuilder getWhereClause() {
        StringBuilder sql = new StringBuilder();
        int i = 0;

        for (String field : where) {
            if (i == 0) {
                sql.append(field);
            } else if (field.indexOf("Or") >= 0) {
                sql.append(" ").append(field);
            } else {
                sql.append(" AND ").append(field);
            }
            i++;
        }
        return sql;
    }

    public List<String> getField() { return field; }
    public void setField(List<String> field) { this.field = field; }

    public List<String> getFrom() { return from; }
    public void setFrom(List<String> from) { this.from = from; }

    public List<String> getWhere() { return where; }
    public void setWhere(List<String> where) { this.where = where; }

    public List<String> getOrder() { return order; }
    public void setOrder(List<String> order) { this.order = order; }

    public List<String> getJoins() { return joins; }
    public void setJoins(List<String> joins) { this.joins = joins; }

    public List<Object> getInsertField() { return insertField; }
    public void setInsertField(List<Object> insertField) { this.insertField = insertField; }

    public List<Object> getWhereField() { return whereField; }
    public void setWhereField(List<Object> whereField) { this.whereField = whereField; }
}
