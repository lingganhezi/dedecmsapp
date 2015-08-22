package com.lingganhezi.ui.widget;

/*
 * Copyright (C) 2006 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import android.database.DataSetObserver;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ListAdapter;
import android.widget.WrapperListAdapter;

import java.util.ArrayList;

/**
 * ListAdapter used when a ListView has header views. This ListAdapter wraps
 * another one and also keeps track of the header views and their associated
 * data objects.
 * <p>
 * This is intended as a base class; you will probably not need to use this
 * class directly in your own code.
 */
public class HeaderViewListAdapter implements WrapperListAdapter, Filterable {
	/**
	 * A class that represents a fixed view in a list, for example a header at
	 * the top or a footer at the bottom.
	 */
	public static class FixedViewInfo {
		/** The view to add to the list */
		public View view;
		/**
		 * The data backing the view. This is returned from
		 * {@link ListAdapter#getItem(int)}.
		 */
		public Object data;
		/** <code>true</code> if the fixed view should be selectable in the list */
		public boolean isSelectable;
	}

	private final ListAdapter mAdapter;

	// These two ArrayList are assumed to NOT be null.
	// They are indeed created when declared in ListView and then shared.
	ArrayList<FixedViewInfo> mHeaderViewInfos;
	ArrayList<FixedViewInfo> mFooterViewInfos;

	// Used as a placeholder in case the provided info views are indeed null.
	// Currently only used by some CTS tests, which may be removed.
	static final ArrayList<FixedViewInfo> EMPTY_INFO_LIST = new ArrayList<FixedViewInfo>();

	boolean mAreAllFixedViewsSelectable;

	private final boolean mIsFilterable;

	public HeaderViewListAdapter(ArrayList<FixedViewInfo> headerViewInfos, ArrayList<FixedViewInfo> footerViewInfos, ListAdapter adapter) {
		mAdapter = adapter;
		mIsFilterable = adapter instanceof Filterable;

		if (headerViewInfos == null) {
			mHeaderViewInfos = EMPTY_INFO_LIST;
		} else {
			mHeaderViewInfos = headerViewInfos;
		}

		if (footerViewInfos == null) {
			mFooterViewInfos = EMPTY_INFO_LIST;
		} else {
			mFooterViewInfos = footerViewInfos;
		}

		mAreAllFixedViewsSelectable = areAllListInfosSelectable(mHeaderViewInfos) && areAllListInfosSelectable(mFooterViewInfos);
	}

	public int getHeadersCount() {
		return mHeaderViewInfos.size();
	}

	public int getFootersCount() {
		return mFooterViewInfos.size();
	}

	@Override
	public boolean isEmpty() {
		return mAdapter == null || mAdapter.isEmpty();
	}

	private boolean areAllListInfosSelectable(ArrayList<FixedViewInfo> infos) {
		if (infos != null) {
			for (FixedViewInfo info : infos) {
				if (!info.isSelectable) {
					return false;
				}
			}
		}
		return true;
	}

	public boolean removeHeader(View v) {
		for (int i = 0; i < mHeaderViewInfos.size(); i++) {
			FixedViewInfo info = mHeaderViewInfos.get(i);
			if (info.view == v) {
				mHeaderViewInfos.remove(i);

				mAreAllFixedViewsSelectable = areAllListInfosSelectable(mHeaderViewInfos) && areAllListInfosSelectable(mFooterViewInfos);

				return true;
			}
		}

		return false;
	}

	public boolean removeFooter(View v) {
		for (int i = 0; i < mFooterViewInfos.size(); i++) {
			FixedViewInfo info = mFooterViewInfos.get(i);
			if (info.view == v) {
				mFooterViewInfos.remove(i);

				mAreAllFixedViewsSelectable = areAllListInfosSelectable(mHeaderViewInfos) && areAllListInfosSelectable(mFooterViewInfos);

				return true;
			}
		}

		return false;
	}

	@Override
	public int getCount() {
		if (mAdapter != null) {
			return getFootersCount() + getHeadersCount() + mAdapter.getCount();
		} else {
			return getFootersCount() + getHeadersCount();
		}
	}

	@Override
	public boolean areAllItemsEnabled() {
		if (mAdapter != null) {
			return mAreAllFixedViewsSelectable && mAdapter.areAllItemsEnabled();
		} else {
			return true;
		}
	}

	@Override
	public boolean isEnabled(int position) {
		// Header (negative positions will throw an IndexOutOfBoundsException)
		int numHeaders = getHeadersCount();
		if (position < numHeaders) {
			return mHeaderViewInfos.get(position).isSelectable;
		}

		// Adapter
		final int adjPosition = position - numHeaders;
		int adapterCount = 0;
		if (mAdapter != null) {
			adapterCount = mAdapter.getCount();
			if (adjPosition < adapterCount) {
				return mAdapter.isEnabled(adjPosition);
			}
		}

		// Footer (off-limits positions will throw an IndexOutOfBoundsException)
		return mFooterViewInfos.get(adjPosition - adapterCount).isSelectable;
	}

	@Override
	public Object getItem(int position) {
		// Header (negative positions will throw an IndexOutOfBoundsException)
		int numHeaders = getHeadersCount();
		if (position < numHeaders) {
			return mHeaderViewInfos.get(position).data;
		}

		// Adapter
		final int adjPosition = position - numHeaders;
		int adapterCount = 0;
		if (mAdapter != null) {
			adapterCount = mAdapter.getCount();
			if (adjPosition < adapterCount) {
				return mAdapter.getItem(adjPosition);
			}
		}

		// Footer (off-limits positions will throw an IndexOutOfBoundsException)
		return mFooterViewInfos.get(adjPosition - adapterCount).data;
	}

	@Override
	public long getItemId(int position) {
		int numHeaders = getHeadersCount();
		if (mAdapter != null && position >= numHeaders) {
			int adjPosition = position - numHeaders;
			int adapterCount = mAdapter.getCount();
			if (adjPosition < adapterCount) {
				return mAdapter.getItemId(adjPosition);
			}
		}
		return -1;
	}

	@Override
	public boolean hasStableIds() {
		if (mAdapter != null) {
			return mAdapter.hasStableIds();
		}
		return false;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		// Header (negative positions will throw an IndexOutOfBoundsException)
		int numHeaders = getHeadersCount();
		if (position < numHeaders) {
			return mHeaderViewInfos.get(position).view;
		}

		// Adapter
		final int adjPosition = position - numHeaders;
		int adapterCount = 0;
		if (mAdapter != null) {
			adapterCount = mAdapter.getCount();
			if (adjPosition < adapterCount) {
				return mAdapter.getView(adjPosition, convertView, parent);
			}
		}

		// Footer (off-limits positions will throw an IndexOutOfBoundsException)
		return mFooterViewInfos.get(adjPosition - adapterCount).view;
	}

	@Override
	public int getItemViewType(int position) {
		int numHeaders = getHeadersCount();
		if (mAdapter != null && position >= numHeaders) {
			int adjPosition = position - numHeaders;
			int adapterCount = mAdapter.getCount();
			if (adjPosition < adapterCount) {
				return mAdapter.getItemViewType(adjPosition);
			}
		}

		return AdapterView.ITEM_VIEW_TYPE_HEADER_OR_FOOTER;
	}

	@Override
	public int getViewTypeCount() {
		if (mAdapter != null) {
			return mAdapter.getViewTypeCount();
		}
		return 1;
	}

	@Override
	public void registerDataSetObserver(DataSetObserver observer) {
		if (mAdapter != null) {
			mAdapter.registerDataSetObserver(observer);
		}
	}

	@Override
	public void unregisterDataSetObserver(DataSetObserver observer) {
		if (mAdapter != null) {
			mAdapter.unregisterDataSetObserver(observer);
		}
	}

	@Override
	public Filter getFilter() {
		if (mIsFilterable) {
			return ((Filterable) mAdapter).getFilter();
		}
		return null;
	}

	@Override
	public ListAdapter getWrappedAdapter() {
		return mAdapter;
	}
}
