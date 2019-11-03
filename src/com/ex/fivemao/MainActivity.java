package com.ex.fivemao;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.AlertDialog;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.media.audiofx.Equalizer;
import android.media.audiofx.Visualizer;
import android.net.Uri;
import android.net.http.SslError;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.os.StatFs;
import android.text.format.Formatter;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AnimationUtils;
import android.webkit.DownloadListener;
import android.webkit.SslErrorHandler;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.RelativeLayout.LayoutParams;
import android.widget.Toast;

import com.boredream.bdvideoplayer.utils.NetworkUtils;
import com.ex.fivemao.audioservice.MusicService;
import com.ex.fivemao.exception.CrashHandler;
import com.ex.fivemao.io.CacheFileUtil;
import com.ex.fivemao.io.PropertiesUtilWithEnc;
import com.ex.fivemao.io.WithEnc;
import com.ex.fivemao.ui.CusttomToast;
import com.ex.fivemao.ui.MakeSureDialog;
import com.ex.fivemao.ui.MyVisualizerView;
import com.ex.fivemao.utils.FlagsUtil;
import com.example.myvideos.http.Downloader;
import com.example.myvideos.http.HttpsGetThread;
import com.example.myvideos.http.proxy.Utils;


public class MainActivity extends BaseActivity {
	private static final float VISUALIZER_HEIGHT_DIP = 130f;//频谱View高度  
	private MyVisualizerView mBaseVisualizerView;  
	private boolean notBind = true;
	private Visualizer mVisualizer;//频谱器  
    private Equalizer mEqualizer; //均衡器  
	
	private static boolean isExit = false;  
	private static Boolean newFilmAlertFlag = true;
	
	public static List<Map<String,Object>> data = new ArrayList<Map<String,Object>>();
	public static List<Map<String,Object>> dataMsgs = new ArrayList<Map<String,Object>>();
	private int currentRandomDisplayIdx = -1;
	RelativeLayout relativeLayoutHomePage;
	ImageView imgVideo;
	ImageView imgSearch;
	ImageView imgMe;
	ImageView imgBoard;
	ImageView imgHothothot;
	
	
	MenuItem itemBGMusic ;
	
	WebView webView;
	Downloader downloader;
	public static boolean networkchecked = false;
	private boolean mobileNetworkConfirmBoxShow = false;
	protected boolean topMovieBlockedByNetworkCheckDialog = false;
	
    @Override
    protected void onCreate(Bundle savedInstanceState) {
    	// 避免从桌面启动程序后，会重新实例化入口类的activity  
    	super.onCreate(savedInstanceState);
    	if (!this.isTaskRoot()) {  
    	    Intent intent = getIntent();  
    	    if (intent != null) {  
    	        String action = intent.getAction();  
    	        if (intent.hasCategory(Intent.CATEGORY_LAUNCHER) && Intent.ACTION_MAIN.equals(action)) {  
    	            finish();  
    	            return;  
    	        }  
    	    }  
    	}  
        setContentView(R.layout.activity_main_major);
        relativeLayoutHomePage = (RelativeLayout)findViewById(R.id.RelativeLayoutHomePage);
        imgVideo = (ImageView)findViewById(R.id.imgVideo);
        imgSearch = (ImageView)findViewById(R.id.imgSearch);
        imgMe = (ImageView)findViewById(R.id.imgMe);
        imgBoard = (ImageView)findViewById(R.id.imgBoard);
        imgHothothot = (ImageView)findViewById(R.id.imgHothothot);
        downloader = new Downloader(this);
        initMainUserData();
        
        relativeLayoutHomePage.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				shakeMainButtons();
			}
		});
        
        
        imgVideo.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				zoomMe(v,new MyAnimationListener() {
					@Override
					public void onAnimationEnd(Animation animation) {
						Intent intent=new Intent();
			            intent.setClass(MainActivity.this, VideoActivity.class);  
			            startActivity(intent);  
					}
				});
				
			}
		});
        
        imgMe.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				zoomMe(v,new MyAnimationListener() {
					@Override
					public void onAnimationEnd(Animation animation) {

						gotoMePage();
					}
				});
			}
		});
        imgSearch.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				zoomMe(v,new MyAnimationListener() {
					@Override
					public void onAnimationEnd(Animation animation) {

						Intent intent=new Intent();
			            intent.setClass(MainActivity.this, SearchActivity.class);  
			            startActivity(intent); 
					}
				}); 
			}
		});
        imgBoard.setOnClickListener(new OnClickListener() {
        	@Override
        	public void onClick(View v) {
        		zoomMe(v,new MyAnimationListener() {
					@Override
					public void onAnimationEnd(Animation animation) {

		        		Intent intent=new Intent();
		        		intent.setClass(MainActivity.this, BoardActivity.class);  
		        		startActivity(intent); 
					}
				});  
        	}
        });
        
        imgHothothot.setOnClickListener(new OnClickListener() {
        	@Override
        	public void onClick(View v) {
        		zoomMe(v,new MyAnimationListener() {
					@Override
					public void onAnimationEnd(Animation animation) {

		        		Intent intent=new Intent();
		        		intent.setClass(MainActivity.this, HotHotActivity.class);  
		        		startActivity(intent);
					}
				});  
        	}
        });
        
        if(webView==null){
        	webView = new WebView(this);
        }
       
        webView.setWebViewClient(new WebViewClient(){
			@Override
			public void onReceivedSslError(WebView view, SslErrorHandler handler, SslError error){
			  handler.proceed();//接受证书
			}
        });
        
		 webView.getSettings().setJavaScriptEnabled(true);  //加上这一行网页为响应式的
        webView.setDownloadListener(new DownloadListener() {
            @Override
            public void onDownloadStart(final String url, String userAgent, String contentDisposition, String mimetype, long contentLength) {
            	if (url.endsWith(".apk")) {
	                runOnUiThread(new Runnable() {
	
						@Override
	                    public void run() {
							String name = url.substring(url.lastIndexOf("/")+1);
							if(name!=null && !name.trim().endsWith(".apk")){
								name = "FiveMao.apk";
							}
	                        downloader.downloadAPK(url,name);//DownLoader 需要在oncreate 中初始化
	                        
	                        
		                    MakeSureDialog dialog = new MakeSureDialog();  
		       	          	 
		       	          	 dialog.setTextTitle("升级提示");
		       	          	 dialog.setContent("正在下载中,下载完成自动进入安装界面,请等待或在通知栏中查看状态。");
		       	          	 
		       	          	dialog.setDialogClickListener(new MakeSureDialog.onDialogClickListener() {  
		       	          	   @Override
		       	          	   public void onSureClick() {
		       	          	   }  
		       	          	   @Override  
		       	          	   public void onCancelClick() {  
		       	          	   }  
		       	          	});  
		       	          	try{
			   	          		dialog.show(getFragmentManager(),"");
			   	          	}catch(Exception e1){
			   	          		CrashHandler.logErrorToFile(e1);
			   	          	}
	       	          	
	                    }
	                });
	            }
            }
        });
        
        webView.setWebViewClient(new WebViewClient(){
	       @Override
	       public boolean shouldOverrideUrlLoading(WebView view, String url) {
		       view.loadUrl(url);
		       return super.shouldOverrideUrlLoading(view, url);
	       }
       });
        
        try{
        	
	        initMainUserData();
	        loadLuckyFilms();
	        
	        if(lockOpened){
		        boolean cleanCacheFiles = cleanCacheFiles();
		        if(!cleanCacheFiles){
		        	checkForNewVersionAndShowTopMovie();
		        }
	        }
	        
	        cleanDownloadedPackagesIfMatched();
	        shakeMainBackground();
	        initLatestMessageData();
	        
	        httpHandlerMeShowTopMessage.postDelayed(showMessageRunn,1000);
        }catch(Exception e){
        	CrashHandler.logErrorToFile(e);
        	Toast.makeText( MainActivity.this, "数据初始化出现问题,如果不能正常使用,请重新安装。", Toast.LENGTH_LONG).show();
        }
        
        
    }
    
    @Override
    protected void lockOpenedCallback() {
    	boolean cleanCacheFiles = cleanCacheFiles();
        if(!cleanCacheFiles){
        	checkForNewVersionAndShowTopMovie();
        }
    }
    
    private void setupVisualizerFxAndUi() {  
        mBaseVisualizerView = new MyVisualizerView(this);  
  
//        mBaseVisualizerView.setLayoutParams(new ViewGroup.LayoutParams(  
//                ViewGroup.LayoutParams.MATCH_PARENT,//宽度  
//                (int) (VISUALIZER_HEIGHT_DIP * getResources().getDisplayMetrics().density)//高度  
//        ));  
        
        LayoutParams params1 = new RelativeLayout.LayoutParams(LayoutParams.MATCH_PARENT, (int) (VISUALIZER_HEIGHT_DIP * getResources().getDisplayMetrics().density));
        params1.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
        mBaseVisualizerView.setLayoutParams(params1);
        
        //将频谱View添加到布局  
        relativeLayoutHomePage.addView(mBaseVisualizerView);  
        //实例化Visualizer，参数SessionId可以通过MediaPlayer的对象获得  
        
    }  
    
   


	public static String getAvailableInternalMemorySize(Context context) {
        File file = Environment.getDataDirectory();
        StatFs statFs = new StatFs(file.getPath());
        long availableBlocksLong = statFs.getAvailableBlocksLong();
        long blockSizeLong = statFs.getBlockSizeLong();
        return Formatter.formatFileSize(context, availableBlocksLong
                * blockSizeLong);
    }
    
    private boolean cleanCacheFiles() {
    	try {
			File[] caches = Utils.listVideoCachesInDir(this);
			
			if(caches!=null && caches.length>50 && Math.random()<0.2){
				long size = 0 ;
				for(File cache:caches){
					size+=cache.length();
				}
				
				MakeSureDialog dialog = new MakeSureDialog();  
	          	 dialog.setTextTitle("提示信息");
	          	 dialog.setContent("您播放视频产生的缓存文件"+caches.length+"个,占用空间为"+(size/1024/1024+1)+"M,目前可用存储空间"+getAvailableInternalMemorySize(this)+",为避免占用过多手机资源,建议清理。（也可在右上角菜单>>设置>>清理缓存,手动清理）");
	          	 dialog.setTextSureButton("马上清理");
	          	 dialog.setTextCancelButton("知道了");
	          	dialog.setDialogClickListener(new MakeSureDialog.onDialogClickListener() {  
	          	   @Override
	          	   public void onSureClick() {
	          		    cleanCache();
	          	   }  
	          	   @Override  
	          	   public void onCancelClick() {  
	          	   }  
	          	});  
	          	dialog.setCancelable(false);
  	          	dialog.show(getFragmentManager(),"");
  	          	
				return true;
			}
			
		} catch (IOException e) {
			CrashHandler.logErrorToFile(e);
		}
		return false;
	}


    

    private boolean checkIfProcessingAlipayMembershipTransaction() {
//    	CacheFileUtil.writeCacheFile(getCacheDir().getAbsolutePath(),
//				"alipay_trans_membership.dat", transID+"notakid"+membershipdays+"notakid"+loginUserName);
		
    	String content = CacheFileUtil.readCacheFile(getCacheDir().getAbsolutePath(), "alipay_trans_membership.dat");
    	if(content!=null && content.length()>0 && content.contains("notakid")){
    		final String[] alipayObjs = content.split("notakid");
    		if(alipayObjs.length>=2 && loginUserName!=null && loginUserName.equals(alipayObjs[2])){
	            
    			VideoBuyMembershipActivity.existingAliTransaction = alipayObjs[0]; 
                VideoBuyMembershipActivity.membershipdays=Integer.parseInt(alipayObjs[1]);
                
	            MakeSureDialog dialog = new MakeSureDialog();  
	          	 
	          	 dialog.setTextTitle("请继续付款:上传支付截图?");
	          	 dialog.setTextSureButton("上传截图");
	          	 dialog.setTextCancelButton("知道了");
	          	 dialog.setContent("交易时间:"+alipayObjs[0]+"\n购买"+VideoBuyMembershipActivity.membershipdays+"日会员\n\n*获取截图:支付宝>>我的>>账单>>交易详单>>截屏,然后点击'上传截图'");
	          	 
	          	dialog.setDialogClickListener(new MakeSureDialog.onDialogClickListener() {  
	          	   @Override
	          	   public void onSureClick() {
	          		    Intent intent=new Intent();
	                    VideoBuyActivity.existingAliTransaction = alipayObjs[0]; 
	            		intent.putExtra("membershipdays", alipayObjs[1]);
	            		intent.setClass(MainActivity.this,VideoBuyMembershipActivity.class);  
	            		startActivity(intent);  
	          	   }  
	          	   @Override  
	          	   public void onCancelClick() {  
	          		 CacheFileUtil.writeCacheFile(getCacheDir().getAbsolutePath(),
								"alipay_trans_membership.dat", "PaymentPossiblyDone");
	          	   }  
	          	});  
	          	dialog.setCancelable(false);
	          	try{
   	          		dialog.show(getFragmentManager(),"");
   	          	}catch(Exception e1){
   	          		CrashHandler.logErrorToFile(e1);
   	          	}
	            return true;
    		}
    	}
    	return false;
		
	}
	private boolean checkIfProcessingAlipayTransaction() {
    	String content = CacheFileUtil.readCacheFile(getCacheDir().getAbsolutePath(), "alipay_trans.dat");
    	if(content!=null && content.length()>0 && content.contains("notakid")){
    		final String[] alipayObjs = content.split("notakid");
    		if(alipayObjs.length>=6 && loginUserName!=null && loginUserName.equals(alipayObjs[5])){
	            
	            MakeSureDialog dialog = new MakeSureDialog();  
	          	 
	          	 dialog.setTextTitle("有未完成的付款,继续支付?");
	          	 dialog.setTextSureButton("去上传截图");
	          	 dialog.setTextCancelButton("不再提醒");
	          	 dialog.setContent("交易时间:"+alipayObjs[0]+"\n购买电影：《"+alipayObjs[2]+"》\n\n*获取截图:支付宝>>我的>>账单>>交易详单>>截屏,然后'上传截图'");
	          	 
	          	dialog.setDialogClickListener(new MakeSureDialog.onDialogClickListener() {  
	          	   @Override
	          	   public void onSureClick() {
	          		   Intent intent=new Intent();
	                    VideoBuyActivity.existingAliTransaction = alipayObjs[0]; 
	            		intent.putExtra("id", alipayObjs[1]);
	            		intent.putExtra("title", alipayObjs[2]);
	            		intent.putExtra("videoFileLink", alipayObjs[3]);
	            		intent.putExtra("description", alipayObjs[4]);
	            		
	            		intent.setClass(MainActivity.this,VideoBuyActivity.class);  
	            		startActivity(intent);  
	          	   }  
	          	   @Override  
	          	   public void onCancelClick() {  
	          		 CacheFileUtil.writeCacheFile(getCacheDir().getAbsolutePath(),
								"alipay_trans.dat", "PaymentPossiblyDone");
	          	   }  
	          	});  
	          	try{
   	          		dialog.show(getFragmentManager(),"");
   	          	}catch(Exception e1){
   	          		CrashHandler.logErrorToFile(e1);
   	          	}
	            return true;
    		}
    	}else{
    		return checkIfProcessingAlipayMembershipTransaction();
    	}
    	return false;
		
	}


	public void initMainUserData() {
    	//CACHE, loginUserName+"notakid"+loginUserBirthday
    	//Toast.makeText( MainActivity.this, "加载用户信息", Toast.LENGTH_SHORT).show();
		loginUserName = null;
		loginUserBirthday = null;
		loginUserEmail = null;
		loginUserProxyEmail = null;
		
        String info = CacheFileUtil.readCacheFile(getCacheDir().getAbsolutePath(), PropertiesUtilWithEnc.getString("cacheFileName"));
        if(info!=null && info.contains("notakid")){
            String[] userInfoLoaded = info.split("notakid");
            if(userInfoLoaded.length>=3){
            	loginUserName = userInfoLoaded[0];
            	loginUserBirthday = userInfoLoaded[1];
            	String emailStrs = userInfoLoaded[2];
            	String[] emails = emailStrs.split(";");
            	if(emails.length>0){
        			loginUserEmail = emails[0];
        		}
            	if(emails.length>1){
            		loginUserProxyEmail = emails[1];
            	}
            	
            	setTitle("五毛影视："+loginUserName);
            	if(userInfoLoaded.length>=4){
            		String[] searchHiss = userInfoLoaded[3].split(",");
            		for(int i = 0; i < searchHiss.length; i++){
            			if(!SearchActivity.searchHistory.contains(searchHiss[i])){
            				SearchActivity.searchHistory.add(searchHiss[i]);
            			}
        			}
            		
            	}
            	
            	
            	VideoPlayerActivity.videosBought.clear();
            	loadFilmsAuthed();
            }
            
        }else{
        	setTitle("五毛影视");
        }
        
	}
	
    private void checkLoginStatus(){
    	if(loginUserName==null){
	    	AlertDialog.Builder builder = new AlertDialog.Builder(this);  
	        builder.setIcon(android.R.drawable.ic_dialog_info);  
	        builder.setTitle("提示:注册/登录 赚佣金？");  
	        builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {  
	            public void onClick(DialogInterface dialog, int which) {  
	            	gotoMePage();
	            }  
	        });  
	        
	        builder.show();  
    	}
    }

	private void checkForNewVersionAndShowTopMovie() {
		final Pattern p = Pattern.compile("[0-9]{1,}\\.[0-9]{1,3}");
		Matcher matcher = p.matcher(getVersionName());
		Double currentVersionNo = 0.0;
		if(matcher.find()){
			try{
				currentVersionNo = Double.parseDouble(matcher.group());
			}catch(Exception e){
			}
		}
		final Double currentVersion = currentVersionNo; 
		
		if(currentVersionNo==null || currentVersionNo<=0.0){
			return;
		}else{
			
			final String appDownloadUrl = String.valueOf(
					PropertiesUtilWithEnc.prop.getProperty("domainCall"))+"/clientApp/checkVersionFile/FiveMao.version/";
			
			HttpsGetThread thread = new HttpsGetThread(new Handler() {
	            @Override
	            public void handleMessage(Message msg) {
	                super.handleMessage(msg);
	                String result = (String) msg.obj;
	                boolean needUpgrade = false;
	                
	                switch (msg.what) {
	                case 200:
                		try{
	                		JSONObject json = new JSONObject(result);
	                		String status = json.getString("status");
	                		final String message = json.getString("message");
	                		
	                		if(status!=null && status.contains("Success") && message!=null && message.split("\n")!=null && message.split("\n").length>0){
	                			
	                			
	                			Matcher matcher = p.matcher(message.split("\n")[0]);
	                			if(matcher.find()){
	                				String versionNoMatched = matcher.group();
	                				Double targetVersionNo = 0.0;
		                			try{
		                				targetVersionNo = Double.parseDouble(versionNoMatched);
		                			}catch(Exception e){
		                			}
		                			
		                			final String version = String.valueOf(targetVersionNo);
		                			
	            					if(targetVersionNo>currentVersion){
	            						needUpgrade = true;
	            						MakeSureDialog dialog = new MakeSureDialog(); 
	    	                			dialog.setTextTitle("[五毛影视]新版本升级");
	    	                			
	    	                			//Toast.makeText( MeActivity.this, status+":"+message, Toast.LENGTH_LONG).show();
	    	                			dialog.setContent(message);
	    	                			dialog.setTextSureButton("手动下载");
	    	                			dialog.setTextCancelButton("马上升级");
	    	                			dialog.setDialogClickListener(new MakeSureDialog.onDialogClickListener() {  
	    	                				@Override  
	    	                				public void onSureClick() {
	    	                					final String apkfile = String.valueOf(PropertiesUtilWithEnc.prop.getProperty("domainCall"))+"/resources/clients/FiveMao.V"+version+".apk";
	    	                					ClipboardManager cm = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
	    	                					cm.setText(apkfile);
	    	                					 	    	    	                			
	    	                					AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);  
	    	                		            builder.setIcon(android.R.drawable.ic_dialog_info);  
	    	                		            builder.setTitle("手动升级提示");
	    	                		            builder.setMessage("默认调用系统浏览器下载,如果失败,请手动下载。链接地址已经复制到剪切板。浏览器（推荐谷歌Chrome浏览器）打开即可手动下载&安装。");
	    	                		            builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {  
	    	                		                public void onClick(DialogInterface dialog, int which) {  
	    	                		                    dialog.dismiss();  
	    	                		                    
	    	    	                					Intent intent= new Intent();        
	    	    	                					intent.setAction("android.intent.action.VIEW");    
	    	    	                					
	    	    	                					Uri content_url = Uri.parse(apkfile);   
	    	    	                					intent.setData(content_url);  
	    	    	                					startActivity(intent);
	    	                		                }  
	    	                		            });  
	    	                		            
	    	                		            builder.show();  
	    	                		            
	    	                				}
	    	                				
	    	                				@Override  
	    	                				public void onCancelClick() { 
	    	                					String apkfile = String.valueOf(PropertiesUtilWithEnc.prop.getProperty("domainCall"))+"/resources/clients/FiveMao.V"+version+".apk";
	    	                					webView.loadUrl(apkfile);
	    	                				}  
	    	                			});  
	    	                			
	    	                			
	    	                			dialog.setCancelable(false);
	    	                			try{
	    	        	   	          		dialog.show(getFragmentManager(),"");
	    	        	   	          	}catch(Exception e1){
	    	        	   	          		CrashHandler.logErrorToFile(e1);
	    	        	   	          	}
	    	                			
	    	                			CacheFileUtil.writeCacheFile(getCacheDir().getAbsolutePath(), "fivemaoversionfeature.dat",message);
	    	                			
	    	                			return;
	            					}
	                			}
	                			
	                			
	                			
							}
                		}catch(Exception e){
                		}
	                		
	                    break;  
	                case 404:  
	                    break;  
	                }  
	                //do top videos
	                boolean hasAlipayTransaction = checkIfProcessingAlipayTransaction();
	                if(!hasAlipayTransaction ){
	                	if(!needUpgrade ){
	                		if(loginUserName!=null){
	                			getTopMovies();//popup
	                		}else{
	                			checkLoginStatus();//popup
	                		}
	                	}
	                }
	            }  
	        },appDownloadUrl, 200);  
	        thread.start();
	        
			
			
		}
		
		
	}
	

	private void getTopMovies(){
		if(!MainActivity.newFilmAlertFlag ){
			return;
		}
		
		int minAge = 18;
    	if(loginUserProxyEmail !=null && loginUserProxyEmail.contains("@")){
    		minAge = 99;
    	}
		String appRecUrl = String.valueOf(PropertiesUtilWithEnc.prop.getProperty("domainCall"))+"/dictionary/filmsLatestFew/"+PropertiesUtilWithEnc.getString("fixedFilmCatID")+"/5/"+minAge+"/";
		
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
						data.clear();
						currentRandomDisplayIdx = -1;
						
						for(int i = 0; i < jsonarr.length(); i++){
							JSONObject jobject = jsonarr.getJSONObject(i);
							Map<String,Object> rowData = new HashMap<String,Object>();
							rowData.put("id", jobject.getLong("id"));
							rowData.put("title", jobject.getString("title"));
							rowData.put("createdBy", jobject.getString("createdBy"));
							rowData.put("createdTime", jobject.getString("createdTime"));
							rowData.put("coverpageImageLink", jobject.getString("coverpageImageLink"));
							rowData.put("description", jobject.getString("description"));
							rowData.put("videoType", jobject.getString("videoType"));
							rowData.put("freeVideoFlag", jobject.getString("freeVideoFlag"));
							rowData.put("minimumAge", jobject.getString("minimumAge"));
							
							String videoType =jobject.getString("videoType");
							
							
							if(jobject.getString("freeVideoFlag")!=null && jobject.getString("freeVideoFlag").equalsIgnoreCase("Y")){
								VideoPlayerActivity.videosFreebies.add(String.valueOf(jobject.getLong("id")));
								VideoDuoPlayerActivity.videosFreebies.add(String.valueOf(jobject.getLong("id")));
							}
							
							if(videoType.equals("Type_VideoUrl")){
								rowData.put("videoFileLink", jobject.getString("videoFileLink"));
							}else{
								rowData.put("videoFileLink", String.valueOf(PropertiesUtilWithEnc.prop.getProperty("domainCall"))+jobject.getString("videoFileLink"));
							}
							rowData.put("itemIdx", "序号"+(i+1));
							rowData.put("desc","今日推荐");
//							rowData.put("desc", "可试看(<font color='gray'>原价:2元</font> <font color='red'>现价:0.5元</font>)");
//							if(VideoPlayerActivity.videosFreebies.contains(String.valueOf(jobject.getLong("id")))){
//    							rowData.put("desc", "免费看");
//    						}
//							if(VideoPlayerActivity.videosBought.contains(String.valueOf(jobject.getLong("id")))){
//								rowData.put("desc", "已购买");
//							}
//							if(membershipExpiryDate!=null){
//								rowData.put("desc", "您已是会员:全场免费");
//							}
//							if(VideoPlayerActivity.videosLuckyShakes.contains(String.valueOf(jobject.getLong("id")))){
//								rowData.put("desc", "幸运一天");
//							}
							
							data.add(rowData);
						}
						
						int idx = 0;
						currentRandomDisplayIdx = idx;
						
						if(!mobileNetworkConfirmBoxShow){
							displayOneTopMovie(idx);
							topMovieBlockedByNetworkCheckDialog = false;
						}else{
							topMovieBlockedByNetworkCheckDialog  = true;
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
	
	
	@Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        Boolean bgMusic = loadBGMusicSettings();
        
        if(bgMusic!=null && bgMusic){
	        for(int i = 0; i < menu.size();i ++){
	        	MenuItem item = menu.getItem(i);
	        	if(item.getItemId()==R.id.action_toogleBGMusic){
	        		itemBGMusic = item;
	        		if(!networkchecked){
	        			networkchecked  = true;
	        			checkNetworkStatus();
	        		}
	        		break;
	        	}
	        }
        }else{
        	for(int i = 0; i < menu.size();i ++){
            	MenuItem item = menu.getItem(i);
            	if(item.getItemId()==R.id.action_recToFriends){
            		item.setVisible(true);
            		break;
            	}
            }
        }
        return true;
    }

	private void initStartMusicService() {
		itemBGMusic.setVisible(true);
		itemBGMusic.setEnabled(true);
		if(!MusicService.isReady){
			MusicService.isplaying = true;
			startBGMusicAfterReset();
		}
		if(MusicService.isplaying){
			Bitmap bmp = WithEnc.getImageBigMapNoEnc(getResources().openRawResource(R.drawable.ic_video_pause));
			itemBGMusic.setIcon(new BitmapDrawable(getResources(),bmp));
		}else{
			Bitmap bmp = WithEnc.getImageBigMapNoEnc(getResources().openRawResource(R.drawable.ic_video_play));
			itemBGMusic.setIcon(new BitmapDrawable(getResources(),bmp));
		}
		
		prepareVisulizer();
		
		new Thread(new Runnable() {
			
			@Override
			public void run() {
				int count = 200;
				while(!MusicService.isReady){
					try {
						Thread.sleep(100);
						count = count - 1;
						if(count<0){
							break;
						}
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
				
				if(MusicService.isReady ){
					bindMVisulizer();
				}
			}
		}).start();
		
		
	}

	private void prepareVisulizer() {
		setupVisualizerFxAndUi();
	}

    private void checkNetworkStatus() {
    		
    	if( NetworkUtils.isWifiConnected(getApplicationContext())){
    		initStartMusicService();
		}else if(NetworkUtils.isMobileConnected(getApplicationContext())){
			if(!mobileNetworkConfirmBoxShow ){
				mobileNetworkConfirmBoxShow = true;
				MakeSureDialog dialog = new MakeSureDialog();
				dialog.setTextTitle("网络提醒");
				dialog.setContent("您当前使用的是手机流量网络,继续播放背景音乐吗？");
				dialog.setTextSureButton("继续播放");
				dialog.setTextCancelButton("暂停音乐");
				
				dialog.setDialogClickListener(new MakeSureDialog.onDialogClickListener() {  
			    	   @Override  
			    	   public void onSureClick() {
			    		   mobileNetworkConfirmBoxShow = false;
			    		   initStartMusicService();
			    		   if(topMovieBlockedByNetworkCheckDialog){
			    			   displayOneTopMovie(0);
			    			   topMovieBlockedByNetworkCheckDialog = false;
			    		   }
			    	   }
			    	   @Override  
			    	   public void onCancelClick() {
			    		   mobileNetworkConfirmBoxShow = false;
			    		   
			    		   if(topMovieBlockedByNetworkCheckDialog){
			    			   displayOneTopMovie(0);
			    			   topMovieBlockedByNetworkCheckDialog = false;
			    		   }
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
	}

	@Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if(id==R.id.action_toogleBGMusic){
        	toogleBGMusic(item);
        }else if(id==R.id.action_recToFriends){
        	recToFriendsDialog();
        }
        
        return super.onOptionsItemSelected(item);
    }
	
	private void toogleBGMusic(final MenuItem item) {
		if(FlagsUtil.bgmusicErrorOccurred!=null && FlagsUtil.bgmusicErrorOccurred){
			item.setVisible(false);
			item.setEnabled(false);
		}else{
			if(MusicService.isplaying){
				Boolean rs = loadBGMusicSettings();
				if(rs==null){
					MakeSureDialog dialog = new MakeSureDialog();  
				   	 dialog.setTextTitle("停止提示");
				   	 dialog.setTextSureButton("设置背景音乐");
					 dialog.setTextCancelButton("我要停止播放");
				   	 dialog.setTextSizeContent(16);
				   	 dialog.setContent("您正在进行[暂停播放]，背景音乐将默认打开，如需永久关闭背景音乐可以到屏幕右上角>菜单>设置>打开/关闭背景音乐，设置。");
				   	 
				   	dialog.setDialogClickListener(new MakeSureDialog.onDialogClickListener() {  
				   	   @Override  
				   	   public void onSureClick() {
					   		Intent intent=new Intent();
				            intent.setClass(MainActivity.this, MenuSettingsActivity.class);  
				            startActivity(intent); 
				   	   }  
				   	   @Override  
				   	   public void onCancelClick() {  
				   		   CacheFileUtil.writeCacheFile(getCacheDir().getAbsolutePath(), "bgmusic.duo.dat","Y");
				   		   pauseMusicSettingIcon(item);
				   	   }
				   	   	
				   	});  
				   	try{
			      		dialog.show(getFragmentManager(),"");
			      	}catch(Exception e1){
			      		CrashHandler.logErrorToFile(e1);
			      	}
				}
			   	else{
			   		pauseMusicSettingIcon(item);
			   	}
			}else{
				
				Bitmap bmp = WithEnc.getImageBigMapNoEnc(getResources().openRawResource(R.drawable.ic_video_pause));
				item.setIcon(new BitmapDrawable(getResources(),bmp));
				item.setTitle("暂停");
				startBGMusic();
			}
		}
	}

	private void pauseMusicSettingIcon(MenuItem item) {
		Bitmap bmp = WithEnc.getImageBigMapNoEnc(getResources().openRawResource(R.drawable.ic_video_play));
		item.setIcon(new BitmapDrawable(getResources(),bmp));
		item.setTitle("播放");
		pauseBGMusic();
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
    						}else if(status!=null && status.contains("Success")){
    							if(filmIdStr!=null && filmIdStr.length()>0){
    								String[] filmIDs = filmIdStr.split(",");
    								if(filmIDs.length>0){
    									for(String item:filmIDs){
    										VideoPlayerActivity.videosBought.add(item.trim());
    									}
    								}
    								
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
			 //&& unameFromFile.equals(MeActivity.loginUserName)) 限定单台手机抽奖次数
			 if(nowDateStr.trim().equals(dateStr.trim())&&loginUserName!=null ){
				 VideoPlayerActivity.totalFreeChancesToday = Integer.parseInt(leftCount);
				 
				 if(Integer.parseInt(leftCount)>0){
					 	 
				 }else{
					 VideoPlayerActivity.videosLuckyShakes.add(filmID);
				 }
				 
				 return true;
			 }
		 }
	}
	
	return false;
	
}
	private Integer getLocalVersion(){
		try{
			
			PackageManager pm = this.getPackageManager();
			PackageInfo pinfo = pm.getPackageInfo(this.getPackageName(), 0);
			int versionCode = pinfo.versionCode;
//			String versionName = pinfo.versionName;
			
			return versionCode;
		}catch(Exception e){
//			Toast.makeText( MainActivity.this, "获取版本异常"+e.getMessage(), Toast.LENGTH_LONG).show();
			return null;
		}
	}
	private String getVersionName(){
		try{
			
			PackageManager pm = this.getPackageManager();
			PackageInfo pinfo = pm.getPackageInfo(this.getPackageName(), 0);
			String versionName = pinfo.versionName;
			
			return versionName;
		}catch(Exception e){
			return "~";
		}
	}
	
//	@Override  
//    public boolean onKeyDown(int keyCode, KeyEvent event) {  
//        if (keyCode == KeyEvent.KEYCODE_BACK) {  
//            exit();  
//            return false;  
//        }  
//        return super.onKeyDown(keyCode, event);  
//    }  
	
	Handler mHandler = new Handler() {  
        @Override  
        public void handleMessage(Message msg) {  
            super.handleMessage(msg);  
            isExit = false;  
        }  
    };
	  
	 private void exit() {  
	        if (!isExit) {  
	            isExit = true;  
	            Toast.makeText(getApplicationContext(), "再按一次退出程序",  
	                    Toast.LENGTH_SHORT).show();  
	            // 利用handler延迟发送更改状态信息  
	            mHandler.sendEmptyMessageDelayed(0, 2000);  
	        } else {  
	        	AlertDialog.Builder builder = new AlertDialog.Builder(this);  
	            builder.setIcon(android.R.drawable.ic_dialog_info);  
	            
	            if(lockPasswordForThisPhone!=null && lockPasswordForThisPhone.length()>0){
	            	builder.setTitle("下次需输入打开密码,确认退出五毛影视？");
	            }else{
	            	builder.setTitle("确定退出五毛影视吗？");
	            }
	            builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {  
	                public void onClick(DialogInterface dialog, int which) {
	                	dialog.dismiss();  
	                	newFilmAlertFlag = true;
	                	lockAppIfApplicable();
	                	stopBGMusicService();
	                	CrashHandler.deleteErrorLogFileIfExits();
	                	MainActivity.super.onBackPressed();
	                }
	            });  
	            
	            builder.show();  
	            
	        }  
	    }  
	 
	 private void searchFileInDir(List<File> resultsHolder,File[] folders,String[] keywords, boolean allSatisfy){
		 for(File file : folders){
			 if(file.isFile()){
				 if(keywords!=null && keywords.length>0){
					 int matched = 0;
					 for(String key:keywords){
						 if(allSatisfy){
							 if(file.getName().contains(key)){
								 matched = matched + 1;
							 }
						 }else{
							 if(file.getName().contains(key)){
								 resultsHolder.add(file);
								 break;
							 }
						 }
					 }
					 if(allSatisfy && matched == keywords.length){
						 resultsHolder.add(file);
					 }
				 }
			 }else if(file.isDirectory()){
				 File[] filesInDir = file.listFiles();
				 for(File f:filesInDir){
					 File[] tempF = new File[]{f};
					 searchFileInDir(resultsHolder,tempF, keywords, allSatisfy);
				 }
			 }
		 }
	 }
	 public int cleanDownloadedPackagesIfMatched(){
	    	File folder = Environment.getExternalStorageDirectory();
//	    	File folder2 = new File(folder.getAbsolutePath()+"/Download");
	    	File folder1 = new File(folder.getAbsolutePath()+"/"+folder.getAbsolutePath());
	    	File[] folders = new File[]{folder1};
	    	
	    	List<File> files = new ArrayList<File>();
	    	searchFileInDir(files,folders,new String[]{"FiveMao",".apk"},true);
	    	
	    	int counter = 0 ;
	    	if(files!=null && files.size()>0){
	    		for(File file:files){
	    			boolean deleted = file.delete();
	    			if(deleted){
	    				counter = counter + 1;
	    			}
	    		}
	    	}
	    	
	    	return counter;
	    }


	private void shakeMainButtons() {
		Animation shake = AnimationUtils.loadAnimation(MainActivity.this, R.anim.shake);
		Animation zoom = AnimationUtils.loadAnimation(MainActivity.this, R.anim.zoom2);
		imgVideo.startAnimation(zoom);
		imgSearch.startAnimation(shake);
		imgMe.startAnimation(shake);
		imgBoard.startAnimation(shake);
		imgHothothot.startAnimation(shake);
	}
	
	private void zoomMe(View view,MyAnimationListener endcallback){
		Animation zoom = AnimationUtils.loadAnimation(MainActivity.this, R.anim.zoom);
		if(endcallback!=null){
			zoom.setAnimationListener(endcallback);
		}
		view.startAnimation(zoom);
	}
	
	@Override
	protected void onStop() {
		countXXX = -1;
		stopMusicIfnotYetReady();
		super.onStop();
	};
	private void stopMusicIfnotYetReady() {
		if(!MusicService.isReady){
			stopBGMusicService();
		}
	}
	
	@Override
	protected void onDestroy() {
		lockAppIfApplicable();
		super.onDestroy();
	}

	@Override
	protected void onResume() {
		super.onResume();
		initMainUserData();
		shakeMainBackground();
		
		if(countXXX>=0){
			countXXX = 12;
		}else{
			countXXX = 12;
			httpHandlerMeShowTopMessage.postDelayed(showMessageRunn,1000);
		}
		
		
		if(itemBGMusic!=null ){
			if(MusicService.isplaying){
				FlagsUtil.bgmusicErrorOccurred = false;
			}
			if(FlagsUtil.bgmusicErrorOccurred!=null && FlagsUtil.bgmusicErrorOccurred){
				itemBGMusic.setVisible(false);
	        	itemBGMusic.setEnabled(false);
			}else{
				Boolean bgMusic = loadBGMusicSettings();
		        
				if(bgMusic!=null && bgMusic){
					if(!networkchecked){
						networkchecked  = true;
						checkNetworkStatus();
					}
					if(MusicService.isplaying){
						Bitmap bmp = WithEnc.getImageBigMapNoEnc(getResources().openRawResource(R.drawable.ic_video_pause));
						itemBGMusic.setIcon(new BitmapDrawable(getResources(),bmp));
					}else{
						Bitmap bmp = WithEnc.getImageBigMapNoEnc(getResources().openRawResource(R.drawable.ic_video_play));
						itemBGMusic.setIcon(new BitmapDrawable(getResources(),bmp));
					}
		        }else{
		        	itemBGMusic.setVisible(false);
		        	itemBGMusic.setEnabled(false);
		        }
			}
		}
		
		
		
	};
	
	private void shakeMainBackground() {
		Animation shake = AnimationUtils.loadAnimation(MainActivity.this, R.anim.shake);
		shake.setAnimationListener(new AnimationListener() {
			@Override
			public void onAnimationStart(Animation animation) {
				
			}
			
			@Override
			public void onAnimationRepeat(Animation animation) {
				
			}
			
			@Override
			public void onAnimationEnd(Animation animation) {
				shakeMainButtons();
			}
		});
		relativeLayoutHomePage.startAnimation(shake);
	}
	
	abstract class MyAnimationListener implements AnimationListener{

		@Override
		public void onAnimationStart(Animation animation) {
			
		}

		@Override
		public abstract void onAnimationEnd(Animation animation);

		@Override
		public void onAnimationRepeat(Animation animation) {
			
		}
		
	}
	
	@Override
	public void onBackPressed() {
		if (this.isTaskRoot()) { 
			exit();
		}else{
			super.onBackPressed();
		}
	}

	private void displayOneTopMovie(final int idx) {
		if(idx>=data.size() || !lockOpened){
			return;
		}
		final Map<String,Object> rowData = data.get(idx);
		String videoType = String.valueOf(rowData.get("videoType"));
		String minimumAgeStr = String.valueOf(rowData.get("minimumAge"));
		
		//Toast.makeText(MainActivity.this, "videoType:"+videoType, Toast.LENGTH_LONG).show();
		
		if(videoType.equals("Type_VideoFileLink")||videoType.equals("Type_VideoUrl")||videoType.equals("Type_VideoFolderLink")){
			if(VideoPlayerActivity.videosBought.contains(Long.parseLong(String.valueOf(rowData.get("id"))))){
				//not showing if bought already!!!
			}else if(VideoDuoPlayerActivity.videosBought.contains(Long.parseLong(String.valueOf(rowData.get("id"))))){
				//not showing if bought already!!!
			}else{
				MakeSureDialog dialog = new MakeSureDialog();  
			   	 dialog.setTextTitle("今日推荐");
			   	 dialog.setTextSureButton("马上播放");
				   	if(idx==(data.size()-1)){
				   		dialog.setTextCancelButton("去搜电影");
				   	}else{
				   	    dialog.setTextCancelButton("换一个");
				   	}
			   	 dialog.setDrawableLeft(String.valueOf(rowData.get("coverpageImageLink")));
			   	 dialog.setTextSizeContent(14);
			   	 if(minimumAgeStr!=null && !minimumAgeStr.equals("null")&& Integer.parseInt(minimumAgeStr)>=18){
			   		dialog.setContent("《"+String.valueOf(rowData.get("title"))+"》"+"(限制级)\n发布时间:"+String.valueOf(rowData.get("createdTime"))+"\n简介:"+String.valueOf(rowData.get("description")));
			   	 }else{
			   		dialog.setContent("《"+String.valueOf(rowData.get("title"))+"》"+"\n发布时间:"+String.valueOf(rowData.get("createdTime"))+"\n简介:"+String.valueOf(rowData.get("description")));
			   	 }
			   	 
			   	dialog.setDialogClickListener(new MakeSureDialog.onDialogClickListener() {  
			   	   @Override  
			   	   public void onSureClick() {
			   		   MainActivity.newFilmAlertFlag = false;
				   		Map<String,Object> line = rowData;
						
						Intent intent=new Intent();
						intent.putExtra("id", String.valueOf(line.get("id")));
						intent.putExtra("title", String.valueOf(line.get("title")));
						intent.putExtra("videoFileLink", String.valueOf(line.get("videoFileLink")));
						intent.putExtra("description", String.valueOf(line.get("description")));
						intent.putExtra("videoType", String.valueOf(line.get("videoType")));
						intent.putExtra("minimumAge", String.valueOf(line.get("minimumAge")));
						intent.putExtra("coverpageImageLink", String.valueOf(line.get("coverpageImageLink")));
						
						intent.setClass(MainActivity.this, VideoDescActivity.class);  
			            startActivity(intent);  
			   	   }  
			   	   @Override  
			   	   public void onCancelClick() {  
			   		   
			   		   if(idx==(data.size()-1)){
			   			   MainActivity.newFilmAlertFlag = false;
			   			   gotoSearchFilmActivity();
			   		   }else{
			   			   if((currentRandomDisplayIdx+1)<data.size()){
			   				   currentRandomDisplayIdx = currentRandomDisplayIdx + 1;
			   				   displayOneTopMovie(currentRandomDisplayIdx);
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
		}
	}
	private void releaseVisulizer() {
		mVisualizer.release();  
	    mEqualizer.release();  
	}
	
	private void bindMVisulizer(){
		if(notBind ){
			notBind = false;
	    	mVisualizer = new Visualizer(MusicService.getMediaPlayer().getAudioSessionId());
	        //采样 - 参数内必须是2的位数 - 如64,128,256,512,1024  
	        mVisualizer.setCaptureSize(Visualizer.getCaptureSizeRange()[1]);  
	        //设置允许波形表示，并且捕获它  
	        mBaseVisualizerView.setVisualizer(mVisualizer);  
	        mVisualizer.setEnabled(true);
	        
	        setupEqualizeFxAndUi();
		}
    }
	 private void setupEqualizeFxAndUi() {  
	    	mEqualizer = new Equalizer(0, MusicService.getMediaPlayer().getAudioSessionId());  
	        mEqualizer.setEnabled(true);// 启用均衡器  
	  
	  }
	 
	 Handler httpHandlerMeShowTopMessage = new Handler();
	 
	 private void initLatestMessageData() {  
	    	
		 Handler httpHandler = new Handler() { 
	        	
	            @Override  
	            public void handleMessage(Message msg) {  
	                super.handleMessage(msg);  
	                String result = (String) msg.obj;
	                
	                switch (msg.what) {  
	                case 200:
	                	
	                    try {
							JSONArray jsonarr = new JSONArray(result);
							if(jsonarr.length()<=0 ){
								return;
							}
							for(int i = 0 ; i < jsonarr.length(); i++){
								JSONObject jobject = jsonarr.getJSONObject(i);
								Map<String,Object> rowData = new HashMap<String,Object>();
								rowData.put("id", jobject.getLong("id"));
								rowData.put("createdBy", jobject.getString("createdBy"));
								rowData.put("createdTime", jobject.getString("createdTime"));
								rowData.put("content", jobject.getString("content"));
								
								String filmName = null;
								String text = String.valueOf(jobject.getString("content"));
								if(text!=null && text.contains("《") && text.contains("》")){
									int start =text.lastIndexOf("《");
									int end = text.lastIndexOf("》")+1;
									if((end-start)>1){
										filmName = text.substring(start, end);
									}
								}
								if(filmName!=null){
									rowData.put("content", jobject.getString("content")+"......");
								}
								
								dataMsgs.add(rowData);
								
							}
							
							
						} catch (Exception e) {
							CrashHandler.logErrorToFile(e);
						}
	                    break;  
	                case 404:  
	                    break;  
	                default:
	                }
	                
	  
	            }  
	        }; 
	        
	        if(data.size()>0){
	        	data.clear();
	        }
	        
	        
	        HttpsGetThread thread = new HttpsGetThread(httpHandler,  
	        		String.valueOf(PropertiesUtilWithEnc.prop.getProperty("domainCall"))+"/itemcomment/getCommentsByKeywords/FiveMaoMessage/1/20/",200);  
	        thread.start();
	    }  
	 
	 int countXXX = 12;
	 Runnable showMessageRunn = new Runnable() {
		
		@Override
		public void run() {
			if(dataMsgs.size()>0 && countXXX >= 0){
				 int idx = (int)(Math.random()*dataMsgs.size());
				 Map<String,Object> rowData = dataMsgs.get(idx);
				 
				 String content = String.valueOf(rowData.get("content"));
				 
				 CusttomToast.makeText(MainActivity.this, content, Toast.LENGTH_LONG).show();
				 
				 countXXX = countXXX - 1;
				 if(countXXX<0){
					 return;
				 }
				 
				httpHandlerMeShowTopMessage.postDelayed(showMessageRunn,5000);
				 
			 }
		}
	};
}
