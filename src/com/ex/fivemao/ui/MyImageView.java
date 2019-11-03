package com.ex.fivemao.ui;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.util.Base64;
import android.widget.ImageView;
import android.widget.Toast;

import com.ex.fivemao.exception.CrashHandler;
  
public class MyImageView extends ImageView {  
    public static final int GET_DATA_SUCCESS = 1;  
    public static final int NETWORK_ERROR = 2;  
    public static final int SERVER_ERROR = 3;  
    //���̲߳��ܲ���UI��ͨ��Handler����ͼƬ  
    private Handler handler = new Handler() {  
        @Override  
        public void handleMessage(Message msg) {  
           switch (msg.what){  
               case GET_DATA_SUCCESS:  
            	   Object[] result = (Object[])msg.obj;
            	   MyImageView igView =  (MyImageView)result[0];
                   Bitmap bitmap = (Bitmap) result[1];  
                   igView.setImageBitmap(bitmap);  
                   break;  
               case NETWORK_ERROR:  
                   Toast.makeText(getContext(),"��������ʧ��",Toast.LENGTH_SHORT).show();  
                   break;  
               case SERVER_ERROR:  
                   Toast.makeText(getContext(),"��������������",Toast.LENGTH_SHORT).show();  
                   break;  
           }  
        }  
    };  
  
    public MyImageView(Context context, AttributeSet attrs, int defStyleAttr) {  
        super(context, attrs, defStyleAttr);  
    }  
  
    public MyImageView(Context context) {  
        super(context);  
    }  
  
    public MyImageView(Context context, AttributeSet attrs) {  
        super(context, attrs);  
    }  
  
    //��������ͼƬ  
    public void setImageURL(String path) {  
        //����һ���߳���������  
    	new MyImageSetterThread(this,path).start();  
    }  
  class MyImageSetterThread extends Thread{
	  MyImageView imageView;
	  String path;
	  public MyImageSetterThread(MyImageView imageView,String path) {
		  this.imageView = imageView;
		  this.path = path;
	  }
	  @Override  
      public void run() {  
          try {  
          	if(path==null || path.trim().length()==0){
          		return;
          	}else if(path.startsWith("data:image")){
          		byte[] decodedString = Base64.decode(path.substring(path.indexOf("base64,")+"base64,".length()), Base64.DEFAULT);
          		Bitmap bitMap = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
          		Message msg = Message.obtain();
          		 Object[] result = new Object[]{imageView,bitMap};
                  msg.obj = result;  
                  msg.what = GET_DATA_SUCCESS;  
                  handler.sendMessage(msg);
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
                      Bitmap bitmap = BitmapFactory.decodeStream(inputStream);  
                      //����Message��ͼƬ����Handler  
                      Message msg = Message.obtain();  
//                      msg.obj = bitmap;
                      Object[] result = new Object[]{imageView,bitmap};
                      msg.obj = result;  
                      msg.what = GET_DATA_SUCCESS;  
                      handler.sendMessage(msg);  
                      inputStream.close();  
                  }else {  
                      //��������������  
                      handler.sendEmptyMessage(SERVER_ERROR);  
                  }  
          	}
          } catch (IOException e) {  
              CrashHandler.logErrorToFile(e);  
              //�������Ӵ���  
              handler.sendEmptyMessage(NETWORK_ERROR);  
          }  
      } 
  }
}  