<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ page import="com.yojori.util.*" %>
<%@ page import="com.yojori.manager.*" %>
<%@ page import="com.yojori.migration.controller.model.*" %>
<%@ page import="java.util.*" %>
<%@include file="/mig/session-admin-check.jsp" %>
<%  
    InsertSql search = (InsertSql) RequestUtils.getBean(request, InsertSql.class);
    InsertSqlManager manager = new InsertSqlManager();

    if ("insertAll".equals(search.getMode())) {
        String[] insert_sql_seq = request.getParameterValues("insert_sql_seq");
        String[] insert_type = request.getParameterValues("insert_type");
        String[] insert_table = request.getParameterValues("insert_table");
        String[] pk_column = request.getParameterValues("pk_column");
        String[] truncate_yn = request.getParameterValues("truncate_yn");
        
        System.out.println("DEBUG: InsertSql Update - seq_len=" + (insert_sql_seq!=null?insert_sql_seq.length:0) + ", trunc_len=" + (truncate_yn!=null?truncate_yn.length:0));

        for (int i = 0; i < insert_sql_seq.length; i++) {
        	System.out.println("DEBUG: Row " + i + " Seq=" + insert_sql_seq[i] + " trunc=" + (truncate_yn!=null && i < truncate_yn.length ? truncate_yn[i] : "null"));
            InsertSql entity = new InsertSql();
            
            entity.setMig_list_seq(search.getMig_list_seq());
            entity.setInsert_type(insert_type[i]);
            entity.setInsert_table(insert_table[i]);
            entity.setPk_column(pk_column[i]);
            
            entity.setTruncate_yn("N");
            
            if (truncate_yn != null && !"".equals(insert_sql_seq[i]) && truncate_yn[i] != null && "Y".equals(truncate_yn[i])) {
            	entity.setTruncate_yn("Y");	
            }
            
            if (insert_table[i] == null || insert_table[i].length() == 0) {
                continue;
            } else if (insert_sql_seq[i] == null || insert_sql_seq[i].length() == 0) { // insert
                entity.setInsert_sql_seq(Config.getOrdNoSequence("IS"));
                
                entity.setCreate_date(new Date());
                entity.setUpdate_date(new Date());
                
                manager.insert(entity);
            } else if (insert_sql_seq[i] != null && insert_sql_seq[i].length() > 0) { // update
                entity.setInsert_sql_seq(insert_sql_seq[i]);
                entity.setUpdate_date(new Date());
                
                manager.update(entity);
            }
        }
    } else if ("autoInsert".equals(search.getMode())) {
    	if (request.getParameter("auto_insert_sql_seq") != null) {
    		search.setInsert_sql_seq(request.getParameter("auto_insert_sql_seq"));
    		search.setInsert_table(request.getParameter("auto_insert_table"));
    		
    		String rtn = manager.autoInsert(search);
    		
    		if (rtn != null && rtn.length() > 0) {
    			 out.println(rtn);
    			 return;
    		}
    	}
    } else if ("goDelete".equals(search.getMode())) {
    	if (request.getParameter("auto_insert_sql_seq") != null) {
    		search.setInsert_sql_seq(request.getParameter("auto_insert_sql_seq"));
    		manager.goDelete(search);
    	}
    }
    
    List<InsertSql> list = manager.getList(search, InterfaceManager.LIST);
    
    request.setAttribute("search", search);
    request.setAttribute("list", list);

    MigrationListManager migManager = new MigrationListManager();
    MigrationList migList = new MigrationList();
    migList.setMig_list_seq(search.getMig_list_seq());
    migList = migManager.find(migList);
    request.setAttribute("migList", migList);
   
    request.getRequestDispatcher("./insert-sql-list-fwd.jsp").forward(request, response);
%>
