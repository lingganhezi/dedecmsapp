package com.lingganhezi.net;

import java.io.File;
import java.util.Map;

import org.apache.http.entity.ContentType;

import android.content.Context;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.Response.ErrorListener;
import com.android.volley.Response.Listener;
import com.lingganhezi.myapp.AppContext;

/**
 * 文件上传器
 * 
 * @author chenzipeng
 *
 */
public class FileUploader {
	private final static String TAG = FileUploader.class.getSimpleName();

	public static FileUploader mInstance;

	private Context mContext;

	private RequestQueue getRequestQueue() {
		return AppContext.getInstance().getMulitiRequestQueue();
	}

	private FileUploader(Context context) {
		mContext = context;
	}

	public static FileUploader getInstance(Context context) {
		if (mInstance == null) {
			mInstance = new FileUploader(context);
		}
		return mInstance;
	}

	/**
	 * 上传文件
	 * 
	 * @param url
	 * @param files
	 *            文件map， key 参数名 ，value 文件路径
	 * @param params
	 *            参数
	 * @param responseListener
	 * @param errorListener
	 * @param contentType
	 */
	public void upload(final String url, final Map<String, File> files, final Map<String, String> params,
			final Response.Listener responseListener, final Response.ErrorListener errorListener, final ContentType contentType) {
		if (null == url || null == responseListener) {
			return;
		}

		MultiPartStringRequest multiPartRequest = new FileMultiPartStringRequest(url, responseListener, errorListener) {

			@Override
			public Map<String, File> getFileUploads() {
				return files;
			}

			@Override
			public Map<String, String> getStringUploads() {
				return params;
			}

			@Override
			public ContentType getFileContentType() {
				return contentType;
			}

		};

		// Log.i(TAG, " volley put : uploadFile " + url);

		getRequestQueue().add(multiPartRequest);
	}

	private class FileMultiPartStringRequest extends MultiPartStringRequest {

		private final int TIME_OUT = 2 * 60 * 1000;// 上传超时 2分钟
		private final int RETRY_TIMES_MAX = 0;// 不重试

		public FileMultiPartStringRequest(String url, Listener<String> listener, ErrorListener errorListener) {
			super(Request.Method.POST, url, listener, errorListener);
			setRetryPolicy(new DefaultRetryPolicy(TIME_OUT, RETRY_TIMES_MAX, 1f));
		}

	}
}
