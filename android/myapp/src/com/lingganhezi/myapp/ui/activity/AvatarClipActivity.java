package com.lingganhezi.myapp.ui.activity;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;

import com.lingganhezi.myapp.R;
import com.lingganhezi.myapp.service.BaseService;
import com.lingganhezi.myapp.service.UserService;
import com.lingganhezi.ui.widget.ClipImageLayout;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.View.OnClickListener;

/**
 * 头像截图activity
 * 
 * @author chenzipeng
 *
 */
public class AvatarClipActivity extends BaseActivity {
	public final static String KEY_BITMAP_PATH = "KEY_BITMAP_PATH";

	private ClipImageLayout mClipImageLayout;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_avatarclip);

		mClipImageLayout = (ClipImageLayout) findViewById(R.id.clip_avatar);
		findViewById(R.id.clip_button).setOnClickListener(mClipButtonCickListener);

		initClip(getIntent());
	}

	private OnClickListener mClipButtonCickListener = new OnClickListener() {

		@Override
		public void onClick(View v) {
			switch (v.getId()) {
			case R.id.clip_button:
				clipAvatar();
				// debug 用来看裁剪结果
				// showCilp();
				break;

			default:
				break;
			}
		}
	};

	/**
	 * 初始化截取图片
	 * 
	 * @param intent
	 */
	private void initClip(Intent intent) {

		if (!intent.hasExtra(KEY_BITMAP_PATH)) {
			showToast(R.string.avatar_pic_not_fount);
			finish();
		}

		// 如果存在在跑的任务就停掉
		if (mLoadImageTask != null && !mLoadImageTask.getStatus().equals(AsyncTask.Status.FINISHED)) {
			mLoadImageTask.cancel(true);
		}

		mLoadImageTask = new LoadImageTask();
		mLoadImageTask.execute(intent.getStringExtra(KEY_BITMAP_PATH));
	}

	private LoadImageTask mLoadImageTask;

	private class LoadImageTask extends AsyncTask<String, Integer, Bitmap> {

		@Override
		protected Bitmap doInBackground(String... params) {
			Bitmap bitmap = BitmapFactory.decodeFile(params[0]);

			// 图片太大 先缩小处理
			final int boundary = 1280;
			if (bitmap.getWidth() > boundary || bitmap.getHeight() > boundary) {
				int srcWidth = bitmap.getWidth();
				int srcHeight = bitmap.getHeight();
				float scale = 1;
				if (srcWidth > srcHeight) {
					scale = (float) boundary / srcWidth;
				} else {
					scale = (float) boundary / srcHeight;
				}

				int outWidth = (int) (srcWidth * scale);
				int outHeight = (int) (srcHeight * scale);

				Matrix m = new Matrix();
				m.postScale(scale, scale);

				bitmap = Bitmap.createBitmap(bitmap, 0, 0, srcWidth, srcHeight, m, true);
			}
			return bitmap;
		}

		@Override
		protected void onPostExecute(Bitmap bitmap) {
			// 加载完成设置图
			dismissDialog();
			mClipImageLayout.setImageBitmap(bitmap);
		}

		@Override
		protected void onPreExecute() {
			showProgressDialog(getString(R.string.avatar_loading));
		}

	};

	/**
	 * 裁剪头像，并保存上传到服务器
	 */
	private void clipAvatar() {
		showProgressDialog(getString(R.string.avatar_uploading));
		// TODO 需要用 AsnyTask来异步执行？
		try {
			Bitmap bitmap = mClipImageLayout.clip();

			// 保存图片
			File tempAvatar = new File(this.getCacheDir() + "/temp_avatar.jpg");
			if (tempAvatar.exists()) {
				tempAvatar.delete();
			}
			FileOutputStream os = new FileOutputStream(tempAvatar);
			bitmap.compress(Bitmap.CompressFormat.JPEG, 100, os);

			// 上传图片
			getServiceManager().getUserService().uploadAvatar(tempAvatar, mMsgHandler);
		} catch (Exception e) {
			dismissDialog();
			showToast(R.string.avatar_upload_faild);
		}

	}

	private Handler mMsgHandler = new Handler(new Handler.Callback() {

		@Override
		public boolean handleMessage(Message msg) {
			switch (msg.what) {
			case UserService.MSG_UPLOAD_USER_AVATR_SUCCESS:
				// 上传成功 关闭Dialog 和 关闭activity
				dismissDialog();
				finish();
				break;
			case UserService.MSG_UPLOAD_USER_AVATR_FAILD:
			case BaseService.MSG_ERROR:
				dismissDialog();
				String errorMsg = msg.getData().getString(BaseService.MESSAGE_FALG);
				errorMsg = errorMsg != null ? ": " + errorMsg : new String();
				showToast(getString(R.string.avatar_upload_faild) + errorMsg);
				break;
			default:
				break;
			}
			return true;
		}
	});

	/**
	 * debug 测试用，用来看裁剪结果
	 */
	private void showCilp() {
		Bitmap bitmap = mClipImageLayout.clip();
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
		byte[] datas = baos.toByteArray();
		Intent intent = new Intent(this, ShowImageActivity.class);
		intent.putExtra("bitmap", datas);
		startActivity(intent);
	}
}
