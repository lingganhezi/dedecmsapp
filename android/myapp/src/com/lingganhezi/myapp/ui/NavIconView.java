package com.lingganhezi.myapp.ui;

import com.lingganhezi.myapp.R;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.Gravity;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

public class NavIconView extends LinearLayout {

	private int pageIndex = -1;
	private Drawable src = null;
	private String title = null;

	public NavIconView(Context context, AttributeSet attrs) {
		super(context, attrs);
		// Styleables from XML
		TypedArray a = getContext().obtainStyledAttributes(attrs, R.styleable.NavIconView);

		if (a.hasValue(R.styleable.NavIconView_pageIndex)) {
			setPageIndex(a.getInt(R.styleable.NavIconView_pageIndex, -1));
		}

		if (a.hasValue(R.styleable.NavIconView_src)) {
			setSrc(a.getDrawable(R.styleable.NavIconView_src));
		}

		if (a.hasValue(R.styleable.NavIconView_title)) {
			setTitle(a.getString(R.styleable.NavIconView_title));
		} else {
			title = new String();
		}

		inflate(context, R.layout.navicon, this);
		setGravity(Gravity.CENTER);
	}

	@Override
	protected void onFinishInflate() {
		super.onFinishInflate();
		ImageView iconView = (ImageView) findViewById(R.id.navicon_icon);
		TextView titleView = (TextView) findViewById(R.id.navicon_title);

		iconView.setImageDrawable(getSrc());
		titleView.setText(getTitle());
	}

	public int getPageIndex() {
		return pageIndex;
	}

	public void setPageIndex(int pageIndex) {
		this.pageIndex = pageIndex;
	}

	public Drawable getSrc() {
		return src;
	}

	public void setSrc(Drawable src) {
		this.src = src;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

}
