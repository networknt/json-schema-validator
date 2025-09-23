/*
 * Copyright (c) 2020 Network New Technologies Inc.
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

import java.util.List;

/**
 * Represents a validation result.
 */
public class Result {
    private final ExecutionContext executionContext;

    public Result(ExecutionContext executionContext) {
        super();
        this.executionContext = executionContext;
    }

    public List<Error> getErrors() {
        return getExecutionContext().getErrors();
    }

    public ExecutionContext getExecutionContext() {
        return executionContext;
    }

    public CollectorContext getCollectorContext() {
        return getExecutionContext().getCollectorContext();
    }
}
