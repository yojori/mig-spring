<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ page import="com.yojori.util.*" %>
<%@ page import="com.yojori.manager.*" %>
<%@ page import="com.yojori.model.*" %>
<%@ page import="com.yojori.db.*" %>
<%@ page import="java.util.*" %>
<%@ page import="java.sql.*" %>
<%@ page import="java.io.*" %>
<%@ page import="oracle.sql.CLOB" %>
<%@ page import="oracle.jdbc.OracleResultSet" %>
<%@ page import="oracle.jdbc.driver.*" %>
<%@page import="org.apache.log4j.Logger"%>
<%!
	 static Logger logger = Logger.getLogger("contest-proc.jsp");
%>
<%

	String select_col = request.getParameter("select_col");
	String other_col = request.getParameter("other_col");
	String select_table = request.getParameter("select_table");

	Connection sourceConn = null;
	Connection targetConn = null;
	
	PreparedStatement sourcePstmt = null;
	PreparedStatement targetPstmt = null;
	ResultSet sourceRs = null;		
	
	ResultSet targetRs = null;	
	
	List<Map> migrationList = new ArrayList<Map>();
	List<String> columnInfo = new ArrayList<String>();
	
	long sourceTime = System.currentTimeMillis();
	long targetTime = 0;

	try {
		
		String query = "SELECT " + select_col + "," + other_col + " FROM " + select_table + " ORDER BY " + other_col;
		
		out.println("select query : " + query);
		
		sourceConn = DBManager.getConnection("jdbc/prod");
		
		sourcePstmt = sourceConn.prepareStatement(query);
		
		sourceRs = sourcePstmt.executeQuery();
		
		ResultSetMetaData meta = sourceRs.getMetaData();
		
		int resultCount = 0;
		
		for(int metaLoop = 1; metaLoop <= meta.getColumnCount(); metaLoop++)
		{
			columnInfo.add(meta.getColumnName(metaLoop).toUpperCase());
		}		
		
		while(sourceRs.next())
		{
			Map result = new HashMap();
			
			for(int metaLoop = 1; metaLoop <= meta.getColumnCount(); metaLoop++)
			{
				if(metaLoop == 1)
					result.put(meta.getColumnName(metaLoop), sourceRs.getClob(metaLoop));
				else
					result.put(meta.getColumnName(metaLoop), sourceRs.getObject(metaLoop));
			}
			
			migrationList.add(result);
			
			resultCount++;
		}		
		
		if(sourceRs != null) sourceRs.close();
		if(sourcePstmt != null) sourcePstmt.close();
		if(sourceConn != null) sourceConn.close();
		
		out.println("record Count : " + resultCount + ", migList.size : " + migrationList.size() +"<BR>");
		
		sourceTime = System.currentTimeMillis() - sourceTime;				
		targetTime = System.currentTimeMillis();		
		
		String[] pk_cols = other_col.split(",");
		
		StringBuffer insertSql = new StringBuffer()
		.append("UPDATE " + select_table + " SET " + select_col + " = EMPTY_CLOB() WHERE 1 = 1  ")
		;
		
		for(int pk_idx = 0; pk_idx < pk_cols.length; pk_idx++)
		{
			insertSql
			.append("and " + pk_cols[pk_idx] + " = ? ");
		}
		
		System.out.println("QUERY : " + insertSql.toString() + "<BR>");

		
		for(Map data : migrationList)
		{
			System.out.println("before get Connection");
			targetConn = DBManager.getConnection("jdbc/dev");
			System.out.println("after get Connection");
			targetConn.setAutoCommit(false);
		
		
			targetPstmt = targetConn.prepareStatement(insertSql.toString());
		
			for(int metaLoop = 1; metaLoop < columnInfo.size(); metaLoop++)
			{
					System.out.println("pstmt : " + (metaLoop));
					targetPstmt.setObject(metaLoop, data.get(columnInfo.get(metaLoop)));
			}
			
			int cnt = targetPstmt.executeUpdate();
			
			targetPstmt.clearParameters();
			
			targetPstmt.close();
		
		

			String querySelect = "SELECT " + select_col + " FROM " + select_table + " WHERE 1 = 1 ";
			
			for(int pk_idx = 0; pk_idx < pk_cols.length; pk_idx++)
			{
				querySelect = querySelect + " and " + pk_cols[pk_idx] + " = ? ";
			}
			
			querySelect = querySelect + " for UPDATE";
			
			System.out.println("queryselect : " + querySelect);
			
			targetPstmt = targetConn.prepareStatement(querySelect);
			
			
			for(int metaLoop = 1; metaLoop < columnInfo.size(); metaLoop++)
			{
					targetPstmt.setObject(metaLoop, data.get(columnInfo.get(metaLoop)));
			}			
			
			targetRs = targetPstmt.executeQuery();
			
		  if(targetRs.next())
		  {
		  	java.sql.Clob clob = (java.sql.Clob)targetRs.getClob(1);
		  	
		  	StringBuffer strOut = new StringBuffer();
		  	
		  	String str = "";
		  	
		  	java.sql.Clob clob_from = (Clob)data.get(columnInfo.get(0));
		  	
		  	if(clob_from != null) {
		  	
			  	BufferedReader br = new BufferedReader(clob_from.getCharacterStream());
			  	
			  	while((str = br.readLine()) != null) {
			  		strOut.append(str);
			  	}		  	
			  	
			  	
			  	Writer writer = clob.setCharacterStream(1);
			  	
			  	writer.write( strOut.toString() );
			  	
			  	writer.flush();
			  	writer.close();
			  	
			  	System.out.println("writer close");
			  }
		  }
		  
		  System.out.println("before commit");
			targetConn.commit();		  
			System.out.println("after commit");
		  
		  if(targetRs != null)
		  	targetRs.close();
		  	
		  if(targetPstmt != null)
		  	targetPstmt.close();
		  	
		  if(targetConn != null)
		  	targetConn.close();
		}
	} catch (SQLException se) {
		if(targetConn != null)
		{
			try {
				targetConn.rollback();
			} catch (SQLException e) { }
		}
		logger.error(se.toString(), se);
		//loop = false;
	} catch (Exception e) {
		if(targetConn != null)
		{
			try {
				targetConn.rollback();
			} catch (SQLException e1) { }
		}
		logger.error(e.toString(), e);
		//loop = false;
	} finally {
		
		if(sourceRs != null)
		{
			try {
				sourceRs.close();
			} catch (SQLException e1) { }
		}
		
		if(sourcePstmt != null)
		{
			try {
				sourcePstmt.close();
			} catch (SQLException e1) {	}
		}
		
		if(sourceConn != null)
		{
			try {
				sourceConn.close();
			} catch (SQLException e) { }
		}
		
		if(targetRs != null)
		{
			targetRs.close();
		}
		
		if(targetPstmt != null)
		{
			try {
				targetPstmt.close();
			} catch (SQLException e) { }
		}
		
		if(targetConn != null)
		{
			try {
				targetConn.close();
			} catch (SQLException e) { }
		}
	}  // end of finally

%>