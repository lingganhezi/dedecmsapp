package com.lingganhezi.myapp.ui.activity;

import com.lingganhezi.myapp.ConfigHelper;
import com.lingganhezi.myapp.R;
import com.lingganhezi.ui.widget.SwitchButton;
import com.lingganhezi.ui.widget.SwitchButton.OnChangeListener;
import com.lingganhezi.utils.BitmapCache;

import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;

/**
 * 设置
 * 
 * @author chenzipeng
 *
 */
public class SettingActivity extends BaseActivity {

	@Override
	protected void onCreate(Bundle arg0) {
		super.onCreate(arg0);
		setContentView(R.layout.activity_setting);
		findViewById(R.id.setting_clean_imagecache).setOnClickListener(mItemCilckListener);
		SwitchButton allowMobileNetworkButton = (SwitchButton) findViewById(R.id.setting_image_networktype);
		allowMobileNetworkButton.setSwitchState(ConfigHelper.getInstance(this).isAllowMobileNetwork());
		allowMobileNetworkButton.setOnChangeListener(mSwtichButtonChangeListener);
	}

	private OnClickListener mItemCilckListener = new OnClickListener() {

		@Override
		public void onClick(View v) {
			switch (v.getId()) {
			case R.id.setting_clean_imagecache:
				cleanImageCache();
				break;

			default:
				break;
			}
		}
	};

	private OnChangeListener mSwtichButtonChangeListener = new OnChangeListener() {

		@Override
		public void onChange(SwitchButton sb, boolean state) {
			switch (sb.getId()) {
			case R.id.setting_image_networktype:
				changeImageNetworkType(state);
				break;

			default:
				break;
			}
		}
	};

	/**
	 * 清除图片缓存
	 */
	private void cleanImageCache() {
		AsyncTask t = new AsyncTask<Object, Integer, Boolean>() {

			@Override
			protected Boolean doInBackground(Object... params) {
				boolean success = true;
				try {
					BitmapCache.getInstance().cleanCacheL2();
				} catch (Exception e) {
					success = false;
				}
				return success;
			}

			@Override
			protected void onPreExecute() {
				showProgressDialog(getString(R.string.setting_clean_imagecache_cleaning));
			}

			@Override
			protected void onPostExecute(Boolean result) {
				dismissDialog();
				if (result) {
					showToast(R.string.setting_clean_imagecache_success);
				} else {
					showToast(R.string.setting_clean_imagecache_error);
				}
			}
		};
		t.execute();
	}

	/**
	 * 修改 图片 是否允许使用移动网络
	 */
	private void changeImageNetworkType(boolean state) {
		ConfigHelper.getInstance(this).setAllowMobileNetwork(state);
	}
}
