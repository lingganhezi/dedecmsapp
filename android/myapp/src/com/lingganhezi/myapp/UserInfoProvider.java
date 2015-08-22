package com.lingganhezi.myapp;

import android.content.UriMatcher;
import android.provider.BaseColumns;

public class UserInfoProvider extends BaseProvider {
	private static final String TAG = UserInfoProvider.class.getSimpleName();
	// 表名
	public static final String TABLE_NAME = "USERINFO";

	public static class UserInfoColumns {
		public static final String _ID = BaseColumns._ID;
		public static final String NAME = "NAME";
		public static final String EMAIL = "EMAIL";
		public static final String PORTRAIT = "PORTRAIT";
		public static final String BIRTHDAY = "BIRTHDAY";
		public static final String CITY = "CITY";
		public static final String DESCRIPTION = "DESCRIPTION";
		public static final String SEX = "SEX";
		public static final String ISFRIEND = "ISFRIEND";
	}

	public static String getCreateSql() {
		return "CREATE TABLE " + TABLE_NAME + "(" + UserInfoColumns._ID + " TEXT PRIMARY KEY," + UserInfoColumns.NAME + " TEXT,"
				+ UserInfoColumns.EMAIL + " TEXT," + UserInfoColumns.BIRTHDAY + " TEXT," + UserInfoColumns.CITY + " TEXT,"
				+ UserInfoColumns.DESCRIPTION + " TEXT," + UserInfoColumns.SEX + " INTEGER," + UserInfoColumns.ISFRIEND + " INTEGER ,"
				+ UserInfoColumns.PORTRAIT + " TEXT" + "); ";
	}

	@Override
	protected void addUriMatch(UriMatcher matcher) {
		String className = this.getClass().getSimpleName();
		matcher.addURI(AUTHORITY + "." + className, null, MATCHER_NORMAL);
		matcher.addURI(AUTHORITY + "." + className, "/*", MATCHER_ID);
	}

	@Override
	protected String getIdColumn() {
		return UserInfoColumns._ID;
	}

	@Override
	protected String getTableName() {
		return TABLE_NAME;
	}
}
