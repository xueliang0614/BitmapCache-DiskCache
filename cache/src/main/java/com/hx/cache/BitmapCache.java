package com.hx.cache;

import android.graphics.Bitmap;
import android.support.v4.util.LruCache;

public class BitmapCache {
    private LruCache<String, Bitmap> mMemoryCache;

    public BitmapCache() {
     // 获取到可用内存的最大值，使用内存超出这个值会引起OutOfMemory异常
       int maxMemory = (int) (Runtime.getRuntime().maxMemory() / 1024);  
       // 使用最大可用内存值的1/8作为缓存的大小。  
       int cacheSize = maxMemory / 8;  
       mMemoryCache = new LruCache<String, Bitmap>(cacheSize) {  
           @Override  
           protected int sizeOf(String key, Bitmap bitmap) {  
               // 重写此方法来衡量每张图片的大小，默认返回图片数量。  
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