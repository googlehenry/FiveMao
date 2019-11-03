package com.example.myvideos.http.proxy;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.TreeMap;

import android.content.Context;

public class Utils {

    public static File getVideoCacheDir(Context context) {
        return new File(context.getExternalCacheDir(), "video-cache");
    }

    public static int cleanVideoCacheDir(Context context) throws IOException {
        File videoCacheDir = getVideoCacheDir(context);
        return cleanDirectory(videoCacheDir);
    }
    
    public static File[] listVideoCachesInDir(Context context) throws IOException {
        File videoCacheDir = getVideoCacheDir(context);
        
        if (!videoCacheDir.exists()) {
            return null;
        }
        File[] contentFiles = videoCacheDir.listFiles();
        
        return contentFiles;
    }

    private static int cleanDirectory(File file) throws IOException {
    	int count = 0;
        if (!file.exists()) {
            return count;
        }
        File[] contentFiles = file.listFiles();
        if (contentFiles != null) {
            for (File contentFile : contentFiles) {
                delete(contentFile);
            }
        }
        count = contentFiles.length;
        return count;
    }

    private static void delete(File file) throws IOException {
        if (file.isFile() && file.exists()) {
            deleteOrThrow(file);
        } else {
            cleanDirectory(file);
            deleteOrThrow(file);
        }
    }

    private static void deleteOrThrow(File file) throws IOException {
        if (file.exists()) {
            boolean isDeleted = file.delete();
            if (!isDeleted) {
                throw new IOException(String.format("File %s can't be deleted", file.getAbsolutePath()));
            }
        }
    }

	public static int cleanVideoCacheDir(Context context, FilenameFilter filenameFilter) {
		File[] files = listVideoCachesInDir(context,filenameFilter);
		int count = 0;
		if(files!=null && files.length>0){
			for(File file:files){
				file.delete();
			}
			count = files.length;
		}
		return count;
	}

	private static File[] listVideoCachesInDir(Context context, FilenameFilter filenameFilter) {
		File videoCacheDir = getVideoCacheDir(context);
        if (!videoCacheDir.exists()) {
            return null;
        }
        File[] contentFiles = videoCacheDir.listFiles(filenameFilter);
        return contentFiles;
	}

	public static List<File> listTop1ScreenshotsIfAvailable(
			Context context, File folder) {
		
			List<File> results = new ArrayList<File>();
		
			FilenameFilter filenameFilter = new FilenameFilter() {
				
				@Override
				public boolean accept(File dir, String filename) {
					String filenameLowercase = filename.toLowerCase();
					if(filenameLowercase.endsWith(".png")||filenameLowercase.endsWith(".jpg")||filenameLowercase.endsWith(".jpeg")){
						File f = new File(dir.getAbsolutePath()+"/"+filename);
						long lastModified = f.lastModified();
						long timeDiff = System.currentTimeMillis() - lastModified;
						if(timeDiff/1000<60){
							return true;
						}
					}
					return false;
				}
			};
			
			File dir = folder;
	        if (dir.exists()) {
	        	TreeMap<Long,File> sortByDateAsc = new TreeMap<Long,File>();
	        	File[] contentFiles = dir.listFiles(filenameFilter);
	        	if(contentFiles!=null && contentFiles.length>0){
	        		for(File cf:contentFiles){
	        			sortByDateAsc.put(cf.lastModified(), cf);
	        		}
	        	}
	        	results.addAll(sortByDateAsc.values());
	        }
	        
	        return results;
		
	}
}