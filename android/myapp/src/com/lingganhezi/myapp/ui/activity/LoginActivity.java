package com.lingganhezi.myapp.ui.activity;

import com.lingganhezi.myapp.R;
import com.lingganhezi.myapp.entity.LoginUserInfo;
import com.lingganhezi.myapp.service.BaseService;
import com.lingganhezi.myapp.service.LoginService;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;

public class LoginActivity extends BaseActivity {
	private final String TAG = LoginActivity.class.getSimpleName();
	// view
	private EditText mEditText_username;
	private EditText mEditText_password;

	public final static String KEY_USERNAME = "KEY_USERNAME";
	public final static String KEY_PASSWORD = "KEY_PASSWORD";

	@Override
	protected void onCreate(Bundle bundle) {
		super.onCreate(bundle);
		setContentView(R.layout.activity_login);
		mEditText_username = (EditText) findViewById(R.id.editText_username);
		mEditText_password = (EditText) findViewById(R.id.editText_password);

		findViewById(R.id.btn_login).setOnClickListener(mViewClickListener);
		findViewById(R.id.login_forgotpassword).setOnClickListener(mViewClickListener);
		findViewById(R.id.login_registe).setOnClickListener(mViewClickListener);

		// 读取缓存的 用户名、密码
		LoginUserInfo loginConfig = getServiceManager().getLoginService().loadLoginUserInfoConfig();
		mEditText_username.setText(loginConfig.getUserId());
		mEditText_password.setText(loginConfig.getPassword());
		// 第一次创建的时候 不会调用OnNewIntent
		handlerIntentLogin(getIntent());
	}

	@Override
	protected void onNewIntent(Intent intent) {
		handlerIntentLogin(intent);
	};

	/**
	 * 检查 intent中的参数 ，如果有就登录
	 */
	private void handlerIntentLogin(Intent intent) {
		Bundle extras = intent.getExtras();
		if (extras != null && extras.containsKey(KEY_USERNAME) && extras.containsKey(KEY_PASSWORD)) {
			login(extras.getString(KEY_USERNAME), extras.getString(KEY_PASSWORD));
		}
	}

	private OnClickListener mViewClickListener = new OnClickListener() {

		@Override
		public void onClick(View v) {
			switch (v.getId()) {
			case R.id.btn_login:
				String username = mEditText_username.getText().toString().trim();
				String password = mEditText_password.getText().toString().trim();
				login(username, password);
				break;
			case R.id.login_forgotpassword:
				startActivity(new Intent(LoginActivity.this, ForgotPasswordActivity.class));
				break;
			case R.id.login_registe:
				startActivity(new Intent(LoginActivity.this, RegisteActivity.class));
				break;
			default:
				break;
			}

		}
	};

	private void login(String username, String password) {

		getServiceManager().getLoginService().login(username, password, mMsgHandler);

		showProgressDialog(getString(R.string.dialog_login_loging));
	}

	Handler mMsgHandler = new Handler(new Handler.Callback() {

		@Override
		public boolean handleMessage(Message msg) {
			int msgtype = msg.what;
			String message = msg.getData().getString(BaseService.MESSAGE_FALG);
			switch (msgtype) {
			case LoginService.MSG_LOGIN_SUCCESS:
				finish();
				break;
			case LoginService.MSG_LOGIN_FAILD:
				if (TextUtils.isEmpty(message)) {
					showToast(R.string.dialog_login_faild);
				} else {
					showToast(message);
				}
				break;
			case BaseService.MSG_ERROR:
				Exception e = (Exception) msg.obj;
				Log.e(TAG, e.getMessage(), e);
				showToast(e);
				break;
			default:
				break;
			}
			dismissDialog();
			return true;
		}
	});

}
