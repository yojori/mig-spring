<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="jaes" uri="http://www.yojori.com/taglib/jaes" %>
<%@include file="/mig/session-admin-check.jsp"%>
<!DOCTYPE html>
<html lang="ko">
<head>
    <meta charset="UTF-8">
    <title>타입 매핑 등록/수정</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/css/bootstrap.min.css" rel="stylesheet">
    <style>
        body { background-color: #f1f3f5; padding: 20px; }
        .card { border-radius: 12px; border: none; box-shadow: 0 4px 15px rgba(0,0,0,0.1); }
        .form-label { font-weight: 600; font-size: 0.9rem; color: #495057; }
    </style>
    <script>
        function save() {
            var f = document.frm;
            if(!f.src_type_name.value) { alert("소스 타입명을 입력하세요."); f.src_type_name.focus(); return; }
            if(!f.tgt_type_name.value) { alert("타겟 타입명을 입력하세요."); f.tgt_type_name.focus(); return; }
            f.action = "type-mapping-proc.jsp";
            f.submit();
        }
    </script>
</head>
<body>
<div class="container">
    <div class="card p-4">
        <h5 class="fw-bold mb-4"><i class="bi bi-pencil-square me-2"></i>타입 매핑 ${mode == 'update' ? '수정' : '등록'}</h5>
        <form name="frm" method="post">
            <input type="hidden" name="mode" value="${mode}">
            <input type="hidden" name="mapping_seq" value="${entry.mapping_seq}">
            
            <div class="row g-3">
                <div class="col-md-6">
                    <label class="form-label">Source DB 유형</label>
                    <select name="src_db_type" class="form-select">
                        <option value="oracle" ${entry.src_db_type == 'oracle' ? 'selected' : ''}>Oracle</option>
                        <option value="postgresql" ${entry.src_db_type == 'postgresql' ? 'selected' : ''}>PostgreSQL</option>
                        <option value="maria" ${entry.src_db_type == 'maria' ? 'selected' : ''}>MariaDB/MySQL</option>
                    </select>
                </div>
                <div class="col-md-6">
                    <label class="form-label">소스 데이터 타입 (예: VARCHAR2)</label>
                    <input type="text" name="src_type_name" class="form-control" value="${entry.src_type_name}" placeholder="대문자 권장">
                </div>
                
                <div class="col-md-6">
                    <label class="form-label">Target DB 유형</label>
                    <select name="tgt_db_type" class="form-select">
                        <option value="postgresql" ${entry.tgt_db_type == 'postgresql' ? 'selected' : ''}>PostgreSQL</option>
                        <option value="maria" ${entry.tgt_db_type == 'maria' ? 'selected' : ''}>MariaDB/MySQL</option>
                    </select>
                </div>
                <div class="col-md-6">
                    <label class="form-label">타겟 데이터 타입 (예: VARCHAR)</label>
                    <input type="text" name="tgt_type_name" class="form-control" value="${entry.tgt_type_name}">
                </div>
                
                <div class="col-md-6">
                    <label class="form-label">우선순위 (낮을수록 먼저 적용)</label>
                    <input type="number" name="priority" class="form-control" value="${entry.priority}">
                </div>
                <div class="col-md-6">
                    <label class="form-label">사용 여부</label>
                    <select name="use_yn" class="form-select">
                        <option value="Y" ${entry.use_yn == 'Y' ? 'selected' : ''}>사용</option>
                        <option value="N" ${entry.use_yn == 'N' ? 'selected' : ''}>미사용</option>
                    </select>
                </div>
            </div>
            
            <div class="mt-5 text-center">
                <button type="button" class="btn btn-secondary px-4 me-2" onclick="window.close();">취소</button>
                <button type="button" class="btn btn-primary px-4" onclick="save();">저장</button>
            </div>
        </form>
    </div>
</div>
</body>
</html>
