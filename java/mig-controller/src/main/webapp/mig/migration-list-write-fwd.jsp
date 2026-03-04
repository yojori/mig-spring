<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="jaes" uri="http://www.yojori.com/taglib/jaes" %>
<%@ page import="c.y.mig.model.*" %>
<%@ page import="c.y.mig.util.*" %>
<%@include file="/mig/session-admin-check.jsp"%>
<%
    MigrationList master = (MigrationList) request.getAttribute("master");
    java.util.List<DBConnMaster> dbList = (java.util.List<DBConnMaster>) request.getAttribute("dbList");
%>
<!DOCTYPE html>
<html lang="ko">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>이관 작업 상세 설정</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/css/bootstrap.min.css" rel="stylesheet">
    <link href="https://cdn.jsdelivr.net/npm/bootstrap-icons@1.10.5/font/bootstrap-icons.css" rel="stylesheet">
    <script src="https://code.jquery.com/jquery-3.6.0.min.js"></script>
    <style>
        body { background-color: #f4f7f6; font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif; }
        .card { border: none; border-radius: 15px; box-shadow: 0 10px 30px rgba(0,0,0,0.05); }
        .form-label { font-weight: 600; color: #344767; }
        .btn-primary { background: linear-gradient(310deg, #7928ca, #ff0080); border: none; }
        .bg-gradient-primary { background: linear-gradient(310deg, #7928ca, #ff0080); }
        
        /* Wizard Styles */
        .step-container { display: none; }
        .step-container.active { display: block; }
        .step-header { display: flex; justify-content: space-between; margin-bottom: 30px; position: relative; }
        .step-header::before { content: ""; position: absolute; top: 18px; left: 0; right: 0; height: 2px; background: #e9ecef; z-index: 1; }
        .step-item { position: relative; z-index: 2; text-align: center; flex: 1; }
        .step-circle { width: 36px; height: 36px; border-radius: 50%; background: #fff; border: 2px solid #e9ecef; display: flex; align-items: center; justify-content: center; margin: 0 auto 8px; font-weight: bold; transition: all 0.3s; }
        .step-item.active .step-circle { background: #7928ca; border-color: #7928ca; color: #fff; box-shadow: 0 0 10px rgba(121, 40, 202, 0.5); }
        .step-item.completed .step-circle { background: #2dce89; border-color: #2dce89; color: #fff; }
        .step-label { font-size: 0.8rem; font-weight: 600; color: #adb5bd; transition: all 0.3s; }
        .step-item.active .step-label { color: #344767; }
    </style>
    <script>
        var currentStep = 1;

        function goStep(step) {
            if (step < 1 || step > 3) return;
            
            // Validation before moving to step 2/3
            if (step > currentStep) {
                if (currentStep == 1) {
                    if (!$("input[name='mig_name']").val()) { alert("작업명을 입력해 주세요."); return; }
                    if (!$("select[name='source_db_alias']").val() || !$("select[name='target_db_alias']").val()) { alert("DB를 선택해 주세요."); return; }
                }
            }

            $(".step-container").removeClass("active");
            $("#step-" + step).addClass("active");
            
            $(".step-item").removeClass("active completed");
            for(var i=1; i<=3; i++) {
                if(i < step) $(".step-item:nth-child("+i+")").addClass("completed");
                if(i == step) $(".step-item:nth-child("+i+")").addClass("active");
            }
            
            currentStep = step;
            
            // Toggle buttons
            $("#btn-prev").toggle(step > 1);
            $("#btn-next").toggle(step < 3);
            $("#btn-save").toggle(step == 3);
        }

        function changeMigType() {
            var type = $("#mig_type").val();
            
            // Reset disabled states for shared properties
            $("#hidden_target_table").prop("disabled", false);
            $("#hidden_source_pk").prop("disabled", false);
            $("#insert_type_table").prop("disabled", false);
            
            $("#nt_target_table").prop("disabled", true);
            $("#nt_insert_type").prop("disabled", true);

            if (type == "TABLE" || type == "DDL") {
                $("#table_settings_area").show();
                $("#normal_thread_settings").hide();
                
                if (type == "TABLE") {
                    $("#strategy_area").show();
                } else {
                    $("#strategy_area").hide();
                    $("select[name='target_strategy']").val(""); // Reset to default when hidden
                }
                
                // DDL일 경우 Insert Type 감추기
                if (type == "DDL") {
                    $("#insert_type_table_container").hide();
                } else {
                    $("#insert_type_table_container").show();
                }
                if (type == "DDL") {
                    $("#truncate_label").text("기존 테이블 삭제 (DROP) 여부");
                    $("#sql_label").text("SQL 문장 또는 대상 테이블명");
                    $("#single_sql_area").show();
                    
                    // Disable bulk areas for DDL single edit
                    $("textarea[name='source_pk_area']").prop("disabled", true).addClass("bg-light");
                    $("textarea[name='target_table_area']").prop("disabled", false).removeClass("bg-light");
                } else if (type == "TABLE") {
                    $("#truncate_label").text("기존 데이터 삭제 (TRUNCATE) 여부");
                    $("#sql_label").text("대상 테이블명");
                    $("#single_sql_area").hide(); // Use individual fields for TABLE
                    
                    // Disable PK for TABLE (Source metadata is used)
                    $("textarea[name='source_pk_area']").prop("disabled", true).addClass("bg-light");
                    $("textarea[name='target_table_area']").prop("disabled", false).removeClass("bg-light");
                } else {
                    $("#truncate_label").text("기존 데이터 삭제 (TRUNCATE) 여부");
                    $("#sql_label").text("SQL 문장 또는 대상 테이블명");
                    $("#single_sql_area").show();
                    
                    $("textarea[name='source_pk_area']").prop("disabled", false).removeClass("bg-light");
                    $("textarea[name='target_table_area']").prop("disabled", false).removeClass("bg-light");
                }

                if (type == "NORMAL" || type == "TABLE" || type == "DDL") {
                    $("#thread_options_area").hide();
                } else {
                    $("#thread_options_area").show();
                }
            } else {
                $("#table_settings_area").hide();
                $("#strategy_area").hide();
                $("select[name='target_strategy']").val("");
                $("#sql_label").text("SQL Sentence");
                
                if (type == "NORMAL") {
                    $("#thread_options_area").hide();
                } else {
                    $("#thread_options_area").show();
                }
            }
            
            // 下단부 공통 파라미터 (Target Table, Insert Type, Truncate) 영역 제어
            if (type == "NORMAL" || type == "THREAD" || type == "THREAD_IDX" || type == "DDL") {
                $("#normal_thread_settings").show();
                
                // 공통 활성화
                $("#nt_target_table").prop("disabled", false);
                
                if (type == "DDL") {
                    $("#nt_insert_type_container").hide();
                    $("#nt_insert_type").prop("disabled", true);
                    
                    $("#nt_truncate_container").show();
                    $("#nt_truncate_yn").prop("disabled", false);
                    
                    $("#truncate_table_container").hide();
                    $("#truncate_yn_table").prop("disabled", true);
                } else {
                    $("#nt_insert_type_container").show();
                    $("#nt_insert_type").prop("disabled", false);
                    
                    $("#nt_truncate_container").hide();
                    $("#nt_truncate_yn").prop("disabled", true);
                    
                    // TABLE 타입 등은 상단 truncate 노출 
                    $("#truncate_table_container").show();
                    $("#truncate_yn_table").prop("disabled", false);
                }
                
                // Disable hidden fields
                $("#hidden_target_table").prop("disabled", true);
                if (type != "DDL") {
                    $("#insert_type_table").prop("disabled", true);
                }
            } else {
                $("#normal_thread_settings").hide();
                $("#truncate_table_container").show();
                $("#truncate_yn_table").prop("disabled", false);
            }
        }

        function setBulkMode(isBulk) {
            if (isBulk) {
                $("#single_sql_area").hide();
                $("#bulk_table_area").show();
                $("#bulk_flag").val("Y");
            } else {
                $("#single_sql_area").show();
                $("#bulk_table_area").hide();
                $("#bulk_flag").val("N");
            }
        }

        $(document).ready(function() {
            var type = $("#mig_type").val();
            var mode = "${master.mode}";
            
            changeMigType();
            
            // Initial view setup: Insert + (Table/DDL) = Bulk area
            if (mode == "insert" && (type == "TABLE" || type == "DDL")) {
                setBulkMode(true);
            } else {
                setBulkMode(false);
            }
            
            $("#mig_type").change(function() {
                changeMigType();
                // Switch input area depending on type for NEW registrations
                if (mode == "insert" && ($(this).val() == "TABLE" || $(this).val() == "DDL")) {
                    setBulkMode(true);
                } else {
                    setBulkMode(false);
                }

            // 쓰레드 타입일 경우 멀티 쓰레드 사용 기본 Y
            if ($(this).val() == "THREAD" || $(this).val() == "THREAD_IDX") {
                $("select[name='thread_use_yn']").val("Y");
            }
        });

        // Initialize display switch state
        var displayYn = "${master.display_yn}";
        if (displayYn == "N") {
            $("#display_yn_switch").prop("checked", false);
            $("input[name='display_yn']").val("N");
        } else {
            $("#display_yn_switch").prop("checked", true);
            $("input[name='display_yn']").val("Y");
        }

        // Toggle display_yn hidden value when switch changes
        $("#display_yn_switch").change(function() {
            $("input[name='display_yn']").val($(this).is(":checked") ? "Y" : "N");
        });
    });
    </script>
</head>
<body class="p-4">
    <div class="container">
        <div class="row justify-content-center">
            <div class="col-lg-10">
                <div class="card p-4">
                    <div class="card-header bg-transparent d-flex justify-content-between align-items-center">
                        <div>
                            <h4 class="fw-bold text-dark mb-0"><i class="bi bi-gear-wide-connected me-2 text-primary"></i>이관 작업 등록/수정</h4>
                            <p class="text-muted small mb-0">이관 방식 및 데이터베이스 연결 정보를 설정해 주세요.</p>
                        </div>
                    </div>
                    <div class="card-body">
                        <form id="form1" name="form1" action="migration-list-proc.jsp" method="post">
                            <input type="hidden" name="mig_master" value="${master.mig_master}">
                            <input type="hidden" name="mig_list_seq" value="${master.mig_list_seq}">
                            <input type="hidden" name="mode" value="${master.mode}">
                            <input type="hidden" name="bulk_flag" id="bulk_flag" value="N">

                            <!-- Step Indicator -->
                            <div class="step-header">
                                <div class="step-item active">
                                    <div class="step-circle">1</div>
                                    <div class="step-label">기본 정보</div>
                                </div>
                                <div class="step-item">
                                    <div class="step-circle">2</div>
                                    <div class="step-label">이관 내용</div>
                                </div>
                                <div class="step-item">
                                    <div class="step-circle">3</div>
                                    <div class="step-label">상세 설정</div>
                                </div>
                            </div>

                            <!-- Step 1: Essential Basics -->
                            <div id="step-1" class="step-container active">
                                <div class="row g-3">
                                    <div class="col-md-4">
                                        <label class="form-label">이관 타입</label>
                                        <select name="mig_type" id="mig_type" class="form-select border-primary shadow-sm">
                                            <option value="NORMAL" <c:if test="${master.mig_type eq 'NORMAL'}">selected</c:if>>일반 (SQL 작성)</option>
                                            <option value="TABLE" <c:if test="${master.mig_type eq 'TABLE'}">selected</c:if>>테이블 복사 (TABLE Copy)</option>
                                            <option value="DDL" <c:if test="${master.mig_type eq 'DDL'}">selected</c:if>>테이블 생성 (DDL Create)</option>
                                            <option value="THREAD" <c:if test="${master.mig_type eq 'THREAD'}">selected</c:if>>멀티 스레드 (자동 페이징)</option>
                                            <option value="THREAD_IDX" <c:if test="${master.mig_type eq 'THREAD_IDX'}">selected</c:if>>멀티 스레드 (인덱스 페이징)</option>
                                            <option value="JAVA" <c:if test="${master.mig_type eq 'JAVA'}">selected</c:if>>자바 커스텀 (JAVA Class)</option>
                                        </select>
                                    </div>
                                    <div class="col-md-8">
                                        <label class="form-label">이관 작업명</label>
                                        <input type="text" name="mig_name" class="form-control" value="${master.mig_name}" placeholder="이름을 입력하세요">
                                    </div>
                                    <div class="col-md-6">
                                        <label class="form-label text-primary"><i class="bi bi-database-up me-1"></i>소스 DB (Source DB)</label>
                                        <select name="source_db_alias" class="form-select">
                                            <option value="">- 선택 -</option>
                                            <c:forEach var="db" items="${dbList}">
                                                <option value="${db.master_code}" <c:if test="${db.master_code eq master.source_db_alias}">selected</c:if>>[${db.master_code}] ${db.db_alias} (${db.db_type})</option>
                                            </c:forEach>
                                        </select>
                                    </div>
                                    <div class="col-md-6">
                                        <label class="form-label text-success"><i class="bi bi-database-down me-1"></i>타겟 DB (Target DB)</label>
                                        <select name="target_db_alias" class="form-select">
                                            <option value="">- 선택 -</option>
                                            <c:forEach var="db" items="${dbList}">
                                                <option value="${db.master_code}" <c:if test="${db.master_code eq master.target_db_alias}">selected</c:if>>[${db.master_code}] ${db.db_alias} (${db.db_type})</option>
                                            </c:forEach>
                                        </select>
                                    </div>
                                    <div id="strategy_area" class="col-12 bg-info-subtle p-3 rounded-3 border border-info" style="display:none;">
                                        <label class="form-label text-primary"><i class="bi bi-lightning-fill me-1"></i>최종 실행 전략 선택</label>
                                        <select name="target_strategy" class="form-select border-primary shadow-sm">
                                            <option value="">- 이관 타입 유지 (기본값) -</option>
                                            <option value="NORMAL">NORMAL (일반 Select-Insert)</option>
                                            <option value="THREAD">THREAD (자동 페이징 - 부하 분산)</option>
                                            <option value="THREAD_IDX">THREAD_IDX (인덱스 활용 - 대용량 전용)</option>
                                        </select>
                                        <p class="small text-muted mt-2 mb-0 ms-1">※ 벌크 등록 시 각 테이블에 적용될 기본 실행 방식입니다.</p>
                                    </div>
                                </div>
                            </div>

                            <!-- Step 2: Migration Content -->
                            <div id="step-2" class="step-container">
                                <div class="row g-3">
                                    <div id="table_settings_area" class="col-12 bg-light p-3 rounded-3 border mb-3" style="display:none;">
                                        <c:if test="${master.mode eq 'update'}">
                                            <input type="hidden" name="source_table" value="${master.source_table}">
                                            <input type="hidden" name="target_table" id="hidden_target_table" value="${master.target_table}">
                                            <input type="hidden" name="source_pk" id="hidden_source_pk" value="${master.source_pk}">
                                        </c:if>
                                        <div class="row g-3 mt-1">
                                            <div class="col-md-6" id="truncate_table_container">
                                                <label class="form-label" id="truncate_label">데이터 삭제 여부</label>
                                                <select name="truncate_yn" id="truncate_yn_table" class="form-select border-warning">
                                                    <option value="N" <c:if test="${master.truncate_yn eq 'N'}">selected</c:if>>N (데이터 유지)</option>
                                                    <option value="Y" <c:if test="${master.truncate_yn eq 'Y'}">selected</c:if>>Y (기존 데이터 삭제)</option>
                                                </select>
                                            </div>
                                            <div class="col-md-6" id="insert_type_table_container">
                                                <label class="form-label">저장 방식 (Insert Type)</label>
                                                <jaes:codeselect name="insert_type" id="insert_type_table" group="pageCode.code.code-0007" selected="${master.insert_type}" styleClass="form-select" />
                                            </div>
                                        </div>
                                        </div>
                                    </div>

                                    <div id="single_sql_area" class="col-12">
                                        <label class="form-label" id="sql_label">SQL 문장 또는 대상 정보</label>
                                        <textarea name="sql_string" class="form-control font-monospace" rows="10" placeholder="SELECT * FROM [테이블명] 또는 이관 대상 물리명 하나를 입력하세요.">${master.sql_string}</textarea>
                                        
                                        <!-- NORMAL, THREAD, THREAD_IDX, DDL 설정 -->
                                        <div id="normal_thread_settings" class="row g-3 mt-2" style="display:none;">
                                            <div class="col-md-6" id="nt_truncate_container" style="display:none;">
                                                <label class="form-label text-warning"><i class="bi bi-trash me-1"></i>기존 테이블 삭제 (DROP) 여부</label>
                                                <select name="truncate_yn" id="nt_truncate_yn" class="form-select border-warning shadow-sm">
                                                    <option value="N" <c:if test="${master.truncate_yn eq 'N'}">selected</c:if>>N (데이터/테이블 유지)</option>
                                                    <option value="Y" <c:if test="${master.truncate_yn eq 'Y'}">selected</c:if>>Y (기존 항목 삭제)</option>
                                                </select>
                                            </div>
                                            <div class="col-md-6">
                                                <label class="form-label text-success"><i class="bi bi-table me-1"></i>타겟 테이블명 (Target Table)</label>
                                                <input type="text" name="target_table" id="nt_target_table" class="form-control border-success shadow-sm" value="${master.target_table}" placeholder="타겟 테이블명을 입력하세요">
                                            </div>
                                            <div class="col-md-6" id="nt_insert_type_container">
                                                <label class="form-label text-success"><i class="bi bi-save me-1"></i>저장 방식 (Insert Type)</label>
                                                <jaes:codeselect name="insert_type" id="nt_insert_type" group="pageCode.code.code-0007" selected="${master.insert_type}" styleClass="form-select border-success shadow-sm" />
                                            </div>
                                        </div>
                                    </div>

                                    <div id="bulk_table_area" class="col-12" style="display:none;">
                                        <div class="row g-2">
                                            <div class="col-md-4">
                                                <label class="form-label small fw-bold text-primary">소스 테이블 목록 <span class="badge bg-primary-subtle text-primary">다중 입력</span></label>
                                                <textarea name="source_table_area" class="form-control font-monospace" rows="12" placeholder="이관할 테이블들을 줄바꿈으로 구분해 입력해 주세요.&#10;예)&#10;TABLE_A&#10;TABLE_B"></textarea>
                                            </div>
                                            <div class="col-md-4">
                                                <label class="form-label small fw-bold text-danger">소스 PK 목록 (선택사항)</label>
                                                <textarea name="source_pk_area" class="form-control font-monospace" rows="12" placeholder="미입력 시 데이터베이스 메타데이터에서 자동으로 PK를 조회합니다."></textarea>
                                            </div>
                                            <div class="col-md-4">
                                                <label class="form-label small fw-bold text-success">타겟 테이블 목록 (선택사항)</label>
                                                <textarea name="target_table_area" class="form-control font-monospace" rows="12" placeholder="타겟 테이블명을 다르게 할 경우 입력하세요.&#10;미입력 시 소스 테이블과 동일하게 설정됩니다."></textarea>
                                            </div>
                                        </div>
                                    </div>
                                </div>
                            </div>

                            <!-- Step 3: Advanced Options -->
                            <div id="step-3" class="step-container">
                                <div class="row g-4 bg-light p-4 rounded-4 border">
                                    <!-- Thread Options Area -->
                                    <div id="thread_options_area" class="row g-4 m-0 p-0">
                                        <div class="col-md-4">
                                            <label class="form-label"><i class="bi bi-cpu me-1"></i>멀티 스레드 사용</label>
                                            <select name="thread_use_yn" class="form-select border-info shadow-sm">
                                                <option value="N" <c:if test="${master.thread_use_yn eq 'N'}">selected</c:if>>N (싱글 스레드)</option>
                                                <option value="Y" <c:if test="${master.thread_use_yn eq 'Y'}">selected</c:if>>Y (멀티 스레드 전송)</option>
                                            </select>
                                        </div>
                                        <div class="col-md-4">
                                            <label class="form-label">스레드 수</label>
                                            <input type="number" name="thread_count" class="form-control" value="${master.thread_count}" min="1" max="100">
                                        </div>
                                        <div class="col-md-4">
                                            <label class="form-label">페이지당 건수</label>
                                            <input type="number" name="page_count_per_thread" class="form-control" value="${master.page_count_per_thread}">
                                        </div>
                                    </div>

                                    <div class="col-md-6 border-top pt-3">
                                        <label class="form-label">정렬 순서</label>
                                        <input type="number" name="ordering" class="form-control" value="${master.ordering}">
                                    </div>
                                    <div class="col-md-6 border-top pt-3">
                                        <label class="form-label">배치 실행 여부</label>
                                        <select name="execute_yn" class="form-select">
                                            <option value="Y" <c:if test="${master.execute_yn eq 'Y'}">selected</c:if>>Y (즉시 가능)</option>
                                            <option value="N" <c:if test="${master.execute_yn eq 'N'}">selected</c:if>>N (목록에만 등록)</option>
                                        </select>
                                    </div>
                                    <div class="col-12 mt-3 p-3 bg-white rounded-3 border">
                                        <div class="form-check form-switch">
                                            <label class="form-check-label fw-bold" for="display_yn_switch">목록 노출 (Display)</label>
                                            <input class="form-check-input" type="checkbox" id="display_yn_switch">
                                            <input type="hidden" name="display_yn" value="${master.display_yn}">
                                        </div>
                                        <p class="small text-muted mb-0 mt-1">이관 목록 화면에 해당 작업을 표시할지 여부입니다.</p>
                                    </div>
                                </div>
                            </div>

                            <!-- Navigation Buttons -->
                            <div class="text-center mt-5">
                                <button type="button" id="btn-prev" class="btn btn-outline-secondary btn-lg px-5 me-2" onclick="goStep(currentStep-1);" style="display:none;">
                                    <i class="bi bi-chevron-left me-1"></i>이전
                                </button>
                                <button type="button" id="btn-next" class="btn btn-primary btn-lg px-5 shadow" onclick="goStep(currentStep+1);">
                                    다음 단계<i class="bi bi-chevron-right ms-2"></i>
                                </button>
                                <button type="submit" id="btn-save" class="btn btn-primary btn-lg px-5 shadow" style="display:none;">
                                    <i class="bi bi-check-lg me-2"></i>최종 저장
                                </button>
                            </div>
                        </form>
                    </div>
                </div>
            </div>
        </div>
    </div>
</body>
</html>
