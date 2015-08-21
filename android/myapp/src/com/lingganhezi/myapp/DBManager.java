package com.lingganhezi.myapp;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DBManager extends SQLiteOpenHelper {
	private static final String DBName = "data.db";

	private final static int version = 1;

	public DBManager(Context context) {
		super(context, DBName, null, version);
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		db.execSQL(ArticleProvider.getCreateSql());
		db.execSQL(MessageSessionProvider.getCreateSql());
		db.execSQL(UserInfoProvider.getCreateSql());
		db.execSQL(MessageProvider.getCreateSql());
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

	}

}
