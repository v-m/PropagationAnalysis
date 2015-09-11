package com.vmusco.softminer.utils;

/**
 *
 * @author Vincenzo Musco - http://www.vmusco.com
 */
public class TypeWithInfo<T> implements Comparable<TypeWithInfo<T>>{
	private T value;
	private T info;

	public TypeWithInfo(T value, T info) {
		this.value = value;
		this.info = info;
	}

	@SuppressWarnings("unchecked")
	public int compareTo(TypeWithInfo<T> o) {
		return ((Comparable<T>)this.value).compareTo((T)o.value);
	}

	public T getValue() {
		return this.value;
	}

	public T getInfo() {
		return this.info;
	}
}
