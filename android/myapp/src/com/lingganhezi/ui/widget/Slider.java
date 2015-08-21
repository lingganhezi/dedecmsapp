package com.lingganhezi.ui.widget;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.view.PagerAdapter;
import android.view.View;
import android.view.ViewGroup;

import com.android.volley.RequestQueue;
import com.android.volley.toolbox.ImageLoader;
import com.lingganhezi.myapp.AppContext;
import com.lingganhezi.utils.BitmapCache;
import com.viewpagerindicator.PageIndicator;

/**
 * 幻灯片
 * 
 * @author chenzipeng
 *
 */
public class Slider {
	private Context mContext;
	private SlideViewPager mViewPager;
	private PageIndicator mPageIndicator;

	List<PagerEntry> mEntrys = new ArrayList<PagerEntry>();
	private ImageLoader mImageLoader;
	private CursorLoader mCursorLoader;

	private LoaderManager mLoaderManager;
	private PaperEntryConverter mPaperEntryConverter;
	private SliderAdapter mSliderAdapter;

	/**
	 * 
	 * @param context
	 * @param slideViewPager
	 * @param pageIndicator
	 * @param loaderManager
	 *            * @param cursorloader 用来读取数据的 cursorLoader
	 * @param paperEntryConverter
	 *            吧Cusror 中的字段 转换成PaderEntry
	 */
	public Slider(Context context, SlideViewPager slideViewPager, PageIndicator pageIndicator) {
		super();
		mContext = context;
		mViewPager = slideViewPager;
		mPageIndicator = pageIndicator;
	}

	public void load(LoaderManager loaderManager, CursorLoader cursorloader, PaperEntryConverter paperEntryConverter) {
		mLoaderManager = loaderManager;
		mPaperEntryConverter = paperEntryConverter;
		mCursorLoader = cursorloader;

		// image cache loader
		RequestQueue queue = AppContext.getInstance().getRequestQueue();
		mImageLoader = new ImageLoader(queue, BitmapCache.getInstance());

		// 设置幻灯片绑定
		mSliderAdapter = new SliderAdapter();
		mViewPager.setAdapter(mSliderAdapter);
		mPageIndicator.setViewPager(mViewPager);

		mLoaderManager.initLoader(mCursorLoader.hashCode(), null, new LoaderCallback());

	}

	public interface PaperEntryConverter {
		public PagerEntry convert(Cursor cursor);
	}

	private class LoaderCallback implements LoaderManager.LoaderCallbacks<Cursor> {

		@Override
		public Loader<Cursor> onCreateLoader(int arg0, Bundle arg1) {
			return mCursorLoader;
		}

		@Override
		public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
			mEntrys.clear();
			if (cursor != null) {
				while (cursor.moveToNext()) {
					mEntrys.add(mPaperEntryConverter.convert(cursor));
				}
			}
			mSliderAdapter.notifyDataSetChanged();
		}

		@Override
		public void onLoaderReset(Loader<Cursor> arg0) {
			mEntrys.clear();
			mSliderAdapter.notifyDataSetChanged();
		}

	}

	private class SliderAdapter extends PagerAdapter {

		@Override
		public int getCount() {
			return mEntrys.size();
		}

		@Override
		public boolean isViewFromObject(View view, Object pager) {
			return view == pager;
		}

		@Override
		public void destroyItem(ViewGroup container, int position, Object pager) {
			container.removeView((View) pager);
		}

		@Override
		public Object instantiateItem(ViewGroup container, int position) {
			final PagerEntry entry = mEntrys.get(position);
			SilderPage pager = new SilderPage(container.getContext(), mImageLoader, entry);
			container.addView(pager);
			return pager;
		}

	}

	public static class PagerEntry {
		public String title;
		public String pic;
		public Intent intent;
	}
}
