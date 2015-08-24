package com.lingganhezi.ui.widget;

import com.lingganhezi.myapp.R;

import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

/**
 * 圆形旋转菜单layout
 * 
 * @author chenzipeng
 *
 */
public class CircleMenuLayout extends ViewGroup {
	private String TAG = CircleMenuLayout.class.getSimpleName();
	private int mRadius;// 半径
	private int innerRadius = 20;// 内圆半径

	private int mAngle;// 当前旋转角度

	private int mMenuCount = 0;// 一圈分多少份，如果为0，那么就以多少个childview 来分

	public CircleMenuLayout(Context context, AttributeSet attrs) {
		super(context, attrs);
		init(attrs);
	}

	private void init(AttributeSet attrs) {
		TypedArray a = getContext().obtainStyledAttributes(attrs, R.styleable.CircleMenuLayout);

		innerRadius = a.getDimensionPixelSize(R.styleable.CircleMenuLayout_innerRadius, innerRadius);

		mMenuCount = a.getInt(R.styleable.CircleMenuLayout_menuCount, mMenuCount);
	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		// 使用 witdh的measureSpec 来决定 是以什么模式计算 半径
		int childCount = getChildCount();

		// 当 wrap_content，或者未指定的时候 使用childview中最大的来做半径
		if (MeasureSpec.getMode(widthMeasureSpec) == MeasureSpec.AT_MOST
				|| MeasureSpec.getMode(widthMeasureSpec) == MeasureSpec.UNSPECIFIED) {
			for (int i = 0; i < childCount; i++) {
				View child = getChildAt(i);
				child.measure(widthMeasureSpec, heightMeasureSpec);
				int childMeasureWidth = child.getMeasuredWidth();
				int childMeasureHeight = child.getMeasuredHeight();
				// 取宽高最大的值，并计算 半径
				int maxLength = Math.max(childMeasureWidth, childMeasureHeight) + innerRadius;
				if (mRadius < maxLength) {
					mRadius = maxLength;
				}
			}
		} else {
			mRadius = MeasureSpec.getSize(widthMeasureSpec) / 2;
			//调用 childview 去算 大小
			for(int i = 0; i < childCount;i++){
				getChildAt(i).measure(MeasureSpec.makeMeasureSpec(mRadius*2, MeasureSpec.AT_MOST), MeasureSpec.makeMeasureSpec(mRadius*2, MeasureSpec.AT_MOST));
			}
		}

		if (innerRadius > mRadius) {
			Log.w(TAG, "innerRadius > mRadius childview can't show");
		}

		setMeasuredDimension(mRadius * 2, mRadius * 2);
		
	}

	@Override
	protected void onLayout(boolean changed, int l, int t, int r, int b) {
		// 圆心坐标
		int centerX = getMeasuredWidth() / 2;
		int centerY = getMeasuredHeight() / 2;

		// 一圈分6份所以这里 每个子view 角度增加量
		final int childCount = getChildCount();
		int angleAdd = 0;
		if (mMenuCount != 0) {
			angleAdd = 360 / mMenuCount;
		} else if (mMenuCount == 0 && childCount != 0) {
			// 当mMenuCount没有设置 时，使用 childCount作为分多少份
			angleAdd = 360 / childCount;
		}

		for (int i = 0; i < childCount; i++) {
			View child = getChildAt(i);
			int childMeasureWidth = child.getMeasuredWidth();
			int childMeasureHeight = child.getMeasuredHeight();
			// 取宽高最大的值
			int maxLength = Math.max(childMeasureWidth, childMeasureHeight);

			// 计算childview 的中间坐标距离 圆心的长度
			int childRauius = innerRadius + (mRadius - innerRadius) / 2;

			// 计算 childview对应的弧度,因为从左侧开始所在加上180度
			double radian = getRadian(mAngle + 180 + angleAdd * i);
			// 计算childview的中心的坐标点
			int x = centerX + (int) (Math.cos(radian) * childRauius);
			int y = centerY + (int) (Math.sin(radian) * childRauius);

			// 计算 开始画的坐标
			int left =  x - maxLength / 2;
			// 因为计算的是圆心，所以要画的时候，还需要 最长值的一半
			int top = y - maxLength / 2;
			int right = left + maxLength;
			int bootm = top + maxLength;

			child.layout(left, top, right, bootm);
		}
		
	}

	/**
	 * 角度转弧度
	 * 
	 * @param angle
	 * @return
	 */
	private double getRadian(int angle) {
		return ((float) angle / 180) * Math.PI;
	}

	ObjectAnimator mRotateAnimator = new ObjectAnimator();//旋转动画
	
	/**
	 * 设置旋转角度
	 * 
	 * @param angle
	 * @param anim 是否允许动画
	 */
	public void setAngle(int angle,boolean anim) {
		mRotateAnimator.cancel();
		if(anim){
			mRotateAnimator.setTarget(this);
			mRotateAnimator.setPropertyName("angle");
			mRotateAnimator.setIntValues(getAngle(),angle);
			//一圈用时2秒
			int duration = Math.abs((angle - getAngle())%360)*(2000/360);
			mRotateAnimator.setDuration(duration);
			mRotateAnimator.start();
		}else{
			setAngle(angle);
		}
		
	}

	/**
	 * 设置旋转角度
	 * @param angle
	 */
	public void setAngle(int angle){
		mAngle = angle;
		requestLayout();
	}
	/**
	 * 获取当前角度
	 * @return
	 */
	public int getAngle(){
		return mAngle;
	}
	
	/**
	 * 偏移旋转角度
	 * @param angle
	 * @param anim 是否允许动画
	 */
	public void offsetAngle(int angle,boolean anim) {
		setAngle(mAngle + angle,anim);
	}
	
	
	
}
