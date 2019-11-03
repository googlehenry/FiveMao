package com.ex.fivemao;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.TimeZone;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.AlertDialog;
import android.content.ClipboardManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.media.MediaMetadataRetriever;
import android.media.MediaPlayer;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.provider.MediaStore.Images;
import android.text.Html;
import android.util.Log;
import android.view.GestureDetector.OnDoubleTapListener;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.boredream.bdvideoplayer.BDVideoSplitView;
import com.boredream.bdvideoplayer.bean.VideoDetailInfo;
import com.boredream.bdvideoplayer.listener.SimpleOnVideoControlListener;
import com.boredream.bdvideoplayer.listener.SimplePlayerCallback;
import com.boredream.bdvideoplayer.utils.DisplayUtils;
import com.danikula.videocache.MyHttpProxyCacheServer;
import com.danikula.videocache.ProxyCacheException;
import com.ex.fivemao.audioservice.MusicService;
import com.ex.fivemao.exception.CrashHandler;
import com.ex.fivemao.io.CacheFileUtil;
import com.ex.fivemao.io.PropertiesUtilWithEnc;
import com.ex.fivemao.ui.MakeSureDialog;
import com.ex.fivemao.utils.FlagsUtil;
import com.ex.fivemao.utils.NetworkUtil;
import com.example.myvideos.http.HttpsGetThread;
import com.example.myvideos.http.proxy.App;
import com.example.myvideos.http.proxy.Utils;

/*�鿴�ز����ܣ����湦�ܣ���������
 * onprepared->onbufferupdate
 * onpause write to file
 * seek to stopped pos
 * retry with fastforward
 * 
 */

public class VideoPlayerActivity extends BaseActivity {
	private static MyHttpProxyCacheServer proxyServer;
	public static boolean popedOnceWhenBufferingDone = false;
	private static long freeMinutes = 6; 
	public static int totalFreeChancesToday = 3;
	public static Set<String> videosBought = new HashSet<String>();
	public static Set<String> videosLuckyShakes = new HashSet<String>();
	public static Set<String> videosFreebies = new HashSet<String>();
	public static String videoUrl ;
	public static String videoID ;
	public static String videoTitle;
	public static String videoFileLink;
	public static String videoDescription;
	public static String baiduYunUrl;
	public static String videoType;
	
	private BDVideoSplitView videoView ;
	private LinearLayout videoPlayWholePage;
	private Button buttonBuy;
	private Button baiduYunPlayVideo;
	private TextView videoBaiduYunDesc;
	private TextView videoPlayingHint;
	private TextView playerVideoName;
	private TextView playerVideoNote;
	MediaPlayer mPlayer;
	
	boolean previesPlay = false;
//	boolean handlingErrorSkipRecord = false;
	private static int lastPos = -1;
	protected boolean cacheFlag = false;
	private boolean bgMusicPausedBySystem = false;
	private boolean mustBack2Previous = false;
	
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        if(MusicService.isplaying){
	        pauseBGMusicBySystem();
			bgMusicPausedBySystem = true;
        }
        
        setContentView(R.layout.activity_sub_videoplay);
        buttonBuy = (Button)findViewById(R.id.buyVideo);
        baiduYunPlayVideo = (Button)findViewById(R.id.baiduYunPlayVideo);
        videoBaiduYunDesc = (TextView)findViewById(R.id.videoBaiduYunDesc);
        videoPlayingHint = (TextView)findViewById(R.id.videoPlayingHint);
        playerVideoName = (TextView)findViewById(R.id.playerVideoName);
        playerVideoNote = (TextView)findViewById(R.id.playerVideoNote);
        videoPlayWholePage = (LinearLayout)findViewById(R.id.videoPlayWholePage);
        if(proxyServer==null){
        	proxyServer = App.getProxy(VideoPlayerActivity.this);
        }
        lastPos = getStopPosLastPlay();
        
        Intent intent = getIntent();
        videoID = intent.getStringExtra("id");
        videoTitle = intent.getStringExtra("title");
        videoDescription = intent.getStringExtra("description");
        videoType = intent.getStringExtra("videoType");
        
        if(loginUserName!=null){
        	videosBought.clear();
        	videosLuckyShakes.clear();
        	loadLuckyFilms();
        	loadFilmsAuthed();
        }else{
        	AlertDialog.Builder builder = new AlertDialog.Builder(this);  
	        builder.setIcon(android.R.drawable.ic_dialog_info);  
	        builder.setTitle("��¼/�齱 ��ѿ���");  
	        builder.setPositiveButton("ȷ��", new DialogInterface.OnClickListener() {  
	            public void onClick(DialogInterface dialog, int which) { 
	            	gotoMePage();
	            }  
	        });  
	        
	        builder.show();  
	        
        	Toast.makeText( VideoPlayerActivity.this, "δ��½,δ����,ֻ���Կ���", Toast.LENGTH_LONG).show();
        }
        
//        if(videoTitle!=null&&videoTitle.contains("ǹ��")){
//        	AlertDialog.Builder builder = new AlertDialog.Builder(this);  
//	        builder.setIcon(android.R.drawable.ic_dialog_info);  
//	        builder.setTitle("ǹ�滭�ʲ���,�����,������ӰƬ����Ʊȥ��ӰԺ��");  
//	        builder.setPositiveButton("ȷ��", new DialogInterface.OnClickListener() {  
//	            public void onClick(DialogInterface dialog, int which) {  
//	            }  
//	        });  
//	        
//	        builder.show();  
//        }
        
        playerVideoName.setText(videoTitle);
        if(videoDescription==null || videoDescription.length()<=0){
        	videoDescription = "�Բ���,��ʱû�аٶ��Ƹ���������ӡ�";
        	baiduYunUrl = null;
        	baiduYunPlayVideo.setVisibility(View.GONE);
        	baiduYunPlayVideo.setFocusable(false);
    		baiduYunPlayVideo.setClickable(false);
    		baiduYunPlayVideo.setTextSize(18);        		
    		baiduYunPlayVideo.setText("�����������������");
        }else{
        	Pattern p = Pattern.compile("http[a-z:./0-9A-Z]{1,}");
        	Matcher m = p.matcher(videoDescription);
        	if(m.find()){
        		String baiduYunLink = m.group();
        		baiduYunUrl = baiduYunLink;
        		baiduYunPlayVideo.setFocusable(true);
        		baiduYunPlayVideo.setClickable(true);
        		baiduYunPlayVideo.setTextSize(24);
        		baiduYunPlayVideo.setText("�ٶ��Ƹ�����Ƶ");
        	}else{
        		baiduYunUrl = null;
        		baiduYunPlayVideo.setFocusable(false);
        		baiduYunPlayVideo.setClickable(false);
        		baiduYunPlayVideo.setTextSize(18);        		
        		baiduYunPlayVideo.setText("�����������������");
        	}
        }
        videoFileLink = intent.getStringExtra("videoFileLink");
        
        

        videoPlayingHint.setText("���ص�Ӱ��...");
        videoUrl = videoFileLink;
        
        String videoUrlTest = videoUrl;
        
        //playerVideoName.setText(videoUrlTest);
//        Uri uri = Uri.parse( videoUrl );
        videoView = (BDVideoSplitView)VideoPlayerActivity.this.findViewById(R.id.vplayPlayer );
        
        videoView.addDoubleTapListener(new OnDoubleTapListener() {
			
			@Override
			public boolean onSingleTapConfirmed(MotionEvent e) {
				return false;
			}

			@Override
			public boolean onDoubleTap(MotionEvent e) {
				doubleClicked(null);
				return false;
			}

			@Override
			public boolean onDoubleTapEvent(MotionEvent e) {
				return false;
			}
		});
        
        videoView.setOnVideoControlListener(new SimpleOnVideoControlListener() {

            @Override
            public void onRetry(int errorStatus) {
            		
            		try {
            			Map<Integer,String> errorMap = new HashMap<Integer,String>();
            			errorMap.put(0,"STATUS_NORMAL");
            			errorMap.put(1,"STATUS_VIDEO_DETAIL_ERROR");
            			errorMap.put(2,"STATUS_VIDEO_SRC_ERROR");
            			errorMap.put(3,"STATUS_UN_WIFI_ERROR");
            			errorMap.put(4,"STATUS_NO_NETWORK_ERROR");
//            			videoView.stopVideoPlayer();
            			Utils.cleanVideoCacheDir(VideoPlayerActivity.this);
            			retryVideoWithnewUrl();
                        
                        Toast.makeText( VideoPlayerActivity.this, "��ʾ:����������:"+errorMap.get(errorStatus), Toast.LENGTH_LONG).show();
                        //CacheFileUtil.writeCacheFile(getCacheDir().getAbsolutePath(), "playerexitpoint.dat", videoID);
					} catch (Exception e) {
						CrashHandler.logErrorToFile(e);
					}
            		
            }

            @Override
            public void onBack() {
                onBackPressed();
            }

            @Override
            public void onFullScreen() {
                DisplayUtils.toggleScreenOrientation(VideoPlayerActivity.this);
            }
        });
        videoView.addMediaPlayerCallback(new SimplePlayerCallback(){
        	@Override
    		public boolean onInfo(MediaPlayer mp, int what, int extra) {
    			if(what==MediaPlayer.MEDIA_INFO_VIDEO_RENDERING_START){
    				videoView.setBackgroundColor(Color.TRANSPARENT);
    				videoPlayingHint.setVisibility(View.GONE);
    				
    				//Toast.makeText( VideoPlayerActivity.this, "��ʼ����:˫�������л���ȫ��/����ģʽ��", Toast.LENGTH_LONG).show();
    			}
    			
    			return true;
    		}
        	
        	@Override
        	public void onPrepared(MediaPlayer mp) {
        		mPlayer = mp;
        		addFilmHotnessOnce();
        		
        	}
        	
        	@Override
        	public void onBufferingUpdate(MediaPlayer mp, int percent) {
        		
        		 // ��õ�ǰ����ʱ��͵�ǰ��Ƶ�ĳ���
                int currentPos = videoView.getCurrentPosition();
                int duration = videoView.getDuration();
                int currentBufferPos = (duration * percent / 100);
                int finalBufferingPos = currentPos>currentBufferPos?currentPos:currentBufferPos;
                
                if(freeMinutes==6){
                	int freeEnlarge = duration/1000/60/10;
                	if(freeEnlarge>freeMinutes){
                		freeMinutes = freeEnlarge;
                	}
                }
                
                if(lastPos==-1){
                	lastPos = getStopPosLastPlay();
                }else{
                	if(lastPos==-2){
                	}else{
                		if(finalBufferingPos>lastPos){
		                	if(finalBufferingPos<lastPos){
		                		if((finalBufferingPos-currentPos)/1000>60){
			                		mp.seekTo(finalBufferingPos);
									SimpleDateFormat sdf1 = new SimpleDateFormat("HH:mm:ss");
					                String timeStrTarget = sdf1.format(new Date(lastPos-TimeZone.getDefault().getRawOffset()));
					                String timeStrCurrent = sdf1.format(new Date(finalBufferingPos-TimeZone.getDefault().getRawOffset()));
									Toast.makeText(VideoPlayerActivity.this, "��ȴ�:����������"+timeStrCurrent+"/"+timeStrTarget, Toast.LENGTH_SHORT).show();
        						}
		                	}else if(finalBufferingPos>lastPos){
		                		mp.seekTo(lastPos);
		                		SimpleDateFormat sdf1 = new SimpleDateFormat("HH:mm:ss");
				                String timeStr = sdf1.format(new Date(lastPos-TimeZone.getDefault().getRawOffset()));
								Toast.makeText(VideoPlayerActivity.this, "��ϲ:�ɹ�������"+timeStr, Toast.LENGTH_SHORT).show();
								lastPos = -2;
		                	}
                		}else{
                			lastPos=-2;
                		}
                	}
                }
                
                
                SimpleDateFormat sdf1 = new SimpleDateFormat("HH:mm:ss");
                String timeStr = sdf1.format(new Date(finalBufferingPos-TimeZone.getDefault().getRawOffset()));
                 
                playerVideoNote.setText("������������ ������������ �ѻ���:"+timeStr+"��");
                
                if(videosBought.contains(videoID) || videosLuckyShakes.contains(videoID)
                		|| videosFreebies.contains(videoID) ||membershipExpiryDate!=null || totalFreeChancesToday>0){
                }else{
                	if(finalBufferingPos>=1000*60*freeMinutes*0.999){
                		if(popedOnceWhenBufferingDone){
                		}else{
	                		try {
	                			int count = proxyServer.shutdownClient(videoUrl);
								//Toast.makeText( VideoPlayerActivity.this,"�������:"+freeMinutes+"����", Toast.LENGTH_LONG).show();
								popedOnceWhenBufferingDone = true;
							} catch (ProxyCacheException e) {
								CrashHandler.logErrorToFile(e);
							}
                		}
	                }
                }
                
        		
        	}
        	
        });
        
        if(lastPos>0){
        	try {
				retryVideoWithnewUrl();
			} catch (IOException e1) {
				CrashHandler.logErrorToFile(e1);
			}
        }else{
        	videoUrlTest = proxyServer.getProxyUrl(videoUrlTest);
        	VideoDetailInfo info = new VideoDetailInfo();
        	info.title = videoTitle;
        	info.videoPath = videoUrlTest;
        	videoView.startPlayVideo(info);
        }
        baiduYunPlayVideo.setOnClickListener(new OnClickListener() {
        	
        	@Override
        	public void onClick(View v) {
        		if(baiduYunUrl!=null){
        			if(bgMusicPausedBySystem){
            			startBGMusic();
            			bgMusicPausedBySystem = false;
            		}
	        		Intent intent= new Intent();
				    intent.setAction("android.intent.action.VIEW");    
				    Uri content_url = Uri.parse(baiduYunUrl);   
				    intent.setData(content_url);  
				    startActivity(intent);
        		}else{
        			
        			MakeSureDialog dialog = new MakeSureDialog();
        			dialog.setTextTitle("������������");
        			if(mPlayer!=null){
        				SimpleDateFormat sdf1 = new SimpleDateFormat("HH:mm:ss");
        				String currentTime = sdf1.format(new Date(mPlayer.getCurrentPosition()-TimeZone.getDefault().getRawOffset()));
        				dialog.setTextTitle("��"+currentTime+"��������������");
        			}
        			dialog.setTextCancelButton("��ͷ����");
        			dialog.setTextCancelButton("��������");
        			dialog.setContent("��ȷ����������Զ����²���,�����Ȼ����,���ֶ������Ͻǲ˵�>>����>>�������,�ر�APP,���´�APP���š�ǿ�ҽ�����Ƶһ���Բ�����ϡ�ȷ����");
        			dialog.setDialogClickListener(new MakeSureDialog.onDialogClickListener() {  
        		    	   @Override  
        		    	   public void onSureClick() {
        		    		   //CacheFileUtil.writeCacheFile(getCacheDir().getAbsolutePath(), "playerexitpoint.dat", videoID);
//        		    		   handlingErrorSkipRecord = true;
        		    		   cleanCache();
        		    	   }
        		    	   @Override  
        		    	   public void onCancelClick() {
//        		    		   try {
//        		    			   String msg = videoID+"notakid"+String.valueOf(mPlayer.getCurrentPosition());
//	        		   				if(handlingErrorSkipRecord){
//	        		   					msg = "NOTREADY";
//	        		   				}
//	        		   				lastPos = mPlayer.getCurrentPosition();
//        		    			   CacheFileUtil.writeCacheFile(getCacheDir().getAbsolutePath(), "playerexitpoint.dat", msg);
//								retryVideoWithnewUrl();
//							} catch (IOException e) {
//								CrashHandler.logErrorToFile(e);
//							}
        		    	   }
        		    	   	
        		    	});
        			try{
	   	          		dialog.show(getFragmentManager(),"");
	   	          	}catch(Exception e1){
	   	          		CrashHandler.logErrorToFile(e1);
	   	          	} 
        			
        		}
        	}
        });
        
        buttonBuy.setOnClickListener(new OnClickListener() {
        	
        	@Override
        	public void onClick(View v) {
        		if(totalFreeChancesToday>=0){
        			buttonBuy.setText("���ҳ齱(�н���ѿ�)");
                     	if( loginUserName!=null){
                     			
         	            		double ran = Math.random();
         	            		if(ran<=0.05){
         	            			
         	            			 MakeSureDialog dialog = new MakeSureDialog();  
         		       	          	 dialog.setTextTitle("��ϲ��,������ëǮ!��");
         		       	          	 dialog.setTextSureButton("����ʹ��");
         		       	          	 dialog.setTextCancelButton("�� ��");
         		       	          	 dialog.setContent("["+Math.round(ran*100)+"]5%�Ļ��ᶼ����,����̫��,���������ѹۿ�����Ӱһ�졣5ëǮһ��������ʡ���ˡ�");
         		       	          	dialog.setDialogClickListener(new MakeSureDialog.onDialogClickListener() {  
         		       	          	   @Override
         		       	          	   public void onSureClick() {
         		       	          		   videosLuckyShakes.add(videoID);
         		       	          		   checkBoughtVideos();
         		       	          	   }  
         		       	          	   @Override  
         		       	          	   public void onCancelClick() { 
         		       	          	   }  
         		       	          	});  
         		       	          	dialog.setCancelable(false);
	         		       	        try{
	         		   	          		dialog.show(getFragmentManager(),"");
	         		   	          	}catch(Exception e1){
	         		   	          		CrashHandler.logErrorToFile(e1);
	         		   	          	}
         		       	          	
         		       	          	totalFreeChancesToday = -1;
         		       	          	String nowDateStr = new SimpleDateFormat("yyyy-MM-dd").format(new Date());
         		       	          	
         		       	          	File fPrefered = new File(Environment.getExternalStorageDirectory().getAbsolutePath()+"/"+Environment.getExternalStorageDirectory().getAbsolutePath()+"/FiveMao");
         		       	          	String folderPath = getCacheDir().getAbsolutePath();
         		       	          	if(Environment.getExternalStorageDirectory().exists()){
         		       	          		folderPath = fPrefered.getAbsolutePath();
         		       	          	}
		       	          		    CacheFileUtil.writeCacheFile(folderPath, "luck.shake",nowDateStr+"notakid"+videoID+"notakid"+totalFreeChancesToday+"notakid"+loginUserName);
		       	          		   
         	            		}else{
         	            			
         	            			MakeSureDialog dialog = new MakeSureDialog();  
         	            			if(totalFreeChancesToday==0){
         	            				dialog.setTextTitle("û��,û�л�����");
         			       	          	 dialog.setContent("["+Math.round(ran*100)+"]ÿ�춼�г齱����,�������԰ɡ�5%�Ļ�����ѿ�Ŷ���������ϲ���ⲿ��Ӱ,��5ëǮ�����������,����һ�������ļ۸�,ûʲôѹ����");
         			       	          	 totalFreeChancesToday = -1;
         	            			}else{
        	 		       	          	 dialog.setTextTitle("û��,����һ��");
        	 		       	          	 dialog.setContent("["+Math.round(ran*100)+"]���컹��"+totalFreeChancesToday+"�λ���,�����Կ��ɡ�5%�Ļ�����ѿ�Ŷ���������ϲ���ⲿ��Ӱ,��5ëǮ�����������,����һ�������ļ۸�,ûʲôѹ����");
         	            			}
         		       	          	dialog.setDialogClickListener(new MakeSureDialog.onDialogClickListener() {  
         		       	          	   @Override
         		       	          	   public void onSureClick() {
         		       	          		
         		       	          	   }  
         		       	          	   @Override  
         		       	          	   public void onCancelClick() { 
         		       	          		
         		       	          	   }  
         		       	          	});  
         		       	          	dialog.setCancelable(false);
	         		       	        try{
	         		   	          		dialog.show(getFragmentManager(),"");
	         		   	          	}catch(Exception e1){
	         		   	          		CrashHandler.logErrorToFile(e1);
	         		   	          	}
         		       	          	
         		       	          	totalFreeChancesToday = totalFreeChancesToday - 1;
	         		       	        String nowDateStr = new SimpleDateFormat("yyyy-MM-dd").format(new Date());
		         		       	    
	         		       	    File fPrefered = new File(Environment.getExternalStorageDirectory().getAbsolutePath()+"/"+Environment.getExternalStorageDirectory().getAbsolutePath()+"/FiveMao");
	     		       	          	String folderPath = getCacheDir().getAbsolutePath();
	     		       	          	if(Environment.getExternalStorageDirectory().exists()){
	     		       	          		folderPath = fPrefered.getAbsolutePath();
	     		       	          	}
		       	          		    CacheFileUtil.writeCacheFile(folderPath, "luck.shake",nowDateStr+"notakid-9999999notakid"+totalFreeChancesToday+"notakid"+loginUserName);
         	            		}
         	            		
         	            		
         	            	}
                     	
                     	
        		}else{
        			buttonBuy.setText(Html.fromHtml("����:<font color='gray'>ԭ��:2Ԫ</font> <font color='white'>�ּ�:0.5Ԫ</font>"));
	        		videoView.pausePlayer();
	        		goToBuyPageWithInfo();
        		}
        	}
        });
        
        
        Animation shake = AnimationUtils.loadAnimation(VideoPlayerActivity.this, R.anim.shake);
        buttonBuy.startAnimation(shake);
        
        handler.postDelayed(run, 1000);
        
    }
    
    private int getStopPosLastPlay(){
    	String lastStop = CacheFileUtil.readCacheFile(getCacheDir().getAbsolutePath(), "playerexitpoint.dat");
		if(lastStop!=null && lastStop.contains("notakid")){
			String[] eees = lastStop.split("notakid");
			String vid = eees[0];
			String pos = eees[1];
			if(vid.equals(videoID)){
				try{
					Integer posInt = Integer.parseInt(pos);
					return posInt;
				}catch(Exception e){
				}
			}
		}
		return -2;
		
    }
    protected void addFilmHotnessOnce() {
    	HttpsGetThread thread = new HttpsGetThread(new Handler() { 
            @Override  
            public void handleMessage(Message msg) {  
                super.handleMessage(msg);  
                String result = (String) msg.obj;  
                //etShowInfo.setText(result+":"+url);
                switch (msg.what) {  
                case 200:  
                	try {
                		if(result==null || result.length()==0){
                		}else{
                			JSONObject json = new JSONObject(result);
    						String status = json.getString("status");
    						String filmIdStr = json.getString("message");
    						if(status!=null && status.contains("Fail")){
    						}else if(status!=null && status.contains("Success")){
    							Toast.makeText( VideoPlayerActivity.this, "�ȶ�+1", Toast.LENGTH_SHORT).show();
    						}
    						
                		}
						
					} catch (JSONException e) {
						CrashHandler.logErrorToFile(e);
					}
                    break;  
                case 404:  
                    // ����ʧ��  
                    break;  
                }  
  
            }  
        },String.valueOf(PropertiesUtilWithEnc.prop.getProperty("domainCall"))+"/clientApp/addfilmhotness/"+videoID+"/", 200);  
        thread.start();
	}

	@Override
    protected void cleanCache(){
    	try {
			
			int count = Utils.cleanVideoCacheDir(this);
			MakeSureDialog dialog = new MakeSureDialog();
			dialog.setTextTitle("��Ƶ��������");
			dialog.setContent("���ƺ���������������,���������ļ�"+count+"������ȷ���������²��š�");
			dialog.setDialogClickListener(new MakeSureDialog.onDialogClickListener() {  
		    	   @Override  
		    	   public void onSureClick() {
		    		   onBackPressed();
//		    		   Intent intent=new Intent();
//			           intent.setClass(VideoPlayerActivity.this, MainActivity.class);  
//			           startActivity(intent);
		    	   }
		    	   @Override  
		    	   public void onCancelClick() {}
		    	   	
		    	});
				try{
	          		dialog.show(getFragmentManager(),"");
	          	}catch(Exception e1){
	          		CrashHandler.logErrorToFile(e1);
	          	}
		} catch (IOException e) {
			CrashHandler.logErrorToFile(e);
		}
    }

    private Bitmap createVideoThumbnail(String url, int width, int height) {
        Bitmap bitmap = null;
        MediaMetadataRetriever retriever = new MediaMetadataRetriever();
        int kind = MediaStore.Video.Thumbnails.MINI_KIND;
        try {
            if (Build.VERSION.SDK_INT >= 14) {
                retriever.setDataSource(url, new HashMap<String, String>());
            } else {
                retriever.setDataSource(url);
            }
            bitmap = retriever.getFrameAtTime();
        } catch (IllegalArgumentException ex) {
            // Assume this is a corrupt video file
        } catch (RuntimeException ex) {
            // Assume this is a corrupt video file.
        } finally {
            try {
                retriever.release();
            } catch (RuntimeException ex) {
                // Ignore failures while cleaning up.
            }
        }
        if (kind == Images.Thumbnails.MICRO_KIND && bitmap != null) {
            bitmap = ThumbnailUtils.extractThumbnail(bitmap, width, height,
                    ThumbnailUtils.OPTIONS_RECYCLE_INPUT);
        }
        return bitmap;
    }
    
    private Handler handler = new Handler();
    
    private Runnable run = new Runnable(){
    	int currentPosition;
		public void run() {
            // ��õ�ǰ����ʱ��͵�ǰ��Ƶ�ĳ���
				currentPosition = videoView.getCurrentPosition();
				
                if(videosBought.contains(videoID) || videosLuckyShakes.contains(videoID)
                		|| videosFreebies.contains(videoID) ||membershipExpiryDate!=null ){
                	//���������棬������ʾ���ٶ������ӡ�
                }else{
                    if(currentPosition>1000*60*freeMinutes){
                    	videoView.stopVideoPlayer();
                    	
                    	//Toast.makeText( VideoPlayerActivity.this, "�Բ�������δ�����ӰƬ,ֻ����Ѳ���ǰ"+freeMinutes+"����!", Toast.LENGTH_LONG).show();
        	            MakeSureDialog dialog = new MakeSureDialog();  
        	          	 
        	          	 dialog.setTextTitle("����ֹͣ��ʾ");
        	          	 dialog.setTextSureButton("���Ϲ���");
        	          	 dialog.setTextCancelButton("֪����");
        	          	 dialog.setContent("�Բ�������δ�����ӰƬ,ֻ����Ѳ���ǰ"+freeMinutes+"����! ��5ëȥ����������?");
        	          	 
        	          	dialog.setDialogClickListener(new MakeSureDialog.onDialogClickListener() {  
        	          	   @Override  
        	          	   public void onSureClick() {
        	          		 goToBuyPageWithInfo();
        	          	   }  
        	          	   @Override  
        	          	   public void onCancelClick() { 
        	          		   
        	          		Toast.makeText(VideoPlayerActivity.this, "������ؼ�������һ����", Toast.LENGTH_LONG).show();
        	          		 Configuration config = getResources().getConfiguration();
        	                 //�����ǰ�Ǻ���
        	                 if(config.orientation == Configuration.ORIENTATION_LANDSCAPE )
        	                 {
        	                     //��Ϊ����
        	                 	VideoPlayerActivity.this.setRequestedOrientation(
        	                             ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        	                     );
        	                 }
        	          	   }  
        	          	});
        	          	dialog.setCancelable(false);
        	          	try{
    	   	          		dialog.show(getFragmentManager(),"");
    	   	          	}catch(Exception e1){
    	   	          		CrashHandler.logErrorToFile(e1);
    	   	          	}
        	            
                    }
                }
            
            handler.postDelayed(run, 1000);
        }
    };
    

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        for(int i = 0; i < menu.size();i ++){
        	MenuItem item = menu.getItem(i);
        	if(item.getItemId()==R.id.action_recFilmToFriends){
        		item.setVisible(true);
        		break;
        	}
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
    	int id = item.getItemId();
        if(id==R.id.action_recFilmToFriends){
        	gobackToDescPageAndExecuteMenuAction();
        }
        return super.onOptionsItemSelected(item);
    }

    private void gobackToDescPageAndExecuteMenuAction() {
		mustBack2Previous = true;
		FlagsUtil.videoDescActivtyExecuteShareFilmToFriendAction = true;
		onBackPressed();
	}
    
        private void doubleClicked(View v) {
			Configuration config = getResources().getConfiguration();
            //�����ǰ�Ǻ���
            if(config.orientation == Configuration.ORIENTATION_LANDSCAPE )
            {
            	if(videoView.isLock()){
            		Toast.makeText(VideoPlayerActivity.this, "��Ļ�Ѿ�����,���Ƚ��������", Toast.LENGTH_SHORT).show();
            	}else{
	                //��Ϊ����
	            	VideoPlayerActivity.this.setRequestedOrientation(
	                        ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
	                );
            	}
            }
            //�����ǰ������
            if(config.orientation == Configuration.ORIENTATION_PORTRAIT )
            {
                //��Ϊ����
            	VideoPlayerActivity.this.setRequestedOrientation(
                        ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE );
            }
			
		}
        public void loadFilmsAuthed() {
            
            HttpsGetThread thread = new HttpsGetThread(new Handler() { 
                @Override  
                public void handleMessage(Message msg) {  
                    super.handleMessage(msg);  
                    String result = (String) msg.obj;  
                    //etShowInfo.setText(result+":"+url);
                    switch (msg.what) {  
                    case 200:  
                    	try {
                    		if(result==null || result.length()==0){
                    		}else{
                    			JSONObject json = new JSONObject(result);
        						String status = json.getString("status");
        						String filmIdStr = json.getString("message");
        						if(status!=null && status.contains("Fail")){
        							//Toast.makeText( VideoPlayerActivity.this, "�ѹ���ĵ�Ӱ�б����ʧ��,���Ժ����ԡ�", Toast.LENGTH_LONG).show();
        						}else if(status!=null && status.contains("Success")){
        							if(filmIdStr!=null && filmIdStr.length()>0){
        								String[] filmIDs = filmIdStr.split(",");
        								if(filmIDs.length>0){
        									for(String item:filmIDs){
        										videosBought.add(item.trim());
        									}
        								}
        								checkBoughtVideos();
        							}
        						}
                    		}
    						
    					} catch (JSONException e) {
    						CrashHandler.logErrorToFile(e);
    					}
                        break;  
                    case 404:  
                        // ����ʧ��  
                        Log.e("TAG", "����ʧ��!");  
                        break;  
                    }  
      
                }  
            },String.valueOf(PropertiesUtilWithEnc.prop.getProperty("domainCall"))+"/user/json/getAccessItems/"+loginUserName, 200);  
            thread.start();
        }   
       @Override
       public void onConfigurationChanged(Configuration newConfig) {
       	   super.onConfigurationChanged(newConfig);
       	   
          if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
              hideOtherUIs();
          } else if (newConfig.orientation == Configuration.ORIENTATION_PORTRAIT){
              showOtherUIs();
              checkBoughtVideos();
          }
       }

   	private void hideOtherUIs() {
   		videoBaiduYunDesc.setVisibility(View.GONE);
   		playerVideoName.setVisibility(View.GONE);
   		playerVideoNote.setVisibility(View.GONE);
   		videoPlayWholePage.setBackgroundColor(Color.BLACK);
   		buttonBuy.setVisibility(View.GONE);
   		baiduYunPlayVideo.setVisibility(View.GONE);
   		getActionBar().hide();
   	}
   	
   	private void showOtherUIs() {
   		videoBaiduYunDesc.setVisibility(View.VISIBLE);
   		playerVideoName.setVisibility(View.VISIBLE);
   		playerVideoNote.setVisibility(View.VISIBLE);
   		videoPlayWholePage.setBackgroundColor(Color.WHITE);
   		buttonBuy.setVisibility(View.VISIBLE);
   		baiduYunPlayVideo.setVisibility(View.VISIBLE);
   		getActionBar().show();
   	}

	private void checkBoughtVideos() {
		playerVideoName.setText("�Կ�"+freeMinutes+"����\n��"+videoTitle+"��");
        if(VideoPlayerActivity.videosFreebies.contains(String.valueOf(videoID))){
        	playerVideoName.setText("��ѿ�:��"+videoTitle+"��");
        }
        if(VideoPlayerActivity.videosBought.contains(String.valueOf(videoID))){
        	playerVideoName.setText("�ѹ���:��"+videoTitle+"��");
        }
        if(membershipExpiryDate!=null){
			playerVideoName.setText("һ�ջ�Ա:��"+videoTitle+"��");
		}
        if(VideoPlayerActivity.videosLuckyShakes.contains(String.valueOf(videoID))){
        	playerVideoName.setText("����һ��:��"+videoTitle+"��");
        }
		if(videosBought.contains(videoID) || videosLuckyShakes.contains(videoID)
        		|| videosFreebies.contains(videoID) || membershipExpiryDate!=null){
			//Toast.makeText( VideoPlayerActivity.this, "�õ�Ӱ�ѹ���", Toast.LENGTH_LONG).show();
			buttonBuy.setVisibility(View.GONE);
			baiduYunPlayVideo.setVisibility(View.VISIBLE);
			videoBaiduYunDesc.setText("ӰƬ����:\n"+videoDescription);
		}else{
			buttonBuy.setVisibility(View.VISIBLE);
			baiduYunPlayVideo.setVisibility(View.GONE);
			Toast.makeText( VideoPlayerActivity.this, "�õ�Ӱ��δ����,ֻ���Կ���", Toast.LENGTH_LONG).show();
		}
		
		
	}
	
	private boolean loadLuckyFilms() {
		 
		File fPrefered = new File(Environment.getExternalStorageDirectory().getAbsolutePath()+"/"+Environment.getExternalStorageDirectory().getAbsolutePath()+"/FiveMao");
		
      	String folderPath = getCacheDir().getAbsolutePath();
      	if(Environment.getExternalStorageDirectory().exists()){
      		folderPath = fPrefered.getAbsolutePath();
      	}
          	
		String lucksSS = CacheFileUtil.readCacheFile(folderPath, "luck.shake");
		if(lucksSS!=null){
			 String[] luckes = lucksSS.split("notakid");
			 if(luckes!=null && luckes.length>=4){
				 String dateStr = luckes[0];
				 String filmID = luckes[1];
				 String leftCount = luckes[2];
				 String unameFromFile = luckes[3];
				 
				 String nowDateStr = new SimpleDateFormat("yyyy-MM-dd").format(new Date());
				 //&& unameFromFile.equals(MeActivity.loginUserName) һ���ֻ�һ���޶�����
				 if(nowDateStr.trim().equals(dateStr.trim())&&loginUserName!=null ){
					 VideoPlayerActivity.totalFreeChancesToday = Integer.parseInt(leftCount);
					 VideoPlayerActivity.videosLuckyShakes.add(filmID);
					 return true;
				 }
			 }
		}
		
		if(totalFreeChancesToday>=0){
			buttonBuy.setText("���ҳ齱(�н���ѿ�)");
		}else{
			buttonBuy.setText(Html.fromHtml("����:<font color='gray'>ԭ��:2Ԫ</font> <font color='white'>�ּ�:0.5Ԫ</font>"));
		}
		
		return false;
		
	}
	
	@Override
	protected void onPause() {
		try{
			if(mPlayer!=null){
				if(mPlayer.isPlaying()){
					mPlayer.pause();
					String msg = videoID+"notakid"+String.valueOf(mPlayer.getCurrentPosition());
	//				if(handlingErrorSkipRecord){
	//					msg = "NOTREADY";
	//				}
					CacheFileUtil.writeCacheFile(getCacheDir().getAbsolutePath(), "playerexitpoint.dat", msg);
					previesPlay = true;
				}
			}
		}catch(Exception e){
			
		}
		super.onPause();
	};
	
	
	@Override
	protected void onResume() {
		if(MusicService.isplaying){
			pauseBGMusicBySystem();
			bgMusicPausedBySystem = true;
		}
		
		try{
			if(mPlayer!=null && previesPlay && !mPlayer.isPlaying()){
				mPlayer.start();
				previesPlay = false;
			}
		}catch(Exception e){
			try{
				mPlayer.start();
				previesPlay = false;
			}catch(Exception e1){
				
			}
		}
		if(totalFreeChancesToday>=0){
			buttonBuy.setText("���ҳ齱(�н���ѿ�)");
		}else{
			buttonBuy.setText(Html.fromHtml("����:<font color='gray'>ԭ��:2Ԫ</font> <font color='white'>�ּ�:0.5Ԫ</font>"));
		}
		super.onResume();
	}
	

	@Override
	public void onBackPressed() {
		Configuration config = getResources().getConfiguration();
        //�����ǰ�Ǻ���
        if(config.orientation == Configuration.ORIENTATION_LANDSCAPE && !mustBack2Previous)
        {
        	if(videoView.isLock()){
        		Toast.makeText(VideoPlayerActivity.this, "��Ļ�Ѿ�����,���Ƚ��������", Toast.LENGTH_SHORT).show();
        	}else{
        		VideoPlayerActivity.this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        	}
        }else{
        	
        	proxyServer=null;
        	popedOnceWhenBufferingDone = false;
        	freeMinutes = 6; 
        	totalFreeChancesToday = 2;
        	videosBought.clear();
        	videosLuckyShakes.clear();
        	videoUrl =null;
        	videoID =null;
        	videoTitle=null;
        	videoFileLink=null;
        	videoDescription=null;
        	baiduYunUrl=null;
        	if(bgMusicPausedBySystem){
    			startBGMusic();
    			bgMusicPausedBySystem = false;
    		}
            super.onBackPressed();
        }
        
	}
	
	@Override
	protected void onStop() {
//		videoView.stopVideoPlayer();
		super.onStop();
	}
	
	@Override
	protected void onDestroy() {
		videoView.stopVideoPlayer();
		super.onDestroy();
	}
	
	
	@Override
	protected void onStart() {
		if(totalFreeChancesToday>=0){
			buttonBuy.setText("���ҳ齱(�н���ѿ�)");
		}else{
			buttonBuy.setText(Html.fromHtml("����:<font color='gray'>ԭ��:2Ԫ</font> <font color='white'>�ּ�:0.5Ԫ</font>"));
		}
		super.onStart();
	}
	
	private void goToBuyPageWithInfo() {
		if(bgMusicPausedBySystem){
			startBGMusic();
			bgMusicPausedBySystem = false;
		}
		Intent intent=new Intent();
		intent.putExtra("id", videoID);
		intent.putExtra("title", videoTitle);
		intent.putExtra("description", videoDescription);
		intent.putExtra("videoFileLink", videoFileLink);
		
		//setClass�����ĵ�һ��������һ��Context����  
		//Context��һ����,Activity��Context�������,Ҳ����˵,���е�Activity���󶼿�������ת��ΪContext����  
		//setClass�����ĵڶ���������Class����,�ڵ�ǰ������,Ӧ�ô�����Ҫ��������Activity��class����  
		intent.setClass(VideoPlayerActivity.this,VideoBuyActivity.class);  
		
		
		startActivity(intent);
	}
	
	@Override
	protected void shareToWechatAction() {
		super.shareToWechatAction();
		 ClipboardManager cm = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
		 cm.setText("��"+videoTitle+"���Ѿ�����,��ӭ�ۿ���ע���û�ÿ��齱������ѿ���APP���������/��װ��ַ:"+String.valueOf(PropertiesUtilWithEnc.prop.getProperty("domainCall"))+"/resources/ui/pages/wumaoDownload.jsp?tag=��ëӰ��APP,������ַʹ��ϵͳ�����(�Ƽ��ȸ�Chrome�����)��,��Ҫֱ����΢��/QQ����򿪡�");
	    
		 MakeSureDialog dialog = new MakeSureDialog();  
		 dialog.setTextTitle("����APP������");
		 dialog.setTextSureButton("΢�ŷ���");
		 dialog.setTextCancelButton("QQ����");
		 dialog.setContent(videoTitle+"��ӭ�����տ���ӰƬ��APP��Ϣ�ѱ����Ƶ����а�,���Է����������(���ް�׿)��");
		 
		dialog.setDialogClickListener(new MakeSureDialog.onDialogClickListener() {  
		   @Override  
		   public void onSureClick() {
		   	try{
		    		Intent intent = new Intent();
						ComponentName cmp = new ComponentName("com.tencent.mm","com.tencent.mm.ui.LauncherUI");
						intent.setAction(Intent.ACTION_MAIN);
						intent.addCategory(Intent.CATEGORY_LAUNCHER);
						intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
						intent.setComponent(cmp);
						startActivityForResult(intent, 0);
		   	} catch (Exception e) {
		      		MakeSureDialog dialog = new MakeSureDialog();  
		      		dialog.setContent("�����ֻ�δ��װ΢��,���ߴ�ʧ��,���Ȱ�װ���ֶ��򿪡�");  
		           try{
	   	          		dialog.show(getFragmentManager(),"");
	   	          	}catch(Exception e1){
	   	          		CrashHandler.logErrorToFile(e);
	   	          	}
		      	}
		   }  
		   @Override  
		   public void onCancelClick() {  
		   	try{
		    		Intent intent = new Intent();
		    		ComponentName cmp = new ComponentName("com.tencent.mobileqq","com.tencent.mobileqq.activity.SplashActivity");
						intent.setAction(Intent.ACTION_MAIN);
						intent.addCategory(Intent.CATEGORY_LAUNCHER);
						intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
						intent.setComponent(cmp);
						startActivityForResult(intent, 0);
		   	} catch (Exception e) {
		      		MakeSureDialog dialog = new MakeSureDialog();  
		      		dialog.setContent("�����ֻ�δ��װQQ,���ߴ�ʧ��,���Ȱ�װ���ֶ��򿪡�");  
		           try{
	   	          		dialog.show(getFragmentManager(),"");
	   	          	}catch(Exception e1){
	   	          		CrashHandler.logErrorToFile(e);
	   	          	}
		      	}
		   	
		   }  
		});  
		try{
     		dialog.show(getFragmentManager(),"");
     	}catch(Exception e1){
     		CrashHandler.logErrorToFile(e1);
     	}

	}

	private void retryVideoWithnewUrl() throws IOException {
		videoView.stopVideoPlayer();
		VideoDetailInfo info = new VideoDetailInfo();
		info.title = videoTitle;
		info.videoPath = proxyServer.getProxyUrl(videoUrl+"?randome="+Math.random()*100);
		videoView.startPlayVideo(info);
	}
}
