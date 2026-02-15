<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
    <%@ page import="com.yojori.util.*" %>
        <%@ page import="com.yojori.manager.*" %>
            <%@ page import="com.yojori.migration.controller.model.*" %>
                <%@ page import="java.util.*" %>
                    <%@include file="/mig/session-admin-check.jsp" %>
                        <% request.setCharacterEncoding("UTF-8"); WorkListManager workManager=new WorkListManager();
                            MigrationList migList=(MigrationList) RequestUtils.getBean(request, MigrationList.class);
                            String[] master_code=request.getParameterValues("master_code"); String[]
                            mig_list_seq=request.getParameterValues("mig_list_seq"); String[]
                            execute_yn=request.getParameterValues("execute_yn"); String[]
                            display_yn=request.getParameterValues("display_yn"); String[]
                            mig_name=request.getParameterValues("mig_name"); List<String> resultMessages = new ArrayList
                            <>();

                                int registeredCount = 0;

                                /* Batch Execution */
                                if (master_code != null && master_code.length > 0 && mig_list_seq != null &&
                                mig_list_seq.length > 0) {
                                for (int index = master_code.length - 1; index >= 0; index--) {
                                if ("Y".equals(execute_yn[index]) && "Y".equals(display_yn[index])) {
                                String msg = registerWork(workManager, mig_list_seq[index], mig_name[index]);
                                resultMessages.add(msg);
                                registeredCount++;
                                }
                                }
                                } else if (migList.getMig_master() != null && migList.getMig_master().length() > 0) {
                                /* Single Execution */
                                String msg = registerWork(workManager, migList.getMig_list_seq(),
                                migList.getMig_name());
                                resultMessages.add(msg);
                                registeredCount++;
                                }
                                %>
                                <%! /* Helper method to register work */ public String registerWork(WorkListManager
                                    manager, String migListSeq, String migName) { WorkList work=new WorkList(); /*
                                    work.setWork_seq(Config.getOrdNoSequence("WK")); // Handled by DB auto-increment */
                                    work.setMig_list_seq(migListSeq); work.setStatus(WorkList.STATUS_READY);
                                    work.setCreate_date(new Date()); manager.insert(work); return "작업 등록 [" + migName
                                    + "] (ID: " + migListSeq + ") - Status: <strong>READY</strong>" ; } %>
                                    <!DOCTYPE html>
                                    <html lang="ko">

                                    <head>
                                        <meta charset="UTF-8">
                                        <meta name="viewport" content="width=device-width, initial-scale=1">
                                        <title>이관 작업 실행 결과</title>
                                        <!-- Bootstrap 5 CSS -->
                                        <link
                                            href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/css/bootstrap.min.css"
                                            rel="stylesheet">
                                        <!-- Bootstrap Icons -->
                                        <link
                                            href="https://cdn.jsdelivr.net/npm/bootstrap-icons@1.10.5/font/bootstrap-icons.css"
                                            rel="stylesheet">
                                        <!-- Google Fonts -->
                                        <link
                                            href="https://fonts.googleapis.com/css2?family=Inter:wght@400;500;600&display=swap"
                                            rel="stylesheet">

                                        <style>
                                            body {
                                                font-family: 'Inter', sans-serif;
                                                background-color: #f8f9fa;
                                            }

                                            .card {
                                                border-radius: 12px;
                                                border: none;
                                                box-shadow: 0 4px 15px rgba(0, 0, 0, 0.05);
                                            }
                                        </style>

                                        <script>
                                            var timeLeft = 5;
                                            function startTimer() {
                                                var timerElement = document.getElementById('timer');
                                                var interval = setInterval(function () {
                                                    timeLeft--;
                                                    timerElement.innerText = timeLeft;

                                                    if (timeLeft <= 0) {
                                                        clearInterval(interval);
                                                        window.close();
                                                    }
                                                }, 1000);
                                            }
                                        </script>
                                    </head>

                                    <body class="d-flex align-items-center justify-content-center min-vh-100 p-4"
                                        onload="startTimer();">

                                        <div class="card p-4 text-center" style="max-width: 500px; width: 100%;">
                                            <div class="mb-3">
                                                <i class="bi bi-check-circle-fill text-success"
                                                    style="font-size: 3rem;"></i>
                                            </div>

                                            <h4 class="fw-bold mb-3">작업 등록 완료</h4>
                                            <p class="text-muted mb-4">
                                                총 <span class="fw-bold text-primary">
                                                    <%=registeredCount%>
                                                </span> 건의 이관 작업이 대기열에 등록되었습니다.<br>
                                                Worker 프로세스가 순차적으로 작업을 처리합니다.
                                            </p>

                                            <!-- Result List -->
                                            <% if (resultMessages.size()> 0) { %>
                                                <div class="text-start bg-light p-3 rounded mb-4 overflow-auto"
                                                    style="max-height: 200px; font-size: 0.9rem;">
                                                    <ul class="list-unstyled mb-0">
                                                        <% for (String msg : resultMessages) { %>
                                                            <li class="mb-1"><i class="bi bi-dot"></i>
                                                                <%=msg%>
                                                            </li>
                                                            <% } %>
                                                    </ul>
                                                </div>
                                                <% } %>

                                                    <div class="d-grid gap-2">
                                                        <button type="button" class="btn btn-secondary"
                                                            onclick="window.close();">
                                                            닫기 (<span id="timer">5</span>초 후 자동 닫힘)
                                                        </button>
                                                    </div>
                                        </div>

                                        <script
                                            src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/js/bootstrap.bundle.min.js"></script>
                                    </body>

                                    </html>