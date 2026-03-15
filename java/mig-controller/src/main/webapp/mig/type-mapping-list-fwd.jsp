<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="jaes" uri="http://www.yojori.com/taglib/jaes" %>
<%@include file="/mig/session-admin-check.jsp"%>
<!DOCTYPE html>
<html lang="ko">
<head>
    <meta charset="UTF-8">
    <title>DDL 타입 매핑 관리</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/css/bootstrap.min.css" rel="stylesheet">
    <link href="https://cdn.jsdelivr.net/npm/bootstrap-icons@1.10.5/font/bootstrap-icons.css" rel="stylesheet">
    <link href="https://fonts.googleapis.com/css2?family=Inter:wght@400;500;600&display=swap" rel="stylesheet">
    <script src="/mig/common.js"></script>
    <style>
        body { font-family: 'Inter', sans-serif; background-color: #f8f9fa; }
        .card { border-radius: 12px; border: none; box-shadow: 0 2px 10px rgba(0,0,0,0.05); }
        .table thead th { background-color: #f1f3f5; font-weight: 600; font-size: 0.85rem; color: #495057; }
    </style>
    <script>
        function goWrite(seq) {
            var url = "/mig/type-mapping-write.jsp";
            if(seq) url += "?mapping_seq=" + seq + "&mode=update";
            popup_window(url, "typeMappingWrite", "left=200, top=200, width=800, height=600");
        }
        function goDelete(seq) {
            if(confirm("정말 삭제하시겠습니까?")) {
                location.href = "type-mapping-proc.jsp?mode=delete&mapping_seq=" + seq;
            }
        }
        function search() {
            document.frm1.submit();
        }
    </script>
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
                <li class="nav-item"><a class="nav-link active fw-bold text-warning" href="./type-mapping-list.jsp">DB Type 관리</a></li>
            </ul>
        </div>
    </div>
</nav>

<div class="container py-4">
    <div class="d-flex justify-content-between align-items-center mb-4">
        <h4 class="fw-bold mb-0">DDL 타입 매핑 설정</h4>
        <button type="button" class="btn btn-primary" onclick="goWrite();"><i class="bi bi-plus-lg me-1"></i> 새 규칙 추가</button>
    </div>

    <form name="frm1" method="post" class="card p-3 mb-4">
        <div class="row g-3 align-items-center">
            <div class="col-auto">
                <label class="small fw-bold">Source DB</label>
                <select name="src_db_type" class="form-select form-select-sm">
                    <option value="">전체</option>
                    <option value="oracle" ${search.src_db_type == 'oracle' ? 'selected' : ''}>Oracle</option>
                    <option value="postgresql" ${search.src_db_type == 'postgresql' ? 'selected' : ''}>PostgreSQL</option>
                    <option value="maria" ${search.src_db_type == 'maria' ? 'selected' : ''}>MariaDB/MySQL</option>
                </select>
            </div>
            <div class="col-auto">
                <label class="small fw-bold">Target DB</label>
                <select name="tgt_db_type" class="form-select form-select-sm">
                    <option value="">전체</option>
                    <option value="postgresql" ${search.tgt_db_type == 'postgresql' ? 'selected' : ''}>PostgreSQL</option>
                    <option value="maria" ${search.tgt_db_type == 'maria' ? 'selected' : ''}>MariaDB/MySQL</option>
                </select>
            </div>
            <div class="col-auto align-self-end">
                <button type="button" class="btn btn-secondary btn-sm" onclick="search();"><i class="bi bi-search"></i> 검색</button>
            </div>
        </div>
    </form>

    <div class="card p-3">
        <div class="table-responsive">
            <table class="table table-hover align-middle mb-0">
                <thead>
                    <tr>
                        <th class="text-center">ID</th>
                        <th class="text-center">Source DB</th>
                        <th class="text-center">Source Type</th>
                        <th class="text-center">Target DB</th>
                        <th class="text-center">Target Type</th>
                        <th class="text-center">우선순위</th>
                        <th class="text-center">사용</th>
                        <th class="text-center">관리</th>
                    </tr>
                </thead>
                <tbody>
                    <c:forEach items="${list}" var="item">
                        <tr>
                            <td class="text-center text-muted small">${item.mapping_seq}</td>
                            <td class="text-center"><span class="badge bg-light text-dark border">${item.src_db_type}</span></td>
                            <td class="text-center fw-bold">${item.src_type_name}</td>
                            <td class="text-center"><span class="badge bg-light text-primary border">${item.tgt_db_type}</span></td>
                            <td class="text-center fw-bold text-primary">${item.tgt_type_name}</td>
                            <td class="text-center">${item.priority}</td>
                            <td class="text-center">
                                <c:choose>
                                    <c:when test="${item.use_yn == 'Y'}"><span class="badge bg-success">Y</span></c:when>
                                    <c:otherwise><span class="badge bg-secondary">N</span></c:otherwise>
                                </c:choose>
                            </td>
                            <td class="text-center">
                                <button type="button" class="btn btn-outline-primary btn-sm" onclick="goWrite('${item.mapping_seq}');">수정</button>
                                <button type="button" class="btn btn-outline-danger btn-sm" onclick="goDelete('${item.mapping_seq}');">삭제</button>
                            </td>
                        </tr>
                    </c:forEach>
                    <c:if test="${empty list}">
                        <tr><td colspan="8" class="text-center py-4 text-muted">등록된 매핑 규칙이 없습니다.</td></tr>
                    </c:if>
                </tbody>
            </table>
        </div>
    </div>
</div>
</body>
</html>
