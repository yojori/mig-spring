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
    <title>Insert Table 등록</title>
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
        
        function goAutocolumn(insert_sql_seq, insert_table) {
            document.frm1.mode.value = "autoInsert";
            document.frm1.auto_insert_sql_seq.value = insert_sql_seq;
            document.frm1.auto_insert_table.value = insert_table;
            document.frm1.submit();
        }
        
        function goDelete(insert_sql_seq, insert_table) {
            if(confirm("정말 삭제하시겠습니까?")) {       
                document.frm1.mode.value = "goDelete";
                document.frm1.auto_insert_sql_seq.value = insert_sql_seq;
                document.frm1.auto_insert_table.value = insert_table;
                document.frm1.submit();
            }       
        }
        
        function goNext() {
            location.href = '/mig/insert-column-list.jsp?mig_list_seq=${search.mig_list_seq}';
        }
        
        function confirmNext() {
            if('${search.mode}' == 'autoInsert') {
                goNext();   
            }
        }    
        function goSaveAndClose() {
            document.frm1.mode.value = "insertAll";
            document.frm1.auto_close.value = "Y";
            document.frm1.submit();
        }

        function checkAutoClose() {
            if('${param.auto_close}' == 'Y') {
                opener.location.reload();
                self.close();
            }
        }

        function updateStep3Labels() {
        // Updated logic for Step 3 labels
        // For TABLE/SQL type: Insert Configuration (Table/Column)
        // For JAVA type: Partitioning Source (Optional) (Table/PK)
        var migType = "${mig_type}";
        if(migType === "JAVA") {
             $("#step3_title").text("Partitioning Source (Optional)");
             // Update grid headers if possible, or just the section title
             // Grid headers are in the i-grid-column, hard to change via JS easily without grid API
             // But we can add a helper text
             $("#step3_desc").text("Configure Source Table and PK Column for Thread Partitioning.");
        } else {
             $("#step3_title").text("Insert Configuration (Table/Class)");
             $("#step3_desc").text("Configure Target Table and Column mapping.");
        }
    }
    </script>
</head>
<body onload="confirmNext(); checkAutoClose();" class="p-3">

<div class="container-fluid">
    <div class="card p-4">
        
        <!-- Header -->
        <div class="d-flex justify-content-between align-items-center mb-4 border-bottom pb-3">
             <div class="d-flex align-items-center gap-2">
                <span class="badge bg-primary rounded-pill">STEP 3</span>
                <c:choose>
                    <c:when test="${migList.mig_type eq 'JAVA'}">
                        <h4 class="fw-bold mb-0 text-dark">Insert Configuration (Class/Method)</h4>
                    </c:when>
                    <c:otherwise>
                        <h5 class="card-title" id="step3_title">Insert Configuration (Table/Class)</h5>
    <span id="step3_desc" class="text-muted small"></span>
                    </c:otherwise>
                </c:choose>
            </div>
            <button type="button" class="btn btn-close" onclick="self.close();" aria-label="Close"></button>
        </div>

        <form name="frm1" method="post" action="./insert-sql-list.jsp">
            <input type="hidden" name="mode" value="${search.mode}" />
            <input type="hidden" name="mig_list_seq" value="${search.mig_list_seq}" />
            <input type="hidden" name="auto_insert_sql_seq" value="" />
            <input type="hidden" name="auto_insert_table" value="" />
            <input type="hidden" name="auto_close" value="" />

            <!-- Action Buttons -->
            <div class="d-flex justify-content-end mb-3 gap-2">
                <button type="button" class="btn btn-warning" onclick="goRegist();">
                    <i class="bi bi-save me-1"></i> 저장 (등록/수정)
                </button>
                <c:choose>
                    <c:when test="${migList.mig_type eq 'JAVA'}">
                        <button type="button" class="btn btn-primary fw-bold" onclick="goSaveAndClose();">
                            <i class="bi bi-check-lg me-1"></i> 수정 완료
                        </button>
                    </c:when>
                    <c:otherwise>
                         <button type="button" class="btn btn-primary fw-bold" onclick="goNext();">
                            다음 단계 <i class="bi bi-chevron-right ms-1"></i>
                        </button>
                    </c:otherwise>
                </c:choose>
            </div>

            <!-- Main Table -->
            <div class="table-responsive border rounded bg-white">
                <table class="table table-hover align-middle mb-0 text-center">
                    <thead>
                        <tr>
                            <th width="12%">Insert Type</th>
                            <c:choose>
                                <c:when test="${migList.mig_type eq 'JAVA'}">
                                    <th width="20%">Class.Method</th>
                                    <th width="15%">Partitioning PK</th>
                                </c:when>
                                <c:otherwise>
                                    <th width="20%">Table Name</th>
                                    <th width="15%">PK Column</th>
                                </c:otherwise>
                            </c:choose>
                            <th width="10%">Truncate</th>
                            <th width="15%">등록일</th>
                            <th width="28%">관리</th>
                        </tr>
                    </thead>
                    <tbody>
                        <%-- Existing Data List --%>
                        <c:forEach items="${list}" var="list" >
                        <input type="hidden" name="insert_sql_seq" value="${list.insert_sql_seq}" />
                        <tr> 
                            <td><jaes:codeselect name="insert_type" id="insert_type" group="pageCode.code.code-0007" selected="${list.insert_type}" /></td>
                            <td><input type="text" class="form-control form-control-sm" name="insert_table" value="${list.insert_table}" placeholder="${migList.mig_type eq 'JAVA' ? 'Class.Method' : 'Table Name'}" /></td>
                            <td><input type="text" class="form-control form-control-sm" name="pk_column" value="${list.pk_column}" placeholder="${migList.mig_type eq 'JAVA' ? 'Partition Key' : 'PK Column'}" /></td>
                            <td>
                                <div class="form-check d-flex justify-content-center">
                                    <input type="hidden" name="truncate_yn" value="${list.truncate_yn == 'Y' ? 'Y' : 'N'}">
                                    <input class="form-check-input" type="checkbox" <c:if test="${list.truncate_yn == 'Y'}"> checked</c:if> onclick="this.parentNode.getElementsByTagName('input')[0].value = this.checked ? 'Y' : 'N';">
                                </div>
                            </td>
                            <td class="small text-muted">${list.create_date}</td>
                            <td>
                                <div class="btn-group btn-group-sm">
                                    <c:if test="${migList.mig_type ne 'JAVA'}">
                                    <button type="button" class="btn btn-outline-primary" onclick="goAutocolumn('${list.insert_sql_seq}','${list.insert_table}');">
                                        <i class="bi bi-magic"></i> 컬럼생성
                                    </button>
                                    </c:if>
                                    <button type="button" class="btn btn-outline-danger" onclick="goDelete('${list.insert_sql_seq}','${list.insert_table}');">
                                        <i class="bi bi-trash"></i> 삭제
                                    </button>
                                </div>
                            </td>
                        </tr>
                        </c:forEach>
                        
                        <%-- Empty Inputs for New Registration (5 rows) --%>
                        <c:forEach begin="1" end="5" var="cnt" varStatus="status" >
                        <tr> 
                            <input type="hidden" name="insert_sql_seq" value="" />
                            <td><jaes:codeselect name="insert_type" id="insert_type" group="pageCode.code.code-0007" /></td>
                            <td><input type="text" class="form-control form-control-sm" name="insert_table" value="" placeholder="${migList.mig_type eq 'JAVA' ? 'Class.Method' : 'Table Name'}" /></td>
                            <td><input type="text" class="form-control form-control-sm" name="pk_column" value="" placeholder="${migList.mig_type eq 'JAVA' ? 'Partition Key' : 'PK Column'}" /></td>
                            <td>
                                <div class="form-check d-flex justify-content-center">
                                    <input type="hidden" name="truncate_yn" value="N">
                                    <input class="form-check-input" type="checkbox" onclick="this.parentNode.getElementsByTagName('input')[0].value = this.checked ? 'Y' : 'N';">
                                </div>
                            </td>
                            <td class="small text-muted">-</td>
                            <td><span class="badge bg-light text-secondary">신규 등록</span></td>
                        </tr>
                        </c:forEach>
                    </tbody>
                </table>
            </div>
            
        </form>
    </div>
</div>

<script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/js/bootstrap.bundle.min.js"></script>
</body>
</html>
