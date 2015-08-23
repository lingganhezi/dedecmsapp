package com.lingganhezi.myapp.service;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.json.JSONArray;

import com.android.volley.Request;
import com.android.volley.Response;
import com.google.gson.reflect.TypeToken;
import com.lingganhezi.myapp.AppContext;
import com.lingganhezi.myapp.Constant;
import com.lingganhezi.myapp.HttpHelper;
import com.lingganhezi.myapp.MessageProvider.MessageColumns;
import com.lingganhezi.myapp.MessageSessionProvider.MessageSessionColumns;
import com.lingganhezi.myapp.entity.LoginUserInfo;
import com.lingganhezi.myapp.entity.Message;
import com.lingganhezi.myapp.entity.MessageSession;
import com.lingganhezi.myapp.entity.Respone;
import com.lingganhezi.myapp.net.ResultResponeListener;
import com.lingganhezi.myapp.service.handler.BaseServiceHandler;
import com.lingganhezi.myapp.service.handler.MessageQueryHandler;
import com.lingganhezi.myapp.service.handler.MessageSyncHandler;
import com.lingganhezi.net.JsonArrayRequest;
import com.lingganhezi.net.JsonObjectRequest;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Handler;
import android.support.v4.content.CursorLoader;
import android.text.TextUtils;
import android.util.Log;

public class MessageService extends BaseService {
	private final String TAG = MessageService.class.getSimpleName();
	private ContentResolver mContentResolver;
	private static MessageService mInstance;
	private final int mQueryCount = 20;

	private final String URL_LIST_MESSAGE = Constant.SERVER_ADD + "/app/pm.php";
	private final String URL_READ_MESSAGE = Constant.SERVER_ADD + "/app/pm.php";
	private final String URL_SEND_MESSAGE = Constant.SERVER_ADD + "/app/pm.php";
	private final String URL_DELETE_MESSAGE = Constant.SERVER_ADD + "/app/pm.php";

	public final static int MSG_SYNC_MESSAGE_SUCCESS = 400;
	public final static int MSG_SYNC_MESSAGE_FAILD = 401;
	public final static int MSG_SEND_MESSAGE_SUCCESS = 410;
	public final static int MSG_SEND_MESSAGE_FAILD = 411;
	public final static int MSG_QUERY_MESSAGE_SUCCESS = 420;
	public final static int MSG_QUERY_MESSAGE_FAILD = 421;

	private MessageService(Context context) {
		super(context);
		mContentResolver = context.getContentResolver();
	}

	public static MessageService getInstance() {
		return getInstance(AppContext.getInstance());
	}

	public static MessageService getInstance(Context context) {
		if (mInstance == null) {
			mInstance = new MessageService(context);
		}
		return mInstance;
	}

	/**
	 * 获取 查询数据的返回条数
	 * 
	 * @return
	 */
	public int getQueryCount() {
		return mQueryCount;
	}

	/**
	 * 获取MessionSession
	 * 
	 * @param sessionid
	 * @return
	 */
	public MessageSession getMessageSession(int sessionid) {
		Cursor cursor = queryMessageSession(sessionid);
		MessageSession session = null;
		if (cursor != null) {
			if (cursor.moveToFirst()) {
				session = getMessageSessionEntry(cursor);
			}
			cursor.close();
		}
		return session;
	}

	/**
	 * 转换成实体
	 * 
	 * @param cursor
	 * @return
	 */
	public MessageSession getMessageSessionEntry(Cursor cursor) {
		// TODO 实现转换
		MessageSession session = new MessageSession();
		session.setId(cursor.getInt(cursor.getColumnIndex(MessageSessionColumns._ID)));
		session.setUserid(cursor.getString(cursor.getColumnIndex(MessageSessionColumns.USERID)));
		session.setOwner(cursor.getString(cursor.getColumnIndex(MessageSessionColumns.OWNER)));
		return session;
	}

	public Cursor queryMessageSession() {
		// TODO 实现查询
		String orderBy = MessageSessionColumns._ID + " DESC LIMIT " + mQueryCount;
		Cursor cursor = mContentResolver.query(Constant.CONTENT_URI_MESSAGE_SESSION_PROVIDER, null, MessageSessionColumns.OWNER + "=?",
				new String[] { UserService.getInstance().getCurrentLoginUserId() }, orderBy);
		return cursor;
	}

	public Cursor queryMessageSession(int sessionid) {
		Cursor cursor = mContentResolver.query(
				Uri.withAppendedPath(Constant.CONTENT_URI_MESSAGE_SESSION_PROVIDER, String.valueOf(sessionid)), null, null, null, null);
		return cursor;
	}

	/**
	 * 是否存在比这个时间点更前的消息
	 * 
	 * @param time
	 * @param sessionid
	 * @return
	 */
	private boolean hasPreMessage(long time, int sessionid) {
		String orderBy = MessageColumns.SENDTIME + " ASC ";
		Cursor cursor = mContentResolver.query(Constant.CONTENT_URI_MESSAGE_PROVIDER, null, MessageColumns.SENDTIME + "<? AND "
				+ MessageColumns.SESSIONID + "=? AND " + MessageSessionColumns.OWNER + "=?",
				new String[] { String.valueOf(time), String.valueOf(sessionid), UserService.getInstance().getCurrentLoginUserId() },
				orderBy);
		boolean hasPre = cursor.getCount() > 0;
		cursor.close();
		return hasPre;
	}

	/**
	 * 获取 这个时间点 往前推20条消息的发送时间
	 * 
	 * @param time
	 * @param sessionid
	 * @return
	 */
	private long getPreMessageSendTime(final long time, int sessionid) {
		// 查询往前20条消息的id
		String orderby = MessageColumns.SENDTIME + " DESC LIMIT " + mQueryCount;
		Cursor c = mContentResolver.query(Constant.CONTENT_URI_MESSAGE_PROVIDER, null, MessageColumns.SENDTIME + "<? AND "
				+ MessageColumns.SESSIONID + "=?", new String[] { String.valueOf(time), String.valueOf(sessionid) }, orderby);
		// 往前推20条的发送时间
		long preSendTime = 0;
		if (c.moveToLast()) {
			preSendTime = c.getLong(c.getColumnIndex(MessageColumns.SENDTIME));
		}
		c.close();
		return preSendTime;
	}

	/**
	 * 获取消息列表loader </br>只会加载最后20条
	 * 
	 * @param sessionid
	 * @return
	 */
	public CursorLoader getMessageLoader(int sessionid) {
		long preSendTime = getPreMessageSendTime(System.currentTimeMillis(), sessionid);

		String orderby = MessageColumns.SENDTIME + " ASC";
		return new CursorLoader(mContext, Constant.CONTENT_URI_MESSAGE_PROVIDER, null, MessageColumns.SENDTIME + ">=? AND "
				+ MessageColumns.SESSIONID + "=?", new String[] { String.valueOf(preSendTime), String.valueOf(sessionid) }, orderby);
	}

	/**
	 * 查询 这个时间点 往前推 20条消息的数据（包括 20条之后的数据）
	 * 
	 * @param time
	 * @param sessionid
	 * @return
	 */
	private Cursor queryPreMessage(final long time, int sessionid) {

		long preSendTime = getPreMessageSendTime(time, sessionid);

		String orderby = MessageColumns.SENDTIME + " ASC";
		return mContentResolver.query(Constant.CONTENT_URI_MESSAGE_PROVIDER, null, MessageColumns.SENDTIME + ">=? AND "
				+ MessageColumns.SESSIONID + "=?", new String[] { String.valueOf(preSendTime), String.valueOf(sessionid) }, orderby);
	}

	/**
	 * 把时间点往前推，直到查询到 有20数据以后，在handler中返回 包括20条消息以后的消息。
	 * 
	 * @param time
	 *            发送时间
	 * @param sessionid
	 * @param handler
	 *            如果 查询到 cursor.getCount() == 0那么 去拉去服务器的数据，并通知 handler，这个时候
	 */
	public void queryMessageByTime(final long time, final int sessionid, final MessageQueryHandler handler) {
		if (hasPreMessage(time, sessionid)) {
			// 没有查询到时到服务器上去拉取数据
			// 获取本地最后一个发送时间最前的 msgid
			String qOrderBy = MessageColumns.SENDTIME + " DESC LIMIT 1";
			Cursor c = mContentResolver.query(Constant.CONTENT_URI_MESSAGE_PROVIDER, null, MessageColumns.MSGID + "!=?",
					new String[] { String.valueOf(Constant.MESSAGE_ID_UNDIFINED) }, qOrderBy);

			String id = null;
			if (c.moveToFirst()) {
				id = c.getString(c.getColumnIndex(MessageColumns.MSGID));
			}
			c.close();

			final String lastid = id;
			// 同步服务器消息
			syncMessage(lastid, new MessageSyncHandler(lastid, new BaseServiceHandler.Callback<String>() {
				@Override
				public void complate(boolean success, String msgid, String message) {
					if (success) {
						// 同步完成后 查询本地消息并返回
						Cursor c = queryPreMessage(time, sessionid);
						android.os.Message handlerMsg = new android.os.Message();
						handlerMsg.what = MSG_QUERY_MESSAGE_SUCCESS;
						handlerMsg.obj = c;
						handler.sendMessage(handlerMsg);
					} else {
						// 失败了发送查询失败的消息
						handler.obtainMessage(MSG_QUERY_MESSAGE_FAILD);
					}
				}
			}));
		} else {
			android.os.Message handlerMsg = new android.os.Message();
			handlerMsg.what = MSG_QUERY_MESSAGE_SUCCESS;
			handlerMsg.obj = queryPreMessage(time, sessionid);
			handler.sendMessage(handlerMsg);
		}
	}

	/**
	 * 拉取服务器最新的消息
	 * 
	 * @param handler
	 */
	public void syncMessage(final Handler handler) {
		syncMessage(null, handler);
	}

	/**
	 * 拉取服务器的msgid 之前的消息
	 * 
	 * @param msgid
	 *            msgid == null 时从服务器上拉取所有消息
	 * @param handler
	 */
	public void syncMessage(final String msgid, final Handler handler) {
		//检查登录
		if(!checkLogin(handler)){
			return;
		}
		
		Map<String, String> params = new HashMap<String, String>();
		params.put("action", "list");
		// TODO 不拉取所有列表只拉取 某个msgid 以后的 数据
		if (TextUtils.isEmpty(msgid)) {
			params.put("all", "true");
		} else {
			params.put("msgid", msgid);
		}

		Request request = new JsonArrayRequest(URL_LIST_MESSAGE, params, new Response.Listener<JSONArray>() {

			@Override
			public void onResponse(JSONArray json) {
				// 这里比较耗时 放入到 aysnctask中执行
				AsyncTask handleTask = new AsyncTask<JSONArray, Integer, android.os.Message>() {

					@Override
					protected android.os.Message doInBackground(JSONArray... params) {
						android.os.Message handlerMsg = null;
						try {
							JSONArray jsonarray = params[0];
							List<Message> messages = HttpHelper.getJsonArray(jsonarray, new TypeToken<List<Message>>() {
							});
							saveServerMessages(messages);
							handlerMsg = handler.obtainMessage(MSG_SYNC_MESSAGE_SUCCESS);
						} catch (Exception e) {
							Log.e(TAG, "syncMessage error:" + msgid, e);
							handlerMsg = handler.obtainMessage(MSG_SYNC_MESSAGE_FAILD);
						}
						return handlerMsg;
					}

					@Override
					protected void onPostExecute(android.os.Message result) {
						result.sendToTarget();
						super.onPostExecute(result);
					}
				};

				handleTask.execute(new JSONArray[] { json });
			}
		}, getErrorListener(handler));
		request.setShouldCache(false);
		getHttpRequesttQueue().add(request);
	}

	/**
	 * 获取消息的contentvalues
	 * 
	 * @param msg
	 * @return
	 */
	private ContentValues getMessageContentValues(Message msg) {
		// 获取messageSessionid
		final int sessionid = getMessageSessionId(msg);
		msg.setSessionid(sessionid);

		ContentValues values = new ContentValues();

		values.put(MessageColumns.MSGID, msg.getMsgid());
		values.put(MessageColumns.FLOGINID, msg.getFloginid());
		values.put(MessageColumns.TOLOGINID, msg.getTologinid());
		values.put(MessageColumns.FOLDER, msg.getFolder());
		values.put(MessageColumns.SUBJECT, msg.getSubject());
		values.put(MessageColumns.SENDTIME, msg.getSendtime());
		values.put(MessageColumns.WRITETIME, msg.getWritetime());
		values.put(MessageColumns.HASVIEW, msg.getHasview());
		values.put(MessageColumns.ISADMIN, msg.getIsadmin());
		values.put(MessageColumns.MESSAGE, msg.getMessage());
		values.put(MessageColumns.STATE, msg.getState());
		values.put(MessageColumns.SESSIONID, msg.getSessionid());
		return values;
	}

	/**
	 * 保存服务器的消息到本地数据库
	 * 
	 * @param messages
	 */
	public void saveServerMessages(Collection<Message> messages) {
		// 插入数据到数据库
		for (Message msg : messages) {
			msg.setState(Constant.MESSAGE_STATE_SENED);
			// 根据 msgid 判断是否应该插入消息
			Cursor c = mContentResolver.query(Constant.CONTENT_URI_MESSAGE_PROVIDER, null, MessageColumns.MSGID + "=?",
					new String[] { String.valueOf(msg.getMsgid()) }, null);
			if (c != null && c.getCount() == 0) {
				ContentValues values = getMessageContentValues(msg);
				mContentResolver.insert(Constant.CONTENT_URI_MESSAGE_PROVIDER, values);
			}
			c.close();
		}
	}

	/**
	 * 保存 消息
	 * 
	 * @param msg
	 * @return
	 */
	public int saveMessage(Message msg) {
		ContentValues values = getMessageContentValues(msg);
		int id = Constant.MESSAGE_ID_UNDIFINED;

		if (msg.getId() != Constant.MESSAGE_ID_UNDIFINED) {// 更新
			mContentResolver.update(Uri.withAppendedPath(Constant.CONTENT_URI_MESSAGE_PROVIDER, String.valueOf(msg.getId())), values, null,
					null);
			id = msg.getId();
		} else {
			Uri uri = mContentResolver.insert(Constant.CONTENT_URI_MESSAGE_PROVIDER, values);
			try {
				id = Integer.valueOf(uri.getPathSegments().get(0));
			} catch (Exception e) {
				Log.e(TAG, "insertMessage", e);
			}
		}
		return id;
	}

	/**
	 * 获取这个用户对应的 sessionid，如果没有就会新增一条session记录
	 * 
	 * @param userid
	 * @return
	 */
	public int getMessageSessionId(String userid) {
		Message msg = buildSendMessage(userid, null);
		return getMessageSessionId(msg);
	}

	/**
	 * 获取这个消息的 sessionid，如果本地没有这个session，就会创建一个 session 数据插入到本地
	 * 
	 * @param msg
	 * @return
	 */
	private int getMessageSessionId(Message msg) {

		if (msg.getSessionid() != Constant.MESSAGESESSION_ID_UNDIFINED) {
			return msg.getSessionid();
		}

		int sessionid = Constant.MESSAGESESSION_ID_UNDIFINED;
		// 处理messageSession
		String targetUserId;
		if (Constant.MESSAGE_BOX_INBOX.equals(msg.getFolder())) {
			targetUserId = String.valueOf(msg.getFloginid());
		} else {
			targetUserId = String.valueOf(msg.getTologinid());
		}
		// 获取当前登录用户id
		String currentUserId = UserService.getInstance().getCurrentLoginUserId();
		
		Cursor sessionCursor = mContentResolver.query(Constant.CONTENT_URI_MESSAGE_SESSION_PROVIDER, null, MessageSessionColumns.USERID
				+ "=? AND "+MessageSessionColumns.OWNER+"=?", new String[] { targetUserId,currentUserId }, null);
		if (sessionCursor != null) {
			if (sessionCursor.getCount() == 0) {

				
				ContentValues values = new ContentValues();
				values.put(MessageSessionColumns.USERID, targetUserId);
				values.put(MessageSessionColumns.OWNER, currentUserId);
				Uri resultUri = mContentResolver.insert(Constant.CONTENT_URI_MESSAGE_SESSION_PROVIDER, values);
				if (resultUri != null) {
					try {
						sessionid = Integer.valueOf(resultUri.getPathSegments().get(0));
					} catch (Exception e) {
						Log.w(TAG, "getMessageSessionId parse insert uri " + resultUri);
					}
				}
			} else {
				if (sessionCursor.moveToFirst()) {
					sessionid = sessionCursor.getInt(sessionCursor.getColumnIndex(MessageSessionColumns._ID));
				}
			}
			sessionCursor.close();
		}
		return sessionid;
	}

	/**
	 * 转换 消息实体
	 * 
	 * @param cursor
	 * @return
	 */
	public Message getMessageEntry(Cursor cursor) {
		Message msg = new Message();
		msg.setId(cursor.getInt(cursor.getColumnIndex(MessageColumns._ID)));
		msg.setMsgid(cursor.getInt(cursor.getColumnIndex(MessageColumns.MSGID)));
		msg.setFloginid(cursor.getString(cursor.getColumnIndex(MessageColumns.FLOGINID)));
		msg.setTologinid(cursor.getString(cursor.getColumnIndex(MessageColumns.TOLOGINID)));
		msg.setFolder(cursor.getString(cursor.getColumnIndex(MessageColumns.FOLDER)));
		msg.setSubject(cursor.getString(cursor.getColumnIndex(MessageColumns.SUBJECT)));
		msg.setSendtime(cursor.getInt(cursor.getColumnIndex(MessageColumns.SENDTIME)));
		msg.setWritetime(cursor.getInt(cursor.getColumnIndex(MessageColumns.WRITETIME)));
		msg.setHasview(cursor.getInt(cursor.getColumnIndex(MessageColumns.HASVIEW)));
		msg.setIsadmin(cursor.getInt(cursor.getColumnIndex(MessageColumns.ISADMIN)));
		msg.setState(cursor.getInt(cursor.getColumnIndex(MessageColumns.STATE)));
		msg.setMessage(cursor.getString(cursor.getColumnIndex(MessageColumns.MESSAGE)));
		return msg;
	}

	/**
	 * 获取会话最后一条消息
	 * 
	 * @param messageSession
	 * @return
	 */
	public Message getLastMessage(MessageSession messageSession) {
		Message msg = null;
		int sessionid = messageSession.getId();
		String order = MessageColumns.SENDTIME + " DESC LIMIT 1";
		Cursor cursor = mContentResolver.query(Constant.CONTENT_URI_MESSAGE_PROVIDER, null, MessageColumns.SESSIONID + "=?",
				new String[] { String.valueOf(sessionid) }, order);
		try {
			if (cursor.moveToFirst()) {
				msg = getMessageEntry(cursor);
			}
		} catch (Exception e) {
			Log.w(TAG, e);
		} finally {
			cursor.close();
		}
		return msg;
	}

	/**
	 * 获取本地数据库时间点最前的一条已经发送或者接收的服务器消息
	 * 
	 * @return
	 */
	public Message getTopServerMessage() {
		String orderBy = MessageColumns.SENDTIME + " DESC LIMIT 1";
		Cursor cursor = mContentResolver.query(Constant.CONTENT_URI_MESSAGE_PROVIDER, null, MessageColumns.MSGID + "!=?",
				new String[] { String.valueOf(Constant.MESSAGE_ID_UNDIFINED) }, orderBy);
		Message message = null;
		if (cursor != null && cursor.moveToFirst()) {
			message = getMessageEntry(cursor);
			cursor.close();
		}
		return message;
	}

	/**
	 * 发送消息
	 * 
	 * @param targetUserid
	 *            对方id
	 * @param msg
	 *            消息内容
	 * @param handler
	 */
	public void sendMessage(Message msg, final Handler handler) {
		
		//检查是否登录
		if(!checkLogin(handler)){
			return;
		}
				
		// 插入到本地数据库
		msg.setState(Constant.MESSAGE_STATE_SENDING);
		int id = saveMessage(msg);
		msg.setId(id);
		final Message messageEntry = msg;

		Map<String, String> params = new HashMap<String, String>();
		params.put("action", "send");
		params.put("msgtoid", msg.getTologinid() == null ? new String() : msg.getTologinid());
		params.put("message", msg.getMessage() == null ? new String() : msg.getMessage());
		Request request = new JsonObjectRequest(URL_SEND_MESSAGE, params, new ResultResponeListener() {

			@Override
			protected void handeResponeSuccess(Respone result) {
				android.os.Message handlerMessage = getMessage();
				try {
					// 保存从服务器返回来的消息实体
					Message retrunMessage = (Message) HttpHelper.getJsonObject(result.getData(), Message.class);
					messageEntry.setMsgid(retrunMessage.getMsgid());
					messageEntry.setFolder(retrunMessage.getFolder());
					messageEntry.setSendtime(retrunMessage.getSendtime());
					messageEntry.setSubject(retrunMessage.getSubject());
					messageEntry.setHasview(retrunMessage.getHasview());
					messageEntry.setState(Constant.MESSAGE_STATE_SENED);

					handlerMessage.what = MSG_SEND_MESSAGE_SUCCESS;
				} catch (Exception e) {
					Log.e(TAG, "send message  parse msgid error" + messageEntry);
					handlerMessage.what = MSG_SEND_MESSAGE_FAILD;
				}
				saveMessage(messageEntry);
				handler.sendMessage(handlerMessage);
			}

			@Override
			protected void handeResponeFaild(Respone result) {
				android.os.Message handlerMessage = getMessage(MSG_SEND_MESSAGE_FAILD);
				messageEntry.setState(Constant.MESSAGE_STATE_FAILD);
				Log.e(TAG, "send message error:" + messageEntry);
				saveMessage(messageEntry);
				handler.sendMessage(handlerMessage);
			}
		}, getErrorListener(handler));
		request.setShouldCache(false);
		getHttpRequesttQueue().add(request);
	}

	/**
	 * 创建发送的消息
	 * 
	 * @param targetUserId
	 * @param msg
	 * @return
	 */
	public Message buildSendMessage(String targetUserId, String msg) {
		Message message = new Message();
		message.setFloginid(UserService.getInstance().getCurrentLoginUserId());
		message.setTologinid(targetUserId);
		message.setMessage(msg);
		message.setFolder(Constant.MESSAGE_BOX_OUTBOX);
		message.setState(Constant.MESSAGE_STATE_SENDING);
		message.setSendtime(System.currentTimeMillis());
		return message;
	}
}
