package com.hx.cache;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Environment;
import android.util.Log;

import com.hx.cache.libcore.io.DiskLruCache;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class BitmapDiskCache {
	private DiskLruCache mDiskLruCache = null;
	private String TAG;

	public BitmapDiskCache(Context context) {
		try {
			File cacheDir = getDiskCacheDir(context, "bitmap");
			if (!cacheDir.exists()) {
				cacheDir.mkdirs();
			}
			mDiskLruCache = DiskLruCache.open(cacheDir, getAppVersion(context), 1, 10 * 1024 * 1024);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public File getDiskCacheDir(Context context, String uniqueName) {  
	    String cachePath;  
	    if (Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState()) || !Environment.isExternalStorageRemovable()) {  
	        cachePath = context.getExternalCacheDir().getPath();  
	    } else {  
	        cachePath = context.getCacheDir().getPath();
			Log.d(TAG, "getDiskCacheDir: -----------------"+cachePath);
		}
		
		
	    return new File(cachePath + File.separator + uniqueName);  
	} 
	
	public int getAppVersion(Context context) {  
	    try {  
	        PackageInfo info = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);  
	        return info.versionCode;  
	    } catch (NameNotFoundException e) {  
	        e.printStackTrace();  
	    }  
	    return 1;  
	}

	//��Ϊ�漰���أ���Ҫ���߳���ִ�д˷���
	public void downloadBitmapToDiskCache(String imageUrl, DownloadListener mDownloadListener) {	  
    	try {
    		downloadListener = mDownloadListener;
            String key = getMD5String(imageUrl);  
            DiskLruCache.Editor editor = mDiskLruCache.edit(key);  
            if (editor != null) {  
                OutputStream outputStream = editor.newOutputStream(0);
                if (downloadUrlToStream(imageUrl, outputStream)) {  
                    editor.commit();
                    if (downloadListener != null) {
                    	Bitmap bmp = getBitmapFromDiskCache(imageUrl);
                    	downloadListener.downloadSuccess(bmp);
                    }
                } else {  
                    editor.abort();
                    if (downloadListener != null) {
                    	downloadListener.downloadFail();
                    }
                }  
            }  
//          mDiskLruCache.flush(); 
        } catch (IOException e) {
            e.printStackTrace();  
        }
	}
	
	public String getMD5String(String key) {  
	    String cacheKey;  
	    try {  
	        final MessageDigest mDigest = MessageDigest.getInstance("MD5");  
	        mDigest.update(key.getBytes());  
	        cacheKey = bytesToHexString(mDigest.digest());  
	    } catch (NoSuchAlgorithmException e) {  
	        cacheKey = String.valueOf(key.hashCode());  
	    }  
	    return cacheKey;  
	}  
	  
	private String bytesToHexString(byte[] bytes) {  
	    StringBuilder sb = new StringBuilder();  
	    for (int i = 0; i < bytes.length; i++) {  
	        String hex = Integer.toHexString(0xFF & bytes[i]);  
	        if (hex.length() == 1) {  
	            sb.append('0');  
	        }  
	        sb.append(hex);  
	    }  
	    return sb.toString();  
	}
	
	private boolean downloadUrlToStream(String urlString, OutputStream outputStream) {  
	    HttpURLConnection urlConnection = null;  
	    BufferedOutputStream out = null;  
	    BufferedInputStream in = null;  
	    try {  
	        final URL url = new URL(urlString);  
	        urlConnection = (HttpURLConnection) url.openConnection();  
	        in = new BufferedInputStream(urlConnection.getInputStream());  
	        out = new BufferedOutputStream(outputStream);  
	        int b;  
	        while ((b = in.read()) != -1) {  
	            out.write(b);  
	        }  
	        return true;  
	    } catch (final IOException e) {  
	        e.printStackTrace();  
	    } finally {  
	        if (urlConnection != null) {  
	            urlConnection.disconnect();  
	        }  
	        try {  
	            if (out != null) {  
	                out.close();  
	            }  
	            if (in != null) {  
	                in.close();  
	            }  
	        } catch (final IOException e) {  
	            e.printStackTrace();  
	        }  
	    }  
	    return false;  
	} 

	public Bitmap getBitmapFromDiskCache(String imageUrl) {
		try {
		    String key = getMD5String(imageUrl); 
		    DiskLruCache.Snapshot snapShot = mDiskLruCache.get(key);  
		    if (snapShot != null) {  
		        InputStream is = snapShot.getInputStream(0);  
		        Bitmap bitmap = BitmapFactory.decodeStream(is);  
                return bitmap;
		    }  
		} catch (IOException e) {  
		    e.printStackTrace();  
		}
		return null;
	}
	
	public boolean removeBitmapFromDiskCache(String imageUrl) {
		try {
		    String key = getMD5String(imageUrl); 
		    return mDiskLruCache.remove(key);
		} catch (IOException e) {  
		    e.printStackTrace();  
		}
		return false;
	}
	
	public void removeAll() {
        try {
			mDiskLruCache.delete();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public void getAllSize() {
        mDiskLruCache.size();
	}
	
	public void flush() {
        try {
			mDiskLruCache.flush();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public void close() {
		try {
			mDiskLruCache.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public DownloadListener downloadListener;
	public interface DownloadListener {
		public void downloadSuccess(Bitmap bitmap);
		public void downloadFail();
	} 
	
}