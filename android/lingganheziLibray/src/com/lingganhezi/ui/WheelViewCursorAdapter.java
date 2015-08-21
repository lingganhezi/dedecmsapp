package com.lingganhezi.ui;

import android.content.Context;
import android.database.Cursor;
import android.support.v4.widget.SimpleCursorAdapter;
import android.view.View;
import android.view.ViewGroup;
import antistatic.spinnerwheel.adapters.WheelViewAdapter;

public class WheelViewCursorAdapter extends SimpleCursorAdapter implements WheelViewAdapter {

	public WheelViewCursorAdapter(Context context, int layout, Cursor c,
			String[] from, int[] to) {
		super(context, layout, c, from, to);
	}

	@Override
	public int getItemsCount() {
		return getCount();
	}

	@Override
	public View getItem(int index, View convertView, ViewGroup parent) {
		return getView(index, convertView, parent);
	}

	@Override
	public View getEmptyItem(View convertView, ViewGroup parent) {
		return null;
	}

}
