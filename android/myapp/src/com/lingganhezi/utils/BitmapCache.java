package com.lingganhezi.utils;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.android.volley.toolbox.ImageLoader.ImageCache;
import com.lingganhezi.myapp.AppContext;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v4.util.LruCache;
import android.util.Log;

public class BitmapCache implements ImageCache {
	private final static String TAG = BitmapCache.class.getSimpleName();

	private final static int APP_VERSION = 1;// 这里长期使用同一个缓存，所以这里版本号不变
	private final static String DEFAULT_CACHE_DIR = "Image";
	private final static int DISK_CACHE_SIZE = 1024 * 1024 * 100; // 100MB
	private final static int MEMORY_CACHE_SIZE = 1024 * 1024 * 20; // 20MB

	private static BitmapCache mInstance;
	private DiskLruCache cacheL2;
	private LruCache<String, Bitmap> cacheL1;

	/**
	 * 处理保存用的线程池
	 */
	private ExecutorService mThreadPool = Executors.newSingleThreadExecutor();

	public static BitmapCache getInstance() {
		if (mInstance == null) {
			mInstance = new BitmapCache();
		}
		return mInstance;
	}

	private BitmapCache() {
		cacheL1 = new LruCache<String, Bitmap>(MEMORY_CACHE_SIZE);
		File cacheDir = new File(AppContext.getInstance().getCacheDir(), DEFAULT_CACHE_DIR);
		try {
			cacheL2 = DiskLruCache.open(cacheDir, APP_VERSION, 1, DISK_CACHE_SIZE);
		} catch (IOException e) {
			Log.e(TAG, "openCache", e);
		}
	}

	@Override
	public Bitmap getBitmap(String url) {
		Bitmap b = cacheL1.get(url);
		if (b == null) {
			b = getDataFromCacheL2(url);
		}
		return b;
	}

	@Override
	public void putBitmap(final String url, final Bitmap bitmap) {
		mThreadPool.execute(new Runnable() {
			@Override
			public void run() {
				cacheL1.put(url, bitmap);
				saveDataToCacheL2(url, bitmap);
			}
		});
	}

	/**
	 * 保存的磁盘
	 * 
	 * @param key
	 * @param bitmap
	 */
	private void saveDataToCacheL2(String key, Bitmap bitmap) {
		try {
			String pKey = StringHelper.MD5Encode(key);
			DiskLruCache.Editor editor = cacheL2.edit(pKey);
			if (editor != null) {
				OutputStream outputStream = editor.newOutputStream(0);
				boolean isSuccessfull = true;
				try {
					bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream);
				} catch (Exception e) {
					isSuccessfull = false;
				}
				if (isSuccessfull) {
					editor.commit();
				} else {
					editor.abort();
				}
				cacheL2.flush();
			}
		} catch (Exception e) {
			Log.w(TAG, "saveDataToCacheL2");
		}

	}

	// 从DiskLruCache中读取数据
	private Bitmap getDataFromCacheL2(String key) {
		Bitmap bitmap = null;
		try {
			String pKey = StringHelper.MD5Encode(key);
			// 第二步:依据key获取到其对应的snapshot
			DiskLruCache.Snapshot snapshot = cacheL2.get(pKey);
			if (snapshot != null) {
				// 第三步:从snapshot中获取到InputStream
				InputStream inputStream = snapshot.getInputStream(0);
				bitmap = BitmapFactory.decodeStream(inputStream);
			}
		} catch (Exception e) {
			Log.w(TAG, "getDataFroCacheL2");
		}
		return bitmap;

	}

	/**
	 * 清除磁盘缓存
	 * 
	 * @throws IOException
	 */
	public void cleanCacheL2() throws IOException {
		cacheL2.delete();
	}
}
