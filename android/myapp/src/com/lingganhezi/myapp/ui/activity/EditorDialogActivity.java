package com.lingganhezi.myapp.ui.activity;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Date;
import com.lingganhezi.myapp.Constant;
import com.lingganhezi.myapp.R;
import com.lingganhezi.myapp.PlaceProvider.PlaceColumns;
import com.lingganhezi.myapp.entity.Place;
import com.lingganhezi.myapp.service.UserService;
import com.lingganhezi.ui.WheelViewCursorAdapter;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;
import antistatic.spinnerwheel.AbstractWheel;
import antistatic.spinnerwheel.OnWheelChangedListener;
import antistatic.spinnerwheel.WheelVerticalView;
import antistatic.spinnerwheel.adapters.AbstractWheelAdapter;
import antistatic.spinnerwheel.adapters.ArrayWheelAdapter;

public class EditorDialogActivity extends BaseActivity {
	private final String TAG = EditorDialogActivity.class.getSimpleName();
	private TextView mTitleView;
	private RelativeLayout mContentLayout;
	private View mCannelButton;
	private View mConfirmButton;

	/**
	 * 标题 </br> KEY_TITLE String
	 */
	public final static String KEY_TITLE = "KEY_TITLE";
	/**
	 * 打开的编辑类型 </br> @see {@link #TYPE_NULL}, {@link #TYPE_MULTI_TEXT},
	 * {@link #TYPE_TEXT}, {@link #TYPE_SEX}, {@link #TYPE_PLACE},
	 * {@link #TYPE_DATE}
	 */
	public final static String KEY_TYPE = "KEY_TYPE";
	/**
	 * 文本 </br> KEY_TEXT String
	 */
	public final static String KEY_TEXT = "KEY_TEXT";
	/**
	 * 性别 </br> KEY_SEX Integer @see
	 * com.lingganhezi.myapp.service.UserService.SEX_ARRAY
	 */
	public final static String KEY_SEX = "KEY_SEX";
	/**
	 * 地区 id </br> KEY_PLACE Integer
	 */
	public final static String KEY_PLACE = "KEY_PLACE";
	/**
	 * 时间 </br> KEY_DATE Date
	 */
	public final static String KEY_DATE = "KEY_DATE";

	/**
	 * 返回result用
	 */
	public final static String KEY_RESULT_DATA = "KEY_RESULT_DATA";

	public final static int TYPE_NULL = -1;
	/**
	 * 多行编辑文本 Extras: </br>KEY_TEXT String 文本初始值
	 */
	public final static int TYPE_MULTI_TEXT = 0;
	/**
	 * 单行编辑文本 Extras: </br>KEY_TEXT String 文本初始值
	 */
	public final static int TYPE_TEXT = 1;
	/**
	 * 性别 Extras: </br>KEY_SEX Integer 文本初始值
	 */
	public final static int TYPE_SEX = 2;
	/**
	 * 地区 Extras: </br>KEY_PLACE Integer 文本初始值
	 */
	public final static int TYPE_PLACE = 3;
	/**
	 * 地区 Extras: </br>KEY_SEX Date 文本初始值
	 */
	public final static int TYPE_DATE = 4;

	private int mType;
	private String mTitile;

	private Handler mHandler = new Handler();

	@Override
	protected void onCreate(Bundle arg0) {
		super.onCreate(arg0);
		setContentView(R.layout.activity_dialog_editor);
		mTitleView = (TextView) findViewById(R.id.editor_title);
		mContentLayout = (RelativeLayout) findViewById(R.id.editor_content);
		mCannelButton = findViewById(R.id.editor_cannel);
		mConfirmButton = findViewById(R.id.editor_confirm);

		mCannelButton.setOnClickListener(mItemClickListener);
		mConfirmButton.setOnClickListener(mItemClickListener);
	}

	private OnClickListener mItemClickListener = new OnClickListener() {

		@Override
		public void onClick(View v) {
			switch (v.getId()) {
			case R.id.editor_cannel:
				setResult(RESULT_CANCELED);
				finish();
				break;
			case R.id.editor_confirm:
				confirm();
				break;
			default:
				break;
			}
		}
	};

	@Override
	protected void onStart() {
		super.onStart();
		// 处理
		Intent intent = getIntent();
		mType = intent.getIntExtra(KEY_TYPE, TYPE_NULL);
		mTitile = intent.getStringExtra(KEY_TITLE);

		mContentLayout.removeAllViews();
		mTitleView.setText(mTitile);

		switch (mType) {
		case TYPE_NULL:
			Log.e(TAG, "KEY_TYPE Extra is null!!");
			finish();
			break;
		case TYPE_TEXT:
			handleRequestText(intent.getStringExtra(KEY_TEXT));
			break;
		case TYPE_MULTI_TEXT:
			handleRequestMultiText(intent.getStringExtra(KEY_TEXT));
			break;
		case TYPE_DATE:
			Serializable dateData = intent.getSerializableExtra(KEY_DATE);
			Date date = new Date();
			if (dateData != null && dateData instanceof Date) {
				date = (Date) dateData;
			}
			handleRequestDate(date);
			break;
		case TYPE_PLACE:
			handleRequestPlace(intent.getIntExtra(KEY_PLACE, 0));
			break;
		case TYPE_SEX:
			handleRequestSex(intent.getIntExtra(KEY_SEX, Constant.SEX_MAN));
			break;
		default:
			Log.e(TAG, "KEY_TYPE Extra not match");
			finish();
			break;
		}
	}

	/**
	 * 生成 编辑view editor view 必须包含 R.id.editor_editor;
	 * 
	 * @param layoutRes
	 * @return
	 */
	private View getEditorView(int layoutRes) {
		View.inflate(this, layoutRes, mContentLayout);
		return mContentLayout.findViewById(R.id.editor_editor);
	}

	private void handleRequestText(String text) {
		final EditText v = (EditText) getEditorView(R.layout.editor_text);
		configureEditText(v, text);
	}

	private void handleRequestMultiText(String text) {
		final EditText v = (EditText) getEditorView(R.layout.editor_multitext);
		configureEditText(v, text);
	}

	/**
	 * 初始化配置 editText 1.设置text</br> 2.设置focus</br> 2.200ms后弹出键盘</br>
	 * 
	 * @param et
	 * @param text
	 */
	private void configureEditText(final EditText et, String text) {
		et.setText(text);
		et.setSelection(et.getText().length());
		et.setFocusable(true);
		et.setFocusableInTouchMode(true);
		// 因为有可能界面还没创建，所以延迟200ms显示键盘
		mHandler.postDelayed(new Runnable() {
			@Override
			public void run() {
				showSoftInput(et);
			}
		}, 200);
	}

	private void handleRequestDate(Date date) {
		View v = getEditorView(R.layout.editor_date);
		DatePicker datePicker = (DatePicker) v;
		datePicker.setMaxDate(System.currentTimeMillis());
		datePicker.init(date.getYear() + 1900, date.getMonth(), date.getDate(), null);
	}

	/**
	 * 处理地区请求
	 * 
	 * @param place
	 */
	private void handleRequestPlace(final Integer placeid) {
		// TODO 接收初始化参数
		WheelVerticalView cityView = (WheelVerticalView) getEditorView(R.layout.editor_place);
		WheelVerticalView provinceView = (WheelVerticalView) mContentLayout.findViewById(R.id.editor_place_province);

		provinceView.addChangingListener(mPlaceWhellChangeListener);
		cityView.addChangingListener(mPlaceWhellChangeListener);

		Place place = getServiceManager().getUserService().getPlace(placeid);
		if (place != null) {
			if (place.getReid() == 0) {
				// 当这个地区 是 省级的
				provinceView.setTag(R.id.tag_default_value, place.getId());
			} else {
				// 市级
				provinceView.setTag(R.id.tag_default_value, place.getReid());
				cityView.setTag(R.id.tag_default_value, place.getId());
			}
		}

		getSupportLoaderManager().initLoader(R.id.editor_place_province, null, mPlaceLoaderCallback);
		// cityView 的loader 在等待 provinceView 完成初始化后再初始化

	}

	/**
	 * 地区loader
	 */
	private LoaderCallbacks mPlaceLoaderCallback = new LoaderCallbacks<Cursor>() {

		@Override
		public Loader<Cursor> onCreateLoader(int id, Bundle arg1) {
			int reid = 0;
			switch (id) {
			case R.id.editor_place_province:
				reid = 0;
				break;
			// 城市
			case R.id.editor_editor:
				View provinceView = mContentLayout.findViewById(R.id.editor_place_province);
				Object tag = provinceView.getTag();
				if (tag != null) {
					reid = (Integer) tag;
				}
				break;
			default:
				break;
			}
			CursorLoader loader = new CursorLoader(EditorDialogActivity.this, Constant.CONTENT_URI_PLACE_PROVIDER,
					null, PlaceColumns.REID + "=?", new String[] { String.valueOf(reid) }, null);
			return loader;
		}

		@Override
		public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
			WheelVerticalView wheelView = null;
			switch (loader.getId()) {
			case R.id.editor_place_province:
				wheelView = (WheelVerticalView) mContentLayout.findViewById(R.id.editor_place_province);
				break;

			case R.id.editor_editor:
				wheelView = (WheelVerticalView) mContentLayout.findViewById(R.id.editor_editor);
				break;
			default:
				break;
			}

			WheelViewCursorAdapter adapter = (WheelViewCursorAdapter) wheelView.getViewAdapter();
			if (adapter == null) {
				adapter = new WheelViewCursorAdapter(EditorDialogActivity.this, R.layout.editor_place_item, cursor,
						new String[] { PlaceColumns.NAME }, new int[] { R.id.editor_place_name });
				wheelView.setViewAdapter(adapter);
			} else {
				adapter.swapCursor(cursor);
			}

			Integer defalutValue = (Integer) wheelView.getTag(R.id.tag_default_value);

			// 设置默认值
			// TODO 优化这个查询 对应的地区id 序号的方法
			if (defalutValue != null && defalutValue != 0) {
				getIndex: while (cursor.moveToNext()) {
					if (cursor.getInt(cursor.getColumnIndex(PlaceColumns._ID)) == defalutValue) {
						wheelView.setCurrentItem(cursor.getPosition());
						break getIndex;
					}
					;
				}

			}
			// 设置tag数据
			setWheelTag(wheelView, wheelView.getCurrentItem());

			// 判断province是否已经加载完成,并开始加载 城市数据
			if (loader.getId() == R.id.editor_place_province) {
				Loader cityLoader = getSupportLoaderManager().getLoader(R.id.editor_editor);
				if (cityLoader == null) {
					getSupportLoaderManager().initLoader(R.id.editor_editor, null, mPlaceLoaderCallback);
				} else {
					cityLoader.startLoading();
				}
			}
		}

		@Override
		public void onLoaderReset(Loader<Cursor> loader) {
			WheelVerticalView wheelView = null;
			switch (loader.getId()) {
			case R.id.editor_place_province:
				wheelView = (WheelVerticalView) mContentLayout.findViewById(R.id.editor_place_province);
				break;

			case R.id.editor_editor:
				wheelView = (WheelVerticalView) mContentLayout.findViewById(R.id.editor_editor);
				break;
			default:
				break;
			}
			WheelViewCursorAdapter adapter = (WheelViewCursorAdapter) wheelView.getViewAdapter();
			if (adapter != null) {
				adapter.swapCursor(null);
			} else {

			}
		}
	};

	/**
	 * 为 Wheel 设置 tag，把 itemIndex 对应的地区 id 设置到当前的Wheel的tag中
	 * 
	 * @param wheel
	 * @param itemIndex
	 */
	private void setWheelTag(AbstractWheel wheel, int itemIndex) {
		WheelViewCursorAdapter adapter = (WheelViewCursorAdapter) (wheel.getViewAdapter());
		// 判断边界 当他超出的时候设置null tag
		if (itemIndex >= adapter.getCount()) {
			wheel.setTag(null);
			return;
		}

		Object item = adapter.getItem(itemIndex);
		if (item != null) {
			Cursor c = (Cursor) item;
			wheel.setTag(c.getInt(c.getColumnIndex(PlaceColumns._ID)));
		} else {
			wheel.setTag(null);
		}
	}

	/**
	 * 地区滚动监听器
	 */
	private OnWheelChangedListener mPlaceWhellChangeListener = new OnWheelChangedListener() {

		@Override
		public void onChanged(AbstractWheel wheel, int oldValue, int newValue) {
			setWheelTag(wheel, newValue);
			// 处理城市联动
			if (wheel.getId() == R.id.editor_place_province) {
				getSupportLoaderManager().restartLoader(R.id.editor_editor, null, mPlaceLoaderCallback);
			}
		}
	};

	private int getSelectedPlace() {
		WheelVerticalView cityView = (WheelVerticalView) mContentLayout.findViewById(R.id.editor_editor);
		// 如果城市存在值取城市
		WheelViewCursorAdapter adapter = (WheelViewCursorAdapter) (cityView.getViewAdapter());
		if (adapter.getCount() > 0) {
			Cursor c = (Cursor) adapter.getItem(cityView.getCurrentItem());
			return c.getInt(c.getColumnIndex(PlaceColumns._ID));
		}

		// 如果不存在取省份
		WheelVerticalView provinceView = (WheelVerticalView) mContentLayout.findViewById(R.id.editor_place_province);
		adapter = (WheelViewCursorAdapter) (provinceView.getViewAdapter());
		Cursor c = (Cursor) adapter.getItem(provinceView.getCurrentItem());
		return c.getInt(c.getColumnIndex(PlaceColumns._ID));
	}

	/**
	 * 处理性别
	 * 
	 * @param sex
	 */
	private void handleRequestSex(Integer sex) {
		WheelVerticalView v = (WheelVerticalView) getEditorView(R.layout.editor_sex);
		AbstractWheelAdapter adapter = new SexWheelAdapter(this);

		v.setViewAdapter(adapter);

		// 设置默认值
		int selectIndex = Arrays.asList(UserService.SEX_ARRAY).indexOf(sex);
		if (selectIndex >= 0 && selectIndex < adapter.getItemsCount()) {
			v.setCurrentItem(selectIndex);
		}
	}

	private static class SexWheelAdapter extends ArrayWheelAdapter<Integer> {

		private Context mContext;

		public SexWheelAdapter(Context context) {
			super(context, UserService.SEX_ARRAY);
			mContext = context;
			setItemResource(R.layout.editor_sex_item);
			setItemTextResource(R.id.editor_sex_name);
		}

		@Override
		public CharSequence getItemText(int index) {
			int sex = UserService.SEX_ARRAY[index];
			return mContext.getString(UserService.SEX_NAME_MAP.get(sex).intValue());
		}

		@Override
		protected void configureTextView(TextView view) {
			// 不加粗
		}
	}

	private void confirm() {
		Intent resultIntent = new Intent();
		View editor = mContentLayout.findViewById(R.id.editor_editor);
		Serializable data = 0;
		switch (mType) {
		case TYPE_TEXT:
			data = ((EditText) editor).getText().toString();
			break;
		case TYPE_MULTI_TEXT:
			data = ((EditText) editor).getText().toString();
			break;
		case TYPE_DATE:
			DatePicker dp = ((DatePicker) editor);
			data = new Date(dp.getYear() - 1900, dp.getMonth() - 1, dp.getDayOfMonth());
			break;
		case TYPE_PLACE:
			data = getSelectedPlace();
			break;
		case TYPE_SEX:
			data = UserService.SEX_ARRAY[((WheelVerticalView) editor).getCurrentItem()];
			break;
		default:
			Log.e(TAG, "KEY_TYPE Extra not match");
			finish();
			break;
		}
		resultIntent.putExtra(KEY_RESULT_DATA, data);
		setResult(RESULT_OK, resultIntent);
		finish();
	}

	/**
	 * 显示软键盘,并把焦点设置到view
	 * 
	 * @param fourceView
	 *            焦点view
	 */
	private void showSoftInput(View fourceView) {
		fourceView.requestFocus();
		InputMethodManager inputManager = (InputMethodManager) fourceView.getContext().getSystemService(
				Context.INPUT_METHOD_SERVICE);

		inputManager.showSoftInput(fourceView, 0);
	}

	@Override
	public void finish() {
		super.finish();
		// 用来清除 personinfo 调用起来的时候 动画被复写的情况
		overridePendingTransition(0, 0);
	}
}
