/*
 * Copyright (c) 2025 the original author or authors.
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
import com.fasterxml.jackson.databind.ObjectMapper;
import com.networknt.schema.InputFormat;

/**
 * BasicJsonNodeReader.
 */
public class BasicJsonNodeReader implements JsonNodeReader {
    private static class Holder {
        private static final BasicJsonNodeReader INSTANCE = new BasicJsonNodeReader();
    }

    public static BasicJsonNodeReader getInstance() {
        return Holder.INSTANCE;
    }

    protected BasicJsonNodeReader() {
    }

    @Override
    public JsonNode readTree(String content, InputFormat inputFormat) throws IOException {
        return getObjectMapper(inputFormat).readTree(content);
    }

    @Override
    public JsonNode readTree(InputStream content, InputFormat inputFormat) throws IOException {
        return getObjectMapper(inputFormat).readTree(content);
    }

    /**
     * Gets the object mapper for the input format.
     * 
     * @param inputFormat the input format
     * @return the object mapper
     */
    protected ObjectMapper getObjectMapper(InputFormat inputFormat) {
        if (InputFormat.JSON.equals(inputFormat)) {
            return JsonMapperFactory.getInstance();
        } else if (InputFormat.YAML.equals(inputFormat)) {
            return YamlMapperFactory.getInstance();
        }
        throw new IllegalArgumentException("Unsupported input format "+inputFormat); 
    }
}
