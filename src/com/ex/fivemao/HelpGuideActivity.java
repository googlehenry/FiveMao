package com.ex.fivemao;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.graphics.Rect;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.ex.fivemao.exception.CrashHandler;
import com.ex.fivemao.io.CacheFileUtil;
import com.ex.fivemao.io.PropertiesUtilWithEnc;


public class HelpGuideActivity extends BaseActivity {
	
	WebView webHelpGuide;
	TextView guideLoadingHint;
	TextView guideToBottom;
	ScrollView guideActScrollView;
	
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sub_helpguide);
        webHelpGuide = (WebView)findViewById(R.id.webHelpGuide);
        guideActScrollView = (ScrollView)findViewById(R.id.guideActScrollView);
        guideLoadingHint = (TextView)findViewById(R.id.guideLoadingHint);
        guideToBottom = (TextView)findViewById(R.id.guideToBottom);
        
        webHelpGuide.getSettings().setJavaScriptEnabled(true);  //加上这一行网页为响应式的
        webHelpGuide.setWebViewClient(new WebViewClient(){
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                view.loadUrl(url);
                return true;   //返回true， 立即跳转，返回false,打开网页有延时
            }
            @Override
            public void onPageFinished(WebView view, String url) {
            	super.onPageFinished(view, url);
            	guideLoadingHint.setVisibility(View.GONE);
            	guideToBottom.setVisibility(View.VISIBLE);
            }
        });   
        
        
        webHelpGuide.loadUrl(PropertiesUtilWithEnc.getString("domainCall")+"/resources/ui/pages/wumaohelpguide/wumaoHelpGuide.jsp?");
        
    }

    
	int yourChoice;
	
    private int loadAndSetVideoLoadingStrategy() {
    	String strategy = CacheFileUtil.readCacheFile(getCacheDir().getAbsolutePath(), "videoloadingstrategy.dat");
    	
    	try{
    		if(strategy!=null && strategy.contains("notakid")){
    			String[] pageInfos = strategy.split("notakid");
    			if(pageInfos!=null && pageInfos.length>=2){
        			String code = pageInfos[0];
        			String desc = pageInfos[1];
	        		int videoLoadingStrategyCode  = Integer.parseInt(code);
	        		yourChoice = videoLoadingStrategyCode;
	        		return videoLoadingStrategyCode;
    			}
    		}else{
    			showChooseVideoLoadingStrategyDialog();
    		}
    	}catch(Exception e){
    		CrashHandler.logErrorToFile(e);
    		return 0;
    	}
    	
    	return 0;
	}
    
    
    private void showChooseVideoLoadingStrategyDialog(){
        
        yourChoice = 0;
        AlertDialog.Builder singleChoiceDialog = 
            new AlertDialog.Builder(HelpGuideActivity.this);
        singleChoiceDialog.setTitle("电影排序设置");
        
        int defaultSelection = loadAndSetVideoLoadingStrategy();
        
        // 第二个参数是默认选项，此处设置为0
        singleChoiceDialog.setSingleChoiceItems(VideoActivity.items, defaultSelection, 
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
                	
                    Toast.makeText(HelpGuideActivity.this, 
                    "你选择了" + VideoActivity.items[yourChoice]+",保存成功。", 
                    Toast.LENGTH_SHORT).show();
                    
                    CacheFileUtil.writeCacheFile(getCacheDir().getAbsolutePath(), "videoloadingstrategy.dat",yourChoice+"notakid"+VideoActivity.items[yourChoice]);
                }
            }
        });
        
        singleChoiceDialog.show();
        
    }
    
    
	@Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        //getMenuInflater().inflate(R.menu.main, menu);
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
    	guideToBottom.setVisibility(View.VISIBLE);
    	if(!bottomTextIsShown()){
    		Toast.makeText(this, "您还没有看完,看完指南才可以【返回】。", Toast.LENGTH_SHORT).show();
    	}else{
    		super.onBackPressed();
    	}
    }


	private boolean bottomTextIsShown() {
		Rect scrollBounds = new Rect();
    	guideActScrollView.getHitRect(scrollBounds);
    	if (guideToBottom.getLocalVisibleRect(scrollBounds)) {
    		return true;
    	    // Any portion of the imageView, even a single pixel, is within the visible window
    	} else {
    		return false;
    	    // NONE of the imageView is within the visible window
    	}
	}
   
}
