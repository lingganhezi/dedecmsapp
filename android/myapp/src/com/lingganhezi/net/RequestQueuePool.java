package com.lingganhezi.net;

import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Field;
import java.util.Comparator;
import java.util.Map;
import java.util.Queue;
import java.util.SortedSet;
import java.util.TreeSet;

import com.android.volley.Request;
import com.android.volley.RequestQueue;

/**
 * 请求队列池
 * 
 * @author chenzipeng
 *
 */
public class RequestQueuePool {

	/**
	 * 得到队列任务计数器字段
	 */
	private static Field mWaitingRequestField = null;
	static {
		Field[] fields = RequestQueue.class.getDeclaredFields();
		AccessibleObject.setAccessible(fields, true);
		for (Field f : fields) {
			if (f.getName() == "mWaitingRequests") {
				mWaitingRequestField = f;
				break;
			}
		}
	}

	/**
	 * 获取某个队列 等待的请求数量
	 * 
	 * @param queue
	 * @return
	 * @throws IllegalAccessException
	 * @throws IllegalArgumentException
	 */
	public static int getRequestWattingCount(RequestQueue queue) throws IllegalAccessException, IllegalArgumentException {
		Map<String, Queue<Request<?>>> requestsMap = (Map<String, Queue<Request<?>>>) mWaitingRequestField.get(queue);
		return requestsMap.size();
	}

	/**
	 * 请求队列比较器，根据 请求队列的等待数排列
	 */
	private final Comparator mComparator = new Comparator<RequestQueue>() {

		@Override
		public int compare(RequestQueue lhs, RequestQueue rhs) {
			if (lhs == rhs) {
				return 0;
			}

			try {
				return getRequestWattingCount(lhs) - getRequestWattingCount(rhs);
			} catch (Exception e) {
				return 0;
			}
		}
	};

	private SortedSet<RequestQueue> mQueueSet = new TreeSet<RequestQueue>(mComparator);

	private int mMaxSize = Integer.MAX_VALUE;

	/**
	 * 创建 一个请求队列池，最大值为Integer.MAX_VALUE
	 */
	public RequestQueuePool() {

	}

	/**
	 * 创建 一个请求队列池，最大值为size
	 * 
	 * @param size
	 */
	public RequestQueuePool(int size) {
		mMaxSize = size;
	}

	/**
	 * 获取 请求池里面较为空闲的
	 * 
	 * @return
	 */
	public RequestQueue getFreeRequestQueue() {
		return mQueueSet.first();
	}

	/**
	 * 注入请求队列 到 请求池
	 * 
	 * @param requestQueue
	 */
	public void put(RequestQueue requestQueue) {
		mQueueSet.add(requestQueue);
	}

}
