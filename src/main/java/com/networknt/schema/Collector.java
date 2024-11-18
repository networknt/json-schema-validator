/*
 * Copyright (c) 2020 Network New Technologies Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.networknt.schema;

/**
 * Basic interface that allows the implementers to collect the information and
 * return it.
 *
 * @param <E> element
 */
public interface Collector<E> {


    /**
     * This method should be called by the intermediate touch points that want to
     * combine the data being collected by this collector. This is an optional
     * method and could be used when the same collector is used for collecting data
     * at multiple touch points or accumulating data at same touch point.
	 * @param object Object
     */
    void combine(Object object);

    /**
     * Final method called by the framework that returns the actual collected data.
     * If the collector is not accumulating data or being used to collect data at
     * multiple touch points, only this method can be implemented.
	 * @return E element
     */
    E collect();


}
