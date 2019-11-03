package com.example.myvideos.http;

import java.io.File;
import java.util.Map;

import android.os.Handler;
import android.os.Message;

public class MultipartFormHttpsPostThread extends Thread { 
	Handler handler;
	Map<String, String> params;
	Map<String, File> files;
    String url;  
    public static final int ERROR = 404;  
    public static final int SUCCESS = 200;  
    public MultipartFormHttpsPostThread(Handler handler, String url,Map<String, String> params, Map<String, File> files) {  
    	this.handler = handler;
        this.url = url;
        this.params = params;
        this.files = files;
    }  
  
    @Override  
    public void run() { 
    	String result = null;  
        try {  
        	HttpsUploadUtil httpsHelper = new HttpsUploadUtil(url);  
            result = httpsHelper.post(url,params,files);
            handler.sendMessage(Message.obtain(handler, SUCCESS, result));
        } catch (Exception e) {
        	handler.sendMessage(Message.obtain(handler, ERROR, e.getMessage()));  
        } 
    }  
}  