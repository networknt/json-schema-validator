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

import java.io.IOException;
import java.io.InputStream;

import com.fasterxml.jackson.databind.JsonNode;
import com.networknt.schema.InputFormat;

/**
 * Reader for reading content to {@link JsonNode}.
 */
public interface JsonNodeReader {

    /**
     * Deserialize content as a tree.
     * 
     * @param content     the content
     * @param inputFormat the input format
     * @return the node
     * @throws IOException IOException
     */
    JsonNode readTree(String content, InputFormat inputFormat) throws IOException;

    /**
     * Deserialize content as a tree.
     * 
     * @param content input stream
     * @param inputFormat input format
     * @return the node
     * @throws IOException IOException
     */
    JsonNode readTree(InputStream content, InputFormat inputFormat) throws IOException;

    /**
     * Creates a builder for {@link JsonNodeReader}.
     *
     * @return the builder
     */
    static DefaultJsonNodeReader.Builder builder() {
        return DefaultJsonNodeReader.builder();
    }
}
