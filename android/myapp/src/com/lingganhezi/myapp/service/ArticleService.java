package com.lingganhezi.myapp.service;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.json.JSONArray;

import com.android.volley.Request;
import com.android.volley.Response;
import com.google.gson.reflect.TypeToken;
import com.lingganhezi.myapp.AppContext;
import com.lingganhezi.myapp.ArticleProvider;
import com.lingganhezi.myapp.HttpHelper;
import com.lingganhezi.myapp.ArticleProvider.ArticleColumns;
import com.lingganhezi.myapp.Constant;
import com.lingganhezi.myapp.entity.Article;
import com.lingganhezi.net.JsonArrayRequest;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Handler;
import android.util.Log;

public class ArticleService extends BaseService {
	private final String TAG = ArticleService.class.getSimpleName();
	private ContentResolver mContentResolver;
	private static ArticleService mInstance;
	private final int mQueryCount = 20;
	/**
	 * MSG 编号 300~400
	 */
	public final static int MSG_SYNC_ARTICLES_SUCCESS = 300;
	public final static int MSG_SYNC_ARTICLES_FAILD = 301;

	private final String URL_GETARTICLE = Constant.SERVER_ADD + "/app/article.php";

	private ArticleService(Context context) {
		super(context);
		mContentResolver = context.getContentResolver();
	}

	private static ArticleService getInstance(Context context) {
		if (mInstance == null) {
			mInstance = new ArticleService(context);
		}
		return mInstance;
	}

	public static ArticleService getInstance() {
		return getInstance(AppContext.getInstance());
	}

	/**
	 * 获取 查询数据的返回条数
	 * 
	 * @return
	 */
	public int getQueryCount() {
		return mQueryCount;
	}

	/**
	 * 转换成实体
	 * 
	 * @param cursor
	 * @return
	 */
	public Article getEntry(Cursor cursor) {
		String id = cursor.getString(cursor.getColumnIndex(ArticleColumns._ID));
		String title = cursor.getString(cursor.getColumnIndex(ArticleColumns.TITLE));
		String description = cursor.getString(cursor.getColumnIndex(ArticleColumns.DESCRIPTION));
		String thumbnail = cursor.getString(cursor.getColumnIndex(ArticleColumns.THUMBNAIL));
		String content = cursor.getString(cursor.getColumnIndex(ArticleColumns.CONTENT));
		String submitDate = cursor.getString(cursor.getColumnIndex(ArticleColumns.SUBMIT_DATE));
		String type = cursor.getString(cursor.getColumnIndex(ArticleColumns.TYPE));
		String url = cursor.getString(cursor.getColumnIndex(ArticleColumns.URL));
		Article article = new Article();
		article.setId(id);
		article.setTitle(title);
		article.setDescription(description);
		article.setThumbnail(thumbnail);
		article.setContent(content);
		article.setSubmitDate(submitDate);
		article.setType(type);
		article.setUrl(url);
		return article;
	}

	/**
	 * 查询文章数据，倒序排列，</br>返回多少条 由 mQueryCount来决定，默认20条
	 * 
	 * @return
	 */
	public Cursor queryArticels() {
		String orderBy = ArticleColumns._ID + " DESC LIMIT " + mQueryCount;
		Cursor cursor = mContentResolver.query(Constant.CONTENT_URI_ARTICLE_PROVIDER, null, null, null, orderBy);
		return cursor;
	}

	/**
	 * 查询 某个id以后的文章数据 ，倒序排列</br>返回多少条 由 mQueryCount来决定，默认20条
	 * 
	 * @param id
	 * @return
	 */
	public Cursor queryArticels(Integer id) {
		// String orderBy = ArticleColumns._ID + " DESC LIMIT " + mQueryCount;
		String orderBy = ArticleColumns._ID + " DESC";
		String where = ArticleColumns._ID + ">?";
		Cursor cursor = mContentResolver.query(Constant.CONTENT_URI_ARTICLE_PROVIDER, null, where, new String[] { String.valueOf(id) },
				orderBy);
		return cursor;
	}

	/**
	 * 插入Articles 列到数据库
	 * 
	 * @param artilces
	 */
	public void insertArticles(Collection<Article> artilces) {
		// 插入数据到数据库
		for (Article article : artilces) {
			Cursor c = mContentResolver.query(Uri.withAppendedPath(Constant.CONTENT_URI_ARTICLE_PROVIDER, article.getId()), null, null,
					null, null);
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
					values.put(ArticleProvider.ArticleColumns.URL, article.getUrl());
					mContentResolver.insert(Constant.CONTENT_URI_ARTICLE_PROVIDER, values);
				}
				c.close();
			}

		}
	}

	/**
	 * 拉取服务器文章列表
	 * 
	 * @param aid
	 *            当aid 为 null 或者 是空字符时会获取最新的
	 * @param handler
	 */
	public void syncArticles(final String aid, final Handler handler) {
		Map<String, String> params = new HashMap<String, String>();
		params.put("action", "getList");
		params.put("aid", aid == null ? new String() : aid);
		Request request = new JsonArrayRequest(URL_GETARTICLE, params, new Response.Listener<JSONArray>() {

			@Override
			public void onResponse(JSONArray json) {
				try {
					List<Article> artilces = HttpHelper.getJsonArray(json, new TypeToken<List<Article>>() {
					});
					insertArticles(artilces);
					handler.obtainMessage(MSG_SYNC_ARTICLES_SUCCESS).sendToTarget();
				} catch (Exception e) {
					Log.e(TAG, "syncArticles error:" + aid, e);
					handler.obtainMessage(MSG_SYNC_ARTICLES_FAILD).sendToTarget();
				}
			}
		}, getErrorListener(handler));
		request.setShouldCache(false);
		getHttpRequesttQueue().add(request);
	}
}
