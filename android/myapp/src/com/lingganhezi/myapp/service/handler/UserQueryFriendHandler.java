package com.lingganhezi.myapp.service.handler;

import java.util.List;

import android.os.Message;

import com.lingganhezi.myapp.entity.UserInfo;
import com.lingganhezi.myapp.service.UserService;

/**
 * 查询用户好友的用Handler </br>用户列表数据用 msg.obj 来传输，类型是 List<UserInfo>
 * 
 * @author chenzipeng
 *
 */
public class UserQueryFriendHandler extends BaseServiceHandler<List<UserInfo>> {

	public UserQueryFriendHandler(Callback<List<UserInfo>> callback) {
		super();
		mCallback = callback;
	}

	@Override
	public void handleMessage(Message msg) {
		switch (msg.what) {
		case UserService.MSG_QUERY_UNFRIEND_SUCCESS:
			callCallback(msg, true);
			break;
		default:
			callCallback(msg, false);
			break;
		}
		super.handleMessage(msg);
	}
}
