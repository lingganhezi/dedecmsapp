package com.lingganhezi.myapp.ui.fragment;

import com.android.volley.RequestQueue;
import com.android.volley.toolbox.ImageLoader;
import com.lingganhezi.myapp.ArticleProvider;
import com.lingganhezi.myapp.Constant;
import com.lingganhezi.myapp.AppContext;
import com.lingganhezi.myapp.R;
import com.lingganhezi.myapp.ArticleProvider.ArticleColumns;
import com.lingganhezi.myapp.entity.Article;
import com.lingganhezi.myapp.service.ArticleService;
import com.lingganhezi.myapp.service.BaseService;
import com.lingganhezi.myapp.ui.activity.ArticleActivity;
import com.lingganhezi.ui.widget.LoadImageView;
import com.lingganhezi.ui.widget.PullRefreshGridLayout;
import com.lingganhezi.ui.widget.SlideViewPager;
import com.lingganhezi.ui.widget.Slider;
import com.lingganhezi.ui.widget.PullRefreshGridLayout.UpdateDataExecutable;
import com.lingganhezi.ui.widget.Slider.PagerEntry;
import com.lingganhezi.ui.widget.Slider.PaperEntryConverter;
import com.lingganhezi.utils.BitmapCache;
import com.viewpagerindicator.CirclePageIndicator;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.widget.CursorAdapter;

public class ArticlesFragment extends BaseFragment {
	private String TAG = ArticlesFragment.class.getSimpleName();
	// view
	private PullRefreshGridLayout mArticlesPullRefresh;
	ArticleService mArticleService;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mArticleService = getServiceManager().getArticleService();
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

		View rootView = inflater.inflate(R.layout.fragment_articles, container, false);

		mArticlesPullRefresh = (PullRefreshGridLayout) rootView.findViewById(R.id.pull_refresh_articles);
		// 设置下拉更新绑定
		mArticlesPullRefresh.setUpdateDataExecutable(mArticlesUpdateDataExecutable);

		// mSwipeLayout.setColorScheme(android.R.color.holo_blue_bright,
		// android.R.color.holo_green_light,
		// android.R.color.holo_orange_light,
		// android.R.color.holo_red_light);

		mAdapter = new ArticleAdapter(getActivity());

		mArticlesPullRefresh.setAdapter(mAdapter);

		// 加载数据库中的文章
		getLoaderManager().initLoader(0, null, mArticleLoaderListener);

		// 设置 幻灯片
		View slideView = inflater.inflate(R.layout.slider_acticles, container, false);
		SlideViewPager viewPagerSpecial = (SlideViewPager) slideView.findViewById(R.id.viewPager_special);
		CirclePageIndicator pageIndicatorSpecial = (CirclePageIndicator) slideView
				.findViewById(R.id.viewPagerIndicator_special);

		Slider slider = new Slider(getActivity(), viewPagerSpecial, pageIndicatorSpecial);

		slider.load(getLoaderManager(), new CursorLoader(getActivity(), Constant.CONTENT_URI_ARTICLE_PROVIDER, null,
				ArticleColumns.TYPE + "=?", new String[] { String.valueOf(Constant.ARTICLE_TYPE_SPECIAL) },
				ArticleColumns._ID + " DESC"), new PaperEntryConverter() {

			@Override
			public PagerEntry convert(Cursor cursor) {
				PagerEntry entry = new PagerEntry();
				entry.title = cursor.getString(cursor.getColumnIndex(ArticleColumns.TITLE));
				entry.pic = cursor.getString(cursor.getColumnIndex(ArticleColumns.THUMBNAIL));
				entry.intent = createIntent(cursor);
				return entry;
			}

			// 创建 幻灯片点击后 的 intent
			private Intent createIntent(Cursor cursor) {
				Intent intent = new Intent(getActivity(), ArticleActivity.class);
				intent.putExtra(ArticleActivity.KEY_TITLE,
						cursor.getString(cursor.getColumnIndex(ArticleColumns.TITLE)));
				intent.putExtra(ArticleActivity.KEY_CONTENT,
						cursor.getString(cursor.getColumnIndex(ArticleColumns.CONTENT)));
				intent.putExtra(ArticleActivity.KEY_URL, cursor.getString(cursor.getColumnIndex(ArticleColumns.URL)));
				return intent;
			}
		});
		mArticlesPullRefresh.addHeaderView(slideView);

		checkFirstTimeRun();

		return rootView;
	}

	private void checkFirstTimeRun() {
		// 检查 数据是否存在 如果为空就调用 刷新控件去刷新
		if (mArticleService.queryArticels().getCount() == 0) {
			mArticlesPullRefresh.refresh();
		}
		;
	}

	private static final int REFRESH_COMPLETE = 0;
	private static final int REFRESH_ERROR = 1;
	private ArticleAdapter mAdapter;

	private class ArticleAdapter extends CursorAdapter implements PullRefreshGridLayout.UpdateDataAdapter {

		ImageLoader mImageLoader;

		public ArticleAdapter(Context context) {
			super(context, null, true);
			RequestQueue queue = AppContext.getInstance().getRequestQueue();
			mImageLoader = new ImageLoader(queue, BitmapCache.getInstance());
		}

		@Override
		public void bindView(View view, Context context, Cursor cursor) {
			Article entry = mArticleService.getEntry(cursor);
			view.setTag(entry);
			((TextView) view.findViewById(R.id.text_title)).setText(entry.getTitle());
			((TextView) view.findViewById(R.id.text_description)).setText(entry.getDescription());
			LoadImageView loadImageView = (LoadImageView) view.findViewById(R.id.image_thumbnail);
			loadImageView.setImageUrl(entry.getThumbnail(), mImageLoader);
		}

		@Override
		public View newView(Context context, Cursor cursor, ViewGroup parent) {
			View view = ((LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(
					R.layout.item_articlelistview, null);
			view.setOnClickListener(mArticleItemClicklistener);
			return view;
		}

		@Override
		public long getLastItemId() {
			Cursor cursor = getCursor();
			cursor.moveToLast();
			long id = -1;
			if (cursor.getCount() > 0) {
				id = cursor.getLong(cursor.getColumnIndex(ArticleColumns._ID));
			}
			return id;
		}

		private OnClickListener mArticleItemClicklistener = new OnClickListener() {

			@Override
			public void onClick(View v) {
				Article article = (Article) v.getTag();
				Intent intent = new Intent(v.getContext(), ArticleActivity.class);
				intent.putExtra(ArticleActivity.KEY_TITLE, article.getTitle());
				intent.putExtra(ArticleActivity.KEY_URL, article.getUrl());
				intent.putExtra(ArticleActivity.KEY_CONTENT, article.getContent());
				startActivity(intent);
			}
		};
	}

	/**
	 * 文章列表
	 */
	private LoaderCallbacks<Cursor> mArticleLoaderListener = new LoaderCallbacks<Cursor>() {

		@Override
		public Loader<Cursor> onCreateLoader(int arg0, Bundle arg1) {
			Context context = getActivity();
			int limit = 20;
			return new CursorLoader(context, Constant.CONTENT_URI_ARTICLE_PROVIDER, null, ArticleColumns.TYPE + "=?",
					new String[] { String.valueOf(Constant.ARTICLE_TYPE_NORMAL) }, ArticleProvider.ArticleColumns._ID
							+ " DESC LIMIT " + limit);
		}

		@Override
		public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
			mAdapter.changeCursor(cursor);
		}

		@Override
		public void onLoaderReset(Loader<Cursor> arg0) {
			mAdapter.changeCursor(null);
		}
	};

	/**
	 * 下拉刷新用的handler
	 */
	private Handler mArticleRefreshHandler = new Handler() {
		@Override
		public void handleMessage(android.os.Message msg) {
			switch (msg.what) {
			case REFRESH_COMPLETE:
				mAdapter.notifyDataSetChanged();
				break;
			case REFRESH_ERROR:
				Toast.makeText(getActivity(), (String) msg.obj, Toast.LENGTH_SHORT).show();
				break;
			}
			mArticlesPullRefresh.onRefreshComplete();
		};
	};

	private UpdateDataExecutable mArticlesUpdateDataExecutable = new UpdateDataExecutable() {

		@Override
		public void update(PullRefreshGridLayout view, boolean pullDownToRefresh) {
			String aid = null;
			if (!pullDownToRefresh) {
				// 获取最后一个的id
				aid = String.valueOf(mAdapter.getLastItemId());
			}
			getServiceManager().getArticleService().syncArticles(aid, mMsgHandler);

		};

		private Handler mMsgHandler = new Handler(new Handler.Callback() {

			@Override
			public boolean handleMessage(Message msg) {
				switch (msg.what) {
				case ArticleService.MSG_SYNC_ARTICLES_SUCCESS:
					mArticleRefreshHandler.obtainMessage(REFRESH_COMPLETE).sendToTarget();
					break;
				case ArticleService.MSG_SYNC_ARTICLES_FAILD:
				case BaseService.MSG_ERROR:
					mArticleRefreshHandler.obtainMessage(REFRESH_ERROR,
							getString(R.string.message_articles_update_faild)).sendToTarget();
					break;
				default:
					break;
				}
				return true;
			}
		});

	};
}
