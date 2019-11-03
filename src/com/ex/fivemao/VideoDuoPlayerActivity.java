package com.ex.fivemao;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TimeZone;
import java.util.TreeMap;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.ActionBar;
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
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.boredream.bdvideoplayer.BDVideoSplitView;
import com.boredream.bdvideoplayer.listener.SimpleOnVideoControlListener;
import com.boredream.bdvideoplayer.listener.SimplePlayerCallback;
import com.boredream.bdvideoplayer.utils.DisplayUtils;
import com.boredream.bdvideoplayer.utils.NetworkUtils;
import com.danikula.videocache.MyHttpProxyCacheServer;
import com.ex.fivemao.audioservice.MusicService;
import com.ex.fivemao.exception.CrashHandler;
import com.ex.fivemao.io.CacheFileUtil;
import com.ex.fivemao.io.PropertiesUtilWithEnc;
import com.ex.fivemao.ui.MakeSureDialog;
import com.ex.fivemao.utils.FlagsUtil;
import com.example.myvideos.http.HttpsGetThread;
import com.example.myvideos.http.proxy.App;
import com.example.myvideos.http.proxy.Utils;

/*查看重播功能，缓存功能，续播问题
 * onprepared->onbufferupdate
 * onpause write to file
 * seek to stopped pos
 * retry with fastforward
 * 
 */

public class VideoDuoPlayerActivity extends BaseActivity {
	private static MyHttpProxyCacheServer proxyServer;
	public static boolean popedOnceWhenBufferingDone = false;
	private static long freeMinutes = 15; 
	public static int totalFreeChancesToday = 3;
	public static Set<String> videosBought = new HashSet<String>();
	public static Set<String> videosLuckyShakes = new HashSet<String>();
	public static Set<String> videosFreebies = new HashSet<String>();
	public List<String> videoUrls = new ArrayList<String>();
	List<String> proxyUrls = new ArrayList<String>();
	public static int currentVideoFileIdx = 0;
	public static int lastPos = 0;
	public static String videoID ;
	public static String videoTitle;
	public static String videoFileLink;
	public static String videoType;
	public static String videoDescription;
	public static String videoCoverpageImageLink;
	public static Integer minimumAge;
	
	private BDVideoSplitView videoView ;
	private LinearLayout videoPlayWholePage;
	private Button buttonBuy;
	private Button buyOneDayMembershipX;
	private TextView videoBaiduYunDesc;
	private TextView videoPlayingHint;
	private TextView playerVideoName;
	private TextView playerVideoNote;
	boolean displayedOnceBuy = false;
	boolean whopageWhite = true;
	boolean stopUpdateThread = false;
	boolean lastDragDone = false;
	protected boolean cacheFlag = false;
	private boolean agreeOnMobilePlay = false;
    
	private int secondThreadRunCount = 0;
	private boolean bgMusicPausedBySystem = false;
	protected boolean mustBack2Previous = false;
	
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        if(MusicService.isplaying){
	        pauseBGMusicBySystem();
			bgMusicPausedBySystem = true;
        }
        
        setContentView(R.layout.activity_sub_videoplay);
        buttonBuy = (Button)findViewById(R.id.buyVideo);
        buyOneDayMembershipX = (Button)findViewById(R.id.buyOneDayMembershipX);
        videoBaiduYunDesc = (TextView)findViewById(R.id.videoBaiduYunDesc);
        videoPlayingHint = (TextView)findViewById(R.id.videoPlayingHint);
        playerVideoName = (TextView)findViewById(R.id.playerVideoName);
        playerVideoNote = (TextView)findViewById(R.id.playerVideoNote);
        videoPlayWholePage = (LinearLayout)findViewById(R.id.videoPlayWholePage);
        if(proxyServer==null){
        	proxyServer = App.getProxy(VideoDuoPlayerActivity.this);
        }
        
        Intent intent = getIntent();
        if(intent!=null){
	        videoID = intent.getStringExtra("id");
	        videoTitle = intent.getStringExtra("title");
	        videoDescription = intent.getStringExtra("description");
	        videoCoverpageImageLink = intent.getStringExtra("videoCoverpageImageLink");
	        videoFileLink = intent.getStringExtra("videoFileLink");
	        videoType = intent.getStringExtra("videoType");
	        String minimumAgeStr = intent.getStringExtra("minimumAge");
	        if(minimumAgeStr!=null){
	        	minimumAge = Integer.parseInt(minimumAgeStr);
	        }
        }
        
        
        videoPlayWholePage.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Configuration config = getResources().getConfiguration();
				if(config.orientation == Configuration.ORIENTATION_PORTRAIT )
		        {
					ActionBar actionBar = VideoDuoPlayerActivity.this.getActionBar();
					if(whopageWhite){
						invisibleOtherUIs();
						
						//actionBar.setDisplayHomeAsUpEnabled(false); 
						//actionBar.setHomeAsUpIndicator(null);
						//actionBar.setTitle("");
						buttonBuy.setVisibility(View.GONE);
						whopageWhite = false;
						Toast.makeText(VideoDuoPlayerActivity.this, "已经黑屏,单击屏幕换成白色背景。", Toast.LENGTH_SHORT).show();
					}else{
						//actionBar.setDisplayHomeAsUpEnabled(true); 
						//actionBar.setHomeAsUpIndicator(R.drawable.backarrow);  
						//actionBar.setTitle("视频播放");
						showOtherUIs();
			            checkBoughtVideos();
						whopageWhite = true;
					}
		        }
			}
		});
        //refreshUserServerData();
        
        
        videoView = (BDVideoSplitView)VideoDuoPlayerActivity.this.findViewById(R.id.vplayPlayer );
        videoView.setDuoPlayerActivity(this);
        
        buyOneDayMembershipX.setOnClickListener(new OnClickListener() {
        	@Override
        	public void onClick(View v) {
        		showSingleChoiceDialog();
        	}
        });
        
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
				lastPos = 0;
            	startPlay(currentVideoFileIdx);
            }

            @Override
            public void onBack() {
                onBackPressed();
            }

            @Override
            public void onFullScreen() {
                DisplayUtils.toggleScreenOrientation(VideoDuoPlayerActivity.this);
            }
            
        });
        
        videoView.addMediaPlayerCallback(new SimplePlayerCallback(){
        	@Override
    		public boolean onInfo(MediaPlayer mp, int what, int extra) {
    			if(what==MediaPlayer.MEDIA_INFO_VIDEO_RENDERING_START){
    				videoView.setBackgroundColor(Color.TRANSPARENT);
    				videoPlayingHint.setVisibility(View.GONE);
    			}
    			return true;
    		}
        	
        	@Override
        	public void onCompletion(MediaPlayer mp) {
        		
        	};
        	
        	@Override
        	public void onPrepared(MediaPlayer mp) {
        		
        	}

        	@Override
        	public void onError(MediaPlayer mp, int what, int extra) {
        		super.onError(mp, what, extra);
        	}
        	
        	@Override
        	public void onBufferingUpdate(MediaPlayer mp, int percent) {
        		
        		 // 获得当前播放时间和当前视频的长度
        		// 获得当前播放时间和当前视频的长度
                int currentPos = videoView.getCurrentPosition();
                int duration = videoView.getDuration();
                int currentBufferPos = (duration * percent / 100);
                int finalBufferingPos = currentPos>currentBufferPos?currentPos:currentBufferPos;
                

        		if(lastPos>0 ){
        			if(finalBufferingPos>lastPos){
        				int seekTo = (lastPos-5*1000);
        				if(seekTo<0){
        					seekTo = lastPos;
        				}
	        			mp.seekTo(seekTo);
	        			mp.start();
	        			Toast.makeText( VideoDuoPlayerActivity.this, "成功续播时间点", Toast.LENGTH_SHORT).show();
	        			lastPos = -1;
        			}else{
        				lastPos = -1;
        			}
        		}
        		
                SimpleDateFormat sdf1 = new SimpleDateFormat("HH:mm:ss");
                String timeStr = sdf1.format(new Date(finalBufferingPos-TimeZone.getDefault().getRawOffset()));
                 
                playerVideoNote.setText("左屏上下亮度 右屏上下声音  已缓冲:"+timeStr+"秒");
                
        	}

        	
        });
        
    	
        buttonBuy.setOnClickListener(new OnClickListener() {
        	
        	@Override
        	public void onClick(View v) {
        		buttonBuyClicked();
        	}
        });
        
        
        Animation shake = AnimationUtils.loadAnimation(VideoDuoPlayerActivity.this, R.anim.shake);
        buttonBuy.startAnimation(shake);
        
        handler.postDelayed(run, 1000);
        
    }

    int yourChoice;
    private void showSingleChoiceDialog(){
        final String[] items = { "一日会员/1.0元","一月会员/8.0元","三月会员/18.0元" };
        final int[] values = { 1,31,93 };
        yourChoice = 0;
        AlertDialog.Builder singleChoiceDialog = 
            new AlertDialog.Builder(VideoDuoPlayerActivity.this);
        singleChoiceDialog.setTitle("选择购买会员类型");
        // 第二个参数是默认选项，此处设置为0
        singleChoiceDialog.setSingleChoiceItems(items, yourChoice, 
            new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                yourChoice = which;
            }
        });
        singleChoiceDialog.setPositiveButton("确定", 
            new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (yourChoice != -1) {
        			Intent intent=new Intent();
        			intent.putExtra("membershipdays", values[yourChoice]);
        			intent.setClass(VideoDuoPlayerActivity.this,VideoBuyMembershipActivity.class);  
        			startActivity(intent);
        			dialog.dismiss();
//                	dialog
//                    Toast.makeText(MeBoughtMembershipActivity.this, 
//                    "你选择了" + items[yourChoice], 
//                    Toast.LENGTH_SHORT).show();
                }
            }
        });
        singleChoiceDialog.show();
    }
    
	private void refreshUserServerData() {
		getAllSplits(videoID);
        
        getCacheSettings();
        getStopPosLastPlay();
        checkAge("提示信息",true);
		if(loginUserName!=null){
        	videosBought.clear();
        	videosLuckyShakes.clear();
        	loadLuckyFilms();
        	loadFilmsAuthed();
        }else{
        	VideoDuoPlayerActivity.totalFreeChancesToday = -1;
        	
        	AlertDialog.Builder builder = new AlertDialog.Builder(this);  
	        builder.setIcon(android.R.drawable.ic_dialog_info);  
	        builder.setTitle("登录/抽奖 免费看？");  
	        builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {  
	            public void onClick(DialogInterface dialog, int which) { 
	            	gotoMePage();
	            }  
	        });  
	        builder.show();  
	        
        	//Toast.makeText( VideoDuoPlayerActivity.this, "未登陆,未购买,只能试看"+freeMinutes+"分钟。", Toast.LENGTH_LONG).show();
        }
        
        checkBoughtVideos();
	}
    
	private void checkAge(String title, boolean cancellable) {
		if(minimumAge>=18 && lockOpened){
			if(loginUserBirthday!=null){
				if(ageNotMeet()){
					blockUser(title,cancellable);
				}
			}else{
				blockUser(title,cancellable);
			}
		}
	}

	private void blockUser(String title,final boolean cancellable) {
		
		
		final MakeSureDialog dialog = new MakeSureDialog();  
	   	 dialog.setTextTitle(title);
	   	 dialog.setTextSureButton("确  定");
	   	 if(cancellable){
	   		 dialog.setTextCancelButton("仍然观看");
	   	 }else{
	   		dialog.setTextCancelButton("我知道了");
	   	 }
	   	 
	   	 dialog.setDrawableLeft(videoCoverpageImageLink);
	   	 dialog.setTextSizeContent(18);
	   	 dialog.setContent(videoTitle+"(限制级)\n" +
	   	 		"观看年龄:"+minimumAge+"+\n" +
	   	 		"您不适合观看此影片。");
	   	 
	   	dialog.setDialogClickListener(new MakeSureDialog.onDialogClickListener() {  
	   	   @Override  
	   	   public void onSureClick() {
	   		   mustBack2Previous = true;
	   		   onBackPressed();
	   	   }  
	   	   @Override  
	   	   public void onCancelClick() {  
	   		if(cancellable){
	   			dialog.dismiss();
		   	 }else{
		   		mustBack2Previous  = true;
		   		onBackPressed();
		   	 }
	   	   }
	   	   	
	   	});  
	   	try{
	   		dialog.setCancelable(false);
    		dialog.show(getFragmentManager(),"");
    	}catch(Exception e1){
    		CrashHandler.logErrorToFile(e1);
    	}
	   	
	}

	private boolean ageNotMeet() {
		if(loginUserBirthday!=null && minimumAge!=null){
			String[] birs = loginUserBirthday.split("-");
			if(birs!=null && birs.length>1){
				String yearStr = birs[0];
				Integer year = Integer.parseInt(yearStr);
				if((new Date().getYear()+1900-year)>=minimumAge){
					return false;
				}
			}
		}
		return true;
	}

    public void setLastDragDone(boolean done){
    	this.lastDragDone = done;
    }
    
    
    private void getAllSplits(String videoID2) {

    	
        Handler httpHandler = new Handler() { 
        	 
        	

			@Override  
            public void handleMessage(Message msg) {
                super.handleMessage(msg);  
                String result = (String) msg.obj;  
                
                switch (msg.what) {  
                case 200:  
                    // 请求成功  
                    try {
						JSONObject jsonObj = new JSONObject(result);
						if(jsonObj==null ||jsonObj.equals("")){
							videoPlayingHint.setText("无播放内容,请稍后再试。");
							return;
						}
						JSONArray videoSplitedFiles = jsonObj.getJSONArray("videoSplitedFiles");
						if(videoSplitedFiles.length()==0){
							videoPlayingHint.setText("无播放内容,请稍后再试。");
							return;
						}
						
						TreeMap<Integer,String> orderedUrlMap = new TreeMap<Integer,String>();
						for(int i =0; i < videoSplitedFiles.length();i++){
							String url = videoSplitedFiles.getString(i);
							if(url.contains("_0")){
								String simpleName = url.substring(url.lastIndexOf("/")+1);
								String[] nameParts = simpleName.split("\\.")[0].split("_");
								String orderStr = nameParts[nameParts.length-2];
								while(orderStr.startsWith("0")&&orderStr.length()>1){
									orderStr = orderStr.substring(1);
								}
								int order = Integer.parseInt(orderStr);
								orderedUrlMap.put(order, PropertiesUtilWithEnc.getString("domainCall")+url);
							}
						}
						videoUrls.addAll(orderedUrlMap.values());
						
						//Collections.sort(videoUrls);
						
						for(String url:videoUrls){
							if(cacheFlag){
								proxyUrls.add(proxyServer.getProxyUrl(url));
							}else{
								proxyUrls.add(url);
							}
						}
						
						
						if(videoUrls.size()>0){
							startPlay(currentVideoFileIdx);
						}else{
							videoPlayingHint.setText("对不起,加载电影失败,请稍后再试。");
						}
				        
					} catch (Exception e) {
						CrashHandler.logErrorToFile(e);
						videoPlayingHint.setText("对不起,加载电影失败,请稍后再试。");
					}
                    
                    break;  
                case 404:  
                    // 请求失败
                	videoPlayingHint.setText("对不起,加载电影失败,请稍后再试。");
                    break;  
                default:
                	videoPlayingHint.setText("对不起,加载电影失败,请稍后再试。");
                }  
            }

        }; 
        
        videoPlayingHint.setVisibility(View.VISIBLE);
        videoPlayingHint.setText("加载电影中...");
        if(videoUrls.size()>0){
        	videoUrls.clear();
        }
        HttpsGetThread thread = new HttpsGetThread(httpHandler,  
        		PropertiesUtilWithEnc.getString("domainCall")+"/film/getFolderFilms/"+videoID2+"/", 200);  
        thread.start();
	}
    
	protected void startPlay(final int startIdx) {
		if(!lockOpened){
			return;
		}
		if(cacheFlag){
			try {
				int count = Utils.cleanVideoCacheDir(this,new FilenameFilter() {
					@Override
					public boolean accept(File dir, String filename) {
						if(filename!=null && filename.endsWith(".download")){
							return true;
						}
						return false;
					}
				});
				if(count>0){
					Toast.makeText(this, "清除缓存临时文件"+count+"个", Toast.LENGTH_SHORT).show();
				}
			} catch (Exception e2) {
				CrashHandler.logErrorToFile(e2);
			}
		}
		
		if( NetworkUtils.isWifiConnected(getApplicationContext())||agreeOnMobilePlay){
			startFilm(startIdx);
		}else if(NetworkUtils.isMobileConnected(getApplicationContext())){
			final MakeSureDialog dialog = new MakeSureDialog();
			dialog.setTextTitle("网络提醒");
			dialog.setContent("您当前使用的是手机流量网络,继续播放电影吗？");
			dialog.setTextSureButton("继续播放");
			dialog.setTextCancelButton("停止播放");
			dialog.setDialogClickListener(new MakeSureDialog.onDialogClickListener() {  
		    	   @Override  
		    	   public void onSureClick() {
		    		   dialog.dismiss();
		    		   agreeOnMobilePlay = true;
		    		   startFilm(startIdx);
		    	   }
		    	   @Override  
		    	   public void onCancelClick() {
		    		   dialog.dismiss();
		    		   videoPlayingHint.setText("手机流量,停止播放电影。");
		    		   videoPlayingHint.setVisibility(View.VISIBLE);
		    		   lastDragDone = true;
		    	   }
		    	   	
		    	});
			dialog.setCancelable(false);
			
			try{
          		dialog.show(getFragmentManager(),"");
          	}catch(Exception e){
          		CrashHandler.logErrorToFile(e);
          	} 
		}
		
		
	}

	private void startFilm(int startIdx) {
		videoPlayingHint.setText("加载电影中...");
		stopUpdateThread = true;
		lastDragDone = false;
		videoView.startVideoSplits(videoTitle,startIdx, proxyUrls,videoUrls);
		stopUpdateThread = false;
	}

	private void getCacheSettings() {
		String cacheFileContent = CacheFileUtil.readCacheFile(getCacheDir().getAbsolutePath(), "playercache.duo.dat");
    	if(cacheFileContent!=null){
    		if(cacheFileContent.trim().equalsIgnoreCase("Y")){
    			cacheFlag = true;
    		}else{
    			cacheFlag = false;
    		}
    	}
	}

	Map<String,String> allPlayHistoryData = new HashMap<String,String>();
	private void getStopPosLastPlay(){
		allPlayHistoryData.clear();
		currentVideoFileIdx = 0;
    	String lastStopFile = CacheFileUtil.readCacheFile(getCacheDir().getAbsolutePath(), "playerexitpoint.duo.dat");
    	if(lastStopFile!=null){
	    	for(String lastStop:lastStopFile.split("\n")){
	    		if(lastStop!=null && lastStop.contains("notakid") ){
	    			String[] eees = lastStop.split("notakid");
	    			String vid = eees[0];
	    			String idx = eees[1];
	    			String pos = eees[2];
	    			allPlayHistoryData.put(vid, lastStop);
	    			
	    			if(vid.equals(videoID)){
	    				try{
	    					Integer idxInt = Integer.parseInt(idx);
	    					Integer posInt = Integer.parseInt(pos);
	    					
	    					currentVideoFileIdx = idxInt;
	    					lastPos = posInt;
	    					
	    				}catch(Exception e){
	    					Toast.makeText( VideoDuoPlayerActivity.this, "加载上次播放记录失败", Toast.LENGTH_SHORT).show();
	    				}
	    			}
	    		}
	    	}
    	}
		
    }
    public void addFilmHotnessOnce() {
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
//    							Toast.makeText( VideoDuoPlayerActivity.this, "热度+1", Toast.LENGTH_SHORT).show();
    						}
    						
                		}
						
					} catch (JSONException e) {
						CrashHandler.logErrorToFile(e);
					}
                    break;  
                case 404:  
                    // 请求失败  
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
			dialog.setTextTitle("视频重试提醒");
			dialog.setContent("您似乎遇到了网络问题,已清理缓存文件"+count+"个。点确定尝试重新播放。");
			dialog.setDialogClickListener(new MakeSureDialog.onDialogClickListener() {  
		    	   @Override  
		    	   public void onSureClick() {
		    		   onBackPressed();
//		    		   Intent intent=new Intent();
//			           intent.setClass(VideoDuoPlayerActivity.this, MainActivity.class);  
//			           startActivity(intent);
		    	   }
		    	   @Override  
		    	   public void onCancelClick() {}
		    	   	
		    	});
			try{
          		dialog.show(getFragmentManager(),"");
          	}catch(Exception e){
          		CrashHandler.logErrorToFile(e);
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
			secondThreadRunCount = secondThreadRunCount + 1;
			if(stopUpdateThread){
				return;
			}
			
			if(secondThreadRunCount==30){
				checkAge("再次提示",true);
			}else if(secondThreadRunCount==90){
				checkAge("再三提示",false);
			}
            // 获得当前播放时间和当前视频的长度
				currentPosition = videoView.getCurrentPosition();
				int countMinisecs = videoView.getPreviousTime(false);
				int currentRealPos = countMinisecs + currentPosition;
                
                if(videosBought.contains(videoID) || videosLuckyShakes.contains(videoID)
                		|| videosFreebies.contains(videoID) ||membershipExpiryDate!=null ){
                	//播放完整版，并且显示出百度云链接。
                }else{
                	
                    if(currentRealPos>1000*60*freeMinutes && !displayedOnceBuy){
                    	displayedOnceBuy  = true;
                    	videoView.stopVideoPlayer();
                    	
                    	//Toast.makeText( VideoPlayerActivity.this, "对不起，您还未购买该影片,只能免费播放前"+freeMinutes+"分钟!", Toast.LENGTH_LONG).show();
        	            MakeSureDialog dialog = new MakeSureDialog();  
        	          	 
        	          	 dialog.setTextTitle("试看结束提示");
        	          	 dialog.setTextSureButton("马上购买");
        	          	 dialog.setTextCancelButton("从头播放");
        	          	 dialog.setContent("对不起，您还未购买该影片,只能免费播放前"+freeMinutes+"分钟! 花5毛去购买完整版?");
        	          	 
        	          	dialog.setDialogClickListener(new MakeSureDialog.onDialogClickListener() {  
        	          	   @Override  
        	          	   public void onSureClick() {
        	          		 goToBuyPageWithInfo();
        	          	   }  
        	          	   @Override  
        	          	   public void onCancelClick() { 
        	          		 Configuration config = getResources().getConfiguration();
        	                 //如果当前是横屏
        	                 if(config.orientation == Configuration.ORIENTATION_LANDSCAPE )
        	                 {
        	                     //设为竖屏
        	                 	VideoDuoPlayerActivity.this.setRequestedOrientation(
        	                             ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        	                     );
        	                 }
        	                 
        	                currentVideoFileIdx = 0;
        	                savePlayPoint(0);
        	                
        	                mustBack2Previous = true;
        	                onBackPressed();
         	        		//Toast.makeText(VideoDuoPlayerActivity.this, "下次可以从头播放", Toast.LENGTH_SHORT).show();
        	          	   }  
        	          	});
        	          	dialog.setCancelable(false);
        	          	try{
        	          		dialog.show(getFragmentManager(),"");
        	          	}catch(Throwable e){
        	          		CrashHandler.logErrorToFile(e);
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
      //action_recToFriends
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
            //如果当前是横屏
            if(config.orientation == Configuration.ORIENTATION_LANDSCAPE )
            {
            	if(videoView.isLock()){
            		Toast.makeText(VideoDuoPlayerActivity.this, "屏幕已经锁定,请先解除锁定。", Toast.LENGTH_SHORT).show();
            	}else{
	                //设为竖屏
	            	VideoDuoPlayerActivity.this.setRequestedOrientation(
	                        ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
	                );
            	}
            }
            //如果当前是竖屏
            if(config.orientation == Configuration.ORIENTATION_PORTRAIT )
            {
                //设为横屏
            	VideoDuoPlayerActivity.this.setRequestedOrientation(
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
        							//Toast.makeText( VideoPlayerActivity.this, "已购买的电影列表加载失败,请稍后再试。", Toast.LENGTH_LONG).show();
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
                        // 请求失败  
                        Log.e("TAG", "请求失败!");  
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
   		buyOneDayMembershipX.setVisibility(View.GONE);
   		getActionBar().hide();
   	}
   	private void invisibleOtherUIs() {
   		videoBaiduYunDesc.setVisibility(View.INVISIBLE);
   		playerVideoName.setVisibility(View.INVISIBLE);
   		playerVideoNote.setVisibility(View.INVISIBLE);
   		videoPlayWholePage.setBackgroundColor(Color.BLACK);
   		buttonBuy.setVisibility(View.INVISIBLE);
   		buyOneDayMembershipX.setVisibility(View.INVISIBLE);
   	}
   	
   	private void showOtherUIs() {
   		videoBaiduYunDesc.setVisibility(View.VISIBLE);
   		playerVideoName.setVisibility(View.VISIBLE);
   		playerVideoNote.setVisibility(View.VISIBLE);
   		videoPlayWholePage.setBackgroundColor(Color.WHITE);
   		buttonBuy.setVisibility(View.VISIBLE);
   		buyOneDayMembershipX.setVisibility(View.VISIBLE);
   		getActionBar().show();
   	}

	private void checkBoughtVideos() {
		if(minimumAge!=null && minimumAge>=18){
			if(ageNotMeet()){
				freeMinutes = 5;
			}else{
				freeMinutes = 10;
			}
			playerVideoName.setText(Html.fromHtml("试看"+freeMinutes+"分钟<br>《"+videoTitle+"》<font color='blue'>(限制级)</font>"));
		}else{
			playerVideoName.setText(Html.fromHtml("试看"+freeMinutes+"分钟<br>《"+videoTitle+"》"));
		}
		
        if(VideoDuoPlayerActivity.videosFreebies.contains(String.valueOf(videoID))){
        	playerVideoName.setText(Html.fromHtml("免费看:《"+videoTitle+"》"));
        }
        if(VideoDuoPlayerActivity.videosBought.contains(String.valueOf(videoID))){
        	playerVideoName.setText(Html.fromHtml("已购买:《"+videoTitle+"》"));
        }
        if(membershipExpiryDate!=null){
			playerVideoName.setText(Html.fromHtml("一日会员:《"+videoTitle+"》"));
		}
        if(VideoDuoPlayerActivity.videosLuckyShakes.contains(String.valueOf(videoID))){
        	playerVideoName.setText(Html.fromHtml("幸运一天:《"+videoTitle+"》"));
        }
		if(videosBought.contains(videoID) || videosLuckyShakes.contains(videoID)
        		|| videosFreebies.contains(videoID) || membershipExpiryDate!=null){
			//Toast.makeText( VideoPlayerActivity.this, "该电影已购买", Toast.LENGTH_LONG).show();
			buttonBuy.setVisibility(View.GONE);
//			baiduYunPlayVideo.setVisibility(View.VISIBLE);
			videoBaiduYunDesc.setText("影片介绍:\n"+videoDescription);
		}else{
			videoBaiduYunDesc.setText("影片介绍:\n"+videoDescription);
			buttonBuy.setVisibility(View.VISIBLE);
		}
		
		if(membershipExpiryDate==null){
			buyOneDayMembershipX.setVisibility(View.VISIBLE);
		}else{
			buyOneDayMembershipX.setVisibility(View.GONE);
		}
		
	}
	
	private boolean loadLuckyFilms() {
		
		if(loginUserName==null){
			VideoDuoPlayerActivity.totalFreeChancesToday = -1;
			return false;
		}
		 
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
				 //&& unameFromFile.equals(MeActivity.loginUserName) 一个手机一天限定次数
				 if(nowDateStr.trim().equals(dateStr.trim())&&loginUserName!=null ){
					 VideoDuoPlayerActivity.totalFreeChancesToday = Integer.parseInt(leftCount);
					 VideoDuoPlayerActivity.videosLuckyShakes.add(filmID);
					 return true;
				 }
			 }
		}
		
		if(totalFreeChancesToday>=0){
			buttonBuy.setText("0.5元购买\n(20%概率免费看)");
		}else{
			buttonBuy.setText(Html.fromHtml("5毛永久完整版<br><font color='gray'>原价:2元</font> <font color='white'>现价:0.5元</font>"));
		}
		
		return false;
		
	}
	
	@Override
	protected void onPause() {
		super.onPause();
	}

	public void savePlayPoint(int fileIdxIfAny) {
		String msg = videoID+"notakid"+currentVideoFileIdx+"notakid0";
		if(fileIdxIfAny>=0){
			msg = videoID+"notakid"+fileIdxIfAny+"notakid0";
		}
		
		StringBuilder sb = new StringBuilder();
		for(String key:allPlayHistoryData.keySet()){
			sb.append(allPlayHistoryData.get(key)).append("\n");
		}
		sb.append(msg).append("\n");
		msg = sb.toString();
		CacheFileUtil.writeCacheFile(getCacheDir().getAbsolutePath(), "playerexitpoint.duo.dat", msg);
	};
	
	
	@Override
	protected void onResume() {
		if(MusicService.isplaying){
			pauseBGMusicBySystem();
			bgMusicPausedBySystem = true;
		}
		
		refreshUserServerData();
		
		
		if(totalFreeChancesToday>=0){
			buttonBuy.setText("0.5元购买\n(20%概率免费看)");
		}else{
			buttonBuy.setText(Html.fromHtml("5毛永久完整版<br><font color='gray'>原价:2元</font> <font color='white'>现价:0.5元</font>"));
		}
		displayedOnceBuy = false;
		super.onResume();
	}
	

	@Override
	public void onBackPressed() {
		Configuration config = getResources().getConfiguration();
        //如果当前是横屏
        if(config.orientation == Configuration.ORIENTATION_LANDSCAPE && !mustBack2Previous)
        {
        	if(videoView.isLock()){
        		Toast.makeText(VideoDuoPlayerActivity.this, "屏幕已经锁定,请先解除锁定。", Toast.LENGTH_SHORT).show();
        	}else{
        		VideoDuoPlayerActivity.this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        	}
        }else{
        	if(!lastDragDone && !mustBack2Previous){
        		Toast.makeText(VideoDuoPlayerActivity.this, "播放器还在初始化，退出会发生未知错误，按返回键强制返回。", Toast.LENGTH_SHORT).show();
        		lastDragDone = true;
        		return;
        	}
        	
        	popedOnceWhenBufferingDone = false;
        	displayedOnceBuy = false;
        	freeMinutes = 15; 
        	videosBought.clear();
        	videosLuckyShakes.clear();
        	videoUrls.clear();
        	videoID =null;
        	videoTitle=null;
        	videoFileLink=null;
        	videoDescription=null;
        	lastPos = 0;
//        	if(bgMusicPausedBySystem){
//    			startBGMusic();
//    			bgMusicPausedBySystem = false;
//    		}
            super.onBackPressed();
        }
        
	}
	
	@Override
	protected void onStop() {
//		if(bgMusicPausedBySystem){
//			startBGMusic();
//			bgMusicPausedBySystem = false;
//		}
		videoView.stopVideoPlayer();
		super.onStop();
		finish();
	}
	@Override
	protected void onDestroy() {
		super.onDestroy();
	}
	
	@Override
	protected void onStart() {
		if(totalFreeChancesToday>=0){
			buttonBuy.setText("0.5元购买\n(20%概率免费看)");
		}else{
			buttonBuy.setText(Html.fromHtml("5毛永久完整版<br><font color='gray'>原价:2元</font> <font color='white'>现价:0.5元</font>"));
		}
//		getStopPosLastPlay();
		super.onStart();
	}
	
	
	
	private void goToBuyPageWithInfo() {
//		String msg = videoID+"notakid"+currentVideoFileIdx+"notakid0";
//		CacheFileUtil.writeCacheFile(getCacheDir().getAbsolutePath(), "playerexitpoint.duo.dat", msg);
//		if(bgMusicPausedBySystem){
//			startBGMusic();
//			bgMusicPausedBySystem = false;
//		}
		
		Intent intent=new Intent();
		intent.putExtra("id", videoID);
		intent.putExtra("title", videoTitle);
		intent.putExtra("videoCoverpageImageLink", videoCoverpageImageLink);
		intent.putExtra("description", videoDescription);
		intent.putExtra("videoFileLink", videoFileLink);
		intent.putExtra("videoType", videoType);
		intent.putExtra("minimumAge", minimumAge);
		//setClass函数的第一个参数是一个Context对象  
		//Context是一个类,Activity是Context类的子类,也就是说,所有的Activity对象都可以向上转型为Context对象  
		//setClass函数的第二个参数是Class对象,在当前场景下,应该传入需要被启动的Activity的class对象  
		intent.setClass(VideoDuoPlayerActivity.this,VideoBuyActivity.class);  
		
		startActivity(intent);
	}
	
	@Override
	protected void shareToWechatAction() {
		super.shareToWechatAction();
		 ClipboardManager cm = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
		 cm.setText("《"+videoTitle+"》已经上线,欢迎观看。"+PropertiesUtilWithEnc.getString("shareWechatText"));
	    
		 MakeSureDialog dialog = new MakeSureDialog();  
		 dialog.setTextTitle("分享APP给朋友");
		 dialog.setTextSureButton("微信分享");
		 dialog.setTextCancelButton("QQ分享");
		 dialog.setContent("《"+videoTitle+"》欢迎您的收看。影片信息已被复制到剪切板(仅限安卓)。");
		 	
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
		      		dialog.setContent("您的手机未安装微信,或者打开失败,请先安装或手动打开。");  
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
		      		dialog.setContent("您的手机未安装QQ,或者打开失败,请先安装或手动打开。");  
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
      	}catch(Exception e){
      		CrashHandler.logErrorToFile(e);
      	}

	}

	


	public void dragtoSplitTimePoint(int minisecsInput) {
		try{
			dragToTimePoint(minisecsInput);
		}catch(Exception e){
			CrashHandler.logErrorToFile(e);
		}
	}

	private void dragToTimePoint(int minisecsInput) {
		
		//试看时不能往后拖过免费时间
		if(videosBought.contains(videoID) || videosLuckyShakes.contains(videoID)
        		|| videosFreebies.contains(videoID) ||membershipExpiryDate!=null ){
        }else{
        	if(minisecsInput>1000*60*freeMinutes){
        		Toast.makeText(VideoDuoPlayerActivity.this, "您只能试看"+freeMinutes+"分钟,不可播放到"+(minisecsInput/1000/60)+"分钟。", Toast.LENGTH_SHORT).show();
        		return;
        	}
        }
		//一个拖动还没完成，不能进行下一次拖动
		if(!lastDragDone){
			Toast.makeText(VideoDuoPlayerActivity.this, "上次拖动完成前不能再进行第二次拖动", Toast.LENGTH_SHORT).show();
			return;
		}
		
		int countMinisecs = 0;
		for(int i = 0; i <videoUrls.size(); i++){
			String url = videoUrls.get(i);
			String simpleName = url.substring(url.lastIndexOf("/")+1);
			String[] nameParts = simpleName.split("\\.")[0].split("_");
			int minisecs = Integer.parseInt(nameParts[nameParts.length-1]);
			countMinisecs = countMinisecs+minisecs;
			if(countMinisecs>minisecsInput){
				currentVideoFileIdx = i;
				lastPos = 0;
				videoView.setVideoTitle(videoTitle+"("+(currentVideoFileIdx+1)+")");//descritive start from 1, not 0
				startPlay(currentVideoFileIdx);
				break;
			}
		}
	}

	private void checkMyLuckOnce() {
		if( loginUserName!=null){
			
				double ran = Math.random();
				if(ran<=0.05){
					 MakeSureDialog dialog = new MakeSureDialog(); 
					 
					 dialog.setDrawableLeft(videoCoverpageImageLink);
		          	 dialog.setTextTitle("恭喜你：抽奖中了!!!");
		          	 dialog.setTextSureButton("确 认");
		          	 dialog.setTextCancelButton("取消");
		          	 dialog.setContent("["+Math.round(ran*100)+"]每天累计20%的机会,可以免费观看一天《"+videoTitle+"》。点【确认-马上播放】观看完整版。");
		          	dialog.setDialogClickListener(new MakeSureDialog.onDialogClickListener() {  
		          	   @Override
		          	   public void onSureClick() {
		          		   videosLuckyShakes.add(videoID);
		          		   checkBoughtVideos();
		          		   mustBack2Previous = true;
		          		   onBackPressed();
		          	   }  
		          	   @Override  
		          	   public void onCancelClick() { 
//		          		  videosLuckyShakes.add(videoID);
//		          		   checkBoughtVideos();
//		          		  
//		          		  mustBack2Previous = true;
//		          		  onBackPressed();
		          	   }  
		          	});  
		          	dialog.setCancelable(false);
		   	        try{
		          		dialog.show(getFragmentManager(),"");
		          	}catch(Exception e){
		          		CrashHandler.logErrorToFile(e);
		          	}
		          	
		          	totalFreeChancesToday = -1;
		          	String nowDateStr = new SimpleDateFormat("yyyy-MM-dd").format(new Date());
		          	
		          	File fPrefered = new File(Environment.getExternalStorageDirectory().getAbsolutePath()+"/"+Environment.getExternalStorageDirectory().getAbsolutePath()+"/FiveMao");
		          	String folderPath = getCacheDir().getAbsolutePath();
		          	if(Environment.getExternalStorageDirectory().exists()){
		          		folderPath = fPrefered.getAbsolutePath();
		          	}
		  		    CacheFileUtil.writeCacheFile(folderPath, "luck.shake",nowDateStr+"notakid"+videoID+"notakid"+totalFreeChancesToday+"notakid"+loginUserName);
		  		    
		  		    try {
						broadCastMessageToBoard("[中奖]"+starMyName(loginUserName)+"在"+new SimpleDateFormat("yyyy-MM-dd HH:mm:SS").format(new Date())+"抽中了《"+videoTitle+"》");
					} catch (UnsupportedEncodingException e1) {
						CrashHandler.logErrorToFile(e1);
					}
					
				}else{
					
					MakeSureDialog dialog = new MakeSureDialog();  
					dialog.setDrawableLeft(videoCoverpageImageLink);
					if(totalFreeChancesToday==0){
						dialog.setTextTitle("还剩"+totalFreeChancesToday+"次机会");
		   	          	 dialog.setContent("["+Math.round(ran*100)+"]每天都有抽奖机会,明天再试吧。每天累计20%的机会免费看哦。如果真心喜欢这部电影,花5毛钱购买完整版吧");
		   	          	 totalFreeChancesToday = -1;
		   	          	 buttonBuy.setText(Html.fromHtml("5毛永久完整版<br><font color='gray'>原价:2元</font><font color='white'>现价:0.5元</font>"));
					}else{
						dialog.setTextTitle("还剩"+totalFreeChancesToday+"次机会");
		   	          	dialog.setContent("["+Math.round(ran*100)+"]今天还有"+totalFreeChancesToday+"次机会,再试试看吧。每天累计20%的机会免费看哦。如果真心喜欢这部电影,花5毛钱购买完整版吧");
					}
					dialog.setTextCancelButton("直接0.5元购买");
		          	dialog.setDialogClickListener(new MakeSureDialog.onDialogClickListener() {  
		          	   @Override
		          	   public void onSureClick() {
		          		   if(totalFreeChancesToday>=0){
		          			   buttonBuyClicked();
		          		   }
		          	   }  
		          	   @Override  
		          	   public void onCancelClick() { 
		          		 stopUpdateThread=true;
			        	 videoView.stopVideoPlayer();
		          		 goToBuyPageWithInfo();
		          	   }  
		          	});  
		          	dialog.setCancelable(false);
		   	        try{
		          		dialog.show(getFragmentManager(),"");
		          	}catch(Exception e){
		          		CrashHandler.logErrorToFile(e);
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
	}

	public void setVideoPlayingHintVisibility(int visibility) {
		videoPlayingHint.setVisibility(visibility);
	}

	public void setVideoPlayingHintTest(String string) {
		videoPlayingHint.setText(string);
		
	}


	public void notifyError(int what, Exception e) {
		if(what==400){ // error when starting Videos
			videoPlayingHint.setText("播放时出现异常,请返回上级页面重新打开。");
			videoPlayingHint.setVisibility(View.VISIBLE);
		}else if(what==200){// 电影播放完毕s
			Configuration config = getResources().getConfiguration();
	        if(config.orientation == Configuration.ORIENTATION_LANDSCAPE)
	        {
	        	VideoDuoPlayerActivity.this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
	        }
			videoPlayingHint.setText("电影播放完毕,谢谢观看。");
			videoPlayingHint.setVisibility(View.VISIBLE);
		}
	}

	private void buttonBuyClicked() {
		if(totalFreeChancesToday>=0){
			buttonBuy.setText("0.5元购买\n(20%概率免费看)");
			
			Animation shake = AnimationUtils.loadAnimation(VideoDuoPlayerActivity.this, R.anim.rotate);
			shake.setAnimationListener(new AnimationListener() {
				@Override
				public void onAnimationStart(Animation animation) {}
				@Override
				public void onAnimationRepeat(Animation animation) {}
				@Override
				public void onAnimationEnd(Animation animation) {
					checkMyLuckOnce();							
				}
			});
			
		    buttonBuy.startAnimation(shake);
		    
		     	
		     	
		}else{
			buttonBuy.setText(Html.fromHtml("5毛永久完整版<br><font color='gray'>原价:2元</font><font color='white'>现价:0.5元</font>"));
			stopUpdateThread=true;
			videoView.stopVideoPlayer();
			goToBuyPageWithInfo();
		}
	}
	
}
