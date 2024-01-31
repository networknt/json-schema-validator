/*
 * Copyright (c) 2016 Network New Technologies Inc.
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
 * Used to implement the various formats for the format keyword.
 */
public interface Format {
    /**
     * @return the format name as referred to in a json schema format node.
     */
    String getName();

    /**
     * Determines if the value matches the format.
     * 
     * @param executionContext the execution context
     * @param value            to match
     * @return true if matches
     */
    boolean matches(ExecutionContext executionContext, String value);

    String getErrorMessageDescription();
}
