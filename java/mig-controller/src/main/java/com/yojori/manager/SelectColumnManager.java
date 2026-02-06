package com.yojori.manager;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.yojori.db.DBManager;
import com.yojori.db.query.Insert;
import com.yojori.db.query.Select;
import com.yojori.db.query.Update;
import com.yojori.migration.controller.model.MigrationList;
import com.yojori.migration.controller.model.SelectColumn;
import com.yojori.util.Config;

// Restored from legacy
public class SelectColumnManager extends Manager {

	private static final Log log = LogFactory.getLog(SelectColumnManager.class);
	
	private void setCountQuery(SelectColumn select) {
		
		Select sql = new Select();

		sql.addField("COUNT(column_seq)");
		sql.addFrom(SELECT_COLUMN);		
		
		setCountQuery(sql);
	}
	
	private void setListQuery(SelectColumn select) {
		
		Select sql = new Select();
		
		sql.addField("column_seq");
		sql.addField("mig_list_seq");
		sql.addField("column_name");
		sql.addField("column_type");
		
		sql.addField("create_date");
		sql.addField("update_date");
		
		sql.addField("ordering");
		
		
		sql.addFrom(SELECT_COLUMN);
		
		sql.addWhere("mig_list_seq = ? ", select.getMig_list_seq());
		sql.addOrder("ordering asc");
		
		setListQuery(sql);
	}
	
	public List<SelectColumn> getList(SelectColumn select, int PAGE_GUBUN) {
		
		List<SelectColumn> list = new ArrayList<SelectColumn>();
		
		Connection con = null;
		PreparedStatement stmt = null;
		ResultSet rs = null;

		
		try {
			
			con = DBManager.getConnection();
			
			setForm(select);
			setPageGubun(PAGE_GUBUN);
			setCountQuery(select);
			setListQuery(select);
			
			if(getPageGubun() == InterfaceManager.PAGE)
			{
				stmt = con.prepareStatement(getCountQuery().toQuery());
				
				setParameter(getCountQuery(), stmt);
				
				rs = stmt.executeQuery();
				
				if(rs.next())
				{
					select.setTotalCount(rs.getInt(1));
				}
				
				if(rs != null)
				{
					rs.close();
					rs = null;
				}
				
				if(stmt != null)
				{
					stmt.clearParameters();
					stmt.close();
					stmt = null;
				}
			}
			
			if(select.getPageSize() > 0)
			{
				stmt = con.prepareStatement(getListQueryString());
				
				setParameter(getListQuery(), stmt, getPageGubun());		
	
				rs = stmt.executeQuery();
				
				int i = 0;
							
				SelectColumn entity = null;
				
				while(rs.next())
				{
					entity = new SelectColumn();
					
					entity.setListIndex(
							((select.getTotalCount() 
									- ((select.getCurrentPage() - 1) 
									* select.getPageSize())) - i));
					
					entity.setColumn_seq(rs.getString("column_seq"));
					entity.setMig_list_seq(rs.getString("mig_list_seq"));

					entity.setColumn_name(rs.getString("column_name"));
					entity.setColumn_type(rs.getString("column_type"));
					
					entity.setCreate_date(rs.getDate("create_date"));
					entity.setUpdate_date(rs.getDate("update_date"));
					
					entity.setOrdering(rs.getInt("ordering"));
					
					list.add(entity);
					
					i++;
				}			
			}
			
		} catch(SQLException e) {
			log.error(e.toString(), e);
		} catch(Exception e) {
			log.error(e.toString(), e);
		} finally {
			DBManager.close(rs, stmt, con);
		}
		
		return list;
	}
	
	public SelectColumn find(SelectColumn selectColumn)
	{		
		Connection con = null;
		PreparedStatement stmt = null;
		ResultSet rs = null;
			
		try {
			
			con = DBManager.getConnection();
			
			Select select = new Select();
			
			select.addField("*");
			
			select.addFrom(SELECT_COLUMN);
			
			select.addWhere("mig_list_seq = ?", selectColumn.getMig_list_seq());
			
			stmt = con.prepareStatement(select.toQuery());
			
			setParameter(select, stmt);

			rs = stmt.executeQuery();
			
			if(rs.next())
			{	
				selectColumn.setColumn_seq(rs.getString("column_seq"));
				selectColumn.setMig_list_seq(rs.getString("mig_list_seq"));

				selectColumn.setColumn_name(rs.getString("column_name"));
				selectColumn.setColumn_type(rs.getString("column_type"));
				
				selectColumn.setCreate_date(rs.getDate("create_date"));
				selectColumn.setUpdate_date(rs.getDate("update_date"));
				
				
				selectColumn.setCreate_date(rs.getDate("create_date"));
				selectColumn.setUpdate_date(rs.getDate("update_date"));
				
				selectColumn.setOrdering(rs.getInt("ordering"));
			
			}
			else
			{
				selectColumn = null;
			}					
			
		} catch(SQLException e) {
			log.error(e.toString(), e);
		} catch(Exception e) {
			log.error(e.toString(), e);
		} finally {
			DBManager.close(rs, stmt, con);
		}
		
		return selectColumn;
	}	
	
	public int insert(SelectColumn selectColumn)
	{
		int rtn = 0;
		
		Connection con = null;
		PreparedStatement stmt = null;
		ResultSet rs = null;
			
		try {
			
			Insert sql = new Insert();
			
			sql.addField("column_seq", selectColumn.getColumn_seq());
			sql.addField("mig_list_seq", selectColumn.getMig_list_seq());
			
			sql.addField("column_name", selectColumn.getColumn_name());
			sql.addField("column_type", selectColumn.getColumn_type());
			
			sql.addField("create_date", selectColumn.getCreate_date());
			sql.addField("update_date", selectColumn.getUpdate_date());
			
			sql.addField("ordering", selectColumn.getOrdering());
			
			sql.addFrom(SELECT_COLUMN);
			
			con = DBManager.getConnection();
			
			stmt = con.prepareStatement(sql.toQuery());
			
			setParameter(sql, stmt);

			rtn = stmt.executeUpdate();
			
		} catch(SQLException e) {
			log.error(e.toString(), e);
		} catch(Exception e) {
			log.error(e.toString(), e);
		} finally {
			DBManager.close(rs, stmt, con);
		}
		
		return rtn;
	}
	
	public int update(SelectColumn selectColumn)
	{
		int rtn = 0;
		
		Connection con = null;
		PreparedStatement stmt = null;
		ResultSet rs = null;
			
		try {
			
			Update sql = new Update();
			
			sql.addField("mig_list_seq", selectColumn.getMig_list_seq());
			
			sql.addField("column_name", selectColumn.getColumn_name());
			sql.addField("column_type", selectColumn.getColumn_type());
			
			//sql.addField("create_date", selectColumn.getCreate_date());
			sql.addField("update_date", selectColumn.getUpdate_date());
			sql.addField("ordering", selectColumn.getOrdering());
			
			sql.addFrom(SELECT_COLUMN);
			
			sql.addWhere("column_seq = ", selectColumn.getColumn_seq());
			
			con = DBManager.getConnection();
			
			stmt = con.prepareStatement(sql.toQuery());
			
			setParameter(sql, stmt);
			
			rtn = stmt.executeUpdate();
	
		} catch(SQLException e) {
			log.error(e.toString(), e);
		} catch(Exception e) {
			log.error(e.toString(), e);
		} finally {
			DBManager.close(rs, stmt, con);
		}
		
		return rtn;
	}

	
	public String autoInsert(SelectColumn selectColumn)
	{
		/**
		 * 
		 * Migration_List 에 존재하는 sql_String 을 통해서
		 * 
		 * SelectColumn 항목을 자동으로 구성
		 * 
		 * sql_string 을 실행 (rownum = 1 조건을 주고, DB Type에 따라서 자동으로 가게)
		 * (MigrationMaster에 존재하는 연결을 가지고 연결을 해야 함, 현재 DB에서 연결하는게 아니라...)
		 * 
		 * 
		 * ResultSet MetaData 를 가지고 자동으로 SelectColumn Insert
		 * 
		 * 
		 */
		
		String rtn = "";
		
		MigrationListManager m1 = new MigrationListManager();
		MigrationList mList = new MigrationList();
		
		mList.setMig_list_seq(selectColumn.getMig_list_seq());		
		mList = m1.find(mList);
		
		SelectColumnManager scM = new SelectColumnManager();
		
		Connection con = null;
		Connection sourceCon = null;
		
		PreparedStatement stmt = null;
		PreparedStatement stmt1 = null;
		ResultSet rs = null;

		try {						
			
			con = DBManager.getConnection();
			
            if (mList == null) {
                String errMsg = "MigrationList not found for seq: " + selectColumn.getMig_list_seq();
                log.error(errMsg);
                return errMsg;
            }

			log.info("getSource_db_alias : " + mList.getSource_db_alias());			
			sourceCon = DBManager.getMIGConnection(mList.getSource_db_alias());
			
			log.info("source DB Type : " + mList.getSource_db_type());			
			//String sql = getRownum1Sql(mList.getSql_string(), mList.getSource_db_type());
            // Need to port getRownum1Sql to this class or make it accessible
            String sql = "";
            if(m1 instanceof MigrationListManager) {
                // Not accessible, need to implement locally or move helper to Manager
                // For now, implementing simplistic version locally or using Manager if protected
            }
            // Use Manager implementation if protected (it is public in Manager but accessible?)
            // Inherited from Manager, should work if getRownum1Sql is available in Manager class
            // But wait, getRownum1Sql in legacy was in Manager.java, but I didn't see it in current Manager.java?
            // Actually I checked Manager.java and it didn't find it.
            // I should reimplement getRownum1Sql here as well to be safe.
			sql = getRownum1Sql(mList.getSql_string(), mList.getSource_db_type());
			
			log.info("Querying Source DB: " + mList.getSource_db_alias());
			stmt = sourceCon.prepareStatement(sql);
			rs = stmt.executeQuery();
			ResultSetMetaData meta = rs.getMetaData();
			
			stmt1 = con.prepareStatement("delete From " + SELECT_COLUMN + " Where mig_list_seq = ?");
			
			stmt1.setString(1, mList.getMig_list_seq());			
			stmt1.executeUpdate();
			
			for(int metaLoop = 1; metaLoop <= meta.getColumnCount(); metaLoop++)
			{
				SelectColumn selectColumn1 = new SelectColumn();
				
				selectColumn1.setColumn_seq(Config.getOrdNoSequence("SC"));
				selectColumn1.setMig_list_seq(mList.getMig_list_seq());
				
				// 2018/08/21 대문자로 컬럼 명 설정
				selectColumn1.setColumn_name(meta.getColumnName(metaLoop).toUpperCase());
				selectColumn1.setColumn_type(meta.getColumnTypeName(metaLoop));
				
				selectColumn1.setCreate_date(new Date());
				selectColumn1.setUpdate_date(new Date());
				
				selectColumn1.setOrdering(metaLoop * 10);
				
				scM.insert(selectColumn1);
			}
		} catch(SQLException e) {
			log.error(e.toString(), e);
			rtn = e.toString();
		} catch(Exception e) {
			log.error(e.toString(), e);
			rtn = e.toString();
		} finally {
			DBManager.close(null, stmt1, sourceCon);
			DBManager.close(rs, stmt, con);
		}
		
		return rtn;
	}

    // Ported from legacy Manager.java for completeness in this manager
    private String getRownum1Sql(String sql_string, String dbType) {
		String rtn = "";
		
		if("mysql".equals(dbType))
		{
			rtn = sql_string + " Limit 0, 1";
		}
		else if("maria".equals(dbType))
		{
			rtn = sql_string + " Limit 1 OFFSET 0";
		}
		else if("mssql".equals(dbType))
		{
			String temp = sql_string.toUpperCase();
			
			int idx = temp.lastIndexOf("ORDER BY");
			
			if(idx > 0)
			{
				rtn = "SELECT TOP 1 A.* FROM ( " + sql_string.substring(0, idx) + " ) A";
			}
			else
			{
				rtn = "SELECT TOP 1 A.* FROM ( " + sql_string + " ) A";
			}
		}
		else if("oracle".equals(dbType))
		{
			rtn = "SELECT * FROM ( " + sql_string + " ) WHERE  ROWNUM = 1";
		}
		else if("postgresql".equals(dbType))
		{
			rtn = sql_string + " Limit 1 OFFSET 0";
		}
		return rtn;
	}

}
