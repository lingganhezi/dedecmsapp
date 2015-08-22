package com.lingganhezi.myapp.net;

import org.json.JSONObject;

import android.os.Handler;
import android.os.Message;

import com.android.volley.Response;
import com.lingganhezi.myapp.Constant;
import com.lingganhezi.myapp.HttpHelper;
import com.lingganhezi.myapp.entity.Respone;
import com.lingganhezi.myapp.service.BaseService;

/**
 * TODO 替换 各个service 的respone listenner. 抽象化 resultrespone的处理
 * 
 * @author chenzipeng
 *
 */
public abstract class ResultResponeListener implements Response.Listener<JSONObject> {
	private Respone mResultRespone;
	private Message mMessage;

	@Override
	public void onResponse(JSONObject json) {
		mResultRespone = (Respone) HttpHelper.getJsonObject(json, Respone.class);
		mMessage = new Message();
		setServerMessage();
		if (mResultRespone.stateCode == Constant.STATE_CODE_SUCCESS) {
			handeResponeSuccess(mResultRespone);
		} else {
			handeResponeFaild(mResultRespone);
		}
	}

	/**
	 * 处理成功
	 * 
	 * @param result
	 */
	protected abstract void handeResponeSuccess(Respone result);

	/**
	 * 处理失败
	 * 
	 * @param result
	 */
	protected abstract void handeResponeFaild(Respone result);

	/**
	 * 把reponse 的 message
	 * 
	 * @param message
	 */
	private void setServerMessage() {
		mMessage.getData().putString(BaseService.MESSAGE_FALG, mResultRespone.message);
	}

	/**
	 * 获取 handler 使用的message
	 * 
	 * @return
	 */
	public Message getMessage() {
		return mMessage;
	}

	/**
	 * 获取 handler 使用的message
	 * 
	 * @return
	 */
	public Message getMessage(int what) {
		mMessage.what = what;
		return mMessage;
	}

	/**
	 * 获取 handler 使用的message
	 * 
	 * @return
	 */
	public Message getMessage(int what, Object entry) {
		mMessage.what = what;
		mMessage.obj = entry;
		return mMessage;
	}

	/**
	 * 发送handler 消息
	 * 
	 * @param handler
	 * @param message
	 */
	public void sendHandlerMessage(Handler handler, Message message) {
		if (handler != null) {
			handler.sendMessage(message);
		}
	}
}
