package com.lingganhezi.myapp.service;

import android.content.Context;

/**
 * Service 管理
 * 
 * @author chenzipeng
 *
 */
public class ServiceManager {
	Context mContext;
	private static ServiceManager instance;

	private ServiceManager(Context context) {
		mContext = context;
	}

	public static ServiceManager getInstance(Context context) {
		if (instance == null) {
			instance = new ServiceManager(context);
		}
		return instance;
	}

	/**
	 * 获取 LoginService
	 * 
	 * @return
	 */
	public LoginService getLoginService() {
		return LoginService.getInstance();
	}

	/**
	 * 获取 UesrService
	 * 
	 * @return
	 */
	public UserService getUserService() {
		return UserService.getInstance();
	}

	/**
	 * 获取 ArticleService
	 * 
	 * @return
	 */
	public ArticleService getArticleService() {
		return ArticleService.getInstance();
	}

	public MessageService getMessageService() {
		return MessageService.getInstance(mContext);
	}
}
