package com.lingganhezi.myapp.entity;

import java.util.Map;

import org.json.JSONObject;

public class Respone {
	public int stateCode;
	public String message;
	private Map data;

	private JSONObject dataJson;

	public JSONObject getData() {
		if (dataJson == null) {
			if (data == null) {
				dataJson = new JSONObject();
			} else {
				dataJson = new JSONObject(data);
			}
		}
		return dataJson;
	}
}
