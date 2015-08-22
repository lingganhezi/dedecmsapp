package com.lingganhezi.myapp;

import org.apache.http.entity.ContentType;

import android.net.Uri;

public class Constant {
	public final static boolean DEBUG = true;
	public final static String DEBUG_USER_ID = "453333657@qq.com";
	public final static String DEBUG_USER_PWD = "chenzipeng";

	/**
	 * 服务器配置
	 */
	public final static String SERVER_HOST = "www.lingganhezi.com";
	public final static int SERVER_PORT = 80;
	public final static String SERVER_PART = "/";
	public final static String SERVER_ADD = "http://" + SERVER_HOST + ":" + SERVER_PORT + SERVER_PART;

	public final static String PROVIDER_AUTHORITY = "com.lingganhezi.myapp.data";

	/**
	 * http 请求队列 ，个数 预先创建多少个 请求队列
	 * 
	 */
	public final static int REQUESTQUEUE_HTTP_REQUEST_SIZE = 4;
	/**
	 * http mulitipat 请求队列 ，个数 预先创建多少个 请求队列
	 * 
	 */
	public final static int REQUESTQUEUE_HTTP_MULITI_REQUEST_SIZE = 2;

	public final static int STATE_CODE_FAILD = 0;
	public final static int STATE_CODE_SUCCESS = 1;

	public final static Uri CONTENT_URI_ARTICLE_PROVIDER = Uri.parse("content://com.lingganhezi.myapp.data.ArticleProvider");
	public final static Uri CONTENT_URI_MESSAGE_SESSION_PROVIDER = Uri.parse("content://com.lingganhezi.myapp.data.MessageSessionProvider");
	public final static Uri CONTENT_URI_USERINFO_PROVIDER = Uri.parse("content://com.lingganhezi.myapp.data.UserInfoProvider");
	public final static Uri CONTENT_URI_PLACE_PROVIDER = Uri.parse("content://com.lingganhezi.myapp.data.PlaceProvider");
	public final static Uri CONTENT_URI_MESSAGE_PROVIDER = Uri.parse("content://com.lingganhezi.myapp.data.MessageProvider");

	/**
	 * 普通文章类型
	 */
	public final static int ARTICLE_TYPE_SPECIAL = -1;
	/**
	 * 专题文章类型，幻灯片
	 */
	public final static int ARTICLE_TYPE_NORMAL = 1;

	/**
	 * 头像 裁剪 图像生成宽高
	 */
	public final static int PERSONAL_AVATAR_CLIP_LENGTH = 200;

	/**
	 * http contenttype
	 */
	public final static ContentType HTTP_CONTENTTYPE_IMAGE_JPEG = ContentType.create("image/jpeg");
	public final static ContentType HTTP_CONTENTTYPE_IMAGE_PNG = ContentType.create("image/png");
	public final static ContentType HTTP_CONTENTTYPE_IMAGE_WBMP = ContentType.create("image/wbmp");

	public final static int SEX_UNKONW = -1;
	public final static int SEX_MAN = 0;
	public final static int SEX_WOMEN = 1;

	/**
	 * 收件箱
	 */
	public final static String MESSAGE_BOX_INBOX = "inbox";
	/**
	 * 发件箱
	 */
	public final static String MESSAGE_BOX_OUTBOX = "outbox";

	public final static int MESSAGE_ID_UNDIFINED = -1;
	public final static int MESSAGE_STATE_SENDING = 0;
	public final static int MESSAGE_STATE_FAILD = -1;
	public final static int MESSAGE_STATE_SENED = 1;

	public final static int MESSAGESESSION_ID_UNDIFINED = -1;
}
