package com.lingganhezi.myapp.ui;

import com.lingganhezi.myapp.Constant;
import com.lingganhezi.myapp.R;
import com.lingganhezi.myapp.entity.Message;
import com.lingganhezi.myapp.ui.activity.PersonalInfoActivity;
import com.lingganhezi.ui.helper.DensityHelper;
import com.lingganhezi.ui.widget.CircularLoadImageView;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

/**
 * 消息view
 * @author chenzipeng
 *
 */
public class MessageItem extends LinearLayout implements View.OnClickListener{
	private CircularLoadImageView mHeaderView;
	private TextView mContentView;
	private ImageView mStateView;
	private ViewGroup mMessageContainer;
	
	private Message mMessage;
	private boolean mMyself;
	private UserHeaderLoader mUserHeaderLoader;
	private final int padding = 10;// dp

	public MessageItem(Context context) {
		super(context);

		LayoutParams layoutParams = new LayoutParams(android.view.ViewGroup.LayoutParams.MATCH_PARENT,
				android.view.ViewGroup.LayoutParams.WRAP_CONTENT);
		setLayoutParams(layoutParams);

		int paddingPx = DensityHelper.dip2px(context, padding);
		setPadding(paddingPx, paddingPx, paddingPx, paddingPx);
		
		inflate(context, R.layout.item_message, this);
		
		mHeaderView = (CircularLoadImageView) findViewById(R.id.message_header);
		mHeaderView.setOnClickListener(this);
		mContentView = (TextView) findViewById(R.id.message_content);
		mStateView = (ImageView) findViewById(R.id.message_state);
		mMessageContainer = (ViewGroup) findViewById(R.id.message_container);
		
	}

	@SuppressLint("NewApi")
	public void setMessage(Message message, final boolean myself) {
		mMessage = message;
		mMyself = myself;
		
		mContentView.setText(mMessage.getMessage());
		mUserHeaderLoader.load(mHeaderView, message.getFloginid());
		
		//设置消息底图
		if(mMyself){
			mMessageContainer.setBackgroundResource(R.drawable.message_item_right);
		}else{
			mMessageContainer.setBackgroundResource(R.drawable.message_item_left);
		}
		
		//设置状态
		if(mMessage.getState() == Constant.MESSAGE_STATE_SENDING){
			mStateView.setVisibility(View.VISIBLE);
			mStateView.setImageResource(R.drawable.loading);
			
		}else if(mMessage.getState() == Constant.MESSAGE_STATE_FAILD){
			mStateView.setVisibility(View.VISIBLE);
			mStateView.setImageResource(R.drawable.warning);
			
		}else if(mMessage.getState() == Constant.MESSAGE_STATE_SENED){
			mStateView.setVisibility(View.GONE);
		}
	}

	@Override
	protected void onLayout(boolean changed, int l, int t, int r, int b) {
		int childCount = getChildCount();
		int left = getPaddingLeft();
		int right = getMeasuredWidth() - getPaddingRight();
		int top = getPaddingTop();
		int bottom = getMeasuredHeight() - getPaddingBottom();

		for (int i = 0; i < childCount; i++) {
			View child = getChildAt(i);
			final int childWidth = child.getMeasuredWidth();

			final LayoutParams childLayoutParams = (LayoutParams) child.getLayoutParams();

			if (mMyself) {
				// 如果是自己，就从右到到左布局
				int childLeft = right - childWidth - childLayoutParams.rightMargin;
				int childright = childLeft + childWidth;
				right = childLeft;
				child.layout(childLeft, top, childright, bottom);
			} else {
				int childLeft = left + childLayoutParams.leftMargin;
				int childright = childLeft + childWidth;
				left = childright;
				child.layout(childLeft, top, childright, bottom);
			}
		}
	}

	public UserHeaderLoader getUserHeaderLoader() {
		return mUserHeaderLoader;
	}

	public void setUserHeaderLoader(UserHeaderLoader userHeaderLoader) {
		mUserHeaderLoader = userHeaderLoader;
	}

	/**
	 * 加载头像 loader
	 * 
	 * @author chenzipeng
	 *
	 */
	public interface UserHeaderLoader {
		public void load(CircularLoadImageView loadImageView, String userid);
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.message_header:
			//点击头像启动个人信息
			String userid = null;
			if(mMyself){
				userid = mMessage.getFloginid();
			}else{
				userid = mMessage.getTologinid();
			}
			Intent intent = new Intent(v.getContext(),PersonalInfoActivity.class);
			intent.putExtra(PersonalInfoActivity.KEY_USERID, userid);
			v.getContext().startActivity(intent);
			break;

		default:
			break;
		}
	}
	
	
}
