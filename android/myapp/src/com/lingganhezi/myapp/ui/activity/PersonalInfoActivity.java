package com.lingganhezi.myapp.ui.activity;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import com.android.volley.NoConnectionError;
import com.lingganhezi.myapp.Constant;
import com.lingganhezi.myapp.R;
import com.lingganhezi.myapp.entity.LoginUserInfo;
import com.lingganhezi.myapp.entity.UserInfo;
import com.lingganhezi.myapp.service.BaseService;
import com.lingganhezi.myapp.service.UserService;
import com.lingganhezi.ui.widget.LoadImageView;
import com.lingganhezi.utils.MediaHelper;

import android.content.ClipData;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.TextView;

public class PersonalInfoActivity extends BaseActivity implements OnClickListener {
	private final String TAG = PersonalInfoActivity.class.getSimpleName();

	private LoadImageView mHeaderPicView;
	private TextView mUserNameView;
	private TextView mPostsView;
	private TextView mGoodsView;
	private TextView mAgeView;
	private TextView mCityView;
	private TextView mDescriptionView;
	private ImageView mSexIconView;

	private String mUserid;
	private UserInfo mUserInfo;
	private UserService mUserService;

	public final static String KEY_USERID = "KEY_USERID";

	private final int REQUEST_CODE_SELECT_AVATAR = 0;
	private final int REQUEST_CODE_AGE = 1;
	private final int REQUEST_CODE_PLACE = 2;
	private final int REQUEST_CODE_DESCRIPTION = 3;
	private final int REQUEST_CODE_SEX = 4;

	@Override
	protected void onCreate(Bundle bundle) {
		super.onCreate(bundle);
		setContentView(R.layout.activity_personalinfo);

		mUserService = getServiceManager().getUserService();

		mHeaderPicView = (LoadImageView) findViewById(R.id.personalinfo_headerpic);
		mUserNameView = (TextView) findViewById(R.id.personalinfo_username);
		mPostsView = (TextView) findViewById(R.id.personalinfo_posts);
		mGoodsView = (TextView) findViewById(R.id.personalinfo_goods);
		mAgeView = (TextView) findViewById(R.id.personalinfo_age);
		mCityView = (TextView) findViewById(R.id.personalinfo_city);
		mDescriptionView = (TextView) findViewById(R.id.personalinfo_description);
		mSexIconView = (ImageView) findViewById(R.id.personalinfo_icon_sex);

		mHeaderPicView.setOnClickListener(this);
		findViewById(R.id.personalinfo_item_age).setOnClickListener(this);
		findViewById(R.id.personalinfo_item_city).setOnClickListener(this);
		findViewById(R.id.personalinfo_item_description).setOnClickListener(this);
		findViewById(R.id.personalinfo_item_sex).setOnClickListener(this);
	}

	@Override
	protected void onStart() {
		super.onStart();
		handleIntent(getIntent());
	}

	private void handleIntent(Intent intent) {
		mUserid = intent.getStringExtra(KEY_USERID);
		if (mUserid == null) {
			throw new IllegalArgumentException("Extra KEY_USERID is null");
		}
		// 先加载UserInfo
		mUserInfo = mUserService.getUserInfo(mUserid);
		if (mUserInfo == null) {
			mUserService.syncUserInfo(mUserid, mMsgHandler);
		} else {
			updateUserInfo();
			mUserService.syncUserInfo(mUserid, new Handler());
		}
	}

	// 这里取 10000，应该不会跟其他消息冲突
	private final int MSG_GET_USERINFO_COMPLATE = 10000;

	Handler mMsgHandler = new Handler(new Handler.Callback() {

		@Override
		public boolean handleMessage(Message msg) {
			switch (msg.what) {
			case MSG_GET_USERINFO_COMPLATE:
				mUserInfo = mUserService.getUserInfo(mUserid);
				if (mUserInfo == null) {
					// 如果没有找到这个userInfo，就发送错误消息
					mMsgHandler.obtainMessage(BaseService.MSG_ERROR).sendToTarget();
					return true;
				}
				updateUserInfo();
				break;
			case UserService.MSG_SYNC_USERINFO_SUCCESS:
			case UserService.MSG_SYNC_USERINFO_FAILD:
				mMsgHandler.obtainMessage(MSG_GET_USERINFO_COMPLATE).sendToTarget();
				break;
			case BaseService.MSG_ERROR:
				// 当没有网络的时候允许
				if (msg.obj != null && msg.obj instanceof NoConnectionError) {
					mMsgHandler.obtainMessage(MSG_GET_USERINFO_COMPLATE).sendToTarget();
					break;
				}
				// TODO 发生错误！
				showToast(getString(R.string.personalinfo_get_userinfo_faild));
				finish();
				break;
			case UserService.MSG_UPDATE_USERINFO_FAILD:
				showToast(R.string.personalinfo_update_faild);
				break;
			default:
				break;
			}
			return true;
		}

	});

	private void updateUserInfo() {
		mUserNameView.setText(mUserInfo.getName());
		mHeaderPicView.setImageUrl(mUserInfo.getProtrait(), getImageLoder());
		Integer post = 0;// TODO 获取文章数
		mPostsView.setText(String.valueOf(post));

		Integer goods = 0;// TODO 获取赞数
		mGoodsView.setText(String.valueOf(goods));

		String age = new String();
		try {
			Date birthday = new SimpleDateFormat("yyyy-MM-dd").parse(mUserInfo.getBirthday());
			Calendar ca = Calendar.getInstance();
			ca.setTime(birthday);
			int birthYear = ca.get(Calendar.YEAR);

			ca.setTime(new Date());
			int currentYear = ca.get(Calendar.YEAR);

			age = String.valueOf(currentYear - birthYear);
		} catch (Exception e) {
		}
		mAgeView.setText(age);

		mCityView.setText(mUserService.loadPlaceName(mUserInfo.getCity()));
		mDescriptionView.setText(mUserInfo.getDescription());

		mSexIconView.setImageResource(UserService.SEX_DRAWABLE_MAP.get(mUserInfo.getSex()));
	}

	@Override
	public void onClick(View v) {
		// 不是当前登录用户
		if (!mUserInfo.getId().equals(mUserService.getCurrentLoginUser().getUserId())) {
			return;
		}
		switch (v.getId()) {
		case R.id.personalinfo_headerpic:
			if (isCurrentLoginUser()) {
				startAvatarPicker();
			}
			break;
		case R.id.personalinfo_item_age:
			if (isCurrentLoginUser()) {
				startEditorDialog(REQUEST_CODE_AGE);
			}
			break;
		case R.id.personalinfo_item_city:
			if (isCurrentLoginUser()) {
				startEditorDialog(REQUEST_CODE_PLACE);
			}
			break;
		case R.id.personalinfo_item_description:
			if (isCurrentLoginUser()) {
				startEditorDialog(REQUEST_CODE_DESCRIPTION);
			}
			break;
		case R.id.personalinfo_item_sex:
			if (isCurrentLoginUser()) {
				startEditorDialog(REQUEST_CODE_SEX);
			}
			break;
		default:
			break;
		}
	}

	private void startEditorDialog(int requestCode) {
		Intent intent = new Intent(PersonalInfoActivity.this, EditorDialogActivity.class);
		switch (requestCode) {
		case REQUEST_CODE_AGE:
			intent.putExtra(EditorDialogActivity.KEY_TITLE, getString(R.string.personalinfo_age));
			intent.putExtra(EditorDialogActivity.KEY_TYPE, EditorDialogActivity.TYPE_DATE);
			Date date = new Date();
			try {
				date = new SimpleDateFormat("yyyy-MM-dd").parse(mUserInfo.getBirthday());
			} catch (Exception e) {
			}
			intent.putExtra(EditorDialogActivity.KEY_DATE, date);
			break;

		case REQUEST_CODE_PLACE:
			intent.putExtra(EditorDialogActivity.KEY_TITLE, getString(R.string.personalinfo_city));
			intent.putExtra(EditorDialogActivity.KEY_TYPE, EditorDialogActivity.TYPE_PLACE);
			Integer placeid = 0;
			try {
				placeid = Integer.valueOf(mUserInfo.getCity());
			} catch (Exception e) {
			}
			intent.putExtra(EditorDialogActivity.KEY_PLACE, placeid);
			break;

		case REQUEST_CODE_DESCRIPTION:
			intent.putExtra(EditorDialogActivity.KEY_TITLE, getString(R.string.personalinfo_description));
			intent.putExtra(EditorDialogActivity.KEY_TYPE, EditorDialogActivity.TYPE_MULTI_TEXT);
			intent.putExtra(EditorDialogActivity.KEY_TEXT, mUserInfo.getDescription());
			break;

		case REQUEST_CODE_SEX:
			intent.putExtra(EditorDialogActivity.KEY_TITLE, getString(R.string.personalinfo_sex));
			intent.putExtra(EditorDialogActivity.KEY_TYPE, EditorDialogActivity.TYPE_SEX);
			intent.putExtra(EditorDialogActivity.KEY_SEX, mUserInfo.getSex());
		default:
			break;
		}
		startActivityForResult(intent, requestCode);
	}

	/**
	 * 启动 头像选择
	 */
	private void startAvatarPicker() {
		Intent intent = new Intent(Intent.ACTION_GET_CONTENT);// ACTION_OPEN_DOCUMENT
		intent.addCategory(Intent.CATEGORY_OPENABLE);
		intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, false);// 不允许多选
		intent.setType("image/*");
		startActivityForResult(intent, REQUEST_CODE_SELECT_AVATAR);
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent resultIntent) {
		if (resultCode != RESULT_OK) {
			return;
		}

		switch (requestCode) {
		// 处理头像选择
		case REQUEST_CODE_SELECT_AVATAR:
			Uri uri = null;
			if (resultIntent.getData() != null) {// 单选
				uri = resultIntent.getData();
			} else if (resultIntent.getClipData() != null) {// 多选
				ClipData clipdata = resultIntent.getClipData();
				if (clipdata.getItemCount() > 0) {
					uri = clipdata.getItemAt(0).getUri();
				}
			}

			if (uri == null) {
				showToast(R.string.avatar_pic_has_not_select);
				return;
			}

			String path = MediaHelper.getPath(this, uri);

			if (TextUtils.isEmpty(path)) {
				showToast(R.string.avatar_pic_not_fount);
				return;
			}
			// 启动裁剪
			Intent intent = new Intent(this, AvatarClipActivity.class);
			intent.putExtra(AvatarClipActivity.KEY_BITMAP_PATH, path);
			startActivity(intent);
			break;

		// 年龄
		case REQUEST_CODE_AGE: {
			Serializable data = resultIntent.getSerializableExtra(EditorDialogActivity.KEY_RESULT_DATA);
			String dateStr = new SimpleDateFormat("yyyy-MM-dd").format((Date) data);
			mUserInfo.setBirthday(dateStr);
			updateCurrentLoginUserInfo();
		}
			break;

		// TODO 地区选择
		case REQUEST_CODE_PLACE: {
			int placeid = resultIntent.getIntExtra(EditorDialogActivity.KEY_RESULT_DATA, 0);
			if (placeid != 0) {
				mUserInfo.setCity(String.valueOf(placeid));
				updateCurrentLoginUserInfo();
			}
		}
			break;

		case REQUEST_CODE_DESCRIPTION: {
			String description = (String) resultIntent.getSerializableExtra(EditorDialogActivity.KEY_RESULT_DATA);
			mUserInfo.setDescription(description);
			updateCurrentLoginUserInfo();
		}
			break;

		case REQUEST_CODE_SEX: {
			int sex = resultIntent.getIntExtra(EditorDialogActivity.KEY_RESULT_DATA, Constant.SEX_UNKONW);
			mUserInfo.setSex(sex);
			updateCurrentLoginUserInfo();
		}
			break;
		default:
			break;
		}

	}

	/**
	 * 更新 当前登陆用户信息，并刷新界面
	 */
	private void updateCurrentLoginUserInfo() {
		// TODO 需要检查登录？
		mUserService.saveUserInfo(mUserInfo, mMsgHandler);
		mUserService.getCurrentLoginUser().setUserInfo(mUserInfo);
		updateUserInfo();
	}

	/**
	 * 是否当前登录用户
	 * 
	 * @return
	 */
	private boolean isCurrentLoginUser() {
		LoginUserInfo currentUser = mUserService.getCurrentLoginUser();
		if (currentUser == null)
			return false;

		if (mUserid.equals(currentUser.getUserId())) {
			return true;
		} else {
			return false;
		}

	}
}
