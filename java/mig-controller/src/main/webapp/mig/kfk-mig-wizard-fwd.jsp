<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ page import="java.util.*,java.util.List,java.util.Set,java.util.HashSet,java.util.Map,c.y.mig.model.*" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="jaes" uri="http://www.yojori.com/taglib/jaes" %>
<%@include file="/mig/session-admin-check.jsp"%>
<!DOCTYPE html>
<html lang="ko">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>Kafka 이관 등록 ( ${level} level )</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/css/bootstrap.min.css" rel="stylesheet">
    <link href="https://cdn.jsdelivr.net/npm/bootstrap-icons@1.10.5/font/bootstrap-icons.css" rel="stylesheet">
    <style>
        body { font-family: 'Inter', sans-serif; background-color: #f8f9fa; }
        .wizard-container { max-width: 900px; margin: 50px auto; background: white; border-radius: 12px; box-shadow: 0 10px 30px rgba(0,0,0,0.1); overflow: hidden; }
        .wizard-header { background: #fff; border-bottom: 2px solid #eee; padding: 30px; }
        .wizard-body { padding: 40px; min-height: 400px; }
        .wizard-footer { background: #f1f3f5; padding: 20px 40px; display: flex; justify-content: center; gap: 20px; }
        .form-label { font-weight: 600; color: #495057; font-size: 0.9rem; }
        .marker-red { color: #dc3545; margin-right: 3px; }
        .desc-box { background: #fdfdfd; border: 1px solid #dee2e6; color: #6c757d; font-size: 0.85rem; padding: 15px; margin-top: 30px; line-height: 1.6; }
        .level-badge { float: right; font-weight: normal; font-size: 1.2rem; color: #999; }
    </style>
    <script>
        function goNext() {
            var frm = document.frmWizard;
            // Basic validation for required fields
            var requiredInputs = frm.querySelectorAll('[required]');
            for (var i = 0; i < requiredInputs.length; i++) {
                if (!requiredInputs[i].value) {
                    alert(requiredInputs[i].title + "을(를) 입력/선택하세요.");
                    requiredInputs[i].focus();
                    return;
                }
            }
            
            // Set level for next step
            frm.level.value = parseInt(frm.level.value) + 1;
            if (frm.level.value > 3) {
                // Final submission
                frm.action = "kfk-mig-wizard-proc.jsp";
                frm.submit();
            } else {
                frm.submit();
            }
        }

        function goPrev() {
            var frm = document.frmWizard;
            frm.level.value = parseInt(frm.level.value) - 1;
            frm.submit();
        }

        function toggleConnectors() {
            var type = document.querySelector('input[name="registration_type"]:checked').value;
            var sourceRow = document.getElementById('row_source_connector');
            var sinkRow = document.getElementById('row_sink_connector');

            if (type === 'SOURCE_ONLY') {
                if(sourceRow) {
                    sourceRow.style.display = '';
                    sourceRow.querySelector('select').disabled = false;
                }
                if(sinkRow) {
                    sinkRow.style.display = 'none';
                    sinkRow.querySelector('select').disabled = true;
                }
            } else if (type === 'SINK_ONLY') {
                if(sourceRow) {
                    sourceRow.style.display = 'none';
                    sourceRow.querySelector('select').disabled = true;
                }
                if(sinkRow) {
                    sinkRow.style.display = '';
                    sinkRow.querySelector('select').disabled = false;
                }
            } else {
                if(sourceRow) {
                    sourceRow.style.display = '';
                    sourceRow.querySelector('select').disabled = false;
                }
                if(sinkRow) {
                    sinkRow.style.display = '';
                    sinkRow.querySelector('select').disabled = false;
                }
            }
        }

        function syncPreview() {
            var frm = document.frmWizard;
            var sourcePre = document.getElementById('sourceCurl');
            var sinkPre = document.getElementById('sinkCurl');
            
            if (sourcePre) {
                var jsonStr = sourcePre.innerText;
                // Simple regex-based sync for demonstration. For robustness, we could rebuild the JSON.
                var configMatch = jsonStr.match(/"config": \{([\s\S]*?)\}/);
                if (configMatch) {
                    var configContent = configMatch[1];
                    var inputs = frm.querySelectorAll('[name^="SOURCE__"]');
                    inputs.forEach(function(input) {
                        var key = input.name.replace('SOURCE__', '');
                        var val = input.value;
                        var re = new RegExp('"' + key.replace('.', '\\.') + '": ".*?"', 'g');
                        configContent = configContent.replace(re, '"' + key + '": "' + val + '"');
                    });
                    sourcePre.innerText = jsonStr.replace(configMatch[1], configContent);
                }
            }
            
            if (sinkPre) {
                var jsonStr = sinkPre.innerText;
                var configMatch = jsonStr.match(/"config": \{([\s\S]*?)\}/);
                if (configMatch) {
                    var configContent = configMatch[1];
                    var inputs = frm.querySelectorAll('[name^="SINK__"]');
                    inputs.forEach(function(input) {
                        var key = input.name.replace('SINK__', '');
                        var val = input.value;
                        var re = new RegExp('"' + key.replace('.', '\\.') + '": ".*?"', 'g');
                        configContent = configContent.replace(re, '"' + key + '": "' + val + '"');
                    });
                    sinkPre.innerText = jsonStr.replace(configMatch[1], configContent);
                }
            }
        }

        window.onload = function() {
            if (document.querySelector('input[name="registration_type"]')) {
                toggleConnectors();
            }
        };
    </script>
</head>
<body>

<div class="wizard-container">
    <div class="wizard-header">
        <span class="level-badge">( ${level} level )</span>
        <h2 class="mb-0 fw-bold">Kafka 이관 등록</h2>
        <div class="mt-3 text-muted">
            <span class="me-4 text-dark"><span class="fw-normal">그룹코드 :</span> ${mig_master}</span>
            <span class="me-4 text-dark"><span class="fw-normal">그룹명 :</span> ${masterInfo.master_name}</span>
            <span class="text-dark"><span class="fw-normal">Target DB :</span> ${migListInfo.target_db_alias}</span>
        </div>
    </div>

    <form name="frmWizard" method="post" action="kfk-mig-wizard.jsp">
        <input type="hidden" name="mig_master" value="${mig_master}">
        <input type="hidden" name="mig_list_seq" value="${mig_list_seq}">
        <c:if test="${level > 0}">
            <input type="hidden" name="registration_type" value="${registration_type}">
        </c:if>
        <input type="hidden" name="source_connector" value="${source_connector}">
        <input type="hidden" name="sink_connector" value="${sink_connector}">
        <input type="hidden" name="sourceDb" value="${sourceDb}">
        <input type="hidden" name="targetDb" value="${targetDb}">
        <input type="hidden" name="level" value="${level}">
        
        <%
            // Prepare a set of keys for parameters that are rendered in the current level
            Set<String> currentKeys = new HashSet<String>();
            List<KfkParamTemplate> p0 = (List<KfkParamTemplate>)request.getAttribute("params");
            List<KfkParamTemplate> ps = (List<KfkParamTemplate>)request.getAttribute("sourceParams");
            List<KfkParamTemplate> pk = (List<KfkParamTemplate>)request.getAttribute("sinkParams");
            if (p0 != null) { for (KfkParamTemplate t : p0) currentKeys.add(t.getParam_key()); }
            if (ps != null) { for (KfkParamTemplate t : ps) currentKeys.add(t.getParam_key()); }
            if (pk != null) { for (KfkParamTemplate t : pk) currentKeys.add(t.getParam_key()); }
            request.setAttribute("currentKeys", currentKeys);
        %>
        
        <!-- Accumulate all parameters from previous steps, excluding those in the current level or standard wizard fields -->
        <c:forEach items="${param}" var="p">
            <c:if test="${p.key ne 'level' && p.key ne 'mig_master' && p.key ne 'mig_list_seq' && p.key ne 'registration_type'}">
                <input type="hidden" name="${p.key}" value="${p.value}">
            </c:if>
        </c:forEach>
        
        <%-- Handle registration_type already handled at line 152 --%>

        <div class="wizard-body">
            <div class="row g-4">
                <c:if test="${level == 0}">
                    <div class="col-12"><h5 class="fw-bold text-primary border-bottom pb-2">기본 이관 설정</h5></div>
                    
                    <c:set var="reg_type" value="${registration_type}" />
                    <c:if test="${empty reg_type}"><c:set var="reg_type" value="BOTH" /></c:if>

                    <!-- Registration Type Selection -->
                    <div class="col-12 row align-items-center">
                        <label class="col-md-3 form-label"><span class="marker-red">*</span>등록 유형</label>
                        <div class="col-md-9">
                            <div class="form-check form-check-inline">
                                <input class="form-check-input" type="radio" name="registration_type" id="type_both" value="BOTH" 
                                       onclick="toggleConnectors()" ${reg_type eq 'BOTH' ? 'checked' : ''}>
                                <label class="form-check-label" for="type_both">전체 등록 (Source + Sink)</label>
                            </div>
                            <div class="form-check form-check-inline">
                                <input class="form-check-input" type="radio" name="registration_type" id="type_source" value="SOURCE_ONLY" 
                                       onclick="toggleConnectors()" ${reg_type eq 'SOURCE_ONLY' ? 'checked' : ''}>
                                <label class="form-check-label" for="type_source">소스만 등록 (Extraction Only)</label>
                            </div>
                            <div class="form-check form-check-inline">
                                <input class="form-check-input" type="radio" name="registration_type" id="type_sink" value="SINK_ONLY" 
                                       onclick="toggleConnectors()" ${reg_type eq 'SINK_ONLY' ? 'checked' : ''}>
                                <label class="form-check-label" for="type_sink">싱크만 등록 (Load Only)</label>
                            </div>
                        </div>
                    </div>
                    
                    <c:forEach items="${params}" var="item">
                        <c:set var="preKey" value="${item.param_key eq 'source_connector' ? 'SOURCE__' : (item.param_key eq 'sink_connector' ? 'SINK__' : 'COMMON__')}${item.param_key}" />
                        <c:set var="val" value="${param[preKey]}" />
                        <c:if test="${empty val}"><c:set var="val" value="${savedParam[preKey]}" /></c:if>
                        <c:if test="${empty val}"><c:set var="val" value="${item.default_value}" /></c:if>
                        
                        <c:if test="${empty val}">
                            <c:if test="${item.param_key eq 'connection.url'}">
                                <c:set var="val" value="${sourceDb.jdbcUrl}" />
                            </c:if>
                            <c:if test="${item.param_key eq 'connection.user' || item.param_key eq 'connection.username'}">
                                <c:set var="val" value="${sourceDb.username}" />
                            </c:if>
                            <c:if test="${item.param_key eq 'connection.password'}">
                                <c:set var="val" value="${sourceDb.password}" />
                            </c:if>
                        </c:if>
                        <c:choose>
                            <c:when test="${item.hidden_yn eq 'Y'}">
                                <input type="hidden" name="${preKey}" value="${val}" 
                                       data-par-column-key="${item.par_column_key}">
                            </c:when>
                            <c:otherwise>
                                <div class="col-12 row align-items-center" id="row_${item.param_key}">
                                    <label class="col-md-3 form-label">
                                        <c:if test="${item.required_yn eq 'Y'}"><span class="marker-red">*</span></c:if>
                                        ${item.param_name}
                                    </label>
                                    <div class="col-md-4">
                                        <c:choose>
                                            <c:when test="${item.input_method eq 'COMBO'}">
                                                <jaes:codeselect name="${preKey}" group="${item.group_cd}" selected="${val}" 
                                                                 styleClass="form-select" onchange="syncPreview()" />
                                            </c:when>
                                            <c:when test="${item.input_method eq 'VIEWTEXT'}">
                                                <input type="text" class="form-control bg-light" name="${preKey}" value="${val}" readonly>
                                            </c:when>
                                            <c:otherwise>
                                                <input type="text" class="form-control" name="${preKey}" value="${val}" 
                                                       ${item.required_yn eq 'Y' ? 'required' : ''} title="${item.param_name}" oninput="syncPreview()">
                                            </c:otherwise>
                                        </c:choose>
                                    </div>
                                    <div class="col-md-5 small text-muted">
                                        ${item.param_explain}
                                    </div>
                                </div>
                            </c:otherwise>
                        </c:choose>
                    </c:forEach>
                </c:if>

                <c:if test="${level > 0}">
                    <c:set var="reg_type" value="${registration_type}" />
                    <c:if test="${empty reg_type}"><c:set var="reg_type" value="BOTH" /></c:if>

                    <c:if test="${reg_type ne 'SINK_ONLY'}">
                        <div class="col-12"><h5 class="fw-bold text-success border-bottom pb-2 mt-4">Source Configuration <small class="text-secondary">(${source_connector})</small></h5></div>
                    <c:forEach items="${sourceParams}" var="item">
                        <c:set var="preKey" value="SOURCE__${item.param_key}" />
                        <c:set var="val" value="${param[preKey]}" />
                        <c:if test="${empty val}"><c:set var="val" value="${savedParam[preKey]}" /></c:if>
                        <c:if test="${empty val}"><c:set var="val" value="${item.default_value}" /></c:if>
                        
                        <c:if test="${empty val}">
                            <c:if test="${item.param_key eq 'connection.url'}">
                                <c:set var="val" value="${sourceDb.jdbcUrl}" />
                            </c:if>
                            <c:if test="${item.param_key eq 'connection.user' || item.param_key eq 'connection.username'}">
                                <c:set var="val" value="${sourceDb.username}" />
                            </c:if>
                            <c:if test="${item.param_key eq 'connection.password'}">
                                <c:set var="val" value="${sourceDb.password}" />
                            </c:if>
                        </c:if>
                        <c:choose>
                            <c:when test="${item.hidden_yn eq 'Y'}">
                                <input type="hidden" name="${preKey}" value="${val}" 
                                       data-par-column-key="${item.par_column_key}">
                            </c:when>
                            <c:otherwise>
                                <div class="col-12 row align-items-center">
                                    <label class="col-md-3 form-label">
                                        <c:if test="${item.required_yn eq 'Y'}"><span class="marker-red">*</span></c:if>
                                        ${item.param_name}
                                    </label>
                                    <div class="col-md-4">
                                        <c:choose>
                                            <c:when test="${item.input_method eq 'COMBO'}">
                                                <jaes:codeselect name="${preKey}" group="${item.group_cd}" selected="${val}" 
                                                                 styleClass="form-select" onchange="syncPreview()" />
                                            </c:when>
                                            <c:when test="${item.input_method eq 'VIEWTEXT'}">
                                                <input type="text" class="form-control bg-light" name="${preKey}" value="${val}" readonly>
                                            </c:when>
                                            <c:otherwise>
                                                <input type="text" class="form-control" name="${preKey}" value="${val}" 
                                                       ${item.required_yn eq 'Y' ? 'required' : ''} title="${item.param_name}" oninput="syncPreview()">
                                            </c:otherwise>
                                        </c:choose>
                                    </div>
                                    <div class="col-md-5 small text-muted">
                                        ${item.param_explain}
                                    </div>
                                </div>
                            </c:otherwise>
                        </c:choose>
                    </c:forEach>
                    </c:if>

                    <c:if test="${reg_type ne 'SOURCE_ONLY'}">
                        <div class="col-12"><h5 class="fw-bold text-info border-bottom pb-2 mt-5">Sink Configuration <small class="text-secondary">(${sink_connector})</small></h5></div>
                    <c:forEach items="${sinkParams}" var="item">
                        <c:set var="preKey" value="SINK__${item.param_key}" />
                        <c:set var="val" value="${param[preKey]}" />
                        <c:if test="${empty val}"><c:set var="val" value="${savedParam[preKey]}" /></c:if>
                        <c:if test="${empty val}"><c:set var="val" value="${item.default_value}" /></c:if>
                        
                        <c:if test="${empty val}">
                            <c:if test="${item.param_key eq 'connection.url'}">
                                <c:set var="val" value="${targetDb.jdbcUrl}" />
                            </c:if>
                            <c:if test="${item.param_key eq 'connection.user' || item.param_key eq 'connection.username'}">
                                <c:set var="val" value="${targetDb.username}" />
                            </c:if>
                            <c:if test="${item.param_key eq 'connection.password'}">
                                <c:set var="val" value="${targetDb.password}" />
                            </c:if>
                        </c:if>
                        <c:choose>
                            <c:when test="${item.hidden_yn eq 'Y'}">
                                <input type="hidden" name="${preKey}" value="${val}" 
                                       data-par-column-key="${item.par_column_key}">
                            </c:when>
                            <c:otherwise>
                                <div class="col-12 row align-items-center">
                                    <label class="col-md-3 form-label">
                                        <c:if test="${item.required_yn eq 'Y'}"><span class="marker-red">*</span></c:if>
                                        ${item.param_name}
                                    </label>
                                    <div class="col-md-4">
                                        <c:choose>
                                            <c:when test="${item.input_method eq 'COMBO'}">
                                                <jaes:codeselect name="${preKey}" group="${item.group_cd}" selected="${val}" 
                                                                 styleClass="form-select" onchange="syncPreview()" />
                                            </c:when>
                                            <c:when test="${item.input_method eq 'VIEWTEXT'}">
                                                <input type="text" class="form-control bg-light" name="${preKey}" value="${val}" readonly>
                                            </c:when>
                                            <c:otherwise>
                                                <input type="text" class="form-control" name="${preKey}" value="${val}" 
                                                       ${item.required_yn eq 'Y' ? 'required' : ''} title="${item.param_name}" oninput="syncPreview()">
                                            </c:otherwise>
                                        </c:choose>
                                    </div>
                                    <div class="col-md-5 small text-muted">
                                        ${item.param_explain}
                                    </div>
                                </div>
                            </c:otherwise>
                        </c:choose>
                    </c:forEach>
                    </c:if>
                </c:if>
            </div>

            <div class="desc-box">
                <c:if test="${level == 0}">
                    이관을 등록하는 화면<br>
                    kfk_param_template의 데이터가 존재하는 Level까지 등록 진행<br>
                    dp_level, dp_order, hidden_yn 정보를 기준으로 등록항목 표시
                </c:if>
                <c:if test="${level == 3}">
                    완료 버튼과 함께 template의 데이터를 param으로 이관하고, kfk_mig_list에도 목록을 조회하기 위한 기본 설정값 정보를 등록<br>
                    완료를 하게되면 바로 connector에 등록이 되는건 아님 목록에서 다시 확인하고 등록을 해야 함<br>
                    작업 개체 수는 1보다 큰값이면 먼저 topic을 먼저 생성
                </c:if>
            </div>
            
            </div>
        </div>

        <div class="wizard-footer">
            <c:if test="${level > 0}">
                <button type="button" class="btn btn-secondary btn-lg px-5 shadow-sm" onclick="goPrev();">이전</button>
            </c:if>
            <button type="button" class="btn btn-primary btn-lg px-5 shadow-sm" onclick="goNext();">${level == 3 ? '완료' : '다음'}</button>
        </div>
    </form>
</div>

<script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/js/bootstrap.bundle.min.js"></script>
</body>
</html>
