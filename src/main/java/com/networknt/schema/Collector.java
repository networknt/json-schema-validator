package com.networknt.schema;

/**
 * Basic interface that allows the implementers to collect the information and
 * return it.
 *
 * @param <E>
 */
public interface Collector<E> {

	public E collect();

}
