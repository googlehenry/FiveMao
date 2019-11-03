package com.ex.fivemao;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.json.JSONArray;
import org.json.JSONObject;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.ex.fivemao.exception.CrashHandler;
import com.ex.fivemao.io.CacheFileUtil;
import com.ex.fivemao.io.PropertiesUtilWithEnc;
import com.ex.fivemao.ui.BaseIntent;
import com.ex.fivemao.ui.MakeSureDialog;
import com.ex.fivemao.ui.MyAdapter;
import com.ex.fivemao.ui.SearchEditText;
import com.ex.fivemao.ui.SearchEditText.OnSearchClickListener;
import com.example.myvideos.http.HttpsGetThread;
import com.example.myvideos.http.MultipartFormHttpsPostThread;


public class SearchActivity extends BaseActivity {
	public static Integer currentPageNo = 1;
	public static Integer currentPageSize = 10;
	private static Integer visibleItemCountLastTime = 0;
	
	public static List<String> searchHistory = new ArrayList<String>();
	public static List<String> searchNoResultAskingForFilms = new ArrayList<String>();
	public static int searchHistoryLimit = 5;
	private Handler httpHandler; 
	ListView meSearchVideos;
	TextView searchHis1;
	TextView searchHis2;
	TextView searchHis3;
	TextView searchHis4;
	TextView searchHis5;
	private MyAdapter adapter;
	public static List<Map<String,Object>> data = new ArrayList<Map<String,Object>>();
	
	SearchEditText searchEdittext ; 
	TextView meSearchVideosLoadingHint;
	private String requestedSearchKey;
	
	
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);
        searchEdittext = (SearchEditText)findViewById(R.id.searchEdittext);
        searchHis1 = (TextView)findViewById(R.id.searchHis1);
        searchHis2 = (TextView)findViewById(R.id.searchHis2);
        searchHis3 = (TextView)findViewById(R.id.searchHis3);
        searchHis4 = (TextView)findViewById(R.id.searchHis4);
        searchHis5 = (TextView)findViewById(R.id.searchHis5);
        meSearchVideos = (ListView)findViewById(R.id.meSearchVideos);
        meSearchVideosLoadingHint = (TextView)findViewById(R.id.meSearchVideosLoadingHint);

        Intent intentSearch= getIntent();
        
        
        if(intentSearch!=null){
        	requestedSearchKey = intentSearch.getStringExtra("searchKey");
        }
        
        
        
        searchHis1.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				searchEdittext.requestFocus();
				searchEdittext.setText(searchHis1.getText());
				processSearch();
			}
		});
        searchHis2.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				searchEdittext.requestFocus();
				searchEdittext.setText(searchHis2.getText());
				processSearch();
			}
		});
        searchHis3.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				searchEdittext.requestFocus();
				searchEdittext.setText(searchHis3.getText());
				processSearch();
			}
		});
        searchHis4.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				searchEdittext.requestFocus();
				searchEdittext.setText(searchHis4.getText());
				processSearch();
			}
		});
        searchHis5.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				searchEdittext.requestFocus();
				searchEdittext.setText(searchHis5.getText());
				processSearch();
			}
		});
        
        searchEdittext.setOnSearchClickListener(new OnSearchClickListener() {
			@Override
			public void onSearchClick(View view) {
				processSearch();
			}
		});
        
        meSearchVideos.setOnScrollListener(new OnScrollListener() {
        	private int totalItem;  
            private int lastItem;  
              
            
			@Override
			public void onScrollStateChanged(AbsListView view, int scrollState) {
				if(this.totalItem == lastItem&&scrollState == SCROLL_STATE_IDLE){  
		            if(!meSearchVideosLoadingHint.isShown()){  
		            	SearchActivity.currentPageNo = SearchActivity.currentPageNo + 1;  
		            	searchFilmsData(true);
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
        
        meSearchVideos.setOnItemClickListener(new OnItemClickListener(){

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
				intent.setClass(SearchActivity.this, VideoDescActivity.class);  
	            //Toast.makeText( SubActivityVideoList.this, "ok,clicked "+arg2+","+line.get("id"), Toast.LENGTH_SHORT).show();
	            startActivity(intent);  
			}
        	
        });
        
        loadSearchHistory();
        
        if(requestedSearchKey!=null){
        	searchEdittext.requestFocus();
	        searchEdittext.setText(requestedSearchKey);
	        searchNoResultAskingForFilms.add(requestedSearchKey);
	        processSearch();
        }else{
        	//display last time search results
        	if(searchHistory.size()>0){
	    		searchEdittext.requestFocus();
				searchEdittext.setText(searchHistory.get(searchHistory.size()-1));
				searchNoResultAskingForFilms.add(searchHistory.get(searchHistory.size()-1));
				processSearch();
				searchEdittext.setText("");
        	}
        }
    }




	private void displayTopMoviesFromHomepage() {
		if(MainActivity.data.size()>0 && data.size()==0){
			data.addAll(MainActivity.data);
			adapter = new MyAdapter(SearchActivity.this, data);
	        meSearchVideos.setAdapter(adapter);
		}
	}




	private void loadSearchHistory() {
		
		int avaiLength = searchHistory.size()>=searchHistoryLimit?searchHistoryLimit:searchHistory.size();
		if(avaiLength>=1){
			searchHis1.setText(searchHistory.get(searchHistory.size()-1));
			searchHis1.setVisibility(View.VISIBLE);
		}
		if(avaiLength>=2){
			searchHis2.setText(searchHistory.get(searchHistory.size()-2));
			searchHis2.setVisibility(View.VISIBLE);
		}
		if(avaiLength>=3){
			searchHis3.setText(searchHistory.get(searchHistory.size()-3));
			searchHis3.setVisibility(View.VISIBLE);
		}
		if(avaiLength>=4){
			searchHis4.setText(searchHistory.get(searchHistory.size()-4));
			searchHis4.setVisibility(View.VISIBLE);
		}
		if(avaiLength>=5){
			searchHis5.setText(searchHistory.get(searchHistory.size()-5));
			searchHis5.setVisibility(View.VISIBLE);
		}
		
		if(loginUserName!=null){
			Set<String> labels = new HashSet<String>();
			StringBuilder sb = new StringBuilder();
			if(searchHis5.getVisibility()==View.VISIBLE && searchHis5.getText().length()>0){
				String txt = searchHis5.getText().toString();
				labels.add(txt);
				sb.append(txt).append(",");
			}
			if(searchHis4.getVisibility()==View.VISIBLE && searchHis4.getText().length()>0){
				String txt = searchHis4.getText().toString();
				if(!labels.contains(txt)){
					labels.add(txt);
					sb.append(txt).append(",");
				}
			}
			if(searchHis3.getVisibility()==View.VISIBLE && searchHis3.getText().length()>0){
				String txt = searchHis3.getText().toString();
				if(!labels.contains(txt)){
					labels.add(txt);
					sb.append(txt).append(",");
				}
			}
			if(searchHis2.getVisibility()==View.VISIBLE && searchHis2.getText().length()>0){
				String txt = searchHis2.getText().toString();
				if(!labels.contains(txt)){
					labels.add(txt);
					sb.append(txt).append(",");
				}
			}
			if(searchHis1.getVisibility()==View.VISIBLE && searchHis1.getText().length()>0){
				String txt = searchHis1.getText().toString();
				if(!labels.contains(txt)){
					labels.add(txt);
					sb.append(txt).append(",");
				}
			}
			
			String sh = sb.toString();
			
			loginUserEmail = loginUserEmail==null?"":loginUserEmail;
			loginUserProxyEmail = loginUserProxyEmail==null?"":loginUserProxyEmail;
			
			CacheFileUtil.writeCacheFile(getCacheDir().getAbsolutePath(), String.valueOf(PropertiesUtilWithEnc.prop.get("cacheFileName")),
					loginUserName+"notakid"+loginUserBirthday+"notakid"+loginUserEmail+";"+loginUserProxyEmail
					+"notakid"+sh);
		}
	}

	private boolean containsInTop5(String item){
		Set<String> searchHistoryTop5 = new HashSet<String>();
		int avaiLength = SearchActivity.searchHistory.size()>=SearchActivity.searchHistoryLimit?SearchActivity.searchHistoryLimit:SearchActivity.searchHistory.size();
		for(int i = 0; i < avaiLength; i++){
			searchHistoryTop5.add(searchHistory.get(searchHistory.size()-1-i));
		}
		return searchHistoryTop5.contains(item);
	}
	private void addToStack(String item){
		if(containsInTop5(item)){
		}else{
			searchHistory.add(item);
			loadSearchHistory();
		}
	}

	@Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        for(int i = 0; i < menu.size();i ++){
        	MenuItem item = menu.getItem(i);
        	if(item.getItemId()==R.id.action_quicklinkFilmList){
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
    private void showNewFilmRequestDialog() {
        /*@setView 装入一个EditView
         */
    	
    	final MakeSureDialog dialog = new MakeSureDialog();
    	dialog.setTextTitle("求片《"+searchEdittext.getText().toString()+"》？");
  		dialog.setTextSureButton("申请收录");
  		dialog.setTextCancelButton("不用了");
  		dialog.setTextSizeContent(20);
  		if(loginUserName!=null){
  			dialog.setContent("[求片]我是:"+starMyName(loginUserName)+",我想看电影:《"+searchEdittext.getText().toString()+"》,麻烦收录一下,谢谢。");
  		}else{
  			dialog.setContent("[求片]我没有登录,不过我想看电影:《"+searchEdittext.getText().toString()+"》,麻烦收录一下,谢谢。");
  		}
  		
  		
  		dialog.setDialogClickListener(new MakeSureDialog.onDialogClickListener() {  
            @Override  
            public void onSureClick() {
            	
            	try {
					sendFilmRequest(dialog.getTextContent());
				} catch (UnsupportedEncodingException e) {
					CrashHandler.logErrorToFile(e);
				}
            	
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
    protected void sendFilmRequest(String textContent) throws UnsupportedEncodingException {
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
                			Toast.makeText( SearchActivity.this, "求片请求已发送["+result+"],请在48小时候再次查看。", Toast.LENGTH_LONG).show();
                		}
						
					} catch (Exception e) {
						CrashHandler.logErrorToFile(e);
					}
                    break;  
                case 404:  
                	Toast.makeText( SearchActivity.this, "求片请求发送失败,后端服务已关闭。", Toast.LENGTH_SHORT).show();
                    break;  
                }  
  
            }  
        },String.valueOf(PropertiesUtilWithEnc.prop.getProperty("domainCall"))+"/itemcomment/addCommentWithType/FiveMaoMessage/?",pairs,null);  
        thread.start();
	}




	private void searchFilmsData(boolean showMore) {  
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
							meSearchVideosLoadingHint.setText("没有搜索结果,请稍后再试。");
							if(data.size()==0){
								Thread.sleep(500);
								remindUserToAddRecEmails(false);
								//showNewFilmRequestDialog();
							}
							return;
						}
						meSearchVideosLoadingHint.setVisibility(View.GONE);
						for(int i = 0 ; i < jsonarr.length(); i++){
							JSONObject jobject = jsonarr.getJSONObject(i);
							Map<String,Object> rowData = new HashMap<String,Object>();
							
							Boolean isActive = jobject.getBoolean("isActive");
							if(isActive!=null && !isActive){
								meSearchVideosLoadingHint.setText("搜索结果被过滤,请稍后再试。");
								if(data.size()==0){
									Thread.sleep(500);
									remindUserToAddRecEmails(true);
								}
								return;
							}
							
							rowData.put("id", jobject.getLong("id"));
							rowData.put("title", jobject.getString("title"));
							rowData.put("createdBy", jobject.getString("createdBy"));
							rowData.put("createdTime", jobject.getString("createdTime"));
							rowData.put("coverpageImageLink", jobject.getString("coverpageImageLink"));
							rowData.put("description", jobject.getString("description"));
							rowData.put("freeVideoFlag", jobject.getString("freeVideoFlag"));
							rowData.put("minimumAge", jobject.getString("minimumAge"));
							rowData.put("videoType", jobject.getString("videoType"));
							String videoType =jobject.getString("videoType");
							if(jobject.getString("freeVideoFlag")!=null && jobject.getString("freeVideoFlag").equalsIgnoreCase("Y")){
								VideoPlayerActivity.videosFreebies.add(String.valueOf(jobject.getLong("id")));
								VideoDuoPlayerActivity.videosFreebies.add(String.valueOf(jobject.getLong("id")));
							}
							
							rowData.put("itemIdx", "序号"+(i+1));
							rowData.put("desc", "可试看(<font color='gray'>原价:2元</font> <font color='red'>现价:0.5元</font>)");
							if(VideoPlayerActivity.videosFreebies.contains(String.valueOf(jobject.getLong("id")))){
    							rowData.put("desc", "免费看");
    						}
							if(VideoPlayerActivity.videosBought.contains(String.valueOf(jobject.getLong("id")))){
								rowData.put("desc", "已购买");
							}
							if(membershipExpiryDate!=null){
								rowData.put("desc", "您已是会员:全场免费");
							}
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
						List<Map<String,Object>> dataMerged = new ArrayList<Map<String,Object>>();
						
						for(int i = 0;i<data.size();i++){
							
							Map<String,Object> tempRow = data.get(i);
							tempRow.put("itemIdx", "序号"+(i+1));
							dataMerged.add(tempRow);
						}
						
						adapter = new MyAdapter(SearchActivity.this, dataMerged);
						
				        meSearchVideos.setAdapter(adapter);
				        
				        if(data.size()>currentPageSize){
				        	meSearchVideos.setSelection(data.size()-visibleItemCountLastTime-jsonarr.length()+1);
	                	}
				        
				        Toast.makeText( SearchActivity.this, "当前第"+currentPageNo+"页.加载了"+jsonarr.length()+"部", Toast.LENGTH_SHORT).show();
				        
					} catch (Exception e) {
						meSearchVideosLoadingHint.setText("对不起,搜索电影失败,请稍后再试。");
					}
                    
                    break;  
                case 404:  
                    // 请求失败
                	meSearchVideosLoadingHint.setText("对不起,搜索电影失败,请稍后再试。");
                    break;
                default:
                	meSearchVideosLoadingHint.setText("对不起,搜索电影失败,请稍后再试。");
                }  
                meSearchVideosLoadingHint.setText("对不起,加载已购电影失败,请稍后再试。");
            }  
        }; 
        
        meSearchVideosLoadingHint.setVisibility(View.VISIBLE);
        meSearchVideosLoadingHint.setText("搜索电影中...");
        if(!showMore && data.size()>0){
        	currentPageNo = 1;
        	data.clear();
        }
        
        int minAge = 18;
    	if(loginUserProxyEmail !=null && loginUserProxyEmail.contains("@")){
    		minAge = 99;
    	}
        //服务器返回首先匹配的分页结果记录
        HttpsGetThread thread = new HttpsGetThread(httpHandler,  
        		//
        		String.valueOf(PropertiesUtilWithEnc.prop.getProperty("domainCall"))+"/dictionary/filmsInKeywords/"+PropertiesUtilWithEnc.getString("fixedFilmCatID")+"/"+currentPageNo+"/"+currentPageSize+"/"+minAge+"/?searchKey="+searchEdittext.getText().toString(), 200);  
        thread.start();
    }

	protected void remindUserToAddRecEmails(boolean has18PlusContent) {
		
		String fName = searchEdittext.getText().toString().trim();
    	if(searchNoResultAskingForFilms.contains(fName) || fName.length()<2 || fName.length()> 40){
    		return;
    	}else{
    		if(has18PlusContent){
    		}else{
    			searchNoResultAskingForFilms.add(fName);
    		}
    	}
    	
		if(loginUserName!=null && (loginUserProxyEmail==null || !loginUserProxyEmail.contains("@")) && has18PlusContent){
			MakeSureDialog dialog = new MakeSureDialog();
	    	dialog.setTextTitle("搜索结果被过滤");
	    	dialog.setContent("您的资料中没有填写【推荐人邮箱】,这会导致您搜索结果部分内容被过滤,询问推荐人或者右上角【联系客服】获取,马上去完善？");
	  		dialog.setTextSureButton("完善推荐人");
	  		dialog.setTextCancelButton("暂不");
	  		
	  		dialog.setDialogClickListener(new MakeSureDialog.onDialogClickListener() {  
	            @Override  
	            public void onSureClick() {
	            	modifyMyProfile();
	            }  
	            @Override  
	            public void onCancelClick() {
	            	//showNewFilmRequestDialog();
	            }  
	        });  
	  		try{
	     		dialog.show(getFragmentManager(),"");
	     	}catch(Exception e1){
	     		CrashHandler.logErrorToFile(e1);
	     	}
  		
		}else{
			if(loginUserName==null && has18PlusContent){
				MakeSureDialog dialog = new MakeSureDialog();
		    	dialog.setTextTitle("搜索结果被限制");
		    	dialog.setContent("您没有登陆,这会导致您搜索结果受限,马上登录？");
		  		dialog.setTextSureButton("去登录");
		  		dialog.setTextCancelButton("暂不");
		  		
		  		dialog.setDialogClickListener(new MakeSureDialog.onDialogClickListener() {  
		            @Override  
		            public void onSureClick() {
		            	gotoMePage();
		            }  
		            @Override  
		            public void onCancelClick() {  
		            	//showNewFilmRequestDialog();
		            }  
		        });  
		  		try{
		     		dialog.show(getFragmentManager(),"");
		     	}catch(Exception e1){
		     		CrashHandler.logErrorToFile(e1);
		     	}
			}else{
				
				showNewFilmRequestDialog();
			}
			
		}
	}




	private void processSearch() {
		Pattern p = Pattern.compile("[\u4e00-\u9fa5]");
		Matcher m = p.matcher(searchEdittext.getText().toString());
		if(searchEdittext.getText().toString().length()>1 || m.find()){
			searchFilmsData(false);
			addToStack(searchEdittext.getText().toString());
			Toast.makeText( SearchActivity.this, "开始搜索:"+searchEdittext.getText().toString(), Toast.LENGTH_SHORT).show();
		}else{
			Toast.makeText( SearchActivity.this, "关键词太短,没有结果.", Toast.LENGTH_SHORT).show();
		}
	}
	
	@Override
	public void onBackPressed() {
		searchHistory.clear();
		searchNoResultAskingForFilms.clear();
		searchHistoryLimit = 5;
		data.clear();
		currentPageNo = 1;
		currentPageSize = 10;
		visibleItemCountLastTime = 0;
		requestedSearchKey = null;
		super.onBackPressed();
	}
}
