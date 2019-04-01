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

import java.util.Collections;
import java.util.Set;

import com.fasterxml.jackson.databind.JsonNode;

/**
 * Used for Keywords that have no validation aspect, but are part of the metaschema.
 */
public class NonValidationKeyword extends AbstractKeyword {

    private static final class Validator extends AbstractJsonValidator {
        private Validator(String keyword) {
            super(keyword);
        }

        @Override
        public Set<ValidationMessage> validate(JsonNode node, JsonNode rootNode, String at) {
            return Collections.emptySet();
        }
    }

    public NonValidationKeyword(String keyword) {
        super(keyword);
    }
    
    @Override
    public JsonValidator newValidator(String schemaPath, JsonNode schemaNode, JsonSchema parentSchema,
            ValidationContext validationContext) throws JsonSchemaException, Exception {
        return new Validator(getValue());
    }
}
