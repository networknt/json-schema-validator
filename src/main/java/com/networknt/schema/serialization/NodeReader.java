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
 * Built-in readers use the same numeric policy for schemas and instances: lossy
 * floating values under the eight count keyword names become decimal nodes.
 * Other numbers keep the configured mapper/factory policy. Numeric values in
 * externally supplied JsonNodes are not reparsed; a rounded double cannot recover
 * the exact decimal text and may differ from a decimal literal in const or enum.
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
     * Reads a schema. Built-in readers promote count-named floating values to
     * decimal nodes when their shortest double decimal would lose precision.
     * This includes count-named fields in literal data, since references may use
     * those objects as schemas. Copies and moved nodes retain their precision.
     * Values outside BigDecimal's range retain ordinary nonfinite parsing.
     * Custom readers and node factories retain their own numeric policies.
     */
    default JsonNode readSchema(String content, InputFormat inputFormat) {
        return readTree(content, inputFormat);
    }

    /** Stream counterpart of {@link #readSchema(String, InputFormat)}. */
    default JsonNode readSchema(InputStream content, InputFormat inputFormat) {
        return readTree(content, inputFormat);
    }
}
