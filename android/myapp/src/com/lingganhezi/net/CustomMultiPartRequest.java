/**
 * Copyright 2013 Mani Selvaraj
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.lingganhezi.net;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;

import org.apache.http.entity.ContentType;

import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.Response.ErrorListener;
import com.android.volley.Response.Listener;
import com.android.volley.toolbox.HttpHeaderParser;

/**
 * 
 * @author chenzipeng
 *
 * @param <T>
 */
public abstract class  CustomMultiPartRequest<T> extends Request implements MultiPartRequest {

	protected final Listener<T> mListener;
	/* To hold the parameter name and the File to upload */
	private Map<String, File> fileUploads = new HashMap<String, File>();

	/* To hold the parameter name and the string content to upload */
	private Map<String, String> stringUploads = new HashMap<String, String>();

	/**
	 * Creates a new request with the given method.
	 *
	 * @param method
	 *            the request {@link Method} to use
	 * @param url
	 *            URL to fetch the string at
	 * @param listener
	 *            Listener to receive the String response
	 * @param errorListener
	 *            Error listener, or null to ignore errors
	 */
	public CustomMultiPartRequest(int method, String url, Listener<T> listener, ErrorListener errorListener) {
		super(method, url, errorListener);
		mListener = listener;
	}

	@Override
	public void addFileUpload(String param, File file) {
		fileUploads.put(param, file);
	}

	@Override
	public void addStringUpload(String param, String content) {
		stringUploads.put(param, content);
	}

	/**
	 * 要上传的文件
	 */
	@Override
	public Map<String, File> getFileUploads() {
		return fileUploads;
	}

	/**
	 * 要上传的参数
	 */
	@Override
	public Map<String, String> getStringUploads() {
		return stringUploads;
	}

	/**
	 * 空表示不上传
	 */
	@Override
	public String getBodyContentType() {
		return null;
	}

	@Override
	public ContentType getFileContentType() {
		return ContentType.APPLICATION_OCTET_STREAM;
	}
}