<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@include file="/mig/session-admin-check.jsp"%>
<!DOCTYPE html>
<html lang="ko">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>Broker Management</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/css/bootstrap.min.css" rel="stylesheet">
    <link href="https://cdn.jsdelivr.net/npm/bootstrap-icons@1.10.5/font/bootstrap-icons.css" rel="stylesheet">
    <link href="https://fonts.googleapis.com/css2?family=Inter:wght@400;500;600&display=swap" rel="stylesheet">
    <style>
        body { font-family: 'Inter', sans-serif; background-color: #f8f9fa; }
        .navbar-brand { font-weight: 600; }
        .card { border-radius: 12px; border: none; box-shadow: 0 2px 10px rgba(0,0,0,0.05); }
        .table thead th { background-color: #f1f3f5; font-weight: 600; text-transform: uppercase; font-size: 0.85rem; color: #495057; }
    </style>
</head>
<body>

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
                <li class="nav-item"><a class="nav-link" href="./kfk-mig-list.jsp">KAFKA 목록</a></li>
                <li class="nav-item"><a class="nav-link" href="./db-con-list.jsp">DB Connection 관리</a></li>
                <li class="nav-item"><a class="nav-link" href="./type-mapping-list.jsp">DB Type 관리</a></li>
                <li class="nav-item"><a class="nav-link active fw-bold text-warning" href="./broker-list.jsp">Broker 관리</a></li>
            </ul>
        </div>
    </div>
</nav>

<div class="container px-4">
    <div class="row mb-4 align-items-center">
        <div class="col-md-8">
            <h4 class="mb-0 fw-bold">Broker 관리</h4>
            <p class="text-muted small mb-0">Kafka 브로커 접속 정보를 확인합니다. (설정은 `pageCode.xml`에서 관리됩니다.)</p>
        </div>
    </div>

    <div class="card p-3">
        <div class="table-responsive">
            <table class="table table-hover align-middle mb-0">
                <thead>
                    <tr>
                        <th class="text-center">Broker ID</th>
                        <th>이름</th>
                        <th class="text-center">Bootstrap Servers (ec1)</th>
                    </tr>
                </thead>
                <tbody>
                    <c:forEach items="${brokerList}" var="list">
                        <tr>
                            <td class="text-center fw-bold">${list.value}</td>
                            <td>${list.text}</td>
                            <td class="text-center"><code>${list.ec1}</code></td>
                        </tr>
                    </c:forEach>
                    <c:if test="${empty brokerList}">
                        <tr><td colspan="3" class="text-center py-4 text-muted">등록된 Broker가 없습니다.</td></tr>
                    </c:if>
                </tbody>
            </table>
        </div>
    </div>
    
    <div class="alert alert-info mt-4">
        <i class="bi bi-info-circle me-2"></i> 신규 브로커 추가나 수정은 서버의 <code>WEB-INF/config/pageCode.xml</code> 파일을 직접 편집해 주세요.
    </div>
</div>

<script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/js/bootstrap.bundle.min.js"></script>
</body>
</html>
