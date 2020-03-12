package com.networknt.schema;

public abstract class AbstractCollector<E> implements Collector<E>{

	@Override
	public void combine(Object object) {
		// Do nothing. This is the default Implementation.
	}
}
