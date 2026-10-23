/*
 * Copyright (c) 2024 the original author or authors.
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
package com.networknt.schema.serialization;

import java.io.InputStream;

import tools.jackson.databind.JsonNode;
import com.networknt.schema.InputFormat;

/**
 * Reader for reading content to {@link JsonNode}.
 * Built-in readers preserve finite floating-point literals as decimal nodes when
 * their written decimal differs from the shortest double representation. Lossless
 * doubles, including signed zero, keep their ordinary representation. Overflow
 * retains ordinary nonfinite parsing, except for the existing exact count policy.
 * Custom readers and node factories retain their numeric policies. Values already
 * rounded in externally supplied JsonNodes cannot recover their source digits.
 */
public interface NodeReader {

    /**
     * Deserialize content as a tree.
     * 
     * @param content     the content
     * @param inputFormat the input format
     * @return the node
     */
    JsonNode readTree(String content, InputFormat inputFormat);

    /**
     * Deserialize content as a tree.
     * 
     * @param content input stream
     * @param inputFormat input format
     * @return the node
     */
    JsonNode readTree(InputStream content, InputFormat inputFormat);

    /**
     * Creates a builder for {@link NodeReader}.
     *
     * @return the builder
     */
    static DefaultNodeReader.Builder builder() {
        return DefaultNodeReader.builder();
    }
    /**
     * Reads a schema using the configured reader policy. Built-in readers apply
     * the same selective decimal precision to schemas and instances, including
     * literal data, defaults and examples. Copies retain their stored precision.
     * Explicit exact-decimal mapper settings bypass selective double decoding.
     */
    default JsonNode readSchema(String content, InputFormat inputFormat) {
        return readTree(content, inputFormat);
    }

    /** Stream counterpart of {@link #readSchema(String, InputFormat)}. */
    default JsonNode readSchema(InputStream content, InputFormat inputFormat) {
        return readTree(content, inputFormat);
    }
}
