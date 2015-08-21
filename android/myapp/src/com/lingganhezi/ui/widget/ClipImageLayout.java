package com.lingganhezi.ui.widget;

import com.lingganhezi.myapp.R;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.widget.RelativeLayout;

public class ClipImageLayout extends RelativeLayout {

	private ClipZoomImageView mZoomImageView;
	private ClipImageBorderView mClipImageView;

	private int mHorizontalPadding;

	public ClipImageLayout(Context context, AttributeSet attrs) {
		super(context, attrs);

		mZoomImageView = new ClipZoomImageView(context);
		mClipImageView = new ClipImageBorderView(context);

		android.view.ViewGroup.LayoutParams lp = new LayoutParams(android.view.ViewGroup.LayoutParams.MATCH_PARENT,
				android.view.ViewGroup.LayoutParams.MATCH_PARENT);

		this.addView(mZoomImageView, lp);
		this.addView(mClipImageView, lp);

		TypedArray a = getContext().obtainStyledAttributes(attrs, R.styleable.ClipImageLayout);

		// 计算padding的px ,默认20px
		mHorizontalPadding = a.getDimensionPixelSize(R.styleable.ClipImageLayout_horizontalPadding, 20);

		mZoomImageView.setHorizontalPadding(mHorizontalPadding);
		mClipImageView.setHorizontalPadding(mHorizontalPadding);
	}

	/**
	 * 裁切图片
	 * 
	 * @return
	 */
	public Bitmap clip() {
		return mZoomImageView.clip();
	}

	/**
	 * 设置图片 </br>很奇怪 如果用这个来设置 不是这个app内部资源的drawable时会出现白了一篇
	 * 
	 * @param drawable
	 */
	public void setImageDrawable(Drawable drawable) {
		mZoomImageView.setImageDrawable(drawable);
	}

	/**
	 * 设置图片
	 * 
	 * @param drawable
	 */
	public void setImageBitmap(Bitmap b) {
		mZoomImageView.setImageBitmap(b);
	}
}
