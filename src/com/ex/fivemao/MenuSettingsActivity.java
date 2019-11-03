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
					setBGMusicSetting.setText("�򿪱�������");
				}else{
					turnonBGMusic();
					setBGMusicSetting.setText("�رձ�������");
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
    			dialog.setTextTitle("������ʾ");
    			dialog.setContent("��������ʷ��¼֮��,�����ҵ�>>������ʷ�б�>>������0����¼,�����¼����,���������");
    			dialog.setTextSureButton("ȷ��");
    			dialog.setTextCancelButton("ȡ��");
    			
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
	    		   	        builder.setTitle("��ʾ��������ʷ����������ϣ�");  
	    		   	        builder.setPositiveButton("ȷ��", new DialogInterface.OnClickListener() {  
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
        	setBGMusicSetting.setText("�رձ�������");
        }else{
        	bgMusicOn = false;
        	setBGMusicSetting.setText("�򿪱�������");
        }
        
        loadCacheSettings();
        
        if(lockPasswordForThisPhone!=null && lockPasswordForThisPhone.trim().length()>0){
        	secureLockApp.setText("�ر�APP������");
    	}else{
    		secureLockApp.setText("��APP������");
    	}
    }

    protected void showLockFunctionDesc() {
    	if(lockPasswordForThisPhone!=null && lockPasswordForThisPhone.trim().length()>0){
    		AlertDialog.Builder builder = new AlertDialog.Builder(this);  
            builder.setIcon(android.R.drawable.ic_dialog_info);  
            builder.setTitle("��ܰ��ʾ");  
            builder.setMessage("�ر�APP���������κ��˶������ڱ��ֻ���APP����ȷ����");
            builder.setPositiveButton("ȷ��", new DialogInterface.OnClickListener() {  
                public void onClick(DialogInterface dialog, int which) {  
                	dialog.dismiss();
                	removeSecureLockPassword();
                }  
            });  
            builder.show();
    		
    	}else{
    		AlertDialog.Builder builder = new AlertDialog.Builder(this);  
            builder.setIcon(android.R.drawable.ic_dialog_info);  
            builder.setTitle("��ܰ��ʾ");  
            builder.setMessage("��APP��������ÿ�δ�APP(�����˳���¼��ëAPP/�л���¼�û���)����Ҫ����������룬��ȷ����");
            builder.setPositiveButton("ȷ��", new DialogInterface.OnClickListener() {  
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
		Toast.makeText(MenuSettingsActivity.this, "�����Ѿ�ȡ��", Toast.LENGTH_SHORT).show();
		lockPasswordForThisPhone = null;
		secureLockApp.setText("��APP������");
	}

	protected void setSecureLockPassword() {
		final EditText editText = new EditText(MenuSettingsActivity.this);
        editText.setHint("��������(���鴿����)");
        
        AlertDialog.Builder inputDialog = new AlertDialog.Builder(MenuSettingsActivity.this);
        inputDialog.setTitle("�������APP���룺").setView(editText);
        inputDialog.setPositiveButton("ȷ��", 
            new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
            	String input = editText.getText().toString().trim();
            	if(input.length()>0){
            		saveSecureOpenAppPassword(input);
            		Toast.makeText(MenuSettingsActivity.this, "�����Ѿ�����", Toast.LENGTH_SHORT).show();
            		lockPasswordForThisPhone = input;
            		secureLockApp.setText("�ر�APP������");
            	}else{
            		Toast.makeText(MenuSettingsActivity.this, "���벻��Ϊ��", Toast.LENGTH_SHORT).show();
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
        builder.setTitle("��ʾ");  
        builder.setMessage("���������Ѿ���,���´���ҳ�鿴,���ŵ�Ӱ����Զ�ֹͣ��");
        builder.setPositiveButton("ȷ��", new DialogInterface.OnClickListener() {  
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
        builder.setTitle("��ʾ");  
        builder.setMessage("���������Ѿ��رա�");
        builder.setPositiveButton("ȷ��", new DialogInterface.OnClickListener() {  
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
        singleChoiceDialog.setTitle("��Ӱ��������");
        
        int defaultSelection = loadAndSetVideoLoadingStrategy();
        
        // �ڶ���������Ĭ��ѡ��˴�����Ϊ0
        singleChoiceDialog.setSingleChoiceItems(VideoActivity.items, defaultSelection, 
            new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                yourChoice = which;
            }
        });
        
        singleChoiceDialog.setPositiveButton("ȷ��", 
            new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (yourChoice != -1) {
                	
                    Toast.makeText(MenuSettingsActivity.this, 
                    "��ѡ����" + VideoActivity.items[yourChoice]+",����ɹ���", 
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
        builder.setTitle("��ʾ");  
        builder.setMessage("�����Ѿ���,���������Ƶ��������,�뵽�˵�>>���ý��棬�ֶ����������档");
        builder.setPositiveButton("ȷ��", new DialogInterface.OnClickListener() {  
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
    			cleanVideoCache.setText("�����Ƶ����");
    		}else{
    			cacheFlag = false;
    			cleanVideoCache.setText("����Ƶ����");
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
        builder.setTitle("�汾��Ϣ");  
        builder.setMessage(content);
        builder.setPositiveButton("ȷ��", new DialogInterface.OnClickListener() {  
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
