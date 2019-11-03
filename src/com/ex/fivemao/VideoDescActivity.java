package com.ex.fivemao;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.TreeMap;

import org.json.JSONArray;
import org.json.JSONObject;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.media.MediaMetadataRetriever;
import android.media.ThumbnailUtils;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.provider.MediaStore.Images;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.ex.fivemao.exception.CrashHandler;
import com.ex.fivemao.io.CacheFileUtil;
import com.ex.fivemao.io.PropertiesUtilWithEnc;
import com.ex.fivemao.io.WithEnc;
import com.ex.fivemao.utils.FlagsUtil;
import com.ex.fivemao.utils.NetworkUtil;
import com.example.myvideos.http.HttpsGetThread;


public class VideoDescActivity extends BaseActivity {
	public static String videoUrl ;
	public static String videoID ;
	public static String videoTitle;
	public static String videoFileLink;
	public static String description;
	public static String videoType;
	public static String minimumAge;
	public static String videoCoverpageImageLink;
	
	private WebView web;
	private Button triggerVideoSnapshots;
	private Button triggerVideo;
	private Button dynamicButtonSnap;
	TextView videoDescHint;
	TextView descVideoName;
	TextView descVideoDescription;
	LinearLayout imageDescsLayout;
	ScrollView videoDescActivtyScrollView;
	String imagePath;
	boolean isProcessingSnapshots = false;
	AlertDialog blockUserDialog;
	
	public List<String> videoUrls = new ArrayList<String>();
	
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sub_videodesc);
        web = (WebView)findViewById(R.id.webViewVideo);
        triggerVideoSnapshots = (Button)findViewById(R.id.triggerVideoSnapshots);
        triggerVideo = (Button)findViewById(R.id.triggerVideo);
        videoDescHint = (TextView)findViewById(R.id.videoDescHint);
        descVideoName = (TextView)findViewById(R.id.descVideoName);
        descVideoDescription = (TextView)findViewById(R.id.descVideoDescription);
        imageDescsLayout = (LinearLayout)findViewById(R.id.imageDescsLayout);
        videoDescActivtyScrollView = (ScrollView)findViewById(R.id.videoDescActivtyScrollView);
        
        Intent intent = getIntent();
        if(intent!=null){
	        videoID = intent.getStringExtra("id");
	        videoTitle = intent.getStringExtra("title");
	        videoFileLink = intent.getStringExtra("videoFileLink");
	        description = intent.getStringExtra("description");
	        videoType = intent.getStringExtra("videoType");
	        minimumAge = intent.getStringExtra("minimumAge");
	        if(minimumAge==null || minimumAge.equalsIgnoreCase("null")){
	        	minimumAge = "0";
	        }
	        videoCoverpageImageLink = intent.getStringExtra("coverpageImageLink");
	        
	        descVideoName.setText(videoTitle);
	        descVideoDescription.setText((description==null || description.equals("null"))?"":description);
	        
	        if(videoCoverpageImageLink!=null && videoCoverpageImageLink.length()>0 && !videoCoverpageImageLink.contains("null")){
	        	Bitmap bitmap = NetworkUtil.loadNetImage(videoCoverpageImageLink);
	        	Drawable image = new BitmapDrawable(bitmap);  
	        	int minW = image.getMinimumWidth();
	        	int minH = image.getMinimumHeight();
	        	image.setBounds(0, 0, minW, minH);
	        	descVideoName.setCompoundDrawables(null, null, null, image);
	        }
	        
	        saveVisitedItem();
        }
        
        
        
        triggerVideo.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				Intent intent=new Intent();
				intent.putExtra("id", videoID);
				intent.putExtra("title", videoTitle);
				intent.putExtra("videoFileLink", videoFileLink);
				intent.putExtra("description", description);
				intent.putExtra("videoType", videoType);
				intent.putExtra("videoCoverpageImageLink", videoCoverpageImageLink);
				intent.putExtra("minimumAge", minimumAge);
				if(videoType!=null && videoType.equals("Type_VideoFolderLink")){
					intent.setClass(VideoDescActivity.this, VideoDuoPlayerActivity.class);
				}else{
					intent.setClass(VideoDescActivity.this, VideoPlayerActivity.class);
				}
	            startActivity(intent);  
			}
		});
        triggerVideoSnapshots.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				if(!isProcessingSnapshots){
					if(blockUserDialog==null){
						creaSnapshotsDesc(false);
					}else{
						Toast.makeText(VideoDescActivity.this, "不能重复生成截图", Toast.LENGTH_SHORT).show();
					}
				}else{
					Toast.makeText(VideoDescActivity.this, "已经在处理,请稍等。", Toast.LENGTH_SHORT).show();
				}
			}
		});
        
        
        web.getSettings().setJavaScriptEnabled(true);  //加上这一行网页为响应式的
        web.setWebViewClient(new WebViewClient(){
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
            	videoDescHint.setText("加载视频简介...");
                view.loadUrl(url);
                return true;   //返回true， 立即跳转，返回false,打开网页有延时
            }
            @Override
            public void onPageFinished(WebView view, String url) {
            	super.onPageFinished(view, url);
            	videoDescHint.setVisibility(View.GONE);
            }
        });   
        
        
        
        if(description!=null&&description.startsWith("http")){
        	web.loadUrl(description);
        }else{
        	videoDescHint.setVisibility(View.GONE);
//        	Intent intentSkipToNext=new Intent();
//        	intentSkipToNext.putExtra("id", videoID);
//        	intentSkipToNext.putExtra("title", videoTitle);
//        	intentSkipToNext.putExtra("videoFileLink", videoFileLink);
//        	intentSkipToNext.putExtra("description", description);
//        	intentSkipToNext.putExtra("videoCoverpageImageLink", videoCoverpageImageLink);
//        	intentSkipToNext.putExtra("videoType", videoType);
//        	intentSkipToNext.putExtra("minimumAge", minimumAge);
//        	
//			if(videoType!=null && videoType.equals("Type_VideoFolderLink")){
//				intentSkipToNext.setClass(VideoDescActivity.this, VideoDuoPlayerActivity.class);
//			}else{
//				intentSkipToNext.setClass(VideoDescActivity.this, VideoPlayerActivity.class);
//			}  
//			
//            startActivity(intentSkipToNext); 
        }
        
        
        
        if(VideoPlayerActivity.videosLuckyShakes.contains(String.valueOf(videoID))){
			descVideoName.setText("幸运一天:"+videoTitle);
		}
        
    }

    private void showPopupUserDialog(String title) {
		AlertDialog.Builder builder = new AlertDialog.Builder(this);  
		builder.setIcon(android.R.drawable.ic_dialog_info);  
		builder.setTitle(title);  
		builder.setCancelable(false);
		blockUserDialog = builder.show();
	}
    @Override
    protected void onResume() {
    	super.onResume();
    	startEffectsZoom(triggerVideo);
    	if(FlagsUtil.videoDescActivtyExecuteShareFilmToFriendAction){
    		autoCreateScreenshotAndRecToFriend();
    		FlagsUtil.videoDescActivtyExecuteShareFilmToFriendAction = false;
    	}
    };

	protected void creaSnapshotsDesc(final boolean autoDoScreenShotAndShareFlag) {
		if(videoType!=null && videoType.equals("Type_VideoFolderLink")){
			isProcessingSnapshots = true;
			videoDescHint.setText("加载视频截图...");
			videoDescHint.setVisibility(View.VISIBLE);
			showPopupUserDialog("正在动态截图并保存到手机缓存,根据当前网速,请等待25秒");
			hander.postDelayed(new Runnable() {
				
				@Override
				public void run() {
					createSnapshots(4,autoDoScreenShotAndShareFlag);
				}
			}, 100);
			
		}else{
			videoDescHint.setText("该视频没有截图简介");
			videoDescHint.setVisibility(View.VISIBLE);
		}
	}



	private void createSnapshots(int no,boolean autoDoScreenShotAndShareFlag) {
		if(videoUrls.size()>0){
			createWithUrls(no,autoDoScreenShotAndShareFlag);
		}else{
			extracFromSplits(videoID,no,autoDoScreenShotAndShareFlag);
		}
	}


	private void createWithUrls(int no,boolean autoDoScreenShotAndShareFlag) {
		if(videoUrls.size()>1){
			imageDescsLayout.removeAllViews();
			
			int step = videoUrls.size()/no;
			int counter = 1;
			for(int i = 1; i < videoUrls.size()-1; i=i+step){
				int idx = (int) Math.floor((Math.random()*videoUrls.size()));
				Bitmap shot = createVideoThumbnail(videoUrls.get(idx),0,0);
				TextView title = new TextView(this);
				title.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));  //设置图片宽高
				
				title.setText("【五毛影视】《"+videoTitle+"》截图-"+counter+"-"+i);
				ImageView imageView = new ImageView(this);
		        imageView.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, 440));  //设置图片宽高
		        imageView.setImageBitmap(shot);
		        
		        imageDescsLayout.addView(title);
				imageDescsLayout.addView(imageView);
				counter = counter + 1;
			}
			
			
			TextView womaoShareTextDesc = new TextView(this);
			womaoShareTextDesc.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));  //设置图片宽高
			womaoShareTextDesc.setText("【五毛影视APP安卓版】扫描下方二维码下载。（长按识别二维码）\n	推荐人："+loginUserEmail+"\n	*加群/注册时需要提供推荐人邮箱以便验证和获得奖励\n");
			womaoShareTextDesc.setTextSize(22);
			imageDescsLayout.addView(womaoShareTextDesc);
			
			ImageView imageView = new ImageView(this);
	        imageView.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));  //设置图片宽高
	        imageView.setImageResource(R.drawable.fivemaoappdownload);
			imageDescsLayout.addView(imageView);
			
			TextView tailSpacesSS = new TextView(this);
			tailSpacesSS.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));  //设置图片宽高
			tailSpacesSS.setText(" \n");
			imageDescsLayout.addView(tailSpacesSS);
			
			Button buttonSnap = new Button(this);
			dynamicButtonSnap = buttonSnap;
			buttonSnap.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));  //设置图片宽高
			buttonSnap.setText("屏幕截图&保存&分享");
			buttonSnap.setTextColor(Color.WHITE);
			buttonSnap.setBackgroundResource(R.drawable.bt_round_shape);
			buttonSnap.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {

					doSnapAndShare("manual");
					
				}
			});
			imageDescsLayout.addView(buttonSnap);
			
			TextView tailSpaces = new TextView(this);
			tailSpaces.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));  //设置图片宽高
			tailSpaces.setText(" \n");
			imageDescsLayout.addView(tailSpaces);
			if(autoDoScreenShotAndShareFlag){
				hander.postDelayed(run, 100);
			}
			videoDescHint.setVisibility(View.GONE);
			isProcessingSnapshots = false;
			if(blockUserDialog!=null){
				blockUserDialog.dismiss();
			}
		}
	}



	private void extracFromSplits(String videoID2,final int snapCount,final boolean autoDoScreenShotAndShareFlag) {

    	
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
							return;
						}
						JSONArray videoSplitedFiles = jsonObj.getJSONArray("videoSplitedFiles");
						if(videoSplitedFiles.length()==0){
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
						
						if(videoUrls.size()>0){
							createWithUrls(snapCount,autoDoScreenShotAndShareFlag);
						}else{
							if(blockUserDialog!=null){
								blockUserDialog.dismiss();
							}
						}
						
					} catch (Exception e) {
						CrashHandler.logErrorToFile(e);
					}
                    
                    break;  
                case 404:  
                    // 请求失败
                    break;  
                default:
                }  
            }

        }; 
        
        
        HttpsGetThread thread = new HttpsGetThread(httpHandler,  
        		PropertiesUtilWithEnc.getString("domainCall")+"/film/getFolderFilms/"+videoID2+"/", 200);  
        thread.start();
	}

	private void saveVisitedItem() {
		StringBuilder sb = new StringBuilder();
		String sep = "notakid";
		String coverFileName = "videoCoverpageImageLink_"+videoID+".base64.jpg";
		sb
		.append(videoID).append(sep)
		.append(videoTitle).append(sep)
		.append(coverFileName).append(sep)
		.append(videoType).append(sep)
		.append(videoFileLink).append(sep)
		.append(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date())).append(sep)
		.append(minimumAge).append(sep)
		.append(description).append("\n");
		
		String extraContent = sb.toString();
		CacheFileUtil.appendCacheFile(getCacheDir().getAbsolutePath(), "visithistory.dat",extraContent);
		CacheFileUtil.writeCacheFile(getCacheDir().getAbsolutePath(), coverFileName,videoCoverpageImageLink);
		
	}



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
        	autoCreateScreenshotAndRecToFriend();
        }
        return super.onOptionsItemSelected(item);
    }
    private void autoCreateScreenshotAndRecToFriend() {
    	if(!isProcessingSnapshots){
    		creaSnapshotsDesc(true);
		}else{
			Toast.makeText(VideoDescActivity.this, "已经在处理,请稍等。", Toast.LENGTH_SHORT).show();
		}
	}

	@Override
    public void onBackPressed() {
    	videoID =null;
    	videoTitle=null ;
    	description=null ;
    	videoFileLink =null;
    	super.onBackPressed();
    }
    
    private Bitmap createVideoThumbnail(String url, int width, int height) {
    	String fname = getScreenShotCachName(url);
    	Bitmap bitmap = getCachedVideoScreenShotIfAny(fname);
    	if(bitmap!=null){
    		//Toast.makeText(this, "从本地缓存中加载...", Toast.LENGTH_SHORT).show();
    		return bitmap;
    	}else{
    		//Toast.makeText(this, "从服务器中加载...", Toast.LENGTH_SHORT).show();
    		
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
	        if (kind == Images.Thumbnails.MICRO_KIND && bitmap != null && width>0 && height>0) {
	            bitmap = ThumbnailUtils.extractThumbnail(bitmap, width, height,
	                    ThumbnailUtils.OPTIONS_RECYCLE_INPUT);
	        }
	        
	        if(bitmap!=null){
	        	if(fname.length()>0){
	        		saveBitmap(bitmap, fname);
	        	}
	        }
    	}
        
        return bitmap;
    }



	private String getScreenShotCachName(String url) {
		//00064_019wangfuchenglong.folder/duxia2_001_723948.mp4
		String fname = "";
		if(videoFileLink!=null && videoFileLink.length()>0 && videoFileLink.contains("/") &&
				videoFileLink.endsWith(".folder")){
			fname = videoFileLink.substring(videoFileLink.lastIndexOf("/")+1);
		}
		if(url.contains("/")){
			fname = fname + "." + url.substring(url.lastIndexOf("/")+1);
		}
		if(fname.length()>0){
			fname = fname+".snapshot";
		}
		return fname;
	}
    
	private void deleteFolderOrFile(File file){
		if(file.isDirectory()){
			File[] subF = file.listFiles();
			if(subF!=null&&subF.length>0){
				for(File sub:subF){
					deleteFolderOrFile(sub);
				}
			}else{
				file.delete();
			}
		}else{
			file.delete();
		}
	}
    public void saveBitmap(Bitmap bm,String picName) {
    	File f = new File(getCacheDir().getAbsolutePath()+"/VideoDescSnaps/"+picName);
    	
    	File folderF = f.getParentFile();
    	if(folderF.exists()){
    		File[] films = folderF.listFiles();
    		if(films!=null && films.length>50){
    			int delta = 50;
    			for(int i = 0; i < delta; i++){
    				File fo = films[i];
    				deleteFolderOrFile(fo);
    			}
    		}
    	}
    	
    	  if(!f.getParentFile().exists()){
    		  f.getParentFile().mkdirs();
    	  }
    	  
    	  try {
	    	   FileOutputStream out = new FileOutputStream(f);
	    	   bm.compress(Bitmap.CompressFormat.JPEG, 90, out);
	    	   out.flush();
	    	   out.close();
    	  } catch (FileNotFoundException e) {
    		  CrashHandler.logErrorToFile(e);
    	  } catch (IOException e) {
    		  CrashHandler.logErrorToFile(e);
    	  }
    }
    public Bitmap getCachedVideoScreenShotIfAny(String picName){
    	File f = new File(getCacheDir().getAbsolutePath()+"/VideoDescSnaps/"+picName);
    	
    	if(f.exists()){
    		try {
				return WithEnc.getImageBigMapNoEnc(new FileInputStream(f));
			} catch (FileNotFoundException e) {
				CrashHandler.logErrorToFile(e);
				return null;
			}
    	}
    	return null;
    }
    
    
    public Bitmap shotScrollView(ScrollView scrollView,String imageTag) {
        int h = 0;
        Bitmap bitmap = null;
        for (int i = 0; i < scrollView.getChildCount(); i++) {
          h += scrollView.getChildAt(i).getHeight();
          scrollView.getChildAt(i).setBackgroundColor(Color.parseColor("#ffffff"));
        }
        bitmap = Bitmap.createBitmap(scrollView.getWidth(), h, Bitmap.Config.RGB_565);
        final Canvas canvas = new Canvas(bitmap);
        scrollView.draw(canvas);
        
        if (bitmap != null)
        {
	         try {
	   	       // 获取内置SD卡路径
	   	       String sdCardPath = Environment.getExternalStorageDirectory().getPath()+"/DCIM/Screenshots";
	   	       // 图片文件路径
	   	       String fna =  "womao_screenshot_"+System.currentTimeMillis()+"_"+imageTag+".png";
	   	       imagePath = sdCardPath + File.separator + fna;
	   	       File file = new File(imagePath);
	   	       File parentFile = file.getParentFile();
	   	       
	   	       if(!parentFile.exists()){
	   	    	   parentFile.mkdirs();
	   	       }
	   	       File[] fs = parentFile.listFiles(new FilenameFilter() {
				
				@Override
				public boolean accept(File dir, String filename) {
					if(filename.endsWith("autoshare.png") && filename.startsWith("womao_screenshot_")){
						return true;
					}
					return false;
				}
	   	       });
	   	       
	   	       if(fs!=null && fs.length>0){
	   	    	   for(File f:fs){
	   	    		   if(f.isFile()){
	   	    			   f.delete();
	   	    		   }
	   	    	   }
	   	       }
	   	       FileOutputStream os = new FileOutputStream(file);
	   	       bitmap.compress(Bitmap.CompressFormat.PNG, 100, os);
	   	       os.flush();
	   	       os.close();
	   	       if(imageTag!=null && imageTag.equals("autoshare")){
	   	    	Toast.makeText(this, "上次截屏临时文件已清理", Toast.LENGTH_SHORT).show();
	   	       }else{
	   	    	   Toast.makeText(this, "截屏已经保存到手机截屏路径[/DCIM/Screenshots/"+fna+"]中。", Toast.LENGTH_SHORT).show();
	   	       }
	         } catch (Exception e) {
	       	  CrashHandler.logErrorToFile(e);
	         }
        }
        return bitmap;
      }

	private void doSnapAndShare(String tag) {
		showPopupUserDialog("截图中...");
		triggerVideoSnapshots.setText("五毛影视");
		triggerVideo.setText("看电影,行!");
		dynamicButtonSnap.setText("长按识别二维码");
		Toast.makeText(VideoDescActivity.this, "处理中...", Toast.LENGTH_SHORT).show();
		
		Bitmap screenshot = shotScrollView(videoDescActivtyScrollView,tag);
		
		popUserWhatsBeenCaptured(screenshot,imagePath);
		
		triggerVideoSnapshots.setText("加载截图简介");
		triggerVideo.setText("点我马上播放");
		dynamicButtonSnap.setText("屏幕截图&保存&分享");
		
		if(blockUserDialog!=null){
			blockUserDialog.dismiss();
		}
		
	}
	
	private void popUserWhatsBeenCaptured(Bitmap bm,final String imgFilePath) {
		final Dialog dia = new Dialog(this);
		
    	dia.setTitle("截图已保存 分享给朋友");
    	dia.setContentView(R.layout.dialog_showrecscreenshot);
    	ImageView imageView = (ImageView) dia.findViewById(R.id.imgShowRecScrenshot);
    	Button btnPositiveShowRecImg = (Button) dia.findViewById(R.id.btnPositiveShowRecImg);
    	btnPositiveShowRecImg.setTextSize(14);
    	Button btnNegativeShowRecImg = (Button) dia.findViewById(R.id.btnNegativeShowRecImg);
    	btnNegativeShowRecImg.setTextSize(14);
    	btnNegativeShowRecImg.setText("取消");
		imageView.setImageBitmap(bm);
		
		btnPositiveShowRecImg.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				dia.dismiss();
				shareImage(imgFilePath);
			}
		});
		btnNegativeShowRecImg.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				dia.dismiss();
			}
		});
		
		dia.show();
		
	}

	private Runnable run = new Runnable() {
		
		@Override
		public void run() {
			doSnapAndShare("autoshare");
		}
	};
	
	Handler hander = new Handler();
    
}
