package com.lingganhezi.ui.widget;

import android.content.Context;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.MotionEvent;

/**
 * 全能Viewpager，自动滚动，循环滑动、分页指示器 更新数据必须调用getMyPagerAdapter,方法获取到的PagerAdapter
 * 
 * 
 * 
 * 2014-4-24 上午9:46:00
 */
public class SlideViewPager extends ViewPager implements Runnable {

	private static final int POST_DELAYED_TIME = 1000 * 2;
	// 手指是否放在上面
	private boolean touching;
	// 滚动处理时间
	private long pageHandlerTime = 0;

	public SlideViewPager(Context context, AttributeSet attrs) {
		super(context, attrs);
		postDelayed(this, POST_DELAYED_TIME);
	}

	@Override
	// 自动滚动关键
	public void run() {
		long currentTime = System.currentTimeMillis();
		if (getAdapter() != null && getAdapter().getCount() > 1 && !touching
				&& (currentTime - pageHandlerTime >= POST_DELAYED_TIME)) {
			int nextItem = getCurrentItem() + 1;
			nextItem = nextItem >= getAdapter().getCount() ? 0 : nextItem;
			setCurrentItem(nextItem, true);
			pageHandlerTime = System.currentTimeMillis();
		}
		postDelayed(this, POST_DELAYED_TIME);
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		pageHandlerTime = System.currentTimeMillis();
		if (event.getAction() == MotionEvent.ACTION_DOWN) {
			this.touching = true;
		} else if (event.getAction() == MotionEvent.ACTION_UP || event.getAction() == MotionEvent.ACTION_CANCEL) {
			this.touching = false;
		}

		return super.onTouchEvent(event);
	}
}
