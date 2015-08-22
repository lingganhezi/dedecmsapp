package com.lingganhezi.myapp.service.handler;

import com.lingganhezi.myapp.service.MessageService;
import android.os.Message;

/**
 * 消息发送 handler
 * 
 * @author chenzipeng
 *
 */
public class MessageSendHandler extends BaseServiceHandler {

	public MessageSendHandler(Callback callback) {
		super();
		mCallback = callback;
	}

	@Override
	public void handleMessage(Message msg) {
		switch (msg.what) {
		case MessageService.MSG_SEND_MESSAGE_SUCCESS:
			callCallback(msg, true);
			break;
		default:
			callCallback(msg, false);
			break;
		}
		super.handleMessage(msg);
	}
}
