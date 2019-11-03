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

import com.ex.fivemao.exception.CrashHandler;
import com.ex.fivemao.io.PropertiesUtilWithEnc;
import com.ex.fivemao.ui.MyBoughtMembershipAdapter;
import com.example.myvideos.http.HttpsGetThread;


public class MeRecDownsActivity extends BaseActivity {
	ListView meRecDowns;
	TextView meRecDownsLoadingHint;
	Button recToFriendBtn;
	Button withDrawMyServiceFees;
	
	Double myFees=0.0;
	
	private MyBoughtMembershipAdapter adapter;
	public static List<Map<String,Object>> data = new ArrayList<Map<String,Object>>();
	
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_me_recdowns);
        meRecDowns = (ListView)findViewById(R.id.meRecDowns);
        meRecDownsLoadingHint = (TextView)findViewById(R.id.meRecDownsLoadingHint);
        recToFriendBtn = (Button)findViewById(R.id.recToFriendBtn);
        withDrawMyServiceFees = (Button)findViewById(R.id.withDrawMyServiceFees);
        
        meRecDowns.setOnItemClickListener(new OnItemClickListener(){

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
				
	            //setClass�����ĵ�һ��������һ��Context����  
	            //Context��һ����,Activity��Context�������,Ҳ����˵,���е�Activity���󶼿�������ת��ΪContext����  
	            //setClass�����ĵڶ���������Class����,�ڵ�ǰ������,Ӧ�ô�����Ҫ��������Activity��class����  

//				intent.setClass(MeBoughtMembershipActivity.this, VideoDescActivity.class);  
	            
	            //Toast.makeText( SubActivityVideoList.this, "ok,clicked "+arg2+","+line.get("id"), Toast.LENGTH_SHORT).show();
//	            startActivity(intent);  
			}
        	
        });
        
        meRecDowns.setOnScrollListener(new OnScrollListener() {
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
        
        getMeFilmsAuth();
        
        withDrawMyServiceFees.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if(myFees>=10){
					contactSupport();
				}else{
					Toast.makeText( MeRecDownsActivity.this, "����ۼ�10.0Ԫ���ϲ������֣�����ϸ�Ķ������" , Toast.LENGTH_SHORT).show();
				}
			}
		});
        
        recToFriendBtn.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				recToFriendsDialog();
			}
		});
    }
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        //action_recToFriends
        for(int i = 0; i < menu.size();i ++){
        	MenuItem item = menu.getItem(i);
        	if(item.getItemId()==R.id.action_recToFriends){
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
        if(id==R.id.action_recToFriends){
        	recToFriendsDialog();
        }
        return super.onOptionsItemSelected(item);
    }
    
protected void getMeFilmsAuth() {
     HttpsGetThread thread = new HttpsGetThread(new Handler() { 
         @Override  
         public void handleMessage(Message msg) {  
             super.handleMessage(msg);  
             String result = (String) msg.obj;  
//             CrashHandler.logInfoToFile(new Exception(result));
             //etShowInfo.setText(result+":"+url);
             startEffectsZoom(recToFriendBtn);
             switch (msg.what) {  
             case 200:  
					try {
						
						JSONArray jsonarr = new JSONArray(result);
						if(jsonarr.length()<=0){
							meRecDownsLoadingHint.setVisibility(View.VISIBLE);
							meRecDownsLoadingHint.setText("��ʱû���Ƽ�Ӷ��,����Ŭ����");
							return;
						}else{
							SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:SS");
							Date now = new Date();
							for(int i = 0 ; i < jsonarr.length(); i++){
								JSONObject jobject = jsonarr.getJSONObject(i);
								Map<String,Object> rowData = new HashMap<String,Object>();
								
								String subjectNameIfAny = jobject.getString("subjectNameIfAny");
								if(subjectNameIfAny!=null && subjectNameIfAny.equals(loginUserName)){
									MeRecDownsActivity.this.setTitle("�Ƽ�Ӷ��("+loginUserEmail+")");
									continue;
								}
								
								rowData.put("subjectNameIfAny", starMyName(subjectNameIfAny));
								rowData.put("idx", (i+1));
								int tempDays = 1;
								if(jobject.getString("createdExactTimeStr")!=null &&
										!String.valueOf(jobject.getString("createdExactTimeStr")).equalsIgnoreCase("null")
										&& jobject.getString("resourceType")!=null
										&& !String.valueOf(jobject.getString("resourceType")).equalsIgnoreCase("null")){
									
									rowData.put("createdExactTimeStr", jobject.getString("createdExactTimeStr"));
									Date createdExactTime = sdf.parse(String.valueOf(jobject.getString("createdExactTimeStr")));
									String resourceType = jobject.getString("resourceType");
									rowData.put("buyTitle", "һ�ջ�Ա");
									Long deltaDaysInMiniSecs = 24*60*60*1000L;
									if(resourceType!=null && !resourceType.equals("Film")){
										try{
											int membershipDays = Integer.parseInt(resourceType);
											if(membershipDays>1){
												deltaDaysInMiniSecs = deltaDaysInMiniSecs * membershipDays;
												if(membershipDays==31){
													rowData.put("buyTitle", "һ�»�Ա");
													tempDays = 31;
												}else if(membershipDays==93){
													rowData.put("buyTitle", "���»�Ա");
													tempDays = 93;
												}
											}
										}catch(Exception e){
										}
									}
									
									Date expiryExactTime = new Date(createdExactTime.getTime()+deltaDaysInMiniSecs);
									rowData.put("expiryExactTimeStr", sdf.format(expiryExactTime));
									
									if(expiryExactTime.getTime()>now.getTime()){
										rowData.put("status", "��Ч��");
									}else{
										rowData.put("status", "�ѹ���");
									}
								}else{
									rowData.put("buyTitle", "��δ����");
									rowData.put("status", "ע���û�");
								}
								
								String membershipType = String.valueOf(rowData.get("buyTitle"));
								if(membershipType!=null){
									if(membershipType.equals("һ�ջ�Ա")){
										myFees = myFees+1.0;
									}else if(membershipType.equals("һ�»�Ա")){
										myFees = myFees+8.0;
									}else if(membershipType.equals("���»�Ա")){
										myFees = myFees+18.0;
									}
								}
								
								
								data.add(rowData);
							}
							
							myFees = myFees * 0.2;//20%Ӷ��
							myFees = ((double)((int)(myFees * 100)))/100;
							
							Collections.reverse(data);
							for(int i = 0; i < data.size(); i++){
								data.get(i).put("idx", i+1);
							}
							
							adapter = new MyBoughtMembershipAdapter(MeRecDownsActivity.this, data);
							
							meRecDowns.setAdapter(adapter);
							
						}
						
						withDrawMyServiceFees.setText("��������:"+myFees+"Ԫ");
						
						if(loginUserEmail!=null){
							if(data.size()>0){
								meRecDownsLoadingHint.setVisibility(View.GONE);
								Toast.makeText( MeRecDownsActivity.this, "��ϲ,��ϲ!�ҵ�����:"+loginUserEmail +"�����"+data.size()+"��¼��", Toast.LENGTH_SHORT).show();
							}else{
								meRecDownsLoadingHint.setVisibility(View.VISIBLE);
								meRecDownsLoadingHint.setText("û���Ƽ�Ӷ������,�����Ŭ����");
								Toast.makeText( MeRecDownsActivity.this, "���޽�����ҵ�����:"+loginUserEmail , Toast.LENGTH_SHORT).show();
							}
						}else{
							meRecDownsLoadingHint.setVisibility(View.VISIBLE);
							meRecDownsLoadingHint.setText("û���Ƽ�Ӷ������(û������,�����Ƽ�)");
							Toast.makeText( MeRecDownsActivity.this, "û������,�����Ƽ�" , Toast.LENGTH_SHORT).show();
						}
						
							//Toast.makeText( MeRecDownsActivity.this, "�ѹ���Ա�ѹ���,��'�����Ա'�˵�ֱ�ӹ���" , Toast.LENGTH_LONG).show();
					} catch (Exception e) {
						meRecDownsLoadingHint.setVisibility(View.VISIBLE);
						meRecDownsLoadingHint.setText("���ִ���,���Ժ�����");
						CrashHandler.logErrorToFile(e);
					}
					
                 break;  
             case 404:  
                 // ����ʧ��  
                 Log.e("TAG", "����ʧ��!");  
                 break;  
             }  

         }

			
     },String.valueOf(PropertiesUtilWithEnc.prop.getProperty("domainCall"))+"/user/json/getRecDowns/"+loginUserName+"/999999/", 200);
     
     meRecDownsLoadingHint.setVisibility(View.VISIBLE);
     
     thread.start();
 }
 
 	@Override
 	public void onBackPressed() {
 		data.clear();
 		super.onBackPressed();
 	}
    
    
}
