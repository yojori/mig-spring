package com.yojori.manager;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.yojori.db.DBManager;
import com.yojori.db.query.Insert;
import com.yojori.db.query.Select;
import com.yojori.db.query.Update;
import com.yojori.model.WorkList;

public class WorkListManager extends Manager {

    private static final Logger log = LoggerFactory.getLogger(WorkListManager.class);

    private void buildListQuery(WorkList table) {
        Select sql = new Select();
        sql.addField("A.*");
        sql.addField("B.mig_name");
        sql.addField("B.mig_master");
        sql.addFrom(WORK_LIST + " A");
        sql.addFrom(MIGRATION_LIST + " B");
        sql.addWhere("A.mig_list_seq = B.mig_list_seq");

        if (table.getMig_list_seq() != null && !table.getMig_list_seq().isEmpty()) {
            sql.addWhere("A.mig_list_seq = ?", table.getMig_list_seq());
        }
        if (table.getStatus() != null && !table.getStatus().isEmpty()) {
            sql.addWhere("A.status = ?", table.getStatus());
        }
        if (table.getOrderBy() != null && !table.getOrderBy().isEmpty()) {
            sql.addOrder(table.getOrderBy());
        } else {
            sql.addOrder("A.work_seq DESC");
        }
        setListQuery(sql);
    }

    private void buildCountQuery(WorkList table) {
        Select sql = new Select();
        sql.addField("COUNT(A.work_seq)");
        sql.addFrom(WORK_LIST + " A");
        sql.addFrom(MIGRATION_LIST + " B");
        sql.addWhere("A.mig_list_seq = B.mig_list_seq");

        if (table.getMig_list_seq() != null && !table.getMig_list_seq().isEmpty()) {
            sql.addWhere("A.mig_list_seq = ?", table.getMig_list_seq());
        }
        if (table.getStatus() != null && !table.getStatus().isEmpty()) {
            sql.addWhere("A.status = ?", table.getStatus());
        }
        setCountQuery(sql);
    }

    public List<WorkList> getList(WorkList table, int PAGE_GUBUN) {
        List<WorkList> list = new ArrayList<>();
        Connection con = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            con = DBManager.getConnection();
            setForm(table);
            setPageGubun(PAGE_GUBUN);
            buildCountQuery(table);
            buildListQuery(table);

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
                while (rs.next()) {
                    WorkList entity = new WorkList();
                    // Calculate list index for display
                    entity.setListIndex(
                        ((table.getTotalCount() - ((table.getCurrentPage() - 1) * table.getPageSize())) - i)
                    );
                    
                    entity.setWork_seq(rs.getString("work_seq"));
                    entity.setMig_list_seq(rs.getString("mig_list_seq"));
                    entity.setWorker_id(rs.getString("worker_id"));
                    entity.setStatus(rs.getString("status"));
                    entity.setStart_date(rs.getTimestamp("start_date"));
                    entity.setEnd_date(rs.getTimestamp("end_date"));
                    entity.setResult_msg(rs.getString("result_msg"));
                    entity.setRead_count(rs.getLong("read_count"));
                    entity.setProc_count(rs.getLong("proc_count"));
                    entity.setCreate_date(rs.getTimestamp("create_date"));
                    entity.setParam_string(rs.getString("param_string"));
                    
                    entity.setMig_name(rs.getString("mig_name"));
                    entity.setMig_master(rs.getString("mig_master"));
                    
                    list.add(entity);
                    i++;
                }
            }
        } catch (Exception e) {
            log.error(e.toString(), e);
        } finally {
            DBManager.close(rs, stmt, con);
        }
        return list;
    }

    public WorkList find(WorkList table) {
        Connection con = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        WorkList entity = null;
        try {
            con = DBManager.getConnection();
            Select sql = new Select();
            sql.addField("A.*");
            sql.addFrom(WORK_LIST + " A");
            sql.addWhere("A.work_seq = ?", table.getWork_seq());

            stmt = con.prepareStatement(sql.toQuery());
            setParameter(sql, stmt);
            rs = stmt.executeQuery();

            if (rs.next()) {
                entity = new WorkList();
                entity.setWork_seq(rs.getString("work_seq"));
                entity.setMig_list_seq(rs.getString("mig_list_seq"));
                entity.setWorker_id(rs.getString("worker_id"));
                entity.setStatus(rs.getString("status"));
                entity.setStart_date(rs.getTimestamp("start_date"));
                entity.setEnd_date(rs.getTimestamp("end_date"));
                entity.setResult_msg(rs.getString("result_msg"));
                entity.setRead_count(rs.getLong("read_count"));
                entity.setProc_count(rs.getLong("proc_count"));
                entity.setCreate_date(rs.getTimestamp("create_date"));
                entity.setParam_string(rs.getString("param_string"));
            }
        } catch (Exception e) {
            log.error(e.toString(), e);
        } finally {
            DBManager.close(rs, stmt, con);
        }
        return entity;
    }

    public int insert(WorkList table) {
        int rtn = 0;
        Connection con = null;
        PreparedStatement stmt = null;
        try {
            Insert sql = new Insert();
            // sql.addField("work_seq", table.getWork_seq()); // handled by DB auto-increment
            sql.addField("mig_list_seq", table.getMig_list_seq());
            sql.addField("status", table.getStatus());
            sql.addField("create_date", table.getCreate_date());
            if (table.getParam_string() != null) sql.addField("param_string", table.getParam_string());

            sql.addFrom(WORK_LIST);

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
    
    // Method for Worker (to update status)
    public int updateStatus(WorkList table) {
        int rtn = 0;
        Connection con = null;
        PreparedStatement stmt = null;
        try {
            Update sql = new Update();
            sql.addField("status", table.getStatus());
            // sql.addField("update_date", new java.util.Date()); // Removed as column missing
                                                               
            if (table.getWorker_id() != null) sql.addField("worker_id", table.getWorker_id());
            if (table.getStart_date() != null) sql.addField("start_date", table.getStart_date());
            if (table.getEnd_date() != null) sql.addField("end_date", table.getEnd_date());
            if (table.getResult_msg() != null) sql.addField("result_msg", table.getResult_msg());
            if (table.getRead_count() >= 0) sql.addField("read_count", table.getRead_count());
            if (table.getProc_count() >= 0) sql.addField("proc_count", table.getProc_count());
            // param_string is usually set at insert, but no harm allowing update if needed (omitting for now)

            sql.addFrom(WORK_LIST);
            sql.addWhere("work_seq = ", table.getWork_seq());

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
