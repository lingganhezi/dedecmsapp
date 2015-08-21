package com.lingganhezi.myapp;

import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;
import com.lingganhezi.myapp.entity.LoginUserInfo;
import com.lingganhezi.myapp.service.LoginService;
import com.lingganhezi.net.HurlStack;
import com.lingganhezi.net.MultiPartStack;
import com.lingganhezi.net.RequestQueuePool;

import android.app.Application;
import android.text.TextUtils;

public class AppContext extends Application {
	private final String TAG = AppContext.class.getSimpleName();
	private static AppContext mInstance;
	private RequestQueuePool mRequestQueuePool;
	private RequestQueuePool mMulitiRequestQueuePool;

	@Override
	public void onCreate() {
		super.onCreate();
		mInstance = this;

		// 创建 请求队列池
		createRequestQueuePool();

		//debug
		if(Constant.DEBUG){
			if(TextUtils.isEmpty(ConfigHelper.getInstance().loadLoginUserInfoConfig().getUserId())){
				ConfigHelper.getInstance().saveLoginUserInfoConfig(new LoginUserInfo(Constant.DEBUG_USER_ID, Constant.DEBUG_USER_PWD));
			}
		}
		
		// 创建的时候自动登录
		LoginService.getInstance().autoLogin();
	}

	public static AppContext getInstance() {
		return mInstance;
	}

	/**
	 * 创建 请求队列管理池
	 */
	private void createRequestQueuePool() {
		mRequestQueuePool = new RequestQueuePool();
		// 创建多个 请求队列
		for (int i = 0; i < Constant.REQUESTQUEUE_HTTP_REQUEST_SIZE; i++) {
			RequestQueue queue = Volley.newRequestQueue(getApplicationContext(), new HurlStack());
			queue.start();
			mRequestQueuePool.put(queue);
		}
		mMulitiRequestQueuePool = new RequestQueuePool();
		for (int i = 0; i < Constant.REQUESTQUEUE_HTTP_MULITI_REQUEST_SIZE; i++) {
			RequestQueue queue = Volley.newRequestQueue(getApplicationContext(), new MultiPartStack());
			queue.start();
			mMulitiRequestQueuePool.put(queue);
		}
	}

	/**
	 * 获取一个请求队列管理池中的 RequestQueue
	 * 
	 * @return
	 */
	public RequestQueue getRequestQueue() {
		return getRequestQueue(false);
	}

	/**
	 * 获取一个 RequestQueue
	 * 
	 * @param isNew
	 *            时候新建一个新的RequestQueue， </br>true:新建一个新的RequestQueue,并启用
	 *            。但是不会加入到 请求队列管理池当中 </br>false:获取管理池中的一个 RequestQueue
	 * @return
	 */
	public RequestQueue getRequestQueue(boolean isNew) {
		if (isNew) {
			RequestQueue q = Volley.newRequestQueue(getApplicationContext());
			q.start();
			return q;
		} else {
			return mRequestQueuePool.getFreeRequestQueue();
		}
	}

	/**
	 * 获取一个请求队列管理池中的 RequestQueue
	 * 
	 * @return
	 */
	public RequestQueue getMulitiRequestQueue() {
		return getMulitiRequestQueue(false);
	}

	/**
	 * 获取一个 Muliti 类型的 RequestQueue
	 * 
	 * @param isNew
	 *            时候新建一个新的RequestQueue， </br>true:新建一个新的RequestQueue,并启用
	 *            。但是不会加入到 请求队列管理池当中 </br>false:获取管理池中的一个 RequestQueue
	 * @return
	 */
	public RequestQueue getMulitiRequestQueue(boolean isNew) {
		if (isNew) {
			RequestQueue q = Volley.newRequestQueue(getApplicationContext(), new MultiPartStack());
			q.start();
			return q;
		} else {
			return mMulitiRequestQueuePool.getFreeRequestQueue();
		}
	}
}
