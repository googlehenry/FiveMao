package com.example.myvideos.http;

import java.net.SocketException;
import java.net.UnknownHostException;
import java.security.KeyStore;
import java.util.List;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.HttpVersion;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.conn.params.ConnManagerParams;
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

import com.ex.fivemao.exception.CrashHandler;
  
/** 
 * Https Post���� 
 */  
public class HttpsPostThread extends Thread {  
  
    private Handler handler;  
    private String httpUrl;  
    private List<NameValuePair> valueList;  
    private int mWhat;  
  
    public static final int ERROR = 404;  
    public static final int SUCCESS = 200;  
  
    public HttpsPostThread(Handler handler, String httpUrl,  
            List<NameValuePair> list, int what) {  
        super();  
        this.handler = handler;  
        this.httpUrl = httpUrl;  
        this.valueList = list;  
        this.mWhat = what;  
    }  
  
    public HttpsPostThread(Handler handler, String httpUrl,  
            List<NameValuePair> list) {  
        super();  
        this.handler = handler;  
        this.httpUrl = httpUrl;  
        this.valueList = list;  
        this.mWhat = SUCCESS;  
    }  
  
    @Override  
    public void run() {  
        // TODO Auto-generated method stub  
        String result = null;  
        try {  
            HttpParams httpParameters = new BasicHttpParams();
            
            // �������ӹ������ĳ�ʱ  
            ConnManagerParams.setTimeout(httpParameters, 10000);  
            // �������ӳ�ʱ  
            HttpConnectionParams.setConnectionTimeout(httpParameters, 10000);  
            // ����socket��ʱ  
            HttpConnectionParams.setSoTimeout(httpParameters, 10000);  
            HttpClient hc = getHttpClient(httpParameters);  
            HttpPost post = new HttpPost(httpUrl);  
            post.setEntity(new UrlEncodedFormEntity(valueList, HTTP.UTF_8));  
            post.setParams(httpParameters);
            HttpResponse response = null;  
            try {  
                response = hc.execute(post);  
            } catch (UnknownHostException e) {  
                throw new Exception("Unable to access "  
                        + e.getLocalizedMessage());  
            } catch (SocketException e) {  
                throw new Exception(e.getLocalizedMessage());  
            }  
            int sCode = response.getStatusLine().getStatusCode();  
            if (sCode == HttpStatus.SC_OK) {  
            	
                result = EntityUtils.toString(response.getEntity(), HTTP.UTF_8);  
                if (handler != null) {  
                    handler.sendMessage(Message.obtain(handler, mWhat, result)); // ����ɹ�  
                }  
            } else {  
                result = "����ʧ��" + sCode; // ����ʧ��  
                // 404 - δ�ҵ�  
                if (handler != null) {  
                    handler.sendMessage(Message.obtain(handler, ERROR, result));  
                }  
            }  
        } catch (Exception e) {  
            CrashHandler.logErrorToFile(e);  
            if (handler != null) {  
                result = "����ʧ��,�쳣�˳�";  
                handler.sendMessage(Message.obtain(handler, ERROR, result));  
            }  
        }  
        super.run();  
    }  
  
    /** 
     * ��ȡHttpClient 
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
  
            // ����http https֧��  
            SchemeRegistry registry = new SchemeRegistry();  
            registry.register(new Scheme("http", PlainSocketFactory  
                    .getSocketFactory(), 80));  
            registry.register(new Scheme("https", sf, 443));// SSL/TSL����֤���̣��˿�Ϊ443  
            ClientConnectionManager ccm = new ThreadSafeClientConnManager(  
                    params, registry);  
            return new DefaultHttpClient(ccm, params);  
        } catch (Exception e) {  
            return new DefaultHttpClient(params);  
        }  
    }  
}  