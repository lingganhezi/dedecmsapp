package com.lingganhezi.myapp.ui.activity;

import com.lingganhezi.myapp.R;
import com.lingganhezi.myapp.entity.LoginUserInfo;
import com.lingganhezi.myapp.service.BaseService;
import com.lingganhezi.myapp.service.LoginService;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;

public class ForgotPasswordActivity extends BaseActivity {
	private final String TAG = LoginActivity.class.getSimpleName();
	// view
	private EditText mEditText_email;

	@Override
	protected void onCreate(Bundle bundle) {
		super.onCreate(bundle);
		setContentView(R.layout.activity_forgotpassword);
		mEditText_email = (EditText) findViewById(R.id.editText_email);

		findViewById(R.id.btn_send).setOnClickListener(mViewClickListener);

		LoginUserInfo loginConfig = getServiceManager().getLoginService().loadLoginUserInfoConfig();
		mEditText_email.setText(loginConfig.getUserId());
	}

	private OnClickListener mViewClickListener = new OnClickListener() {

		@Override
		public void onClick(View v) {
			switch (v.getId()) {
			case R.id.btn_send:
				sendForgotpassword();
				break;
			default:
				break;
			}

		}
	};

	private void sendForgotpassword() {
		String email = mEditText_email.getText().toString().trim();
		getServiceManager().getLoginService().forgotpassword(email, mMsgHandler);

		showProgressDialog(getString(R.string.dialog_forgotpassword_sending));
	}

	Handler mMsgHandler = new Handler(new Handler.Callback() {

		@Override
		public boolean handleMessage(Message msg) {
			int msgtype = msg.what;
			String message = msg.getData().getString(BaseService.MESSAGE_FALG);
			switch (msgtype) {
			case LoginService.MSG_FORGOTPASSWORD_SUCCESS:
				finish();
				break;
			case LoginService.MSG_FORGOTPASSWORD_FAILD:
				if (TextUtils.isEmpty(message)) {
					showToast(R.string.dialog_forgotpassword_faild);
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
