package com.lingganhezi.myapp.ui.fragment;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.json.JSONArray;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.Response.Listener;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageLoader;
import com.google.gson.reflect.TypeToken;
import com.lingganhezi.myapp.ArticleProvider;
import com.lingganhezi.myapp.Constant;
import com.lingganhezi.myapp.AppContext;
import com.lingganhezi.myapp.HttpHelper;
import com.lingganhezi.myapp.R;
import com.lingganhezi.myapp.ArticleProvider.ArticleColumns;
import com.lingganhezi.myapp.entity.Article;
import com.lingganhezi.myapp.ui.activity.ArticleActivity;
import com.lingganhezi.net.JsonArrayRequest;
import com.lingganhezi.ui.widget.LoadImageView;
import com.lingganhezi.ui.widget.SlideViewPager;
import com.lingganhezi.ui.widget.Slider;
import com.lingganhezi.ui.widget.Slider.PagerEntry;
import com.lingganhezi.ui.widget.Slider.PaperEntryConverter;
import com.lingganhezi.utils.BitmapCache;
import com.viewpagerindicator.CirclePageIndicator;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.widget.CursorAdapter;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v4.widget.SwipeRefreshLayout.OnRefreshListener;

/**
 * 使用官方下拉更新组件
 * 
 * @author chenzipeng
 *
 */
@Deprecated
public class ArticlesFragmentOld extends BaseFragment {
	private String TAG = ArticlesFragmentOld.class.getSimpleName();

	RequestQueue mVolleyRequesttQueue;

	// view
	private SwipeRefreshLayout mSwipeArticles;
	private ListView mListViewArticles;

	private final String mGetArticleUrl = "http://192.168.112.126:8080/a/articles.json";

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View rootView = inflater.inflate(R.layout.fragment_articles_old, container, false);

		mSwipeArticles = (SwipeRefreshLayout) rootView.findViewById(R.id.swipe_articles);
		mListViewArticles = (ListView) rootView.findViewById(R.id.listview_articles);

		SlideViewPager viewPagerSpecial = (SlideViewPager) rootView.findViewById(R.id.viewPager_special);
		CirclePageIndicator pageIndicatorSpecial = (CirclePageIndicator) rootView
				.findViewById(R.id.viewPagerIndicator_special);

		// 设置 幻灯片
		Slider slider = new Slider(getActivity(), viewPagerSpecial, pageIndicatorSpecial);
		slider.load(getLoaderManager(), new CursorLoader(getActivity(), Constant.CONTENT_URI_ARTICLE_PROVIDER, null,
				null, null, null), new PaperEntryConverter() {

			@Override
			public PagerEntry convert(Cursor cursor) {
				PagerEntry entry = new PagerEntry();
				entry.title = cursor.getString(cursor.getColumnIndex(ArticleColumns.TITLE));
				entry.pic = cursor.getString(cursor.getColumnIndex(ArticleColumns.THUMBNAIL));
				entry.intent = createIntent(cursor);
				return entry;
			}

			private Intent createIntent(Cursor cursor) {
				Intent intent = new Intent(getActivity(), ArticleActivity.class);
				intent.putExtra(ArticleActivity.KEY_TITLE,
						cursor.getString(cursor.getColumnIndex(ArticleColumns.TITLE)));
				intent.putExtra(ArticleActivity.KEY_TITLE,
						cursor.getString(cursor.getColumnIndex(ArticleColumns.CONTENT)));
				return intent;
			}
		});

		// 设置下拉更新绑定
		mSwipeArticles.setOnRefreshListener(mArticlesOnRefreshListenner);

		// mSwipeLayout.setColorScheme(android.R.color.holo_blue_bright,
		// android.R.color.holo_green_light,
		// android.R.color.holo_orange_light,
		// android.R.color.holo_red_light);

		mAdapter = new ArticleAdapter(getActivity());

		mListViewArticles.setAdapter(mAdapter);

		// 加载数据库中的文章
		getLoaderManager().initLoader(0, null, mArticleLoaderListener);

		// 启动访问队列
		mVolleyRequesttQueue = AppContext.getInstance().getRequestQueue();

		return rootView;
	}

	private static final int REFRESH_COMPLETE = 0;
	private static final int REFRESH_ERROR = 1;
	private ArticleAdapter mAdapter;

	private class ArticleAdapter extends CursorAdapter {

		ImageLoader mImageLoader;

		public ArticleAdapter(Context context) {
			super(context, null, true);
			RequestQueue queue = AppContext.getInstance().getRequestQueue();
			mImageLoader = new ImageLoader(queue, BitmapCache.getInstance());
		}

		@Override
		public void bindView(View view, Context context, Cursor cursor) {
			Article entry = getEntry(cursor);
			view.setTag(entry);
			((TextView) view.findViewById(R.id.text_title)).setText(entry.getTitle());
			((TextView) view.findViewById(R.id.text_description)).setText(entry.getDescription());
			LoadImageView loadImageView = (LoadImageView) view.findViewById(R.id.image_thumbnail);
			loadImageView.setImageUrl(entry.getThumbnail(), mImageLoader);
		}

		@Override
		public View newView(Context context, Cursor cursor, ViewGroup parent) {
			return ((LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(
					R.layout.item_articlelistview, null);
		}

		private Article getEntry(Cursor cursor) {
			String id = cursor.getString(cursor.getColumnIndex(ArticleColumns._ID));
			String title = cursor.getString(cursor.getColumnIndex(ArticleColumns.TITLE));
			String description = cursor.getString(cursor.getColumnIndex(ArticleColumns.DESCRIPTION));
			String thumbnail = cursor.getString(cursor.getColumnIndex(ArticleColumns.THUMBNAIL));
			String content = cursor.getString(cursor.getColumnIndex(ArticleColumns.CONTENT));
			String submitDate = cursor.getString(cursor.getColumnIndex(ArticleColumns.SUBMIT_DATE));
			String type = cursor.getString(cursor.getColumnIndex(ArticleColumns.TYPE));
			Article article = new Article();
			article.setId(id);
			article.setTitle(title);
			article.setDescription(description);
			article.setThumbnail(thumbnail);
			article.setContent(content);
			article.setSubmitDate(submitDate);
			article.setType(type);
			return article;
		}

	}

	private LoaderCallbacks<Cursor> mArticleLoaderListener = new LoaderCallbacks<Cursor>() {

		@Override
		public Loader<Cursor> onCreateLoader(int arg0, Bundle arg1) {
			Context context = getActivity();
			int limit = 20;
			return new CursorLoader(context, Constant.CONTENT_URI_ARTICLE_PROVIDER, null, null, null,
					ArticleProvider.ArticleColumns._ID + " DESC LIMIT " + limit);
		}

		@Override
		public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
			mAdapter.swapCursor(cursor);
		}

		@Override
		public void onLoaderReset(Loader<Cursor> arg0) {
			mAdapter.swapCursor(null);
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
			mSwipeArticles.setRefreshing(false);
		};
	};

	private OnRefreshListener mArticlesOnRefreshListenner = new OnRefreshListener() {

		@Override
		public void onRefresh() {
			String url = mGetArticleUrl;
			Map<String, String> params = new HashMap<String, String>();
			Request request = new JsonArrayRequest(url, params, responeListener, errorListener);
			request.setShouldCache(false);
			mVolleyRequesttQueue.add(request);
		}

		/**
		 * 完成网络数据获取
		 */
		private Listener<JSONArray> responeListener = new Response.Listener<JSONArray>() {

			@Override
			public void onResponse(JSONArray response) {
				List<Article> artilces = HttpHelper.getJsonArray(response, new TypeToken<List<Article>>() {
				});
				// 插入数据到数据库
				Context context = getActivity();
				for (Article article : artilces) {
					try {
						Cursor c = context.getContentResolver().query(
								Uri.withAppendedPath(Constant.CONTENT_URI_ARTICLE_PROVIDER, article.getId()), null,
								null, null, null);
						if (c != null) {
							if (c.getCount() == 0) {
								ContentValues values = new ContentValues();
								values.put(ArticleProvider.ArticleColumns._ID, article.getId());
								values.put(ArticleProvider.ArticleColumns.TITLE, article.getTitle());
								values.put(ArticleProvider.ArticleColumns.DESCRIPTION, article.getDescription());
								values.put(ArticleProvider.ArticleColumns.THUMBNAIL, article.getThumbnail());
								values.put(ArticleProvider.ArticleColumns.CONTENT, article.getContent());
								values.put(ArticleProvider.ArticleColumns.SUBMIT_DATE, article.getSubmitDate());
								values.put(ArticleProvider.ArticleColumns.TYPE, article.getType());
								context.getContentResolver().insert(Constant.CONTENT_URI_ARTICLE_PROVIDER, values);
							}
							c.close();
						}

					} catch (Exception e) {
						Log.w(TAG, "insert Articles error:" + article.getId());
					}

				}
				mArticleRefreshHandler.sendEmptyMessage(REFRESH_COMPLETE);
			}

		};

		/**
		 * 网络访问发生错误
		 */
		private Response.ErrorListener errorListener = new Response.ErrorListener() {

			@Override
			public void onErrorResponse(VolleyError error) {
				Log.w(TAG, "get articles list error:" + error.getMessage());
				Message msg = new Message();
				msg.what = REFRESH_ERROR;
				msg.obj = getString(R.string.message_articles_update_faild);
				mArticleRefreshHandler.sendMessage(msg);
			}
		};
	};
}
