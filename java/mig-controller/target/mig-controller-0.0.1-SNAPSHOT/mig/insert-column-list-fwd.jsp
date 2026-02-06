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
    <title>Insert Column 설정</title>
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
        .form-select-sm, .form-control-sm { font-size: 0.9rem; }
    </style>
    
    <script>
        function goRegist() {
            document.frm1.mode.value = "insertAll";
            document.frm1.submit();
        }
        
        function deleteColumn(insert_column_seq) {
            if(confirm("정말 이 매핑을 삭제하시겠습니까?")) {
                document.frm1.mode.value = "deleteColumn";
                document.frm1.deleteColumnSeq.value = insert_column_seq;
                document.frm1.submit();    	
            }
        }
    </script>
</head>
<body class="p-3">

<div class="container-fluid">
    <div class="card p-4">
        
        <!-- Header -->
        <div class="d-flex justify-content-between align-items-center mb-4 border-bottom pb-3">
             <div class="d-flex align-items-center gap-2">
                <span class="badge bg-primary rounded-pill">STEP 4</span>
                <h4 class="fw-bold mb-0 text-dark">Insert Column 매핑</h4>
            </div>
            <button type="button" class="btn btn-close" onclick="self.close();" aria-label="Close"></button>
        </div>

        <form name="frm1" method="post" action="./insert-column-list.jsp">
            <input type="hidden" name="mode" value="${search.mode}" />
            <input type="hidden" name="deleteColumnSeq" value="" />
            <input type="hidden" name="mig_list_seq" value="${search.mig_list_seq}" />

            <!-- Action Buttons -->
            <div class="d-flex justify-content-end mb-3 gap-2">
                <button type="button" class="btn btn-warning" onclick="goRegist();">
                    <i class="bi bi-save me-1"></i> 저장 (등록/수정)
                </button>
                 <button type="button" class="btn btn-secondary px-4" onclick="self.close();">
                    <i class="bi bi-check-lg me-1"></i> 완료
                </button>
            </div>

            <!-- Main Table -->
            <div class="table-responsive border rounded bg-white">
                <table class="table table-hover align-middle mb-0 text-center">
                    <thead>
                        <tr>
                            <th width="10%">Type</th>
                            <th width="15%">Table</th>
                            <th width="15%">Target Column</th>
                            <th width="10%">Type</th>
                            <th width="25%">Source Mapping (Select)</th>
                            <th width="15%">Fixed Value</th>
                            <th width="10%">관리</th>
                        </tr>
                    </thead>
                    <tbody>
                        <c:forEach items="${list}" var="list" >
                        <input type="hidden" name="insert_column_seq" value="${list.insert_column_seq}" />
                        <tr> 
                            <td><span class="badge bg-light text-dark border">${list.insert_type}</span></td>
                            <td class="small fw-bold">${list.insert_table}</td>
                            <td class="fw-bold text-primary">${list.column_name}</td>
                            <td class="small text-muted">${list.column_type}</td>
                            <td>
                                <select name="insert_data" class="form-select form-select-sm">
                                    <option value="">-- 매핑 선택 --</option>
                                    <c:forEach items="${sList}" var="slist">
                                        <option value="${slist.column_name}" <c:if test="${slist.column_name==list.insert_data}">selected</c:if>>
                                            ${slist.column_name} (${slist.column_type})
                                        </option>
                                    </c:forEach>
                                </select>
                            </td>
                            <td><input type="text" class="form-control form-control-sm" name="insert_value" value="${list.insert_value}" placeholder="고정값" /></td>
                            <td>
                                <button type="button" class="btn btn-outline-danger btn-sm" onclick="deleteColumn('${list.insert_column_seq}');" title="삭제">
                                    <i class="bi bi-trash"></i>
                                </button>
                            </td>
                        </tr>
                        </c:forEach>
                        <c:if test="${empty list}">
                             <tr><td colspan="7" class="text-center py-5 text-muted">
                                 매핑된 컬럼 정보가 없습니다. 이전 단계에서 '컬럼 생성'을 진행해주세요.
                             </td></tr>
                        </c:if>
                    </tbody>
                </table>
            </div>

            <!-- Alert / Guide -->
             <div class="alert alert-info mt-4 d-flex align-items-center" role="alert">
                <i class="bi bi-info-circle-fill flex-shrink-0 me-2 fs-5"></i>
                <div>
                     <strong>Tip:</strong> UPDATE 시에는 WHERE 절에 들어갈 변수의 순서까지 고려해야 합니다. <br>
                     특히 PK 컬럼은 반드시 마지막 순서로 오도록 매핑해주세요.
                </div>
            </div>
            
        </form>
    </div>
</div>

<script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/js/bootstrap.bundle.min.js"></script>
</body>
</html>
