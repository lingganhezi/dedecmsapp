package com.lingganhezi.ui.widget;

import com.lingganhezi.myapp.R;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PorterDuff.Mode;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

public class SwitchButton extends View implements android.view.View.OnClickListener {
	private Bitmap mSwitchBottom, mSwitchThumb, mSwitchFrame, mSwitchMask;
	private float mCurrentX = 0;
	private boolean mSwitchOn = true;// 开关默认是开着的
	private int mMoveLength;// 最大移动距离
	private float mLastX = 0;// 第一次按下的有效区域

	private Rect mDest = null;// 绘制的目标区域大小
	private Rect mSrc = null;// 截取源图片的大小
	private int mDeltX = 0;// 移动的偏移量
	private Paint mPaint = null;
	private OnChangeListener mListener = null;
	private boolean mFlag = false;

	public SwitchButton(Context context) {
		this(context, null);
	}

	public SwitchButton(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}

	public SwitchButton(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		init();
	}

	private void init() {
		mSwitchBottom = BitmapFactory.decodeResource(getResources(), R.drawable.switch_bottom);
		mSwitchThumb = BitmapFactory.decodeResource(getResources(), R.drawable.switch_btn_pressed);
		mSwitchFrame = BitmapFactory.decodeResource(getResources(), R.drawable.switch_frame);
		mSwitchMask = BitmapFactory.decodeResource(getResources(), R.drawable.switch_mask);
	}

	private boolean isInited = false;

	/**
	 * 初始化相关资源
	 */
	public void reMeasureBitmap() {
		Bitmap switchBottom = mSwitchBottom;
		Bitmap switchFrame = mSwitchFrame;
		Bitmap switchMask = mSwitchMask;
		Bitmap switchThumb = mSwitchThumb;
		setOnClickListener(this);
		setOnTouchListener(new OnTouchListener() {

			@Override
			public boolean onTouch(View v, MotionEvent event) {
				return false;
			}
		});

		// 计算 图大小
		float scale = (float) getMeasuredWidth() / switchFrame.getWidth();
		Matrix m = new Matrix();
		m.postScale(scale, scale);

		mSwitchBottom = Bitmap.createBitmap(switchBottom, 0, 0, switchBottom.getWidth(), switchBottom.getHeight(), m,
				true);
		mSwitchThumb = Bitmap.createBitmap(switchThumb, 0, 0, switchThumb.getWidth(), switchThumb.getHeight(), m, true);
		mSwitchFrame = Bitmap.createBitmap(switchFrame, 0, 0, switchFrame.getWidth(), switchFrame.getHeight(), m, true);
		mSwitchMask = Bitmap.createBitmap(switchMask, 0, 0, switchMask.getWidth(), switchMask.getHeight(), m, true);

		switchBottom.recycle();
		switchThumb.recycle();
		switchFrame.recycle();
		switchMask.recycle();

		mMoveLength = mSwitchBottom.getWidth() - mSwitchFrame.getWidth();
		mDest = new Rect(0, 0, mSwitchFrame.getWidth(), mSwitchFrame.getHeight());
		mSrc = new Rect();
		mPaint = new Paint();
		mPaint.setAntiAlias(true);
		mPaint.setAlpha(255);
		mPaint.setXfermode(new PorterDuffXfermode(Mode.DST_IN));
		// 算到了重新计算过大小
		setMeasuredDimension(mSwitchFrame.getWidth(), mSwitchFrame.getHeight());
		isInited = true;
	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		if (MeasureSpec.getMode(widthMeasureSpec) == MeasureSpec.EXACTLY) {
			super.onMeasure(widthMeasureSpec,
					MeasureSpec.makeMeasureSpec(mSwitchFrame.getHeight(), MeasureSpec.EXACTLY));
		} else {
			setMeasuredDimension(mSwitchFrame.getWidth(), mSwitchFrame.getHeight());
		}
		if (!isInited) {
			reMeasureBitmap();
		}
	}

	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);
		if (mDeltX > 0 || mDeltX == 0 && mSwitchOn) {
			if (mSrc != null) {
				mSrc.set(mMoveLength - mDeltX, 0, mSwitchBottom.getWidth() - mDeltX, mSwitchFrame.getHeight());
			}
		} else if (mDeltX < 0 || mDeltX == 0 && !mSwitchOn) {
			if (mSrc != null) {
				mSrc.set(-mDeltX, 0, mSwitchFrame.getWidth() - mDeltX, mSwitchFrame.getHeight());
			}
		}
		// 这儿是离屏缓冲，自己感觉类似双缓冲机制吧
		int count = canvas
				.saveLayer(new RectF(mDest), null, Canvas.MATRIX_SAVE_FLAG | Canvas.CLIP_SAVE_FLAG
						| Canvas.HAS_ALPHA_LAYER_SAVE_FLAG | Canvas.FULL_COLOR_LAYER_SAVE_FLAG
						| Canvas.CLIP_TO_LAYER_SAVE_FLAG);

		canvas.drawBitmap(mSwitchBottom, mSrc, mDest, null);
		canvas.drawBitmap(mSwitchThumb, mSrc, mDest, null);
		canvas.drawBitmap(mSwitchFrame, 0, 0, null);
		canvas.drawBitmap(mSwitchMask, 0, 0, mPaint);
		canvas.restoreToCount(count);
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		switch (event.getAction()) {
		case MotionEvent.ACTION_DOWN:
			mLastX = event.getX();
			break;
		case MotionEvent.ACTION_MOVE:
			mCurrentX = event.getX();
			mDeltX = (int) (mCurrentX - mLastX);
			// 如果开关开着向左滑动，或者开关关着向右滑动（这时候是不需要处理的）
			if ((mSwitchOn && mDeltX < 0) || (!mSwitchOn && mDeltX > 0)) {
				mFlag = true;
				mDeltX = 0;
			}

			if (Math.abs(mDeltX) > mMoveLength) {
				mDeltX = mDeltX > 0 ? mMoveLength : -mMoveLength;
			}
			invalidate();
			return true;
		case MotionEvent.ACTION_UP:
			if (Math.abs(mDeltX) > 0 && Math.abs(mDeltX) < mMoveLength / 2) {
				mDeltX = 0;
				invalidate();
				return true;
			} else if (Math.abs(mDeltX) > mMoveLength / 2 && Math.abs(mDeltX) <= mMoveLength) {
				mDeltX = mDeltX > 0 ? mMoveLength : -mMoveLength;
				mSwitchOn = !mSwitchOn;
				if (mListener != null) {
					mListener.onChange(this, mSwitchOn);
				}
				invalidate();
				mDeltX = 0;
				return true;
			} else if (mDeltX == 0 && mFlag) {
				// 这时候得到的是不需要进行处理的，因为已经move过了
				mDeltX = 0;
				mFlag = false;
				return true;
			}
			return super.onTouchEvent(event);
		default:
			break;
		}
		invalidate();
		return super.onTouchEvent(event);
	}

	public void setOnChangeListener(OnChangeListener listener) {
		mListener = listener;
	}

	public interface OnChangeListener {
		public void onChange(SwitchButton sb, boolean state);
	}

	@Override
	public void onClick(View v) {
		setSwitchState(!mSwitchOn);
	}

	public void setSwitchState(boolean isSwitchOn) {
		mDeltX = mSwitchOn ? mMoveLength : -mMoveLength;
		mSwitchOn = isSwitchOn;
		if (mListener != null) {
			mListener.onChange(this, mSwitchOn);
		}
		invalidate();
		mDeltX = 0;
	}

	public boolean getSwitchState() {
		return mSwitchOn;
	}
}
