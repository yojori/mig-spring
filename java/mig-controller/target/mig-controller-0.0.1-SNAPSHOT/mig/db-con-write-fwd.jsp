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
    <title>DB 연결 마스터 등록</title>
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
        .form-control:focus { border-color: #86b7fe; box-shadow: 0 0 0 0.25rem rgba(13,110,253,0.15); }
        .sticky-bottom { position: sticky; bottom: 0; z-index: 1020; background: #fff; border-top: 1px solid #eee; padding: 15px 0; }
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
                <li class="nav-item"><a class="nav-link active fw-bold text-white" href="./db-con-list.jsp">DB Connection 관리</a></li>
            </ul>
        </div>
    </div>
</nav>

<div class="container pb-5" style="max-width: 800px;">
    
    <!-- Title -->
    <div class="mb-4 text-center">
        <h3 class="fw-bold text-primary"><i class="bi bi-database-gear me-2"></i>DB 연결 정보 등록/수정</h3>
        <p class="text-muted">데이터베이스 접속 정보를 안전하게 관리하세요.</p>
    </div>

    <!-- Form -->
    <div class="card p-4">
        <form name="frm1" method="post" action="./db-con-write-proc.jsp" class="needs-validation" novalidate>
            <input type="hidden" name="mode" value="${master.mode}" />

            <div class="mb-3">
                <label for="master_code" class="form-label">Master Alias (Code)</label>
                <div class="input-group">
                    <span class="input-group-text"><i class="bi bi-tag-fill"></i></span>
                    <input type="text" class="form-control" id="master_code" name="master_code" value="${master.master_code}" placeholder="예: SOURCE_DB, TARGET_DB" required ${master.mode == 'update' ? 'readonly' : ''}>
                </div>
                <div class="form-text">시스템에서 문자로 식별할 유일한 코드를 입력하세요. (영문 대문자 권장)</div>
            </div>

            <div class="row g-3 mb-3">
                <div class="col-md-6">
                    <label for="db_type" class="form-label">DB Type</label>
                    <jaes:codeselect name="db_type" id="db_type" group="pageCode.code.code-0003" selected="${master.db_type}" />
                </div>
                <div class="col-md-6">
                    <label for="character_set" class="form-label">Encoding (Charset)</label>
                    <input type="text" class="form-control" id="character_set" name="character_set" value="${master.character_set}" placeholder="예: UTF-8, EUR-KR">
                </div>
            </div>

            <div class="mb-3">
                <label for="driverClass" class="form-label">Driver Class Name</label>
                <input type="text" class="form-control" id="driverClass" name="driverClass" value="${master.driverClass}" placeholder="예: oracle.jdbc.driver.OracleDriver">
            </div>

            <div class="mb-3">
                <label for="jdbcUrl" class="form-label">JDBC URL</label>
                <div class="input-group">
                    <span class="input-group-text"><i class="bi bi-link-45deg"></i></span>
                    <input type="text" class="form-control" id="jdbcUrl" name="jdbcUrl" value="${master.jdbcUrl}" placeholder="예: jdbc:oracle:thin:@localhost:1521/XE">
                </div>
            </div>

            <div class="row g-3 mb-4">
                <div class="col-md-6">
                    <label for="username" class="form-label">Username</label>
                     <div class="input-group">
                        <span class="input-group-text"><i class="bi bi-person-fill"></i></span>
                        <input type="text" class="form-control" id="username" name="username" value="${master.username}" placeholder="DB 계정 아이디">
                    </div>
                </div>
                <div class="col-md-6">
                    <label for="password" class="form-label">Password</label>
                    <div class="input-group">
                        <span class="input-group-text"><i class="bi bi-key-fill"></i></span>
                        <input type="password" class="form-control" id="password" name="password" value="${master.password}" placeholder="DB 계정 암호">
                    </div>
                </div>
            </div>

            <div class="d-grid gap-2 d-md-flex justify-content-md-end">
                <a href="./db-con-list.jsp" class="btn btn-secondary px-4 me-md-2">
                    <i class="bi bi-arrow-left"></i> 목록으로
                </a>
                <button type="submit" class="btn btn-primary px-5">
                    <i class="bi bi-save me-1"></i> 저장하기
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
