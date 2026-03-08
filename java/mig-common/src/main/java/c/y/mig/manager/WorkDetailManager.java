package c.y.mig.manager;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;

import org.slf4j.LoggerFactory;

import org.springframework.stereotype.Component;

import c.y.mig.db.DBManager;
import c.y.mig.db.query.Insert;
import c.y.mig.db.query.Select;
import c.y.mig.model.WorkDetail;


@Component
public class WorkDetailManager extends Manager {

    private static final Logger log = LoggerFactory.getLogger(WorkDetailManager.class);

    public int insert(WorkDetail detail) {
        int rtn = 0;
        Connection con = null;
        PreparedStatement stmt = null;
        try {
            Insert sql = new Insert();
            sql.addField("work_seq", detail.getWork_seq());
            sql.addField("thread_idx", detail.getThread_idx());
            sql.addField("paging_idx", detail.getPaging_idx());
            sql.addField("query_params", truncate(detail.getQuery_params(), 4000));
            sql.addField("read_cnt", detail.getRead_cnt());
            sql.addField("read_ms", detail.getRead_ms());
            sql.addField("proc_cnt", detail.getProc_cnt());
            sql.addField("proc_ms", detail.getProc_ms());
            sql.addField("status", detail.getStatus());
            sql.addField("err_msg", truncate(detail.getErr_msg(), 4000));
            
            sql.addFrom(WORK_DETAIL);

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

    public List<WorkDetail> findList(WorkDetail table) {
        List<WorkDetail> list = new ArrayList<>();
        Connection con = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            con = DBManager.getConnection();
            Select sql = new Select();
            sql.addField("*");
            sql.addFrom(WORK_DETAIL);
            sql.addWhere("work_seq = ?", table.getWork_seq());
            sql.addOrder("detail_seq ASC");

            stmt = con.prepareStatement(sql.toQuery());
            setParameter(sql, stmt);
            rs = stmt.executeQuery();

            while (rs.next()) {
                WorkDetail entity = new WorkDetail();
                entity.setDetail_seq(rs.getInt("detail_seq"));
                entity.setWork_seq(rs.getInt("work_seq"));
                entity.setThread_idx(rs.getInt("thread_idx"));
                entity.setPaging_idx(rs.getInt("paging_idx"));
                entity.setQuery_params(rs.getString("query_params"));
                entity.setRead_cnt(rs.getInt("read_cnt"));
                entity.setRead_ms(rs.getInt("read_ms"));
                entity.setProc_cnt(rs.getInt("proc_cnt"));
                entity.setProc_ms(rs.getInt("proc_ms"));
                entity.setStatus(rs.getString("status"));
                entity.setErr_msg(rs.getString("err_msg"));
                entity.setCreate_date(rs.getTimestamp("create_date"));
                list.add(entity);
            }
        } catch (Exception e) {
            log.error(e.toString(), e);
        } finally {
            DBManager.close(rs, stmt, con);
        }
        return list;
    }


    private String truncate(String str, int maxLen) {
        if (str == null) return null;
        if (str.length() <= maxLen) return str;
        return str.substring(0, maxLen - 3) + "...";
    }
}
