package com.lingganhezi.myapp.entity;

import com.lingganhezi.myapp.Constant;

public class Message {
	private int id = Constant.MESSAGE_ID_UNDIFINED;
	private int msgid = Constant.MESSAGE_ID_UNDIFINED;
	private String floginid;
	private String tologinid;
	private String folder;
	private String subject;
	private long sendtime;
	private long writetime;
	private int hasview;
	private int isadmin;
	private String message;
	private int sessionid = Constant.MESSAGESESSION_ID_UNDIFINED;
	private int state = Constant.MESSAGE_STATE_SENDING;

	public int getState() {
		return state;
	}

	public void setState(int state) {
		this.state = state;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getFloginid() {
		return floginid;
	}

	public void setFloginid(String floginid) {
		this.floginid = floginid;
	}

	public String getTologinid() {
		return tologinid;
	}

	public void setTologinid(String tologinid) {
		this.tologinid = tologinid;
	}

	public String getFolder() {
		return folder;
	}

	public void setFolder(String folder) {
		this.folder = folder;
	}

	public String getSubject() {
		return subject;
	}

	public void setSubject(String subject) {
		this.subject = subject;
	}

	public long getSendtime() {
		return sendtime;
	}

	public void setSendtime(long sendtime) {
		this.sendtime = sendtime;
	}

	public long getWritetime() {
		return writetime;
	}

	public void setWritetime(long writetime) {
		this.writetime = writetime;
	}

	public int getHasview() {
		return hasview;
	}

	public void setHasview(int hasview) {
		this.hasview = hasview;
	}

	public int getIsadmin() {
		return isadmin;
	}

	public void setIsadmin(int isadmin) {
		this.isadmin = isadmin;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public int getSessionid() {
		return sessionid;
	}

	public void setSessionid(int sessionid) {
		this.sessionid = sessionid;
	}

	public int getMsgid() {
		return msgid;
	}

	public void setMsgid(int msgid) {
		this.msgid = msgid;
	}

	@Override
	public String toString() {
		return "Message [id=" + id + ", msgid=" + msgid + ", floginid=" + floginid + ", tologinid=" + tologinid + ", folder=" + folder
				+ ", subject=" + subject + ", sendtime=" + sendtime + ", writetime=" + writetime + ", hasview=" + hasview + ", isadmin="
				+ isadmin + ", message=" + message + ", sessionid=" + sessionid + ", state=" + state + "]";
	}

}
