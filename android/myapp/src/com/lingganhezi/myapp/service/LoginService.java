package com.lingganhezi.myapp.service;

import java.util.HashMap;
import java.util.Map;

import org.json.JSONObject;

import com.android.volley.Request;
import com.lingganhezi.myapp.AppContext;
import com.lingganhezi.myapp.ConfigHelper;
import com.lingganhezi.myapp.Constant;
import com.lingganhezi.myapp.HttpHelper;
import com.lingganhezi.myapp.entity.LoginUserInfo;
import com.lingganhezi.myapp.entity.Respone;
import com.lingganhezi.myapp.entity.UserInfo;
import com.lingganhezi.myapp.net.ResultResponeListener;
import com.lingganhezi.net.JsonObjectRequest;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;

public class LoginService extends BaseService {
	private final String TAG = LoginService.class.getSimpleName();
	private static LoginService instance;
	/**
	 * MSG 编号 100~200
	 */
	public final static int MSG_LOGIN_SUCCESS = 100;
	public final static int MSG_LOGIN_FAILD = 101;
	public final static int MSG_REGISTE_SUCCESS = 110;
	public final static int MSG_REGISTE_FAILD = 111;
	public final static int MSG_FORGOTPASSWORD_SUCCESS = 120;
	public final static int MSG_FORGOTPASSWORD_FAILD = 121;
	public final static int MSG_LOGOUT_SUCCESS = 131;
	// api地址
	private final String LOGIN_URL = Constant.SERVER_ADD + "/app/login.php";
	private final String REGISTE_URL = Constant.SERVER_ADD + "/app/registe.php";
	private final String FORGOTPASSWORD_URL = Constant.SERVER_ADD + "/app/forgotpassword.php";

	private LoginService(Context context) {
		super(context);
	}

	private static LoginService getInstance(Context context) {
		if (instance == null) {
			instance = new LoginService(context);
		}
		return instance;
	}

	public static LoginService getInstance() {
		return getInstance(AppContext.getInstance());
	}

	/**
	 * 是否已经登陆
	 * 
	 * @return
	 */
	public boolean isLogined() {
		return getServiceManager().getUserService().getCurrentLoginUser() != null;
	}

	/**
	 * 登录 在调用登录后，会再去调用获取用户信息
	 * 
	 * @param username
	 * @param password
	 * @param handler
	 *            用来接收回调消息用的handler
	 */
	public void login(final String username, final String password, final Handler handler) {
		Log.i(TAG, "#login:" + username);

		Map<String, String> params = new HashMap<String, String>();
		params.put("action", "login");
		params.put("username", username);
		params.put("password", password);

		Request request = new JsonObjectRequest(LOGIN_URL, params, new ResultResponeListener() {

			@Override
			protected void handeResponeSuccess(Respone result) {
				//登录成功后，后台更新好友列表信息
				UserService.getInstance().syncFrieds(null);
				
				final Message msg = getMessage(MSG_LOGIN_SUCCESS);

				//更新当前登录用户的个人信息
				getServiceManager().getUserService().syncUserInfo(username, new Handler(new Handler.Callback() {

					@Override
					public boolean handleMessage(Message syncmsg) {
						switch (syncmsg.what) {
						case UserService.MSG_SYNC_USERINFO_SUCCESS:
							UserInfo userInfo = (UserInfo) syncmsg.obj;
							LoginUserInfo loginUserInfo = new LoginUserInfo(userInfo);
							loginUserInfo.setPassword(password);

							getServiceManager().getUserService().setCurrentLoginUser(loginUserInfo);
							break;

						default:
							break;
						}
						// TODO 需要处理同步信息发生的错误？
						sendHandlerMessage(handler, msg);
						return true;
					}
				}));
			}

			@Override
			protected void handeResponeFaild(Respone result) {
				sendHandlerMessage(handler, getMessage(MSG_LOGIN_FAILD));
			}

		}, getErrorListener(handler));

		getHttpRequesttQueue().add(request);
	}

	/**
	 * 注销
	 * 
	 * @param handler
	 *            用来接收回调消息用的handler
	 */
	public void logout(final Handler handler) {
		if (!isLogined()) {
			handler.sendMessage(handler.obtainMessage(MSG_ERROR, new Exception("you has not login!")));
		} else {
			Log.i(TAG, "#logout");
			// TODO 注销,需要调用服务器接口？
			getServiceManager().getUserService().setCurrentLoginUser(null);
			ConfigHelper.getInstance().cleanCookie();
			//清空好友关系
			UserService.getInstance().clearFreindRelationship();
			handler.sendMessage(handler.obtainMessage(MSG_LOGOUT_SUCCESS));
		}
	}

	/**
	 * 忘记密码，调用远程忘记密码发送验证邮件
	 * 
	 * @param emial
	 * @param handler
	 */
	public void forgotpassword(String emial, final Handler handler) {
		Map<String, String> params = new HashMap<String, String>();
		params.put("email", emial);

		Request request = new JsonObjectRequest(FORGOTPASSWORD_URL, params, new ResultResponeListener() {

			@Override
			protected void handeResponeSuccess(Respone result) {
				sendHandlerMessage(handler, getMessage(MSG_FORGOTPASSWORD_SUCCESS));
			}

			@Override
			protected void handeResponeFaild(Respone result) {
				sendHandlerMessage(handler, getMessage(MSG_FORGOTPASSWORD_FAILD));
			}
		}, getErrorListener(handler));

		getHttpRequesttQueue().add(request);
	}

	/**
	 * 注册
	 * 
	 * @param email
	 * @param password
	 * @param handler
	 */
	public void registe(String email, String password, final Handler handler) {
		Log.i(TAG, "#registe:" + email);
		Map<String, String> params = new HashMap<String, String>();
		params.put("email", email);
		params.put("password", password);

		Request request = new JsonObjectRequest(REGISTE_URL, params, new ResultResponeListener() {

			@Override
			protected void handeResponeSuccess(Respone result) {
				sendHandlerMessage(handler, getMessage(MSG_REGISTE_SUCCESS));
			}

			@Override
			protected void handeResponeFaild(Respone result) {
				sendHandlerMessage(handler, getMessage(MSG_REGISTE_FAILD));
			}

		}, getErrorListener(handler));

		getHttpRequesttQueue().add(request);
	}

	public void saveLoginUserInfoConfig(LoginUserInfo loginUserInfo) {
		ConfigHelper.getInstance().saveLoginUserInfoConfig(loginUserInfo);
	}

	/**
	 * 读取登录信息, 读取出来的 LoginUserInfo 里面User是空的
	 * 
	 * @return
	 */
	public LoginUserInfo loadLoginUserInfoConfig() {
		return ConfigHelper.getInstance().loadLoginUserInfoConfig();
	}

	/**
	 * 自动登录 读取 缓存的配置文件然后登录
	 * 
	 * @param handler
	 *            ,完成后登录事件处理
	 */
	public void autoLogin() {
		if (!isAllowAutoLogin()) {
			return;
		}
		LoginUserInfo loginConfig = loadLoginUserInfoConfig();
		if (!TextUtils.isEmpty(loginConfig.getUserId())) {
			// 因为本地有数据先运行 “离线登陆”
			final UserService userService = getServiceManager().getUserService();
			UserInfo userInfo = userService.getUserInfo(loginConfig.getUserId());
			if(userInfo == null){
				//当本地数据库没有找到这个用户数据的时候，不自动登录
				return;
			}
			
			loginConfig.setUserInfo(userInfo);
			userService.setCurrentLoginUser(loginConfig);

			// 下面真正验证登陆
			Log.i(TAG, "#autoLogin");
			login(loginConfig.getUserId(), loginConfig.getPassword(), new Handler(new Handler.Callback() {

				@Override
				public boolean handleMessage(Message msg) {
					switch (msg.what) {
					case MSG_LOGIN_FAILD:// 登陆失败
						userService.setCurrentLoginUser(null);
						break;
					case MSG_ERROR:
						// TODO 其他错误 需要处理？比如解析json
						break;
					default:
						break;
					}
					return false;
				}
			}));
		}
	}

	/**
	 * 是否允许自动登录
	 * 
	 * @return
	 */
	public boolean isAllowAutoLogin() {
		return ConfigHelper.getInstance().isAllowAutoLogin();
	}
}
