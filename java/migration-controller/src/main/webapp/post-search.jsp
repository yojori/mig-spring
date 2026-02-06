
<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@page import="java.io.StringReader"%>
<%@ page import="java.io.BufferedReader"%>
<%@ page import="java.io.InputStreamReader"%>
<%@ page import="java.io.StringReader"%>
<%@ page import="java.net.HttpURLConnection"%>
<%@ page import="java.net.URL"%>
<%@ page import="java.util.*" %>
<%@ page import="java.io.*" %>
<%@ page import="java.net.*" %>
<%@ page import="javax.xml.parsers.DocumentBuilder"%>
<%@ page import="javax.xml.parsers.DocumentBuilderFactory"%>
<%@ page import="javax.xml.xpath.XPath"%>
<%@ page import="javax.xml.xpath.XPathConstants"%>
<%@ page import="javax.xml.xpath.XPathExpression"%>
<%@ page import="javax.xml.xpath.XPathFactory"%> 
<%@ page import="org.w3c.dom.Document"%>
<%@ page import="org.w3c.dom.Element"%>
<%@ page import="org.w3c.dom.Node"%>
<%@ page import="org.w3c.dom.NodeList"%>
<%@ page import="org.xml.sax.InputSource"%>
<%@ page import="org.json.simple.JSONObject"%>
<%@ page import="org.json.simple.parser.JSONParser"%>
<%@ page import="org.json.simple.parser.ParseException"%>
<%@page import="org.json.simple.JSONArray"%>
<%@page import="com.yojori.util.StringUtil"%>
<%
String serviceKey = "devU01TX0FVVEgyMDIyMDMyNjIxNTI0MTExMjM5MTY=";


//String[][] arr_value = new String[2200][20];
String[][] arr_value = new String[2200][14];

// D:\이재석\01 20191105 능곡 관련\20220324 등기결과 및 양식.xlsx

File file = new File("C:\\java\\juso2.txt"); 
if(file.exists()) 
{
	BufferedReader inFile = new BufferedReader(new FileReader(file)); 
	String sLine = null; 
	
	int index = 0;
	
	//for(int index = 0; index < arr_value.length; index++)
	while( (sLine = inFile.readLine()) != null ) 
	{
		// 0 조합원번호, 1 부번, 2 청산여부, 3 이름, 4 주소, 5 중복여부 
		// 배열에서 주소를 가져옴
		
		System.out.println(sLine);


		String[] temp = sLine.split(";", 6);
		
        arr_value[index][0] = temp[0];
        arr_value[index][1] = temp[1];
        arr_value[index][2] = temp[2];
        arr_value[index][3] = temp[3];
        arr_value[index][4] = temp[4];
        arr_value[index][5] = temp[5];
        
        arr_value[index][6] = "";
        arr_value[index][7] = "";
        arr_value[index][8] = "";
        arr_value[index][9] = "";		
        arr_value[index][10] = "";
        arr_value[index][11] = "";
        arr_value[index][12] = "";
        arr_value[index][13] = "";
		
		//arr_value[index] = sLine.split(";", 6);
		
		//System.out.println("length : " + arr_value[index].length);
		
		String keyword = StringUtil.nvl(arr_value[index][4]);
		
		 String san_yn = StringUtil.nvl(arr_value[index][2]);
		 String duple_yn = StringUtil.nvl(arr_value[index][5]).replace(";", "");
		 
		 //System.out.println("keyword " + keyword + ",san_yn : " + san_yn + ", duple_yn : " + duple_yn);
		 
		 
		 if("".equals(keyword))
		 {
			 index++;
			 continue;
		 }
		 
		 /*
		 if("Y".equals(san_yn) || "Y".equals(duple_yn))
		 {
			 continue;
		 }
		 */
		 
		 //if(index == 10)
		//	 break;
		 
		
		// 스페이스 구분자로 주소를 자르기
		String[] newStr = keyword.split("\\s+");
		
		String str_juso = "";
		int inner_loop_count = 0;
		
		String[] ar_juso = new String[3];
		
		for(int xxx = 0; xxx < newStr.length; xxx++)
		{
			//if(inner_loop_count == 5)
			//	break;
			
			String loop_key = newStr[xxx];
			
		    str_juso = str_juso + " " + loop_key;
		    
		    //System.out.println(" loop_key : " + loop_key + ", juso :" + str_juso);
		    
		    if(inner_loop_count >= 3 && inner_loop_count <= 5)
		    {
		    	ar_juso[inner_loop_count - 3] = str_juso;	    	
		    }
		    
		    
	        inner_loop_count++;	    
		}
		
		//System.out.println("ar_juso.length : " + ar_juso.length);
		
		for(int zz = 0; zz < ar_juso.length; zz++)
		{
			// System.out.println(" juso :" + ar_juso[zz]);
			
		    // 요청변수 설정
		    String currentPage = "1";
		    String countPerPage = "1";
		    
		    str_juso = ar_juso[zz];
		    
		    if(str_juso == null)
		    	break;
		    
		    // System.out.println("str_juso : " + ar_juso[zz]);
	
		    // API 호출 URL 정보 설정
		    String apiUrl = "https://www.juso.go.kr/addrlink/addrLinkApi.do?currentPage=" + currentPage
		            + "&countPerPage=" + countPerPage
		            + "&keyword=" + URLEncoder.encode(str_juso,"UTF-8")
		            + "&confmKey=" + serviceKey
		            + "&resultType=json"
		            ;
		    URL url = new URL(apiUrl); 
		    BufferedReader br = new BufferedReader(
		    new InputStreamReader(
		    url.openStream(),"UTF-8"));
		    StringBuffer sb = new StringBuffer();
		    String tempStr = null;
		    while(true)
		    {
		        tempStr = br.readLine();
		        
		        if(tempStr == null) break;
		        
		        sb.append(tempStr);
		    }
		    
		    br.close();
		    
		    //System.out.println(sb.toString() + "<br>");
		    
			JSONParser parser = new JSONParser();
			      
			JSONObject obj = null;
			      
			try {
			     
				
				obj = (JSONObject)parser.parse(sb.toString());
				 
				 JSONObject result = (JSONObject)obj.get("results");
				 
				 //System.out.println("errorcode : " + ((JSONObject)result.get("common")).get("errorCode"));
				 
				 JSONArray array = (JSONArray)result.get("juso");
				 
				 if(array != null && array.size() == 1)
				 {
					 JSONObject js_juso = (JSONObject)array.get(0);
					 
					 //System.out.println("str_juso : " +  str_juso + ", zz : " + (zz+7));
					 
					 //System.out.println("zipcode : " +  js_juso.get("zipNo") + ", zz : " + (zz+8));
					 
					 arr_value[index][7 + (zz) * 2] = str_juso;
					 arr_value[index][8 + (zz) * 2] = (String)js_juso.get("zipNo");
				 }
			     
			} catch (ParseException e) {
			     System.out.println("변환에 실패");
			     e.printStackTrace();
			}
		}
		
		index++;
	} // end of while
	
	inFile.close();
} // end of if exist

%>

<html>
<head>
<meta http-equiv="content-type" content="text/html; charset=UTF-8">
<title>우편번호 목록</title>
<body topmargin="0" leftmargin="10" >
<table width="100%" cellpadding="0" cellspacing="0" border="1">
<tr height=34 align=center>
    <td>조합원번호</td>
    <td>부번</td>
    <td>청산여부</td>
    <td>이름</td>
    <td>주소</td>
    <td>주소중복</td>

    <td>주소1</td>
    <td>우편1</td>    
    <td>주소2</td>
    <td>우편2</td>    
    <td>주소3</td>
    <td>우편3</td>
</tr>
<%
for(int index = 0; index < arr_value.length; index++)
{
    /*
    arr_value[index][1] = addrseNm;
    arr_value[index][2] = applcntNm;
    arr_value[index][3] = dlvyDe;
    arr_value[index][4] = dlvySttus;
    arr_value[index][5] = dlvyDate;
    arr_value[index][6] = nowLc;
    arr_value[index][7] = processSttus;
    arr_value[index][8] = detailDc;        
    */
    
%>  
<tr height=34 align=center>
    <td><%=arr_value[index][0]%></td>
    <td><%=arr_value[index][1]%></td>
    <td><%=arr_value[index][2]%></td>
    <td><%=arr_value[index][3]%></td>
    <td><%=arr_value[index][4]%></td>
    <td><%=arr_value[index][5] %></td>
    
    <td><%=arr_value[index][7]%></td>
    <td><%=arr_value[index][8]%></td>
    
    <td><%=arr_value[index][9]%></td>    
    <td><%=arr_value[index][10]%></td>
    
    <td><%=arr_value[index][11]%></td>
    <td><%=arr_value[index][12]%></td>    
</tr>
    
<%  
}
%>
</table>
</body>
</html>



