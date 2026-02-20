
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
    <title>이관 목록 등록</title>
    <!-- Bootstrap 5 CSS -->
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/css/bootstrap.min.css" rel="stylesheet">
    <!-- Bootstrap Icons -->
    <link href="https://cdn.jsdelivr.net/npm/bootstrap-icons@1.10.5/font/bootstrap-icons.css" rel="stylesheet">
    <!-- Google Fonts -->
    <link href="https://fonts.googleapis.com/css2?family=Inter:wght@400;500;600&display=swap" rel="stylesheet">
    
    <style>
        body { font-family: 'Inter', sans-serif; background-color: #f8f9fa; }
        .form-label { font-weight: 500; color: #495057; font-size: 0.9rem; }
        .card { border-radius: 12px; border: none; box-shadow: 0 4px 15px rgba(0,0,0,0.05); }
        .input-group-text { background-color: #e9ecef; }
    </style>
    
    <script>
        function changeType() {
            // Logic handled by backend/original script, keeping placeholder or reimplementing if needed.
            // Original logic was commented out in the source, so I will leave it as is or clean up.
            // If the user wants specific dynamic behavior, we can uncomment and adapt to Bootstrap.
            /*
            var mig_type = document.querySelector("select[name='mig_type']").value;
            // Add logic here if needed based on original requirements
            */
        }
    </script>
</head>
<body onload="changeType();" class="p-3">

<div class="container-fluid">
    <div class="card p-4">
        
        <div class="d-flex justify-content-between align-items-center mb-4 border-bottom pb-3">
            <h4 class="fw-bold mb-0 text-primary"><i class="bi bi-file-earmark-plus me-2"></i>이관 목록 등록/수정</h4>
            <span class="badge bg-secondary">Pop-up</span>
        </div>

        <form name="frm1" method="post" action="./migration-list-proc.jsp" class="needs-validation" novalidate>
            <input type="hidden" name="mode" value="${master.mode}" />
            <input type="hidden" name="mig_master" value="${master.mig_master}" />
            <input type="hidden" name="mig_list_seq" value="${master.mig_list_seq}" />

                <!-- Step 1: Basic Configuration -->
                <div id="step-1">
                    <div class="row g-3">
                        <div class="col-12">
                             <label for="mig_name" class="form-label">이관명 (Migration Name)</label>
                             <div class="input-group">
                                <span class="input-group-text"><i class="bi bi-tag"></i></span>
                                <input type="text" class="form-control" name="mig_name" id="mig_name" value="${master.mig_name}" required placeholder="작업 이름을 입력하세요">
                             </div>
                        </div>

                        <div class="col-md-6">
                            <label class="form-label">이관 유형</label>
                            <jaes:codeselect name="mig_type" id="mig_type" group="pageCode.code.code-0002" selected="${master.mig_type}" onchange="changeType();" styleClass="form-select" />
                        </div>
                        <div class="col-md-6">
                            <label class="form-label">정렬 순서</label>
                            <input type="number" class="form-control" name="ordering" value="${master.ordering}" placeholder="0">
                        </div>

                        <div class="col-md-6">
                            <label class="form-label d-block">실행 여부</label>
                            <div class="border rounded p-2 bg-light">
                                <jaes:coderadios name="execute_yn" id="execute_yn" group="pageCode.code.code-0004" checked="${master.execute_yn}" type="button" />
                            </div>
                        </div>
                        <div class="col-md-6">
                            <label class="form-label d-block">표시 여부</label>
                            <div class="border rounded p-2 bg-light">
                                <jaes:coderadios name="display_yn" id="display_yn" group="pageCode.code.code-0009" checked="${master.display_yn}" type="button" />
                            </div>
                        </div>
                    </div>
                    
                    <div class="mt-4 d-flex justify-content-end gap-2 border-top pt-3">
                        <button type="button" class="btn btn-secondary px-4" onclick="self.close();">닫기</button>
                        <button type="button" class="btn btn-primary px-4 fw-bold" onclick="nextStep();">
                            다음 <i class="bi bi-arrow-right"></i>
                        </button>
                    </div>
                </div>

                <!-- Step 2: Detail Configuration -->
                <div id="step-2" style="display:none;">
                    <div class="row g-3">
                        
                        <!-- Thread Settings (Visible only for Thread Types) -->
                        <div id="thread_settings" class="col-12 row g-3">
                            <div class="col-md-6">
                                <label class="form-label d-block">Thread 사용 여부</label>
                                <div class="border rounded p-2 bg-light">
                                    <jaes:coderadios name="thread_use_yn" id="thread_use_yn" group="pageCode.code.code-0001" checked="${master.thread_use_yn}" type="button" styleClass="btn-outline-secondary" />
                                </div>
                            </div>
                            <div class="col-md-6">
                                <label class="form-label d-block">Thread 개수</label>
                                 <div class="border rounded p-2 bg-light">
                                    <jaes:coderadios name="thread_count" id="thread_count" group="pageCode.code.code-0005" checked="${master.thread_count}" defaultValue="5" type="button" styleClass="btn-outline-secondary" />
                                 </div>
                            </div>
                            <div class="col-12">
                                <label class="form-label d-block">Page Count / Thread</label>
                                <div class="border rounded p-2 bg-light">
                                    <jaes:coderadios name="page_count_per_thread" id="page_count_per_thread" group="pageCode.code.code-0006" checked="${master.page_count_per_thread}" defaultValue="3000" type="button" styleClass="btn-outline-secondary" />
                                </div>
                            </div>
                        </div>

                        <!-- DB Alias -->
                        <div class="col-md-6">
                            <label class="form-label">Source DB Alias</label>
                            <div class="input-group">
                                <span class="input-group-text text-primary"><i class="bi bi-database-up"></i></span>
                                <select class="form-select" name="source_db_alias">
                                    <option value="">선택하세요</option>
                                    <c:forEach var="db" items="${dbList}">
                                        <option value="${db.master_code}" <c:if test="${master.source_db_alias eq db.master_code}">selected</c:if>>
                                            ${db.db_alias} (${db.master_code})
                                        </option>
                                    </c:forEach>
                                </select>
                            </div>
                        </div>
                         <div class="col-md-6">
                            <label class="form-label">Target DB Alias</label>
                            <div class="input-group">
                                <span class="input-group-text text-success"><i class="bi bi-database-down"></i></span>
                                <select class="form-select" name="target_db_alias">
                                    <option value="">선택하세요</option>
                                    <c:forEach var="db" items="${dbList}">
                                        <option value="${db.master_code}" <c:if test="${master.target_db_alias eq db.master_code}">selected</c:if>>
                                            ${db.db_alias} (${db.master_code})
                                        </option>
                                    </c:forEach>
                                </select>
                            </div>
                        </div>

                        <!-- SQL Editor -->
                        <div id="sql_settings" class="col-12">
                            <label class="form-label">Select SQL <small class="text-muted">(Source 데이터를 조회할 쿼리)</small></label>
                            <textarea class="form-control font-monospace" name="sql_string" rows="6" style="font-size: 0.85rem; background:#fdfdfd; border-color:#dcdcdc;">${master.sql_string}</textarea>
                        </div>

                        <!-- Table Settings (Visible only for TABLE Type) -->
                        <div id="table_settings" class="col-12" style="display:none;">
                            <div class="alert alert-info small py-2 mb-2">
                                <i class="bi bi-info-circle-fill me-1"></i> 여러 줄 입력 시 엔터(Enter)로 구분하여 일괄 등록됩니다.
                            </div>
                            <div class="row g-2">
                                <div class="col-md-4">
                                     <label class="form-label small fw-bold" id="lbl_source_table">Source Tables</label>
                                     <textarea class="form-control font-monospace" name="source_table_area" rows="8" placeholder="SOURCE_TABLE_A&#10;SOURCE_TABLE_B" style="font-size:0.85rem; background:#fdfdfd;"></textarea>
                                </div>
                                <div class="col-md-4">
                                     <label class="form-label small fw-bold" id="lbl_source_pk">Source PK (Order By)</label>
                                     <textarea class="form-control font-monospace" name="source_pk_area" rows="8" placeholder="ID&#10;CODE, REG_DT" style="font-size:0.85rem; background:#fdfdfd;"></textarea>
                                </div>
                                <div class="col-md-4">
                                     <label class="form-label small fw-bold" id="lbl_target_table">Target Tables</label>
                                     <textarea class="form-control font-monospace" name="target_table_area" rows="8" placeholder="TARGET_TABLE_A&#10;TARGET_TABLE_B" style="font-size:0.85rem; background:#fdfdfd;"></textarea>
                                </div>
                                <div class="col-12 mt-2">
                                    <div class="form-check">
                                        <input class="form-check-input" type="checkbox" name="truncate_yn_area" value="Y" id="truncate_yn_chk">
                                        <label class="form-check-label small" for="truncate_yn_chk">Target Table Truncate (이관 전 데이터 삭제)</label>
                                    </div>
                                </div>
                            </div>
                        </div>
                    </div>

                    <div class="mt-4 d-flex justify-content-end gap-2 border-top pt-3">
                        <button type="button" class="btn btn-outline-secondary px-4" onclick="prevStep();">
                            <i class="bi bi-arrow-left"></i> 이전
                        </button>
                        <button type="button" class="btn btn-success px-4 fw-bold" onclick="document.frm1.submit();">
                            <i class="bi bi-check-circle me-1"></i> 저장
                        </button>
                    </div>
                </div>
            </form>
        </div>
    </div>
    
    <iframe width=0 height=0 name='hiddenframe' style='display:none;'></iframe>
    <script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/js/bootstrap.bundle.min.js"></script>
    <script>
        function nextStep() {
            // Validation
            var name = document.getElementById("mig_name");
            if(!name.value.trim()) {
                alert("이관명을 입력하세요.");
                name.focus();
                return;
            }
            
            // Logic to show/hide sections based on type
            var type = document.getElementById("mig_type").value;
            
            // 1. Thread Settings Logic
            var threadSection = document.getElementById("thread_settings");
            if (type.indexOf("THREAD") > -1 || type === "JAVA") { 
                threadSection.style.display = "flex";
            } else {
                threadSection.style.display = "none";
            }
            
            // 2. SQL vs Table Settings Logic
            var sqlSection = document.getElementById("sql_settings");
            var tableSection = document.getElementById("table_settings");
            
            if (type === "TABLE") {
                sqlSection.style.display = "none";
                tableSection.style.display = "block";
            } else if (type === "JAVA") {
                sqlSection.style.display = "block";
                tableSection.style.display = "none";
            } else {
                sqlSection.style.display = "block";
                tableSection.style.display = "none";
            }
            
            document.getElementById("step-1").style.display = "none";
            document.getElementById("step-2").style.display = "block";
            
            changeType();
        }

        function prevStep() {
            document.getElementById("step-2").style.display = "none";
            document.getElementById("step-1").style.display = "block";
        }
        
        function changeType() {
             var type = document.getElementById("mig_type").value;
             var lblSourceTable = document.getElementById("lbl_source_table");
             var lblSourcePk = document.getElementById("lbl_source_pk");
             var lblTargetTable = document.getElementById("lbl_target_table");

             if (type === "JAVA") {
                 if(lblSourceTable) lblSourceTable.innerHTML = "Class Name <small class='text-muted fw-normal'>(패키지 포함 전체 경로)</small>";
                 if(lblSourcePk) lblSourcePk.innerHTML = "Method Name";
                 if(lblTargetTable) lblTargetTable.innerHTML = "Description / Target Path";
             } else {
                 if(lblSourceTable) lblSourceTable.innerHTML = "Source Tables";
                 if(lblSourcePk) lblSourcePk.innerHTML = "Source PK (Order By)";
                 if(lblTargetTable) lblTargetTable.innerHTML = "Target Tables";
             }
        }
    </script>
</body>
</html>

