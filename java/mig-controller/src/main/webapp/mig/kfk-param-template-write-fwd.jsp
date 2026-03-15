<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="jaes" uri="http://www.yojori.com/taglib/jaes" %>
<%@include file="/mig/session-admin-check.jsp"%>
<!DOCTYPE html>
<html lang="ko">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>Kafka Param Template ${master.mode == 'update' ? '수정' : '등록'}</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/css/bootstrap.min.css" rel="stylesheet">
    <link href="https://cdn.jsdelivr.net/npm/bootstrap-icons@1.10.5/font/bootstrap-icons.css" rel="stylesheet">
    <link href="https://fonts.googleapis.com/css2?family=Inter:wght@400;500;600&display=swap" rel="stylesheet">
    <style>
        body { font-family: 'Inter', sans-serif; background-color: #f8f9fa; }
        .card { border-radius: 12px; border: none; box-shadow: 0 2px 10px rgba(0,0,0,0.05); }
        .form-label { font-weight: 500; font-size: 0.85rem; color: #495057; }
        .section-title { font-size: 0.95rem; font-weight: 600; color: #212529; border-left: 4px solid #0d6efd; padding-left: 10px; margin-bottom: 20px; }
    </style>
    <script>
        function checkSubmit() {
            var frm = document.frm1;
            if (frm.id.value == "") { alert("ID를 입력하세요."); frm.id.focus(); return false; }
            if (frm.param_name.value == "") { alert("파라미터명을 입력하세요."); frm.param_name.focus(); return false; }
            if (frm.param_key.value == "") { alert("파라미터 키를 입력하세요."); frm.param_key.focus(); return false; }
            
            if(confirm("저장하시겠습니까?")) {
                frm.action = "kfk-param-template-proc.jsp";
                frm.submit();
            }
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
                <li class="nav-item"><a class="nav-link" href="./type-mapping-list.jsp">DB Type 관리</a></li>
                <li class="nav-item"><a class="nav-link active fw-bold text-warning" href="./kfk-param-template-list.jsp"><i class="bi bi-gear-fill me-1"></i>Kafka Param Template 관리</a></li>
            </ul>
        </div>
    </div>
</nav>

<div class="container px-4">
    <div class="row mb-4 align-items-center">
        <div class="col-md-6">
            <h4 class="mb-0 fw-bold">Parameter Template ${master.mode == 'update' ? '수정' : '등록'}</h4>
        </div>
        <div class="col-md-6 text-end">
            <button type="button" class="btn btn-secondary me-2" onclick="location.href='kfk-param-template-list.jsp'"><i class="bi bi-list"></i> 목록</button>
            <button type="button" class="btn btn-primary fw-bold" onclick="checkSubmit();"><i class="bi bi-save"></i> 저장</button>
        </div>
    </div>

    <form name="frm1" method="post">
        <input type="hidden" name="mode" value="${master.mode}">
        
        <div class="card p-4">
            <div class="section-title">기본 정보</div>
            <div class="row g-3">
                <div class="col-md-4">
                    <label class="form-label">ID</label>
                    <input type="text" class="form-control" name="id" value="${master.id}" ${master.mode == 'update' ? 'readonly bg-light' : ''} placeholder="e.g. PT-2507001">
                </div>
                <div class="col-md-4">
                    <label class="form-label">파라미터명</label>
                    <input type="text" class="form-control" name="param_name" value="${master.param_name}" placeholder="화면에 표시될 이름">
                </div>
                <div class="col-md-4">
                    <label class="form-label">파라미터 키 (Param Key)</label>
                    <input type="text" class="form-control" name="param_key" value="${master.param_key}" placeholder="Kafka Config Key">
                </div>
                
                <div class="col-md-12">
                    <label class="form-label">설명 (Explain)</label>
                    <textarea class="form-control" name="param_explain" rows="2">${master.param_explain}</textarea>
                </div>
            </div>

            <div class="section-title mt-4">위저드 설정</div>
            <div class="row g-3">
                <div class="col-md-3">
                    <label class="form-label">위저드 단계 (dp_level)</label>
                    <select class="form-select" name="dp_level">
                        <option value="0" ${master.dp_level == 0 ? 'selected' : ''}>0 Level (기본설정)</option>
                        <option value="1" ${master.dp_level == 1 ? 'selected' : ''}>1 Level (Topic/Conv)</option>
                        <option value="2" ${master.dp_level == 2 ? 'selected' : ''}>2 Level (Mapping)</option>
                        <option value="3" ${master.dp_level == 3 ? 'selected' : ''}>3 Level (Run Config)</option>
                    </select>
                </div>
                <div class="col-md-3">
                    <label class="form-label">정렬 순서 (dp_order)</label>
                    <input type="number" class="form-control" name="dp_order" value="${master.dp_order}" step="10">
                </div>
                <div class="col-md-3">
                    <label class="form-label">입력 방식</label>
                    <select class="form-select" name="input_method">
                        <option value="TEXT" ${master.input_method == 'TEXT' ? 'selected' : ''}>TEXT (입력박스)</option>
                        <option value="COMBO" ${master.input_method == 'COMBO' ? 'selected' : ''}>COMBO (셀렉트)</option>
                        <option value="AUTO" ${master.input_method == 'AUTO' ? 'selected' : ''}>AUTO (자동채움)</option>
                        <option value="VIEWTEXT" ${master.input_method == 'VIEWTEXT' ? 'selected' : ''}>VIEWTEXT (읽기전용)</option>
                    </select>
                </div>
                <div class="col-md-3">
                    <label class="form-label">숨김 여부 (hidden_yn)</label>
                    <select class="form-select" name="hidden_yn">
                        <option value="N" ${master.hidden_yn == 'N' ? 'selected' : ''}>표시함 (N)</option>
                        <option value="Y" ${master.hidden_yn == 'Y' ? 'selected' : ''}>숨김 (Y)</option>
                    </select>
                </div>

                <div class="col-md-4">
                    <label class="form-label">공통코드 그룹 (group_cd)</label>
                    <input type="text" class="form-control" name="group_cd" value="${master.group_cd}" placeholder="콤보박스용 코드">
                </div>
                <div class="col-md-4">
                    <label class="form-label">부모 클래스 ID (par_class_id)</label>
                    <input type="text" class="form-control" name="par_class_id" value="${master.par_class_id}" placeholder="Connector Class">
                </div>
                <div class="col-md-4">
                    <label class="form-label">부모 파라미터 ID (par_param_id)</label>
                    <input type="text" class="form-control" name="par_param_id" value="${master.par_param_id}">
                </div>

                <div class="col-md-4">
                    <label class="form-label">부모 컬럼 Key (par_column_key)</label>
                    <input type="text" class="form-control" name="par_column_key" value="${master.par_column_key}" placeholder="e.g. ec1, ec2">
                </div>
                <div class="col-md-4">
                    <label class="form-label">컬럼 타입</label>
                    <select class="form-select" name="column_type">
                        <option value="string" ${master.column_type == 'string' ? 'selected' : ''}>String</option>
                        <option value="int" ${master.column_type == 'int' ? 'selected' : ''}>Integer</option>
                    </select>
                </div>
                <div class="col-md-2">
                    <label class="form-label">필수 여부</label>
                    <select class="form-select" name="required_yn">
                        <option value="Y" ${master.required_yn == 'Y' ? 'selected' : ''}>필수</option>
                        <option value="N" ${master.required_yn == 'N' ? 'selected' : ''}>선택</option>
                    </select>
                </div>
                <div class="col-md-2">
                    <label class="form-label">기본값</label>
                    <input type="text" class="form-control" name="default_value" value="${master.default_value}">
                </div>
            </div>
        </div>
    </form>
</div>

<script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/js/bootstrap.bundle.min.js"></script>
</body>
</html>
