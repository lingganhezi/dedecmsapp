package com.lingganhezi.myapp.service.handler;

import com.lingganhezi.myapp.service.MessageService;
import com.lingganhezi.myapp.service.UserService;

import android.database.Cursor;
import android.os.Handler;
import android.os.Message;

public class MessageSyncHandler extends Handler{
	private MessageSyncCallback mMessageSyncCallback;
	private String mMsgid;
	
	public MessageSyncHandler(String msgid,MessageSyncCallback callback) {
		super();
		mMsgid = msgid;
		mMessageSyncCallback = callback;
	}
	
	@Override
	public void handleMessage(Message msg) {
		switch (msg.what) {
		//请求成功后
		case MessageService.MSG_SYNC_MESSAGE_SUCCESS:
			if(mMessageSyncCallback != null){
				mMessageSyncCallback.complate(true,mMsgid);
			}
			break;
		default:
			if(mMessageSyncCallback != null){
				mMessageSyncCallback.complate(false,mMsgid);
			}
			break;
		}
		super.handleMessage(msg);
	}
	
	public MessageSyncCallback getMessageSyncCallback() {
		return mMessageSyncCallback;
	}

	public void setMessageSyncCallback(MessageSyncCallback MessageSyncCallback) {
		mMessageSyncCallback = MessageSyncCallback;
	}

	/**
	 * 消息同步回调
	 * @author chenzipeng
	 *
	 */
	public static interface MessageSyncCallback{
		/**
		 * 当请求完成后，会调用此方法
		 * @param success
		 * @param msgid 
		 */
		public void complate(boolean success,String msgid);
	}
}
