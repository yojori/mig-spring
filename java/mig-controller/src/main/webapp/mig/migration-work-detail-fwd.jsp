<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@include file="/mig/session-admin-check.jsp" %>
<!DOCTYPE html>
<html lang="ko">
<head>
    <meta charset="UTF-8">
    <title>이관 실행 상세 내역 (Work Seq: ${work_seq})</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/css/bootstrap.min.css" rel="stylesheet">
    <link href="https://cdn.jsdelivr.net/npm/bootstrap-icons@1.10.5/font/bootstrap-icons.css" rel="stylesheet">
    <link href="https://fonts.googleapis.com/css2?family=Inter:wght@400;500;600&display=swap" rel="stylesheet">
    <style>
        body { font-family: 'Inter', sans-serif; background-color: #f8f9fa; font-size: 0.9rem; }
        .table thead th { background-color: #f1f3f5; font-weight: 600; font-size: 0.8rem; color: #495057; }
        .query-params { max-width: 300px; white-space: nowrap; overflow: hidden; text-overflow: ellipsis; cursor: help; }
        .error-msg { color: #dc3545; max-width: 300px; white-space: nowrap; overflow: hidden; text-overflow: ellipsis; cursor: help; }
        .status-badge { font-size: 0.75rem; padding: 0.3em 0.6em; border-radius: 10px; }
    </style>
</head>
<body class="p-3">
    <div class="d-flex justify-content-between align-items-center mb-3">
        <h5 class="fw-bold mb-0">이관 실행 상세 내역 <span class="text-muted small">(#${work_seq})</span></h5>
        <button type="button" class="btn btn-sm btn-outline-secondary" onclick="window.close();">닫기</button>
    </div>

    <div class="card shadow-sm">
        <div class="table-responsive" style="max-height: 700px;">
            <table class="table table-hover align-middle mb-0">
                <thead class="sticky-top">
                    <tr>
                        <th class="text-center">Idx</th>
                        <th class="text-center">Thread</th>
                        <th class="text-center">Paging</th>
                        <th>Parameters / Query</th>
                        <th class="text-center">Read (cnt/ms)</th>
                        <th class="text-center">Proc (cnt/ms)</th>
                        <th class="text-center">상태</th>
                        <th class="text-center">시간</th>
                    </tr>
                </thead>
                <tbody>
                    <c:forEach items="${list}" var="item">
                        <tr>
                            <td class="text-center text-secondary small">${item.detail_seq}</td>
                            <td class="text-center fw-bold">${item.thread_idx}</td>
                            <td class="text-center">${item.paging_idx}</td>
                            <td>
                                <div class="query-params small text-muted" title="${item.query_params}">
                                    ${item.query_params}
                                </div>
                                <c:if test="${not empty item.err_msg}">
                                    <div class="error-msg small mt-1" title="${item.err_msg}">
                                        <i class="bi bi-exclamation-triangle-fill"></i> ${item.err_msg}
                                    </div>
                                </c:if>
                            </td>
                            <td class="text-center">
                                <span class="fw-bold text-dark">${item.read_cnt}</span>
                                <div class="text-muted small">${item.read_ms}ms</div>
                            </td>
                            <td class="text-center">
                                <span class="fw-bold text-primary">${item.proc_cnt}</span>
                                <div class="text-muted small">${item.proc_ms}ms</div>
                            </td>
                            <td class="text-center">
                                <c:choose>
                                    <c:when test="${item.status eq 'SUCCESS'}"><span class="badge bg-success status-badge">SUCCESS</span></c:when>
                                    <c:when test="${item.status eq 'FAIL'}"><span class="badge bg-danger status-badge">FAIL</span></c:when>
                                    <c:otherwise><span class="badge bg-secondary status-badge">${item.status}</span></c:otherwise>
                                </c:choose>
                            </td>
                            <td class="text-center small">
                                <fmt:formatDate value="${item.create_date}" pattern="HH:mm:ss" />
                            </td>
                        </tr>
                    </c:forEach>
                    <c:if test="${empty list}">
                        <tr>
                            <td colspan="8" class="text-center py-5 text-muted">상세 실행 내역이 존재하지 않습니다.</td>
                        </tr>
                    </c:if>
                </tbody>
            </table>
        </div>
    </div>

    <script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/js/bootstrap.bundle.min.js"></script>
</body>
</html>
