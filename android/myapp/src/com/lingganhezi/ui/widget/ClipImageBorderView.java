package com.lingganhezi.ui.widget;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.Path;
import android.graphics.Path.Direction;
import android.graphics.Rect;
import android.graphics.Region.Op;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.View;

public class ClipImageBorderView extends View {
	/**
	 * 水平方向与View的边距
	 */
	private int mHorizontalPadding;
	/**
	 * 边框的颜色，默认为白色
	 */
	private int mBorderColor = Color.parseColor("#FFFFFF");
	/**
	 * 边框的宽度 单位dp
	 */
	private int mBorderWidth = 1;

	public ClipImageBorderView(Context context) {
		this(context, null);
	}

	public ClipImageBorderView(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}

	public ClipImageBorderView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);

		mBorderWidth = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, mBorderWidth, getResources()
				.getDisplayMetrics());
	}

	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);
		drawCircleWingdow(canvas, this);
	}

	public void setHorizontalPadding(int mHorizontalPadding) {
		this.mHorizontalPadding = mHorizontalPadding;
	}

	/**
	 * 画圆形窗口
	 * 
	 * @param canvas
	 * @param view
	 * @return
	 */
	private Canvas drawCircleWingdow(Canvas canvas, View view) {
		canvas.save();
		Path path = new Path();

		// 计算矩形区域的宽度
		int witdh = getClipWindowWidth();
		// 中心坐标
		int centerX = getWidth() / 2;
		int centerY = getHeight() / 2;
		// 半径
		int radius = witdh / 2;
		Paint paint = new Paint();
		paint.setAntiAlias(true);
		paint.setColor(Color.parseColor("#aa000000"));
		paint.setStyle(Style.FILL);

		Rect viewDrawingRect = new Rect();
		view.getDrawingRect(viewDrawingRect);

		path.addCircle(centerX, centerY, radius, Direction.CW);
		// 画外边
		canvas.clipPath(path, Op.DIFFERENCE);
		canvas.drawRect(viewDrawingRect, paint);
		// TODO 绘制外边框
		// 绘制外边框
		// canvas.restore();
		// canvas.clipPath(path,Op.INTERSECT);
		// paint.setColor(mBorderColor);
		// paint.setStrokeWidth(mBorderWidth);
		// paint.setStyle(Style.STROKE);
		// canvas.drawRect(viewDrawingRect, paint);

		canvas.restore();
		return canvas;
	}

	/**
	 * 获取中间窗口的宽度
	 * 
	 * @return
	 */
	public int getClipWindowWidth() {
		return getWidth() - 2 * mHorizontalPadding;
	}

	/**
	 * 获取中间创空的高度
	 * 
	 * @return
	 */
	public int getClipWindowHeight() {
		return getClipWindowWidth();
	}
}
