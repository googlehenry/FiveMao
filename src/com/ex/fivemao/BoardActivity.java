package com.ex.fivemao;

import java.io.UnsupportedEncodingException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.json.JSONArray;
import org.json.JSONObject;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
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
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.ex.fivemao.exception.CrashHandler;
import com.ex.fivemao.io.PropertiesUtilWithEnc;
import com.ex.fivemao.ui.MyBoardMsgAdapter;
import com.example.myvideos.http.HttpsGetThread;
import com.example.myvideos.http.MultipartFormHttpsPostThread;


public class BoardActivity extends BaseActivity {
	public static Integer currentPageNo = 1;
	public static Integer currentPageSize = 10;
	private static Integer visibleItemCountLastTime = 0;
	public static String badwords = "cunt,prick,wanker,arsehole,asshole,tosser,berk,wally,git,shithead,pillock,bastard,eejit,jerk,penis,wacko,horseshit,nutter,prat,dork,creep,drip,twit,geezer,hooker,suker,slapper,bugger,bollocks,bitch,cretin,motherfucker,fuck,操,草,逼,色情,情色,毛片,AV,av,日,妈,干,屌,鸡巴";
	private static String lastTimeMsg;
	
	private Handler httpHandler;  
	private ListView messagesList;
	TextView messagesLoadingHint;
	EditText messageTextBox;
	Button btnSend;
	protected ListAdapter adapter;
	
//	private View mFooterParentXml; 
	
	public static List<Map<String,Object>> data = new ArrayList<Map<String,Object>>();
	private static Set<Long> msgIDs = new HashSet<Long>();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_board);
        messagesList = (ListView) findViewById(R.id.siteMessages);
        messagesLoadingHint = (TextView) findViewById(R.id.addingMessagesLoadingHint);
        messageTextBox = (EditText) findViewById(R.id.messageTextBox);
        btnSend = (Button) findViewById(R.id.btnSend);
        
        btnSend.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				
				String message = messageTextBox.getText().toString().trim();
				if(message.length()>1){
					try {
						for(String b:badwords.split(",")){
							if(message.contains(b)){
								message=message.replace(b, "*");
							}
						}
						message = "[手机留言]"+message;
						
						if(lastTimeMsg!=null && lastTimeMsg.equals(message)){
							Toast.makeText(BoardActivity.this, "请不要重复发送", Toast.LENGTH_SHORT).show();
						}else{
							sendSuggestionRequest(message);
							lastTimeMsg = message;
							messageTextBox.setText("");
						}
					} catch (UnsupportedEncodingException e) {
						CrashHandler.logErrorToFile(e);
					}
				}else{
					Toast.makeText(BoardActivity.this, "留言太短请重新输入", Toast.LENGTH_SHORT).show();
					
				}
			}
			
		});
        
        
//        mFooterParentXml = LayoutInflater.from(this).inflate(R.layout.layout_footer_listview, null);//加载footerParent布局
//        View parentFooter = mFooterParentXml.findViewById(R.id.mFooterparent);
//
//        parentFooter.setFocusable(false);
//        parentFooter.setClickable(false);
//        parentFooter.setOnClickListener(new OnClickListener() {
//			@Override
//			public void onClick(View v) {
//				
//			}
//		});
//        messagesList.addFooterView(parentFooter);
        
        initData(false);
        
        messagesList.setOnScrollListener(new OnScrollListener() {
        	private int totalItem;  
            private int lastItem;  
              
            
			@Override
			public void onScrollStateChanged(AbsListView view, int scrollState) {
				if(this.totalItem == lastItem&&scrollState == SCROLL_STATE_IDLE){  
		            if(!messagesLoadingHint.isShown()){  
		            	BoardActivity.currentPageNo = BoardActivity.currentPageNo + 1;  
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
        
        messagesList.setOnItemClickListener(new OnItemClickListener(){

			@Override
			public void onItemClick(AdapterView<?> adapterView, View view, int arg2,
					long arg3) {
				final Map<String,Object> line = data.get(arg2);
				boolean processed = false;
				String text = String.valueOf(line.get("content"));
				if(text!=null && text.contains("《") && text.contains("》")){
					int start =text.lastIndexOf("《")+1;
					int end = text.lastIndexOf("》");
					if((end-start)>1){
						processed = true;
						final String filmName = text.substring(start,end);
						
						AlertDialog.Builder builder = new AlertDialog.Builder(BoardActivity.this);  
				        builder.setIcon(android.R.drawable.ic_dialog_info);  
				        builder.setTitle("提示:发现电影《"+filmName+"》，马上去搜索观看？");  
				        builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {  
				            public void onClick(DialogInterface dialog, int which) {  
								Intent intentSearch=new Intent();
								intentSearch.putExtra("searchKey", filmName);
								intentSearch.setClass(BoardActivity.this, SearchActivity.class);
								startActivity(intentSearch);  
				            }  
				        });  
				        builder.setOnCancelListener(new OnCancelListener() {
							
							@Override
							public void onCancel(DialogInterface dialog) {
								gotoMessageDetailActivity(line);  
							}
						});
				        builder.show();  
					}
					
				}else if(loginUserName!=null && text!=null && text.contains("【") && text.contains("】")){
					int start =text.lastIndexOf("【")+1;
					int end = text.lastIndexOf("】");
					if((end-start)>1){
						processed = true;
						String filmName = text.substring(start,end);
						
						AlertDialog.Builder builder = new AlertDialog.Builder(BoardActivity.this);  
				        builder.setIcon(android.R.drawable.ic_dialog_info);  
				        builder.setTitle("也购买"+filmName+"试试？");
				        builder.setMessage("会员在有效期内全场免费观看,越长越实惠：一日会员(1.0元/1天)，一月会员(8.0元/31天)，三月会员(18.0元/93天)。");
				        builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {  
				            public void onClick(DialogInterface dialog, int which) {  
				            	Intent intent=new Intent();
				        		intent.setClass(BoardActivity.this, MeBoughtMembershipActivity.class);  
				        		startActivity(intent);
				            }  
				        });  
				        builder.setOnCancelListener(new OnCancelListener() {
							
							@Override
							public void onCancel(DialogInterface dialog) {
								gotoMessageDetailActivity(line);  
							}
						});
				        builder.show();  
					}
					
				}
				//if(itemContent.contains("【") && itemContent.contains("】")){
				
				
				if(!processed){
					gotoMessageDetailActivity(line);
				}
			}

			private void gotoMessageDetailActivity(Map<String, Object> line) {
				Intent intent=new Intent();
				intent.putExtra("id", String.valueOf(line.get("id")));
				intent.putExtra("createdBy", String.valueOf(line.get("createdBy")));
				intent.putExtra("createdTime", String.valueOf(line.get("createdTime")));
				intent.putExtra("content", String.valueOf(line.get("content")));
				intent.putExtra("itemIdx", String.valueOf(line.get("itemIdx")));
	            //setClass函数的第一个参数是一个Context对象  
	            //Context是一个类,Activity是Context类的子类,也就是说,所有的Activity对象都可以向上转型为Context对象  
	            //setClass函数的第二个参数是Class对象,在当前场景下,应该传入需要被启动的Activity的class对象  
	            intent.setClass(BoardActivity.this, BoardMsgDescActivity.class);  

	            
	            //Toast.makeText( SubActivityVideoList.this, "ok,clicked "+arg2+","+line.get("id"), Toast.LENGTH_SHORT).show();
	            startActivity(intent);
			}
        	
        });
        
        if(loginUserName==null){
        	btnSend.setFocusable(false);
        	btnSend.setClickable(false);
        	btnSend.setText("未登录");
        }else{
        	btnSend.setFocusable(true);
        	btnSend.setClickable(true);
        	btnSend.setText("发送");
        }
    }
    
    

    private void initData(final boolean append) {  
//    	pairs.put("keywords", "[手机留言],[求片]");
    	
        httpHandler = new Handler() { 
        	
            @Override  
            public void handleMessage(Message msg) {  
                super.handleMessage(msg);  
                String result = (String) msg.obj;
                
                switch (msg.what) {  
                case 200:
                	
                    try {
						JSONArray jsonarr = new JSONArray(result);
						if(jsonarr.length()<=0 ){
							messagesLoadingHint.setText("已没有留言,请稍后再试。");
//							mFooterParentXml.setVisibility(View.GONE);
							return;
						}
						messagesLoadingHint.setVisibility(View.GONE);
						int oldPosition = data.size();
						for(int i = 0 ; i < jsonarr.length(); i++){
							JSONObject jobject = jsonarr.getJSONObject(i);
							Map<String,Object> rowData = new HashMap<String,Object>();
							rowData.put("id", jobject.getLong("id"));
							rowData.put("createdBy", jobject.getString("createdBy"));
							rowData.put("createdTime", jobject.getString("createdTime"));
							rowData.put("content", jobject.getString("content"));
							
							String filmName = null;
							String text = String.valueOf(jobject.getString("content"));
							if(text!=null && text.contains("《") && text.contains("》")){
								int start =text.lastIndexOf("《");
								int end = text.lastIndexOf("》")+1;
								if((end-start)>1){
									filmName = text.substring(start, end);
								}
							}
							if(filmName!=null){
								rowData.put("content", jobject.getString("content")+"......");
							}
							
							rowData.put("itemIdx", "序号"+(i+1));
							if(!msgIDs.contains(jobject.getLong("id"))){
								msgIDs.add(jobject.getLong("id"));
								data.add(rowData);
							}
						}
						
						List<Map<String,Object>> dataMerge = new ArrayList<Map<String,Object>>();
						
						for(int i = 0;i<data.size();i++){
							Map<String,Object> tempRow = data.get(i);
							tempRow.put("itemIdx", "序号"+(i+1));
							dataMerge.add(tempRow);
						}
						
						adapter = new MyBoardMsgAdapter(BoardActivity.this,dataMerge);
//						adapter=new SimpleAdapter(BoardActivity.this, dataMerge,  R.layout.items_messages, 
//								new  String[]{"createdTime","content"}, 
//								new  int[]{R.id.itemCreatedTime,R.id.itemContent});
						messagesList.setAdapter(adapter);
						
						if(data.size()>currentPageSize){
							messagesList.setSelection(oldPosition-visibleItemCountLastTime+1);
	                		Toast.makeText( BoardActivity.this, "又加载了"+jsonarr.length()+"条留言"+(data.size()+1-currentPageSize)+"-"+(data.size())+"号", Toast.LENGTH_SHORT).show();
	                	}
						
						if(!append && jsonarr.length()<currentPageSize){
							messagesLoadingHint.setText("已没有留言,请稍后再试。");
							messagesLoadingHint.setVisibility(View.VISIBLE);
						}
						
					} catch (Exception e) {
						messagesLoadingHint.setText("对不起,加载推荐电影失败,请稍后再试。");
					}
                    break;  
                case 404:  
                	messagesLoadingHint.setText("对不起,加载推荐电影失败,请稍后再试。");
                    break;  
                default:
                	messagesLoadingHint.setText("对不起,加载推荐电影失败,请稍后再试。");
                }
                
  
            }  
        }; 
        
        if(!append && data.size()>0){
        	currentPageNo = 1;
        	data.clear();
        	msgIDs.clear();
        }
        
        messagesLoadingHint.setVisibility(View.VISIBLE);
        messagesLoadingHint.setText("稍等,加载留言中...");
        
        HttpsGetThread thread = new HttpsGetThread(httpHandler,  
        		String.valueOf(PropertiesUtilWithEnc.prop.getProperty("domainCall"))+"/itemcomment/getCommentsByKeywords/FiveMaoMessage/"+currentPageNo+"/"+currentPageSize+"/",200);  
        thread.start();
    }  
     
    protected void sendSuggestionRequest(String textContent) throws UnsupportedEncodingException {
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
                			Toast.makeText( BoardActivity.this, "留言已发送["+result+"]", Toast.LENGTH_LONG).show();
                			
                			Map<String,Object> rowData = new HashMap<String,Object>();
							rowData.put("id", 666666+data.size());
							rowData.put("createdBy", loginUserName);
							rowData.put("createdTime", new SimpleDateFormat("yyyy-MM-dd").format(new Date()));
							rowData.put("content", lastTimeMsg);
							rowData.put("itemIdx", "序号0");
							
							int oldPosition = data.size();
							
							List<Map<String,Object>> dataMerge = new ArrayList<Map<String,Object>>();
							dataMerge.add(rowData);
							
							for(int i = 0;i<data.size();i++){
								Map<String,Object> tempRow = data.get(i);
								tempRow.put("itemIdx", "序号"+(i+1));
								dataMerge.add(tempRow);
							}
							
							data.clear();
							Iterator<Map<String,Object>> iter = dataMerge.iterator();
							while(iter.hasNext()){
								data.add(iter.next());
							}
							
							adapter=new SimpleAdapter(BoardActivity.this, dataMerge,  R.layout.items_messages, 
									new  String[]{"createdTime","content"}, 
									new  int[]{R.id.itemCreatedTime,R.id.itemContent});
							messagesList.setAdapter(adapter);
							
							if(data.size()>currentPageSize){
								messagesList.setSelection(oldPosition-visibleItemCountLastTime+1);
		                	}
                		}
						
					} catch (Exception e) {
						CrashHandler.logErrorToFile(e);
					}
                    break;  
                case 404:  
                	Toast.makeText( BoardActivity.this, "留言请求发送失败,后端服务已关闭。", Toast.LENGTH_SHORT).show();
                    break;  
                }  
  
            }  
        },String.valueOf(PropertiesUtilWithEnc.prop.getProperty("domainCall"))+"/itemcomment/addCommentWithType/FiveMaoMessage/",pairs,null);  
        thread.start();
	}
    @Override
    public void onBackPressed() {
//    	currentPageNo = 1;
//    	currentPageSize = 10;
//    	data.clear();
//    	msgIDs.clear();
//    	lastTimeMsg=null;
    	super.onBackPressed();
    }
    
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
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
}
