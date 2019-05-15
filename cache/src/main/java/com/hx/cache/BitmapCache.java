package com.hx.cache;

import android.graphics.Bitmap;
import android.support.v4.util.LruCache;

public class BitmapCache {
    private LruCache<String, Bitmap> mMemoryCache;

    public BitmapCache() {
     // ��ȡ�������ڴ�����ֵ��ʹ���ڴ泬�����ֵ������OutOfMemory�쳣
       int maxMemory = (int) (Runtime.getRuntime().maxMemory() / 1024);  
       // ʹ���������ڴ�ֵ��1/8��Ϊ����Ĵ�С��  
       int cacheSize = maxMemory / 8;  
       mMemoryCache = new LruCache<String, Bitmap>(cacheSize) {  
           @Override  
           protected int sizeOf(String key, Bitmap bitmap) {  
               // ��д�˷���������ÿ��ͼƬ�Ĵ�С��Ĭ�Ϸ���ͼƬ������  
               return bitmap.getByteCount() / 1024;  
           }  
       };
    }

    public void addBitmapToMemoryCache(String key, Bitmap bitmap) {  
       if (getBitmapFromMemCache(key) == null) {  
          mMemoryCache.put(key, bitmap);  
      }  
  }  

   public Bitmap getBitmapFromMemCache(String key) {  
       return mMemoryCache.get(key);  
   } 
}