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
    <title>이관 목록 등록 - 컬럼 선택</title>
    <!-- Bootstrap 5 CSS -->
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/css/bootstrap.min.css" rel="stylesheet">
    <!-- Bootstrap Icons -->
    <link href="https://cdn.jsdelivr.net/npm/bootstrap-icons@1.10.5/font/bootstrap-icons.css" rel="stylesheet">
    <!-- Google Fonts -->
    <link href="https://fonts.googleapis.com/css2?family=Inter:wght@400;500;600&display=swap" rel="stylesheet">
    
    <style>
        body { font-family: 'Inter', sans-serif; background-color: #f8f9fa; }
        .card { border-radius: 12px; border: none; box-shadow: 0 4px 15px rgba(0,0,0,0.05); }
        .table thead th { background-color: #f1f3f5; font-weight: 600; text-transform: uppercase; font-size: 0.85rem; color: #495057; }
        .table-hover tbody tr:hover { background-color: #f8f9fa; }
    </style>
    
    <script>
        function goRegist() {
            document.getElementById("mode").value = "auto";
            document.getElementById("frm1").submit();
        }
        
        function goNext() {
            location.href = '/mig/insert-sql-list.jsp?mig_list_seq=${search.mig_list_seq}';
        }
        
        function confirmNext() {
            if('${search.mode}' == 'auto') {
                goNext();   
            }
        }
    </script>
</head>
<body onload="confirmNext();" class="p-3">

<div class="container-fluid">
    <div class="card p-4">
        
        <!-- Header -->
        <div class="d-flex justify-content-between align-items-center mb-4 border-bottom pb-3">
             <div class="d-flex align-items-center gap-2">
                <span class="badge bg-primary rounded-pill">STEP 2</span>
                <h4 class="fw-bold mb-0 text-dark">컬럼 자동 등록 (Select Column)</h4>
            </div>
            <button type="button" class="btn btn-close" onclick="self.close();" aria-label="Close"></button>
        </div>

        <form name="frm1" id="frm1" method="post" action="./select-column-list.jsp">
            <input type="hidden" name="mode" id="mode" value="${search.mode}" />
            <input type="hidden" name="mig_list_seq" id="mig_list_seq" value="${search.mig_list_seq}" />

            <!-- Action Buttons -->
            <div class="d-flex justify-content-end mb-3 gap-2">
                <button type="button" class="btn btn-outline-primary" onclick="goRegist();">
                    <i class="bi bi-magic me-1"></i> 자동 컬럼 등록
                </button>
                 <button type="button" class="btn btn-primary fw-bold" onclick="goNext();">
                    다음 단계 <i class="bi bi-chevron-right ms-1"></i>
                </button>
            </div>

            <!-- Main Table -->
            <div class="table-responsive border rounded">
                <table class="table table-hover align-middle mb-0">
                    <thead>
                        <tr>
                            <th class="text-center">Column Seq</th>
                            <th>Column Name</th>
                            <th class="text-center">Type</th>
                            <th class="text-center">Ordering</th>
                            <th class="text-center">등록일</th>
                        </tr>
                    </thead>
                    <tbody>
                        <c:forEach items="${list}" var="list">
                            <tr>
                                <td class="text-center fw-bold text-secondary">${list.column_seq}</td>
                                <td><span class="fw-bold text-dark">${list.column_name}</span></td>
                                <td class="text-center">
                                    <span class="badge bg-light text-dark border">${list.column_type}</span>
                                </td>
                                <td class="text-center">${list.ordering}</td>
                                <td class="text-center text-muted small">${list.create_date}</td>
                            </tr>
                        </c:forEach>
                        <c:if test="${empty list}">
                             <tr><td colspan="5" class="text-center py-5 text-muted">
                                 <i class="bi bi-table d-block fs-3 mb-2"></i>
                                 등록된 컬럼 정보가 없습니다.<br>
                                 '자동 컬럼 등록' 버튼을 눌러주세요.
                             </td></tr>
                        </c:if>
                    </tbody>
                </table>
            </div>
            
        </form>
    </div>
</div>

<script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/js/bootstrap.bundle.min.js"></script>
</body>
</html>
