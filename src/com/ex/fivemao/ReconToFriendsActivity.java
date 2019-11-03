package com.ex.fivemao;

import java.io.File;

import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;


public class ReconToFriendsActivity extends BaseActivity {
	Button saveAsImageAndShare;
	TextView recExtraText;
	
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sub_recontofriend);
        recExtraText = (TextView) findViewById(R.id.recExtraText);
        saveAsImageAndShare = (Button) findViewById(R.id.saveAsImageAndShare);
        saveAsImageAndShare.setText("截屏保存&分享");
        recExtraText.setText("推荐人:"+loginUserEmail+"。\n*加群/注册时需要提供推荐人邮箱以便验证和获得奖励");
        
        saveAsImageAndShare.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				saveAsImageAndShare.setText("长按识别二维码");
				
				File imgFile = screenshotWindow();
				shareImage(imgFile.getAbsolutePath());
				
				saveAsImageAndShare.setText("截屏保存&分享");
			}
		});
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
    
   
}
