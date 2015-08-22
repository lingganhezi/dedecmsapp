package com.lingganhezi.myapp.service.handler;

import com.lingganhezi.myapp.service.BaseService;

import android.os.Handler;
import android.os.Message;

/**
 * 基础处理ServiceHandler 会处理 由service 发出的消息，发出的消息 需要符合以下规则 </br> </br>msg.obj 数据实体，
 * </br>msg.getData().getString(BaseService.MESSAGE_FALG) 为服务器返回的文本消息，比如一些出错的信息等
 * 
 * @author chenzipeng
 *
 * @param <T>
 *            数据实体类型
 */
public class BaseServiceHandler<T> extends Handler {
	protected Callback mCallback;

	/**
	 * 获取 完成以后 msg.obj
	 * 
	 * @param message
	 * @return
	 */
	T getMessageEntry(Message message) {
		if (message.obj != null) {
			return (T) message.obj;
		}
		return null;
	}

	/**
	 * 获取服务器消息
	 * 
	 * @param message
	 * @return
	 */
	String getServerMessage(Message message) {
		return message.getData().getString(BaseService.MESSAGE_FALG);
	}

	/**
	 * 调用callback
	 * 
	 * @param msg
	 * @param isSuccess
	 */
	protected void callCallback(Message msg, boolean isSuccess) {
		if (mCallback != null) {
			// 使用msg.obj 来传输用户列表
			mCallback.complate(isSuccess, getMessageEntry(msg), getServerMessage(msg));
		}
	}

	/**
	 * 同步用户信息 Callback
	 * 
	 * @author chenzipeng
	 *
	 */
	public interface Callback<T> {
		/**
		 * 当完成以后的回调
		 * 
		 * @param succes
		 *            结果
		 * @param entry
		 *            数据实体 T
		 * @param message
		 *            服务器返回的消息
		 */
		public void complate(boolean success, T entry, String message);
	}
}
