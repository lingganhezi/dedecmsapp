package com.lingganhezi.myapp;

import android.provider.BaseColumns;

public class PlaceProvider extends BaseProvider {
	private static final String TAG = PlaceProvider.class.getSimpleName();
	public static final String TABLENAME = "PLACE";

	public static class PlaceColumns {
		public static final String _ID = BaseColumns._ID;
		public static final String NAME = "NAME";
		public static final String REID = "REID";
		public static final String DISORDER = "DISORDER";
	}

	@Override
	public boolean onCreate() {
		boolean result = super.onCreate();
		mDBManager = new BaseDataDBManager(getContext());
		return result;
	}

	@Override
	protected String getIdColumn() {
		return PlaceColumns._ID;
	}

	@Override
	protected String getTableName() {
		return TABLENAME;
	}

}
