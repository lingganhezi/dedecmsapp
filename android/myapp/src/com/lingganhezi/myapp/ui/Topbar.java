package com.lingganhezi.myapp.ui;

import com.lingganhezi.myapp.R;

import android.animation.Animator;
import android.animation.AnimatorInflater;
import android.animation.Animator.AnimatorListener;
import android.app.Activity;
import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class Topbar extends RelativeLayout {

	private TextView mTitleView;
	private View mBackButton;
	private String mTitle;
	private boolean isShowBackButton;

	public Topbar(Context context, AttributeSet attrs) {
		super(context, attrs);

		// Styleables from XML
		TypedArray a = getContext().obtainStyledAttributes(attrs, R.styleable.Topbar);

		if (a.hasValue(R.styleable.Topbar_title)) {
			mTitle = a.getString(R.styleable.Topbar_title);
		} else {
			mTitle = new String();
		}

		isShowBackButton = a.getBoolean(R.styleable.Topbar_showBackButton, true);
		((LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(R.layout.topbar, this);

		mShowAnimator = AnimatorInflater.loadAnimator(getContext(), R.animator.topbar_show);
		mShowAnimator.setTarget(this);
		mShowAnimator.addListener(mShowAnimatorListener);

		mHideAnimator = AnimatorInflater.loadAnimator(getContext(), R.animator.topbar_hide);
		mHideAnimator.setTarget(this);
		mHideAnimator.addListener(mHideAnimatorListener);
	}

	@Override
	protected void onFinishInflate() {
		super.onFinishInflate();
		mTitleView = (TextView) findViewById(R.id.topbar_title);
		setTitle(mTitle);

		mBackButton = findViewById(R.id.topbar_back);
		mBackButton.setOnClickListener(mBackListener);

		if (isShowBackButton) {
			showBackButton();
		} else {
			hideBackButton();
		}
	}

	/**
	 * 设置标题
	 * 
	 * @param title
	 */
	public void setTitle(CharSequence title) {
		mTitleView.setText(title);
	}

	/**
	 * 设置 返回动作，
	 * 
	 * @param runnable
	 */
	public void setBackAction(Runnable runnable) {
		mBackRunnable = runnable;
	}

	private OnClickListener mBackListener = new OnClickListener() {

		@Override
		public void onClick(View v) {
			if (mBackRunnable != null) {
				mBackRunnable.run();
			}
		}
	};

	/**
	 * 默认返回动作 会关闭当前activity
	 */
	private Runnable mBackRunnable = new Runnable() {

		@Override
		public void run() {
			// 默认是关闭当前activity
			if (getContext() instanceof Activity) {
				((Activity) getContext()).finish();
			}
		}
	};

	// 显示 隐藏动画
	private Animator mShowAnimator;
	private Animator mHideAnimator;

	private boolean isShowAnimRuning = false;
	private boolean isHideAnimRuning = false;

	private AnimatorListener mShowAnimatorListener = new AnimatorListener() {

		@Override
		public void onAnimationStart(Animator animation) {
			isShowAnimRuning = true;
			setVisibility(View.VISIBLE);
		}

		@Override
		public void onAnimationEnd(Animator animation) {
			isShowAnimRuning = false;
			setVisibility(View.VISIBLE);
		}

		@Override
		public void onAnimationCancel(Animator animation) {

		}

		@Override
		public void onAnimationRepeat(Animator animation) {

		}
	};

	private AnimatorListener mHideAnimatorListener = new AnimatorListener() {

		@Override
		public void onAnimationStart(Animator animation) {
			isHideAnimRuning = true;
			setVisibility(View.VISIBLE);
		}

		@Override
		public void onAnimationEnd(Animator animation) {
			isHideAnimRuning = false;
			setVisibility(View.GONE);
		}

		@Override
		public void onAnimationCancel(Animator animation) {

		}

		@Override
		public void onAnimationRepeat(Animator animation) {

		}
	};

	public void showAnimation() {
		if (isShowAnimRuning) {
			return;
		}
		if (isHideAnimRuning) {
			mHideAnimator.cancel();
			mShowAnimator.start();
			return;
		}

		if (this.getVisibility() == View.GONE) {
			mShowAnimator.start();
		}
	}

	public void hideAnimation() {
		if (isHideAnimRuning) {
			return;
		}
		if (isShowAnimRuning) {
			mShowAnimator.cancel();
		}
		if (this.getVisibility() == View.VISIBLE) {
			mHideAnimator.start();
		}
	}

	public void showBackButton() {
		mBackButton.setVisibility(View.VISIBLE);
	}

	public void hideBackButton() {
		mBackButton.setVisibility(View.GONE);
	}
}
