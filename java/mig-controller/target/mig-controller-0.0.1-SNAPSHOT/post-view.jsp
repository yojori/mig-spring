<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ page import="com.yojori.util.*" %>
<%@ page import="com.yojori.manager.*" %>
<%@ page import="com.yojori.model.*" %>
<%@ page import="java.util.*" %>
<%@ page import="java.io.*" %>
<%@ page import="java.net.*" %>
<%@page import="javax.xml.parsers.*"%>
<%@page import="org.w3c.dom.*"%>
<%@page import="org.xml.sax.InputSource"%>
<%@page import="java.io.StringReader"%>
<%




    String serviceKey = URLDecoder.decode("lQ93QNyp6gVra8iguHkWi4dazQ%2BEfDQ0f%2FDp8PDy0zqEVEvtngZG3me2EHKnDkc4dI%2Bux%2FIHU6eb2lFRn5UsGw%3D%3D", "UTF-8");
    
    StringBuilder urlBuilder = new StringBuilder("http://openapi.epost.go.kr/trace/retrieveLongitudinalService/retrieveLongitudinalService/getLongitudinalDomesticList"); /*URL*/
    //urlBuilder.append("?" + URLEncoder.encode("ServiceKey","UTF-8") + "=" + serviceKey); /*Service Key*/
    urlBuilder.append("?" + URLEncoder.encode("ServiceKey","UTF-8") + "=" + URLEncoder.encode(serviceKey, "UTF-8")); /*Service Key*/
    urlBuilder.append("&" + URLEncoder.encode("rgist","UTF-8") + "=" + URLEncoder.encode("1400504758412", "UTF-8")); /*등기번호*/
    URL url = new URL(urlBuilder.toString());
    HttpURLConnection conn = (HttpURLConnection) url.openConnection();
    conn.setRequestMethod("GET");
    conn.setRequestProperty("Content-type", "application/json");
    System.out.println("Response code: " + conn.getResponseCode());
    BufferedReader rd;
    if(conn.getResponseCode() >= 200 && conn.getResponseCode() <= 300) {
        rd = new BufferedReader(new InputStreamReader(conn.getInputStream(), "UTF-8"));
    } else {
        rd = new BufferedReader(new InputStreamReader(conn.getErrorStream()));
    }
    StringBuilder sb = new StringBuilder();
    String line;
    while ((line = rd.readLine()) != null) {
        sb.append(line);
    }
    rd.close();
    conn.disconnect();
    //out.println(sb.toString());
    
    // xml 파싱하기
    //InputSource is = new InputSource(new StringReader(sb));
//            builder = factory.newDocumentBuilder();
//          doc = builder.parse(is);
    
    DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
    DocumentBuilder doc = factory.newDocumentBuilder();
    Document parser = doc.parse(sb.toString());
    
    NodeList list = parser.getElementsByTagName("longitudinalDomesticList");
    
    if(list != null && list.getLength() > 0)
    {
    	int length = list.getLength();
    	
    	System.out.println("length : " + length);
    	
    	Node longitudinalDomesticList = list.item(length - 1);
    	
    	NodeList childList = longitudinalDomesticList.getChildNodes();
    	
    	String dlvyDate = "";
    	String nowLc = "";
    	String processSttus = "";
    	String detailDc = "";    	
    	
    	for(int k = 0; k < childList.getLength(); k++)
    	{
    		Node item = childList.item(k);
    		String value = item.getNodeName();
    		
    		if(value.equals("dlvyDate"))
    			dlvyDate = item.getTextContent();
    		
    		if(value.equals("nowLc"))
                dlvyDate = dlvyDate + " " + item.getTextContent();
    		
    		if(value.equals("processSttus"))
    			processSttus = item.getTextContent();
            
            if(value.equals("detailDc"))
            	detailDc = item.getTextContent();
    	}
    	
    	out.println("dlvyDate : " + dlvyDate + ", nowLc : " + nowLc + ", processSttus : " + processSttus + ", detailDc : " + detailDc);
    }
%>




