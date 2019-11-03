package com.ex.fivemao.utils;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;

public class NetworkUtil {
	public static Bitmap loadNetImage(String path){
    	Bitmap bitmap = null;
    	try{
	    	if(path.startsWith("data:image")){
	    		byte[] decodedString = Base64.decode(path.substring(path.indexOf("base64,")+"base64,".length()), Base64.DEFAULT);
	    		bitmap = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
	    	}else{
	            //把传过来的路径转成URL  
	            URL url = new URL(path);  
	            //获取连接  
	            HttpURLConnection connection = (HttpURLConnection) url.openConnection();  
	            //使用GET方法访问网络  
	            connection.setRequestMethod("GET");  
	            //超时时间为10秒  
	            connection.setConnectTimeout(10000);  
	            //获取返回码  
	            int code = connection.getResponseCode();  
	            if (code == 200) {  
	                InputStream inputStream = connection.getInputStream();  
	                //使用工厂把网络的输入流生产Bitmap  
	                bitmap = BitmapFactory.decodeStream(inputStream);  
	                inputStream.close();  
	            }else {  
	            }  
	    	}
	    	return bitmap;
    	}catch(Exception e){
    		return bitmap;
    	}
    }
}
