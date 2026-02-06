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
    <title>DB Connection Manager</title>
    <!-- Bootstrap 5 CSS -->
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/css/bootstrap.min.css" rel="stylesheet">
    <!-- Bootstrap Icons -->
    <link href="https://cdn.jsdelivr.net/npm/bootstrap-icons@1.10.5/font/bootstrap-icons.css" rel="stylesheet">
    <!-- Google Fonts -->
    <link href="https://fonts.googleapis.com/css2?family=Inter:wght@400;500;600&display=swap" rel="stylesheet">
    
    <style>
        body { font-family: 'Inter', sans-serif; background-color: #f8f9fa; }
        .navbar-brand { font-weight: 600; }
        .card { border-radius: 12px; border: none; box-shadow: 0 2px 10px rgba(0,0,0,0.05); }
        .table thead th { background-color: #f1f3f5; font-weight: 600; text-transform: uppercase; font-size: 0.85rem; color: #495057; }
        .table-hover tbody tr:hover { background-color: #f8f9fa; }
    </style>
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
                <li class="nav-item"><a class="nav-link" href="./migration-master-list.jsp">Data 이관 Master</a></li>
                <li class="nav-item"><a class="nav-link" href="./migration-work-list.jsp">이관 진행 현황</a></li>
                <li class="nav-item"><a class="nav-link active fw-bold text-warning" href="./db-con-list.jsp">DB Connection 관리</a></li>
            </ul>
        </div>
    </div>
</nav>

<div class="container px-4">
    <form name="frm1" method="get">
        
        <!-- Header & Search -->
        <div class="row mb-4 align-items-center">
            <div class="col-md-6">
                <h4 class="mb-0 fw-bold">DB Connection 관리</h4>
                <p class="text-muted small mb-0">마이그레이션 대상 데이터베이스 연결 정보를 관리합니다.</p>
            </div>
            <div class="col-md-6 text-end">
                <div class="card p-2 d-inline-block bg-white">
                    <div class="input-group">
                        <input type="text" class="form-control form-control-sm" name="searchWord" value="${search.searchWord}" placeholder="검색어 입력" />
                        <button type="button" class="btn btn-primary btn-sm" onclick="document.frm1.submit();">
                            <i class="bi bi-search"></i> 검색
                        </button>
                    </div>
                </div>
            </div>
        </div>

        <!-- Action Toolbar -->
        <div class="d-flex justify-content-end mb-3">
             <a href="db-con-write.jsp" class="btn btn-primary fw-bold">
                <i class="bi bi-database-add me-1"></i> Connection 등록
            </a>
        </div>

        <!-- Main Table -->
        <div class="card p-3">
            <div class="table-responsive">
                <table class="table table-hover align-middle mb-0">
                    <thead>
                        <tr>
                            <th class="text-center">Alias (Code)</th>
                            <th class="text-center">DB Type</th>
                            <th class="text-center">Charset</th>
                            <th class="text-center">등록일</th>
                            <th class="text-center">관리</th>
                        </tr>
                    </thead>
                    <tbody>
                        <c:forEach items="${list}" var="list">
                            <tr>
                                <td class="text-center fw-bold">
                                    <a href="db-con-write.jsp?mode=update&master_code=${list.master_code}" class="text-decoration-none text-dark">
                                        <i class="bi bi-database me-2 text-secondary"></i>${list.master_code}
                                    </a>
                                </td>
                                <td class="text-center">
                                    <span class="badge bg-light text-dark border">${list.db_type}</span>
                                </td>
                                <td class="text-center">${list.character_set}</td>
                                <td class="text-center text-muted small">${list.create_date}</td>
                                <td class="text-center">
                                    <a href="db-con-write.jsp?mode=update&master_code=${list.master_code}" class="btn btn-outline-secondary btn-sm">
                                        <i class="bi bi-pencil-square"></i> 수정
                                    </a>
                                </td>
                            </tr>
                        </c:forEach>
                        <c:if test="${empty list}">
                             <tr><td colspan="5" class="text-center py-4 text-muted">등록된 DB Connection이 없습니다.</td></tr>
                        </c:if>
                    </tbody>
                </table>
            </div>
            
            <!-- Paging -->
            <div class="d-flex justify-content-center mt-4">
                 <jaes:paging totalCount="${search.totalCount}" linkUrl="./db-con-list.jsp?searchWord=${search.searchWord}" pageSize="${search.pageSize}" skin="front" currentPage="${search.currentPage}" prefix="search" />
            </div>
        </div>
    </form>
    <iframe width=0 height=0 name='hiddenframe' style='display:none;'></iframe>
</div>

<script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/js/bootstrap.bundle.min.js"></script>
</body>
</html>
