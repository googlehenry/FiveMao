package com.boredream.bdvideoplayer.utils;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

public class NetworkUtils {

    /**
     * 判断是否有网络连接
     */
	
    public static boolean isNetworkConnected(Context context) {
        if (context != null) {
            ConnectivityManager mConnectivityManager = (ConnectivityManager) context
                    .getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo mNetworkInfo = mConnectivityManager.getActiveNetworkInfo();
            if (mNetworkInfo != null) {
                return mNetworkInfo.isAvailable();
            }
        }
        return false;
    }
    public static boolean isNetworkConnected(Context context,NetworkConnectedCallback callback) {
        if (context != null) {
            ConnectivityManager mConnectivityManager = (ConnectivityManager) context
                    .getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo mNetworkInfo = mConnectivityManager.getActiveNetworkInfo();
            if (mNetworkInfo != null) {
            	boolean rs = mNetworkInfo.isAvailable(); 
                if( rs ){
                	callback.onNetworkConnected();
                }
                return rs;
            }
        }
        return false;
    }


    /**
     * 判断WIFI网络是否可用
     */
    public static boolean isWifiConnected(Context context) {
        if (context != null) {
            ConnectivityManager mConnectivityManager = (ConnectivityManager) context
                    .getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo mWiFiNetworkInfo = mConnectivityManager
                    .getNetworkInfo(ConnectivityManager.TYPE_WIFI);
            if (mWiFiNetworkInfo != null) {
                return mWiFiNetworkInfo.isConnected();
            }
        }
        return false;
    }
    public static boolean isWifiConnected(Context context,WifiConnectedCallback callback) {
        if (context != null) {
            ConnectivityManager mConnectivityManager = (ConnectivityManager) context
                    .getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo mWiFiNetworkInfo = mConnectivityManager
                    .getNetworkInfo(ConnectivityManager.TYPE_WIFI);
            if (mWiFiNetworkInfo != null) {
            	boolean rs = mWiFiNetworkInfo.isConnected(); 
                if( rs ){
                	callback.onWifiConected();
                }
                return rs;
            }
        }
        return false;
    }


    /**
     * 判断MOBILE网络是否可用
     */
    public static boolean isMobileConnected(Context context) {
        if (context != null) {
            ConnectivityManager mConnectivityManager = (ConnectivityManager) context
                    .getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo mMobileNetworkInfo = mConnectivityManager
                    .getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
            if (mMobileNetworkInfo != null) {
                return mMobileNetworkInfo.isConnected();
            }
        }
        return false;
    }
    public static boolean isMobileConnected(Context context,MobileConnectedCallback callback) {
    	if (context != null) {
    		ConnectivityManager mConnectivityManager = (ConnectivityManager) context
    				.getSystemService(Context.CONNECTIVITY_SERVICE);
    		NetworkInfo mMobileNetworkInfo = mConnectivityManager
    				.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
    		if (mMobileNetworkInfo != null) {
    			boolean rs = mMobileNetworkInfo.isConnected(); 
                if( rs ){
                	callback.onMobileConnected();
                }
                return rs;
    		}
    	}
    	return false;
    }

   
    	public interface WifiConnectedCallback{
			public void onWifiConected();
    	}
    	public interface MobileConnectedCallback{
			public void onMobileConnected();
    	}
    	public interface NetworkConnectedCallback{
			public void onNetworkConnected();
    	}
}
