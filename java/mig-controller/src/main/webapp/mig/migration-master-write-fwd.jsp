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
    <title>Data 이관 Master 등록</title>
    <!-- Bootstrap 5 CSS -->
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/css/bootstrap.min.css" rel="stylesheet">
    <!-- Bootstrap Icons -->
    <link href="https://cdn.jsdelivr.net/npm/bootstrap-icons@1.10.5/font/bootstrap-icons.css" rel="stylesheet">
    <!-- Google Fonts -->
    <link href="https://fonts.googleapis.com/css2?family=Inter:wght@400;500;600&display=swap" rel="stylesheet">
    
    <style>
        body { font-family: 'Inter', sans-serif; background-color: #f8f9fa; }
        .navbar-brand { font-weight: 600; }
        .card { border-radius: 12px; border: none; box-shadow: 0 4px 15px rgba(0,0,0,0.05); }
        .form-label { font-weight: 500; color: #495057; }
        .form-control:focus { border-color: #ffc107; box-shadow: 0 0 0 0.25rem rgba(255,193,7,0.25); }
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

<div class="container pb-5" style="max-width: 700px;">
    
    <!-- Title -->
    <div class="mb-4 text-center">
        <h3 class="fw-bold text-dark"><i class="bi bi-folder-plus me-2 text-warning"></i>Data 이관 Master 등록/수정</h3>
        <p class="text-muted">마이그레이션 프로젝트(Master)의 기본 정보를 설정합니다.</p>
    </div>

    <!-- Form -->
    <div class="card p-4">
        <form name="frm1" method="post" action="./migration-master-proc.jsp" class="needs-validation" novalidate>
            <input type="hidden" name="mode" value="${master.mode}" />
            <input type="hidden" name="master_code" value="${master.master_code}" />

            <div class="mb-4">
                <label for="master_name" class="form-label">프로젝트 명 (Master Name)</label>
                <div class="input-group">
                    <span class="input-group-text bg-light"><i class="bi bi-card-heading"></i></span>
                    <input type="text" class="form-control form-control-lg" id="master_name" name="master_name" value="${master.master_name}" placeholder="예: 차세대 시스템 데이터 이관" required>
                </div>
            </div>

            <div class="row g-3 mb-4">
                <div class="col-md-6">
                    <label class="form-label d-block">표시 여부</label>
                    <div class="border rounded p-2 px-3 bg-light d-flex align-items-center" style="height: 48px;">
                         <jaes:coderadios name="display_yn" id="display_yn" group="pageCode.code.code-0009" checked="${master.display_yn}" />
                    </div>
                </div>
                <div class="col-md-6">
                    <label for="ordering" class="form-label">표시 순서</label>
                    <div class="input-group">
                        <span class="input-group-text bg-light"><i class="bi bi-sort-numeric-down"></i></span>
                        <input type="number" class="form-control" id="ordering" name="ordering" value="${master.ordering}" placeholder="0">
                    </div>
                </div>
            </div>

            <!-- Action Buttons -->
            <div class="d-grid gap-2 d-md-flex justify-content-md-end border-top pt-4">
                <a href="./migration-master-list.jsp" class="btn btn-secondary px-4 me-md-2">
                    <i class="bi bi-arrow-left"></i> 목록으로
                </a>
                <button type="submit" class="btn btn-warning px-5 fw-bold text-dark">
                    <i class="bi bi-check-lg me-1"></i> 저장하기
                </button>
            </div>
        </form>
    </div>
</div>

<!-- Hidden Frame -->
<iframe width=0 height=0 name='hiddenframe' style='display:none;'></iframe>

<script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/js/bootstrap.bundle.min.js"></script>
</body>
</html>
