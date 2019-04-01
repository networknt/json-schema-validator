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
import java.util.Map;
import java.util.Set;

import com.fasterxml.jackson.databind.JsonNode;

public abstract class AbstractJsonValidator implements JsonValidator {
    private final String keyword;
    protected AbstractJsonValidator(String keyword) {
        this.keyword = keyword;
    }
    public Set<ValidationMessage> validate(JsonNode node) {
        return validate(node, node, AT_ROOT);
    }
    
    protected ValidationMessage buildValidationMessage(ErrorMessageType errorMessageType, String at, String... arguments) {
        return ValidationMessage.of(keyword, errorMessageType, at, arguments);
    }
    protected ValidationMessage buildValidationMessage(ErrorMessageType errorMessageType, String at, Map<String, Object> details) {
        return ValidationMessage.of(keyword, errorMessageType, at, details);
    }

    protected Set<ValidationMessage> pass() {
        return Collections.emptySet();
    }
    
    protected Set<ValidationMessage> fail(ErrorMessageType errorMessageType, String at, Map<String, Object> details) {
        return Collections.singleton(buildValidationMessage(errorMessageType, at, details));
    }
    
    protected Set<ValidationMessage> fail(ErrorMessageType errorMessageType, String at, String...arguments) {
        return Collections.singleton(buildValidationMessage(errorMessageType, at, arguments));
    }
}
