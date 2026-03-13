<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="jaes" uri="http://www.yojori.com/taglib/jaes" %>
<%@include file="/mig/session-admin-check.jsp"%>
<!DOCTYPE html>
<html lang="ko">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>Kafka Param Template Manager</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/css/bootstrap.min.css" rel="stylesheet">
    <link href="https://cdn.jsdelivr.net/npm/bootstrap-icons@1.10.5/font/bootstrap-icons.css" rel="stylesheet">
    <link href="https://fonts.googleapis.com/css2?family=Inter:wght@400;500;600&display=swap" rel="stylesheet">
    <style>
        body { font-family: 'Inter', sans-serif; background-color: #f8f9fa; }
        .card { border-radius: 12px; border: none; box-shadow: 0 2px 10px rgba(0,0,0,0.05); }
        .table thead th { background-color: #f1f3f5; font-weight: 600; text-transform: uppercase; font-size: 0.85rem; color: #495057; }
        .badge-level { font-size: 0.75rem; padding: 0.3em 0.6em; }
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
                <li class="nav-item"><a class="nav-link" href="./type-mapping-list.jsp">DB Type 관리</a></li>
                <li class="nav-item"><a class="nav-link active fw-bold text-warning" href="./kfk-param-template-list.jsp"><i class="bi bi-gear-fill me-1"></i>Kafka Param Template 관리</a></li>
            </ul>
        </div>
    </div>
</nav>

<div class="container-fluid px-4">
    <div class="row mb-4 align-items-center">
        <div class="col-md-6">
            <h4 class="mb-0 fw-bold text-dark">Kafka Parameter Template 관리</h4>
            <p class="text-muted small mb-0">위저드 단계별 입력 항목(Level 0-3)을 정의합니다.</p>
        </div>
        <div class="col-md-6 text-end">
            <a href="kfk-param-template-write.jsp" class="btn btn-primary fw-bold">
                <i class="bi bi-plus-circle me-1"></i> 항목 추가
            </a>
        </div>
    </div>

    <div class="card p-3">
        <div class="table-responsive">
            <table class="table table-hover align-middle mb-0">
                <thead>
                    <tr>
                        <th class="text-center">ID</th>
                        <th class="text-center">Level</th>
                        <th>파라미터명 (Key)</th>
                        <th class="text-center">입력방식</th>
                        <th>설명</th>
                        <th class="text-center">필수</th>
                        <th class="text-center">관리</th>
                    </tr>
                </thead>
                <tbody>
                    <c:forEach items="${list}" var="item">
                        <tr>
                            <td class="text-center small text-muted">${item.id}</td>
                            <td class="text-center">
                                <span class="badge rounded-pill bg-secondary badge-level">Lv.${item.dp_level}</span>
                            </td>
                            <td>
                                <div class="fw-bold">${item.param_name}</div>
                                <div class="small text-primary"><code>${item.param_key}</code></div>
                            </td>
                            <td class="text-center">
                                <span class="badge bg-light text-dark border">${item.input_method}</span>
                            </td>
                            <td class="small text-muted">${item.param_explain}</td>
                            <td class="text-center">
                                <c:if test="${item.required_yn eq 'Y'}"><i class="bi bi-check-circle-fill text-danger text-opacity-75"></i></c:if>
                                <c:if test="${item.required_yn ne 'Y'}"><i class="bi bi-dash-circle text-muted text-opacity-50"></i></c:if>
                            </td>
                            <td class="text-center">
                                <div class="btn-group btn-group-sm">
                                    <a href="kfk-param-template-write.jsp?mode=update&id=${item.id}" class="btn btn-outline-secondary"><i class="bi bi-pencil"></i></a>
                                    <button type="button" class="btn btn-outline-danger" onclick="doDelete('${item.id}');"><i class="bi bi-trash"></i></button>
                                </div>
                            </td>
                        </tr>
                    </c:forEach>
                    <c:if test="${empty list}">
                        <tr><td colspan="7" class="text-center py-5 text-muted">등록된 파라미터 템플릿 항목이 없습니다.</td></tr>
                    </c:if>
                </tbody>
            </table>
        </div>
    </div>
</div>

<form name="delFrm" method="post" action="kfk-param-template-proc.jsp">
    <input type="hidden" name="mode" value="delete">
    <input type="hidden" name="id" value="">
</form>

<script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/js/bootstrap.bundle.min.js"></script>
<script>
    function doDelete(id) {
        if(confirm("정말 삭제하시겠습니까?")) {
            document.delFrm.id.value = id;
            document.delFrm.submit();
        }
    }
</script>
</body>
</html>
