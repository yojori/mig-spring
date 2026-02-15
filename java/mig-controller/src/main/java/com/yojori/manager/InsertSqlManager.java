package com.yojori.manager;

import com.yojori.db.DBManager;
import com.yojori.db.query.*;
import com.yojori.migration.controller.model.InsertSql;
import com.yojori.migration.controller.model.InsertColumn;
import com.yojori.migration.controller.model.MigrationList;
import com.yojori.util.Config;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class InsertSqlManager extends Manager {
    private static final Logger log = LoggerFactory.getLogger(InsertSqlManager.class);

    // Simplified CRUD for InsertSql
    public List<InsertSql> getList(InsertSql table, int PAGE_GUBUN) {
        // Implementation omitted for brevity in this single-shot, but conceptually
        // identical to others.
        // Supporting finding by mig_list_seq for Worker logic.

        List<InsertSql> list = new ArrayList<>();
        Connection con = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            con = DBManager.getConnection();
            Select sql = new Select();
            sql.addField("*");
            sql.addFrom(INSERT_SQL);
            sql.addWhere("mig_list_seq = ?", table.getMig_list_seq());
            sql.addOrder("ordering asc");

            stmt = con.prepareStatement(sql.toQuery());
            setParameter(sql, stmt);
            rs = stmt.executeQuery();

            while (rs.next()) {
                InsertSql entity = new InsertSql();
                entity.setInsert_sql_seq(rs.getString("insert_sql_seq"));
                entity.setMig_list_seq(rs.getString("mig_list_seq"));
                entity.setInsert_type(rs.getString("insert_type"));
                entity.setInsert_table(rs.getString("insert_table"));
                entity.setPk_column(rs.getString("pk_column"));
                entity.setOrdering(rs.getInt("ordering"));
                entity.setCreate_date(rs.getDate("create_date"));
                entity.setUpdate_date(rs.getDate("update_date"));
                entity.setTruncate_yn(rs.getString("truncate_yn"));
                list.add(entity);
            }
        } catch (Exception e) {
            log.error(e.toString(), e);
        } finally {
            DBManager.close(rs, stmt, con);
        }
        return list;
    }

    public int insert(InsertSql table) {
        int rtn = 0;
        Connection con = null;
        PreparedStatement stmt = null;
        try {
            Insert sql = new Insert();
            sql.addField("insert_sql_seq", table.getInsert_sql_seq());
            sql.addField("mig_list_seq", table.getMig_list_seq());
            sql.addField("insert_type", table.getInsert_type());
            sql.addField("insert_table", table.getInsert_table());
            sql.addField("pk_column", table.getPk_column());
			sql.addField("create_date", table.getCreate_date());
			sql.addField("update_date", table.getUpdate_date());
            sql.addField("truncate_yn", table.getTruncate_yn());
           
            // ordering is not in base model for InsertSql but inferred from legacy logic or schema
            // JSP doesn't set it explicitly in loop, but it might be auto-increment or similar if needed.
            // For now, following JSP usage.

            sql.addFrom(INSERT_SQL);

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

    public int update(InsertSql table) {
        int rtn = 0;
        Connection con = null;
        PreparedStatement stmt = null;
        try {
            Update sql = new Update();
            sql.addField("insert_type", table.getInsert_type());
            sql.addField("insert_table", table.getInsert_table());
            sql.addField("pk_column", table.getPk_column());
            sql.addField("update_date", table.getUpdate_date());
            sql.addField("truncate_yn", table.getTruncate_yn());

            sql.addFrom(INSERT_SQL);
            sql.addWhere("insert_sql_seq = ", table.getInsert_sql_seq());

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

    public String autoInsert(InsertSql search) {
        String rtn = "";
        
        Connection con = null;
        Connection targetCon = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        
        try {
            MigrationListManager mListManager = new MigrationListManager();
            MigrationList mList = new MigrationList();
            mList.setMig_list_seq(search.getMig_list_seq());
            mList = mListManager.find(mList);
            
            if (mList == null) {
                return "Migration List Not Found";
            }
            
            con = DBManager.getConnection();
            targetCon = DBManager.getMIGConnection(mList.getTarget_db_alias());
            
            String query = "SELECT * FROM " + search.getInsert_table();
            String sql = getRownum1Sql(query, mList.getTarget_db_type()); 
            
            stmt = targetCon.prepareStatement(sql);
            rs = stmt.executeQuery();
            ResultSetMetaData meta = rs.getMetaData();
            
            InsertColumnManager colManager = new InsertColumnManager();
            colManager.deleteByInsertSqlSeq(search.getInsert_sql_seq());
            
            for(int i = 1; i <= meta.getColumnCount(); i++) {
                InsertColumn col = new InsertColumn();
                
                col.setInsert_column_seq(Config.getOrdNoSequence("IS"));
                col.setInsert_sql_seq(search.getInsert_sql_seq());
                
                col.setColumn_name(meta.getColumnName(i).toUpperCase());
                col.setColumn_type(meta.getColumnTypeName(i));
                col.setInsert_value(""); 
                col.setInsert_data(meta.getColumnName(i).toUpperCase());
                
                col.setCreate_date(new Date());
                col.setUpdate_date(new Date());
                
                colManager.insert(col);
            }
            
        } catch (SQLException e) {
            log.error(e.toString(), e);
            rtn = e.getMessage();
        } catch (Exception e) {
            log.error(e.toString(), e);
            rtn = e.getMessage();
        } finally {
            DBManager.close(rs, stmt, targetCon);
            DBManager.close(null, null, con);
        }
        
        return rtn;
    }

    private String getRownum1Sql(String sql_string, String dbType) {
        String rtn = "";
        if("mysql".equals(dbType)) {
            rtn = sql_string + " Limit 0, 1";
        } else if("maria".equals(dbType)) {
            rtn = sql_string + " Limit 1 OFFSET 0";
        } else if("mssql".equals(dbType)) {
            String temp = sql_string.toUpperCase();
            int idx = temp.lastIndexOf("ORDER BY");
            if(idx > 0) rtn = "SELECT TOP 1 A.* FROM ( " + sql_string.substring(0, idx) + " ) A";
            else rtn = "SELECT TOP 1 A.* FROM ( " + sql_string + " ) A";
        } else if("oracle".equals(dbType)) {
            rtn = "SELECT * FROM ( " + sql_string + " ) WHERE  ROWNUM = 1";
        } else if("postgresql".equals(dbType)) {
             rtn = sql_string + " Limit 1 OFFSET 0";
        }
        return rtn;
    }

    public void goDelete(InsertSql search) {
         Connection con = null;
        PreparedStatement stmt = null;
        try {
            con = DBManager.getConnection();
            stmt = con.prepareStatement("DELETE FROM " + INSERT_SQL + " WHERE insert_sql_seq = ?");
            stmt.setString(1, search.getInsert_sql_seq());
            stmt.executeUpdate();
        } catch (SQLException e) {
            log.error(e.toString(), e);
        } catch (Exception e) {
            log.error(e.toString(), e);
        } finally {
            DBManager.close(null, stmt, con);
        }
    }
}
