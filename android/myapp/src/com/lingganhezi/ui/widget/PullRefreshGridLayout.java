package com.lingganhezi.ui.widget;

import java.util.ArrayList;

import android.animation.AnimatorSet;
import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ListAdapter;
import android.widget.TextView;
import android.widget.WrapperListAdapter;

import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshGridView;
import com.lingganhezi.ui.widget.HeaderViewListAdapter.FixedViewInfo;

/**
 * 下拉网格更新组件
 * 
 * @author chenzipeng
 * 
 */
public class PullRefreshGridLayout extends PullToRefreshGridView {

	private UpdateDataExecutable mUpdateDataExecutable;

	protected TextView mMsgTextView;
	protected AnimatorSet mShowMsgAnimatorSet;

	public PullRefreshGridLayout(Context context, AttributeSet attrs) {
		super(context, attrs);
		// 允许刷新的时候滑动
		setScrollingWhileRefreshingEnabled(true);
	}

	private OnRefreshListener2<GridView> mOnRefreeListener = new OnRefreshListener2<GridView>() {

		@Override
		public void onPullDownToRefresh(PullToRefreshBase<GridView> refreshView) {
			updateData(true);
		}

		@Override
		public void onPullUpToRefresh(PullToRefreshBase<GridView> refreshView) {

			// refresh的监听需要忽略 mode设置为disabled的情况， 比如题型选择那里。
			if (getMode() == Mode.DISABLED) {
				return;
			}

			if (!isLoadEnd()) {
				updateData(false);
			}
		}
	};

	private OnLastItemVisibleListener mOnLastItemVisibleListener = new OnLastItemVisibleListener() {
		private final int DEAFLUT_VISIBLE_ITEM_INDEX = -1;
		private int lastVisibleItemIndex = DEAFLUT_VISIBLE_ITEM_INDEX;

		@Override
		public void onLastItemVisible() {
			if (getMode() == Mode.BOTH || getMode() == Mode.PULL_UP_TO_REFRESH) {
				int tempLastVisibleItemIndex = PullRefreshGridLayout.this.getRefreshableView().getLastVisiblePosition();
				if (!isLoadEnd() && lastVisibleItemIndex != tempLastVisibleItemIndex) {
					lastVisibleItemIndex = tempLastVisibleItemIndex;
					setCurrentMode(Mode.PULL_FROM_END);// 强制设置下拉
					setState(State.REFRESHING, false);
				}
			}
		}
	};

	@Override
	protected void onFinishInflate() {
		super.onFinishInflate();
		setMode(Mode.BOTH);
		setOnRefreshListener(mOnRefreeListener);
		setOnLastItemVisibleListener(mOnLastItemVisibleListener);
		// TODO 自定义刷新header & footer
		// setLoadingDrawable(drawable)

	};

	/**
	 * 强制上拉更新
	 */
	public void refresh() {
		setState(State.MANUAL_REFRESHING, false);
	}

	/**
	 * 调用更新数据
	 * 
	 * @param pullDownToRefresh
	 *            是否下拉更新， true ： 下拉更新</br> false: 上拉更新
	 */
	protected void updateData(boolean pullDownToRefresh) {

		if (getUpdateDataExecutable() != null) {
			getUpdateDataExecutable().update(this, pullDownToRefresh);
		}
	}

	/**
	 * 数据更新接口
	 * 
	 * @author chenzipeng
	 * 
	 */
	public interface UpdateDataExecutable {
		/**
		 * 更新数据 在数据更新完成以后调用 PullRefreshGridLayout.onRefreshComplete();来控制完成
		 * 
		 * @param view
		 *            PullRefireshLayout
		 * @param pullDownToRefresh
		 *            是否下拉更新， true ： 下拉更新</br> false: 上拉更新
		 */
		void update(PullRefreshGridLayout view, boolean pullDownToRefresh);
	}

	/**
	 * 更新数据用adapter接口
	 * 
	 * @author chenzipeng
	 *
	 */
	public interface UpdateDataAdapter {
		/**
		 * 返回最后一个id
		 * 
		 * @return
		 */
		long getLastItemId();
	}

	public void setUpdateDataExecutable(UpdateDataExecutable updateDataExecutable) {
		mUpdateDataExecutable = updateDataExecutable;
	}

	public UpdateDataExecutable getUpdateDataExecutable() {
		return mUpdateDataExecutable;
	}

	/**
	 * 显示运行结果弹出
	 * 
	 * @param msg
	 */
	public void showRefreshResult(String msg) {
		mMsgTextView.setText(msg);
		mShowMsgAnimatorSet.start();
	}

	private ArrayList<FixedViewInfo> mHeaderViewInfos = new ArrayList<FixedViewInfo>();
	private ArrayList<FixedViewInfo> mFooterViewInfos = new ArrayList<FixedViewInfo>();

	/**
	 * Add a fixed view to appear at the top of the list. If this method is
	 * called more than once, the views will appear in the order they were
	 * added. Views added using this call can take focus if they want.
	 * <p>
	 * Note: When first introduced, this method could only be called before
	 * setting the adapter with {@link #setAdapter(ListAdapter)}. Starting with
	 * {@link android.os.Build.VERSION_CODES#KITKAT}, this method may be called
	 * at any time. If the ListView's adapter does not extend
	 * {@link HeaderViewListAdapter}, it will be wrapped with a supporting
	 * instance of {@link WrapperListAdapter}.
	 *
	 * @param v
	 *            The view to add.
	 */
	public void addHeaderView(View v) {
		final FixedViewInfo info = new FixedViewInfo();
		info.view = v;
		info.data = null;
		info.isSelectable = false;
		mHeaderViewInfos.add(info);
		addSpecialView();
	}

	/**
	 * Add a fixed view to appear at the top of the list. If this method is
	 * called more than once, the views will appear in the order they were
	 * added. Views added using this call can take focus if they want.
	 * <p>
	 * Note: When first introduced, this method could only be called before
	 * setting the adapter with {@link #setAdapter(ListAdapter)}. Starting with
	 * {@link android.os.Build.VERSION_CODES#KITKAT}, this method may be called
	 * at any time. If the ListView's adapter does not extend
	 * {@link HeaderViewListAdapter}, it will be wrapped with a supporting
	 * instance of {@link WrapperListAdapter}.
	 *
	 * @param v
	 *            The view to add.
	 */
	public void addFooterView(View v) {
		final FixedViewInfo info = new FixedViewInfo();
		info.view = v;
		info.data = null;
		info.isSelectable = false;
		mFooterViewInfos.add(info);
		addSpecialView();
	}

	private void addSpecialView() {
		if (getRefreshableView() != null) {
			// Wrap the adapter if it wasn't already wrapped.
			ListAdapter adapter = getRefreshableView().getAdapter();
			if (adapter != null) {
				if (!(adapter instanceof HeaderViewListAdapter)) {
					adapter = new HeaderViewListAdapter(mHeaderViewInfos, mFooterViewInfos, adapter);
					this.setAdapter(adapter);
				}

				// In the case of re-adding a header view, or adding one later
				// on,
				// we need to notify the observer.
				if (adapter instanceof BaseAdapter) {
					((BaseAdapter) adapter).notifyDataSetChanged();
				} else {
					Log.w("pullrefrshGirdlayout", " addHeaderView not notifyDataSetChanged");
				}

			}
		}
	}
}
