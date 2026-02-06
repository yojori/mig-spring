<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="jaes" uri="http://www.yojori.com/taglib/jaes" %>
<%@include file="/mig/session-admin-check.jsp"%>
<!DOCTYPE html>
<html lang="ko">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>Migration List</title>
    <!-- Bootstrap 5 CSS -->
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/css/bootstrap.min.css" rel="stylesheet">
    <!-- Bootstrap Icons -->
    <link href="https://cdn.jsdelivr.net/npm/bootstrap-icons@1.10.5/font/bootstrap-icons.css" rel="stylesheet">
    <!-- Google Fonts -->
    <link href="https://fonts.googleapis.com/css2?family=Inter:wght@400;500;600&display=swap" rel="stylesheet">
    
    <script src="/mig/common.js"></script>
    <script src="/mig/ajax.js"></script>
    <style>
        body {
            font-family: 'Inter', sans-serif;
            background-color: #f8f9fa;
        }
        .navbar-brand { font-weight: 600; }
        .card { border-radius: 12px; border: none; box-shadow: 0 2px 10px rgba(0,0,0,0.05); }
        .table thead th { 
            background-color: #f1f3f5; 
            font-weight: 600; 
            text-transform: uppercase;
            font-size: 0.85rem;
            color: #495057;
        }
        .btn-sm { border-radius: 6px; }
        .status-badge { font-size: 0.8rem; padding: 0.4em 0.8em; border-radius: 20px; }
    </style>
    <script>
        // Existing Functions Preserved
        function goPopup(mig_master, mig_list_seq) {
            var url = "/mig/migration-list-write.jsp?mig_master=" + mig_master;
            if(mig_list_seq && mig_list_seq.length > 0) url += "&mode=update&mig_list_seq=" + mig_list_seq;
            popup_window(url, "downgrade", "left=150, top=150, width=1200, height=800, scrollbars=yes");
        }
        function goPopupSelect(mig_list_seq) {
            popup_window("/mig/select-column-list.jsp?mig_list_seq=" + mig_list_seq, "selectColumn", "left=150, top=150, width=1200, height=800, scrollbars=yes");
        }
        function goPopupInsert(mig_list_seq) {
            popup_window("/mig/insert-sql-list.jsp?mig_list_seq=" + mig_list_seq, "insertSql", "left=150, top=150, width=1200, height=800, scrollbars=yes");
        }
        function goPopupRelation(mig_list_seq) {
            popup_window("/mig/insert-column-list.jsp?mig_list_seq=" + mig_list_seq, "insertColumn", "left=150, top=150, width=1200, height=800, scrollbars=yes");
        }
        function goPopupTable(mig_list_seq) {
            popup_window("/mig/insert-table-write.jsp?mig_list_seq=" + mig_list_seq, "insertTable", "left=150, top=150, width=1200, height=800, scrollbars=yes");
        }
        function goExecute(master_code, mig_list_seq, mig_name) {
            popup_window("/mig/migration-proc.jsp?mig_master=" + master_code + "&mig_list_seq=" + mig_list_seq + "&mig_name=" + encodeURI(mig_name), "downgrade111", "left=150, top=150, width=1200, height=800, scrollbars=yes");
        }
        function goAll(master_code) {
            if(confirm("전체 실행 할까요?")) {
                var url    ="/mig/migration-proc.jsp";
                var title  = "downgrade222";
                window.open("", title, "left=150, top=150, width=800, height=600, scrollbars=yes");
                var frm = document.frm1;
                frm.target = title;
                frm.action = url;
                frm.submit();
            }
        }
        function search() {
            var frm = document.frm1;
            frm.action = "migration-list.jsp?mig_master=${master.master_code}";
            frm.submit();
        }
    </script>
</head>
<body>

<!-- Navbar -->
<nav class="navbar navbar-expand-lg navbar-dark bg-dark mb-4">
    <div class="container-fluid">
        <a class="navbar-brand" href="/mig/migration-master-list.jsp"><i class="bi bi-rocket-takeoff-fill me-2"></i>Migration 2.0</a>
        <button class="navbar-toggler" type="button" data-bs-toggle="collapse" data-bs-target="#navbarNav">
            <span class="navbar-toggler-icon"></span>
        </button>
        <div class="collapse navbar-collapse" id="navbarNav">
            <ul class="navbar-nav ms-auto">
                <li class="nav-item"><a class="nav-link active fw-bold text-warning" href="./migration-master-list.jsp">Data 이관 Master</a></li>
                <li class="nav-item"><a class="nav-link" href="./migration-work-list.jsp">이관 진행 현황</a></li>
                <li class="nav-item"><a class="nav-link" href="./db-con-list.jsp">DB Connection 관리</a></li>
            </ul>
        </div>
    </div>
</nav>

<div class="container-fluid px-4">
    <form name="frm1" method="post">
        
        <!-- Header & Search -->
        <div class="row mb-4 align-items-center">
            <div class="col-md-8">
                <h4 class="mb-0 fw-bold">이관 작업 목록</h4>
                <div class="text-muted small mt-1">
                    Master Code: <span class="badge bg-secondary">${master.master_code}</span> &nbsp; | &nbsp; 
                    Name: <strong>${master.master_name}</strong>
                </div>
            </div>
            <div class="col-md-4 text-end">
                <div class="card p-2 d-inline-block bg-white">
                    <div class="d-flex align-items-center">
                        <label class="me-2 small fw-bold text-secondary">표시여부</label>
                        <div class="me-2">
                            <jaes:coderadios name="display_yn" id="display_yn" defaultName="전체" defaultValue="" group="pageCode.code.code-0009" checked="${search.display_yn}" type="button" />
                        </div>
                        <button type="button" class="btn btn-primary btn-sm" onclick="search();"><i class="bi bi-search"></i> 검색</button>
                    </div>
                </div>
            </div>
        </div>

        <!-- Action Toolbar -->
        <div class="d-flex justify-content-between mb-3">
            <button type="button" class="btn btn-outline-success fw-bold" onclick="goAll('${master.master_code}');">
                <i class="bi bi-play-circle-fill me-1"></i> 전체 실행
            </button>
            <button type="button" class="btn btn-primary fw-bold" onclick="goPopup('${master.master_code}','');">
                <i class="bi bi-plus-lg me-1"></i> 이관 List 등록
            </button>
        </div>

        <!-- Main Table -->
        <div class="card p-3">
            <div class="table-responsive">
                <table class="table table-hover align-middle mb-0">
                    <thead>
                        <tr>
                            <th class="text-center">ID</th>
                            <th>이관명</th>
                            <th class="text-center">DB Source</th>
                            <th class="text-center">DB Target</th>
                            <th class="text-center">설정</th>
                            <th class="text-center">Thread</th>
                            <th class="text-center">Fetch</th>
                            <th class="text-center">표시</th>
                            <th class="text-center">사용</th>
                            <th class="text-center">유형</th>
                            <th class="text-center">Action</th>
                        </tr>
                    </thead>
                    <tbody>
                        <c:forEach items="${list}" var="list">
                            <!-- Hidden Fields -->
                            <input type="hidden" name="master_code" value="${master.master_code}">
                            <input type="hidden" name="mig_list_seq" value="${list.mig_list_seq}">
                            <input type="hidden" name="execute_yn" value="${list.execute_yn}">
                            <input type="hidden" name="display_yn" value="${list.display_yn}">
                            <input type="hidden" name="mig_name" value="${list.mig_name}">

                            <tr>
                                <td class="text-center fw-bold text-secondary">
                                    <a href="javascript:goPopup('${list.mig_master}','${list.mig_list_seq}');" class="text-decoration-none text-dark">${list.mig_list_seq}</a>
                                </td>
                                <td>
                                    <c:if test="${list.mig_type=='NORMAL' || list.mig_type=='THREAD' || list.mig_type=='THREAD_IDX' || list.mig_type=='THREAD_MULTI'}">
                                        <a href="javascript:goPopupSelect('${list.mig_list_seq}');" class="fw-bold text-primary text-decoration-none">
                                    </c:if>
                                    ${list.mig_name}
                                    <c:if test="${list.mig_type=='NORMAL' || list.mig_type=='THREAD' || list.mig_type=='THREAD_IDX' || list.mig_type=='THREAD_MULTI'}"></a></c:if>
                                </td>
                                <td class="text-center small">
                                    ${list.source_db_alias}<br>
                                    <span class="text-muted" style="font-size:0.75rem;">${list.source_db_type}</span>
                                </td>
                                <td class="text-center small">
                                    ${list.target_db_alias}<br>
                                    <span class="text-muted" style="font-size:0.75rem;">${list.target_db_type}</span>
                                </td>
                                <td class="text-center">
                                    <div class="btn-group btn-group-sm">
                                        <a href="javascript:goPopupInsert('${list.mig_list_seq}');" class="btn btn-outline-secondary btn-sm" title="Insert SQL"><i class="bi bi-file-earmark-code"></i></a>
                                        <a href="javascript:goPopupRelation('${list.mig_list_seq}');" class="btn btn-outline-secondary btn-sm" title="Mapping"><i class="bi bi-diagram-3"></i></a>
                                    </div>
                                </td>
                                <td class="text-center">${list.thread_count}</td>
                                <td class="text-center">${list.page_count_per_thread}</td>
                                <td class="text-center">
                                     <c:choose>
                                        <c:when test="${list.display_yn eq 'Y'}"><span class="badge bg-success status-badge">Y</span></c:when>
                                        <c:otherwise><span class="badge bg-light text-secondary border status-badge">N</span></c:otherwise>
                                    </c:choose>
                                </td>
                                <td class="text-center">
                                    <c:choose>
                                        <c:when test="${list.execute_yn eq 'Y'}"><span class="badge bg-primary status-badge">Y</span></c:when>
                                        <c:otherwise><span class="badge bg-light text-secondary border status-badge">N</span></c:otherwise>
                                    </c:choose>
                                </td>
                                <td class="text-center">
                                    <c:if test="${list.mig_type=='TABLE'}"><a href="javascript:goPopupTable('${list.mig_list_seq}');" class="badge bg-info text-dark text-decoration-none"></c:if>
                                    ${list.mig_type}
                                    <c:if test="${list.mig_type=='TABLE'}"></a></c:if>
                                </td>
                                <td class="text-center">
                                    <button type="button" class="btn btn-success btn-sm px-3" onclick="goExecute('${master.master_code}','${list.mig_list_seq}','${list.mig_name}')">
                                        <i class="bi bi-play-fill"></i> 실행
                                    </button>
                                </td>
                            </tr>
                        </c:forEach>
                        
                        <c:if test="${empty list}">
                            <tr><td colspan="11" class="text-center py-4 text-muted">등록된 마이그레이션 작업이 없습니다.</td></tr>
                        </c:if>
                    </tbody>
                </table>
            </div>
        </div>

    </form>
    <iframe width=0 height=0 name='hiddenframe' style='display:none;'></iframe>
</div>

<!-- Bootstrap 5 JS Bundle -->
<script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/js/bootstrap.bundle.min.js"></script>

</body>
</html>

