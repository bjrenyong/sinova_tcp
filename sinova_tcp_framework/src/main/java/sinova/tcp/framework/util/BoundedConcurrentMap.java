package sinova.tcp.framework.util;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

/**
 * 有边界限制的并发MAP<br/>
 * @param <K> Map的key
 * @param <V> Map的value
 */
public class BoundedConcurrentMap<K, V> {
	private Map<K, V> map = null;
	private final Semaphore sem;

	/**
	 * 构造方法
	 * @param bound 边界值
	 */
	public BoundedConcurrentMap(int bound) {
		this.map = new ConcurrentHashMap<K, V>(bound);
		this.sem = new Semaphore(bound);
	}

	/**
	 * 向MAP放入一组值，如阻塞则挂起直到获取到信号
	 * @param key 键
	 * @param value 值
	 * @return 操作是否成功（实际上总是成功）
	 * @throws InterruptedException
	 */
	public boolean put(K key, V value) throws InterruptedException {
		this.sem.acquire();
		V oldValue = null;
		try {
			oldValue = this.map.put(key, value);
			return true;
		} finally {
			if (oldValue != null) {
				this.sem.release();
			}
		}
	}

	/**
	 * 向MAP放入一组值，如阻塞则挂起一段时间
	 * @param key 键
	 * @param value 值
	 * @param waitInMillis 阻塞等待的毫秒数
	 * @return 操作是否成功
	 * @throws InterruptedException
	 */
	public boolean put(K key, V value, long waitInMillis) throws InterruptedException {
		boolean acquire = this.sem.tryAcquire(waitInMillis, TimeUnit.MILLISECONDS);
		if (acquire) {
			V oldValue = null;
			try {
				oldValue = this.map.put(key, value);
			} finally {
				if (oldValue != null) {
					this.sem.release();
				}
			}
		}
		return acquire;
	}

	public V get(K key) {
		return map.get(key);
	}

	public V remove(K key) {
		V value = this.map.remove(key);
		if (value != null) {
			this.sem.release();
		}
		return value;
	}

	public List<V> removeAll() {
		List<V> values = new ArrayList<V>();
		Iterator<K> keyIter = this.map.keySet().iterator();
		while (keyIter.hasNext()) {
			V value = remove(keyIter.next());
			if(value!=null){
				values.add(value);
			}
		}
		return values;
	}

	public Map<K, V> getMap() {
		return map;
	}

	public Set<K> getKeys() {
		return map.keySet();
	}
}
