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
import com.yojori.db.query.Delete;
import com.yojori.db.query.Insert;
import com.yojori.db.query.Select;
import com.yojori.db.query.Update;
import com.yojori.model.InsertColumn;

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
            
            // If checking by mig_list_seq, we join INSERT_SQL (B).
            if (table.getMig_list_seq() != null && !table.getMig_list_seq().isEmpty()) {
                sql.addField("B.insert_type");
                sql.addField("B.insert_table");
                sql.addFrom(INSERT_COLUMN + " A");
                sql.addFrom(INSERT_SQL + " B");
                sql.addWhere("A.insert_sql_seq = B.insert_sql_seq");
                sql.addWhere("B.mig_list_seq = ?", table.getMig_list_seq());
            } else {
            	// Even without mig_list_seq, if we need insert_type/table, we might need value.
            	// But typically getList is called with mig_list_seq from JSP.
                sql.addFrom(INSERT_COLUMN + " A");
                if (table.getInsert_sql_seq() != null && !table.getInsert_sql_seq().isEmpty()) {
                    sql.addWhere("A.insert_sql_seq = ?", table.getInsert_sql_seq());
                }
            }

            stmt = con.prepareStatement(sql.toQuery());
            setParameter(sql, stmt);
            rs = stmt.executeQuery();

            while (rs.next()) {
                InsertColumn entity = new InsertColumn();
                entity.setColumn_seq(rs.getString("insert_column_seq"));
                entity.setInsert_sql_seq(rs.getString("insert_sql_seq"));
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
            sql.addField("insert_sql_seq", table.getInsert_sql_seq());
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
            sql.addWhere("insert_column_seq = ", table.getInsert_column_seq());

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

    public int deleteByInsertSqlSeq(String insert_sql_seq) {
        int rtn = 0;
        Connection con = null;
        PreparedStatement stmt = null;
        try {
            Delete sql = new Delete();
            sql.addFrom(INSERT_COLUMN);
            sql.addWhere("insert_sql_seq = ?", insert_sql_seq);
            
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
