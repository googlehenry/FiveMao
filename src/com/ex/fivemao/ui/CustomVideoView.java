package com.ex.fivemao.ui;


import android.content.Context;
import android.content.res.Configuration;
import android.util.AttributeSet;
import android.widget.VideoView;

public class CustomVideoView extends VideoView {
	
    public CustomVideoView(Context context) {
        super(context);
    }

    public CustomVideoView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public CustomVideoView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }
    

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
    	super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        //如果当前是横屏
    	Configuration config = getResources().getConfiguration();
        if(config.orientation == Configuration.ORIENTATION_LANDSCAPE )
        {
        	// 其实就是在这里做了一些处理。
        	int width = getDefaultSize(0, widthMeasureSpec);
            int height = getDefaultSize(0, heightMeasureSpec);
            setMeasuredDimension(width, height);
        }else if(config.orientation == Configuration.ORIENTATION_PORTRAIT ){
        	int width = getDefaultSize(0, widthMeasureSpec);
        	int pref = (int)(width*0.666);
            int height = pref;
            setMeasuredDimension(width, height);
        }
        
    }
    @Override
    protected void onConfigurationChanged(Configuration newConfig) {
    	super.onConfigurationChanged(newConfig);
    }
}
