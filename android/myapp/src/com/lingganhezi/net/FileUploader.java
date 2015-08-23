package com.lingganhezi.net;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.util.Map;

import org.apache.http.entity.ContentType;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.NetworkResponse;
import com.android.volley.ParseError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.Response.ErrorListener;
import com.android.volley.Response.Listener;
import com.android.volley.toolbox.HttpHeaderParser;
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

		FileMultiPartResultRequest multiPartRequest = new FileMultiPartResultRequest(url, responseListener, errorListener) {

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

	private class FileMultiPartResultRequest extends CustomMultiPartRequest<JSONObject> {

		private final int TIME_OUT = 2 * 60 * 1000;// 上传超时 2分钟
		private final int RETRY_TIMES_MAX = 0;// 不重试

		public FileMultiPartResultRequest(String url, Listener<JSONObject> listener, ErrorListener errorListener) {
			super(Request.Method.POST, url, listener, errorListener);
			setRetryPolicy(new DefaultRetryPolicy(TIME_OUT, RETRY_TIMES_MAX, 1f));
		}

		@Override
		protected void deliverResponse(Object response) {
			if(mListener !=null){
				mListener.onResponse((JSONObject)response);
			}
		}

		@Override
		protected Response parseNetworkResponse(NetworkResponse response) {
			try {
				String parsed = new String(response.data, HttpHeaderParser.parseCharset(response.headers));
				return Response.success( new JSONObject(parsed), HttpHeaderParser.parseCacheHeaders(response));
			} catch (UnsupportedEncodingException e) {
				return Response.error(new ParseError(e));
			} catch (JSONException je) {
				return Response.error(new ParseError(je));
			}
		}
	}
}
