package c.y.mig.manager;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import c.y.mig.db.DBManager;
import c.y.mig.db.query.Delete;
import c.y.mig.db.query.Insert;
import c.y.mig.db.query.Select;
import c.y.mig.db.query.Update;
import c.y.mig.model.InsertColumn;

public class InsertColumnManager extends Manager {
    private static final Logger log = LoggerFactory.getLogger(InsertColumnManager.class);

    public List<InsertColumn> getList(InsertColumn table, int PAGE_GUBUN) {
        List<InsertColumn> list = new ArrayList<>();
        Connection con = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            con = DBManager.getConnection();
            Select sql = new Select();
            sql.addField("A.*");
            sql.addFrom(INSERT_COLUMN + " A");
            
            if (table.getMig_list_seq() != null && !table.getMig_list_seq().isEmpty()) {
                sql.addWhere("A.mig_list_seq = ?", table.getMig_list_seq());
            }

            stmt = con.prepareStatement(sql.toQuery());
            setParameter(sql, stmt);
            rs = stmt.executeQuery();

            while (rs.next()) {
                InsertColumn entity = new InsertColumn();
                entity.setInsert_column_seq(rs.getString("insert_column_seq"));
                entity.setMig_list_seq(rs.getString("mig_list_seq"));
                entity.setColumn_name(rs.getString("column_name"));
                entity.setColumn_type(rs.getString("column_type"));
                entity.setInsert_value(rs.getString("insert_value"));
                entity.setInsert_data(rs.getString("insert_data"));
                entity.setCreate_date(rs.getDate("create_date"));
                entity.setUpdate_date(rs.getDate("update_date"));
                
                try {
                	entity.setInsert_type(rs.getString("insert_type"));
                	entity.setInsert_table(rs.getString("insert_table"));
                } catch (Exception e) {}
                
                list.add(entity);
            }
        } catch (Exception e) {
            log.error(e.toString(), e);
        } finally {
            DBManager.close(rs, stmt, con);
        }
        return list;
    }

    public int insert(InsertColumn table) {
        int rtn = 0;
        Connection con = null;
        PreparedStatement stmt = null;
        try {
            Insert sql = new Insert();
            sql.addField("insert_column_seq", table.getInsert_column_seq());
            sql.addField("mig_list_seq", table.getMig_list_seq());
            sql.addField("column_name", table.getColumn_name());
            sql.addField("column_type", table.getColumn_type());
            sql.addField("insert_value", table.getInsert_value());
            sql.addField("insert_data", table.getInsert_data());
            sql.addField("create_date", table.getCreate_date());
            sql.addField("update_date", table.getUpdate_date());

            sql.addFrom(INSERT_COLUMN);

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

    public int update(InsertColumn table) {
        int rtn = 0;
        Connection con = null;
        PreparedStatement stmt = null;
        try {
            Update sql = new Update();
            sql.addField("insert_data", table.getInsert_data());
            sql.addField("insert_value", table.getInsert_value());
            sql.addField("update_date", table.getUpdate_date());

            sql.addFrom(INSERT_COLUMN);
            sql.addWhere("insert_column_seq = ?", table.getInsert_column_seq());

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

    public int delete(InsertColumn table) {
        int rtn = 0;
        Connection con = null;
        PreparedStatement stmt = null;
        try {
            Delete sql = new Delete();
            sql.addFrom(INSERT_COLUMN);
            sql.addWhere("insert_column_seq = ?", table.getInsert_column_seq());
            
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

    public int deleteByMigListSeq(String mig_list_seq) {
        int rtn = 0;
        Connection con = null;
        PreparedStatement stmt = null;
        try {
            Delete sql = new Delete();
            sql.addFrom(INSERT_COLUMN);
            sql.addWhere("mig_list_seq = ?", mig_list_seq);
            
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
