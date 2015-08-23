package com.lingganhezi.myapp.ui.fragment;

import com.handmark.pulltorefresh.library.PullToRefreshBase.Mode;
import com.lingganhezi.myapp.Constant;
import com.lingganhezi.myapp.R;
import com.lingganhezi.myapp.UserInfoProvider.UserInfoColumns;
import com.lingganhezi.myapp.entity.UserInfo;
import com.lingganhezi.myapp.service.LoginService;
import com.lingganhezi.myapp.service.MessageService;
import com.lingganhezi.myapp.service.UserService;
import com.lingganhezi.myapp.service.handler.UserFriendHandler;
import com.lingganhezi.myapp.ui.activity.MessageActivity;
import com.lingganhezi.myapp.ui.activity.PersonalInfoActivity;
import com.lingganhezi.myapp.ui.activity.QueryFriendActivity;
import com.lingganhezi.ui.widget.CircularLoadImageView;
import com.lingganhezi.ui.widget.PullRefreshGridLayout;
import com.lingganhezi.ui.widget.SortSideBar;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.widget.CursorAdapter;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver.OnScrollChangedListener;
import android.view.Window;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AdapterView;
import android.widget.SectionIndexer;
import android.widget.TextView;

public class FriendFragment extends BaseFragment implements View.OnClickListener, PullRefreshGridLayout.UpdateDataExecutable,
		SortSideBar.OnTouchingLetterChangedListener,OnScrollListener{
	private View mMainView;
	private PullRefreshGridLayout mFriendList;
	private TextView mSideDialog;
	private SortSideBar mSortSideBar;

	private FriendAdapter mFriendAdapter;

	private UserService mUserService;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mUserService = UserService.getInstance();
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		if (mMainView != null) {
			if (mMainView.getParent() != null) {
				((ViewGroup) mMainView.getParent()).removeView(mMainView);
			}
			return mMainView;
		}

		mMainView = inflater.inflate(R.layout.fragment_friend, container, false);
		init();
		return mMainView;
	}

	private void init() {
		mFriendList = (PullRefreshGridLayout) mMainView.findViewById(R.id.friend_list);
		mSideDialog = (TextView) mMainView.findViewById(R.id.friend_dialog);
		mSortSideBar = (SortSideBar) mMainView.findViewById(R.id.friend_sidrbar);
		mMainView.findViewById(R.id.newfriend).setOnClickListener(this);

		mFriendAdapter = new FriendAdapter();
		mFriendList.setAdapter(mFriendAdapter);
		mFriendList.setUpdateDataExecutable(this);
		mFriendList.setMode(Mode.PULL_DOWN_TO_REFRESH);
		mFriendList.setOnScrollListener(this);
		mSortSideBar.setOnTouchingLetterChangedListener(this);
	}

	public class FriendAdapter extends CursorAdapter implements SectionIndexer, View.OnLongClickListener{

		public FriendAdapter() {
			super(getActivity(), getActivity().getContentResolver().query(Constant.CONTENT_URI_USERINFO_PROVIDER, null,
					UserInfoColumns.ISFRIEND + "=1", null, UserInfoColumns.NAME + " ASC"), true);
		}

		@Override
		public Object[] getSections() {
			return new Object[0];
		}

		/**
		 * @param 实际为char的值
		 */
		@Override
		public int getPositionForSection(int section) {
			Cursor cursor = getCursor();
			cursor.moveToFirst();
			int index = 0;
			while (cursor.moveToNext()) {

				String name = cursor.getString(cursor.getColumnIndex(UserInfoColumns.NAME));
				if (section <= name.toUpperCase().charAt(0)) {
					return index;
				}

				index++;
			}
			return 0;
		}

		@Override
		public int getSectionForPosition(int position) {
			Cursor cursor = (Cursor) getItem(position);
			try {
				return cursor.getString(cursor.getColumnIndex(UserInfoColumns.NAME)).toUpperCase().charAt(0);
			} catch (Exception e) {
			}
			return " ".charAt(0);
		}
		
		private class ItemHolder {
			CircularLoadImageView header;
			TextView name;
		}

		@Override
		public void bindView(View convertView, Context ctx, Cursor cursor) {
			ItemHolder holder = (ItemHolder) convertView.getTag();
			UserInfo userinfo = UserService.getInstance().getUserInfoEntry(cursor);
			holder.header.setImageUrl(userinfo.getProtrait(), getImageLoder());
			holder.name.setText(userinfo.getName());
			convertView.setTag(R.id.tag_bind_data, userinfo);
			holder.header.setTag(R.id.tag_bind_data, userinfo);
		}

		@Override
		public View newView(Context ctx, Cursor cursor, ViewGroup parent) {
			View convertView = LayoutInflater.from(mContext).inflate(R.layout.item_friend, parent, false);
			ItemHolder holder = new ItemHolder();
			holder.header = (CircularLoadImageView) convertView.findViewById(R.id.friend_header);
			holder.name = (TextView) convertView.findViewById(R.id.friend_name);

			convertView.setTag(holder);

			holder.header.setOnClickListener(FriendFragment.this);
			convertView.setOnClickListener(FriendFragment.this);
			convertView.setOnLongClickListener(this);
			return convertView;
		}

		@Override
		public boolean onLongClick(View v) {
			UserInfo userInfo = (UserInfo) v.getTag(R.id.tag_bind_data);
			showDelFriendDialog(userInfo);
			return true;
		}

	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.newfriend:
			// 打开 添加好友 activity
			Intent intent = new Intent(getActivity(), QueryFriendActivity.class);
			startActivity(intent);
			break;
		case R.id.item_friend:
			// 点击list 的item打开对话
			startMessageActivity((UserInfo) v.getTag(R.id.tag_bind_data));
			break;
		case R.id.friend_header:
			// 点击头像打开个人信息
			startPersoninfoActivity((UserInfo) v.getTag(R.id.tag_bind_data));
			break;
		case R.id.delfriend:
			//删除好友
			//关闭dialog
			mFriendControlDialog.dismiss();
			
			delFirend((UserInfo)v.getTag());
			break;
		default:
			break;
		}
	}
	
	/**
	 * 下拉更新去获取最新的好友信息
	 */
	@Override
	public void update(PullRefreshGridLayout view, boolean pullDownToRefresh) {
		if (pullDownToRefresh) {
			mUserService.syncFrieds( new UserFriendHandler(new UserFriendHandler.Callback() {

				@Override
				public void complate(boolean success, Object entry, String message) {
					if (!success) {
						if (TextUtils.isEmpty(message)) {
							message = getString(R.string.friend_sync_faild);
						}
						mBaseActivity.showToast(message);
					}
					mFriendList.onRefreshComplete();
				}
			}));
		}
	}


	@Override
	public void onTouchingLetterChanged(String letter) {
		int pos =  mFriendAdapter.getPositionForSection(letter.charAt(0));
		mFriendList.getRefreshableView().setSelection(pos);
	}


	@Override
	public void onScrollStateChanged(AbsListView view, int scrollState) {
		//没有需要做的动作
	}

	@Override
	public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
		int itempos = firstVisibleItem ;
		int witchLetterInt = mFriendAdapter.getSectionForPosition(itempos);
		char witchLetter = (char) witchLetterInt;
		mSortSideBar.setSelection(String.valueOf(witchLetter));
	}

	private Dialog mFriendControlDialog;
	
	private Dialog getFriendControlDialog(){
		if(mFriendControlDialog == null){
			mFriendControlDialog = new Dialog(getActivity(),R.style.DialogTheme);
		}	
		return mFriendControlDialog;
	}
	
	/**
	 * 显示删除好友对话框
	 * @param userinfo
	 */
	private void showDelFriendDialog(UserInfo userinfo){
		Dialog friendControlDialog = getFriendControlDialog();
		
		friendControlDialog.setCancelable(true);
		friendControlDialog.setContentView(R.layout.dialog_friend_control);
		View delBtn = friendControlDialog.findViewById(R.id.delfriend);
		delBtn.setOnClickListener(this);
		delBtn.setTag(userinfo);
		friendControlDialog.show();
	}
	
	/**
	 * 删除好友
	 * @param userinfo
	 */
	private void delFirend(UserInfo userinfo){
		showProgressDialog(getString(R.string.friend_deleting));
		
		mUserService.delFriend(userinfo.getId(),  new UserFriendHandler(new UserFriendHandler.Callback() {

			@Override
			public void complate(boolean success, Object entry, String message) {
				if (!success) {
					if (TextUtils.isEmpty(message)) {
						message = getString(R.string.friend_del_faild);
					}
					mBaseActivity.showToast(message);
				}
				dismissDialog();
			}
		}));
	}
	
	/**
	 * 启动消息对话activity
	 * 
	 * @param userinfo
	 */
	private void startMessageActivity(UserInfo userinfo) {
		if (!LoginService.getInstance().isLogined()) {
			mBaseActivity.showToast(R.string.login_not_logined);
			return;
		}
		// 获取messageSession
		int sessionid = MessageService.getInstance().getMessageSessionId(userinfo.getId());
		Intent intent = new Intent(getActivity(), MessageActivity.class);
		intent.putExtra(MessageActivity.KEY_MESSAGE_SESSION_ID, sessionid);
		startActivity(intent);
	}

	/**
	 * 启动 个人信息
	 * 
	 * @param userinfo
	 */
	private void startPersoninfoActivity(UserInfo userinfo) {
		Intent intent = new Intent(getActivity(), PersonalInfoActivity.class);
		intent.putExtra(PersonalInfoActivity.KEY_USERID, userinfo.getId());
		startActivity(intent);
	}
	
	@Override
	public void onDestroy() {
		if(mFriendControlDialog != null){
			mFriendControlDialog.dismiss();
		}
		super.onDestroy();
	}
}
