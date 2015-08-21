package com.lingganhezi.ui.widget;

import com.android.volley.toolbox.ImageLoader;
import com.lingganhezi.myapp.R;
import com.lingganhezi.ui.widget.Slider.PagerEntry;

import android.content.Context;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class SilderPage extends RelativeLayout {
	private PagerEntry mEntry;
	private ImageLoader mLoader;
	private LoadImageView mImageView;
	private TextView mTitleView;

	public SilderPage(Context context, ImageLoader loader, PagerEntry entry) {
		super(context);
		init();
		setLoader(loader);
		setEntry(entry);
	}

	private void init() {
		View content = inflate(getContext(), R.layout.slider_page, this);
		mImageView = (LoadImageView) content.findViewById(R.id.imageView);
		mTitleView = (TextView) content.findViewById(R.id.titleView);
	}

	public PagerEntry getEntry() {
		return mEntry;
	}

	/**
	 * 设置数据实体
	 * 
	 * 调用此方法前必须 设置 SetLoader
	 * 
	 * @param entry
	 */
	public void setEntry(final PagerEntry entry) {
		if (getLoader() == null) {
			throw new NullPointerException("SilderPage loader is null,has not setLoader ?");
		}
		this.mEntry = entry;

		mImageView.setImageUrl(entry.pic, mLoader);
		this.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				if (entry.intent != null) {
					v.getContext().startActivity(entry.intent);
				}
			}
		});
		mTitleView.setText(entry.title);
	}

	public ImageLoader getLoader() {
		return mLoader;
	}

	public void setLoader(ImageLoader loader) {
		mLoader = loader;
	}

}
