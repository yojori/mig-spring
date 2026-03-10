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
    <title>Kafka Template Manager</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/css/bootstrap.min.css" rel="stylesheet">
    <link href="https://cdn.jsdelivr.net/npm/bootstrap-icons@1.10.5/font/bootstrap-icons.css" rel="stylesheet">
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
                <li class="nav-item"><a class="nav-link" href="./db-con-list.jsp">DB Connection 관리</a></li>
                <li class="nav-item"><a class="nav-link active fw-bold text-warning" href="./kfk-template-list.jsp">Kafka Template 관리</a></li>
            </ul>
        </div>
    </div>
</nav>

<div class="container px-4">
    <form name="frm1" method="get">
        
        <div class="row mb-4 align-items-center">
            <div class="col-md-6">
                <h4 class="mb-0 fw-bold">Kafka Template 관리</h4>
                <p class="text-muted small mb-0">실시간 이관을 위한 Kafka 커넥터 템플릿 정보를 관리합니다.</p>
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

        <div class="d-flex justify-content-end mb-3">
             <a href="kfk-template-write.jsp" class="btn btn-primary fw-bold">
                <i class="bi bi-plus-circle me-1"></i> Template 등록
            </a>
        </div>

        <div class="card p-3">
            <div class="table-responsive">
                <table class="table table-hover align-middle mb-0">
                    <thead>
                        <tr>
                            <th class="text-center">Template ID</th>
                            <th class="text-center">이름</th>
                            <th class="text-center">Type</th>
                            <th class="text-center">Class</th>
                            <th class="text-center">사용여부</th>
                            <th class="text-center">관리</th>
                        </tr>
                    </thead>
                    <tbody>
                        <c:forEach items="${list}" var="list">
                            <tr>
                                <td class="text-center fw-bold">
                                    <a href="kfk-template-write.jsp?mode=update&template_id=${list.template_id}" class="text-decoration-none text-dark">
                                        <i class="bi bi-box-seam me-2 text-secondary"></i>${list.template_id}
                                    </a>
                                </td>
                                <td>${list.template_name}</td>
                                <td class="text-center">
                                    <c:choose>
                                        <c:when test="${list.template_type eq 'SOURCE'}"><span class="badge bg-primary text-white border">SOURCE</span></c:when>
                                        <c:otherwise><span class="badge bg-success text-white border">TARGET</span></c:otherwise>
                                    </c:choose>
                                </td>
                                <td><span class="small text-muted">${list.connector_class}</span></td>
                                <td class="text-center">
                                    <c:choose>
                                        <c:when test="${list.use_yn eq 'Y'}"><span class="badge bg-light text-success border">Y</span></c:when>
                                        <c:otherwise><span class="badge bg-light text-secondary border">N</span></c:otherwise>
                                    </c:choose>
                                </td>
                                <td class="text-center">
                                    <a href="kfk-template-write.jsp?mode=update&template_id=${list.template_id}" class="btn btn-outline-secondary btn-sm">
                                        <i class="bi bi-pencil-square"></i> 수정
                                    </a>
                                </td>
                            </tr>
                        </c:forEach>
                        <c:if test="${empty list}">
                             <tr><td colspan="6" class="text-center py-4 text-muted">등록된 Template이 없습니다.</td></tr>
                        </c:if>
                    </tbody>
                </table>
            </div>
            
        </div>
    </form>
</div>

<script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/js/bootstrap.bundle.min.js"></script>
</body>
</html>
