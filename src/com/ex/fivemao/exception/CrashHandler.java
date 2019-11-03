package com.ex.fivemao.exception;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Environment;

import com.ex.fivemao.MainActivity;


public class CrashHandler  implements Thread.UncaughtExceptionHandler {
	Thread.UncaughtExceptionHandler mDefaultHandler;
	
	Context context;
	public CrashHandler( Context applicationContext) {
		this.context = applicationContext;
		mDefaultHandler = Thread.getDefaultUncaughtExceptionHandler();  
	}
	
    @Override
    public void uncaughtException(Thread thread, Throwable throwable) {
    	
    	logErrorToFile(throwable);
    	
    	new Thread() {
            @Override
            public void run() {
                Intent intent = new Intent(context, MainActivity.class);
                PendingIntent restartIntent = PendingIntent.getActivity(context, 0, intent, Intent.FLAG_ACTIVITY_NEW_TASK);
                AlarmManager mgr = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
                mgr.set(AlarmManager.RTC, System.currentTimeMillis() + 1000, restartIntent);
//                android.os.Process.killProcess(android.os.Process.myPid());
                //System.exit(2);
            }
        }.start();
        
//    	if(throwable instanceof IllegalStateException){
//    		mDefaultHandler.uncaughtException(thread,throwable);
//    	}
    	
    	
        //ÍË³ö³ÌÐò
//        android.os.Process.killProcess(android.os.Process.myPid());
//        System.exit(0);
    }
	public static void logErrorToFile(Throwable throwable) {
		File fPrefered = new File(Environment.getExternalStorageDirectory().getAbsolutePath()+"/fivemaolog/error.txt");
    	
    	File folder = fPrefered.getParentFile();

    	if(!folder.exists()){
    		folder.mkdirs();
    	}
    	
    	StringWriter sw = new StringWriter();    
    	PrintWriter pw = new PrintWriter(sw);    
    	throwable.printStackTrace(pw);    
    	String msg=sw.toString();  
    	try {
			BufferedWriter fw = new BufferedWriter(new FileWriter(fPrefered,true));
			fw.write(msg);
			fw.close();
		} catch (IOException e) {
			CrashHandler.logErrorToFile(e);
		}
	}
	public static void logInfoToFile(Throwable throwable) {
		File fPrefered = new File(Environment.getExternalStorageDirectory().getAbsolutePath()+"/fivemaolog/info.txt");
		
		File folder = fPrefered.getParentFile();
		
		if(!folder.exists()){
			folder.mkdirs();
		}
		
		StringWriter sw = new StringWriter();    
		PrintWriter pw = new PrintWriter(sw);    
		throwable.printStackTrace(pw);    
		String msg=sw.toString();  
		try {
			BufferedWriter fw = new BufferedWriter(new FileWriter(fPrefered));
			fw.write(msg);
			fw.close();
		} catch (IOException e) {
			CrashHandler.logErrorToFile(e);
		}
	}

	public static void deleteErrorLogFileIfExits() {
		File fPrefered = new File(Environment.getExternalStorageDirectory().getAbsolutePath()+"/fivemaolog/error.txt");
		if(fPrefered.exists()){
			fPrefered.delete();
		}
	}
}