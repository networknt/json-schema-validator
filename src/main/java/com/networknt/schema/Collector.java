package com.networknt.schema;

/**
 * Basic interface that allows the implementers to collect the information and
 * return it.
 *
 * @param <E>
 */
public interface Collector<E> {

	/**
	 * This method should be called by the intermediate touch points that want to
	 * combine the data being collected by this collector. This is an optional
	 * method and could be used when the same collector is used for collecting data
	 * at multiple touch points or accumulating data at same touch point.
	 */
	public default void combine(Object object) {
	};

	/**
	 * Final method called by the framework that returns the actual collected data.
	 * If the collector is not accumulating data or being used to collect data at
	 * multiple touch points, only this method can be implemented.
	 */
	public E collect();

}
