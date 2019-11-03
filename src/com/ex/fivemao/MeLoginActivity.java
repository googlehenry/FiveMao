package com.ex.fivemao;

import java.util.Calendar;
import java.util.regex.Pattern;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.Html;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.ex.fivemao.exception.CrashHandler;
import com.ex.fivemao.io.CacheFileUtil;
import com.ex.fivemao.io.PropertiesUtilWithEnc;
import com.ex.fivemao.ui.MakeSureDialog;
import com.example.myvideos.http.HttpsGetThread;


public class MeLoginActivity extends BaseActivity {
	
	TextView loginTitle;
	EditText userId;
	EditText pass;
	EditText userEmailAct;
	EditText userProxyEmail;
	
	Button confirmBtn;
	Button button_bar;
	Button button_barMyMail;
	Button button_barRecMail;
	String action;
	LinearLayout userEmailLine;
	LinearLayout recEmailLine;
	
	int lastBirthdayYear = 0;
	int lastBirthdayMonth = 0;
	int lastBirthdayDay = 0;
	
	boolean hasUpdateItems = false;
	
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_me_login);
        
        userEmailLine = (LinearLayout) findViewById(R.id.userEmailLine);
        recEmailLine = (LinearLayout) findViewById(R.id.recEmailLine);
        
        loginTitle = (TextView) findViewById(R.id.loginTitle);
        userId = (EditText) findViewById(R.id.userId);
        userEmailAct = (EditText) findViewById(R.id.userEmailAct);
        userProxyEmail = (EditText) findViewById(R.id.userProxyEmail);
        pass = (EditText) findViewById(R.id.pass);
        confirmBtn = (Button) findViewById(R.id.confirmBtn);
        button_bar = (Button) findViewById(R.id.button_bar);
        button_barMyMail = (Button) findViewById(R.id.button_barMyMail);
        button_barRecMail = (Button) findViewById(R.id.button_barRecMail);
        
        Intent intent = getIntent();
        if(intent!=null){
        	action = intent.getStringExtra("action");
        }        
        if(action!=null){
        	button_bar.setVisibility(View.VISIBLE);
        	button_barMyMail.setVisibility(View.VISIBLE);
        	button_barRecMail.setVisibility(View.VISIBLE);
        	
        	if(action.equalsIgnoreCase("login")){
        		loginTitle.setText("五毛影视登录");
        		userEmailLine.setVisibility(View.GONE);
        		recEmailLine.setVisibility(View.GONE);
        	}else if(action.equalsIgnoreCase("signup")){
        		loginTitle.setText("五毛影视注册");
        		userEmailLine.setVisibility(View.VISIBLE);
        		recEmailLine.setVisibility(View.VISIBLE);
        	}else if(action.equalsIgnoreCase("ModifyMyProfile")){
        		loginTitle.setText("修改我的资料");
        		button_bar.setVisibility(View.GONE);
        		userId.setText(Html.fromHtml(loginUserName));
        		userId.setFocusable(false);
        		userId.setClickable(false);
        		pass.setText(loginUserBirthday);
        		pass.setFocusable(false);
        		pass.setClickable(false);
        		if(loginUserEmail==null || loginUserEmail.trim().length()==0 || !loginUserEmail.contains("@")){
        			if(loginUserEmail!=null && loginUserEmail.equals("null")){
        				loginUserEmail = "";
        			}
        			userEmailAct.setText(loginUserEmail);
        			userEmailAct.setFocusable(true);
        			userEmailAct.setClickable(true);
        			hasUpdateItems = true;
        		}else{
        			button_barMyMail.setVisibility(View.GONE);
        			userEmailAct.setText(loginUserEmail);
        			userEmailAct.setFocusable(false);
        			userEmailAct.setClickable(false);
        		}
        		
        		if(loginUserProxyEmail==null || loginUserProxyEmail.trim().length()==0 || !loginUserProxyEmail.contains("@")){
        			if(loginUserProxyEmail!=null && loginUserProxyEmail.equals("null")){
        				loginUserProxyEmail = "";
        			}
        			userProxyEmail.setText(loginUserProxyEmail);
        			userProxyEmail.setFocusable(true);
        			userProxyEmail.setClickable(true);
        			hasUpdateItems = true;
        		}else{
        			button_barRecMail.setVisibility(View.GONE);
        			userProxyEmail.setText(loginUserProxyEmail);
        			userProxyEmail.setFocusable(false);
        			userProxyEmail.setClickable(false);
        		}
        		
        		userEmailLine.setVisibility(View.VISIBLE);
        		recEmailLine.setVisibility(View.VISIBLE);
        	}
        }
        
        if(!hasUpdateItems && action!=null && action.equalsIgnoreCase("ModifyMyProfile")){
        	loginTitle.setText("我的资料(不能修改)");
        	confirmBtn.setText("复制邮箱地址");
        }else{
        	confirmBtn.setText("确 定");
        }
        
        button_bar.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				userId.requestFocus();
				userId.setText("");
			}
        	
        });
        button_barMyMail.setOnClickListener(new OnClickListener() {
        	@Override
        	public void onClick(View v) {
        		userEmailAct.requestFocus();
        		userEmailAct.setText("");
        	}
        	
        });
        
        button_barRecMail.setOnClickListener(new OnClickListener() {
        	@Override
        	public void onClick(View v) {
        		userProxyEmail.requestFocus();
        		userProxyEmail.setText("");
        	}
        	
        });
        confirmBtn.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if(action!=null){
					Animation shake = AnimationUtils.loadAnimation(MeLoginActivity.this, R.anim.shake);
					confirmBtn.startAnimation(shake);
		        	if(action.equalsIgnoreCase("login")){
		        		if(userId.getText()!=null && userId.getText().toString().length()>=6 
		        				&& pass.getText()!=null && pass.getText().length()>=8){
		        			
		        			checkUserName(new LoginHandler() {
								@Override
								public void callback(String message) {
									if(message!=null && message.equals("已存在")){
										loginCheckPassword();
									}else if (message!=null && message.equals("不存在")){
										
										MakeSureDialog dialog = new MakeSureDialog(); 
										dialog.setTextTitle("提示信息");
										dialog.setContent("用户名不存在,请先注册。");
										dialog.setDialogClickListener(new MakeSureDialog.onDialogClickListener() {  
										    @Override  
										    public void onSureClick() { 
										    	onBackPressed();
										    }  
								  
										    @Override  
										    public void onCancelClick() {  
										    }  
										});  
										dialog.show(getFragmentManager(),"");
										
									}
								}
							});
		        			
		        		}else{
		        			Toast.makeText( MeLoginActivity.this, "用户名必须至少6位(字母/数字)，生日必须选择。" , Toast.LENGTH_LONG).show();
		        		}
		        	}else if(action.equalsIgnoreCase("signup")){
		        		if(userId.getText()!=null && userId.getText().toString().length()>=6 
		        				&& pass.getText()!=null && pass.getText().length()>=8 
		        				&& validEmail(userEmailAct.getText().toString().trim())
		        				){
		        			boolean userProxyEmailOk = true;
		        			if(userProxyEmail.getText().length()>0 && !validEmail(userProxyEmail.getText().toString().trim())){
		        				userProxyEmailOk = false;
		        			}
		        			if(userProxyEmailOk){
		        				if(userEmailAct.getText().toString().trim().equalsIgnoreCase(userProxyEmail.getText().toString().trim())){
		        					Toast.makeText( MeLoginActivity.this, "我的邮箱和推荐人邮箱不能相同,请联系推荐人/客服获取推荐人邮箱。" , Toast.LENGTH_LONG).show();
		        				}else{
		        					showSignUpConfirmBox(true);
		        				}
		        			}else{
		        				Toast.makeText( MeLoginActivity.this, "推荐人邮箱格式错误:可为空/正确的邮件地址" , Toast.LENGTH_LONG).show();
		        			}
		        		}else{
		        			Toast.makeText( MeLoginActivity.this, "用户名必须至少6位(字母/数字)，生日必须选择，邮箱必须有效。" , Toast.LENGTH_LONG).show();
		        		}
		        	}else if(action.equalsIgnoreCase("ModifyMyProfile")){
		        		if(hasUpdateItems){
			        		if(userId.getText()!=null && userId.getText().toString().length()>=6 
			        				&& pass.getText()!=null && pass.getText().length()>=8 
			        				&& validEmail(userEmailAct.getText().toString().trim())
			        				){
			        			boolean userProxyEmailOk = true;
			        			if(userProxyEmail.getText().length()>0 && !validEmail(userProxyEmail.getText().toString().trim())){
			        				userProxyEmailOk = false;
			        			}
			        			if(userProxyEmailOk){
			        				if(userEmailAct.getText().toString().trim().equalsIgnoreCase(userProxyEmail.getText().toString().trim())){
			        					Toast.makeText( MeLoginActivity.this, "我的邮箱和推荐人邮箱不能相同,请联系推荐人/客服获取推荐人邮箱。" , Toast.LENGTH_LONG).show();
			        				}else{
			        					showSignUpConfirmBox(false);
			        				}
			        			}else{
			        				Toast.makeText( MeLoginActivity.this, "推荐人邮箱格式错误:可为空/正确的邮件地址" , Toast.LENGTH_LONG).show();
			        			}
			        		}else{
			        			Toast.makeText( MeLoginActivity.this, "用户名必须至少6位(字母/数字)，生日必须选择，邮箱必须有效。" , Toast.LENGTH_LONG).show();
			        		}
		        		}else{
		        			showEmailChoicePopup();
		        		}
		        	}
		        }
			}

			
		});
        //\w[-\w.+]*@([A-Za-z0-9][-A-Za-z0-9]+\.)+[A-Za-z]{2,14}
        pass.setFocusable(false);
        pass.setClickable(true);
        pass.setOnClickListener(new OnClickListener() {
            
            @Override
            public void onClick(View arg0) {
            	if(loginUserName!=null){
            		return;
            	}
                Calendar c=Calendar.getInstance();
                int y = c.get(Calendar.YEAR)-18;
                int m = c.get(Calendar.MONTH);
                int d = c.get(Calendar.DAY_OF_MONTH);
                
                if(lastBirthdayYear>0){
                	y = lastBirthdayYear;
                }
                if(lastBirthdayMonth>0){
                	m = lastBirthdayMonth;
                }
                if(lastBirthdayDay>0){
                	d = lastBirthdayDay;
                }
                
                Dialog dateDialog=new DatePickerDialog(MeLoginActivity.this, new DatePickerDialog.OnDateSetListener() {
                    
                    @Override
                    public void onDateSet(DatePicker arg0, int arg1, int arg2, int arg3) {
                    	StringBuffer stringBuilder=new StringBuffer("");
                        stringBuilder.append(arg1+"-"+(arg2+1)+"-"+arg3);
                        lastBirthdayYear = arg1;
                        lastBirthdayMonth = arg2;
                        lastBirthdayDay = arg3;
                        pass.setText(stringBuilder);
                    }
                    
                }, y, m, d);
                dateDialog.setTitle("请选择出生日期作为密码");
                dateDialog.setCancelable(false);
                dateDialog.show();
            }
        });

    }
    
    private boolean validEmail(String mailInput) {
		Pattern p = Pattern.compile("\\w[-\\w.+]*@([A-Za-z0-9][-A-Za-z0-9]+\\.)+[A-Za-z]{2,14}");
		return p.matcher(mailInput).matches();
	}
    
    int yourChoice = 0;
    protected void showEmailChoicePopup() {
        yourChoice = 0;
        String[] items = new String[]{"我的邮箱+推荐人邮箱","我的邮箱","我的推荐人邮箱"};
        AlertDialog.Builder singleChoiceDialog = 
            new AlertDialog.Builder(MeLoginActivity.this);
        singleChoiceDialog.setTitle("复制内容选择");

        int defaultSelection = 0;
        
        // 第二个参数是默认选项，此处设置为0
        singleChoiceDialog.setSingleChoiceItems(items, defaultSelection, 
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
                	if(yourChoice==0){
                		shareToQQWechat("我的邮箱:"+loginUserEmail+";\n我的推荐人邮箱:"+loginUserProxyEmail);
                	}else if(yourChoice==1){
                		shareToQQWechat("我的邮箱:"+loginUserEmail);
                	}else if(yourChoice==2){
                		shareToQQWechat("我的推荐人邮箱:"+loginUserProxyEmail);
                	}
                }
            }
        });
        
        singleChoiceDialog.show();
        
    
    
	}
private void signonNewUser(final int actionCode) {
	//actioncode:0,new user; 1,mofify user emails
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
    						String message = json.getString("message");
    						//Toast.makeText( MeActivity.this, status+":"+message, Toast.LENGTH_LONG).show();
    						if(status!=null && status.contains("Fail")){
    							Toast.makeText( MeLoginActivity.this, result+",请稍后再试。", Toast.LENGTH_LONG).show();
    						}else if(status!=null && status.contains("Success")){
    							loginUserName = userId.getText().toString().trim();
    							loginUserBirthday = pass.getText().toString().trim();
    							if(message!=null && message.length()>0){
	    							String[] emails = message.split(";");
	    							if(emails.length>0){
	    								loginUserEmail = emails[0];
	    							}
	    							if(emails.length>1){
	    								loginUserProxyEmail = emails[1];
	    							}
    							}
    							StringBuilder sb = new StringBuilder();
    							for(int i = 0; i < SearchActivity.searchHistory.size(); i++){
    								sb.append(","+SearchActivity.searchHistory.get(i));
    							}
    							String sh = sb.toString();
    							if(sb.length()>1){
    								sh = sb.substring(1);
    							}
    							
    							loginUserEmail = loginUserEmail==null?"":loginUserEmail;
    							loginUserProxyEmail = loginUserProxyEmail==null?"":loginUserProxyEmail;
    							
    							CacheFileUtil.writeCacheFile(getCacheDir().getAbsolutePath(), PropertiesUtilWithEnc.getString("cacheFileName"), loginUserName+"notakid"+loginUserBirthday+"notakid"+loginUserEmail+";"+loginUserProxyEmail+"notakid"+sh);
    							if(actionCode==0){
    								Toast.makeText( MeLoginActivity.this, "注册成功"+loginUserEmail, Toast.LENGTH_SHORT).show();
    							}else if(actionCode==1){
    								Toast.makeText( MeLoginActivity.this, "修改成功"+loginUserEmail, Toast.LENGTH_SHORT).show();
    							}
    							onBackPressed();
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
        },String.valueOf(PropertiesUtilWithEnc.prop.getProperty("domainCall"))+"/user/json/addNewUser?uname="+userId.getText().toString().trim()+"&upass="+pass.getText().toString().trim()+"&securityQquestion="+userEmailAct.getText().toString().trim()+"&securityQanswer="+userProxyEmail.getText().toString().trim()+"&actionCode="+actionCode, 200);  
        thread.start();
    }

interface LoginHandler{
	public void callback(String message);
}
private void checkUserName(final LoginHandler loginHandler) {  
    
    HttpsGetThread thread = new HttpsGetThread(new Handler() { 
        @Override  
        public void handleMessage(Message msg) {  
            super.handleMessage(msg);  
            String result = (String) msg.obj;  
            switch (msg.what) {  
            case 200:  
            	try {
            		if(result==null || result.length()==0){
            			
            		}else{
            			JSONObject json = new JSONObject(result);
						String message = json.getString("message");
						if(message!=null && message.contains("已存在")){
							loginHandler.callback("已存在");
						}else if(message!=null && message.contains("不存在")){
							loginHandler.callback("不存在");
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
    },String.valueOf(PropertiesUtilWithEnc.prop.getProperty("domainCall"))+"/user/json/validateName/"+userId.getText().toString().trim(), 200);  
    thread.start();
}

private void loginCheckPassword() {  
        
        HttpsGetThread thread = new HttpsGetThread(new Handler() { 
        	
            @Override  
            public void handleMessage(Message msg) {
                super.handleMessage(msg);  
                String result = (String) msg.obj;  
                switch (msg.what) {  
                case 200:  
                	try {
                		if(result==null || result.length()==0){
                		}else{
                			JSONObject json = new JSONObject(result);
    						String status = json.getString("status");
    						String message = json.getString("message");
    						//Toast.makeText( MeActivity.this, status+":"+message, Toast.LENGTH_LONG).show();
    						if(status!=null && status.contains("Fail")){
    			                Toast.makeText( MeLoginActivity.this, result+",请稍后再试。", Toast.LENGTH_LONG).show();
    						}else if(status!=null && status.contains("Success")){
    							loginUserName = userId.getText().toString().trim();
    							loginUserBirthday = pass.getText().toString().trim();
    							if(message!=null && message.length()>0){
	    							String[] emails = message.split(";");
	    							if(emails.length>0){
	    								loginUserEmail = emails[0];
	    							}
	    							if(emails.length>1){
	    								loginUserProxyEmail = emails[1];
	    							}
    							}
    							StringBuilder sb = new StringBuilder();
    							for(int i = 0; i < SearchActivity.searchHistory.size(); i++){
    								sb.append(","+SearchActivity.searchHistory.get(i));
    							}
    							String sh = sb.toString();
    							if(sb.length()>1){
    								sh = sb.substring(1);
    							}
    							
    							loginUserEmail = loginUserEmail==null?"":loginUserEmail;
    							loginUserProxyEmail = loginUserProxyEmail==null?"":loginUserProxyEmail;
    							
    							CacheFileUtil.writeCacheFile(getCacheDir().getAbsolutePath(), PropertiesUtilWithEnc.getString("cacheFileName"), loginUserName+"notakid"+loginUserBirthday+"notakid"+loginUserEmail+";"+loginUserProxyEmail+"notakid"+sh);
    							Toast.makeText( MeLoginActivity.this, "登陆成功"+loginUserEmail, Toast.LENGTH_SHORT).show();
    							
    							onBackPressed();
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
        },String.valueOf(PropertiesUtilWithEnc.prop.getProperty("domainCall"))+"/user/json/checkUsernamePassword?uname="+userId.getText().toString().trim()+"&upass="+pass.getText().toString().trim(), 200);  
        thread.start();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        
        for(int i = 0; i < menu.size();i ++){
        	MenuItem item = menu.getItem(i);
        	if(item.getItemId()==R.id.action_findMyPassword){
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
        if(id==R.id.action_findMyPassword){
        	findMyPasswordGuide();
        }
        return super.onOptionsItemSelected(item);
    }
	private void findMyPasswordGuide() {
		showUsernameInputDialog();
	}
	
	private void showUsernameInputDialog() {
		final EditText editTextUsername = new EditText(this);
        editTextUsername.setHint("输入我的用户名");
        
        final AlertDialog.Builder inputDialog = new AlertDialog.Builder(this);
        inputDialog.setTitle("第一步:请输入用户名").setView(editTextUsername);
        inputDialog.setPositiveButton("确定", 
            new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
            	String input = editTextUsername.getText().toString().trim().trim();
        		if(input.length()<6){
        			showUsernameInputDialog();
        			Toast.makeText( MeLoginActivity.this, "用户名必须至少6位(字母/数字)。", Toast.LENGTH_SHORT).show();
        		}else{
        			loginUserName = input;
        			showMyEmailInputDialog();
        		}
            }
        });
        inputDialog.show();
	}
	
	private void showMyEmailInputDialog() {
		final EditText editTextUsername = new EditText(this);
        editTextUsername.setHint("输入我的邮箱");
        
        final AlertDialog.Builder inputDialog = new AlertDialog.Builder(this);
        inputDialog.setTitle("第二步:请输入绑定的邮箱").setView(editTextUsername);
        inputDialog.setPositiveButton("确定", 
            new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
            	String input = editTextUsername.getText().toString().trim().trim();
        		if(input.length()==0){
        			showMyEmailInputDialog();
        			Toast.makeText( MeLoginActivity.this, "邮件地址不能空。", Toast.LENGTH_SHORT).show();
        		}else{
        			loginUserEmail = input;
        			if(validEmail(loginUserEmail)){
        				findMyPassword(loginUserName,loginUserEmail);
        			}else{
        				showMyEmailInputDialog();
        				Toast.makeText( MeLoginActivity.this, "邮件地址格式不正确。", Toast.LENGTH_SHORT).show();
        			}
        		}
            }
        });
        inputDialog.show();
	}
	
	protected void findMyPassword(String userName, String userEmail) {
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
    						String message = json.getString("message");
    						//Toast.makeText( MeActivity.this, status+":"+message, Toast.LENGTH_LONG).show();
    						if(status!=null && status.contains("Fail")){
    							Toast.makeText( MeLoginActivity.this, result+",请稍后再试。", Toast.LENGTH_LONG).show();
    						}else if(status!=null && status.contains("Success")){
    							AlertDialog.Builder builder = new AlertDialog.Builder(MeLoginActivity.this);  
    					        builder.setIcon(android.R.drawable.ic_dialog_info);  
    					        builder.setTitle("密码找回结果");
    					        builder.setMessage("我的生日密码为:【"+message+"】,请牢记,去重新登录？");
    					        builder.setPositiveButton("知道了", new DialogInterface.OnClickListener() {  
    					            public void onClick(DialogInterface dialog, int which) {  
    					            	onBackPressed();
    					            }
    					        });  
    					        builder.setCancelable(false);
    					        builder.show();  
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
        },String.valueOf(PropertiesUtilWithEnc.prop.getProperty("domainCall"))+"/user/json/findMyPasswordByMailAccount?uname="+userName+"&upass="+"&securityQquestion="+userEmail+"&securityQanswer=", 200);  
        thread.start();
	}

	private void showSignUpConfirmBox(final boolean isNewUser) {
		MakeSureDialog dialog = new MakeSureDialog();  
		if(isNewUser){
			dialog.setTextTitle("提示:注册信息不能修改!!");
			dialog.setContent(
					   "用  户  名:"+userId.getText().toString()
					+"\n生日密码:"+pass.getText().toString()
					+"\n我的邮箱:"+userEmailAct.getText().toString()
					+"\n推荐人:"+userProxyEmail.getText().toString().trim()
					+"\n使用以上信息注册吗？");
		}else{
			dialog.setTextTitle("信息提交后不能再次修改!!");
			dialog.setContent(
					   "用  户  名:"+userId.getText().toString()
					+"\n生日密码:******"
					+"\n我的邮箱:"+userEmailAct.getText().toString()
					+"\n推荐人:"+userProxyEmail.getText().toString().trim()
					+"\n使用以上信息更新吗？");
		}
		dialog.setDialogClickListener(new MakeSureDialog.onDialogClickListener() {  
		    @Override  
		    public void onSureClick() {
		    	if(isNewUser){
		    		checkUserName(new LoginHandler() {
		    			@Override
		    			public void callback(String message) {
		    				if(message!=null && message.equals("已存在")){
		    					
		    					MakeSureDialog dialog = new MakeSureDialog(); 
		    					dialog.setTextTitle("提示信息");
		    					dialog.setContent("用户名已存在,直接登录?");
		    					dialog.setDialogClickListener(new MakeSureDialog.onDialogClickListener() {  
		    						@Override  
		    						public void onSureClick() { 
		    							loginCheckPassword();
		    						}  
		    						
		    						@Override  
		    						public void onCancelClick() {  
		    						}  
		    					});  
		    					dialog.show(getFragmentManager(),"");
		    				}else if (message!=null && message.equals("不存在")){
		    					if(isNewUser){
		    						signonNewUser(0);
		    					}else{
		    						signonNewUser(1);
		    					}
		    				}
		    			}
		    		});
		    	}else{
		    		signonNewUser(1);
		    	}
		    }  
  
		    @Override  
		    public void onCancelClick() {  
		    }  
		});  
		dialog.show(getFragmentManager(),"");
	}
    
    
    
    
}
