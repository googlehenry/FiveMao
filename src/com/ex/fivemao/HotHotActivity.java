package com.ex.fivemao;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.json.JSONArray;
import org.json.JSONObject;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.ex.fivemao.io.PropertiesUtilWithEnc;
import com.ex.fivemao.ui.MyAdapter;
import com.example.myvideos.http.HttpsGetThread;


public class HotHotActivity extends BaseActivity {
	public static Integer currentPageNo = 1;
	public static Integer currentPageSize = 10;
	private static Integer visibleItemCountLastTime = 0;
	
	private Handler httpHandler;  
	private ListView tv_serverVideoCatList;
	TextView videosLoadingHint;
	
	private View mFooterParentXml; 

	private MyAdapter adapter;
	public static List<Map<String,Object>> data = new ArrayList<Map<String,Object>>();
	private static Set<Long> videoIDs = new HashSet<Long>();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video);
        videosLoadingHint = (TextView)findViewById(R.id.videosLoadingHint);
        tv_serverVideoCatList = (ListView)findViewById(R.id.videos);
        
        mFooterParentXml = LayoutInflater.from(this).inflate(R.layout.layout_footer_listview, null);//加载footerParent布局
        View parentFooter = mFooterParentXml.findViewById(R.id.mFooterparent);

//        parentFooter.setFocusable(false);
//        parentFooter.setClickable(false);
//        parentFooter.setOnClickListener(new OnClickListener() {
//			@Override
//			public void onClick(View v) {
//				initData(true);
//			}
//		});
//        tv_serverVideoCatList.addFooterView(parentFooter);
        
        initData(false);
        
        tv_serverVideoCatList.setOnScrollListener(new OnScrollListener() {
        	private int totalItem;  
            private int lastItem;  
              
            
			@Override
			public void onScrollStateChanged(AbsListView view, int scrollState) {
				if(this.totalItem == lastItem&&scrollState == SCROLL_STATE_IDLE){  
		            if(!videosLoadingHint.isShown()){  
		            	HotHotActivity.currentPageNo = HotHotActivity.currentPageNo + 1;  
		                initData(true);
		            }
		            
		        }  
			}
			
			@Override
			public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
				
				this.lastItem = firstVisibleItem+visibleItemCount;  
		        this.totalItem = totalItemCount;  
		        visibleItemCountLastTime = visibleItemCount;
			}
		});
        
        tv_serverVideoCatList.setOnItemClickListener(new OnItemClickListener(){

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
		            
					intent.setClass(HotHotActivity.this, VideoDescActivity.class);  
		            
		            //Toast.makeText( SubActivityVideoList.this, "ok,clicked "+arg2+","+line.get("id"), Toast.LENGTH_SHORT).show();
		            startActivity(intent);  
			}
        	
        });
        
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
    
    
    private void initData(final boolean append) {  
        httpHandler = new Handler() { 
        	
            @Override  
            public void handleMessage(Message msg) {  
                super.handleMessage(msg);  
                String result = (String) msg.obj;  
                if(!append && data.size()>0){
                	data.clear();
                }
//                Toast.makeText( HotHotActivity.this, "重试中000-"+msg.what+","+result, Toast.LENGTH_SHORT).show();
                switch (msg.what) {  
                case 200:  
                	
                    try {
						JSONArray jsonarr = new JSONArray(result);
						if(jsonarr.length()<=0){
							videosLoadingHint.setText("已没有热播的电影数据,请稍后再试。");
							mFooterParentXml.setVisibility(View.GONE);
							return;
						}
						videosLoadingHint.setVisibility(View.GONE);
						
						int oldPosition = data.size();
						for(int i = 0 ; i < jsonarr.length(); i++){
							JSONObject jobject = jsonarr.getJSONObject(i);
							Map<String,Object> rowData = new HashMap<String,Object>();
							rowData.put("id", jobject.getLong("id"));
							rowData.put("title", jobject.getString("title"));
							rowData.put("createdBy", jobject.getString("createdBy"));
							rowData.put("createdTime", jobject.getString("createdTime"));
							rowData.put("coverpageImageLink", jobject.getString("coverpageImageLink"));
							rowData.put("thumbsUPCount", jobject.getString("thumbsUPCount"));
							rowData.put("description", jobject.getString("description"));
							rowData.put("freeVideoFlag", jobject.getString("freeVideoFlag"));
							rowData.put("minimumAge", jobject.getString("minimumAge"));
							rowData.put("videoType", jobject.getString("videoType"));
							String videoType =jobject.getString("videoType");
							if(jobject.getString("freeVideoFlag")!=null && jobject.getString("freeVideoFlag").equalsIgnoreCase("Y")){
								VideoPlayerActivity.videosFreebies.add(String.valueOf(jobject.getLong("id")));
								VideoDuoPlayerActivity.videosFreebies.add(String.valueOf(jobject.getLong("id")));
							}
							
							rowData.put("desc", "热度:+"+jobject.getString("thumbsUPCount")+"<br>可试看(<font color='gray'>原价:2元</font> <font color='red'>现价:0.5元</font>)");
							
							if(VideoPlayerActivity.videosFreebies.contains(String.valueOf(jobject.getLong("id")))){
								rowData.put("desc", "热度:+"+jobject.getString("thumbsUPCount")+"<br>免费看");
    						}
							if(VideoPlayerActivity.videosBought.contains(String.valueOf(jobject.getLong("id")))){
								rowData.put("desc", "热度:+"+jobject.getString("thumbsUPCount")+"<br>已购买");
							}
							if(membershipExpiryDate!=null){
								rowData.put("desc", "热度:+"+jobject.getString("thumbsUPCount")+"<br>您已是会员:全场免费");
							}
							if(VideoPlayerActivity.videosLuckyShakes.contains(String.valueOf(jobject.getLong("id")))){
								rowData.put("desc", "热度:+"+jobject.getString("thumbsUPCount")+"<br>幸运一天");
							}
							
							
							if(videoType.equals("Type_VideoUrl")){
								rowData.put("videoFileLink", jobject.getString("videoFileLink"));
							}else{
								rowData.put("videoFileLink", String.valueOf(PropertiesUtilWithEnc.prop.getProperty("domainCall"))+jobject.getString("videoFileLink"));
							}
							if(videoType.equals("Type_VideoFileLink")||videoType.equals("Type_VideoUrl")||videoType.equals("Type_VideoFolderLink")){
								if(!videoIDs.contains(jobject.getLong("id"))){
									if(jobject.getString("freeVideoFlag")!=null && jobject.getString("freeVideoFlag").equalsIgnoreCase("Y")){
										VideoPlayerActivity.videosFreebies.add(String.valueOf(jobject.getLong("id")));
									}
									data.add(rowData);
									videoIDs.add(jobject.getLong("id"));
								}
								
							}
						}
						
						
						List<Map<String,Object>> dataMerge = new ArrayList<Map<String,Object>>();
						
						for(int i = 0;i<data.size();i++){
							
							Map<String,Object> tempRow = data.get(i);
							tempRow.put("itemIdx", "序号"+(i+1));
							dataMerge.add(tempRow);
						}
						adapter = new MyAdapter(HotHotActivity.this, data);
						
				        tv_serverVideoCatList.setAdapter(adapter);
				        if(data.size()>currentPageSize){
				        	tv_serverVideoCatList.setSelection(oldPosition-visibleItemCountLastTime+1);
	                	}
				        Toast.makeText( HotHotActivity.this, "当前第"+currentPageNo+"页.加载了"+jsonarr.length()+"部"+(data.size()+1-currentPageSize)+"-"+(data.size())+"号", Toast.LENGTH_SHORT).show();
				        
					} catch (Exception e) {
						videosLoadingHint.setText("对不起,加载热播电影失败,请稍后再试。"+e.getMessage());
					}
                    
                    break;  
                case 404:  
                	videosLoadingHint.setText("对不起,加载热播电影失败,请稍后再试。");
                    break;  
                default:
                	videosLoadingHint.setText("对不起,加载热播电影失败,请稍后再试。");
                }
                
            }  
        }; 
        
        if(!append && data.size()>0){
        	HotHotActivity.currentPageNo = 1;
        }
        
        videosLoadingHint.setVisibility(View.VISIBLE);
        videosLoadingHint.setText("加载热播影片中...");
        int minAge = 18;
    	if(loginUserProxyEmail !=null && loginUserProxyEmail.contains("@")){
    		minAge = 99;
    	}
        String url = PropertiesUtilWithEnc.getString("domainCall")+"/dictionary/hotFilmsInPage/"+PropertiesUtilWithEnc.getString("fixedFilmCatID")+"/"+currentPageNo+"/"+currentPageSize+"/"+minAge+"/";
        HttpsGetThread thread = new HttpsGetThread(httpHandler,  url, 200);  
        thread.start();
        
    }  
    @Override
    public void onBackPressed() {
    	currentPageNo = 1;
    	currentPageSize = 8;
    	visibleItemCountLastTime = 0;
    	data.clear();
    	videoIDs.clear();
    	super.onBackPressed();
    }
}
