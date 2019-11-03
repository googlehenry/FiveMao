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
	            //�Ѵ�������·��ת��URL  
	            URL url = new URL(path);  
	            //��ȡ����  
	            HttpURLConnection connection = (HttpURLConnection) url.openConnection();  
	            //ʹ��GET������������  
	            connection.setRequestMethod("GET");  
	            //��ʱʱ��Ϊ10��  
	            connection.setConnectTimeout(10000);  
	            //��ȡ������  
	            int code = connection.getResponseCode();  
	            if (code == 200) {  
	                InputStream inputStream = connection.getInputStream();  
	                //ʹ�ù��������������������Bitmap  
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
