/*
 * Copyright (c) 2023 the original author or authors.
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
package com.networknt.schema.result;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.networknt.schema.JsonNodePath;
import com.networknt.schema.SchemaLocation;

/**
 * Sub schema results.
 */
public class JsonNodeResults {

    /**
     * Stores the invalid results.
     */
    private final Map<JsonNodePath, List<JsonNodeResult>> values = new HashMap<>();

    public void setResult(JsonNodePath instanceLocation, SchemaLocation schemaLocation, JsonNodePath evaluationPath,
            boolean valid) {
        JsonNodeResult result = new JsonNodeResult(instanceLocation, schemaLocation, evaluationPath, valid);
        List<JsonNodeResult> v = values.computeIfAbsent(instanceLocation, k -> new ArrayList<>());
        v.add(result);
    }

    public boolean isValid(JsonNodePath instanceLocation, JsonNodePath evaluationPath) {
        List<JsonNodeResult> instance = values.get(instanceLocation);
        if (instance != null) {
            for (JsonNodeResult result : instance) {
                if (evaluationPath.startsWith(result.getEvaluationPath())) {
                    if(!result.isValid()) {
                        return false;
                    }
                }
            }
        }
        return true;
    }

}
