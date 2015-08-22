package com.lingganhezi.net;

import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONException;

import android.text.TextUtils;

import com.android.volley.AuthFailureError;
import com.android.volley.NetworkResponse;
import com.android.volley.ParseError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.Response.ErrorListener;
import com.android.volley.Response.Listener;
import com.android.volley.toolbox.HttpHeaderParser;
import com.lingganhezi.myapp.ConfigHelper;
import com.lingganhezi.myapp.HttpHelper;

public class JsonArrayRequest extends Request<JSONArray> {
	private Map<String, String> mParams;
	private Listener<JSONArray> mListener;

	public JsonArrayRequest(String url, Map<String, String> params, Listener<JSONArray> listener, ErrorListener errorListener) {
		super(Method.POST, url, errorListener);
		mListener = listener;
		mParams = params;
	}

	@Override
	protected Map<String, String> getParams() throws AuthFailureError {
		return mParams;
	}

	@Override
	protected Response<JSONArray> parseNetworkResponse(NetworkResponse response) {
		try {
			String jsonString = new String(response.data, HttpHeaderParser.parseCharset(response.headers));

			// 保存cookie
			ConfigHelper.getInstance().saveCookie(HttpHelper.getCookieMap(response.headers));

			return Response.success(new JSONArray(jsonString), HttpHeaderParser.parseCacheHeaders(response));
		} catch (UnsupportedEncodingException e) {
			return Response.error(new ParseError(e));
		} catch (JSONException je) {
			return Response.error(new ParseError(je));
		}
	}

	@Override
	protected void deliverResponse(JSONArray response) {
		mListener.onResponse(response);
	}

	@Override
	public Map<String, String> getHeaders() throws AuthFailureError {
		Map<String, String> headers = new HashMap<String, String>();
		String cookieStr = ConfigHelper.getInstance().getCookie();
		if (!TextUtils.isEmpty(cookieStr)) {
			headers.put("Cookie", cookieStr);
		}
		return headers;
	}
}
