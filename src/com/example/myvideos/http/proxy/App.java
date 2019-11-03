package com.example.myvideos.http.proxy;

import android.app.Application;
import android.content.Context;

import com.danikula.videocache.MyHttpProxyCacheServer;
public class App extends Application {

    private static MyHttpProxyCacheServer proxy;

    public static MyHttpProxyCacheServer getProxy(Context context) {
        return App.proxy == null ? (App.proxy = App.newProxy(context)) : App.proxy;
    }

    private static MyHttpProxyCacheServer newProxy(Context context) {
        return new MyHttpProxyCacheServer.Builder(context)
        		.maxCacheFilesCount(200)
                .cacheDirectory(Utils.getVideoCacheDir(context))
                .build();
    }
}