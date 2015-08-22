package com.lingganhezi.myapp.service;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONException;
import com.android.volley.Request;
import com.android.volley.Response.Listener;
import com.google.gson.reflect.TypeToken;
import com.lingganhezi.myapp.AppContext;
import com.lingganhezi.myapp.Constant;
import com.lingganhezi.myapp.HttpHelper;
import com.lingganhezi.myapp.PlaceProvider.PlaceColumns;
import com.lingganhezi.myapp.R;
import com.lingganhezi.myapp.UserInfoProvider.UserInfoColumns;
import com.lingganhezi.myapp.entity.LoginUserInfo;
import com.lingganhezi.myapp.entity.Place;
import com.lingganhezi.myapp.entity.Respone;
import com.lingganhezi.myapp.entity.UserInfo;
import com.lingganhezi.myapp.net.ResultResponeListener;
import com.lingganhezi.net.FileUploader;
import com.lingganhezi.net.JsonArrayRequest;
import com.lingganhezi.net.JsonObjectRequest;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

/**
 * 用户信息 service
 * 
 * @author chenzipeng
 *
 */
public class UserService extends BaseService {
	private final String TAG = UserService.class.getSimpleName();

	private final static String URL_GET_USER_INFO = Constant.SERVER_ADD + "/app/userInfo.php";
	private final static String URL_UPDATE_USER_INFO = Constant.SERVER_ADD + "/app/userInfo.php";
	private final static String URL_UPLOAD_USER_AVATAR = Constant.SERVER_ADD + "/app/userInfo_edit_avatar.php";
	private final static String URL_FRIEND = Constant.SERVER_ADD + "/app/space.php";

	/**
	 * MSG 编号 200~300
	 */
	public final static int MSG_SYNC_USERINFO_SUCCESS = 200;
	public final static int MSG_SYNC_USERINFO_FAILD = 201;
	public final static int MSG_UPLOAD_USER_AVATR_SUCCESS = 210;
	public final static int MSG_UPLOAD_USER_AVATR_FAILD = 211;
	public final static int MSG_UPDATE_USERINFO_SUCCESS = 220;
	public final static int MSG_UPDATE_USERINFO_FAILD = 221;
	public final static int MSG_QUERY_UNFRIEND_SUCCESS = 230;
	public final static int MSG_QUERY_UNFRIEND_FAILD = 231;
	public final static int MSG_NEW_FRIEND_SUCCESS = 240;
	public final static int MSG_NEW_FRIEND_FAILD = 241;
	public final static int MSG_SYNC_FRIEND_SUCCESS = 250;
	public final static int MSG_SYNC_FRIEND_FAILD = 251;

	private static UserService instance;
	private LoginUserInfo mCurrentLoginUser;

	// TODO 放到其他地方
	/**
	 * 性别资源
	 */
	public static Map<Integer, Integer> SEX_NAME_MAP = new HashMap<Integer, Integer>();
	static {
		SEX_NAME_MAP.put(Constant.SEX_UNKONW, R.string.sex_unknow);
		SEX_NAME_MAP.put(Constant.SEX_MAN, R.string.sex_man);
		SEX_NAME_MAP.put(Constant.SEX_WOMEN, R.string.sex_women);
	}

	public static Integer[] SEX_ARRAY = SEX_NAME_MAP.keySet().toArray(new Integer[3]);

	public static Map<Integer, Integer> SEX_DRAWABLE_MAP = new HashMap<Integer, Integer>();
	static {
		SEX_DRAWABLE_MAP.put(Constant.SEX_UNKONW, R.drawable.sex_unkonw);
		SEX_DRAWABLE_MAP.put(Constant.SEX_MAN, R.drawable.sex_man);
		SEX_DRAWABLE_MAP.put(Constant.SEX_WOMEN, R.drawable.sex_women);
	}

	private UserService(Context context) {
		super(context);
	}

	private static UserService getInstance(Context context) {
		if (instance == null) {
			instance = new UserService(context);
		}
		return instance;
	}

	public static UserService getInstance() {
		return getInstance(AppContext.getInstance());
	}

	/**
	 * 设置当前登录用户
	 * 
	 * @param loginUserInfo
	 */
	public void setCurrentLoginUser(LoginUserInfo loginUserInfo) {
		mCurrentLoginUser = loginUserInfo;
		getServiceManager().getLoginService().saveLoginUserInfoConfig(loginUserInfo);
	}

	/**
	 * 获取当前登录用户的id
	 * 如果没有登录就返回一个空字符串
	 * @return
	 */
	public String getCurrentLoginUserId(){
		if(getCurrentLoginUser() != null){
			return getCurrentLoginUser().getUserId();
		}else{
			return "";
		}
	}
	/**
	 * 获取当前登陆用户
	 * 
	 * @return
	 */
	public LoginUserInfo getCurrentLoginUser() {
		return mCurrentLoginUser;
	}

	/**
	 * 同步获取服务器用户的信息
	 * 
	 * @return
	 */
	public void syncUserInfo(String userid, final Handler handler) {
		Log.i(TAG, "#syncUserInfo:" + userid);
		// 从网络获取
		Map<String, String> params = new HashMap<String, String>();
		params.put("action", "get");
		params.put("userid", userid);

		Request request = new JsonObjectRequest(URL_GET_USER_INFO, params, new ResultResponeListener() {

			@Override
			protected void handeResponeSuccess(Respone result) {
				UserInfo userinfo = (UserInfo) HttpHelper.getJsonObject(result.getData(), UserInfo.class);
				// TODO 保存信息到本地数据库
				saveUserInfoLocal(userinfo);
				sendHandlerMessage(handler, getMessage(MSG_SYNC_USERINFO_SUCCESS, userinfo));
			}

			@Override
			protected void handeResponeFaild(Respone result) {
				sendHandlerMessage(handler, getMessage(MSG_SYNC_USERINFO_FAILD));
			}

		}, getErrorListener(handler));

		getHttpRequesttQueue().add(request);
	}

	/**
	 * 保存用户信息并上传服务器
	 * 
	 * @param userinfo
	 */
	public void saveUserInfo(UserInfo userinfo) {
		saveUserInfo(userinfo, null);
	}

	/**
	 * 保存用户信息并上传服务器
	 * 
	 * @param userinfo
	 * @param handler
	 */
	public void saveUserInfo(final UserInfo userinfo, final Handler handler) {
		saveUserInfoLocal(userinfo);
		Log.i(TAG, "#saveUserInfo:" + userinfo.getId());
		Map<String, String> params = new HashMap<String, String>();
		params.put("action", "save");
		params.put("uname", userinfo.getName());
		params.put("birthday", userinfo.getBirthday());
		params.put("sex", String.valueOf(userinfo.getSex()));
		params.put("description", userinfo.getDescription());
		params.put("email", userinfo.getEmail());
		params.put("place", userinfo.getCity());

		Request request = new JsonObjectRequest(URL_UPDATE_USER_INFO, params, new ResultResponeListener() {

			@Override
			protected void handeResponeSuccess(Respone result) {
				sendHandlerMessage(handler, getMessage(MSG_UPDATE_USERINFO_SUCCESS));
			}

			@Override
			protected void handeResponeFaild(Respone result) {
				sendHandlerMessage(handler, getMessage(MSG_UPDATE_USERINFO_FAILD));
			}
		}, getErrorListener(handler));

		getHttpRequesttQueue().add(request);
	}

	/**
	 * 持久化 用户信息到本地
	 * 
	 * @param userinfo
	 */
	private void saveUserInfoLocal(UserInfo userinfo) {
		Uri uri = Uri.withAppendedPath(Constant.CONTENT_URI_USERINFO_PROVIDER, userinfo.getId());
		Cursor cursor = getContentResolver().query(uri, null, null, null, null);
		if (cursor != null) {
			try {
				ContentValues values = new ContentValues();
				values.put(UserInfoColumns._ID, userinfo.getId());
				values.put(UserInfoColumns.NAME, userinfo.getName());
				values.put(UserInfoColumns.EMAIL, userinfo.getEmail());
				values.put(UserInfoColumns.PORTRAIT, userinfo.getProtrait());
				values.put(UserInfoColumns.BIRTHDAY, userinfo.getBirthday());
				values.put(UserInfoColumns.CITY, userinfo.getCity());
				values.put(UserInfoColumns.DESCRIPTION, userinfo.getDescription());
				values.put(UserInfoColumns.SEX, userinfo.getSex());
				values.put(UserInfoColumns.ISFRIEND, userinfo.getIsFriend());

				if (cursor.getCount() > 0) {
					getContentResolver().update(uri, values, null, null);
				} else {
					getContentResolver().insert(Constant.CONTENT_URI_USERINFO_PROVIDER, values);
				}

			} catch (Exception e) {
				Log.e(TAG, "persistentUserInfo", e);
			} finally {
				cursor.close();
			}

		}
		// 启动保存信息到服务器
	}

	/**
	 * 获取userinfo实体
	 * 
	 * @param cursor
	 * @return
	 */
	public UserInfo getUserInfoEntry(Cursor cursor) {
		UserInfo userInfo = new UserInfo();
		userInfo.setId(cursor.getString(cursor.getColumnIndex(UserInfoColumns._ID)));
		userInfo.setEmail(cursor.getString(cursor.getColumnIndex(UserInfoColumns.EMAIL)));
		userInfo.setName(cursor.getString(cursor.getColumnIndex(UserInfoColumns.NAME)));
		userInfo.setProtrait(cursor.getString(cursor.getColumnIndex(UserInfoColumns.PORTRAIT)));
		userInfo.setBirthday(cursor.getString(cursor.getColumnIndex(UserInfoColumns.BIRTHDAY)));
		userInfo.setCity(cursor.getString(cursor.getColumnIndex(UserInfoColumns.CITY)));
		userInfo.setDescription(cursor.getString(cursor.getColumnIndex(UserInfoColumns.DESCRIPTION)));
		userInfo.setSex(cursor.getInt(cursor.getColumnIndex(UserInfoColumns.SEX)));
		userInfo.setIsFriend(cursor.getInt(cursor.getColumnIndex(UserInfoColumns.ISFRIEND)));
		return userInfo;
	}

	/**
	 * 获取 地区实体
	 * 
	 * @param cursor
	 * @return
	 */
	public Place getPlaceEntry(Cursor cursor) {
		Place place = new Place();
		place.setId(cursor.getInt(cursor.getColumnIndex(PlaceColumns._ID)));
		place.setReid(cursor.getInt(cursor.getColumnIndex(PlaceColumns.REID)));
		place.setName(cursor.getString(cursor.getColumnIndex(PlaceColumns.NAME)));
		place.setDisOrder(cursor.getInt(cursor.getColumnIndex(PlaceColumns.DISORDER)));
		return place;
	}

	/**
	 * 根据用户id获取实体
	 * 
	 * @param userid
	 * @return
	 */
	public UserInfo getUserInfo(String userid) {
		Uri uri = Uri.withAppendedPath(Constant.CONTENT_URI_USERINFO_PROVIDER, userid);
		Cursor cursor = getContentResolver().query(uri, null, null, null, null);
		UserInfo userInfo = null;
		if (cursor != null) {
			try {
				if (cursor.moveToFirst()) {
					userInfo = getUserInfoEntry(cursor);
				}
			} catch (Exception e) {
				// TODO: handle exception
			} finally {
				cursor.close();
			}
		}
		return userInfo;
	}

	/**
	 * 查询地区
	 * 
	 * @param id
	 * @return
	 */
	public Place getPlace(int id) {
		Uri uri = Uri.withAppendedPath(Constant.CONTENT_URI_PLACE_PROVIDER, String.valueOf(id));
		Cursor cursor = getContentResolver().query(uri, null, null, null, null);
		Place place = new Place();
		if (cursor != null) {
			try {
				if (cursor.moveToFirst()) {
					place = getPlaceEntry(cursor);
				}
			} catch (Exception e) {
			} finally {
				cursor.close();
			}
		}
		return place;
	};

	/**
	 * 根据地区id获取名称
	 * 
	 * @param id
	 * @return
	 */
	public String loadPlaceName(String id) {
		String name = null;
		try {
			// 尝试转换成int
			Integer.valueOf(id);
		} catch (Exception e) {
			return name;
		}
		Uri uri = Uri.withAppendedPath(Constant.CONTENT_URI_PLACE_PROVIDER, id);
		Cursor cursor = getContentResolver().query(uri, null, null, null, null);
		if (cursor != null) {
			try {
				if (cursor.moveToFirst()) {
					name = cursor.getString(cursor.getColumnIndex(PlaceColumns.NAME));
				}
			} catch (Exception e) {
				Log.e(TAG, "loadPlaceName", e);
			} finally {
				cursor.close();
			}
		}
		return name;
	}

	/**
	 * 上传头像 还没实现
	 * 
	 * @param avatarBitmap
	 * @param handler
	 */
	@Deprecated
	public void uploadAvatar(Bitmap avatarBitmap, final Handler handler) {
		Log.e(TAG, "uoloadAvatar(Bitmap avatarBitmap,final Handler handler)  is not implement");
	}

	/**
	 * 上传头像
	 *
	 * @param avatar
	 * @param handler
	 */
	public void uploadAvatar(File avatar, final Handler handler) {
		
		//检查是否登录
		if(!checkLogin(handler)){
			return;
		}
		
		final String userid = getCurrentLoginUser().getUserId();

		Map<String, File> files = new HashMap<String, File>();
		Map<String, String> params = new HashMap<String, String>();
		files.put("face", avatar);
		params.put("action", "save");
		params.put("userid", userid);

		FileUploader.getInstance(mContext).upload(URL_UPLOAD_USER_AVATAR, files, params, new ResultResponeListener() {

			@Override
			protected void handeResponeSuccess(Respone result) {
				Message msg = getMessage(MSG_UPLOAD_USER_AVATR_SUCCESS);
				String avatarPath = null;
				try {
					avatarPath = result.getData().getString("avatar");
				} catch (JSONException e) {
					Log.e(TAG, "uploadAvatar error", e);
					msg.what = MSG_UPLOAD_USER_AVATR_FAILD;
					sendHandlerMessage(handler, msg);
					return;
				}

				// 更新数据数据
				UserInfo userInfo = getUserInfo(userid);
				userInfo.setProtrait(avatarPath);
				saveUserInfoLocal(userInfo);

				// 更新正在登陆的用户数据
				LoginUserInfo loginUserInfo = getCurrentLoginUser();
				if (loginUserInfo == null || loginUserInfo.getUserId() != userid) {
					msg.what = MSG_UPLOAD_USER_AVATR_FAILD;
					msg.getData().putString(MESSAGE_FALG, mContext.getString(R.string.login_not_logined));
					sendHandlerMessage(handler, msg);
					return;
				}
				loginUserInfo.setUserInfo(userInfo);

				sendHandlerMessage(handler, msg);
			}

			@Override
			protected void handeResponeFaild(Respone result) {
				Message msg = handler.obtainMessage(MSG_UPLOAD_USER_AVATR_FAILD);
				msg.getData().putString(MESSAGE_FALG, result.message);
				handler.sendMessage(msg);
			}
		}, getErrorListener(handler), Constant.HTTP_CONTENTTYPE_IMAGE_JPEG);
	}

	/**
	 * 添加新朋友
	 * 
	 * @param userid
	 * @param handler
	 */
	public void addNewfriend(final String userid, final Handler handler) {
		Log.i(TAG, "#addNewfriend:" + userid);
		
		//检查登录
		if(!checkLogin(handler)){
			return;
		}
		
		Map<String, String> params = new HashMap<String, String>();
		params.put("action", "newfriend");
		params.put("userid", userid);

		Request request = new JsonObjectRequest(URL_FRIEND, params, new ResultResponeListener() {

			@Override
			protected void handeResponeSuccess(Respone result) {
				Message msg = getMessage(MSG_NEW_FRIEND_SUCCESS);
				// 更新用户状态
				UserInfo userInfo = getUserInfo(userid);
				if (userInfo != null) {
					userInfo.setIsFriend(1);
					saveUserInfo(userInfo);
				}
				sendHandlerMessage(handler, msg);
			}

			@Override
			protected void handeResponeFaild(Respone result) {
				sendHandlerMessage(handler, getMessage(MSG_NEW_FRIEND_FAILD));
			}

		}, getErrorListener(handler));

		getHttpRequesttQueue().add(request);
	}

	/**
	 * 查询服务器上 未关注的会员
	 * 
	 * </br>注意这个只能在主线程调用
	 * 
	 * @param username
	 * @param handler
	 */
	public void serachUnfriend(final String username, final Handler handler) {
		Map<String, String> params = new HashMap<String, String>();
		params.put("action", "listunfriend");
		params.put("uname", username);
		// 由于 listFirends封装了查询服务器上朋友列表 和非朋友列表的接口，所以这里 做个message转发
		listFriends(params, new Handler(new Handler.Callback() {

			@Override
			public boolean handleMessage(Message msg) {
				Message handleMessage = new Message();
				handleMessage.obj = msg.obj;
				handleMessage.setData(msg.getData());
				switch (msg.what) {
				case MSG_SYNC_FRIEND_SUCCESS:
					handleMessage.what = MSG_QUERY_UNFRIEND_SUCCESS;
					break;
				default:
					// 其他情况都为 失败
					handleMessage.what = MSG_QUERY_UNFRIEND_FAILD;
					break;
				}
				if (handler != null) {
					handler.sendMessage(handleMessage);
				}
				return true;
			}
		}));
	}

	/**
	 * 同步好友信息
	 * 
	 * @param handler
	 */
	public void syncFrieds(final Handler handler) {
		Map<String, String> params = new HashMap<String, String>();
		params.put("action", "listfriend");
		listFriends(params, handler);
	}

	/**
	 * 查询服务器好友
	 * 
	 * @param params
	 *            http请求参数
	 * @param handler
	 */
	public void listFriends(Map<String, String> params, final Handler handler) {
		//不检查登录

		Request request = new JsonArrayRequest(URL_FRIEND, params, new Listener<JSONArray>() {

			@Override
			public void onResponse(JSONArray json) {

				// 这里比较耗时 放入到 aysnctask中执行
				AsyncTask handleTask = new AsyncTask<JSONArray, Integer, android.os.Message>() {

					@Override
					protected android.os.Message doInBackground(JSONArray... params) {
						android.os.Message handlerMsg = null;
						try {
							JSONArray jsonarray = params[0];
							List<UserInfo> users = HttpHelper.getJsonArray(jsonarray, new TypeToken<List<UserInfo>>() {
							});
							// 这改成批量修改？提高性能,或者不需要保存？
							for (UserInfo user : users) {
								saveUserInfo(user);
							}

							handlerMsg = handler.obtainMessage(MSG_SYNC_FRIEND_SUCCESS);
							handlerMsg.obj = users;
						} catch (Exception e) {
							Log.e(TAG, "syncFriends error", e);
							handlerMsg = handler.obtainMessage(MSG_SYNC_FRIEND_FAILD);
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

		getHttpRequesttQueue().add(request);
	}
}
