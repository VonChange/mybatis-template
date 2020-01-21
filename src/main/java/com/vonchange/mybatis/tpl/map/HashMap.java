package com.vonchange.mybatis.tpl.map;

import java.util.Map;

/**
 *支持链式调用的HashMap
 * @author von_change@163.com
 * @date 2015-6-14 下午10:37:59
 * @param <K>
 * @param <V>
 */
public class HashMap<K, V> extends java.util.HashMap<K, V> implements Map<K, V> {

	private static final long serialVersionUID = -7709853214708221134L;

	public HashMap() {
		super();
	}

	public HashMap(Map<? extends K, ? extends V> map) {
		super(map);
	}

	public HashMap<K, V> set(K key, V value) {
		super.put(key, value);
		return this;
	}
}
