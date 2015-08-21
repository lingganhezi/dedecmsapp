package com.lingganhezi.myapp.ui.fragment;

import com.android.volley.toolbox.ImageLoader;
import com.lingganhezi.myapp.R;
import com.lingganhezi.myapp.entity.MessageSession;
import com.lingganhezi.myapp.entity.UserInfo;
import com.lingganhezi.myapp.service.MessageService;
import com.lingganhezi.myapp.service.UserService;
import com.lingganhezi.myapp.service.handler.UserSyncHandler;
import com.lingganhezi.myapp.ui.activity.MessageActivity;
import com.lingganhezi.ui.widget.LoadImageView;
import com.lingganhezi.ui.widget.PullRefreshGridLayout;
import com.lingganhezi.ui.widget.PullRefreshGridLayout.UpdateDataExecutable;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.TextView;
import android.support.v4.widget.CursorAdapter;

public class MessageSessionFragment extends BaseFragment {
	private String TAG = MessageSessionFragment.class.getSimpleName();
	private PullRefreshGridLayout mSessionList;
	private SessionAdapter mSessionAdapter;
	private MessageService mMessageService;
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		mMessageService = getServiceManager().getMessageService();
		
		View root = inflater.inflate(R.layout.fragment_message_session, container, false);
		mSessionList = (PullRefreshGridLayout) root.findViewById(R.id.messages_sessionList);
		
		Cursor cursor = mMessageService.queryMessageSession();
		mSessionAdapter = new SessionAdapter(this.getActivity(), cursor, getImageLoder());
		mSessionList.setAdapter(mSessionAdapter);

		mSessionList.setUpdateDataExecutable(mMessageSessionUpdater);
		return root;
	}
	
	@Override
	public void onResume() {
		super.onResume();
		mSessionAdapter.notifyDataSetChanged();
	}

	public static class SessionAdapter extends CursorAdapter {
		private MessageService mMessageService;
		private UserService mUserService;
		private ImageLoader mImageLoader;

		public SessionAdapter(Context context, Cursor c, ImageLoader imageLoader) {
			super(context, c, true);
			this.mMessageService = MessageService.getInstance();
			this.mUserService = UserService.getInstance();
			this.mImageLoader = imageLoader;
		}

		@Override
		public void bindView(View view, Context context, Cursor cursor) {
			MessageSession session = mMessageService.getMessageSessionEntry(cursor);

			String targetUserId = session.getUserid();

			final LoadImageView headerView = ((LoadImageView) view.findViewById(R.id.headerpic));
			UserInfo targetUser = UserService.getInstance().getUserInfo(targetUserId);
			// 获取头像
			if (targetUser != null) {
				headerView.setImageUrl(targetUser.getProtrait(), mImageLoader);
			} else {
				// 数据库中不存在开始同步信息
				mUserService.syncUserInfo(targetUserId, new UserSyncHandler(targetUserId,
						new UserSyncHandler.SyncCallback() {

							@Override
							public void complate(boolean succes, UserInfo userInfo) {
								if (succes) {
									headerView.setImageUrl(userInfo.getProtrait(), mImageLoader);
								} else {
									// TODO 获取头像失败时的动作，重试？
								}
							}
						}));
			}

			// 获取最后的消息
			com.lingganhezi.myapp.entity.Message lastMessage = MessageService.getInstance().getLastMessage(session);
			if (lastMessage != null) {
				((TextView) view.findViewById(R.id.content)).setText(lastMessage.getMessage());
			}
			view.setTag(session);
		}

		@Override
		public View newView(Context context, Cursor cursor, ViewGroup parent) {
			View item = ((LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(
					R.layout.item_messagesession, null);
			item.setOnClickListener(itemClickListener);
			return item;
		}

		private OnClickListener itemClickListener = new OnClickListener() {

			@Override
			public void onClick(View v) {
				MessageSession session = (MessageSession) v.getTag();
				Intent intent = new Intent(v.getContext(), MessageActivity.class);
				intent.putExtra(MessageActivity.KEY_MESSAGE_SESSION_ID, session.getId());
				v.getContext().startActivity(intent);
			}
		};
	}

	private UpdateDataExecutable mMessageSessionUpdater = new UpdateDataExecutable() {

		@Override
		public void update(PullRefreshGridLayout view, boolean pullDownToRefresh) {
			if (pullDownToRefresh) {
				mMessageService.syncMessage(mRefreshHandler);
			} else {
				com.lingganhezi.myapp.entity.Message message = mMessageService.getTopServerMessage();
				if(message != null){
					mMessageService.syncMessage(String.valueOf(message.getMsgid()),mRefreshHandler);
				}else{
					//当本地数据库中不存在数据时，更新所有数据
					mMessageService.syncMessage(mRefreshHandler);
				}
			}
		}

		private Handler mRefreshHandler = new Handler(new Handler.Callback() {

			@Override
			public boolean handleMessage(Message msg) {
				switch (msg.what) {
				case MessageService.MSG_ERROR:
				case MessageService.MSG_SYNC_MESSAGE_FAILD:
					mBaseActivity.showToast(R.string.message_refresh_error);
					break;
				case MessageService.MSG_SYNC_MESSAGE_SUCCESS:
					mSessionAdapter.notifyDataSetChanged();
					break;
				default:
					break;
				}
				mSessionList.onRefreshComplete();
				return true;
			}
		});
	};
}
