package com.lingganhezi.myapp;

import android.provider.BaseColumns;

public class MessageProvider extends BaseProvider {
	private static final String TAG = MessageProvider.class.getSimpleName();
	// 表名
	public static final String TABLE_NAME = "MESSAGE";

	public static class MessageColumns {
		public static final String _ID = BaseColumns._ID;
		public static final String MSGID = "MSGID";
		public static final String FLOGINID = "FLOGINID";
		public static final String TOLOGINID = "TOLOGINID";
		public static final String FOLDER = "FOLDER";
		public static final String SUBJECT = "SUBJECT";
		public static final String SENDTIME = "SENDTIME";
		public static final String WRITETIME = "WRITETIME";
		public static final String HASVIEW = "HASVIEW";
		public static final String ISADMIN = "ISADMIN";
		public static final String MESSAGE = "MESSAGE";
		public static final String SESSIONID = "SESSIONID";
		public static final String STATE = "STATE";
	}

	public static String getCreateSql() {
		return "CREATE TABLE " + TABLE_NAME + "(" + MessageColumns._ID + " INTEGER PRIMARY KEY," + MessageColumns.MSGID + " INTEGER,"
				+ MessageColumns.FLOGINID + " TEXT," + MessageColumns.TOLOGINID + " TEXT," + MessageColumns.FOLDER + " TEXT,"
				+ MessageColumns.SUBJECT + " TEXT," + MessageColumns.SENDTIME + " INTEGER," + MessageColumns.WRITETIME + " INTEGER,"
				+ MessageColumns.HASVIEW + " INTEGER," + MessageColumns.ISADMIN + " INTEGER," + MessageColumns.SESSIONID + " INTEGER,"
				+ MessageColumns.MESSAGE + " TEXT," + MessageColumns.STATE + " INTEGER" + "); ";
	}

	@Override
	protected String getIdColumn() {
		return MessageColumns._ID;
	}

	@Override
	protected String getTableName() {
		return TABLE_NAME;
	}
}
