package com.ex.fivemao;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONObject;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.ex.fivemao.exception.CrashHandler;
import com.ex.fivemao.io.CacheFileUtil;
import com.ex.fivemao.io.PropertiesUtilWithEnc;
import com.ex.fivemao.ui.MakeSureDialog;
import com.example.myvideos.http.HttpsGetThread;


public class MeActivity extends BaseActivity {
	LinearLayout meAcLoginSignOnSection;
	LinearLayout meAcLoinedSection;
	Button loginToWumao;
	Button signupToWumao;
	Button logoffFromWumao;
	Button checkMyRecDowns;
	Button checkBoughtMemberships;
	Button checkBoughtVideos;
	Button checkVisitedVideos;
	Button buyOneDayMembership;
	Button buy1MonthMembership;
	Button buy3MonthsMembership;
	TextView recToFriendNotice;
	
	private List<Map<String,Object>> membershipData = new ArrayList<Map<String,Object>>();
	
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_me);
        meAcLoginSignOnSection = (LinearLayout)findViewById(R.id.meAcLoginSignOnSection);
        meAcLoinedSection = (LinearLayout)findViewById(R.id.meAcLoinedSection);
        loginToWumao = (Button)findViewById(R.id.loginToWumao);
        signupToWumao = (Button)findViewById(R.id.signupToWumao);
        logoffFromWumao = (Button)findViewById(R.id.logoffFromWumao);
        checkMyRecDowns = (Button)findViewById(R.id.checkMyRecDowns);
        checkBoughtMemberships = (Button)findViewById(R.id.checkBoughtMemberships);
        checkBoughtVideos = (Button)findViewById(R.id.checkBoughtVideos);
        checkVisitedVideos = (Button)findViewById(R.id.checkVisitedVideos);
        buyOneDayMembership = (Button)findViewById(R.id.buyOneDayMembership);
        buy1MonthMembership = (Button)findViewById(R.id.buy1MonthMembership);
        buy3MonthsMembership = (Button)findViewById(R.id.buy3MonthsMembership);
        recToFriendNotice = (TextView)findViewById(R.id.recToFriendNotice);
        
        
        adjustUI();
        
        if(loginUserName==null){
        	displaySimpleAlertDialog("1.推荐APP给好友可获得20%佣金\n2.注册时填写推荐人邮箱后可看限制级V+电影");
        }
        
        loginToWumao.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				gotoLoginPage();
			}
		});
        
        signupToWumao.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent intent=new Intent();
				intent.putExtra("action", "signup");
				intent.setClass(MeActivity.this, MeLoginActivity.class);  
	            startActivity(intent); 
			}
		});
        
        checkMyRecDowns.setOnClickListener(new OnClickListener() {
        	@Override
        	public void onClick(View v) {
        		gotoRecDownsActivty();
        	}
        });
        
        logoffFromWumao.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				MakeSureDialog dialog = new MakeSureDialog();  
	          	 
	          	 dialog.setTextTitle("退出登录提示");
	          	 dialog.setTextSureButton("退出登录");
	          	 dialog.setTextCancelButton("不退出了");
	          	 dialog.setContent("退出登录会清除缓存的用户账号,搜索历史数据,您确定吗?");
	          	dialog.setDialogClickListener(new MakeSureDialog.onDialogClickListener() {  
	          	   @Override
	          	   public void onSureClick() {
		          		loginUserName = null;
		 				loginUserBirthday = null;
		 				loginUserEmail = null;
		 				loginUserProxyEmail = null;
		 				VideoPlayerActivity.videosBought.clear();
		 				VideoPlayerActivity.videosLuckyShakes.clear();
		 				
		 				MeActivity.this.setTitle("我的资料");
		 				
		 				CacheFileUtil.writeCacheFile(getCacheDir().getAbsolutePath(), PropertiesUtilWithEnc.getString("cacheFileName"), "");
		 				Toast.makeText( MeActivity.this, "退出登录,清除用户信息" , Toast.LENGTH_SHORT).show();
		 				
		 				adjustUI();
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
        
        checkBoughtVideos.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent intent=new Intent();
				intent.setClass(MeActivity.this, MeBoughtVideosActivity.class);  
	            startActivity(intent); 
			}
		});
        
        checkVisitedVideos.setOnClickListener(new OnClickListener() {
        	@Override
        	public void onClick(View v) {
        		Intent intent=new Intent();
        		intent.setClass(MeActivity.this, MeVisitedVideosActivity.class);  
        		startActivity(intent); 
        	}
        });
        checkBoughtMemberships.setOnClickListener(new OnClickListener() {
        	@Override
        	public void onClick(View v) {
        		gotoBoughtMembershipListPage(); 
        	}
        });
        
        buyOneDayMembership.setOnClickListener(new OnClickListener() {
        	@Override
        	public void onClick(View v) {
        		AlertDialog.Builder builder = new AlertDialog.Builder(MeActivity.this);  
                builder.setIcon(android.R.drawable.ic_dialog_info);  
                if(membershipExpiryDate!=null){
                	builder.setTitle("您已经是会员,过期才能再次购买！！！");
	                builder.setPositiveButton("确 定", new DialogInterface.OnClickListener() {  
	                    public void onClick(DialogInterface dialog, int which) { 
	                    }  
	                });  
                }else{
	                builder.setTitle("好消息:购买一日会员,24小时内全场免费");
	                builder.setPositiveButton("知道了", new DialogInterface.OnClickListener() {  
	                    public void onClick(DialogInterface dialog, int which) { 
	            			Intent intent=new Intent();
	            			intent.putExtra("membershipdays", 1);
	            			intent.setClass(MeActivity.this,VideoBuyMembershipActivity.class);  
	            			startActivity(intent);
	                    }  
	                });  
                }
                builder.setCancelable(false);
                builder.show(); 
        	}
        });
        
        buy1MonthMembership.setOnClickListener(new OnClickListener() {
        	@Override
        	public void onClick(View v) {
        		AlertDialog.Builder builder = new AlertDialog.Builder(MeActivity.this);  
                builder.setIcon(android.R.drawable.ic_dialog_info);  
                if(membershipExpiryDate!=null){
                	builder.setTitle("您已经是会员,过期才能再次购买！！！");
	                builder.setPositiveButton("确 定", new DialogInterface.OnClickListener() {  
	                    public void onClick(DialogInterface dialog, int which) { 
	                    }  
	                });  
                }else{
	                builder.setTitle("好消息:限时优惠价,购买一月(31天)会员,全场免费看。");
	                builder.setPositiveButton("知道了", new DialogInterface.OnClickListener() {  
	                    public void onClick(DialogInterface dialog, int which) { 
	            			Intent intent=new Intent();
	            			intent.putExtra("membershipdays", 31);
	            			intent.setClass(MeActivity.this,VideoBuyMembershipActivity.class);  
	            			startActivity(intent);
	                    }  
	                }); 
                }
                builder.setCancelable(false);
                builder.show(); 
        	}
        });
        
        buy3MonthsMembership.setOnClickListener(new OnClickListener() {
        	@Override
        	public void onClick(View v) {
        		AlertDialog.Builder builder = new AlertDialog.Builder(MeActivity.this);  
                builder.setIcon(android.R.drawable.ic_dialog_info);  
                if(membershipExpiryDate!=null){
                	builder.setTitle("您已经是会员,过期才能再次购买！！！");
	                builder.setPositiveButton("确 定", new DialogInterface.OnClickListener() {  
	                    public void onClick(DialogInterface dialog, int which) { 
	                    }  
	                });  
                }else{
	                builder.setTitle("好消息:限时优惠价,购买三月(93天)会员,全场免费看。");
	                builder.setPositiveButton("知道了", new DialogInterface.OnClickListener() {  
	                    public void onClick(DialogInterface dialog, int which) { 
	            			Intent intent=new Intent();
	            			intent.putExtra("membershipdays", 93);
	            			intent.setClass(MeActivity.this,VideoBuyMembershipActivity.class);  
	            			startActivity(intent);
	                    }  
	                });  
                }
                builder.setCancelable(false);
                builder.show(); 
        	}
        });
        
        
       getMeFilmsAuth();
       
       shakeOneIteration();
    }
    
    protected void gotoRecDownsActivty() {
    	Intent intent=new Intent();
		intent.setClass(this, MeRecDownsActivity.class);  
        startActivity(intent); 
	}

	private void gotoLoginPage(){
		Intent intent=new Intent();
		intent.putExtra("action", "login");
		intent.setClass(this, MeLoginActivity.class);  
        startActivity(intent); 
	}
    private void shakeOneIteration() {
    	
    	Animation shake2 = AnimationUtils.loadAnimation(MeActivity.this, R.anim.shake2);
    	shake2.setAnimationListener(new AnimationListener() {
			
			@Override
			public void onAnimationStart(Animation animation) {
				
			}
			
			@Override
			public void onAnimationRepeat(Animation animation) {
				
			}
			
			@Override
			public void onAnimationEnd(Animation animation) {
				shakeOthers();
			}
		});
    	
    	buyOneDayMembership.startAnimation(shake2);
		buy1MonthMembership.startAnimation(shake2);
		buy3MonthsMembership.startAnimation(shake2);
	}
    private void shakeOthers(){
    	Animation shake = AnimationUtils.loadAnimation(MeActivity.this, R.anim.shake);
    	Animation zoom = AnimationUtils.loadAnimation(MeActivity.this, R.anim.zoom2);
		buyOneDayMembership.startAnimation(zoom);
		buy1MonthMembership.startAnimation(shake);
		buy3MonthsMembership.startAnimation(shake);
    }
	protected void getMeFilmsAuth() {
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
						}else{
							SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:SS");
							
							for(int i = 0 ; i < jsonarr.length(); i++){
								JSONObject jobject = jsonarr.getJSONObject(i);
								String createdExactTimeStr = jobject.getString("createdExactTimeStr");
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
								Date createdExactTime = sdf.parse(createdExactTimeStr);
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
						}
						processMembershipData();
					} catch (Exception e) {
						e.printStackTrace();
					}
					
                    break;  
                case 404:  
                    // 请求失败  
                    Log.e("TAG", "请求失败!");  
                    break;  
                }  
  
            }

			
        },String.valueOf(PropertiesUtilWithEnc.prop.getProperty("domainCall"))+"/user/json/getAccessItemsByUserResource/"+loginUserName+"/999999/", 200);  
        thread.start();
    }

    @Override
	protected void onResume() {
		super.onResume();
		adjustUI();
		getMeFilmsAuth();
		shakeOneIteration();
	};

	public static String getDistanceTime(Date one, Date two) {  
        DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");  
        long day = 0;  
        long hour = 0;  
        long min = 0;  
        long sec = 0;  
        
        long time1 = one.getTime();  
        long time2 = two.getTime();  
        long diff ;  
        if(time1<time2) {  
            diff = time2 - time1;  
        } else {  
            diff = time1 - time2;  
        }  
        day = diff / (24 * 60 * 60 * 1000);  
        hour = (diff / (60 * 60 * 1000) - day * 24);  
        min = ((diff / (60 * 1000)) - day * 24 * 60 - hour * 60);  
        sec = (diff/1000-day*24*60*60-hour*60*60-min*60);  
        return day + "天" + hour + "小时" + min + "分" + sec + "秒";  
    }  
	
	private void processMembershipData() {
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:SS");
		
		if(membershipExpiryDate!=null){
			
			String timeStr = getDistanceTime(membershipExpiryDate,new Date());
			
			buyOneDayMembership.setText("*已是会员[还剩"+timeStr+"]");
			Animation rotate = AnimationUtils.loadAnimation(MeActivity.this, R.anim.rotate);
			buyOneDayMembership.startAnimation(rotate);
			
		}else{
			buyOneDayMembership.setText("一元购买一日会员");
		}
		
	}
	
	
	private void adjustUI() {
		
		if(loginUserName==null){
        	loginToWumao.setVisibility(View.VISIBLE);
        	signupToWumao.setVisibility(View.VISIBLE);
        	logoffFromWumao.setVisibility(View.GONE);
        	meAcLoinedSection.setVisibility(View.GONE);
        	checkMyRecDowns.setVisibility(View.GONE);
        	recToFriendNotice.setVisibility(View.VISIBLE);
        	this.setTitle("个人信息");
        }else{
        	loginToWumao.setVisibility(View.GONE);
        	signupToWumao.setVisibility(View.GONE);
        	logoffFromWumao.setVisibility(View.VISIBLE);
        	meAcLoinedSection.setVisibility(View.VISIBLE);
        	checkMyRecDowns.setVisibility(View.VISIBLE);
        	recToFriendNotice.setVisibility(View.GONE);
        	this.setTitle(loginUserName+"的个人信息");
        }
	}
	
	@Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
      //action_recToFriends
        for(int i = 0; i < menu.size();i ++){
        	MenuItem item = menu.getItem(i);
        	if(item.getItemId()==R.id.action_modifyMyProfile){
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
        if(id==R.id.action_modifyMyProfile){
        	modifyMyProfile();
        }
        return super.onOptionsItemSelected(item);
    }

	private void gotoBoughtMembershipListPage() {
		Intent intent=new Intent();
		intent.setClass(MeActivity.this, MeBoughtMembershipActivity.class);  
		startActivity(intent);
	}
    
   @Override
	public void onBackPressed() {
		super.onBackPressed();
	}
   
}
