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

public interface ErrorMessageType {
    /**
     * Your error code. Please ensure global uniqueness. Builtin error codes are sequential numbers.
     * <p>
     * Customer error codes could have a prefix to denote the namespace of your custom keywords and errors.
     *
     * @return error code
     */
    String getErrorCode();

    default String getCustomMessage() {
        return null;
    }

    /**
     * Get the text representation of the error code.
     *
     * @return The error code value.
     */
    default String getErrorCodeValue() {
        return getErrorCode();
    }

}
