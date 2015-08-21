package com.lingganhezi.myapp.service.handler;

import com.lingganhezi.myapp.service.MessageService;
import android.database.Cursor;
import android.os.Handler;
import android.os.Message;

public class MessageQueryHandler extends Handler {
	private MessageQueryCallback mMessageQueryCallback;

	public MessageQueryHandler(MessageQueryCallback callback){
		super();
		mMessageQueryCallback = callback;
	}
	
	@Override
	public void handleMessage(Message msg) {
		switch (msg.what) {
		// 请求成功后
		case MessageService.MSG_QUERY_MESSAGE_SUCCESS:
			Cursor c = (Cursor) msg.obj;
			if (mMessageQueryCallback != null) {
				mMessageQueryCallback.complate(true, c);
			}
			break;
		default:
			if (mMessageQueryCallback != null) {
				mMessageQueryCallback.complate(false, null);
			}
			break;
		}

		super.handleMessage(msg);
	}

	public MessageQueryCallback getMessageQueryCallback() {
		return mMessageQueryCallback;
	}

	public void setMessageQueryCallback(MessageQueryCallback messageQueryCallback) {
		mMessageQueryCallback = messageQueryCallback;
	}

	/**
	 * 消息查询回调
	 * 
	 * @author chenzipeng
	 *
	 */
	public static interface MessageQueryCallback {
		/**
		 * 当请求完成后，会调用此方法
		 * 
		 * @param success
		 * @param cursor
		 *            当 success == false 时为null
		 */
		public void complate(boolean success, Cursor cursor);
	}
}
