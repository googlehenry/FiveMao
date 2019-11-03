package com.example.myvideos.http;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.HashMap;
import java.util.Map;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

public class HttpsUploadUtil {
	  private static final String TAG = "uploadFile";
	  private static final int TIME_OUT = 20 * 1000; // 超时时间
	  private static final String CHARSET = "utf-8"; // 设置编码
	  private HttpsURLConnection conn = null;  
	  /**
	   * 通过拼接的方式构造请求内容，实现参数传输以及文件传输
	   * 
	   * @param url Service net address
	   * @param params text content
	   * @param files pictures
	   * @return String result of Service response
	   * @throws IOException
	   */
	  public HttpsUploadUtil(String url) {
		  try {
			prepareHttpsConnection(url);
		} catch (Exception e) {
		} 
	  }
	  private void prepareHttpsConnection(String url) throws KeyManagementException, NoSuchAlgorithmException, IOException {  
	        SSLContext sslContext = SSLContext.getInstance("TLS");  
	        sslContext.init(null, new TrustManager[]{new MyTrustManager()}, new SecureRandom());  
	        HttpsURLConnection.setDefaultSSLSocketFactory(sslContext.getSocketFactory());  
	        HttpsURLConnection.setDefaultHostnameVerifier(new MyHostnameVerifier());  
	        conn = (HttpsURLConnection) new URL(url).openConnection();  
	        conn.setDoOutput(true);  
	        conn.setDoInput(true);  
	        conn.setConnectTimeout(TIME_OUT);  
	    }  
	  
	  
	  public int getHttpResponseCode(String url){
		  try{
			  SSLContext sslContext = SSLContext.getInstance("TLS");  
		        sslContext.init(null, new TrustManager[]{new MyTrustManager()}, new SecureRandom());  
		        HttpsURLConnection.setDefaultSSLSocketFactory(sslContext.getSocketFactory());  
		        HttpsURLConnection.setDefaultHostnameVerifier(new MyHostnameVerifier());  
		        conn = (HttpsURLConnection) new URL(url).openConnection();  
		        conn.setDoOutput(false);  
		        conn.setDoInput(false);  
		        conn.setConnectTimeout(TIME_OUT);  
				// 得到响应码
				int res = conn.getResponseCode();
				
				return res;
		  }catch(Exception e){
			  return -1;
		  }
	  }
	  public String getHttpResponseCodeText(String url){
		  try{
			  SSLContext sslContext = SSLContext.getInstance("TLS");  
		        sslContext.init(null, new TrustManager[]{new MyTrustManager()}, new SecureRandom());  
		        HttpsURLConnection.setDefaultSSLSocketFactory(sslContext.getSocketFactory());  
		        HttpsURLConnection.setDefaultHostnameVerifier(new MyHostnameVerifier());  
		        conn = (HttpsURLConnection) new URL(url).openConnection();  
		        conn.setDoOutput(false);  
		        conn.setDoInput(false);  
		        conn.setConnectTimeout(TIME_OUT);  
				// 得到响应码
				int res = conn.getResponseCode();
				
				return String.valueOf(res);
		  }catch(Exception e){
			  return e.getMessage();
		  }
	  }
	  
	  public String post(String url, Map<String, String> params, Map<String, File> files)
	      throws Exception {
	    String BOUNDARY = java.util.UUID.randomUUID().toString();
	    String PREFIX = "--", LINEND = "\r\n";
	    String MULTIPART_FROM_DATA = "multipart/form-data";
	    String CHARSET = "UTF-8";
	    
	    
	    SSLContext sslContext = SSLContext.getInstance("TLS");  
        sslContext.init(null, new TrustManager[]{new MyTrustManager()}, new SecureRandom());  
        conn.setDefaultSSLSocketFactory(sslContext.getSocketFactory());  
        conn.setDefaultHostnameVerifier(new MyHostnameVerifier());  
	      
	    conn.setRequestMethod("POST");
	    conn.setRequestProperty("connection", "keep-alive");
	    conn.setRequestProperty("Charsert", "UTF-8");
	    conn.setRequestProperty("Content-Type", MULTIPART_FROM_DATA + ";boundary=" + BOUNDARY);
	    // 首先组拼文本类型的参数
	    DataOutputStream outStream = new DataOutputStream(conn.getOutputStream());
	    if(params!=null){
	    	StringBuilder sb = new StringBuilder();
		    for (Map.Entry<String, String> entry : params.entrySet()) {
		      sb.append(PREFIX);
		      sb.append(BOUNDARY);
		      sb.append(LINEND);
		      sb.append("Content-Disposition: form-data; name=\"" + entry.getKey() + "\"" + LINEND);
		      sb.append("Content-Type: text/plain; charset=" + CHARSET + LINEND);
		      sb.append("Content-Transfer-Encoding: 8bit" + LINEND);
		      sb.append(LINEND);
		      sb.append(entry.getValue());
		      sb.append(LINEND);
		    }
		    outStream.write(sb.toString().getBytes());
	    }
	    
	    // 发送文件数据
	    if (files != null)
	      for (Map.Entry<String, File> file : files.entrySet()) {
	        StringBuilder sb1 = new StringBuilder();
	        sb1.append(PREFIX);
	        sb1.append(BOUNDARY);
	        sb1.append(LINEND);
	        sb1.append("Content-Disposition: form-data; name=\""+file.getKey()+"\"; filename=\""
	            + file.getValue().getName() + "\"" + LINEND);
	        String imageType = "png";
	        int idxPoint = file.getValue().getName().lastIndexOf(".")+1;
	        if(idxPoint>0){
	        	imageType = file.getValue().getName().substring(idxPoint).toLowerCase();
	        }
	        sb1.append("Content-Type: image/" + imageType + LINEND);
	        sb1.append(LINEND);
	        outStream.write(sb1.toString().getBytes());
	        InputStream is = new FileInputStream(file.getValue());
	        byte[] buffer = new byte[1024];
	        int len = 0;
	        while ((len = is.read(buffer)) != -1) {
	          outStream.write(buffer, 0, len);
	        }
	        is.close();
	        outStream.write(LINEND.getBytes());
	      }
	    // 请求结束标志
	    byte[] end_data = (PREFIX + BOUNDARY + PREFIX + LINEND).getBytes();
	    outStream.write(end_data);
	    outStream.flush();
	    outStream.close();
	    // 得到响应码
	    StringBuilder sb2 = new StringBuilder();
	    int res = conn.getResponseCode();
	    InputStream in = conn.getInputStream();
	    
	    if (res == 200) {
	    	
	        byte[] buffer = new byte[1024];
	        int len = 0;
	        while ((len = in.read(buffer)) != -1) {
	          String dataLine = new String(buffer,0,len,"UTF-8");
	          sb2.append(dataLine);
	        }
	        
	    }
	      in.close();
	    conn.disconnect();
	    return sb2.toString();
	  }
	 
	  
	  private static class MyHostnameVerifier implements HostnameVerifier {  
		  
	        @Override  
	        public boolean verify(String hostname, SSLSession session) {  
	            return true;  
	        }  
	    }  
	  
	  private static class MyTrustManager implements X509TrustManager {  
		  
	        @Override  
	        public void checkClientTrusted(X509Certificate[] chain, String authType) throws CertificateException {  
	  
	        }  
	  
	        @Override  
	        public void checkServerTrusted(X509Certificate[] chain, String authType) throws CertificateException {  
	  
	        }  
	  
	        @Override  
	        public X509Certificate[] getAcceptedIssuers() {  
	            return null;  
	        }  
	    }  
	}