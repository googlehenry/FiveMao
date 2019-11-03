package com.ex.fivemao.ui;

import android.app.Activity;
import android.content.Intent;

public class BaseIntent extends Intent{
	public static final String CALLER_NAME_KEY = "CALLER"; 
	
	public void setCallerActivityClassName(Activity activity){
		if(activity!=null){
			this.putExtra(CALLER_NAME_KEY, activity.getClass().getName());
		}
	}
	
	public String getCallerActivityClassName(){
		return this.getStringExtra(CALLER_NAME_KEY);
	}
}
