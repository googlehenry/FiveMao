package com.ex.fivemao;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import android.content.Intent;
import android.os.Bundle;
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

import com.ex.fivemao.io.CacheFileUtil;
import com.ex.fivemao.ui.MyAdapter;


public class MeVisitedVideosActivity extends BaseActivity {
	ListView meVisitedVideos;
	private MyAdapter adapter;
	public static List<Map<String,Object>> data = new ArrayList<Map<String,Object>>();
	
	TextView meVisitedVideosLoadingHint;
	
	int maxHistoryCount = 60;
	
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_me_visitedvideos);
        meVisitedVideos = (ListView)findViewById(R.id.meVisitedVideos);
        meVisitedVideosLoadingHint = (TextView)findViewById(R.id.meVisitedVideosLoadingHint);
        
        meVisitedVideos.setOnItemClickListener(new OnItemClickListener(){

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

				intent.setClass(MeVisitedVideosActivity.this, VideoDescActivity.class);  
	            
	            //Toast.makeText( SubActivityVideoList.this, "ok,clicked "+arg2+","+line.get("id"), Toast.LENGTH_SHORT).show();
	            startActivity(intent);  
			}
        	
        });
        
        meVisitedVideos.setOnScrollListener(new OnScrollListener() {
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
        
        
        initMyVisitedFilmsData();
        
        
    }
    private void initMyVisitedFilmsData() {
    	String content = CacheFileUtil.readCacheFile(getCacheDir().getAbsolutePath(), "visithistory.dat");
    	List<Map<String,Object>> dataFromFile = new ArrayList<Map<String,Object>>();
		if(content!=null){
			String[] lines = content.split("\n");
			int counter = 0;
			for(String line:lines){
				if(line.contains("notakid")){
					String[] videoInfos = line.split("notakid");
					if(videoInfos!=null && videoInfos.length>=8){
						Map<String,Object> rowData = new HashMap<String,Object>();
						String id = videoInfos[0];
						rowData.put("id", id);
						rowData.put("title", videoInfos[1]);
						rowData.put("createdBy", "");
						rowData.put("createdTime", "");
						String coverFileName = videoInfos[2];
						rowData.put("coverpageImageLink", CacheFileUtil.readCacheFile(getCacheDir().getAbsolutePath(), coverFileName));
						String videoType = videoInfos[3];
						rowData.put("videoType", videoType);
						rowData.put("videoFileLink", videoInfos[4]);
						String visitedTime = videoInfos[5];
						rowData.put("visitedTime", visitedTime);
						rowData.put("minimumAge", videoInfos[6]);
						rowData.put("description", videoInfos[7]);
						
						rowData.put("itemIdx", "序号"+(counter+1));
						rowData.put("desc", "观看时间:"+visitedTime);
						
						dataFromFile.add(rowData);
						counter = counter + 1;
					}
				}
			}
			
			Collections.reverse(dataFromFile);
			
			Set<String> ids = new HashSet<String>();
			int totalcount = 0;
			for(Map<String,Object> line:dataFromFile){
				String idStr = String.valueOf(line.get("id"));
				if(!ids.contains(idStr)){
					ids.add(idStr);
					line.put("itemIdx", totalcount);
					data.add(line);
					totalcount = totalcount + 1;
					if(totalcount>maxHistoryCount){
						Toast.makeText(MeVisitedVideosActivity.this, "仅保留"+maxHistoryCount+"条播放记录", Toast.LENGTH_SHORT).show();
						break;
					}
				}
			}
			
			
			adapter = new MyAdapter(MeVisitedVideosActivity.this, data);
			
	        meVisitedVideos.setAdapter(adapter);
	        
	        
		}
		MeVisitedVideosActivity.this.setTitle("播放历史列表:共"+data.size()+"部");
        if(data.size()>0){
	        Toast.makeText(MeVisitedVideosActivity.this, "您已访问电影"+data.size()+"部,在菜单>>设置>>清除播放历史,清除记录", Toast.LENGTH_SHORT).show();
	        
	        saveCompactedHistoryToFile();
        }
	}
    
	private void saveCompactedHistoryToFile() {
		List<Map<String,Object>> dataToFile = new ArrayList<Map<String,Object>>();
		dataToFile.addAll(data);
		Collections.reverse(dataToFile);
		
		StringBuilder sb = new StringBuilder();
		String sep = "notakid";
		for(Map<String,Object> item:dataToFile){
			String coverFileName = "videoCoverpageImageLink_"+item.get("id")+".base64.jpg";
			sb
			.append(item.get("id")).append(sep)
			.append(item.get("title")).append(sep)
			.append(coverFileName).append(sep)
			.append(item.get("videoType")).append(sep)
			.append(item.get("videoFileLink")).append(sep)
			.append(item.get("visitedTime")).append(sep)
			.append(item.get("minimumAge")).append(sep)
			.append(item.get("description")).append("\n");
		}
		
		String extraContent = sb.toString();
		CacheFileUtil.appendCacheFile(getCacheDir().getAbsolutePath(), "visithistory.dat",extraContent);
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
    
    @Override
 	public void onBackPressed() {
 		data.clear();
 		super.onBackPressed();
 	}
}
