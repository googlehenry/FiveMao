package com.ex.fivemao;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.ClipboardManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.ex.fivemao.audioservice.MusicService;
import com.ex.fivemao.exception.CrashHandler;
import com.ex.fivemao.io.CacheFileUtil;
import com.ex.fivemao.io.PropertiesUtilWithEnc;
import com.ex.fivemao.ui.BaseIntent;
import com.ex.fivemao.ui.MakeSureDialog;
import com.ex.fivemao.utils.FlagsUtil;
import com.example.myvideos.http.HttpsGetThread;
import com.example.myvideos.http.MultipartFormHttpsPostThread;
import com.example.myvideos.http.proxy.Utils;

public class BaseActivity extends Activity {
	protected String loginUserName;
	protected String loginUserBirthday;
	protected String loginUserEmail;
	protected String loginUserProxyEmail;
	
	protected String lockPasswordForThisPhone;
	protected boolean lockOpened = false;
	protected Date membershipExpiryDate;
	
	protected String callerClassName;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		initUserData();
		Intent intent = getIntent();
		if(intent instanceof BaseIntent){
        	BaseIntent baseIntent = (BaseIntent)intent;
        	callerClassName = baseIntent.getCallerActivityClassName();
        }
		loadLockAppSettings(true);//for this phone, not this user
		
		//registerReceiver(mHomeKeyEventReceiver, new IntentFilter(  Intent.ACTION_CLOSE_SYSTEM_DIALOGS));  
		ActionBar actionBar = getActionBar();  
		if(this instanceof MainActivity){
			
		}else{
			actionBar.setDisplayShowHomeEnabled(false);
			actionBar.setDisplayHomeAsUpEnabled(true); 
			actionBar.setHomeAsUpIndicator(R.drawable.backarrow);  
		}
	}
	
	private BroadcastReceiver mHomeKeyEventReceiver = new BroadcastReceiver() {  
        String SYSTEM_REASON = "reason";  
        String SYSTEM_HOME_KEY = "homekey";  
        String SYSTEM_HOME_KEY_LONG = "recentapps";  
           
        @Override  
        public void onReceive(Context context, Intent intent) {  
            String action = intent.getAction();  
            if (action.equals(Intent.ACTION_CLOSE_SYSTEM_DIALOGS)) {  
                String reason = intent.getStringExtra(SYSTEM_REASON);  
                if (TextUtils.equals(reason, SYSTEM_HOME_KEY)) {  
                	//��ʾ����home��,�����˺�̨  
                	homePressedQuickly();
                }else if(TextUtils.equals(reason, SYSTEM_HOME_KEY_LONG)){  
                    //��ʾ����home��,��ʾ���ʹ�õĳ����б�  
                	onBackPressed();
                }  
            }   
        }  
    };
	
    
    protected void homePressedQuickly(){
    	//onBackPressed();
    }
    
    protected void homePressedLong(){
    	//onBackPressed();
    }
	
	@Override
	protected void onDestroy() {
		super.onDestroy();
		
	}
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		int id = item.getItemId();
		if(id==R.id.action_settings){
			startSettingsPage(); 
        	return true;
        } else if(id==android.R.id.home){
        	onBackPressed();
        	return true;
        }else if(id==R.id.action_shareToWechat){
        	showRecPageChoice();
        	return true;
        }else if(id==R.id.action_contactSuport){
        	contactSupport();
        	return true;
        }else if(id==R.id.action_quicklinkSearchFilms){
        	gotoSearchFilmActivity();
        }else if(id==R.id.action_helpGuide){
        	gotoHelpGuideActivity();
        }else if(id==R.id.action_quicklinkFilmList){
        	gotoFilmListActivity();
        }
		return super.onOptionsItemSelected(item);
	}
	

	protected void startEffectsZoom(View view){
		if(view!=null){
			Animation zoom = AnimationUtils.loadAnimation(this, R.anim.zoom2);
			view.startAnimation(zoom);
		}
	}
	private void showRecPageChoice() {

		MakeSureDialog dialog = new MakeSureDialog();
		dialog.setTextTitle("�Ƽ������� ׬20%Ӷ��");
		if(loginUserName==null){
			dialog.setContent("��ȥ����¼��ʹ�������Ƽ�,����ֱ�ӡ��������ӡ������ѣ�");
			dialog.setTextSureButton("ȥ��¼ҳ��");
		}else{
			recToFriendsDialog();
			return;
		}
		dialog.setTextCancelButton("ֱ�ӷ�������");
		
		dialog.setDialogClickListener(new MakeSureDialog.onDialogClickListener() {  
	    	   @Override  
	    	   public void onSureClick() {
	    		   if(loginUserName==null){
	    			   gotoMePage();
	    		   }else{
	    			   gotoRecAppActivty();
	    		   }
	    	   }
	    	   @Override  
	    	   public void onCancelClick() {
	    		   shareToWechatAction();
	    	   }
	    	   	
	    	});
			try{
          		dialog.show(getFragmentManager(),"");
          	}catch(Exception e1){
          		CrashHandler.logErrorToFile(e1);
          	} 
		
	}

	protected void gotoMePage() {
		Intent intent=new Intent();
        intent.setClass(this, MeActivity.class);  
        startActivity(intent);   
	}

	protected void gotoRecAppActivty() {
		Intent intent=new Intent();
		intent.setClass(this, ReconToFriendsActivity.class);  
		startActivity(intent);
	}

	protected void gotoHelpGuideActivity() {
		Intent intent=new Intent();
        intent.setClass(this, HelpGuideActivity.class);  
        startActivity(intent); 
	}

	public Boolean loadBGMusicSettings() {
		boolean bgMusicOn = false;
    	String cacheFileContent = CacheFileUtil.readCacheFile(getCacheDir().getAbsolutePath(), "bgmusic.duo.dat");
    	if(cacheFileContent!=null){
    		if(cacheFileContent.trim().equalsIgnoreCase("Y")){
    			bgMusicOn = true;
    		}else{
    			bgMusicOn = false;
    		}
    	}else{
    		return false;
    	}
    	return bgMusicOn;
	}
	protected void resetBGMusic() {
		Intent intent = new Intent(this, MusicService.class);
		MusicService.action = "RESET";
		startService(intent);
	}
	protected void pauseBGMusic() {
		Intent intent = new Intent(this, MusicService.class);
		MusicService.action = "PAUSE";
		startService(intent);
	}
	
	protected void pauseBGMusicBySystem() {
		Intent intent = new Intent(this, MusicService.class);
		MusicService.action = "PAUSEBYSYS";
		startService(intent);
	}

	protected void startBGMusic() {
		Intent intent = new Intent(this, MusicService.class);
		MusicService.action = "RESUME";
		startService(intent);
	}

	protected void startBGMusicAfterReset() {
		Intent intent = new Intent(this, MusicService.class);
		MusicService.action = "START";
		startService(intent);
	}

	protected void stopBGMusicService() {
		Intent intent = new Intent(this, MusicService.class);
		stopService(intent);
	}

	protected void gotoFilmListActivity() {
		Intent intent=new Intent();
		intent.setClass(this, VideoActivity.class);
		startActivity(intent);
	}
	protected void gotoSearchFilmActivity() {
		Intent intent=new Intent();
        intent.setClass(this, SearchActivity.class);  
        startActivity(intent); 
	}

	private void startSettingsPage() {
		Intent intent=new Intent();
		intent.setClass(this, MenuSettingsActivity.class);  
		startActivity(intent);
	}
	protected void cleanCache() {
		try {
			
			File[] files = Utils.listVideoCachesInDir(this);
			if(files==null){
				return;
			}
			StringBuilder sb = new StringBuilder();
			for(File file:files){
				sb.append(file.getName()).append("\n");
			}
			
			int count = Utils.cleanVideoCacheDir(this);
			//logged in, has bought this film, then has the ability to get from sever directly
			
			MakeSureDialog dialog = new MakeSureDialog();
			dialog.setTextTitle("����������ʾ");
			dialog.setContent("���������ļ�"+count+"����");
			dialog.setTextSureButton("ȷ��");
			dialog.setTextCancelButton("�رջ���");
			if(sb.length()>0){
				dialog.setContent("���������ļ�"+count+"����\n�ļ��б�����:\n"+sb.toString());
			}
			
			dialog.setDialogClickListener(new MakeSureDialog.onDialogClickListener() {  
		    	   @Override  
		    	   public void onSureClick() {
		    	   }
		    	   @Override  
		    	   public void onCancelClick() {
		    		   //�رջ���
		    		    CacheFileUtil.writeCacheFile(getCacheDir().getAbsolutePath(), "playercache.duo.dat","N");
		    		    if(BaseActivity.this instanceof MenuSettingsActivity){
		    		    	((MenuSettingsActivity)BaseActivity.this).loadCacheSettings();
		    		    }
		    	   }
		    	   	
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
	protected void shareToWechatAction() {
		
		 ClipboardManager cm = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
		 String msg = PropertiesUtilWithEnc.getString("shareWechatText");
		 if(loginUserEmail!=null){
			 msg = "�Ƽ���:"+loginUserEmail+"��"+ msg;
		 }
		 cm.setText(msg);
		 
		 MakeSureDialog dialog = new MakeSureDialog();  
		 dialog.setTextTitle("�Ƽ�APP������");
		 dialog.setTextSureButton("΢�ŷ���");
		 dialog.setTextCancelButton("QQ����");
		 dialog.setContent(msg+" �Ѹ��Ƶ����а塣ȥ����ɡ�");
		 
		dialog.setDialogClickListener(new MakeSureDialog.onDialogClickListener() {  
		   @Override  
		   public void onSureClick() {
		   	try{
		    		openWechat();
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
		    		openQQ();
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
		});  
		try{
      		dialog.show(getFragmentManager(),"");
      	}catch(Exception e){
      		CrashHandler.logErrorToFile(e);
      	} 

	}
	
	@Override
	protected void onResume() {
		super.onResume();
		initUserData();
		loadLockAppSettings(false);
	};

	protected void initUserData() {
    	//CACHE, loginUserName+"notakid"+loginUserBirthday
    	//Toast.makeText( MainActivity.this, "�����û���Ϣ", Toast.LENGTH_SHORT).show();
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
            	
            	if(userInfoLoaded.length>=4){
            		String[] searchHiss = userInfoLoaded[3].split(",");
            		for(int i = 0; i < searchHiss.length; i++){
            			if(!SearchActivity.searchHistory.contains(searchHiss[i])){
            				SearchActivity.searchHistory.add(searchHiss[i]);
            			}
        			}
            		
            	}
            	
            	loadFilmsAuthed();
            	getFilmsAuth();
            }
        }
	}
	
	protected void loadFilmsAuthed() {  
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
                    // ����ʧ��  
                    Log.e("TAG", "����ʧ��!");  
                    break;  
                }  
  
            }  
        },PropertiesUtilWithEnc.getString("domainCall")+"/user/json/getAccessItems/"+loginUserName, 200);  
        thread.start();
    }
	protected void contactSupport(){
		
		final Dialog dia = new Dialog(this);
		
		
    	dia.setTitle("ʹ������������ ��ϵ�ͷ�");
    	
    	dia.setContentView(R.layout.dialog_showrecscreenshot);
    	ImageView imageView = (ImageView) dia.findViewById(R.id.imgShowRecScrenshot);
    	Button btnPositiveShowRecImg = (Button) dia.findViewById(R.id.btnPositiveShowRecImg);
    	btnPositiveShowRecImg.setText("ע������");
    	btnPositiveShowRecImg.setTextSize(14);
    	Button btnNegativeShowRecImg = (Button) dia.findViewById(R.id.btnNegativeShowRecImg);
    	btnNegativeShowRecImg.setTextSize(14);
    	btnNegativeShowRecImg.setText("������ʽ��ϵ");
		imageView.setImageResource(R.drawable.contactsupportqrcode);
		
		btnPositiveShowRecImg.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				dia.dismiss();
				
				AlertDialog.Builder builder = new AlertDialog.Builder(BaseActivity.this);  
	            builder.setIcon(android.R.drawable.ic_dialog_info);  
	            builder.setTitle("΢�ź��� ��֤��ʽ");
	            builder.setMessage("���΢�ź���ʱ��ʹ��������֤��Ϣ:\n��ʽ��#��ë�û���#�Ƽ�������\n���磺#testabc#testxyz@qq.com\n�������Աʶ��");
	            builder.show(); 
	            
			}
		});
		btnNegativeShowRecImg.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				dia.dismiss();
				contactViaText();
			}
		});
		
		dia.show();
        
	}

	private void contactViaText() {
		String ns = PropertiesUtilWithEnc.getString("contactSupportNames").split(",")[0]+"\n"
				 +PropertiesUtilWithEnc.getString("contactSupportNames").split(",")[1]+"\n"
				 +PropertiesUtilWithEnc.getString("contactSupportNames").split(",")[2]+"\n";
		 
			 MakeSureDialog dialog = new MakeSureDialog();  
			 dialog.setTextSureButton("΢����ϵ");
			 dialog.setTextCancelButton("QQ��ϵ");
			 
			 ClipboardManager cm = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
     		 cm.setText(ns);
     		
			 dialog.setContent(ns +"�Ѿ����Ƶ����а塣���������Ϊ�Ƽ��ˡ�");
			 
			 
	   dialog.setDialogClickListener(new MakeSureDialog.onDialogClickListener() {  
	       @Override  
	       public void onSureClick() {
	       	try{
	           	ClipboardManager cm = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
	           	String text = PropertiesUtilWithEnc.getString("contactSupportNames").split(",")[0];
	        		cm.setText(text);
	        		
	        		AlertDialog.Builder builder = new AlertDialog.Builder(BaseActivity.this);  
		            builder.setIcon(android.R.drawable.ic_dialog_info);  
		            builder.setTitle(text+" �Ѹ���");
		            builder.setPositiveButton("��΢��", new DialogInterface.OnClickListener() {  
		                public void onClick(DialogInterface dialog, int which) {
		                	dialog.dismiss();  
		                	openWechat();
		                }
		            });  
		            
		            builder.show(); 
	        		
	        		
	        		
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
	           	ClipboardManager cm = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
	           	String text = PropertiesUtilWithEnc.getString("contactSupportNames").split(",")[1];
        		cm.setText(text);
        		
        		AlertDialog.Builder builder = new AlertDialog.Builder(BaseActivity.this);  
	            builder.setIcon(android.R.drawable.ic_dialog_info);  
	            builder.setTitle(text+" �Ѹ���");
	            builder.setPositiveButton("��QQ", new DialogInterface.OnClickListener() {  
	                public void onClick(DialogInterface dialog, int which) {
	                	dialog.dismiss();  
	                	openQQ();
	                }
	            });  
	            
	            builder.show(); 
	        		
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
     	}catch(Exception e){
     		CrashHandler.logErrorToFile(e);
     	}
	}
	
	
	protected void shareToQQWechat(String text){
		 	final String ns = text;
		 
			 MakeSureDialog dialog = new MakeSureDialog();  
			 dialog.setTextSureButton("΢����ϵ");
			 dialog.setTextCancelButton("QQ��ϵ");
			 
			 ClipboardManager cm = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
     		 cm.setText(ns);
     		 dialog.setContent("��" + ns +"���Ѿ����Ƶ����а�");
     		 
	   dialog.setDialogClickListener(new MakeSureDialog.onDialogClickListener() {  
	       @Override  
	       public void onSureClick() {
	       	try{
	        		openWechat();
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
	        		openQQ();
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
    	}catch(Exception e){
    		CrashHandler.logErrorToFile(e);
    	}
	}
	
	protected void broadCastMessageToBoard(String textContent) throws UnsupportedEncodingException {
    	Map<String,String> pairs = new HashMap<String,String>();
    	pairs.put("comment", textContent);
    	pairs.put("uname", loginUserName);
    	
    	MultipartFormHttpsPostThread thread = new MultipartFormHttpsPostThread(new Handler() { 
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
                		}
					} catch (Exception e) {
						CrashHandler.logErrorToFile(e);
					}
                    break;  
                case 404:  
                    break;  
                }  
  
            }  
        },String.valueOf(PropertiesUtilWithEnc.prop.getProperty("domainCall"))+"/itemcomment/addCommentWithType/FiveMaoMessage/",pairs,null);  
        thread.start();
	}
	
	protected String starMyName(String loginUserName) {
		if(loginUserName!=null && loginUserName.length()>=4){
			StringBuilder name = new StringBuilder();
			for(int i = 0; i < loginUserName.length(); i++){
				if((i+1)%2==0){
					name.append("*");
				}else{
					name.append(loginUserName.charAt(i));
				}
			}
			return name.toString();
		}
		return "�����û�";
	}  
	
	protected void getFilmsAuth() {
        HttpsGetThread thread = new HttpsGetThread(new Handler() { 
            @Override  
            public void handleMessage(Message msg) {  
                super.handleMessage(msg);  
                String result = (String) msg.obj;  
                //etShowInfo.setText(result+":"+url);
                switch (msg.what) {  
                case 200:  
                	
					try {
						membershipExpiryDate = null;
						JSONArray jsonarr = new JSONArray(result);
					
						if(jsonarr.length()<=0){
							return;
						}
						SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:SS");
						
						for(int i = 0 ; i < jsonarr.length(); i++){
							JSONObject jobject = jsonarr.getJSONObject(i);
							String createdExactTimeStr = jobject.getString("createdExactTimeStr");
							Date createdExactTime = sdf.parse(createdExactTimeStr);
							
							String resourceType = jobject.getString("resourceType");
							
							Long deltaDaysInMiniSecs = 24*60*60*1000L;
							if(resourceType!=null && !resourceType.equals("Film")){
								try{
									int membershipDays = Integer.parseInt(resourceType);
									if(membershipDays>1){
										deltaDaysInMiniSecs = deltaDaysInMiniSecs * membershipDays;
									}
								}catch(Exception e){
								}
							}
							
							createdExactTime = new Date(createdExactTime.getTime()+deltaDaysInMiniSecs);
							if(createdExactTime.getTime()>new Date().getTime()){
								if(membershipExpiryDate==null){
									membershipExpiryDate = createdExactTime;
								}else{
									if(createdExactTime.getTime()>membershipExpiryDate.getTime()){
										membershipExpiryDate = createdExactTime;
									}
								}
							}
						}
						
					} catch (Exception e) {
						e.printStackTrace();
					}
					
                    break;  
                case 404:  
                    // ����ʧ��  
                    Log.e("TAG", "����ʧ��!");  
                    break;  
                }  
  
            }

			
        },String.valueOf(PropertiesUtilWithEnc.prop.getProperty("domainCall"))+"/user/json/getAccessItemsByUserResource/"+loginUserName+"/999999/", 200);  
        thread.start();
    }
	
	protected void displaySimpleAlertDialog(String msg) {
    	AlertDialog.Builder builder = new AlertDialog.Builder(this);  
        builder.setIcon(android.R.drawable.ic_dialog_info);  
        builder.setTitle("��ʾ��Ϣ");  
        builder.setMessage(msg);
        builder.setPositiveButton("ȷ��", new DialogInterface.OnClickListener() {  
            public void onClick(DialogInterface dialog, int which) {  
            	
            }  
        });  
        
        builder.show();  
	}
	
	protected void loadLockAppSettings(boolean showLockUpPopUp) {
    	String loadLockAppSettings = CacheFileUtil.readCacheFile(getCacheDir().getAbsolutePath(), "LockAppSettings.duo.dat");
    	String lockOpenedStr = CacheFileUtil.readCacheFile(getCacheDir().getAbsolutePath(), "LockAppSettings.lockOpened.duo.dat");
    	lockPasswordForThisPhone = loadLockAppSettings;
    	
    	if(lockOpenedStr==null){
    		if(lockPasswordForThisPhone!=null && lockPasswordForThisPhone.trim().length()>0){
    			lockOpened = false;
    		}else{
    			lockOpened = true;
    		}
    	}else{
	    	if(lockOpenedStr.equals("N")){
	    		lockOpened = false;
	    	}else{
	    		lockOpened = true;
	    	}
    	}
    	FlagsUtil.lockOpened = lockOpened;
    	
    	if(lockPasswordForThisPhone!=null && lockPasswordForThisPhone.trim().length()>0){
    		if(!lockOpened){
    			if(showLockUpPopUp || FlagsUtil.lockOpened != lockOpened){
    				askForSecurePasswordBeforeProcceed();
    			}
    		}
    	}
    }

	protected void lockAppIfApplicable(){
		if(lockPasswordForThisPhone!=null && lockPasswordForThisPhone.length()>0){
			CacheFileUtil.writeCacheFile(getCacheDir().getAbsolutePath(), "LockAppSettings.lockOpened.duo.dat","N");
		}
	}
	private void askForSecurePasswordBeforeProcceed() {
		final EditText editText = new EditText(this);
        editText.setHint("��������");
        
        final AlertDialog.Builder inputDialog = new AlertDialog.Builder(this);
        inputDialog.setTitle("�������APP���룺").setView(editText);
        inputDialog.setPositiveButton("ȷ��", 
            new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
            	String input = editText.getText().toString().trim();
        		if(input.equals(lockPasswordForThisPhone)){
        			lockOpened = true;
        			FlagsUtil.lockOpened = lockOpened;
        			CacheFileUtil.writeCacheFile(getCacheDir().getAbsolutePath(), "LockAppSettings.lockOpened.duo.dat","Y");
        			Toast.makeText(BaseActivity.this, "�����ɹ�", Toast.LENGTH_SHORT).show();
        			lockOpenedCallback();
        		}else{
        			lockOpened = false;
        			FlagsUtil.lockOpened = lockOpened;
        			CacheFileUtil.writeCacheFile(getCacheDir().getAbsolutePath(), "LockAppSettings.lockOpened.duo.dat","N");
        			askForSecurePasswordBeforeProcceed();
        			Toast.makeText(BaseActivity.this, "����ʧ��:�������", Toast.LENGTH_SHORT).show();
        		}
            }
        });
        inputDialog.setCancelable(false);
        inputDialog.show();
	}
	protected void modifyMyProfile() {
		if(loginUserName!=null){
			Intent intent=new Intent();
			intent.putExtra("action", "ModifyMyProfile");
			intent.setClass(this, MeLoginActivity.class);  
	        startActivity(intent);
		}else{
			Toast.makeText(BaseActivity.this, "û��½,�����޸ġ�", Toast.LENGTH_SHORT).show();
		}
	}
	 protected void recToFriendsDialog() {
		 if(loginUserEmail!=null && loginUserEmail.replace("null", "").length()>0){
			 MakeSureDialog dialog = new MakeSureDialog();
		    	dialog.setTextTitle("�Ƽ���ʾ");
		    	dialog.setContent("������ѡ��ʹ��ͼƬ��ά���Ƽ�������,Ҳ����ֱ��ʹ���������ӣ�����ѡ���ǣ�");
		  		dialog.setTextSureButton("��ά���Ƽ�");
		  		dialog.setTextCancelButton("��������");
		  		
		  		dialog.setDialogClickListener(new MakeSureDialog.onDialogClickListener() {  
		            @Override  
		            public void onSureClick() {
		            	gotoRecAppActivty();
		            }  
		            @Override  
		            public void onCancelClick() {  
		            	 String msg = "�Ƽ���:"+loginUserEmail+"��"+PropertiesUtilWithEnc.getString("shareWechatText");
		    			 shareToQQWechat(msg);
		            }  
		        });  
		     	dialog.show(getFragmentManager(),"");
			
		 }else{
			 
			 MakeSureDialog dialog = new MakeSureDialog();
		    	dialog.setTextTitle("�ҵ�����û������");
		    	dialog.setContent("����������û����д���ҵ����䡿,��ᵼ�����޷�������Ƽ�APP׬Ӷ��,����ȥ���ƣ�");
		  		dialog.setTextSureButton("ȥ��������");
		  		dialog.setTextCancelButton("�ݲ�");
		  		
		  		dialog.setDialogClickListener(new MakeSureDialog.onDialogClickListener() {  
		            @Override  
		            public void onSureClick() {
		            	modifyMyProfile();
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
		  		
			 Toast.makeText( this, "û�л�ȡ����������,�ݲ����Ƽ�,�������Ϊ�������,���Ժ����ԡ�" , Toast.LENGTH_SHORT).show();
		 }
	 }
	//��ȡ��Ļ�ķ���
	    protected File screenshotWindow() {
	     // ��ȡ��Ļ
	    	
	     View dView = getWindow().getDecorView();
	     dView.setDrawingCacheEnabled(true);
	     dView.buildDrawingCache();
	     Bitmap bmp = dView.getDrawingCache();
	     if (bmp != null)
	     {
	      try {
		       // ��ȡ����SD��·��
		       String sdCardPath = Environment.getExternalStorageDirectory().getPath()+"/DCIM/Screenshots";
		       // ͼƬ�ļ�·��
		       String imagePath = sdCardPath + File.separator + "womao_screenshot_"+System.currentTimeMillis()+"qrcode.png";
		       File file = new File(imagePath);
		       File parentFile = file.getParentFile();
		       if(!parentFile.exists()){
		    	   parentFile.mkdirs();
		       }
		       File[] fs = parentFile.listFiles(new FilenameFilter() {
					
					@Override
					public boolean accept(File dir, String filename) {
						if(filename.endsWith("qrcode.png") && filename.startsWith("womao_screenshot_")){
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
		       bmp.compress(Bitmap.CompressFormat.PNG, 100, os);
		       os.flush();
		       os.close();
		       return file;
	      } catch (Exception e) {
	    	  CrashHandler.logErrorToFile(e);
	      }
	     }
	     return null;
	    }
	    
	    
	 protected void shareImage(String imagePathF){
	    	if(imagePathF!=null){
		    	Intent intent = new Intent(Intent.ACTION_SEND); // ���������͵�����
		        File file = new File(imagePathF);
		        intent.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(file));// ���������
		        intent.setType("image/*");// �����͵���������
		        Intent chooser = Intent.createChooser(intent, "Share screen shot");
		        if(intent.resolveActivity(getPackageManager()) != null){
		         startActivity(chooser);
		        }
	    	}
	    }
	 
	protected void lockOpenedCallback(){}

	private void openWechat() {
		Intent intent = new Intent();
		ComponentName cmp = new ComponentName("com.tencent.mm","com.tencent.mm.ui.LauncherUI");
		intent.setAction(Intent.ACTION_MAIN);
		intent.addCategory(Intent.CATEGORY_LAUNCHER);
		intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		intent.setComponent(cmp);
		startActivityForResult(intent, 0);
	}

	private void openQQ() {
		Intent intent = new Intent();
		ComponentName cmp = new ComponentName("com.tencent.mobileqq","com.tencent.mobileqq.activity.SplashActivity");
		intent.setAction(Intent.ACTION_MAIN);
		intent.addCategory(Intent.CATEGORY_LAUNCHER);
		intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		intent.setComponent(cmp);
		startActivityForResult(intent, 0);
	}
	
}
