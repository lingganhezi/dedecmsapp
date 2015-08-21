package com.lingganhezi.ui.widget;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.AnimationDrawable;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.ViewGroup.LayoutParams;
import android.widget.ImageView;

import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.NetworkImageView;
import com.android.volley.toolbox.ImageLoader.ImageContainer;
import com.android.volley.toolbox.ImageLoader.ImageListener;
import com.lingganhezi.myapp.R;

/**
 * 加载网络图片，
 * 
 * @author chenzipeng
 *
 */
public class LoadImageView extends ImageView {

	/** The URL of the network image to load */
	private String mUrl;

	/**
	 * Resource ID of the image to be used as a placeholder until the network
	 * image is loaded.
	 */
	protected int mDefaultImageId;

	/**
	 * Resource ID of the image to be used if the network response fails.
	 */
	protected int mErrorImageId;

	/** Local copy of the ImageLoader. */
	private ImageLoader mImageLoader;

	/** Current ImageContainer. (either in-flight or finished) */
	private ImageContainer mImageContainer;

	public LoadImageView(Context context) {
		super(context);
		init();
	}

	public LoadImageView(Context context, AttributeSet attrs) {
		super(context, attrs);
		init();
	}

	public LoadImageView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		init();
	}

	private void init() {
		mDefaultImageId = R.drawable.loading;
		mErrorImageId = R.drawable.image_error;
	}

	@Override
	public void setImageResource(int resId) {
		super.setImageResource(resId);
		if (R.drawable.loading == resId) {
			setScaleType(ScaleType.CENTER);
			((AnimationDrawable) getDrawable()).start();
		} else {
			setScaleType(ScaleType.CENTER_CROP);
		}
	}

	@Override
	public void setImageBitmap(Bitmap bm) {
		super.setImageBitmap(bm);
		setScaleType(ScaleType.CENTER_CROP);
	}

	/**
	 * Sets URL of the image that should be loaded into this view. Note that
	 * calling this will immediately either set the cached image (if available)
	 * or the default image specified by
	 * {@link NetworkImageView#setDefaultImageResId(int)} on the view.
	 *
	 * NOTE: If applicable, {@link NetworkImageView#setDefaultImageResId(int)}
	 * and {@link NetworkImageView#setErrorImageResId(int)} should be called
	 * prior to calling this function.
	 *
	 * 当url 与 imageLoader 与之前设置的一样的话就不回去更新
	 * 
	 * @param url
	 *            The URL that should be loaded into this ImageView.
	 * @param imageLoader
	 *            ImageLoader that will be used to make the request.
	 */
	public void setImageUrl(String url, ImageLoader imageLoader) {
		if (url == mUrl && imageLoader == mImageLoader) {
			return;
		}
		mUrl = url;
		mImageLoader = imageLoader;
		// The URL has potentially changed. See if we need to load it.
		loadImageIfNecessary(false);
	}

	/**
	 * Gets the URL of the image that should be loaded into this view, or null
	 * if no URL has been set. The image may or may not already be downloaded
	 * and set into the view.
	 * 
	 * @return the URL of the image to be set into the view, or null.
	 */
	public String getImageURL() {
		return mUrl;
	}

	/**
	 * Loads the image for the view if it isn't already loaded.
	 * 
	 * @param isInLayoutPass
	 *            True if this was invoked from a layout pass, false otherwise.
	 */
	void loadImageIfNecessary(final boolean isInLayoutPass) {
		int width = getWidth();
		int height = getHeight();
		ScaleType scaleType = getScaleType();

		boolean wrapWidth = false, wrapHeight = false;
		if (getLayoutParams() != null) {
			wrapWidth = getLayoutParams().width == LayoutParams.WRAP_CONTENT;
			wrapHeight = getLayoutParams().height == LayoutParams.WRAP_CONTENT;
		}

		// if the view's bounds aren't known yet, and this is not a
		// wrap-content/wrap-content
		// view, hold off on loading the image.
		boolean isFullyWrapContent = wrapWidth && wrapHeight;
		if (width == 0 && height == 0 && !isFullyWrapContent) {
			return;
		}

		// if the URL to be loaded in this view is empty, cancel any old
		// requests and clear the
		// currently loaded image.
		if (TextUtils.isEmpty(mUrl)) {
			if (mImageContainer != null) {
				mImageContainer.cancelRequest();
				mImageContainer = null;
			}
			setDefaultImageOrNull();
			return;
		}

		// if there was an old request in this view, check if it needs to be
		// canceled.
		if (mImageContainer != null && mImageContainer.getRequestUrl() != null) {
			if (mImageContainer.getRequestUrl().equals(mUrl)) {
				// if the request is from the same URL, return.
				return;
			} else {
				// if there is a pre-existing request, cancel it if it's
				// fetching a different URL.
				mImageContainer.cancelRequest();
				setDefaultImageOrNull();
			}
		}

		// Calculate the max image width / height to use while ignoring
		// WRAP_CONTENT dimens.
		int maxWidth = wrapWidth ? 0 : width;
		int maxHeight = wrapHeight ? 0 : height;

		// The pre-existing content of this view didn't match the current URL.
		// Load the new image
		// from the network.
		ImageContainer newContainer = mImageLoader.get(mUrl, new ImageListener() {
			@Override
			public void onErrorResponse(VolleyError error) {
				if (mErrorImageId != 0) {
					setImageResource(mErrorImageId);
				}
			}

			@Override
			public void onResponse(final ImageContainer response, boolean isImmediate) {
				// If this was an immediate response that was delivered
				// inside of a layout
				// pass do not set the image immediately as it will
				// trigger a requestLayout
				// inside of a layout. Instead, defer setting the image
				// by posting back to
				// the main thread.
				if (isImmediate && isInLayoutPass) {
					post(new Runnable() {
						@Override
						public void run() {
							onResponse(response, false);
						}
					});
					return;
				}

				if (response.getBitmap() != null) {
					setImageBitmap(response.getBitmap());
				} else if (mDefaultImageId != 0) {
					setImageResource(mDefaultImageId);
				}
			}
		}, maxWidth, maxHeight, scaleType);

		// update the ImageContainer to be the new bitmap container.
		mImageContainer = newContainer;
	}

	private void setDefaultImageOrNull() {
		if (mDefaultImageId != 0) {
			setImageResource(mDefaultImageId);
			((AnimationDrawable) getDrawable()).start();
		} else {
			setImageBitmap(null);
		}
	}

	@Override
	protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
		super.onLayout(changed, left, top, right, bottom);
		loadImageIfNecessary(true);
	}

	@Override
	protected void onDetachedFromWindow() {
		if (mImageContainer != null) {
			// If the view was bound to an image request, cancel it and clear
			// out the image from the view.
			mImageContainer.cancelRequest();
			setImageBitmap(null);
			// also clear out the container so we can reload the image if
			// necessary.
			mImageContainer = null;
		}
		super.onDetachedFromWindow();
	}

	@Override
	protected void drawableStateChanged() {
		super.drawableStateChanged();
		invalidate();
	}

}
