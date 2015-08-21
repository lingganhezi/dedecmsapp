package com.lingganhezi.myapp.ui.activity;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;

import com.android.volley.RequestQueue;
import com.android.volley.TimeoutError;
import com.android.volley.toolbox.ImageLoader;
import com.lingganhezi.myapp.AppContext;
import com.lingganhezi.myapp.R;
import com.lingganhezi.myapp.service.ServiceManager;
import com.lingganhezi.utils.BitmapCache;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.support.v4.app.FragmentActivity;
import android.widget.Toast;

public class BaseActivity extends FragmentActivity {
	private ServiceManager mServiceManager;
	public Dialog mDialog;

	public void showAlertDialog(String msg) {
		dismissDialog();
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		mDialog = builder.setMessage(msg).create();
		showDialog(msg);
	}

	public void showProgressDialog(String msg) {
		dismissDialog();
		mDialog = new ProgressDialog(this);
		mDialog.setCancelable(false);
		showDialog(msg);
	}

	/**
	 * 显示对话框
	 * 
	 * @param login
	 */
	public void showDialog(String msg) {
		mDialog.setTitle(msg);
		if (!mDialog.isShowing()) {
			mDialog.show();
		}
	}

	public void dismissDialog() {
		if (mDialog != null && mDialog.isShowing()) {
			mDialog.dismiss();
			mDialog = null;
		}
	}

	@Override
	public void onDestroy() {
		dismissDialog();
		super.onDestroy();
	}

	public ServiceManager getServiceManager() {
		if (mServiceManager == null) {
			mServiceManager = ServiceManager.getInstance(this);
		}
		return mServiceManager;
	}

	/**
	 * toast 方式显示异常信息到界面
	 * 
	 * @param e
	 */
	public void showToast(Exception e) {
		if (e instanceof SocketTimeoutException || e instanceof TimeoutError) {
			showToast(R.string.error_networktimeout);
		} else if (e instanceof UnknownHostException) {
			showToast(R.string.error_unknowhost);
		} else if (e instanceof FileNotFoundException) {
			showToast(R.string.error_filenotfound);
		} else if (e instanceof IllegalAccessException) {
			showToast(R.string.error_notaccess);
		} else if (e instanceof IOException) {
			showToast(R.string.error_io);
		} else {
			showToast(R.string.error_unknow);
		}
	}

	/**
	 * 显示Toast消息
	 * 
	 * @param resid
	 */
	public void showToast(int resid) {
		showToast(getResources().getString(resid));
	}

	/**
	 * 显示Toast消息
	 * 
	 * @param msg
	 */
	public void showToast(String msg) {
		Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
	}

	private RequestQueue mHttpRequesttQueue;

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
	 * 获取 在 当前 Actvity 范围内的ImageLoader 注意 这里会创建 一个HttpRequestQueue 在这里处理消息
	 * 
	 * @return
	 */
	public ImageLoader getImageLoder() {
		if (mImageLoader == null) {
			mImageLoader = new ImageLoader(getHttpRequestQueue(), BitmapCache.getInstance());
		}
		return mImageLoader;
	}

}
