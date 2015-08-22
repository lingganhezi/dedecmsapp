package com.lingganhezi.myapp.service.handler;

import android.os.Handler;
import android.os.Message;

import com.lingganhezi.myapp.entity.UserInfo;
import com.lingganhezi.myapp.service.UserService;

/**
 * 同步用户信息的用Handler
 * 
 * @author chenzipeng
 *
 */
public class UserSyncHandler extends Handler {
	private String mUserid;
	private SyncCallback mSyncCallback;

	public UserSyncHandler(String userid, SyncCallback callback) {
		super();
		this.mUserid = userid;
		mSyncCallback = callback;
	}

	@Override
	public void handleMessage(Message msg) {
		switch (msg.what) {
		case UserService.MSG_SYNC_USERINFO_SUCCESS:
			if (mSyncCallback != null) {
				mSyncCallback.complate(true, UserService.getInstance().getUserInfo(mUserid));
			}
			break;
		default:
			if (mSyncCallback != null) {
				mSyncCallback.complate(false, null);
			}
			break;
		}
		super.handleMessage(msg);
	}

	/**
	 * 同步用户信息 Callback
	 * 
	 * @author chenzipeng
	 *
	 */
	public interface SyncCallback {
		/**
		 * 当完成以后的回调
		 * 
		 * @param succes
		 *            结果
		 * @param userInfo
		 *            如果success == false，这里就为null
		 */
		public void complate(boolean succes, UserInfo userInfo);
	}
}
