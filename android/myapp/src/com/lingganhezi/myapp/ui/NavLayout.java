package com.lingganhezi.myapp.ui;

import java.util.HashSet;
import java.util.Set;

import android.content.Context;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.util.AttributeSet;
import android.view.View;
import android.widget.LinearLayout;

public class NavLayout extends LinearLayout implements OnPageChangeListener {
	private ViewPager mViewPager;
	private Set<NavIconView> mNavIcons = new HashSet<NavIconView>();

	public NavLayout(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public void setViewPager(ViewPager viewPager) {
		mViewPager = viewPager;
		viewPager.addOnPageChangeListener(this);
		// 设置默认选择
		onPageSelected(viewPager.getCurrentItem());
	}

	@Override
	public void onPageSelected(int index) {
		for (NavIconView icon : mNavIcons) {
			icon.setSelected(icon.getPageIndex() == index);
		}
	}

	@Override
	public void onPageScrolled(int index, float offset, int offsetpx) {

	}

	@Override
	public void onPageScrollStateChanged(int state) {

	}

	private OnClickListener mNavClicklistenner = new OnClickListener() {

		@Override
		public void onClick(View v) {
			Integer pageIndex = ((NavIconView) v).getPageIndex();
			mViewPager.setCurrentItem(pageIndex, false);
		}
	};

	@Override
	protected void onFinishInflate() {
		for (int i = 0; i < getChildCount(); i++) {
			View child = getChildAt(i);
			if (child instanceof NavIconView) {
				mNavIcons.add((NavIconView) child);
				child.setOnClickListener(mNavClicklistenner);
			}
		}
	};

}
