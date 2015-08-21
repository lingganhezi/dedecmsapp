package com.lingganhezi.myapp.ui.activity;

import com.lingganhezi.myapp.R;
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

public class RegisteActivity extends BaseActivity {
	private final String TAG = LoginActivity.class.getSimpleName();
	// view
	private EditText mEditText_email;
	private EditText mEditText_password;

	@Override
	protected void onCreate(Bundle bundle) {
		super.onCreate(bundle);
		setContentView(R.layout.activity_registe);
		mEditText_email = (EditText) findViewById(R.id.editText_email);
		mEditText_password = (EditText) findViewById(R.id.editText_password);

		findViewById(R.id.btn_registe).setOnClickListener(mViewClickListener);
		findViewById(R.id.registe_hasaccount).setOnClickListener(mViewClickListener);
	}

	private OnClickListener mViewClickListener = new OnClickListener() {

		@Override
		public void onClick(View v) {
			switch (v.getId()) {
			case R.id.btn_registe:
				registe();
				break;
			case R.id.registe_hasaccount:
				startActivity(new Intent(RegisteActivity.this, LoginActivity.class));
				break;
			default:
				break;
			}

		}
	};

	private String email;
	private String password;

	private void registe() {
		email = mEditText_email.getText().toString().trim();
		password = mEditText_password.getText().toString().trim();

		getServiceManager().getLoginService().registe(email, password, mMsgHandler);

		showProgressDialog(getString(R.string.dialog_registe_registing));
	}

	Handler mMsgHandler = new Handler(new Handler.Callback() {

		@Override
		public boolean handleMessage(Message msg) {
			int msgtype = msg.what;
			String message = msg.getData().getString(BaseService.MESSAGE_FALG);
			switch (msgtype) {
			case LoginService.MSG_REGISTE_SUCCESS:
				// 启动登录
				Intent intent = new Intent(RegisteActivity.this, LoginActivity.class);
				intent.putExtra(LoginActivity.KEY_USERNAME, email);
				intent.putExtra(LoginActivity.KEY_PASSWORD, password);
				startActivity(intent);
				// 关闭当前activity
				finish();
				break;
			case LoginService.MSG_REGISTE_FAILD:
				if (TextUtils.isEmpty(message)) {
					showToast(R.string.dialog_registe_faild);
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
