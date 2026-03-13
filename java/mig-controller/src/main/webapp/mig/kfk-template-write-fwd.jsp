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
    <title>Kafka Template Registration</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/css/bootstrap.min.css" rel="stylesheet">
    <link href="https://cdn.jsdelivr.net/npm/bootstrap-icons@1.10.5/font/bootstrap-icons.css" rel="stylesheet">
    <link href="https://fonts.googleapis.com/css2?family=Inter:wght@400;500;600&display=swap" rel="stylesheet">
    <style>
        body { font-family: 'Inter', sans-serif; background-color: #f8f9fa; }
        .navbar-brand { font-weight: 600; }
        .card { border-radius: 12px; border: none; box-shadow: 0 2px 10px rgba(0,0,0,0.05); }
        .form-label { font-weight: 500; font-size: 0.9rem; color: #495057; }
    </style>
    <script>
        function checkSubmit() {
            var frm = document.frm1;
            if (frm.template_id.value == "") {
                alert("Template ID를 입력하세요.");
                frm.template_id.focus();
                return false;
            }
            if (frm.template_name.value == "") {
                alert("Template Name을 입력하세요.");
                frm.template_name.focus();
                return false;
            }
            
            if(confirm("저장하시겠습니까?")) {
                frm.action = "kfk-template-proc.jsp";
                frm.submit();
            }
        }
    </script>
</head>
<body>

<nav class="navbar navbar-expand-lg navbar-dark bg-dark mb-4">
    <div class="container-fluid">
        <a class="navbar-brand" href="/mig/migration-master-list.jsp"><i class="bi bi-rocket-takeoff-fill me-2"></i>Migration 2.0</a>
    </div>
</nav>

<div class="container px-4">
    <div class="row mb-4 align-items-center">
        <div class="col-md-6">
            <h4 class="mb-0 fw-bold">Kafka Template ${master.mode == 'update' ? '수정' : '등록'}</h4>
        </div>
        <div class="col-md-6 text-end">
            <button type="button" class="btn btn-secondary me-2" onclick="location.href='kfk-template-list.jsp'"><i class="bi bi-list"></i> 목록</button>
            <button type="button" class="btn btn-primary" onclick="checkSubmit();"><i class="bi bi-save"></i> 저장</button>
        </div>
    </div>

    <form name="frm1" method="post">
        <input type="hidden" name="mode" value="${master.mode}">
        
        <div class="card p-4">
            <div class="row g-3">
                <div class="col-md-6">
                    <label class="form-label">Template ID</label>
                    <input type="text" class="form-control" name="template_id" value="${master.template_id}" ${master.mode == 'update' ? 'readonly bg-light' : ''} placeholder="e.g. SINK_MSSQL_01">
                </div>
                <div class="col-md-6">
                    <label class="form-label">Template Name</label>
                    <input type="text" class="form-control" name="template_name" value="${master.template_name}">
                </div>
                
                <div class="col-md-6">
                    <label class="form-label">Type</label>
                    <select class="form-select" name="template_type">
                        <option value="SOURCE" ${master.template_type == 'SOURCE' ? 'selected' : ''}>SOURCE (Producer)</option>
                        <option value="TARGET" ${master.template_type == 'TARGET' ? 'selected' : ''}>TARGET (Consumer)</option>
                    </select>
                </div>
                <div class="col-md-6">
                    <label class="form-label">사용 여부</label>
                    <select class="form-select" name="use_yn">
                        <option value="Y" ${master.use_yn == 'Y' ? 'selected' : ''}>사용</option>
                        <option value="N" ${master.use_yn == 'N' ? 'selected' : ''}>미사용</option>
                    </select>
                </div>
                
                <div class="col-12">
                    <label class="form-label">Connector Class</label>
                    <input type="text" class="form-control" name="connector_class" value="${master.connector_class}" placeholder="e.g. io.confluent.connect.jdbc.JdbcSinkConnector">
                </div>
                
                <div class="col-12">
                    <label class="form-label">Description</label>
                    <textarea class="form-control" name="description" rows="3">${master.description}</textarea>
                </div>
                
                <div class="col-md-4">
                    <label class="form-label">정렬 순서</label>
                    <input type="number" class="form-control" name="ordering" value="${master.ordering}" step="10">
                </div>
            </div>
        </div>
    </form>
</div>
</body>
</html>
