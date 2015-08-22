package com.lingganhezi.myapp.ui.activity;

import java.util.List;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.TextView;

import com.lingganhezi.myapp.R;
import com.lingganhezi.myapp.entity.UserInfo;
import com.lingganhezi.myapp.service.UserService;
import com.lingganhezi.myapp.service.handler.BaseServiceHandler;
import com.lingganhezi.myapp.service.handler.UserFriendHandler;
import com.lingganhezi.myapp.service.handler.UserQueryFriendHandler;
import com.lingganhezi.myapp.ui.ListAdapter;
import com.lingganhezi.ui.widget.CircularLoadImageView;
import com.lingganhezi.ui.widget.CustomEditView;

/**
 * 查询不是 好友的 会员
 * 
 * @author chenzipeng
 *
 */
public class QueryFriendActivity extends BaseActivity implements OnClickListener {
	private ListView mUserList;
	private CustomEditView mSreachText;
	private UserInfoAdapter mAdapter;

	private UserService mUserService;

	@Override
	protected void onCreate(Bundle arg0) {
		super.onCreate(arg0);

		mUserService = UserService.getInstance();

		setContentView(R.layout.activity_queryfriend);

		mUserList = (ListView) findViewById(R.id.list);
		mSreachText = (CustomEditView) findViewById(R.id.sreach_text);
		findViewById(R.id.sreach_btn).setOnClickListener(this);

		mAdapter = new UserInfoAdapter();
		mUserList.setAdapter(mAdapter);
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.sreach_btn:
			// 发送搜索
			sreach();
			break;
		case R.id.newfriend:
			newFriend((UserInfo) v.getTag());
			break;
		case R.id.header:
			startPersoninfoActivity((UserInfo)v.getTag(R.id.tag_bind_data));
			break;
		default:
			break;
		}
	}

	/**
	 * 搜素
	 */
	private void sreach() {
		String username = mSreachText.getText().toString().trim();
		showProgressDialog(getString(R.string.queryfriend_querying));
		mUserService.serachUnfriend(username, new UserQueryFriendHandler(new BaseServiceHandler.Callback<List<UserInfo>>() {

			@Override
			public void complate(boolean succes, List<UserInfo> users, String message) {
				if (succes) {
					mAdapter.clear();
					mAdapter.add(users);
					mAdapter.notifyDataSetChanged();
					if (users.size() == 0) {
						showToast(R.string.queryfriend_not_found);
					}
				} else {
					showToast(R.string.queryfriend_query_faild);
				}
				dismissDialog();
			}
		}));
	};

	/**
	 * 加关注
	 * 
	 * @param userinfo
	 * @param pos
	 */
	private void newFriend(final UserInfo userinfo) {
		final String userid = userinfo.getId();
		showProgressDialog(getResources().getString(R.string.queryfriend_newfriend_adding));
		mUserService.addNewfriend(userid, new UserFriendHandler(new BaseServiceHandler.Callback<UserInfo>() {
			@Override
			public void complate(boolean succes, UserInfo entry, String message) {
				if (succes) {
					showToast(R.string.queryfriend_newfriend_add_success);
					mAdapter.remove(userinfo);
					mAdapter.notifyDataSetChanged();
				} else {
					if (TextUtils.isEmpty(message)) {
						message = getString(R.string.queryfriend_newfriend_add_faild);
					}
					showToast(message);
				}
				dismissDialog();
			}
		}));
	};

	private class UserInfoAdapter extends ListAdapter<UserInfo> {

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			if (convertView == null) {
				convertView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_queryfriend, parent, false);
				Holder hodler = new Holder();
				hodler.header = (CircularLoadImageView) convertView.findViewById(R.id.header);
				hodler.name = (TextView) convertView.findViewById(R.id.name);
				hodler.newfriend = convertView.findViewById(R.id.newfriend);
				hodler.header.setOnClickListener(QueryFriendActivity.this);
				hodler.newfriend.setOnClickListener(QueryFriendActivity.this);
				convertView.setTag(hodler);
			}

			Holder hodler = (Holder) convertView.getTag();
			UserInfo user = getItem(position);
			hodler.header.setImageUrl(user.getProtrait(), getImageLoder());
			hodler.header.setTag(R.id.tag_bind_data, user);
			hodler.name.setText(user.getName());
			hodler.newfriend.setTag(user);

			return convertView;
		}

		public class Holder {
			CircularLoadImageView header;
			TextView name;
			View newfriend;
		}

	}
	
	/**
	 * 启动 个人信息
	 * 
	 * @param userinfo
	 */
	private void startPersoninfoActivity(UserInfo userinfo) {
		Intent intent = new Intent(this, PersonalInfoActivity.class);
		intent.putExtra(PersonalInfoActivity.KEY_USERID, userinfo.getId());
		startActivity(intent);
	}
}
