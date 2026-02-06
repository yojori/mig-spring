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
    <title>Migration Master List</title>
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
        .status-badge { font-size: 0.8rem; padding: 0.4em 0.8em; border-radius: 20px; }
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
                <li class="nav-item"><a class="nav-link active fw-bold text-warning" href="./migration-master-list.jsp">Data 이관 Master</a></li>
                <li class="nav-item"><a class="nav-link" href="./migration-work-list.jsp">이관 진행 현황</a></li>
                <li class="nav-item"><a class="nav-link" href="./db-con-list.jsp">DB Connection 관리</a></li>
            </ul>
        </div>
    </div>
</nav>

<div class="container px-4">
    <form name="frm1" method="post">
        
        <!-- Header & Search -->
        <div class="row mb-4 align-items-center">
            <div class="col-md-6">
                <h4 class="mb-0 fw-bold">Data 이관 Master 목록</h4>
                <p class="text-muted small mb-0">마이그레이션 프로젝트(Master)를 관리합니다.</p>
            </div>
            <div class="col-md-6 text-end">
                <div class="card p-2 d-inline-block bg-white">
                    <div class="d-flex align-items-center">
                        <label class="me-2 small fw-bold text-secondary">표시여부</label>
                        <div class="me-2">
                             <jaes:coderadios name="display_yn" id="display_yn" defaultName="전체" defaultValue="" group="pageCode.code.code-0009" checked="${search.display_yn}" />
                        </div>
                        <button type="button" class="btn btn-primary btn-sm" onclick="document.frm1.submit();">
                            <i class="bi bi-search"></i> 검색
                        </button>
                    </div>
                </div>
            </div>
        </div>

        <!-- Action Toolbar -->
        <div class="d-flex justify-content-end mb-3">
             <a href="migration-master-write.jsp" class="btn btn-primary fw-bold">
                <i class="bi bi-plus-lg me-1"></i> Master 등록
            </a>
        </div>

        <!-- Main Table -->
        <div class="card p-3">
            <div class="table-responsive">
                <table class="table table-hover align-middle mb-0">
                    <thead>
                        <tr>
                            <th class="text-center">Master Code</th>
                            <th>마스터명</th>
                            <th class="text-center">표시여부</th>
                            <th class="text-center">정렬</th>
                            <th class="text-center">등록일</th>
                            <th class="text-center">관리</th>
                        </tr>
                    </thead>
                    <tbody>
                        <c:forEach items="${list}" var="list">
                            <tr>
                                <td class="text-center fw-bold text-secondary">
                                    <a href="migration-list.jsp?mig_master=${list.master_code}" class="text-decoration-none text-dark" title="작업 목록 이동">
                                        ${list.master_code} <i class="bi bi-box-arrow-in-right ms-1 text-primary"></i>
                                    </a>
                                </td>
                                <td>
                                    <a href="migration-master-write.jsp?mode=update&master_code=${list.master_code}" class="fw-bold text-primary text-decoration-none">
                                        ${list.master_name}
                                    </a>
                                </td>
                                <td class="text-center">
                                    <c:choose>
                                        <c:when test="${list.display_yn eq 'Y'}"><span class="badge bg-success status-badge">Y</span></c:when>
                                        <c:otherwise><span class="badge bg-light text-secondary border status-badge">N</span></c:otherwise>
                                    </c:choose>
                                </td>
                                <td class="text-center">${list.ordering}</td>
                                <td class="text-center text-muted small">${list.create_date}</td>
                                <td class="text-center">
                                    <a href="migration-master-write.jsp?mode=update&master_code=${list.master_code}" class="btn btn-outline-secondary btn-sm">
                                        <i class="bi bi-pencil-square"></i> 수정
                                    </a>
                                </td>
                            </tr>
                        </c:forEach>
                        <c:if test="${empty list}">
                             <tr><td colspan="6" class="text-center py-4 text-muted">등록된 Master 데이터가 없습니다.</td></tr>
                        </c:if>
                    </tbody>
                </table>
            </div>
        </div>
    </form>
    <iframe width=0 height=0 name='hiddenframe' style='display:none;'></iframe>
</div>

<script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/js/bootstrap.bundle.min.js"></script>
</body>
</html>
