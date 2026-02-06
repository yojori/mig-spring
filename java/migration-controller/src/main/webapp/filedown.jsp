<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ page import="com.yojori.util.*" %>
<%@ page import="com.yojori.manager.*" %>
<%@ page import="com.yojori.model.*" %>
<%@ page import="java.util.*" %>
<%@ page import="java.io.*" %>
<%
	java.net.URL url = new java.net.URL("https://withus.sknetworks.com/downloadAttachfiles.do"); // 호출할 url
	Map<String,Object> params = new LinkedHashMap<>(); // 파라미터 세팅
	params.put("att_seq", "692394BvBQSNVfk");
	params.put("grp_cd", "344225");
	
	// out.println("mig_list_seq : " + valueArray[0][index] + "<BR>");
	
	StringBuilder postData = new StringBuilder();
	for(Map.Entry<String,Object> param : params.entrySet()) {
	    if(postData.length() != 0) postData.append('&');
	    postData.append(java.net.URLEncoder.encode(param.getKey(), "UTF-8"));
	    postData.append('=');
	    postData.append(java.net.URLEncoder.encode(String.valueOf(param.getValue()), "UTF-8"));
	}
	
	byte[] postDataBytes = postData.toString().getBytes("UTF-8");
	
	java.net.HttpURLConnection conn = (java.net.HttpURLConnection)url.openConnection();
	conn.setRequestMethod("POST");
	conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
	conn.setRequestProperty("Content-Length", String.valueOf(postDataBytes.length));
	conn.setDoOutput(true);
	conn.getOutputStream().write(postDataBytes); // POST 호출
    
    String toPath = "D:\\upload\\download\\";
    String toDate = "20170801";
    String fileName = "20170801161800554Gs2GZQmbczn035phWQY1NDhnFjLg1RvC9nTCdDbMQkxfxBl3jRNp!1968468605!15015708116749.502936015704807E9.pdf";

    //String orgDir = pDir.getPath();
    //orgDir = orgDir.replace(fromPath, toPath);

    File intoDir = new File(toPath + toDate);

    intoDir.mkdirs();

    //FileInputStream fis = null;
    StringBuffer sb = null;

    FileOutputStream fos = null;
    
    try {
         
        // 복사 될 곳을 지정
        File des = new File(toPath + toDate + "\\" + fileName);
         
        InputStream input = conn.getInputStream();
        
        fos = new FileOutputStream(des);
                     
        int readBuffer = 0;
        byte [] buffer = new byte[1024];
        // BufferedReader 로 부터 파일의 내용을 읽어와 저장한다.
        while((readBuffer = input.read(buffer)) != -1) {
        	fos.write(buffer, 0, readBuffer);            
        }
        
    } catch (Exception e) {

        e.printStackTrace();

    } finally {

        try {

            if(fos != null)fos.close();

        } catch (IOException e) {

            e.printStackTrace();

        }

    }	

%>
