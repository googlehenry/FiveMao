package com.ex.fivemao;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.json.JSONArray;
import org.json.JSONObject;

import android.app.AlertDialog;
import android.content.DialogInterface;
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
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.ex.fivemao.io.CacheFileUtil;
import com.ex.fivemao.io.PropertiesUtilWithEnc;
import com.ex.fivemao.ui.MyAdapter;
import com.example.myvideos.http.HttpsGetThread;


public class VideoActivity extends BaseActivity {
	public static Integer currentPageNo = 1;
	public static Integer currentPageSize = 10;
	private static Integer visibleItemCountLastTime = 0;
	
	private static Integer firstVisibleItemID = 0;
	private static Integer currentPageNoWithFirstVisibleItem = 0;
	private static Integer timerCount = 3;
	
	private ListView tv_serverVideoCatList;
	TextView videosLoadingHint;
	private Handler httpHandler ;
	private View mFooterParentXml;

	private MyAdapter adapter;
	public static List<Map<String,Object>> data = new ArrayList<Map<String,Object>>();
	private static Set<Long> videoIDs = new HashSet<Long>();
	public static String[] items = { "����¼˳��(��-��)","����¼����(��-��)","��Ƭ��˳��(A-Z)","��Ƭ������(Z-A)","���ּ�����(��ͨ-����)","���ּ�����(����-��ͨ)" };
	private int videoLoadingStrategyCode = 2;
	
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video);
        videosLoadingHint = (TextView)findViewById(R.id.videosLoadingHint);
        tv_serverVideoCatList = (ListView)findViewById(R.id.videos);
        
        mFooterParentXml = LayoutInflater.from(this).inflate(R.layout.layout_footer_listview, null);//����footerParent����
        View parentFooter = mFooterParentXml.findViewById(R.id.mFooterparent);

        
//        tv_serverVideoCatList.addFooterView(parentFooter);
        
        loadAndSetVideoLoadingStrategy();
        
        if((loginUserProxyEmail==null || !loginUserProxyEmail.contains("@"))){
        	AlertDialog.Builder builder = new AlertDialog.Builder(this);  
            builder.setIcon(android.R.drawable.ic_dialog_info);  
            builder.setTitle("��Ƶ�б� ��ʾ��Ϣ");
            builder.setMessage("��ǰ����ʾ����ͨ��Ӱ���б�,�ۿ������Ƽ���Ӱ����Ҫ��¼���ڡ�������Ϣ-�޸���Ϣ���˵������á��Ƽ������䡿��\n" +
            		"��ѻ�ȡ�Ƽ������������ַ�ʽ:һ��ѯ���Լ����Ƽ���;�������Ͻǲ˵�����ϵ�ͷ�����ȡ��");
            builder.setPositiveButton("ȷ��", new DialogInterface.OnClickListener() {  
                public void onClick(DialogInterface dialog, int which) {
                	dialog.dismiss();  
                }
            });  
            
            builder.show(); 
        	//Toast.makeText(this, "ע��ʱ���Ƽ��ˡ�Ϊ��,����ʾ�������ݡ����¼���ڡ�������Ϣ-�޸���Ϣ�����޸�", Toast.LENGTH_SHORT).show();
        }
        //initData(false);
        
        
        tv_serverVideoCatList.setOnScrollListener(new OnScrollListener() {
        	private int totalItem;  
            private int lastItem;
            private int firstItem;  
              
            
			@Override
			public void onScrollStateChanged(AbsListView view, int scrollState) {
				if(this.totalItem == lastItem&&scrollState == SCROLL_STATE_IDLE){
					if(videosLoadingHint.isShown() && videosLoadingHint.getText().toString().contains("��һҳ")){
						videosLoadingHint.setVisibility(View.GONE);
						VideoActivity.currentPageNo = data.size()/currentPageSize;  
					}
		            if(!videosLoadingHint.isShown()){  
		            	VideoActivity.currentPageNo = VideoActivity.currentPageNo + 1;  
		                initData(true);
		                timerCount = timerCount -1;
		                if(timerCount==0){
		                	showInputDialog();
		                	timerCount = 3;
		                }
		            }
		        }else if(this.firstItem == 0 &&scrollState == SCROLL_STATE_IDLE){
		        	if(!videosLoadingHint.isShown()){  
		            	VideoActivity.currentPageNo = VideoActivity.currentPageNo - 1;  
		                initDataBack(true);
		                timerCount = timerCount -1;
		                if(timerCount==0 && VideoActivity.currentPageNo>0){
		                	showInputDialog();
		                	timerCount = 3;
		                }
		            }
		        }
			}
			
		
			@Override
			public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
				
				this.lastItem = firstVisibleItem+visibleItemCount;  
		        this.totalItem = totalItemCount;  
		        this.firstItem = firstVisibleItem;
		        visibleItemCountLastTime = visibleItemCount;
		        try{
		        	firstVisibleItemID = Integer.valueOf(String.valueOf(data.get(firstVisibleItem).get("id")));
		        	currentPageNoWithFirstVisibleItem = currentPageNo-((totalItemCount-firstVisibleItem)/currentPageSize);
		        }catch(Exception e){
		        }
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
	            //setClass�����ĵ�һ��������һ��Context����  
	            //Context��һ����,Activity��Context�������,Ҳ����˵,���е�Activity���󶼿�������ת��ΪContext����  
	            //setClass�����ĵڶ���������Class����,�ڵ�ǰ������,Ӧ�ô�����Ҫ��������Activity��class����
				intent.setClass(VideoActivity.this, VideoDescActivity.class);  
	            
	            //Toast.makeText( SubActivityVideoList.this, "ok,clicked "+arg2+","+line.get("id"), Toast.LENGTH_SHORT).show();
	            startActivity(intent);  
			}
        	
        });
        
    }

    private void loadAndSetVideoLoadingStrategy() {
    	String strategy = CacheFileUtil.readCacheFile(getCacheDir().getAbsolutePath(), "videoloadingstrategy.dat");
    	
    	try{
    		if(strategy!=null && strategy.contains("notakid")){
    			String[] pageInfos = strategy.split("notakid");
    			if(pageInfos!=null && pageInfos.length>=2){
        			String code = pageInfos[0];
        			String desc = pageInfos[1];
	        		videoLoadingStrategyCode  = Integer.parseInt(code);
	        		initData(false);
    			}
    		}else{
    			showChooseVideoLoadingStrategyDialog();
    		}
    	}catch(Exception e){
    		videoLoadingStrategyCode = 2 ;
    		initData(false);
    	}
    	
    	
	}

    int yourChoice;
    private void showChooseVideoLoadingStrategyDialog(){
        
        yourChoice = 2;
        AlertDialog.Builder singleChoiceDialog = 
            new AlertDialog.Builder(VideoActivity.this);
        singleChoiceDialog.setTitle("��Ӱ��������");
        // �ڶ���������Ĭ��ѡ��˴�����Ϊ0
        singleChoiceDialog.setSingleChoiceItems(items, 2, 
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
                	videoLoadingStrategyCode = yourChoice;
                	
                    Toast.makeText(VideoActivity.this, 
                    "��ѡ����" + items[yourChoice]+"�������п����޸�", 
                    Toast.LENGTH_SHORT).show();
                    
                    initData(false);
                    
                    CacheFileUtil.writeCacheFile(getCacheDir().getAbsolutePath(), "videoloadingstrategy.dat",yourChoice+"notakid"+items[yourChoice]);
                }
            }
        });
        
        singleChoiceDialog.setCancelable(false);
        singleChoiceDialog.show();
    }
    
    
	private void showInputDialog() {
        /*@setView װ��һ��EditView
         */
        final EditText editText = new EditText(VideoActivity.this);
        editText.setHint("����ҳ�� ��հ״�ȡ��");
        
        AlertDialog.Builder inputDialog = new AlertDialog.Builder(VideoActivity.this);
        inputDialog.setTitle("��ʾ:��ǰ��"+currentPageNo+"ҳ,ÿҳ"+currentPageSize+"��,ֱ��ת������XXҳ��").setView(editText);
        inputDialog.setPositiveButton("ȷ��", 
            new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
            	String input = editText.getText().toString();
            	if(input.length()>0){
            		try{
            			Integer pageInput = Integer.parseInt(input);
            			data.clear();
            			currentPageNo = pageInput; 
            			initData(true);
            		}catch(Exception e){
            			Toast.makeText(VideoActivity.this, "����ת��:ҳ�����,������һ������", Toast.LENGTH_SHORT).show();
            		}
            	}
            }
        }).show();
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
                
//                Toast.makeText( VideoActivity.this, "������000-"+msg.what, Toast.LENGTH_SHORT).show();
                switch (msg.what) {  
                case 200:  
                    try {
                    	
						JSONArray jsonarr = new JSONArray(result);
						
						if(jsonarr.length()<=0){
							mFooterParentXml.setVisibility(View.GONE);
							videosLoadingHint.setText("��û���Ƽ��ĵ�Ӱ,���Ժ����ԡ�");
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
							rowData.put("description", jobject.getString("description"));
							rowData.put("freeVideoFlag", jobject.getString("freeVideoFlag"));
							rowData.put("minimumAge", jobject.getString("minimumAge"));
							rowData.put("videoType", jobject.getString("videoType"));
							String videoType =jobject.getString("videoType");
							if(jobject.getString("freeVideoFlag")!=null && jobject.getString("freeVideoFlag").equalsIgnoreCase("Y")){
								VideoPlayerActivity.videosFreebies.add(String.valueOf(jobject.getLong("id")));
								VideoDuoPlayerActivity.videosFreebies.add(String.valueOf(jobject.getLong("id")));
							}
							
							rowData.put("desc", "���Կ�(<font color='gray'>ԭ��:2Ԫ</font> <font color='red'>�ּ�:0.5Ԫ</font>)");
							if(VideoPlayerActivity.videosFreebies.contains(String.valueOf(jobject.getLong("id")))){
    							rowData.put("desc", "��ѿ�");
    						}
							if(VideoPlayerActivity.videosBought.contains(String.valueOf(jobject.getLong("id")))){
								rowData.put("desc", "�ѹ���");
							}
							if(membershipExpiryDate!=null){
								rowData.put("desc", "�����ǻ�Ա:ȫ�����");
							}
							if(VideoPlayerActivity.videosLuckyShakes.contains(String.valueOf(jobject.getLong("id")))){
								rowData.put("desc", "����һ��");
							}
							
							
//							if((jobject.getLong("id")-firstVisibleItemID)==0){
//								rowData.put("desc",rowData.get("desc")+"\n�ϴο�������");
//							}
							
							if(videoType.equals("Type_VideoUrl")){
								rowData.put("videoFileLink", jobject.getString("videoFileLink"));
							}else{
								rowData.put("videoFileLink", String.valueOf(PropertiesUtilWithEnc.prop.getProperty("domainCall"))+jobject.getString("videoFileLink"));
							}
							if(videoType.equals("Type_VideoFileLink")||videoType.equals("Type_VideoUrl")||videoType.equals("Type_VideoFolderLink")){
								if(!videoIDs.contains(jobject.getLong("id"))){
									data.add(rowData);
									videoIDs.add(jobject.getLong("id"));
								}
							}
						}
						
						
						
						for(int i = 0;i<data.size();i++){
							Map<String,Object> tempRow = data.get(i);
							tempRow.put("itemIdx", "���"+(i+1));
							data.set(i,tempRow);
						}
						adapter = new MyAdapter(VideoActivity.this, data);
						
				        tv_serverVideoCatList.setAdapter(adapter);
				        if(data.size()>currentPageSize){
				        	tv_serverVideoCatList.setSelection(data.size()-visibleItemCountLastTime-jsonarr.length()+1);
	                	}
				        Toast.makeText( VideoActivity.this, "��ǰ��"+currentPageNo+"ҳ.������"+jsonarr.length()+"��", Toast.LENGTH_SHORT).show();
					} catch (Exception e) {
						videosLoadingHint.setText("�Բ���,�����Ƽ���Ӱʧ��,���Ժ����ԡ�");
					}
                    
                    break;  
                case 404:  
                	videosLoadingHint.setText("�Բ���,�����Ƽ���Ӱʧ��,���Ժ����ԡ�");
                    break;  
                default:
                	videosLoadingHint.setText("�Բ���,�����Ƽ���Ӱʧ��,���Ժ����ԡ�");
                }
                
            }  
        }; 
        
        
        //firsttime loading, resume to last visited page
        if(!append&& data.size()==0){
        	//CacheFileUtil.writeCacheFile(getCacheDir().getAbsolutePath(), "videolistexitpoint.dat", String.valueOf(currentPageNo)+"notakid"+String.valueOf(firstVisibleItemM));
        	String pageInfo = CacheFileUtil.readCacheFile(getCacheDir().getAbsolutePath(), "videolistexitpoint.dat");
        	
        	try{
        		if(pageInfo!=null && pageInfo.contains("notakid")){
        			String[] pageInfos = pageInfo.split("notakid");
        			if(pageInfos!=null && pageInfos.length>=2){
	        			String pageNo = pageInfos[0];
	        			String firstViewItem = pageInfos[1];
		        		Integer pageNoInt = Integer.parseInt(pageNo);
		        		Integer firstViewItemInt = Integer.parseInt(firstViewItem);
		        		if(pageNoInt>1){
		        			currentPageNo = pageNoInt;
		        		}
		        		if(firstViewItemInt>0){
		        			firstVisibleItemID = firstViewItemInt;
//		        			Toast.makeText(VideoActivity.this, "firstVisibleItemID:"+firstVisibleItemID, Toast.LENGTH_LONG).show();
		        		}
        			}
        		}
        	}catch(Exception e){}
        	
        }
        
        if(!append && data.size()>0){
        	VideoActivity.currentPageNo = 1;
        }
        
        
        videosLoadingHint.setVisibility(View.VISIBLE);
        videosLoadingHint.setText("�����Ƽ�ӰƬ��...");
        try{
//        	String url = String.valueOf(PropertiesUtilWithEnc.prop.getProperty("domainCall"))+"/dictionary/filmsInPage/"+String.valueOf(PropertiesUtilWithEnc.prop.get("fixedFilmCatID"))+"/"+currentPageNo+"/"+currentPageSize+"/";
        	int minAge = 18;
        	if(loginUserProxyEmail !=null && loginUserProxyEmail.contains("@")){
        		minAge = 99;
        	}
        	String url = PropertiesUtilWithEnc.getString("domainCall")+"/dictionary/filmsInPage/"+PropertiesUtilWithEnc.getString("fixedFilmCatID")+"/"+currentPageNo+"/"+currentPageSize+"/"+videoLoadingStrategyCode+"/"+minAge+"/";
	        HttpsGetThread thread = new HttpsGetThread(httpHandler,  
	        		url, 200);  
	        thread.start();
	        
        }catch(Exception e){
        	videosLoadingHint.setText("����ʧ��,�뷵���ϼ����´򿪡�");
        }
    }  
    private void initDataBack(boolean append) {  
    	httpHandler = new Handler() { 
    		
    		@Override  
    		public void handleMessage(Message msg) {  
    			super.handleMessage(msg);  
    			String result = (String) msg.obj;  
    			switch (msg.what) {  
    			case 200:  
    				try {
    					JSONArray jsonarr = new JSONArray(result);
    					if(jsonarr.length()<=0){
    						videosLoadingHint.setText("�Ѿ������һҳ,��������ǰ��...");
    						return;
    					}
    					int counterAdded = 0;
    					videosLoadingHint.setVisibility(View.GONE);
    					for(int i = 0 ; i < jsonarr.length(); i++){
    						JSONObject jobject = jsonarr.getJSONObject(i);
    						Map<String,Object> rowData = new HashMap<String,Object>();
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
							}
    						
    						rowData.put("desc", "���Կ�(<font color='gray'>ԭ��:2Ԫ</font> <font color='red'>�ּ�:0.5Ԫ</font>)");
    						if(VideoPlayerActivity.videosFreebies.contains(String.valueOf(jobject.getLong("id")))){
    							rowData.put("desc", "��ѿ�");
    						}
    						if(VideoPlayerActivity.videosBought.contains(String.valueOf(jobject.getLong("id")))){
    							rowData.put("desc", "�ѹ���");
    						}
    						if(membershipExpiryDate!=null){
								rowData.put("desc", "�����ǻ�Ա:ȫ�����");
							}
    						if(VideoPlayerActivity.videosLuckyShakes.contains(String.valueOf(jobject.getLong("id")))){
    							rowData.put("desc", "����һ��");
    						}
    						
//    						if((jobject.getLong("id")-firstVisibleItemID)==0){
//    							rowData.put("desc",rowData.get("desc")+"\n�ϴο�������");
//    						}
    						
    						if(videoType.equals("Type_VideoUrl")){
    							rowData.put("videoFileLink", jobject.getString("videoFileLink"));
    						}else{
    							rowData.put("videoFileLink", String.valueOf(PropertiesUtilWithEnc.prop.getProperty("domainCall"))+jobject.getString("videoFileLink"));
    						}
    						if(videoType.equals("Type_VideoFileLink")||videoType.equals("Type_VideoUrl")||videoType.equals("Type_VideoFolderLink")){
    							if(!videoIDs.contains(jobject.getLong("id"))){
    								data.add(counterAdded,rowData);
    								videoIDs.add(jobject.getLong("id"));
    								counterAdded = counterAdded + 1;
    							}
    						}
    					}
    					
    					List<Map<String,Object>> dataMerged = new ArrayList<Map<String,Object>>();
    					for(int i = 0;i<data.size();i++){
    						Map<String,Object> tempRow = data.get(i);
    						tempRow.put("itemIdx", "���"+(i+1));
    						dataMerged.add(tempRow);
    					}
    					data.clear();
    					data.addAll(dataMerged);
    					
    					adapter = new MyAdapter(VideoActivity.this, data);
    					
    					tv_serverVideoCatList.setAdapter(adapter);
    					if(data.size()>currentPageSize){
    						int lv = jsonarr.length()-1-visibleItemCountLastTime;
    						if(lv<0){
    							lv = 0;
    						}
    						tv_serverVideoCatList.setSelection(lv);
    					}
    					Toast.makeText( VideoActivity.this, "��ǰ��"+currentPageNo+"ҳ.������"+jsonarr.length()+"��", Toast.LENGTH_SHORT).show();
    				} catch (Exception e) {
    					videosLoadingHint.setText("�Բ���,�����Ƽ���Ӱʧ��,���Ժ����ԡ�");
    				}
    				
    				break;  
    			case 404:  
    				videosLoadingHint.setText("�Բ���,�����Ƽ���Ӱʧ��,���Ժ����ԡ�");
    				break;  
    			default:
    				videosLoadingHint.setText("�Բ���,�����Ƽ���Ӱʧ��,���Ժ����ԡ�");
    			}
    			
    		}  
    	}; 
    	
    	if(currentPageNo<0){
    		return;
    	}
    	videosLoadingHint.setVisibility(View.VISIBLE);
    	videosLoadingHint.setText("�����Ƽ�ӰƬ��...");
    	try{
    		int minAge = 18;
        	if(loginUserProxyEmail !=null && loginUserProxyEmail.contains("@")){
        		minAge = 99;
        	}
    		String url = PropertiesUtilWithEnc.getString("domainCall")+"/dictionary/filmsInPage/"+PropertiesUtilWithEnc.getString("fixedFilmCatID")+"/"+currentPageNo+"/"+currentPageSize+"/"+videoLoadingStrategyCode+"/"+minAge+"/";
    		HttpsGetThread thread = new HttpsGetThread(httpHandler,  
    				url, 200);  
    		thread.start();
    		
    	}catch(Exception e){
    		videosLoadingHint.setText("����ʧ��,�뷵���ϼ����´򿪡�");
    	}
    }  
    
    @Override
    public void onBackPressed() {
    	CacheFileUtil.writeCacheFile(getCacheDir().getAbsolutePath(), "videolistexitpoint.dat", String.valueOf(currentPageNoWithFirstVisibleItem)+"notakid"+String.valueOf(firstVisibleItemID));
    	currentPageNo = 1;
    	currentPageSize = 8;
    	visibleItemCountLastTime = 0;
//    	firstVisibleItemID = 0;
//    	currentPageNoWithFirstVisibleItem = 0;
    	timerCount = 3;
    	data.clear();
    	videoIDs.clear();
    	super.onBackPressed();
    }
    
    @Override
	protected void onPause() {
		CacheFileUtil.writeCacheFile(getCacheDir().getAbsolutePath(), "videolistexitpoint.dat", String.valueOf(currentPageNoWithFirstVisibleItem)+"notakid"+String.valueOf(firstVisibleItemID));
		super.onPause();
	};
    
}
