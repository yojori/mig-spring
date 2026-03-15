<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@include file="/mig/session-admin-check.jsp"%>
<!DOCTYPE html>
<html lang="ko">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>Kafka 이관 목록</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/css/bootstrap.min.css" rel="stylesheet">
    <link href="https://cdn.jsdelivr.net/npm/bootstrap-icons@1.10.5/font/bootstrap-icons.css" rel="stylesheet">
    <link href="https://fonts.googleapis.com/css2?family=Inter:wght@400;500;600&display=swap" rel="stylesheet">
    
    <style>
        body { font-family: 'Inter', sans-serif; background-color: #f8f9fa; }
        .navbar-brand { font-weight: 600; }
        .card { border-radius: 12px; border: none; box-shadow: 0 2px 10px rgba(0,0,0,0.05); }
        .table thead th { background-color: #e9ecef; font-weight: 600; text-transform: none; font-size: 0.9rem; color: #495057; border-bottom: 2px solid #dee2e6; }
        .table tbody td { border-bottom: 1px solid #eee; }
        .btn-action { padding: 0.25rem 0.5rem; }
        .description-box { background: white; border: 1px solid #ced4da; padding: 15px; font-size: 0.9rem; border-radius: 4px; }
        .header-info { font-size: 1.25rem; }
        .header-label { color: #6c757d; margin-right: 10px; }
        .cursor-pointer { cursor: pointer; }
        .status-actions i:hover { background-color: #f0f0f0; transform: scale(1.1); transition: all 0.2s; }
        .status-text { font-size: 0.85rem; }
    </style>
</head>
<body>

<nav class="navbar navbar-expand-lg navbar-dark bg-dark mb-4">
    <div class="container-fluid">
        <a class="navbar-brand" href="/mig/migration-master-list.jsp"><i class="bi bi-rocket-takeoff-fill me-2"></i>Migration 2.0</a>
        <div class="collapse navbar-collapse">
            <ul class="navbar-nav ms-auto">
                <li class="nav-item"><a class="nav-link" href="./migration-master-list.jsp">Data 이관 Master</a></li>
                <li class="nav-item"><a class="nav-link" href="./migration-work-list.jsp">이관 진행 현황</a></li>
                <li class="nav-item"><a class="nav-link active fw-bold text-warning" href="./kfk-mig-list.jsp">KAFKA 목록</a></li>
                <li class="nav-item"><a class="nav-link" href="./db-con-list.jsp">DB Connection 관리</a></li>
            </ul>
        </div>
    </div>
</nav>

<div class="container px-4">
    <div class="d-flex justify-content-between align-items-center mb-3">
        <h2 class="fw-bold m-0">Kafka 이관</h2>
    </div>


    <div class="mb-3">
        <div class="btn-group shadow-sm" role="group">
            <button type="button" class="btn btn-primary" onclick="doBulkAction('start')" title="Bulk Start"><i class="bi bi-play-fill"></i></button>
            <button type="button" class="btn btn-primary" onclick="doBulkAction('pause')" title="Bulk Pause"><i class="bi bi-pause-fill"></i></button>
            <button type="button" class="btn btn-primary" onclick="doBulkAction('stop')" title="Bulk Stop"><i class="bi bi-stop-fill"></i></button>
        </div>
    </div>

    <div class="card p-0 overflow-hidden mb-5">
        <table class="table table-bordered align-middle mb-0 text-center">
            <thead>
                <tr class="table-light">
                    <th rowspan="2" style="width: 50px;"><input type="checkbox" class="form-check-input"></th>
                    <th>이관코드</th>
                    <th>이관명</th>
                    <th>connector</th>
                    <th>source db alias</th>
                    <th>sink db alias</th>
                </tr>
                <tr class="table-light">
                    <th>source/sink 구분</th>
                    <th>connector 생성여부</th>
                    <th>connector 실행여부</th>
                    <th colspan="2">상태 및 제어</th>
                </tr>
            </thead>
            <tbody>
                <c:forEach items="${list}" var="item">
                    <tr id="row1_${item.mig_list_seq}" class="mig-row">
                        <td rowspan="2"><input type="checkbox" name="chk_seq" value="${item.mig_list_seq}" class="form-check-input"></td>
                        <td class="bg-white">${item.mig_list_seq}</td>
                        <td class="text-start ps-3 bg-white">
                            <a href="javascript:openWizard('${item.mig_master}', '${item.mig_list_seq}')" class="text-decoration-none fw-bold">
                                ${item.mig_name}
                            </a>
                        </td>
                        <td class="bg-white small">${item.source_connector}<br>${item.sink_connector}</td>
                        <td class="bg-white">${item.source_db_alias}</td>
                        <td class="bg-white">${item.target_db_alias}</td>
                    </tr>
                    <tr id="row2_${item.mig_list_seq}">
                        <td class="bg-light small fw-bold">${item.registration_type}</td>
                        <td class="bg-light status-created" data-seq="${item.mig_list_seq}">-</td>
                        <td class="bg-light status-running" data-seq="${item.mig_list_seq}">-</td>
                        <td class="bg-light text-start ps-3" colspan="2">
                            <div class="d-flex align-items-center">
                                <div class="status-actions me-2" data-seq="${item.mig_list_seq}">
                                    <!-- Icons will be injected here by JS -->
                                </div>
                                <span class="status-text fw-bold" data-seq="${item.mig_list_seq}">Loading...</span>
                            </div>
                        </td>
                    </tr>
                </c:forEach>
                <c:if test="${empty list}">
                    <tr><td colspan="6" class="py-4 text-muted">등록된 이관 항목이 없습니다.</td></tr>
                </c:if>
            </tbody>
        </table>
    </div>
</div>

<script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/js/bootstrap.bundle.min.js"></script>
<script src="https://code.jquery.com/jquery-3.6.0.min.js"></script>
<script>
$(document).ready(function() {
    loadAllStatuses();
});

function loadAllStatuses() {
    $('.mig-row').each(function() {
        var seq = $(this).attr('id').replace('row1_', '');
        updateStatus(seq);
    });
}

function updateStatus(seq) {
    $.ajax({
        url: 'kfk-mig-list-proc.jsp',
        data: { mode: 'status', mig_list_seq: seq },
        dataType: 'json',
        success: function(res) {
            var actions = $('.status-actions[data-seq="' + seq + '"]');
            var statusEl = $('.status-text[data-seq="' + seq + '"]');
            actions.empty();

            if (res.success && res.status) {
                var s = res.status.source || res.status.sink || 'NOT_FOUND';
                var isCreated = (s !== 'NOT_FOUND' && s !== 'UNKNOWN');
                var isRunning = (s === 'RUNNING');
                
                $('.status-created[data-seq="' + seq + '"]').text(isCreated ? 'Y' : 'N');
                $('.status-running[data-seq="' + seq + '"]').text(isRunning ? 'Y' : 'N');
                
                var actionHtml = '';
                var statusText = s;

                if (s === 'NOT_FOUND' || s === 'UNKNOWN') {
                    actionHtml = '<i class="bi bi-play-fill text-primary border rounded px-1 cursor-pointer me-1" onclick="doAction(\'' + seq + '\', \'start\')" title="Start"></i>';
                    statusText = (s === 'NOT_FOUND') ? 'Ready' : 'Unknown';
                    statusEl.removeClass('text-success text-danger text-warning').addClass('text-muted');
                } else if (s === 'RUNNING') {
                    actionHtml = '<i class="bi bi-pause-fill text-warning border rounded px-1 cursor-pointer me-1" onclick="doAction(\'' + seq + '\', \'pause\')" title="Pause"></i>';
                    actionHtml += '<i class="bi bi-stop-fill text-danger border rounded px-1 cursor-pointer me-1" onclick="doAction(\'' + seq + '\', \'stop\')" title="Stop"></i>';
                    statusEl.removeClass('text-muted text-danger text-warning').addClass('text-success');
                } else if (s === 'PAUSED') {
                    actionHtml = '<i class="bi bi-play-fill text-primary border rounded px-1 cursor-pointer me-1" onclick="doAction(\'' + seq + '\', \'resume\')" title="Resume"></i>';
                    actionHtml += '<i class="bi bi-stop-fill text-danger border rounded px-1 cursor-pointer me-1" onclick="doAction(\'' + seq + '\', \'stop\')" title="Stop"></i>';
                    statusEl.removeClass('text-muted text-success text-danger').addClass('text-warning');
                } else {
                    // Other states (FAILED, etc.)
                    actionHtml = '<i class="bi bi-play-fill text-primary border rounded px-1 cursor-pointer me-1" onclick="doAction(\'' + seq + '\', \'start\')" title="Restart"></i>';
                    actionHtml += '<i class="bi bi-stop-fill text-danger border rounded px-1 cursor-pointer me-1" onclick="doAction(\'' + seq + '\', \'stop\')" title="Stop"></i>';
                    statusEl.removeClass('text-muted text-success text-warning').addClass('text-danger');
                }
                
                actions.html(actionHtml);
                statusEl.text(statusText);
            } else {
                statusEl.text('Error').addClass('text-danger');
                actions.html('<i class="bi bi-play-fill text-primary border rounded px-1 cursor-pointer me-1" onclick="doAction(\'' + seq + '\', \'start\')" title="Retry"></i>');
            }
        },
        error: function() {
            var actions = $('.status-actions[data-seq="' + seq + '"]');
            var statusEl = $('.status-text[data-seq="' + seq + '"]');
            actions.empty().html('<i class="bi bi-play-fill text-primary border rounded px-1 cursor-pointer me-1" onclick="doAction(\'' + seq + '\', \'start\')" title="Retry"></i>');
            statusEl.text('Failed').addClass('text-danger');
        }
    });
}

function openWizard(master, seq) {
    var url = "kfk-mig-wizard.jsp?mig_master=" + master + "&mig_list_seq=" + seq + "&mode=update";
    window.open(url, 'kfkWizard', 'width=1200,height=800,scrollbars=yes,resizable=yes');
}

function doAction(seq, mode, quiet) {
    if (!quiet && !confirm('정말 진행하시겠습니까?')) return;
    
    $.ajax({
        url: 'kfk-mig-list-proc.jsp',
        data: { mode: mode, mig_list_seq: seq },
        dataType: 'json',
        success: function(res) {
            if (res.success) {
                if (!quiet) alert('처리되었습니다.');
                updateStatus(seq);
            } else {
                alert('오류 발생 (' + seq + '): ' + res.message);
            }
        }
    });
}

function doBulkAction(mode) {
    var checked = $('input[name="chk_seq"]:checked');
    if (checked.length === 0) {
        alert('선택된 항목이 없습니다.');
        return;
    }
    
    if (!confirm(checked.length + '건에 대해 [' + mode + '] 처리를 진행하시겠습니까?')) return;
    
    checked.each(function() {
        var seq = $(this).val();
        doAction(seq, mode, true);
    });
}
</script>
</body>
</html>
