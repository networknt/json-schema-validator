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

public abstract class AbstractJsonValidator implements JsonValidator {
    private final JsonNodePath schemaLocation;
    private final JsonNodePath evaluationPath;
    private final Keyword keyword;

    public AbstractJsonValidator(JsonNodePath schemaLocation, JsonNodePath evaluationPath, Keyword keyword) {
        this.schemaLocation = schemaLocation;
        this.evaluationPath = evaluationPath;
        this.keyword = keyword;
    }

    @Override
    public JsonNodePath getSchemaLocation() {
        return schemaLocation;
    }

    @Override
    public JsonNodePath getEvaluationPath() {
        return evaluationPath;
    }

    @Override
    public String getKeyword() {
        return keyword.getValue();
    }

    @Override
    public String toString() {
        return getEvaluationPath().getName(-1);
    }
}
