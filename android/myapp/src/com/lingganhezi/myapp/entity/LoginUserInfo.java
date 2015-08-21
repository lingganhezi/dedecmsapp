package com.lingganhezi.myapp.entity;

public class LoginUserInfo {
	private UserInfo UserInfo;
	private String userId = "";
	private String password = "";

	public LoginUserInfo(String userid, String password) {
		setUserId(userid);
		setPassword(password);
	}

	public LoginUserInfo(UserInfo userInfo) {
		setUserInfo(userInfo);
	}

	public UserInfo getUserInfo() {
		return UserInfo;
	}

	public void setUserInfo(UserInfo userInfo) {
		UserInfo = userInfo;
		if (userInfo != null) {
			userId = userInfo.getId();
		}
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public String getUserId() {
		return userId;
	}

	public void setUserId(String userId) {
		this.userId = userId;
	}

}
