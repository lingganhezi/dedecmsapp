package com.lingganhezi.myapp.service;

import android.content.ContentResolver;
import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.android.volley.RequestQueue;
import com.android.volley.VolleyError;
import com.android.volley.Response.ErrorListener;
import com.lingganhezi.myapp.AppContext;
import com.lingganhezi.myapp.R;
import com.lingganhezi.myapp.entity.LoginUserInfo;

public class BaseService {
	public final static String MESSAGE_FALG = "message_flag";

	protected Context mContext;
	private ServiceManager mServiceManager;

	// MSG
	public final static int MSG_ERROR = -1;

	protected BaseService(Context context) {
		mContext = context;
		mServiceManager = ServiceManager.getInstance(context);
	}

	/**
	 * 获取http请求队列
	 * 
	 * @return
	 */
	public RequestQueue getHttpRequesttQueue() {
		return AppContext.getInstance().getRequestQueue();
	}

	/**
	 * Http发送访问错误 listenner
	 * 
	 * @param handler
	 * @return
	 */
	protected ErrorListener getErrorListener(final Handler handler) {
		return new ErrorListener() {
			@Override
			public void onErrorResponse(VolleyError error) {
				Log.e("BaseService", "networkerror", error);
				if (handler != null) {
					Message msg = handler.obtainMessage(MSG_ERROR, error);
					handler.sendMessage(msg);
				}
			}
		};
	}

	protected ServiceManager getServiceManager() {
		return mServiceManager;
	}

	protected ContentResolver getContentResolver() {
		return mContext.getContentResolver();
	}
	
	/**
	 * 检查是否登录，
	 * 如果 没有登录就会使用handler发送一条MSG_ERROR，没有登录的消息
	 * @param handler
	 */
	public boolean checkLogin(Handler handler){
		if (!LoginService.getInstance().isLogined()) {
			// 未登陆
			if(handler != null){
				Message msg = handler.obtainMessage(MSG_ERROR);
				msg.getData().putString(MESSAGE_FALG, mContext.getString(R.string.login_not_logined));
				handler.sendMessage(msg);
			}
			return false;
		}else{
			return true;
		}
	}
}
