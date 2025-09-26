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

import com.networknt.schema.SchemaLocation;
import com.networknt.schema.path.NodePath;

/**
 * Instance results.
 */
public class InstanceResults {

    /**
     * Stores the invalid results.
     */
    private final Map<NodePath, List<InstanceResult>> values = new HashMap<>();

    public void setResult(NodePath instanceLocation, SchemaLocation schemaLocation, NodePath evaluationPath,
            boolean valid) {
        InstanceResult result = new InstanceResult(instanceLocation, schemaLocation, evaluationPath, valid);
        List<InstanceResult> v = values.computeIfAbsent(instanceLocation, k -> new ArrayList<>());
        v.add(result);
    }

    public boolean isValid(NodePath instanceLocation, NodePath evaluationPath) {
        List<InstanceResult> instance = values.get(instanceLocation);
        if (instance != null) {
            for (InstanceResult result : instance) {
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
