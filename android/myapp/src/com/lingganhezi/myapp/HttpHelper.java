package com.lingganhezi.myapp;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONObject;

import android.text.TextUtils;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

public class HttpHelper {
	private final static String TAG = HttpHelper.class.getSimpleName();
	public static Gson gson = new Gson();

	public static String parserParameters(Map<String, String> params) {
		StringBuilder paramsStr = new StringBuilder();
		for (Map.Entry<String, String> entry : params.entrySet()) {
			paramsStr.append(entry.getKey());
			paramsStr.append('=');
			paramsStr.append(entry.getValue());
			paramsStr.append('&');
		}
		if (paramsStr.length() > 0) {
			paramsStr.deleteCharAt(paramsStr.length() - 1);
		}
		return paramsStr.toString();
	}

	public static Object getJsonObject(JSONObject json, Class clazz) {
		return gson.fromJson(json.toString(), clazz);
	}

	public static List getJsonArray(JSONArray json, TypeToken token) {
		return gson.fromJson(json.toString(), token.getType());
	}

	/**
	 * 获取 header 的中cookie
	 * 
	 * @param headers
	 * @return
	 */
	public static Map<String, String> getCookieMap(Map<String, String> headers) {
		final String cookieKey = "Set-Cookie";
		if (headers.containsKey(cookieKey)) {
			return parseCookieString(headers.get(cookieKey));

		} else {
			return new HashMap<String, String>();
		}

	}

	/**
	 * 解析header 特殊封装jsonArray格式的 cookie字符串
	 * 
	 * @param cookieString
	 * 
	 * @return
	 */
	public static Map<String, String> parseCookieString(String cookieJsonArrayStr) {
		JSONArray cookieJsonArray = new JSONArray();
		Map<String, String> cookieMap = new HashMap<String, String>();
		try {

			cookieJsonArray = new JSONArray(cookieJsonArrayStr);

			for (int i = 0; i < cookieJsonArray.length(); i++) {
				String cookieStr = cookieJsonArray.getString(i);
				String[] cookieStrs = cookieStr.split(";");
				for (int j = 0; j < cookieStrs.length; j++) {
					String[] onecookie = cookieStrs[j].split("=");
					cookieMap.put(onecookie[0], onecookie[1]);
				}
			}
		} catch (Exception e) {
			Log.e(TAG, "parseCookieString", e);
		}

		return cookieMap;
	}

	/**
	 * 根据一个map返回cookie字符串 </br>格式: a=b;c=d
	 * 
	 * @param cookieMap
	 * @return
	 */
	public static String converCookieToString(Map<String, String> cookieMap) {
		StringBuffer sb = new StringBuffer();
		for (String key : cookieMap.keySet()) {
			sb.append(key);
			sb.append("=");
			sb.append(cookieMap.get(key));
			sb.append(";");
		}
		return sb.toString();
	}

	/**
	 * 根据 cookie字符串 生成 一个map </br>格式: a=b;c=d
	 * 
	 * @param cookieMap
	 * @return
	 */
	public static Map<String, String> converCookieStringToMap(String cookieMapString) {

		if (TextUtils.isEmpty(cookieMapString)) {
			return new HashMap<String, String>();
		}

		Map<String, String> cookieMap = new HashMap<String, String>();
		String[] cookieStrs = cookieMapString.split(";");
		for (int i = 0; i < cookieStrs.length; i++) {
			try {
				String[] onecookie = cookieStrs[i].split("=");
				cookieMap.put(onecookie[0], onecookie[1]);
			} catch (Exception e) {
				Log.w("converCookieStringToMap", e.getMessage());
			}
		}

		return cookieMap;
	}
}
