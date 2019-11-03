package com.ex.fivemao;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;


public class BoardMsgDescActivity extends BaseActivity {
	private static String msgId ;
	private static String createdBy ;
	private static String createdTime;
	private static String content;
	private static String itemIdx;
	
	TextView descMsgTitle;
	TextView descMsgContent;
	TextView descMsgOther;
	
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sub_boardmsgdesc);
        descMsgTitle = (TextView)findViewById(R.id.descMsgTitle);
        descMsgContent = (TextView)findViewById(R.id.descMsgContent);
        descMsgOther = (TextView)findViewById(R.id.descMsgOther);
        
        Intent intent = getIntent();
        msgId = intent.getStringExtra("id");
        createdBy = intent.getStringExtra("createdBy");
        createdTime = intent.getStringExtra("createdTime");
        content = intent.getStringExtra("content");
        for(String bad:BoardActivity.badwords.split(",")){
        	if(content.contains(bad)){
        		content = content.replace(bad, "*");
        	}
        }
        itemIdx = intent.getStringExtra("itemIdx");
        
        descMsgTitle.setText(itemIdx);
        descMsgContent.setText(content);
        descMsgOther.setText("--by "+starMyName(createdBy)+" at "+createdTime);
    }



    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
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
