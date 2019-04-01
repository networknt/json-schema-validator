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

import java.util.Collection;
import java.util.Collections;
import java.util.Map;

import com.fasterxml.jackson.databind.JsonNode;

public class FormatKeyword implements Keyword {
    private final ValidatorTypeCode type;
    private final Map<String, Format> formats;

    public FormatKeyword(ValidatorTypeCode type, Map<String, Format> formats) {
        this.type = type;
        this.formats = formats;
    }

    Collection<Format> getFormats() {
        return Collections.unmodifiableCollection(formats.values());
    }
    
    @Override
    public JsonValidator newValidator(String schemaPath, JsonNode schemaNode, JsonSchema parentSchema, ValidationContext validationContext)
            throws Exception {
        Format format = null;
        if (schemaNode != null && schemaNode.isTextual()) {
            String formatName = schemaNode.textValue();
            format = formats.get(formatName);
        }

        return new FormatValidator(schemaPath, schemaNode, parentSchema, validationContext, format);
    }
    
    @Override
    public String getValue() {
        return type.getValue();
    }
    
    
}
