package com.lingganhezi.myapp.ui.fragment;

import com.android.volley.RequestQueue;
import com.android.volley.toolbox.ImageLoader;
import com.lingganhezi.myapp.AppContext;
import com.lingganhezi.myapp.service.ServiceManager;
import com.lingganhezi.myapp.ui.activity.BaseActivity;
import com.lingganhezi.utils.BitmapCache;

import android.app.Dialog;
import android.os.Bundle;
import android.support.v4.app.Fragment;

public class BaseFragment extends Fragment {

	protected BaseActivity mBaseActivity;
	protected Dialog mDialog;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mBaseActivity = (BaseActivity) this.getActivity();
	}

	protected void showAlertDialog(String msg) {
		mBaseActivity.showAlertDialog(msg);
	}

	protected void showProgressDialog(String msg) {
		mBaseActivity.showProgressDialog(msg);
	}

	/**
	 * 显示对话框
	 * 
	 * @param login
	 */
	protected void showDialog(String msg) {
		mBaseActivity.showDialog(msg);
	}

	protected void dismissDialog() {
		mBaseActivity.dismissDialog();
	}

	public ServiceManager getServiceManager() {
		return mBaseActivity.getServiceManager();
	}

	/**
	 * 获取 HttpRequestQueue
	 * 
	 * @return
	 */
	public RequestQueue getHttpRequestQueue() {
		return AppContext.getInstance().getRequestQueue();
	}

	private ImageLoader mImageLoader;

	/**
	 * 获取 在 当前 Fragment 范围内的ImageLoader 注意 这里会创建 一个HttpRequestQueue 在这里处理消息
	 * 
	 * @return
	 */
	public ImageLoader getImageLoder() {
		if (mImageLoader == null) {
			mImageLoader = new ImageLoader(getHttpRequestQueue(), BitmapCache.getInstance());
		}
		return mImageLoader;
	}

	/**
	 * 获取 在 当前 Actvity 范围内的ImageLoader 注意 这里会创建 一个Acitivyt 范围 HttpRequestQueue
	 * 在这里处理消息
	 * 
	 * @return
	 */
	public ImageLoader getActivityImageLoder() {
		return mBaseActivity.getImageLoder();
	}
}
