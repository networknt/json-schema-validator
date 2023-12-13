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

package com.networknt.schema;

/**
 * Stores the execution context for the validation run.
 */
public class ExecutionContext {
    private ExecutionConfig executionConfig;
    private CollectorContext collectorContext;

    public ExecutionContext() {
        this(new CollectorContext());
    }

    public ExecutionContext(CollectorContext collectorContext) {
        this(new ExecutionConfig(), collectorContext);
    }
    
    public ExecutionContext(ExecutionConfig executionConfig) {
        this(executionConfig, new CollectorContext());
    }
    
    public ExecutionContext(ExecutionConfig executionConfig, CollectorContext collectorContext) {
        this.collectorContext = collectorContext;
        this.executionConfig = executionConfig;
    }

    public CollectorContext getCollectorContext() {
        return collectorContext;
    }

    public void setCollectorContext(CollectorContext collectorContext) {
        this.collectorContext = collectorContext;
    }

    public ExecutionConfig getExecutionConfig() {
        return executionConfig;
    }

    public void setExecutionConfig(ExecutionConfig executionConfig) {
        this.executionConfig = executionConfig;
    }
}
