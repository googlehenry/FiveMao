package com.ex.fivemao.io;

import java.io.File;
import java.io.FileOutputStream;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.provider.MediaStore;

import com.ex.fivemao.exception.CrashHandler;

public class CacheFileUtil {

	public static String getRealFilePath( final Context context, final Uri uri ) {
        if ( null == uri ) return null;
        final String scheme = uri.getScheme();
        String data = null;
        if ( scheme == null )
            data = uri.getPath();
        else if ( ContentResolver.SCHEME_FILE.equals( scheme ) ) {
            data = uri.getPath();
        } else if ( ContentResolver.SCHEME_CONTENT.equals( scheme ) ) {
            Cursor cursor = context.getContentResolver().query( uri, new String[] { MediaStore.Images.ImageColumns.DATA }, null, null, null );
            if ( null != cursor ) {
                if ( cursor.moveToFirst() ) {
                    int index = cursor.getColumnIndex( MediaStore.Images.ImageColumns.DATA );
                    if ( index > -1 ) {
                        data = cursor.getString( index );
                    }
                }
                cursor.close();
            }
        }
        return data;
    }
	
	public static File writeBitmapToFile(String cacheDir,String fileName,Bitmap bitmap){
		try {  
            File file = new File(cacheDir, fileName);  
            
            FileOutputStream out = new FileOutputStream(file);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 90, out);
            out.close();
            
        } catch (Exception e) {  
            CrashHandler.logErrorToFile(e);  
        }  
		return null;
	}
	public static void writeCacheFile(String cacheDir,String fileName,String content) {  
        try {  
            File file = new File(cacheDir, fileName);  
            FileOutputStream fos = new FileOutputStream(file);
            byte[] bytes = content.getBytes();
            fos.write(WithEnc.encodeBuffer(bytes, 0, bytes.length));
            fos.close();  
        } catch (Exception e) {  
            CrashHandler.logErrorToFile(e);  
        }  
    }  
	
	public static void appendCacheFile(String cacheDir,String fileName,String extraContent) {  
        try {  
            File file = new File(cacheDir, fileName);  
            FileOutputStream fos = new FileOutputStream(file,true);
            byte[] bytes = extraContent.getBytes();
            fos.write(WithEnc.encodeBuffer(bytes, 0, bytes.length));
            fos.close();  
        } catch (Exception e) {  
            CrashHandler.logErrorToFile(e);  
        }  
    }  
	
	
	
	public static String readCacheFile(String cacheDir,String fileName) {  
        try {  
            File file = new File(cacheDir, fileName);  
            if(file.exists()){
	            byte[] bytes = WithEnc.withEncFileBytes(file);
	            return new String(bytes);
            }
            return null;
        } catch (Exception e) {  
            CrashHandler.logErrorToFile(e);  
            return null;
        }  
    }  
	
}
