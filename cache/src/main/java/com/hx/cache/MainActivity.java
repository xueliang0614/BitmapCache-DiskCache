package com.hx.cache;

import android.app.Activity;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.widget.CardView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.FrameLayout;
import android.widget.GridView;
import android.widget.ImageView;

import com.hx.cache.BitmapDiskCache.DownloadListener;

import java.util.HashSet;
import java.util.Set;

public class MainActivity extends Activity {
	private GridView gridview;
	private BitmapCache bitmapCache;
	private BitmapDiskCache bitmapDiskCache;
	private Set<BitmapDownloadTask> taskCollection;
	private CardView cardView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        bitmapCache = new BitmapCache();
        bitmapDiskCache = new BitmapDiskCache(this);
        taskCollection = new HashSet<BitmapDownloadTask>();
        gridview = (GridView) findViewById(R.id.gridview);
        gridview.setAdapter(new Adapter());
    }
    
	@Override
	protected void onPause() {
		super.onPause();
		bitmapDiskCache.flush();
	}
	
    @Override
    protected void onDestroy() {
        super.onDestroy();
        bitmapDiskCache.close();
    }
    
    
    public class Adapter extends BaseAdapter {

		@Override
		public int getCount() {
			return Images.imageUrls.length;
		}

		@Override
		public String getItem(int position) {
			return Images.imageUrls[position];
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			ViewHolder viewHolder = null;
			if(convertView == null) {
				viewHolder = new ViewHolder();
				convertView = LayoutInflater.from(MainActivity.this).inflate(R.layout.image_item, null);
				viewHolder.image = (ImageView) convertView.findViewById(R.id.img);		
				convertView.setTag(viewHolder);
			} else {
				viewHolder = (ViewHolder)convertView.getTag();
			}

			FrameLayout.LayoutParams pra = (FrameLayout.LayoutParams) viewHolder.image.getLayoutParams();
			pra.height = (gridview.getWidth() - 2*dip2px(2)) / 3;
			viewHolder.image.setLayoutParams(pra);
			
			String imageUrl = getItem(position);
			//��ÿ��ImageView����Ψһ��tag����ֹͼƬ���ش���
			viewHolder.image.setTag(imageUrl);
			viewHolder.image.setImageResource(R.drawable.error);			
			loadBitmaps(viewHolder.image, imageUrl);
			return convertView;
		}
		
		public class ViewHolder {
			public ImageView image;
		}
    	
    }   
    
    public void loadBitmaps(ImageView imageView, String imageUrl) {
		try {
			Bitmap bitmap = bitmapCache.getBitmapFromMemCache(imageUrl);
			if (bitmap == null) {
				BitmapDownloadTask task = new BitmapDownloadTask();
				taskCollection.add(task);
				task.execute(imageUrl);
			} else {
				if (imageView != null && bitmap != null) {
					imageView.setImageBitmap(bitmap);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}   
    
	public int dip2px(float dipValue) {
		final float scale = getResources().getDisplayMetrics().density;
		return (int) (dipValue * scale + 0.5f);
	}
	
	public void showlog(String info) {
		System.out.print("LRU " + info + "\n");
	}
	
	public void cancelAllTasks() {
		if (taskCollection != null) {
			for (BitmapDownloadTask task : taskCollection) {
				task.cancel(false);
			}
		}
	}
	
	class BitmapDownloadTask extends AsyncTask<String, Void, Bitmap> {
		private String imageUrl;

		@Override
		protected Bitmap doInBackground(String... params) {
			imageUrl = params[0];
			Bitmap bitmap = bitmapDiskCache.getBitmapFromDiskCache(imageUrl);
			if (bitmap != null) {
				bitmapCache.addBitmapToMemoryCache(imageUrl, bitmap);
				return bitmap;
			} else {
				bitmapDiskCache.downloadBitmapToDiskCache(imageUrl, new DownloadListener() {
	    			@Override
	    			public void downloadSuccess(Bitmap bitmap) {
	    				bitmapCache.addBitmapToMemoryCache(imageUrl, bitmap);	    				
	    			}
	    			@Override
	    			public void downloadFail() {
	    			}		
	        	});
				return bitmapDiskCache.getBitmapFromDiskCache(imageUrl);
			}
		}

		@Override
		protected void onPostExecute(Bitmap bitmap) {
			super.onPostExecute(bitmap);
			ImageView imageView = (ImageView) gridview.findViewWithTag(imageUrl);
			//ͨ��Tag�ҵ���Ӧ��ImageView�����ImageView������Ļ���򷵻�null
			if (imageView != null && bitmap != null) {
				imageView.setImageBitmap(bitmap);
			}
			taskCollection.remove(this);
		}
	}   
}
