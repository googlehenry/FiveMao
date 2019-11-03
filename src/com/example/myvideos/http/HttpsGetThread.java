package com.example.myvideos.http;

import java.net.SocketException;
import java.net.UnknownHostException;
import java.security.KeyStore;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.HttpVersion;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.params.HttpProtocolParams;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;

import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.ex.fivemao.exception.CrashHandler;
  
/** 
 * Https get请求 
 */  
public class HttpsGetThread extends Thread {  
  
    private Handler handler;  
    private String httpUrl;  
    private int mWhat;  
  
    // public static final int ERROR = 404;  
    // public static final int SUCCESS = 200;  
  
    public HttpsGetThread(Handler handler, String httpUrl) {  
        super();  
        this.handler = handler;  
        this.httpUrl = httpUrl;  
        mWhat = 200;  
    }  
    
  
    public HttpsGetThread(Handler handler, String httpUrl, int what) {  
        super();  
        this.handler = handler;  
        this.httpUrl = httpUrl;  
        mWhat = what;  
    }  
  
    @Override  
    public void run() {  
        // TODO Auto-generated method stub  
        try {  
            HttpParams httpParameters = new BasicHttpParams();  
            HttpConnectionParams.setConnectionTimeout(httpParameters, 10000);  
            HttpConnectionParams.setSoTimeout(httpParameters, 10000);  
            HttpClient hc = getHttpClient(httpParameters);  
            HttpGet get = new HttpGet(httpUrl);  
            get.setParams(httpParameters);  
            HttpResponse response = null;  
            try {  
                response = hc.execute(get);  
            } catch (UnknownHostException e) {  
                throw new Exception("Unable to access "  
                        + e.getLocalizedMessage());  
            } catch (SocketException e) {  
                throw new Exception(e.getLocalizedMessage());  
            }  
            int sCode = response.getStatusLine().getStatusCode();  
            if (sCode == HttpStatus.SC_OK) {  
            	
                String result = EntityUtils.toString(response.getEntity(),  
                		HTTP.UTF_8);  
                handler.sendMessage(Message.obtain(handler, mWhat, result)); // 请求成功  
            } else {  
                String result = EntityUtils.toString(response.getEntity(),  
                        HTTP.UTF_8);  
            }  
  
        } catch (Exception e) {  
            CrashHandler.logErrorToFile(e);  
            Log.i("info", "=============异常退出==============");  
            handler.sendMessage(Message.obtain(handler, 404, "异常退出:"+e.getMessage()));  
        }  
        super.run();  
    }  
  
    /** 
     * 获取HttpClient 
     *  
     * @param params 
     * @return 
     */  
    public static HttpClient getHttpClient(HttpParams params) {  
        try {  
            KeyStore trustStore = KeyStore.getInstance(KeyStore  
                    .getDefaultType());  
            trustStore.load(null, null);  
  
            SSLSocketFactory sf = new SSLSocketFactoryImp(trustStore);  
            sf.setHostnameVerifier(SSLSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER);  
  
            HttpProtocolParams.setVersion(params, HttpVersion.HTTP_1_1);  
            HttpProtocolParams.setContentCharset(params, HTTP.UTF_8);  
            HttpProtocolParams.setUseExpectContinue(params, true);  
  
            // 设置http https支持  
            SchemeRegistry registry = new SchemeRegistry();  
            registry.register(new Scheme("http", PlainSocketFactory  
                    .getSocketFactory(), 80));  
            registry.register(new Scheme("https", sf, 443));// SSL/TSL的认证过程，端口为443  
            ClientConnectionManager ccm = new ThreadSafeClientConnManager(  
                    params, registry);  
            return new DefaultHttpClient(ccm, params);  
        } catch (Exception e) {  
            return new DefaultHttpClient(params);  
        }  
    }  
}  