package com.lingganhezi.myapp.service.handler;

import android.os.Message;

import com.lingganhezi.myapp.service.UserService;

/**
 * 用户朋友handler，用来处理 添加，删除好友 </br>用户表数据用 msg.obj 来传输，类型是 UserInfo
 * 
 * @author chenzipeng
 *
 */
public class UserFriendHandler extends BaseServiceHandler {

	public UserFriendHandler(Callback callback) {
		super();
		mCallback = callback;
	}

	@Override
	public void handleMessage(Message msg) {
		String messageStr = getServerMessage(msg);

		switch (msg.what) {
		case UserService.MSG_QUERY_UNFRIEND_SUCCESS:
		case UserService.MSG_NEW_FRIEND_SUCCESS:
		case UserService.MSG_SYNC_FRIEND_SUCCESS:
			callCallback(msg, true);
		default:
			callCallback(msg, false);
			break;
		}
		super.handleMessage(msg);
	}

}
