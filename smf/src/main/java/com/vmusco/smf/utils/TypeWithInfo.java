package com.vmusco.smf.utils;

public class TypeWithInfo<T> implements Comparable<TypeWithInfo<T>>{
	private T value;
	private T info;

	public TypeWithInfo(T value, T info) {
		this.value = value;
		this.info = info;
	}

	public int compareTo(TypeWithInfo<T> o) {
		return ((Comparable)this.value).compareTo(o.value);
	}

	public T getValue() {
		return this.value;
	}

	public T getInfo() {
		return this.info;
	}
}
