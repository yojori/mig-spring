package com.yojori.manager;

import com.yojori.db.DBManager;
import com.yojori.db.query.*;
import com.yojori.migration.controller.model.InsertTable;
import lombok.extern.slf4j.Slf4j;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

@Slf4j
public class InsertTableManager extends Manager {

    private void setListQuery(InsertTable table) {
        Select sql = new Select();
        sql.addField("insert_table_seq");
        sql.addField("source_table");
        sql.addField("source_pk");
        sql.addField("target_table");
        sql.addField("mig_list_seq");
        sql.addField("truncate_yn");
        sql.addField("create_date");
        sql.addField("update_date");

        sql.addFrom(INSERT_TABLE);
        sql.addWhere("mig_list_seq = ?", table.getMig_list_seq());
        sql.addOrder("insert_table_seq asc");
        setListQuery(sql);
    }

    private void setCountQuery(InsertTable table) {
        Select sql = new Select();
        sql.addField("COUNT(insert_table_seq)");
        sql.addFrom(INSERT_TABLE);
        setCountQuery(sql);
    }

    public List<InsertTable> getList(InsertTable table, int PAGE_GUBUN) {
        List<InsertTable> list = new ArrayList<InsertTable>();
        Connection con = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;

        try {
            con = DBManager.getConnection();
            setForm(table);
            setPageGubun(PAGE_GUBUN);
            setCountQuery(table);
            setListQuery(table);

            if (getPageGubun() == InterfaceManager.PAGE) {
                stmt = con.prepareStatement(getCountQuery().toQuery());
                setParameter(getCountQuery(), stmt);
                rs = stmt.executeQuery();
                if (rs.next())
                    table.setTotalCount(rs.getInt(1));
                DBManager.close(rs, stmt, null);
            }

            if (table.getPageSize() > 0) {
                stmt = con.prepareStatement(getListQueryString());
                setParameter(getListQuery(), stmt, getPageGubun());
                rs = stmt.executeQuery();
                int i = 0;
                InsertTable entity = null;
                while (rs.next()) {
                    entity = new InsertTable();
                    entity.setListIndex(
                            ((table.getTotalCount() - ((table.getCurrentPage() - 1) * table.getPageSize())) - i));
                    entity.setInsert_table_seq(rs.getString("insert_table_seq"));
                    entity.setSource_table(rs.getString("source_table"));
                    entity.setSource_pk(rs.getString("source_pk"));
                    entity.setTarget_table(rs.getString("target_table"));
                    entity.setMig_list_seq(rs.getString("mig_list_seq"));
                    entity.setTruncate_yn(rs.getString("truncate_yn"));
                    entity.setCreate_date(rs.getDate("create_date"));
                    entity.setUpdate_date(rs.getDate("update_date"));
                    list.add(entity);
                    i++;
                }
            }
        } catch (SQLException e) {
            log.error(e.toString(), e);
        } catch (Exception e) {
            log.error(e.toString(), e);
        } finally {
            DBManager.close(rs, stmt, con);
        }
        return list;
    }

    public InsertTable find(InsertTable master) {
        Connection con = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            con = DBManager.getConnection();
            Select select = new Select();
            select.addField("*");
            select.addFrom(INSERT_TABLE);
            select.addWhere("insert_table_seq = ?", master.getInsert_table_seq());

            stmt = con.prepareStatement(select.toQuery());
            setParameter(select, stmt);
            rs = stmt.executeQuery();

            if (rs.next()) {
                master.setInsert_table_seq(rs.getString("insert_table_seq"));
                master.setSource_table(rs.getString("source_table"));
                master.setSource_pk(rs.getString("source_pk"));
                master.setTarget_table(rs.getString("target_table"));
                master.setMig_list_seq(rs.getString("mig_list_seq"));
                master.setTruncate_yn(rs.getString("truncate_yn"));
                master.setCreate_date(rs.getDate("create_date"));
                master.setUpdate_date(rs.getDate("update_date"));
            } else {
                master = null;
            }
        } catch (SQLException e) {
            log.error(e.toString(), e);
        } catch (Exception e) {
            log.error(e.toString(), e);
        } finally {
            DBManager.close(rs, stmt, con);
        }
        return master;
    }

    public int insert(InsertTable master) {
        int rtn = 0;
        Connection con = null;
        PreparedStatement stmt = null;
        try {
            Insert sql = new Insert();
            sql.addField("insert_table_seq", master.getInsert_table_seq());
            sql.addField("source_table", master.getSource_table());
            sql.addField("source_pk", master.getSource_pk());
            sql.addField("target_table", master.getTarget_table());
            sql.addField("mig_list_seq", master.getMig_list_seq());
            sql.addField("truncate_yn", master.getTruncate_yn());
            sql.addField("create_date", master.getCreate_date());
            sql.addField("update_date", master.getUpdate_date());
            sql.addFrom(INSERT_TABLE);

            con = DBManager.getConnection();
            stmt = con.prepareStatement(sql.toQuery());
            setParameter(sql, stmt);
            rtn = stmt.executeUpdate();
        } catch (SQLException e) {
            log.error(e.toString(), e);
        } catch (Exception e) {
            log.error(e.toString(), e);
        } finally {
            DBManager.close(null, stmt, con);
        }
        return rtn;
    }

    public int update(InsertTable master) {
        int rtn = 0;
        Connection con = null;
        PreparedStatement stmt = null;
        try {
            Update sql = new Update();
            sql.addField("source_table", master.getSource_table());
            sql.addField("source_pk", master.getSource_pk());
            sql.addField("target_table", master.getTarget_table());
            sql.addField("mig_list_seq", master.getMig_list_seq());
            sql.addField("truncate_yn", master.getTruncate_yn());
            sql.addField("update_date", master.getUpdate_date());
            sql.addFrom(INSERT_TABLE);
            sql.addWhere("insert_table_seq = ", master.getInsert_table_seq());

            con = DBManager.getConnection();
            stmt = con.prepareStatement(sql.toQuery());
            setParameter(sql, stmt);
            rtn = stmt.executeUpdate();
        } catch (SQLException e) {
            log.error(e.toString(), e);
        } catch (Exception e) {
            log.error(e.toString(), e);
        } finally {
            DBManager.close(null, stmt, con);
        }
        return rtn;
    }

    public int delete(InsertTable master) {
        int rtn = 0;
        Connection con = null;
        PreparedStatement stmt = null;
        try {
            Delete sql = new Delete();
            sql.addWhere("insert_table_seq = ?", master.getInsert_table_seq());
            sql.addFrom(INSERT_TABLE);

            con = DBManager.getConnection();
            stmt = con.prepareStatement(sql.toQuery());
            setParameter(sql, stmt);
            rtn = stmt.executeUpdate();
        } catch (SQLException e) {
            log.error(e.toString(), e);
        } catch (Exception e) {
            log.error(e.toString(), e);
        } finally {
            DBManager.close(null, stmt, con);
        }
        return rtn;
    }
}
