package com.lingganhezi.ui.widget;

import com.lingganhezi.myapp.R;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.NinePatchDrawable;
import android.util.AttributeSet;
import android.graphics.Paint;
import android.graphics.PorterDuff.Mode;
import android.graphics.PorterDuffXfermode;

/**
 * 圆形的loadImgeView
 * 
 * @author chenzipeng
 *
 */
public class CircularLoadImageView extends LoadImageView {
	private int mBorderThickness = 1;
	private int defaultColor = 0xFFFFFFFF;
	// 如果只有其中一个有值，则只画一个圆形边框
	private int mBorderOutsideColor = Color.WHITE;
	private int mBorderInsideColor = Color.GRAY;
	// 控件默认长、宽
	private int defaultWidth = 0;
	private int defaultHeight = 0;

	public CircularLoadImageView(Context context, AttributeSet attrs) {
		super(context, attrs);
		// Styleables from XML
		TypedArray a = getContext().obtainStyledAttributes(attrs, R.styleable.CircularLoadImageView);

		if (a.hasValue(R.styleable.CircularLoadImageView_defaultSrc)) {
			mDefaultImageId = a.getResourceId(R.styleable.CircularLoadImageView_defaultSrc, mDefaultImageId);
		}
	}

	@Override
	protected void onDraw(Canvas canvas) {
		Drawable drawable = getDrawable();
		if (drawable == null) {
			return;
		}
		if (getWidth() == 0 || getHeight() == 0) {
			return;
		}

		if (drawable.getClass() == NinePatchDrawable.class)
			return;
		if (!(drawable instanceof BitmapDrawable))
			return;
		Bitmap b = ((BitmapDrawable) drawable).getBitmap();
		Bitmap bitmap = b.copy(Bitmap.Config.ARGB_8888, true);
		if (defaultWidth == 0) {
			defaultWidth = getWidth();
		}

		if (defaultHeight == 0) {
			defaultHeight = getHeight();
		}

		int radius = 0;

		if (mBorderInsideColor != defaultColor && mBorderOutsideColor != defaultColor) {// 定义画两个边框，分别为外圆边框和内圆边框

			radius = (defaultWidth < defaultHeight ? defaultWidth : defaultHeight) / 2 - 2 * mBorderThickness;
			// 画内圆
			drawCircleBorder(canvas, radius + mBorderThickness / 2, mBorderInsideColor);
			// 画外圆
			drawCircleBorder(canvas, radius + mBorderThickness + mBorderThickness / 2, mBorderOutsideColor);

		} else if (mBorderInsideColor != defaultColor && mBorderOutsideColor == defaultColor) {// 定义画一个边框
			radius = (defaultWidth < defaultHeight ? defaultWidth : defaultHeight) / 2 - mBorderThickness;
			drawCircleBorder(canvas, radius + mBorderThickness / 2, mBorderInsideColor);

		} else if (mBorderInsideColor == defaultColor && mBorderOutsideColor != defaultColor) {// 定义画一个边框
			radius = (defaultWidth < defaultHeight ? defaultWidth : defaultHeight) / 2 - mBorderThickness;
			drawCircleBorder(canvas, radius + mBorderThickness / 2, mBorderOutsideColor);

		} else {// 没有边框
			radius = (defaultWidth < defaultHeight ? defaultWidth : defaultHeight) / 2;
		}

		Bitmap roundBitmap = getCroppedRoundBitmap(bitmap, radius);
		canvas.drawBitmap(roundBitmap, defaultWidth / 2 - radius, defaultHeight / 2 - radius, null);
	}

	/**
	 * 
	 * 获取裁剪后的圆形图片
	 * 
	 * @param radius半径
	 */

	public Bitmap getCroppedRoundBitmap(Bitmap bmp, int radius) {
		Bitmap scaledSrcBmp;
		int diameter = radius * 2;
		// 为了防止宽高不相等，造成圆形图片变形，因此截取长方形中处于中间位置最大的正方形图片
		int bmpWidth = bmp.getWidth();
		int bmpHeight = bmp.getHeight();
		int squareWidth = 0, squareHeight = 0;
		int x = 0, y = 0;
		Bitmap squareBitmap;
		if (bmpHeight > bmpWidth) {// 高大于宽
			squareWidth = squareHeight = bmpWidth;
			x = 0;
			y = (bmpHeight - bmpWidth) / 2;
			// 截取正方形图片
			squareBitmap = Bitmap.createBitmap(bmp, x, y, squareWidth, squareHeight);
		} else if (bmpHeight < bmpWidth) {// 宽大于高
			squareWidth = squareHeight = bmpHeight;
			x = (bmpWidth - bmpHeight) / 2;
			y = 0;
			squareBitmap = Bitmap.createBitmap(bmp, x, y, squareWidth, squareHeight);
		} else {
			squareBitmap = bmp;
		}

		if (squareBitmap.getWidth() != diameter || squareBitmap.getHeight() != diameter) {
			scaledSrcBmp = Bitmap.createScaledBitmap(squareBitmap, diameter, diameter, true);
		} else {
			scaledSrcBmp = squareBitmap;
		}

		Bitmap output = Bitmap.createBitmap(scaledSrcBmp.getWidth(), scaledSrcBmp.getHeight(), Config.ARGB_8888);
		Canvas canvas = new Canvas(output);
		Paint paint = new Paint();
		Rect rect = new Rect(0, 0, scaledSrcBmp.getWidth(), scaledSrcBmp.getHeight());
		paint.setAntiAlias(true);
		paint.setFilterBitmap(true);
		paint.setDither(true);
		canvas.drawARGB(0, 0, 0, 0);
		canvas.drawCircle(scaledSrcBmp.getWidth() / 2, scaledSrcBmp.getHeight() / 2, scaledSrcBmp.getWidth() / 2, paint);
		paint.setXfermode(new PorterDuffXfermode(Mode.SRC_IN));
		canvas.drawBitmap(scaledSrcBmp, rect, rect, paint);

		bmp = null;
		squareBitmap = null;
		scaledSrcBmp = null;
		return output;

	}

	/**
	 * 
	 * 边缘画圆
	 */
	private void drawCircleBorder(Canvas canvas, int radius, int color) {
		Paint paint = new Paint();
		/* 去锯齿 */
		paint.setAntiAlias(true);
		paint.setFilterBitmap(true);
		paint.setDither(true);
		paint.setColor(color);
		/* 设置paint的　style　为STROKE：空心 */
		paint.setStyle(Paint.Style.STROKE);
		/* 设置paint的外框宽度 */
		paint.setStrokeWidth(mBorderThickness);
		canvas.drawCircle(defaultWidth / 2, defaultHeight / 2, radius, paint);
	}

}
