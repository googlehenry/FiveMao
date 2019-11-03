package com.ex.fivemao;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
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
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.ex.fivemao.io.PropertiesUtilWithEnc;
import com.ex.fivemao.ui.MyBoughtMembershipAdapter;
import com.example.myvideos.http.HttpsGetThread;


public class MeBoughtMembershipActivity extends BaseActivity {
	ListView meBoughtMembership;
	private MyBoughtMembershipAdapter adapter;
	public static List<Map<String,Object>> data = new ArrayList<Map<String,Object>>();
	boolean hasRecordEffective = false;
	TextView meBoughtMembershipLoadingHint;
	Button buyMemershipBtn;
	Button recToFriendBtnFromMemberBuypage;
	
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_me_boughtmemberships);
        meBoughtMembership = (ListView)findViewById(R.id.meBoughtMembership);
        meBoughtMembershipLoadingHint = (TextView)findViewById(R.id.meBoughtMembershipLoadingHint);
        buyMemershipBtn = (Button)findViewById(R.id.buyMemershipBtn);
        recToFriendBtnFromMemberBuypage = (Button)findViewById(R.id.recToFriendBtnFromMemberBuypage);
        
        
        meBoughtMembership.setOnItemClickListener(new OnItemClickListener(){

			@Override
			public void onItemClick(AdapterView<?> adapterView, View view, int arg2,
					long arg3) {
				Map<String,Object> line = data.get(arg2);
				
//				Intent intent=new Intent();
//				intent.putExtra("id", String.valueOf(line.get("id")));
//				intent.putExtra("title", String.valueOf(line.get("title")));
//				intent.putExtra("videoFileLink", String.valueOf(line.get("videoFileLink")));
//				intent.putExtra("description", String.valueOf(line.get("description")));
//				intent.putExtra("videoType", String.valueOf(line.get("videoType")));
//				intent.putExtra("coverpageImageLink", String.valueOf(line.get("coverpageImageLink")));
				
	            //setClass函数的第一个参数是一个Context对象  
	            //Context是一个类,Activity是Context类的子类,也就是说,所有的Activity对象都可以向上转型为Context对象  
	            //setClass函数的第二个参数是Class对象,在当前场景下,应该传入需要被启动的Activity的class对象  

//				intent.setClass(MeBoughtMembershipActivity.this, VideoDescActivity.class);  
	            
	            //Toast.makeText( SubActivityVideoList.this, "ok,clicked "+arg2+","+line.get("id"), Toast.LENGTH_SHORT).show();
//	            startActivity(intent);  
			}
        	
        });
        
        meBoughtMembership.setOnScrollListener(new OnScrollListener() {
            int firstVisibleItem = -1;
		
			@Override
			public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
				this.firstVisibleItem = firstVisibleItem;
		       
			}


			@Override
			public void onScrollStateChanged(AbsListView view, int scrollState) {
				if(scrollState == SCROLL_STATE_IDLE){
			        if(firstVisibleItem==0){
			        }else{
			        }
				}
			}
		});
        
        
        buyMemershipBtn.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				gotoBuyMembershipActivity();
			}
		});
        
        recToFriendBtnFromMemberBuypage.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				recToFriendsDialog();
			}
		});
        
        getMeFilmsAuth();
        
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        for(int i = 0; i < menu.size();i ++){
        	MenuItem item = menu.getItem(i);
        	if(item.getItemId()==R.id.action_buyOnedayMembership){
        		item.setVisible(true);
        		break;
        	}
        }
        return true;
    }

    int yourChoice;
    private void showSingleChoiceDialog(){
        final String[] items = { "一日会员/1.0元","一月会员/8.0元","三月会员/18.0元" };
        final int[] values = { 1,31,93 };
        yourChoice = 1;
        AlertDialog.Builder singleChoiceDialog = 
            new AlertDialog.Builder(MeBoughtMembershipActivity.this);
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
        			intent.setClass(MeBoughtMembershipActivity.this,VideoBuyMembershipActivity.class);  
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
    
    private void gotoBuyMembershipActivity() {
    	
    	AlertDialog.Builder builder = new AlertDialog.Builder(this);  
        builder.setIcon(android.R.drawable.ic_dialog_info);
        String textSure = "知道了";
        if(!hasRecordEffective){
        	builder.setTitle("好消息:购买会员后,全场免费!!!");
        }else{
        	builder.setTitle("提示:您当前已是会员,请过期后再买");
        }
        builder.setPositiveButton(textSure, new DialogInterface.OnClickListener() {  
            public void onClick(DialogInterface dialog, int which) { 
            	if(!hasRecordEffective){
            		showSingleChoiceDialog();
            	}
            	dialog.dismiss();
            }  
        });  
        builder.setCancelable(false);
        builder.show();  
        
    	
	}
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if(id==R.id.action_buyOnedayMembership){
        	gotoBuyMembershipActivity();
        }
        return super.onOptionsItemSelected(item);
    }
    
 protected void getMeFilmsAuth() {
     HttpsGetThread thread = new HttpsGetThread(new Handler() { 
         @Override  
         public void handleMessage(Message msg) {  
             super.handleMessage(msg);  
             String result = (String) msg.obj;  
             startEffectsZoom(buyMemershipBtn);
             //etShowInfo.setText(result+":"+url);
             switch (msg.what) {  
             case 200:  
					try {
						membershipExpiryDate = null;
						JSONArray jsonarr = new JSONArray(result);
						if(jsonarr.length()<=0){
						}else{
							SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:SS");
							Date now = new Date();
							for(int i = 0 ; i < jsonarr.length(); i++){
								JSONObject jobject = jsonarr.getJSONObject(i);
								Map<String,Object> rowData = new HashMap<String,Object>();
								rowData.put("idx", (i+1));
								
								rowData.put("createdExactTimeStr", jobject.getString("createdExactTimeStr"));
								Date createdExactTime = sdf.parse(String.valueOf(jobject.getString("createdExactTimeStr")));
								String resourceType = jobject.getString("resourceType");
								rowData.put("buyTitle", "一日会员");
								
								Long deltaDaysInMiniSecs = 24*60*60*1000L;
								if(resourceType!=null && !resourceType.equals("Film")){
									try{
										int membershipDays = Integer.parseInt(resourceType);
										if(membershipDays>1){
											deltaDaysInMiniSecs = deltaDaysInMiniSecs * membershipDays;
											if(membershipDays==31){
												rowData.put("buyTitle", "一月会员");
											}else if(membershipDays==93){
												rowData.put("buyTitle", "三月会员");
											}
										}
									}catch(Exception e){
									}
								}
								
								Date expiryExactTime = new Date(createdExactTime.getTime()+deltaDaysInMiniSecs);
								rowData.put("expiryExactTimeStr", sdf.format(expiryExactTime));
								
								if(expiryExactTime.getTime()>now.getTime()){
									rowData.put("status", "生效中");
									hasRecordEffective = true;
								}else{
									rowData.put("status", "已过期");
								}
								
								
								data.add(rowData);
							}
							
							Collections.reverse(data);
							for(int i = 0; i < data.size(); i++){
								data.get(i).put("idx", i+1);
							}
							
							adapter = new MyBoughtMembershipAdapter(MeBoughtMembershipActivity.this, data);
							
							meBoughtMembership.setAdapter(adapter);
						}
						
						if(!hasRecordEffective){
							Toast.makeText( MeBoughtMembershipActivity.this, "已购会员已过期,点'购买会员'菜单直接购买。" , Toast.LENGTH_LONG).show();
						}
						
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
 	public void onBackPressed() {
 		data.clear();
 		hasRecordEffective = false;
 		super.onBackPressed();
 	}
    
    
}
