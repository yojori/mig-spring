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
    Connection sourceConn = null;
    
    PreparedStatement sourcePstmt = null;
    ResultSet sourceRs = null;  
    
    
    List<Map> resultList = new ArrayList();
    
    
    try {
        
        String query = "SELECT SEQ, REQ_NAME, HUBO_SEQ, HUBO_GUBUN, HUBO_NAME FROM OMR_CHOOCHUN ORDER BY SEQ ";
        
        out.println("select query : " + query);
        
        sourceConn = DBManager.getConnection();
        
        sourcePstmt = sourceConn.prepareStatement(query);
        
        sourceRs = sourcePstmt.executeQuery();
        
        int index = 0;
        
        while(sourceRs.next())
        {
            Map map = new HashMap();
            
            map.put("seq", sourceRs.getString("SEQ"));
            map.put("req_name", sourceRs.getString("REQ_NAME"));
            map.put("hubo_seq", sourceRs.getString("HUBO_SEQ"));
            map.put("hubo_gubun", sourceRs.getString("HUBO_GUBUN"));
            map.put("hubo_name", sourceRs.getString("HUBO_NAME"));
            
            resultList.add(map);
        }       
       
        
    } catch (SQLException se) {
        logger.error(se.toString(), se);
        //loop = false;
    } catch (Exception e) {
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
            } catch (SQLException e1) { }
        }
        
        if(sourceConn != null)
        {
            try {
                sourceConn.close();
            } catch (SQLException e) { }
        }
        
    }  // end of finally
    
    out.println("resultSize : " + resultList.size() + "<br>");
    
    //map.put("hubo_seq", sourceRs.getString("HUBO_SEQ"));
    //map.put("hubo_gubun", sourceRs.getString("HUBO_GUBUN"));
    //map.put("hubo_name", sourceRs.getString("HUBO_NAME"));    
%>
<table width="100%" cellpadding="0" cellspacing="0" border="1">
<tr height=34 align=center>
    <td>seq</td>
    <td>추천자명</td>
    <td>후보 SEQ</td>
    <td>후보구분</td>
    <td>후보자명</td>    
    <td>DELI_NO</td>
    <td>DELI_NAME</td>
    <td>DELI_STATUS</td>
    <td>DELI_AREA</td>
</tr>
<%    
    
    for(int i = 0; i < resultList.size(); i++)
    {
    	
    	Map map = resultList.get(i);
    	
    	String seq = (String)map.get("seq");
    	String req_name = (String)map.get("req_name");
    	
    	String hubo_seq = (String)map.get("hubo_seq");
    	String hubo_gubun = (String)map.get("hubo_gubun");
    	String hubo_name = (String)map.get("hubo_name");
    	
        //map.put("hubo_seq", sourceRs.getString("HUBO_SEQ"));
        //map.put("hubo_gubun", sourceRs.getString("HUBO_GUBUN"));
        //map.put("hubo_name", sourceRs.getString("HUBO_NAME"));        	
    	
        Connection sourceConn1 = null;
        
        PreparedStatement sourcePstmt1 = null;
        ResultSet sourceRs1 = null;  
        
        
        try {
            
            String query = "SELECT DELI_NO, RECV_NAME, DELI_STATUS, DELI_AREA FROM OMR_POST WHERE RECV_NAME LIKE '%' ? '%' ";
            
            //out.println("select query : " + query);
            
            sourceConn1 = DBManager.getConnection();
            
            sourcePstmt1 = sourceConn1.prepareStatement(query);
            
            sourcePstmt1.setString(1, req_name);
            
            sourceRs1 = sourcePstmt1.executeQuery();
            
            int loopList = 0;
            
            while(sourceRs1.next())
            {
            	
                //String bugo_seq = (String)map.get("bugo_seq");
                //String hubo_gubun = (String)map.get("hubo_gubun");
                //String hubo_name = (String)map.get("hubo_name");            	
%>
<tr height=34 align=center>
    <td><%=seq%></td>
    <td><%=req_name%></td>
    <td><%=hubo_seq%></td>
    <td><%=hubo_gubun%></td>
    <td><%=hubo_name%></td>        
    <td><%=sourceRs1.getString("deli_no")%></td>
    <td><%=sourceRs1.getString("recv_name")%></td>
    <td><%=sourceRs1.getString("deli_status")%></td>
    <td><%=sourceRs1.getString("deli_area")%></td>
</tr>
<%            	  
               loopList++;
            }       
            
            if(loopList == 0)
            {
%>
<tr height=34 align=center>
    <td><%=seq%></td>
    <td><%=req_name%></td>
    <td><%=hubo_seq%></td>
    <td><%=hubo_gubun%></td>
    <td><%=hubo_name%></td>      
    <td></td>
    <td></td>
    <td></td>
    <td></td>
</tr>
<%          	
            }
            
        } catch (SQLException se) {
            logger.error(se.toString(), se);
            //loop = false;
        } catch (Exception e) {
            logger.error(e.toString(), e);
            //loop = false;
        } finally {
            
            if(sourceRs1 != null)
            {
                try {
                    sourceRs1.close();
                } catch (SQLException e1) { }
            }
            
            if(sourcePstmt1 != null)
            {
                try {
                    sourcePstmt1.close();
                } catch (SQLException e1) { }
            }
            
            if(sourceConn1 != null)
            {
                try {
                    sourceConn1.close();
                } catch (SQLException e) { }
            }
            
        }  // end of finally    	
    	
    }
%>


