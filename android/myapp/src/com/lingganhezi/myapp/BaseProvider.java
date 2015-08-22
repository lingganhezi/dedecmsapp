package com.lingganhezi.myapp;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.text.TextUtils;

public abstract class BaseProvider extends ContentProvider {
	protected static final int MATCHER_NORMAL = 1;
	protected static final int MATCHER_ID = 2;

	private UriMatcher mUriMatcher;
	protected SQLiteOpenHelper mDBManager;

	public static final String AUTHORITY = Constant.PROVIDER_AUTHORITY;

	// 当Content Provider启动时被调用
	@Override
	public boolean onCreate() {
		mUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
		addUriMatch(mUriMatcher);

		mDBManager = new DBManager(getContext());
		return true;
	}

	protected void addUriMatch(UriMatcher matcher) {
		String className = this.getClass().getSimpleName();
		matcher.addURI(AUTHORITY + "." + className, null, MATCHER_NORMAL);
		matcher.addURI(AUTHORITY + "." + className, "/#", MATCHER_ID);
	}

	// 删除数据
	@Override
	public int delete(Uri uri, String selection, String[] selectionArgs) {
		SQLiteDatabase db = mDBManager.getWritableDatabase();
		int count;
		switch (mUriMatcher.match(uri)) {
		case MATCHER_NORMAL:
			count = db.delete(getTableName(), selection, selectionArgs);
			break;

		case MATCHER_ID:
			String aid = uri.getPathSegments().get(0);
			count = db.delete(getTableName(), getIdColumn() + "='" + aid + "'"
					+ (!TextUtils.isEmpty(selection) ? " AND (" + selection + ')' : ""), selectionArgs);
			break;

		default:
			throw new IllegalArgumentException("Unnown URI" + uri);
		}
		getContext().getContentResolver().notifyChange(uri, null);
		return count;
	}

	// 如果有自定类型，必须实现该方法
	@Override
	public String getType(Uri uri) {
		return null;
	}

	// 插入数据库
	@Override
	public Uri insert(Uri uri, ContentValues initialValues) {
		if (mUriMatcher.match(uri) != MATCHER_NORMAL) {
			throw new IllegalArgumentException("Uri is not match MATCHER_NORMAL " + uri);
		}
		ContentValues values;
		if (initialValues != null) {
			values = new ContentValues(initialValues);
		} else {
			values = new ContentValues();
		}
		// 返回以毫秒为单位的系统当前时间
		Long now = Long.valueOf(java.lang.System.currentTimeMillis());

		SQLiteDatabase db = mDBManager.getWritableDatabase();
		long rowId = db.insert(getTableName(), null, values);
		if (rowId > 0) {
			Uri noteUri = ContentUris.withAppendedId(getContentUri(), rowId);
			getContext().getContentResolver().notifyChange(noteUri, null);
			return noteUri;
		}
		throw new SQLException("Failed to insert row into" + uri);
	}

	// 查询操作 将查询的数据以 Cursor 对象的形式返回
	@Override
	public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
		SQLiteQueryBuilder qb = new SQLiteQueryBuilder();
		switch (mUriMatcher.match(uri)) {
		case MATCHER_NORMAL:
			qb.setTables(getTableName());
			break;

		case MATCHER_ID:
			qb.setTables(getTableName());
			qb.appendWhere(getIdColumn() + "='" + uri.getPathSegments().get(0) + "'");
			break;

		default:
			throw new IllegalArgumentException("Unknown URI " + uri);
		}
		String orderBy;
		// 返回true，如果字符串为空或0长度
		if (TextUtils.isEmpty(sortOrder)) {
			orderBy = getIdColumn() + " ASC";
		} else {
			orderBy = sortOrder;
		}
		SQLiteDatabase db = mDBManager.getReadableDatabase();
		Cursor c = qb.query(db, projection, selection, selectionArgs, null, null, orderBy);
		// 用来为Cursor对象注册一个观察数据变化的URI
		c.setNotificationUri(getContext().getContentResolver(), uri);
		return c;
	}

	// 更新数据
	@Override
	public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
		SQLiteDatabase db = mDBManager.getWritableDatabase();
		int count;
		switch (mUriMatcher.match(uri)) {
		case MATCHER_NORMAL:
			count = db.update(getTableName(), values, selection, selectionArgs);
			break;

		case MATCHER_ID:
			String noteId = uri.getPathSegments().get(0);
			count = db.update(getTableName(), values, getIdColumn() + "='" + noteId + "'"
					+ (!TextUtils.isEmpty(selection) ? " AND (" + selection + ')' : ""), selectionArgs);
			break;

		default:
			throw new IllegalArgumentException("Unknow URI " + uri);
		}

		if (count > 0) {
			// TODO 当匹配是 MATCHER_ID的时候 需要 特殊通知 带id的uri？
			getContext().getContentResolver().notifyChange(uri, null);
		}
		return count;
	}

	@Override
	public int bulkInsert(Uri uri, ContentValues[] values) {
		int numValues = 0;
		SQLiteDatabase db = mDBManager.getWritableDatabase();
		db.beginTransaction(); // 开始事务
		try {
			// 数据库操作
			numValues = values.length;
			for (int i = 0; i < numValues; i++) {
				insert(uri, values[i]);
			}
			db.setTransactionSuccessful(); // 别忘了这句 Commit
		} finally {
			db.endTransaction(); // 结束事务
		}
		return numValues;
	}

	protected Uri getContentUri() {
		return Uri.parse("content://" + AUTHORITY + "." + this.getClass().getSimpleName() + "/");
	}

	/**
	 * 获取id 字段名
	 * 
	 * @return
	 */
	protected abstract String getIdColumn();

	/**
	 * 获取表明
	 * 
	 * @return
	 */
	protected abstract String getTableName();

}
