package com.lingganhezi.myapp.ui;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import android.widget.BaseAdapter;

public abstract class ListAdapter<T> extends BaseAdapter {
	private List<T> mData = new ArrayList<T>();

	@Override
	public int getCount() {
		return mData.size();
	}

	@Override
	public T getItem(int position) {
		return mData.get(position);
	}

	@Override
	public long getItemId(int position) {
		return mData.get(position).hashCode();
	}

	public void add(T item) {
		mData.add(item);
	}

	public void add(T item, int pos) {
		if (pos >= mData.size() - 1) {
			pos = mData.size() - 1;
		}
		mData.add(pos, item);
	}

	public void add(Collection<T> items) {
		mData.addAll(items);
	}

	public void remove(T item) {
		mData.remove(item);
	}

	public void remove(int pos) {
		mData.remove(pos);
	}

	public void clear() {
		mData.clear();
	}

	public List<T> getData() {
		return mData;
	}
}