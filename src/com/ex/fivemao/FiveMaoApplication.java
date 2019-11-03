package com.ex.fivemao;

import android.app.Application;

import com.ex.fivemao.exception.CrashHandler;

public class FiveMaoApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        Thread.setDefaultUncaughtExceptionHandler(new CrashHandler(getApplicationContext()));
    }
}