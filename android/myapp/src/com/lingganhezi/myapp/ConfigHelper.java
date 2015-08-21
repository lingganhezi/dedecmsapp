package com.lingganhezi.myapp;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import com.lingganhezi.myapp.entity.LoginUserInfo;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;

public class ConfigHelper {
	private static ConfigHelper mInstance;
	private Context mContext;
	private final static String config_name = "config";

	private final String KEY_ALLOW_MOBILE_NETWORK = "ALLOW_MOBILE_NETWORK";
	private final String KEY_USERID = "USERID";
	private final String KEY_PASSWORD = "PASSWORD";
	private final String KEY_AUTO_LOGIN = "AUTO_LOGIN";
	private final String KEY_COOKIE = "COOKIE";
	SharedPreferences mConfig;

	private ConfigHelper(Context context) {
		mContext = context;
		mConfig = context.getSharedPreferences(config_name, Context.MODE_PRIVATE);
	}

	public static ConfigHelper getInstance() {
		return ConfigHelper.getInstance(AppContext.getInstance());
	}

	public static ConfigHelper getInstance(Context context) {
		if (mInstance == null) {
			mInstance = new ConfigHelper(context);
		}
		return mInstance;
	}

	public boolean isAllowMobileNetwork() {
		return mConfig.getBoolean(KEY_ALLOW_MOBILE_NETWORK, true);
	}

	public void setAllowMobileNetwork(boolean isAllow) {
		Editor editor = mConfig.edit();
		editor.putBoolean(KEY_ALLOW_MOBILE_NETWORK, isAllow);
		editor.commit();
	}

	/**
	 * 保存 登录信息配置
	 */
	public void saveLoginUserInfoConfig(LoginUserInfo loginUserInfo) {
		Editor editor = mConfig.edit();
		if (loginUserInfo == null) {
			editor.putBoolean(KEY_AUTO_LOGIN, false);
		} else {
			String userid = loginUserInfo.getUserId();
			String password = loginUserInfo.getPassword();
			editor.putString(KEY_USERID, userid);
			editor.putString(KEY_PASSWORD, password);
			editor.putBoolean(KEY_AUTO_LOGIN, true);
		}
		editor.commit();
	}

	/**
	 * 读取登录信息, 读取出来的 LoginUserInfo 里面User是空的
	 * 
	 * @return
	 */
	public LoginUserInfo loadLoginUserInfoConfig() {
		String userid = mConfig.getString(KEY_USERID, "");
		String password = mConfig.getString(KEY_PASSWORD, "");
		LoginUserInfo loginUserInfo = new LoginUserInfo(userid, password);
		return loginUserInfo;
	}

	/**
	 * 是否允许自动登录
	 * 
	 * @return
	 */
	public boolean isAllowAutoLogin() {
		return mConfig.getBoolean(KEY_AUTO_LOGIN, false);
	}

	/**
	 * 保存配置
	 * 
	 * @param key
	 * @param value
	 */
	private void saveConfig(String key, Serializable value) {
		Editor editor = mConfig.edit();
		if (value instanceof Boolean) {
			editor.putBoolean(key, (Boolean) value);

		} else if (value instanceof Integer) {
			editor.putInt(key, (Integer) value);

		} else if (value instanceof Float) {
			editor.putFloat(key, (Float) value);

		} else if (value instanceof Long) {
			editor.putLong(key, (Long) value);

		} else if (value instanceof String) {
			editor.putString(key, (String) value);

		} else if (value instanceof Set) {
			editor.putStringSet(key, (Set) value);
		}
		editor.commit();
	}

	/**
	 * 内存cookie缓存
	 */
	private Map<String, String> mCookies = new HashMap<String, String>();

	/**
	 * 清除cookie
	 */
	public void cleanCookie() {
		mCookies.clear();
		saveConfig(KEY_COOKIE, "");
	}

	/**
	 * 保存cookie
	 * 
	 * @param cookieMap
	 */
	public void saveCookie(Map<String, String> cookieMap) {
		mCookies.putAll(cookieMap);
		saveConfig(KEY_COOKIE, HttpHelper.converCookieToString(mCookies));
	}

	/**
	 * 获取Cookie
	 * 
	 * @return
	 */
	public String getCookie() {
		// 先判断内存缓存，不用每次都读 文件
		if (mCookies.size() > 0) {
			return HttpHelper.converCookieToString(mCookies);
		} else {
			// 第一次初始化
			String cookieString = mConfig.getString(KEY_COOKIE, "");
			mCookies = HttpHelper.converCookieStringToMap(cookieString);
			return cookieString;
		}
	}

}
