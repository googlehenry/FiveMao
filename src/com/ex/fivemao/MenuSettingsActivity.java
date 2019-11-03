package com.ex.fivemao;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import com.ex.fivemao.audioservice.MusicService;
import com.ex.fivemao.exception.CrashHandler;
import com.ex.fivemao.io.CacheFileUtil;
import com.ex.fivemao.ui.MakeSureDialog;


public class MenuSettingsActivity extends BaseActivity {
	private Button setVideosLoadingStrategy;
	private Button cleanVideoCache;
	private Button setBGMusicSetting;
	private Button cleanPlayHistory;
	private Button secureLockApp;
	private Button checkAppVersionInfo;
	private boolean cacheFlag = false;
	private boolean bgMusicOn = true;
	
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sub_settings);
        
        setVideosLoadingStrategy = (Button) findViewById(R.id.setVideosLoadingStrategy);
        cleanVideoCache = (Button) findViewById(R.id.cleanVideoCache);
        cleanVideoCache = (Button) findViewById(R.id.cleanVideoCache);
        setBGMusicSetting = (Button) findViewById(R.id.setBGMusicSetting);
        cleanPlayHistory = (Button) findViewById(R.id.cleanPlayHistory);
        secureLockApp = (Button) findViewById(R.id.secureLockApp);
        checkAppVersionInfo = (Button) findViewById(R.id.checkAppVersionInfo);
        
        
        setVideosLoadingStrategy.setOnClickListener(new OnClickListener() {
        	@Override
        	public void onClick(View v) {
        		showChooseVideoLoadingStrategyDialog();
        	}
        	
        });
        
        secureLockApp.setOnClickListener(new OnClickListener() {
        	@Override
        	public void onClick(View v) {
        		showLockFunctionDesc();
        	}
        });
        
        setBGMusicSetting.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if(bgMusicOn){
					turnoffBGMusic();
					setBGMusicSetting.setText("打开背景音乐");
				}else{
					turnonBGMusic();
					setBGMusicSetting.setText("关闭背景音乐");
				}
			}

		});
        
        cleanVideoCache.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if(cacheFlag){
					cleanCache();
				}else{
					openCache();
				}
			}

		});
        cleanPlayHistory.setOnClickListener(new OnClickListener() {
        	@Override
        	public void onClick(View v) {
        		MakeSureDialog dialog = new MakeSureDialog();
    			dialog.setTextTitle("清理提示");
    			dialog.setContent("清理播放历史纪录之后,您在我的>>播放历史列表>>将看到0条记录,如果记录过多,建议清除。");
    			dialog.setTextSureButton("确定");
    			dialog.setTextCancelButton("取消");
    			
    			dialog.setDialogClickListener(new MakeSureDialog.onDialogClickListener() {  
    		    	   @Override  
    		    	   public void onSureClick() {
    		    		   CacheFileUtil.writeCacheFile(getCacheDir().getAbsolutePath(), "visithistory.dat","");
    		    		   //String coverFileName = "videoCoverpageImageLink_"+item.get("id")+".base64.jpg";
    		    		   File[] cachedImageFiles = getCacheDir().listFiles(new FilenameFilter() {
							@Override
							public boolean accept(File dir, String filename) {
								if(filename.startsWith("videoCoverpageImageLink_")&&
										filename.endsWith(".base64.jpg")	){
									return true;
								}
								return false;
							}
    		    		   });
    		    		   
    		    		   for(File cif:cachedImageFiles){
    		    			   cif.delete();
    		    		   }
    		    		   
    		    		   AlertDialog.Builder builder = new AlertDialog.Builder(MenuSettingsActivity.this);  
	    		   	        builder.setIcon(android.R.drawable.ic_dialog_info);  
	    		   	        builder.setTitle("提示：播放历史数据清理完毕！");  
	    		   	        builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {  
	    		   	            public void onClick(DialogInterface dialog, int which) {  
	    		   	            }
	    		   	        });  
	    		   	        builder.show();  
			    			
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
        
        checkAppVersionInfo.setOnClickListener(new OnClickListener() {
        	@Override
        	public void onClick(View v) {
        		loadVersionInfo();
        	}
        });
        Boolean bgMusic = loadBGMusicSettings();
        if(bgMusic!=null && bgMusic){
        	bgMusicOn = true;
        	setBGMusicSetting.setText("关闭背景音乐");
        }else{
        	bgMusicOn = false;
        	setBGMusicSetting.setText("打开背景音乐");
        }
        
        loadCacheSettings();
        
        if(lockPasswordForThisPhone!=null && lockPasswordForThisPhone.trim().length()>0){
        	secureLockApp.setText("关闭APP密码锁");
    	}else{
    		secureLockApp.setText("打开APP密码锁");
    	}
    }

    protected void showLockFunctionDesc() {
    	if(lockPasswordForThisPhone!=null && lockPasswordForThisPhone.trim().length()>0){
    		AlertDialog.Builder builder = new AlertDialog.Builder(this);  
            builder.setIcon(android.R.drawable.ic_dialog_info);  
            builder.setTitle("温馨提示");  
            builder.setMessage("关闭APP密码锁后任何人都可以在本手机打开APP，您确定吗？");
            builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {  
                public void onClick(DialogInterface dialog, int which) {  
                	dialog.dismiss();
                	removeSecureLockPassword();
                }  
            });  
            builder.show();
    		
    	}else{
    		AlertDialog.Builder builder = new AlertDialog.Builder(this);  
            builder.setIcon(android.R.drawable.ic_dialog_info);  
            builder.setTitle("温馨提示");  
            builder.setMessage("打开APP密码锁后每次打开APP(包括退出登录五毛APP/切换登录用户等)都需要输入解锁密码，您确定吗？");
            builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {  
                public void onClick(DialogInterface dialog, int which) {  
                	dialog.dismiss();
                	setSecureLockPassword();
                }  
            });  
            builder.show();
            
    	}
	}

	protected void removeSecureLockPassword() {
		saveSecureOpenAppPassword("");
		Toast.makeText(MenuSettingsActivity.this, "密码已经取消", Toast.LENGTH_SHORT).show();
		lockPasswordForThisPhone = null;
		secureLockApp.setText("打开APP密码锁");
	}

	protected void setSecureLockPassword() {
		final EditText editText = new EditText(MenuSettingsActivity.this);
        editText.setHint("输入密码(建议纯数字)");
        
        AlertDialog.Builder inputDialog = new AlertDialog.Builder(MenuSettingsActivity.this);
        inputDialog.setTitle("请输入打开APP密码：").setView(editText);
        inputDialog.setPositiveButton("确定", 
            new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
            	String input = editText.getText().toString().trim();
            	if(input.length()>0){
            		saveSecureOpenAppPassword(input);
            		Toast.makeText(MenuSettingsActivity.this, "密码已经保存", Toast.LENGTH_SHORT).show();
            		lockPasswordForThisPhone = input;
            		secureLockApp.setText("关闭APP密码锁");
            	}else{
            		Toast.makeText(MenuSettingsActivity.this, "密码不能为空", Toast.LENGTH_SHORT).show();
            	}
            }
        }).show();
	}

	protected void saveSecureOpenAppPassword(String password) {
		CacheFileUtil.writeCacheFile(getCacheDir().getAbsolutePath(), "LockAppSettings.duo.dat",password);
	}

	protected void turnonBGMusic() {
    	CacheFileUtil.writeCacheFile(getCacheDir().getAbsolutePath(), "bgmusic.duo.dat","Y");
    	AlertDialog.Builder builder = new AlertDialog.Builder(this);  
        builder.setIcon(android.R.drawable.ic_dialog_info);  
        builder.setTitle("提示");  
        builder.setMessage("背景音乐已经打开,重新打开首页查看,播放电影后会自动停止。");
        builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {  
            public void onClick(DialogInterface dialog, int which) {  
            	dialog.dismiss();
            }  
        });  
        
        builder.show();
        
        bgMusicOn = true;
	}

	protected void turnoffBGMusic() {
		stopBGMusicService();
		CacheFileUtil.writeCacheFile(getCacheDir().getAbsolutePath(), "bgmusic.duo.dat","N");
    	AlertDialog.Builder builder = new AlertDialog.Builder(this);  
        builder.setIcon(android.R.drawable.ic_dialog_info);  
        builder.setTitle("提示");  
        builder.setMessage("背景音乐已经关闭。");
        builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {  
            public void onClick(DialogInterface dialog, int which) {  
            	dialog.dismiss();
            }  
        });  
        builder.show();  
        bgMusicOn = false;
	}

	int yourChoice;
	
    private int loadAndSetVideoLoadingStrategy() {
    	String strategy = CacheFileUtil.readCacheFile(getCacheDir().getAbsolutePath(), "videoloadingstrategy.dat");
    	
    	try{
    		if(strategy!=null && strategy.contains("notakid")){
    			String[] pageInfos = strategy.split("notakid");
    			if(pageInfos!=null && pageInfos.length>=2){
        			String code = pageInfos[0];
        			String desc = pageInfos[1];
	        		int videoLoadingStrategyCode  = Integer.parseInt(code);
	        		yourChoice = videoLoadingStrategyCode;
	        		return videoLoadingStrategyCode;
    			}
    		}else{
    			showChooseVideoLoadingStrategyDialog();
    		}
    	}catch(Exception e){
    		CrashHandler.logErrorToFile(e);
    		return 0;
    	}
    	
    	return 0;
	}
    
    
    private void showChooseVideoLoadingStrategyDialog(){
        
        yourChoice = 0;
        AlertDialog.Builder singleChoiceDialog = 
            new AlertDialog.Builder(MenuSettingsActivity.this);
        singleChoiceDialog.setTitle("电影排序设置");
        
        int defaultSelection = loadAndSetVideoLoadingStrategy();
        
        // 第二个参数是默认选项，此处设置为0
        singleChoiceDialog.setSingleChoiceItems(VideoActivity.items, defaultSelection, 
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
                	
                    Toast.makeText(MenuSettingsActivity.this, 
                    "你选择了" + VideoActivity.items[yourChoice]+",保存成功。", 
                    Toast.LENGTH_SHORT).show();
                    
                    CacheFileUtil.writeCacheFile(getCacheDir().getAbsolutePath(), "videoloadingstrategy.dat",yourChoice+"notakid"+VideoActivity.items[yourChoice]);
                }
            }
        });
        
        singleChoiceDialog.show();
        
    }
    
    

    protected void openCache() {
    	CacheFileUtil.writeCacheFile(getCacheDir().getAbsolutePath(), "playercache.duo.dat","Y");
    	AlertDialog.Builder builder = new AlertDialog.Builder(this);  
        builder.setIcon(android.R.drawable.ic_dialog_info);  
        builder.setTitle("提示");  
        builder.setMessage("缓存已经打开,如果遇到视频加载问题,请到菜单>>设置界面，手动这里清理缓存。");
        builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {  
            public void onClick(DialogInterface dialog, int which) {  
            	dialog.dismiss();
            }  
        });  
        
        builder.show();  
        
        loadCacheSettings();
       
	}
    

	public void loadCacheSettings() {
    	String cacheFileContent = CacheFileUtil.readCacheFile(getCacheDir().getAbsolutePath(), "playercache.duo.dat");
    	if(cacheFileContent!=null){
    		if(cacheFileContent.trim().equalsIgnoreCase("Y")){
    			cacheFlag = true;
    			cleanVideoCache.setText("清除视频缓存");
    		}else{
    			cacheFlag = false;
    			cleanVideoCache.setText("打开视频缓存");
    		}
    	}
	}
	


	private void loadVersionInfo() {
    	String content = CacheFileUtil.readCacheFile(getCacheDir().getAbsolutePath(), "fivemaoversionfeature.dat");
    	if(content==null || content.length()==0){
    		try {
				InputStream in = getAssets().open("FiveMao.version");
				int lengh = in.available();
				byte[] buf = new byte[lengh];
				in.read(buf);
				content = new String(buf);
				in.close();
			} catch (IOException e) {
				CrashHandler.logErrorToFile(e);
			}
    	}
    	AlertDialog.Builder builder = new AlertDialog.Builder(this);  
        builder.setIcon(android.R.drawable.ic_dialog_info);  
        builder.setTitle("版本信息");  
        builder.setMessage(content);
        builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {  
            public void onClick(DialogInterface dialog, int which) {  
            	dialog.dismiss();
            }  
        });  
        
        builder.show();  
    	
	}



	@Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        //getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        return super.onOptionsItemSelected(item);
    }
    
   
}
