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

/**
 * Represents an error when processing the JsonSchema.
 */
public class JsonSchemaException extends RuntimeException {
    private static final long serialVersionUID = -7805792737596582110L;
    private final ValidationMessage validationMessage;

    public JsonSchemaException(ValidationMessage validationMessage) {
        this.validationMessage = validationMessage;
    }

    public JsonSchemaException(String message) {
        super(message);
        this.validationMessage = null;
    }

    public JsonSchemaException(Throwable throwable) {
        super(throwable);
        this.validationMessage = null;
    }

    @Override
    public String getMessage() {
        return this.validationMessage != null ? this.validationMessage.getMessage() : super.getMessage();
    }

    public ValidationMessage getValidationMessage() {
        return this.validationMessage;
    }

    public Set<ValidationMessage> getValidationMessages() {
        if (validationMessage == null) {
            return Collections.emptySet();
        }
        return Collections.singleton(validationMessage);
    }
}
