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
package com.networknt.schema.serialization.node;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;

/**
 * {@link JsonNodeFactoryFactory} that produces {@link LocationJsonNodeFactory}.
 * <p>
 * Note that this will adversely affect performance as nodes with the same value
 * can no longer be cached and reused.
 */
public class LocationJsonNodeFactoryFactory implements JsonNodeFactoryFactory {
    
    private static final LocationJsonNodeFactoryFactory INSTANCE = new LocationJsonNodeFactoryFactory();
    
    public static LocationJsonNodeFactoryFactory getInstance() {
        return INSTANCE;
    }

    @Override
    public JsonNodeFactory getJsonNodeFactory(JsonParser jsonParser) {
        return new LocationJsonNodeFactory(jsonParser);
    }
}