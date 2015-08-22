package com.lingganhezi.myapp.service.handler;

import com.lingganhezi.myapp.service.MessageService;
import android.os.Message;

public class MessageSyncHandler extends BaseServiceHandler<String> {
	private String mMsgid;

	/**
	 * 
	 * @param msgid
	 * @param callback
	 *            回传的 string 类型为 msgid
	 */
	public MessageSyncHandler(String msgid, Callback<String> callback) {
		super();
		mMsgid = msgid;
		mCallback = callback;
	}

	@Override
	public void handleMessage(Message msg) {
		// 赋值给 msg.obj
		msg.obj = mMsgid;

		switch (msg.what) {
		// 请求成功后
		case MessageService.MSG_SYNC_MESSAGE_SUCCESS:
			callCallback(msg, true);
			break;
		default:
			callCallback(msg, false);
			break;
		}
		super.handleMessage(msg);
	}

}
