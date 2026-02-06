<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="jaes" uri="http://www.yojori.com/taglib/jaes" %>
<%@include file="/mig/session-admin-check.jsp"%>
<html>
<head>
<meta http-equiv="content-type" content="text/html; charset=UTF-8">
<title>Insert Table 등록</title>
<link rel="stylesheet" href="/mig/style.css" type="text/css">
<script language="javascript" src="/mig/common.js"></script>
<script>
    function goRegist()
    {
    	document.frm1.mode.value = "insertAll";
    	document.frm1.submit();
    }
    
    function goTableCreate()
    {
    	/*
    	if(document.getElementsByName("source_table").legnth == 0 || document.getElementsByName("target_table").legnth == 0)
    	{
    		alert("테이블 저장 후 생성버튼을 누르세요.");
    		return;
    	}
    	*/
    	
    	document.frm1.mode.value = "createAll";
    	document.frm1.submit();
    }
    
    function goDelete(insert_table_seq)
    {
    	if(confirm("삭제하시겠습니까?"))
    	{    	
    		document.frm1.mode.value = "goDelete";
    		document.frm1.auto_insert_table_seq.value = insert_table_seq;
    		document.frm1.submit();
    	}    	
    }
    
    function createBatch(mig_list_seq)
    {
        var url = "/mig/insert-table-area.jsp?mig_list_seq=" + mig_list_seq; 
        popup_window(url, "createBatch", "left=150, top=150, width=1200, height=800, scrollbars=yes");
    }
</script>
</head>
<body topmargin="0" leftmargin="10">
<br>
<br>
<input type="button" name="btn1" value="등록/수정" onclick="goRegist();">&nbsp;&nbsp;&nbsp;
<input type="button" name="btn3" id="btn3" value="Close" onclick="self.close();">
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
<!--
    자동 테이블 생성은 2019/01/12 정상적으로 동작하지 않음 
<input type="button" name="btn4" id="btn4" value="테이블 생성" onclick="goTableCreate();">
 -->
 <input type="button" name="btn5" id="btn5" value="일괄 등록" onclick="createBatch('${search.mig_list_seq}');">
<br>
<br># 기본적으로 테이블 이관은 Thread를 사용, Page Count Per Thread 개수만큼 한번에 불러와서 진행
<br>&nbsp;&nbsp;&nbsp;&nbsp;Page Count Per Thread 건수 이하의 테이블은 source_pk 등록 필요 없으나
<br># 건수이상의 테이블은 Paging을 사용해야 함 그래서 source_pk(Order by) 컬럼 입력이 필요
<br># pk가 한개면 id 라고 입력하고 PK 2개 이상이면 id1, id2 이렇게 comma 구분자로 걍 입력 
<br>&nbsp;&nbsp;&nbsp;&nbsp;-> Order by id1, id2 이렇게 Order by 절에 사용됨
<br><br>
<form name="frm1" method="post" action="./insert-table-write.jsp">
<input type="hidden" name="mode" value="${search.mode}" />
<input type="hidden" name="mig_list_seq" value="${search.mig_list_seq}" />
<input type="hidden" name="auto_insert_table_seq" value="" />
<table width=100% cellpadding=0 cellspacing=0>
<tr><td colspan=6 height=3 bgcolor=#979797></td></tr>
<tr height=34 align=center>
    <td>source_table</td>
    <td>source_pk(Order by)</td>
    <td>target_table</td>
    <td>Truncate Yn</td>
    <td>삭제</td>
    <td>등록일</td>
        </tr>
<tr><td colspan=6 height=1 bgcolor=#D5D3D3></td></tr>
<tr><td colspan=6 height=2 bgcolor=#F5F5F5></td></tr>

<!-- List -->
<c:forEach items="${list}" var="list" >
<input type="hidden" name="insert_table_seq" value="${list.insert_table_seq}" />
<tr height=28 align=center> 
    <td class="date"><input type="text" name="source_table" value="${list.source_table}"></td>
    <td class="date"><input type="text" name="source_pk" value="${list.source_pk}"></td>
    <td class="date"><input type="text" name="target_table" value="${list.target_table}"></td>
    <td class="date"><input type="checkbox" name="truncate_${list.insert_table_seq}" id="truncat_${list.insert_table_seq}" <c:if test="${list.truncate_yn == 'Y'}"> checked</c:if> value="Y" /></td>
    <td class="date"><input type="button" name="btn2" value="삭제" onclick="goDelete('${list.insert_table_seq}');" /></td>
    <td class="date">${list.create_date}</td>
</tr>
<tr><td colspan=6 height=1 bgcolor=#E7E7E7></td></tr>
</c:forEach>

<c:forEach begin="1" end="5" var="cnt" >
<tr height=28 align=center> 
    <input type="hidden" name="insert_table_seq" value="" />
    <td class="date"><input type="text" name="source_table" value=""></td>
    <td class="date"><input type="text" name="source_pk" value=""></td>
    <td class="date"><input type="text" name="target_table" value=""></td>
    <td class="date">&nbsp;</td>
    <td class="date">&nbsp;</td>
    <td class="date">create_date</td>
</tr>
</c:forEach>
<tr><td colspan=6 bgcolor=#DADADA height=1>
</table>
</form>
</body>
</html>