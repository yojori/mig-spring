<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
    <%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
        <%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
            <%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
                <%@ taglib prefix="jaes" uri="http://www.yojori.com/taglib/jaes" %>
                    <%@include file="/mig/session-admin-check.jsp" %>
                        <!DOCTYPE html>
                        <html lang="ko">

                        <head>
                            <meta charset="UTF-8">
                            <meta name="viewport" content="width=device-width, initial-scale=1">
                            <title>이관 진행 현황</title>
                            <!-- Bootstrap 5 CSS -->
                            <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/css/bootstrap.min.css"
                                rel="stylesheet">
                            <!-- Bootstrap Icons -->
                            <link href="https://cdn.jsdelivr.net/npm/bootstrap-icons@1.10.5/font/bootstrap-icons.css"
                                rel="stylesheet">
                            <!-- Google Fonts -->
                            <link href="https://fonts.googleapis.com/css2?family=Inter:wght@400;500;600&display=swap"
                                rel="stylesheet">

                            <script src="/mig/common.js"></script>
                            <script src="/mig/ajax.js"></script>
                            <style>
                                body {
                                    font-family: 'Inter', sans-serif;
                                    background-color: #f8f9fa;
                                }

                                .navbar-brand {
                                    font-weight: 600;
                                }

                                .card {
                                    border-radius: 12px;
                                    border: none;
                                    box-shadow: 0 2px 10px rgba(0, 0, 0, 0.05);
                                }

                                .table thead th {
                                    background-color: #f1f3f5;
                                    font-weight: 600;
                                    text-transform: uppercase;
                                    font-size: 0.85rem;
                                    color: #495057;
                                }

                                .status-badge {
                                    font-size: 0.8rem;
                                    padding: 0.4em 0.8em;
                                    border-radius: 20px;
                                }

                                .table-hover tbody tr:hover {
                                    background-color: #f8f9fa;
                                }

                                .hover-link:hover {
                                    color: #0d6efd !important;
                                    text-decoration: underline !important;
                                }
                            </style>
                        </head>

                        <body>

                            <!-- Navbar -->
                            <nav class="navbar navbar-expand-lg navbar-dark bg-dark mb-4">
                                <div class="container-fluid">
                                    <a class="navbar-brand" href="/mig/migration-master-list.jsp"><i
                                            class="bi bi-rocket-takeoff-fill me-2"></i>Migration 2.0</a>
                                    <button class="navbar-toggler" type="button" data-bs-toggle="collapse"
                                        data-bs-target="#navbarNav">
                                        <span class="navbar-toggler-icon"></span>
                                    </button>
                                    <div class="collapse navbar-collapse" id="navbarNav">
                                        <ul class="navbar-nav ms-auto">
                                            <li class="nav-item"><a class="nav-link"
                                                    href="./migration-master-list.jsp">Data 이관 Master</a></li>
                                            <li class="nav-item"><a class="nav-link active fw-bold text-warning"
                                                    href="./migration-work-list.jsp">이관 진행 현황</a></li>
                                            <li class="nav-item"><a class="nav-link" href="./db-con-list.jsp">DB
                                                    Connection 관리</a></li>
                                        </ul>
                                    </div>
                                </div>
                            </nav>

                            <div class="container px-4">
                                <form name="frm1" method="get">

                                    <!-- Header & Search -->
                                    <div class="row mb-4 align-items-center">
                                        <div class="col-md-6">
                                            <h4 class="mb-0 fw-bold">이관 진행 현황</h4>
                                            <p class="text-muted small mb-0">마이그레이션 작업 실행 이력을 조회합니다.</p>
                                        </div>
                                        <div class="col-md-6 text-end">
                                            <div class="card p-2 d-inline-block bg-white">
                                                <div class="d-flex align-items-center">
                                                    <label class="me-2 small fw-bold text-secondary">작업 상태</label>
                                                    <select name="status" class="form-select form-select-sm me-2"
                                                        style="width: 120px;">
                                                        <option value="">전체</option>
                                                        <option value="READY" ${search.status=='READY' ? 'selected' : ''
                                                            }>대기중 (READY)</option>
                                                        <option value="RUNNING" ${search.status=='RUNNING' ? 'selected'
                                                            : '' }>진행중 (RUNNING)</option>
                                                        <option value="DONE" ${search.status=='DONE' ? 'selected' : ''
                                                            }>완료 (DONE)</option>
                                                        <option value="FAIL" ${search.status=='FAIL' ? 'selected' : ''
                                                            }>실패 (FAIL)</option>
                                                    </select>
                                                    <button type="button" class="btn btn-primary btn-sm"
                                                        onclick="document.frm1.submit();">
                                                        <i class="bi bi-search"></i> 검색
                                                    </button>
                                                </div>
                                            </div>
                                        </div>
                                    </div>

                                    <!-- Main Table -->
                                    <div class="card p-3">
                                        <div class="table-responsive">
                                            <table class="table table-hover align-middle mb-0">
                                                <thead>
                                                    <tr>
                                                        <th class="text-center">Work SEQ</th>
                                                        <th>이관명</th>
                                                        <th class="text-center">Worker ID</th>
                                                        <th class="text-center">상태</th>
                                                        <th class="text-center">시작 시간</th>
                                                        <th class="text-center">종료 시간</th>
                                                        <th class="text-center">반영 건수</th>
                                                        <th class="text-center">결과 메시지</th>
                                                        <th class="text-center">등록일</th>
                                                    </tr>
                                                </thead>
                                                <tbody>
                                                    <c:forEach items="${list}" var="list">
                                                        <tr>
                                                            <td class="text-center fw-bold text-secondary">
                                                                ${list.work_seq}
                                                            </td>
                                                            <td>
                                                                <span
                                                                    class="fw-bold text-primary">${list.mig_name}</span>
                                                                <a href="./migration-list.jsp?mig_master=${list.mig_master}" class="text-muted small text-decoration-none hover-link">(${list.mig_list_seq})</a>
                                                            </td>
                                                            <td class="text-center small text-muted">${list.worker_id}
                                                            </td>
                                                            <td class="text-center">
                                                                <c:choose>
                                                                    <c:when test="${list.status eq 'READY'}"><span
                                                                            class="badge bg-secondary status-badge">READY</span>
                                                                    </c:when>
                                                                    <c:when test="${list.status eq 'RUNNING'}"><span
                                                                            class="badge bg-primary status-badge">RUNNING</span>
                                                                    </c:when>
                                                                    <c:when test="${list.status eq 'DONE'}"><span
                                                                            class="badge bg-success status-badge">DONE</span>
                                                                    </c:when>
                                                                    <c:when test="${list.status eq 'FAIL'}"><span
                                                                            class="badge bg-danger status-badge">FAIL</span>
                                                                    </c:when>
                                                                    <c:otherwise><span
                                                                            class="badge bg-light text-dark border status-badge">${list.status}</span>
                                                                    </c:otherwise>
                                                                </c:choose>
                                                            </td>
                                                            <td class="text-center small">
                                                                <fmt:formatDate value="${list.start_date}"
                                                                    pattern="MM-dd HH:mm:ss" />
                                                            </td>
                                                            <td class="text-center small">
                                                                <fmt:formatDate value="${list.end_date}"
                                                                    pattern="MM-dd HH:mm:ss" />
                                                                <c:if test="${list.status eq 'DONE'}">
                                                                    <div class="text-secondary"
                                                                        style="font-size: 0.85em;">${list.durationStr}
                                                                    </div>
                                                                </c:if>
                                                            </td>
                                                            <td class="text-center small">
                                                                <div class="text-secondary" title="Read Count">R:
                                                                    <fmt:formatNumber value="${list.read_count}"
                                                                        pattern="#,###" />
                                                                </div>
                                                                <div class="text-primary fw-bold"
                                                                    title="Processed Count">P:
                                                                    <fmt:formatNumber value="${list.proc_count}"
                                                                        pattern="#,###" />
                                                                </div>
                                                            </td>
                                                            <td class="text-center small text-truncate"
                                                                style="max-width: 200px;" title="${list.result_msg}">
                                                                ${list.result_msg}
                                                            </td>
                                                            <td class="text-center small text-muted">
                                                                <fmt:formatDate value="${list.create_date}"
                                                                    pattern="yyyy-MM-dd HH:mm:ss" />
                                                            </td>
                                                        </tr>
                                                    </c:forEach>
                                                    <c:if test="${empty list}">
                                                        <tr>
                                                            <td colspan="9" class="text-center py-4 text-muted">등록된 작업
                                                                내역이 없습니다.</td>
                                                        </tr>
                                                    </c:if>
                                                </tbody>
                                            </table>
                                        </div>

                                        <!-- Paging -->
                                        <div class="d-flex justify-content-center mt-4">
                                            <jaes:paging totalCount="${search.totalCount}"
                                                linkUrl="./migration-work-list.jsp?status=${search.status}"
                                                pageSize="${search.pageSize}" skin="front"
                                                currentPage="${search.currentPage}" prefix="search" />
                                        </div>
                                    </div>
                                    <input type="hidden" name="currentPage" value="${search.currentPage}">
                                </form>
                                <iframe width=0 height=0 name='hiddenframe' style='display:none;'></iframe>
                            </div>

                            <script
                                src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/js/bootstrap.bundle.min.js"></script>
                        </body>

                        </html>