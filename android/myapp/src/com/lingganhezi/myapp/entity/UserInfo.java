package com.lingganhezi.myapp.entity;

/**
 * 用户信息
 * 
 * @author chenzipeng
 *
 */
public class UserInfo {
	private String id;
	private String name;
	private String email;
	private String birthday;
	private String city;
	private String description;
	private Integer sex;
	private int isFriend;
	/**
	 * 头像
	 */
	private String protrait;

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getProtrait() {
		return protrait;
	}

	public void setProtrait(String protrait) {
		this.protrait = protrait;
	}

	public String getBirthday() {
		return birthday;
	}

	public void setBirthday(String birthday) {
		this.birthday = birthday;
	}

	public String getCity() {
		return city;
	}

	public void setCity(String city) {
		this.city = city;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public Integer getSex() {
		return sex;
	}

	public void setSex(Integer sex) {
		this.sex = sex;
	}

	public int getIsFriend() {
		return isFriend;
	}

	public boolean IsFriend() {
		return isFriend != 0;
	}

	public void setIsFriend(int isFriend) {
		this.isFriend = isFriend;
	}

}
