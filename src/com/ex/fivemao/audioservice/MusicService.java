package com.ex.fivemao.audioservice;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;

import android.app.Service;
import android.content.Intent;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaPlayer.OnPreparedListener;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.widget.Toast;

import com.danikula.videocache.MyHttpProxyCacheServer;
import com.ex.fivemao.MainActivity;
import com.ex.fivemao.exception.CrashHandler;
import com.ex.fivemao.io.CacheFileUtil;
import com.ex.fivemao.io.PropertiesUtilWithEnc;
import com.ex.fivemao.utils.FlagsUtil;
import com.example.myvideos.http.HttpsGetThread;
import com.example.myvideos.http.proxy.App;

public class MusicService extends Service {
    
	private static MyHttpProxyCacheServer proxyServer;
	private List<String> musicUrls = new ArrayList<String>();

	private int currentIdx = 0;
	
	private static MediaPlayer mediaPlayer = new MediaPlayer();

	private boolean cacheFlag = false;
	 
	  public static boolean isReady = false;
	  
	  public static boolean isplaying = false;
	  public static String action = "START";
	  
	  
	@Override
	  public void onCreate() {
	    //onCreate在Service的生命周期中只会调用一次
	    super.onCreate();
	 
	    //初始化媒体播放器
	    if(proxyServer==null){
        	proxyServer = App.getProxy(this);
        }
	    mediaPlayer.setOnPreparedListener(new OnPreparedListener() {
			@Override
			public void onPrepared(MediaPlayer mp) {
				isReady = true;
				isplaying = true;
				FlagsUtil.bgmusicErrorOccurred = false;
				mp.start();
			}
		});
	    mediaPlayer.setOnErrorListener(new MediaPlayer.OnErrorListener() {
	      @Override
	      public boolean onError(MediaPlayer mp, int what, int extra) {
	        mp.reset();
	        FlagsUtil.bgmusicErrorOccurred = true;
	        Toast.makeText(MusicService.this, "音乐服务出现错误,已停止。", Toast.LENGTH_SHORT).show();
	        return false;
	      }
	    });
	    
	    mediaPlayer.setOnCompletionListener(new OnCompletionListener() {
			@Override
			public void onCompletion(MediaPlayer mp) {
				if((currentIdx+1)<musicUrls.size()){
					currentIdx = currentIdx + 1;
					prepareMusic(currentIdx);
				}else{
					currentIdx = 0;
					prepareMusic(currentIdx);
				}
			}
		});
	    
	    getCacheSettings();	    
	  }

	private void loadMusic() {
		String appRecUrl = String.valueOf(PropertiesUtilWithEnc.prop.getProperty("domainCall"))+"/dictionary/fewRandomSimpleMusics/"+PropertiesUtilWithEnc.getString("fixedMusicCatID")+"/3/";
		
		HttpsGetThread thread = new HttpsGetThread(new Handler() { 
            @Override  
            public void handleMessage(Message msg) {  
                super.handleMessage(msg);  
                String result = (String) msg.obj;  
                switch (msg.what) {  
                case 200:  
                	try {
						JSONArray jsonarr = new JSONArray(result);
						if(jsonarr.length()<=0){
							return;
						}
						
						for(int i = 0; i < jsonarr.length(); i++){
							JSONObject jobject = jsonarr.getJSONObject(i);
							String url = String.valueOf(PropertiesUtilWithEnc.prop.getProperty("domainCall"))
									+"/resources/ui/"+jobject.getString("musicSoundFileLink").replace("../", "");
							if(cacheFlag){
								musicUrls.add(proxyServer.getProxyUrl(url));
							}else{
								musicUrls.add(url);
							}
						}
						if(musicUrls.size()>0){
							Collections.shuffle(musicUrls);
							prepareMusic(0);
						}
					} catch (Exception e) {
						CrashHandler.logErrorToFile(e);
					}
                    break;  
                case 404:  
                    break;  
                }  
  
            }  
        },appRecUrl, 200);  
        thread.start();
	}

	private void prepareMusic(final int idx) {
		startPlayIdx(idx);
	}

	private void startPlayIdx(int idx) {
		currentIdx = idx;
		try{
			mediaPlayer.reset();
			mediaPlayer.setDataSource(musicUrls.get(currentIdx));
			mediaPlayer.prepareAsync();
		} catch (IOException e) {
		  CrashHandler.logErrorToFile(e);
		  isplaying = false;
		  isReady = false;
		}
	}
	 
	  @Override
	  public int onStartCommand(Intent intent, int flags, int startId) {
	    //每次调用Context的startService都会触发onStartCommand回调方法
	    //所以onStartCommand在Service的生命周期中可能会被调用多次
		  if(action.equals("RESUME")){
		    if(isReady && !mediaPlayer.isPlaying()){
			      mediaPlayer.start();
			      isplaying = true;
			      Toast.makeText(this, "开始播放背景音乐", Toast.LENGTH_SHORT).show();
		    }else if(FlagsUtil.bgmusicErrorOccurred!=null || FlagsUtil.bgmusicErrorOccurred){
		    	loadMusic();
				isplaying = true;
		    }
		  }else if(action.equals("PAUSE")){
			  if(isReady && isplaying && mediaPlayer.isPlaying()){
			      mediaPlayer.pause();
			      isplaying = false;
			      Toast.makeText(this, "停止播放背景音乐", Toast.LENGTH_SHORT).show();
			  }else if(FlagsUtil.bgmusicErrorOccurred!=null || FlagsUtil.bgmusicErrorOccurred){
			    	loadMusic();
					isplaying = true;
			  }
		  }else if(action.equals("PAUSEBYSYS")){
			  if(isReady && isplaying && mediaPlayer.isPlaying()){
			      mediaPlayer.pause();
			      isplaying = false;
			      Toast.makeText(this, "停止播放背景音乐", Toast.LENGTH_SHORT).show();
			  }
		  }else if(action.equals("RESET")){
			  if(isReady){
				  isplaying = false;
			      mediaPlayer.reset();
			  }
		  }else if(action.equals("START")){
			  if(isReady){
			  }else{
				  loadMusic();
				  isplaying = true;
			  }
		  }
		  
	    return START_STICKY;
	  }
	 
	  @Override
	  public IBinder onBind(Intent intent) {
	    //该Service中不支持bindService方法，所以此处直接返回null
	    return null;
	  }
	 
	  @Override
	  public void onDestroy() {
	    //当调用Context的stopService或Service内部执行stopSelf方法时就会触发onDestroy回调方法
	    super.onDestroy();
	    if(mediaPlayer != null && isReady){
	      stopMe();
	    }else{
	    	new Thread(new Runnable() {
				
				@Override
				public void run() {
					int count= 200;
					while(!isReady){
						try {
							Thread.sleep(1000);
							count = count -1;
							if(count<0){
								break;
							}
						} catch (InterruptedException e) {
							CrashHandler.logErrorToFile(e);
						}
					}
					if(isReady){
						stopMe();
					}
					
				}
			}).start();
	    }
	  }

	private void stopMe() {
		if(mediaPlayer.isPlaying()){
	        //停止播放音乐
	        mediaPlayer.stop();
	      }
	      //释放媒体播放器资源
	      mediaPlayer.release();
	      isplaying = false;
	      isReady = false;
	      Toast.makeText(this, "停止背景音乐服务", Toast.LENGTH_SHORT).show();
	}
	  
	  private void getCacheSettings() {
			String cacheFileContent = CacheFileUtil.readCacheFile(getCacheDir().getAbsolutePath(), "playercache.duo.dat");
	    	if(cacheFileContent!=null){
	    		if(cacheFileContent.trim().equalsIgnoreCase("Y")){
	    			cacheFlag  = true;
	    		}else{
	    			cacheFlag = false;
	    		}
	    	}
		}

	public static MediaPlayer getMediaPlayer() {
		return mediaPlayer;
	}
	
	
}
