package com.lingganhezi.myapp.ui.activity;

import java.util.ArrayList;
import java.util.List;

import com.lingganhezi.myapp.R;
import com.lingganhezi.myapp.ui.NavLayout;
import com.lingganhezi.myapp.ui.fragment.ArticlesFragment;
import com.lingganhezi.myapp.ui.fragment.MessageSessionFragment;
import com.lingganhezi.myapp.ui.fragment.PersonalFragment;
import com.viewpagerindicator.IconPagerAdapter;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.os.Bundle;
import android.support.v4.view.ViewPager;

public class MainActivity extends BaseActivity {

	SectionsPagerAdapter mSectionsPagerAdapter;

	ViewPager mViewPager;
	NavLayout mNavbar;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		getFragmentManager();
		android.app.FragmentManager.enableDebugLogging(true);
		setContentView(R.layout.activity_main);

		mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());

		mViewPager = (ViewPager) findViewById(R.id.pager);
		mViewPager.setAdapter(mSectionsPagerAdapter);

		mNavbar = (NavLayout) findViewById(R.id.navbar);
		mNavbar.setViewPager(mViewPager);
	}

	public class SectionsPagerAdapter extends FragmentPagerAdapter implements IconPagerAdapter {
		private List<Fragment> pages = new ArrayList<Fragment>();
		
		public SectionsPagerAdapter(FragmentManager fm) {
			super(fm);
			pages.add(new ArticlesFragment());
			pages.add(new MessageSessionFragment());
			pages.add(new ArticlesFragment());
			pages.add(new PersonalFragment());
		}

		@Override
		public Fragment getItem(int position) {
			return pages.get(position);
		}

		@Override
		public int getCount() {
			return pages.size();
		}

		@Override
		public CharSequence getPageTitle(int position) {
			return null;
		}

		@Override
		public int getIconResId(int index) {
			return R.drawable.ic_launcher;
		}
	}

	private long lastBackPressed;

	@Override
	public void onBackPressed() {
		long now = System.currentTimeMillis();
		if (now - lastBackPressed < 1000) {
			super.onBackPressed();
			return;
		} else {
			showToast(getString(R.string.exit_commit));
		}
		lastBackPressed = now;
	}

}
