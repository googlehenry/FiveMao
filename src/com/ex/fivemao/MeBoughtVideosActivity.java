package com.ex.fivemao;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONObject;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AdapterView.OnItemClickListener;

import com.ex.fivemao.io.PropertiesUtilWithEnc;
import com.ex.fivemao.ui.MyAdapter;
import com.example.myvideos.http.HttpsGetThread;


public class MeBoughtVideosActivity extends BaseActivity {
	private Handler httpHandler; 
	ListView meBoughtVideos;
	private MyAdapter adapter;
	public static List<Map<String,Object>> data = new ArrayList<Map<String,Object>>();
	
	TextView meBoughtVideosLoadingHint;
	
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_me_boughtvideos);
        meBoughtVideos = (ListView)findViewById(R.id.meBoughtVideos);
        meBoughtVideosLoadingHint = (TextView)findViewById(R.id.meBoughtVideosLoadingHint);
        
        meBoughtVideos.setOnItemClickListener(new OnItemClickListener(){

			@Override
			public void onItemClick(AdapterView<?> adapterView, View view, int arg2,
					long arg3) {
				Map<String,Object> line = data.get(arg2);
				
				Intent intent=new Intent();
				intent.putExtra("id", String.valueOf(line.get("id")));
				intent.putExtra("title", String.valueOf(line.get("title")));
				intent.putExtra("videoFileLink", String.valueOf(line.get("videoFileLink")));
				intent.putExtra("description", String.valueOf(line.get("description")));
				intent.putExtra("videoType", String.valueOf(line.get("videoType")));
				intent.putExtra("minimumAge", String.valueOf(line.get("minimumAge")));
				intent.putExtra("coverpageImageLink", String.valueOf(line.get("coverpageImageLink")));
				
	            //setClass函数的第一个参数是一个Context对象  
	            //Context是一个类,Activity是Context类的子类,也就是说,所有的Activity对象都可以向上转型为Context对象  
	            //setClass函数的第二个参数是Class对象,在当前场景下,应该传入需要被启动的Activity的class对象  

				intent.setClass(MeBoughtVideosActivity.this, VideoDescActivity.class);  
	            
	            //Toast.makeText( SubActivityVideoList.this, "ok,clicked "+arg2+","+line.get("id"), Toast.LENGTH_SHORT).show();
	            startActivity(intent);  
			}
        	
        });
        
        meBoughtVideos.setOnScrollListener(new OnScrollListener() {
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
        
        
        initMyBoughtFilmsData();
        
        
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        for(int i = 0; i < menu.size();i ++){
        	MenuItem item = menu.getItem(i);
        	if(item.getItemId()==R.id.action_quicklinkSearchFilms){
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
        return super.onOptionsItemSelected(item);
    }
    
 private void initMyBoughtFilmsData() { 
    	if(loginUserName==null){
    		Toast.makeText(MeBoughtVideosActivity.this, "用户还未登录", Toast.LENGTH_SHORT).show();
    		return;
    	}
    	
        httpHandler = new Handler() { 
        	 
        	
            @Override  
            public void handleMessage(Message msg) {
                super.handleMessage(msg);  
                String result = (String) msg.obj;  
                
                switch (msg.what) {  
                case 200:  
                    // 请求成功  
                	
                    try {
                    	
						JSONArray jsonarr = new JSONArray(result);
						if(jsonarr.length()<=0){
							meBoughtVideosLoadingHint.setText("暂未购买电影,请稍后再试。");
							return;
						}
						
						meBoughtVideosLoadingHint.setVisibility(View.GONE);
						for(int i = 0 ; i < jsonarr.length(); i++){
							JSONObject jobject = jsonarr.getJSONObject(i);
							Map<String,Object> rowData = new HashMap<String,Object>();
							rowData.put("id", jobject.getLong("id"));
							rowData.put("title", jobject.getString("title"));
							rowData.put("createdBy", jobject.getString("createdBy"));
							rowData.put("createdTime", jobject.getString("createdTime"));
							rowData.put("coverpageImageLink", jobject.getString("coverpageImageLink"));
							rowData.put("description", jobject.getString("description"));
							rowData.put("minimumAge", jobject.getString("minimumAge"));
							rowData.put("videoType", jobject.getString("videoType"));
							String videoType =jobject.getString("videoType");
							rowData.put("itemIdx", "序号"+(i+1));
							rowData.put("desc", "已购买");
							if(VideoPlayerActivity.videosLuckyShakes.contains(String.valueOf(jobject.getLong("id")))){
								rowData.put("desc", "幸运一天");
							}
							
							if(videoType.equals("Type_VideoUrl")){
								rowData.put("videoFileLink", jobject.getString("videoFileLink"));
							}else{
								rowData.put("videoFileLink", String.valueOf(PropertiesUtilWithEnc.prop.getProperty("domainCall"))+jobject.getString("videoFileLink"));
							}
							if(videoType.equals("Type_VideoFileLink")||videoType.equals("Type_VideoUrl")||videoType.equals("Type_VideoFolderLink")){
								data.add(rowData);
							}
						}
						
						
						adapter = new MyAdapter(MeBoughtVideosActivity.this, data);
						
				        meBoughtVideos.setAdapter(adapter);
				        
				        MeBoughtVideosActivity.this.setTitle("已购电影列表:共"+data.size()+"部");
				        Toast.makeText(MeBoughtVideosActivity.this, "您已购电影"+data.size()+"部", Toast.LENGTH_SHORT).show();
				    
					} catch (Exception e) {
						meBoughtVideosLoadingHint.setText("对不起,加载已购电影失败,请稍后再试。");
					}
                    break;  
                case 404:  
                    // 请求失败
                	meBoughtVideosLoadingHint.setText("对不起,加载已购电影失败,请稍后再试。");
                    break;  
                default:
                	meBoughtVideosLoadingHint.setText("对不起,加载已购电影失败,请稍后再试。");
                }  
                meBoughtVideosLoadingHint.setText("对不起,加载已购电影失败,请稍后再试。");
            }  
        }; 
        
        meBoughtVideosLoadingHint.setVisibility(View.VISIBLE);
        meBoughtVideosLoadingHint.setText("登陆成功,加载已购电影中...");
        if(data.size()>0){
        	data.clear();
        }
        HttpsGetThread thread = new HttpsGetThread(httpHandler,  
        		String.valueOf(PropertiesUtilWithEnc.prop.getProperty("domainCall"))+"/dictionary/filmsInUserBought/"+PropertiesUtilWithEnc.getString("fixedFilmCatID")+"/?uname="+loginUserName, 200);  
        thread.start();
    }
    
 	@Override
 	public void onBackPressed() {
 		data.clear();
 		super.onBackPressed();
 	}
    
    
}
