package com.lingganhezi.myapp.ui.activity;

import com.handmark.pulltorefresh.library.PullToRefreshBase.Mode;
import com.lingganhezi.myapp.Constant;
import com.lingganhezi.myapp.R;
import com.lingganhezi.myapp.MessageProvider.MessageColumns;
import com.lingganhezi.myapp.entity.LoginUserInfo;
import com.lingganhezi.myapp.entity.Message;
import com.lingganhezi.myapp.entity.MessageSession;
import com.lingganhezi.myapp.entity.UserInfo;
import com.lingganhezi.myapp.service.LoginService;
import com.lingganhezi.myapp.service.MessageService;
import com.lingganhezi.myapp.service.UserService;
import com.lingganhezi.myapp.service.handler.MessageQueryHandler;
import com.lingganhezi.myapp.service.handler.UserSyncHandler;
import com.lingganhezi.myapp.ui.MessageItem;
import com.lingganhezi.myapp.ui.Topbar;
import com.lingganhezi.ui.widget.CircularLoadImageView;
import com.lingganhezi.ui.widget.PullRefreshGridLayout;
import com.lingganhezi.ui.widget.PullRefreshGridLayout.UpdateDataExecutable;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.Loader;
import android.support.v4.widget.CursorAdapter;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.FilterQueryProvider;

public class MessageActivity extends BaseActivity implements View.OnClickListener {
	private MessageService mMessageService;

	private PullRefreshGridLayout mMessageList;
	private MessageAdapter mMessageAdapter;
	private View mSendButton;
	private View mShowEmoButton;// TODO 实现表情
	private EditText mMessageText;
	private Topbar mTopbar;

	private MessageSession mSession;

	public final static String KEY_MESSAGE_SESSION_ID = "KEY_MESSAGE_SESSION_ID";

	@Override
	protected void onCreate(Bundle arg0) {
		super.onCreate(arg0);
		setContentView(R.layout.activity_message);

		mMessageAdapter = new MessageAdapter(this);

		mMessageList = (PullRefreshGridLayout) findViewById(R.id.messages_list);
		mTopbar = (Topbar) findViewById(R.id.topbar);
		mSendButton = findViewById(R.id.message_send);
		// mShowEmoButton = findViewById(R.id.message_show_emo_btn);
		mMessageText = (EditText) findViewById(R.id.message_text);

		mMessageList.setAdapter(mMessageAdapter);
		mMessageList.setUpdateDataExecutable(mMessageUpdater);
		// 设置只允许下拉更新
		mMessageList.setMode(Mode.PULL_FROM_START);

		mSendButton.setOnClickListener(this);
		// mShowEmoButton.setOnClickListener(this);

		mMessageService = MessageService.getInstance();

		init(getIntent());

	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.message_send:
			sendMessage();
			break;
		default:
			break;
		}
	}

	@Override
	protected void onNewIntent(Intent intent) {
		super.onNewIntent(intent);
		init(intent);
	}

	private void init(Intent intent) {
		if (!intent.hasExtra(KEY_MESSAGE_SESSION_ID)) {
			finish();
			return;
		}

		if (!LoginService.getInstance().isLogined()) {
			showToast(R.string.dialog_login_not_logined);
			finish();
			return;
		}

		int sessionid = intent.getIntExtra(KEY_MESSAGE_SESSION_ID, -1);

		MessageSession session = mMessageService.getMessageSession(sessionid);
		if (session == null) {
			showToast(R.string.message_session_not_found);
			finish();
			return;
		}
		// 获取对方的id
		mSession = session;

		initTargetUserInfo();

		// 开始初始化Message
		Bundle params = new Bundle();
		params.putInt(KEY_MESSAGE_SESSION_ID, sessionid);
		getSupportLoaderManager().initLoader(R.id.messages_list, params, mMessageLoaderCallback);
	}

	private LoaderCallbacks<Cursor> mMessageLoaderCallback = new LoaderCallbacks<Cursor>() {

		@Override
		public void onLoaderReset(Loader<Cursor> loader) {
			mMessageAdapter.changeCursor(null);
		}

		@Override
		public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
			mMessageAdapter.changeCursor(cursor);
			//设置到最后
			int count = mMessageList.getRefreshableView().getCount();
			int pos = count - 1;
			mMessageList.getRefreshableView().setSelection(pos);

		}

		@Override
		public Loader<Cursor> onCreateLoader(int loaderid, Bundle params) {
			int sessionid = params.getInt(KEY_MESSAGE_SESSION_ID);
			return mMessageService.getMessageLoader(sessionid);
		}
	};

	private class MessageAdapter extends CursorAdapter {
		private MessageService mMessageService;
		private UserService mUserService;

		public MessageAdapter(Context context) {
			super(context, null, true);
			mMessageService = MessageService.getInstance();
			mUserService = UserService.getInstance();
		}

		@Override
		public void bindView(View messageView, Context context, Cursor cursor) {
			LoginUserInfo currentUser = mUserService.getCurrentLoginUser();
			Message message = mMessageService.getMessageEntry(cursor);
			boolean myself = (message.getFloginid().equals(currentUser.getUserId()));
			((MessageItem) messageView).setMessage(message, myself);
		}

		@Override
		public View newView(Context context, Cursor c, ViewGroup parent) {
			MessageItem item = new MessageItem(context);
			item.setUserHeaderLoader(mMessageUserHeaderLoader);
			return item;
		}

		private MessageItem.UserHeaderLoader mMessageUserHeaderLoader = new MessageItem.UserHeaderLoader() {

			@Override
			public void load(final CircularLoadImageView loadImageView, final String userid) {
				UserInfo user = mUserService.getUserInfo(userid);
				if (user != null) {
					loadImageView.setImageUrl(user.getProtrait(), getImageLoder());
				}
			}

		};
	}

	private UpdateDataExecutable mMessageUpdater = new UpdateDataExecutable() {

		@Override
		public void update(PullRefreshGridLayout view, boolean pullDownToRefresh) {
			// 只有下拉的时候才会去请求刷新数据
			if (pullDownToRefresh) {
				if (mMessageAdapter.getCount() > 0) {
					// 取第一个 item的时间
					Cursor cursor = (Cursor) mMessageAdapter.getItem(0);
					Message message = mMessageService.getMessageEntry(cursor);
					mMessageService.queryMessageByTime(message.getSendtime(),mSession.getId(), mMessageQueryHandler);
				} else {
					// 当没有最后一个消息的时候就去 拉去所有的消息?是否会造成无限自动刷新
					// mMessageService.queryMessageByTime(Long.MAX_VALUE,mMessageQueryHandler);
					mMessageList.onRefreshComplete();
				}
			} else {
				mMessageList.onRefreshComplete();
			}
		}

		private MessageQueryHandler mMessageQueryHandler = new MessageQueryHandler(
				new MessageQueryHandler.MessageQueryCallback() {

					@Override
					public void complate(boolean success, final Cursor cursor) {
						if (success) {
						
							//bug 这里会闪一下，以为之前 swapCursor的时候 会去更新画面
							mMessageList.post(new Runnable() {
								
								@Override
								public void run() {
									//设置 位置到刚刚的位置
									int oldCount = mMessageAdapter.getCount();
									int newCount = cursor.getCount();
									final int pos = newCount - oldCount < 0 ?0:newCount - oldCount;					
									//替换cursor
									mMessageAdapter.changeCursor(cursor);
									mMessageList.getRefreshableView().setSelection(pos);
								}
							});
							
							
						} else {
							showToast(R.string.message_refresh_error);
						}
						mMessageList.onRefreshComplete();
					}
				});

	};

	/**
	 * 发送消息
	 */
	private void sendMessage() {
		String text = mMessageText.getText().toString();
		
		if(TextUtils.isEmpty(text)){
			showToast(R.string.message_msg_isempty);
			return;
		}
		
		// 清空对话框
		mMessageText.getEditableText().clear();
		
		Message message = mMessageService.buildSendMessage(mSession.getUserid(), text);
		mMessageService.sendMessage(message, new Handler(new Handler.Callback() {

			@Override
			public boolean handleMessage(android.os.Message msg) {
				switch (msg.what) {
				case MessageService.MSG_SEND_MESSAGE_SUCCESS:
					// TODO 成功后处理
					break;
				default:
					// TODO 发送失败处理
					showToast(R.string.message_send_faild);
					break;
				}
				return true;
			}
		}));
		// 发送发送命令后，更新adapter
		//由于cursorAdpater 设置了自动更新 ，这里就不用通知数据改变
		//mMessageAdapter.notifyDataSetChanged();
	};

	/**
	 * 初始化 目标用户数据
	 */
	private void initTargetUserInfo() {
		UserInfo targetUser = UserService.getInstance().getUserInfo(mSession.getUserid());
		if (targetUser != null) {
			// 获取对方的名字,设置到topbar
			mTopbar.setTitle(targetUser.getName());
		} else {
			// 如果本地没有数据就去拉服务器的数据
			UserService.getInstance().syncUserInfo(mSession.getUserid(),
					new UserSyncHandler(mSession.getUserid(), new UserSyncHandler.SyncCallback() {

						@Override
						public void complate(boolean succes, UserInfo userInfo) {
							if (succes) {
								// 成功以后，刷新数据
								mTopbar.setTitle(userInfo.getName());
								mMessageAdapter.notifyDataSetChanged();
							} else {
								// TODO 失败时的处理
							}
						}
					}));
		}
	}

}
