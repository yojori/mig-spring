<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ page import="com.yojori.util.*" %>
<%@ page import="com.yojori.manager.*" %>
<%@ page import="com.yojori.model.*" %>
<%@ page import="java.util.*" %>
<%@include file="/mig/session-admin-check.jsp" %>
<%
    MigrationList master = (MigrationList) RequestUtils.getBean(request, MigrationList.class);
    MigrationListManager manager = new MigrationListManager();

    if ("update".equals(master.getMode()) && master.getMig_list_seq() != null) {
        master = manager.find(master);
    } else {
        // 등록 시 기본값 설정
        master.setThread_use_yn("N");
        master.setThread_count(10);
        master.setPage_count_per_thread(10000);
        master.setExecute_yn("Y");
        master.setDisplay_yn("Y");
        // DB 연결 해서 Max + 10 으로 가져오기
        master.setOrdering(manager.findMax(master) + 10);
    }

    // DB Connection List 가져오기
    DBConnMasterManager dbManager = new DBConnMasterManager();
    DBConnMaster dbSearch = new DBConnMaster();
    List<DBConnMaster> dbList = dbManager.getList(dbSearch, InterfaceManager.LIST);

    request.setAttribute("dbList", dbList);
    request.setAttribute("master", master);

    request.getRequestDispatcher("./migration-list-write-fwd.jsp").forward(request, response);
%>