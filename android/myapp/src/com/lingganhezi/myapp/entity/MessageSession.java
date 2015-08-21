package com.lingganhezi.myapp.entity;

import com.lingganhezi.myapp.Constant;

public class MessageSession {
	private int id = Constant.MESSAGESESSION_ID_UNDIFINED;
	private String userid;

	public String getUserid() {
		return userid;
	}

	public void setUserid(String userid) {
		this.userid = userid;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

}
