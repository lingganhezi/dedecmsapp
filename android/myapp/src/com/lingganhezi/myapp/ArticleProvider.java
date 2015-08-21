package com.lingganhezi.myapp;

import android.provider.BaseColumns;

public class ArticleProvider extends BaseProvider {
	private static final String TAG = ArticleProvider.class.getSimpleName();
	public static final String TABLENAME = "ARTICLE";

	public static class ArticleColumns {
		public static final String _ID = BaseColumns._ID;
		public static final String TITLE = "TITLE";
		public static final String DESCRIPTION = "DESCRIPTION";
		public static final String THUMBNAIL = "THUMBNAIL";
		public static final String CONTENT = "CONTENT";
		public static final String SUBMIT_DATE = "SUBMIT_DATE";
		public static final String GET_DATE = "GET_DATE";
		public static final String TYPE = "TYPE";
		public static final String URL = "URL";
	}

	public static String getCreateSql() {
		return "CREATE TABLE " + TABLENAME + "(" + ArticleColumns._ID + " INTEGER PRIMARY KEY," + ArticleColumns.TITLE
				+ " TEXT," + ArticleColumns.DESCRIPTION + " TEXT," + ArticleColumns.THUMBNAIL + " TEXT,"
				+ ArticleColumns.CONTENT + " TEXT," + ArticleColumns.SUBMIT_DATE + " LONG," + ArticleColumns.TYPE
				+ " INTEGER," + ArticleColumns.URL + " TEXT," + ArticleColumns.GET_DATE + " LONG" + "); ";
	}

	@Override
	protected String getIdColumn() {
		return ArticleColumns._ID;
	}

	@Override
	protected String getTableName() {
		return TABLENAME;
	}

}
