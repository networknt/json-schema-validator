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
package com.networknt.schema;

/**
 * A {@link JsonMetaSchemaFactory} that does not meta-schemas that aren't
 * explicitly configured in the {@link JsonSchemaFactory}.
 */
public class DisallowUnknownJsonMetaSchemaFactory implements JsonMetaSchemaFactory {
    @Override
    public JsonMetaSchema getMetaSchema(String iri, JsonSchemaFactory schemaFactory, SchemaValidatorsConfig config) {
        throw new InvalidSchemaException(ValidationMessage.builder()
                .message("Unknown meta-schema ''{1}''. Only meta-schemas that are explicitly configured can be used.")
                .arguments(iri).build());
    }

    private static class Holder {
        private static final DisallowUnknownJsonMetaSchemaFactory INSTANCE = new DisallowUnknownJsonMetaSchemaFactory();
    }

    /**
     * Gets the instance of {@link DisallowUnknownJsonMetaSchemaFactory}.
     * 
     * @return the json meta schema factory
     */
    public static DisallowUnknownJsonMetaSchemaFactory getInstance() {
        return Holder.INSTANCE;
    }
}
