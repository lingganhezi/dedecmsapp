package com.lingganhezi.myapp.ui.fragment;

import com.lingganhezi.myapp.R;
import com.lingganhezi.myapp.entity.LoginUserInfo;
import com.lingganhezi.myapp.service.LoginService;
import com.lingganhezi.myapp.ui.activity.AboutActivity;
import com.lingganhezi.myapp.ui.activity.LoginActivity;
import com.lingganhezi.myapp.ui.activity.PersonalInfoActivity;
import com.lingganhezi.myapp.ui.activity.SettingActivity;
import com.lingganhezi.ui.widget.LoadImageView;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.TextView;

public class PersonalFragment extends BaseFragment {
	private String TAG = PersonalFragment.class.getSimpleName();
	private View mPersonInfoItemView;
	private LoadImageView mHeaderPicView;
	private TextView mNameView;
	private View mLoginItemView;
	private View mLogoutItemView;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View root = inflater.inflate(R.layout.fragment_personal, container, false);

		mPersonInfoItemView = root.findViewById(R.id.personal_info);
		mHeaderPicView = (LoadImageView) mPersonInfoItemView.findViewById(R.id.personal_info_headerpic);
		mNameView = (TextView) mPersonInfoItemView.findViewById(R.id.personal_info_name);

		mLoginItemView = root.findViewById(R.id.personal_login);
		mLogoutItemView = root.findViewById(R.id.personal_logout);

		mPersonInfoItemView.setOnClickListener(mItemClickListener);
		mLoginItemView.setOnClickListener(mItemClickListener);
		mLogoutItemView.setOnClickListener(mItemClickListener);
		root.findViewById(R.id.personal_setting).setOnClickListener(mItemClickListener);
		root.findViewById(R.id.personal_about).setOnClickListener(mItemClickListener);

		return root;
	}

	@Override
	public void onStart() {
		super.onStart();
		refreshLoginState();
	}

	private void refreshLoginState() {
		// 判断是否已经登陆
		if (getServiceManager().getLoginService().isLogined()) {
			mPersonInfoItemView.setVisibility(View.VISIBLE);
			mLoginItemView.setVisibility(View.GONE);
			mLogoutItemView.setVisibility(View.VISIBLE);
			// 初始化 个人信息view
			initPersonInfoView();

		} else {
			mPersonInfoItemView.setVisibility(View.GONE);
			mLoginItemView.setVisibility(View.VISIBLE);
			mLogoutItemView.setVisibility(View.GONE);
		}
	}

	private OnClickListener mItemClickListener = new OnClickListener() {

		@Override
		public void onClick(View v) {
			final int viewId = v.getId();
			Intent intent = null;
			switch (viewId) {
			case R.id.personal_login:
				intent = new Intent(getActivity(), LoginActivity.class);
				break;
			case R.id.personal_setting:
				intent = new Intent(getActivity(), SettingActivity.class);
				break;
			case R.id.personal_about:
				intent = new Intent(getActivity(), AboutActivity.class);
				break;
			case R.id.personal_logout:
				logout();
				return;
			case R.id.personal_info:
				LoginUserInfo currentUser = getServiceManager().getUserService().getCurrentLoginUser();
				intent = new Intent(getActivity(), PersonalInfoActivity.class);
				intent.putExtra(PersonalInfoActivity.KEY_USERID, currentUser.getUserId());
				break;
			default:
				break;
			}

			try {
				getActivity().startActivity(intent);
			} catch (Exception e) {
				Log.e(TAG, e.getMessage(), e);
			}
		}
	};

	private void initPersonInfoView() {
		LoginUserInfo currentUser = getServiceManager().getUserService().getCurrentLoginUser();
		if (currentUser != null) {
			mHeaderPicView.setImageUrl(currentUser.getUserInfo().getProtrait(), getImageLoder());
			mNameView.setText(currentUser.getUserInfo().getName());
		}
	}

	private void logout() {
		showProgressDialog(getResources().getString(R.string.dialog_logout_logouting));
		getServiceManager().getLoginService().logout(new Handler(new Handler.Callback() {

			@Override
			public boolean handleMessage(Message msg) {
				switch (msg.what) {
				case LoginService.MSG_LOGOUT_SUCCESS:
					break;
				default:
					mBaseActivity.showToast(R.string.dialog_logout_faild);
					break;
				}
				dismissDialog();
				refreshLoginState();
				return true;
			}
		}));
	}
}
