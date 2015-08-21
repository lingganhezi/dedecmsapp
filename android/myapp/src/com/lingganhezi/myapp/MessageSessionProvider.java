package com.lingganhezi.myapp;

import android.provider.BaseColumns;

public class MessageSessionProvider extends BaseProvider {
	private static final String TAG = MessageSessionProvider.class.getSimpleName();
	// 表名
	public static final String TABLE_NAME = "MESSAGE_SESSION";

	public static class MessageSessionColumns {
		public static final String _ID = BaseColumns._ID;
		public static final String USERID = "USERID";
	}

	public static String getCreateSql() {
		return "CREATE TABLE " + TABLE_NAME + "(" + MessageSessionColumns._ID + " INTEGER PRIMARY KEY,"
				+ MessageSessionColumns.USERID + " TEXT" + "); ";
	}

	@Override
	protected String getIdColumn() {
		return MessageSessionColumns._ID;
	}

	@Override
	protected String getTableName() {
		return TABLE_NAME;
	}
}
