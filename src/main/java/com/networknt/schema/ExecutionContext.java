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

import com.networknt.schema.annotation.JsonNodeAnnotations;
import com.networknt.schema.result.JsonNodeResults;

import java.util.Stack;

/**
 * Stores the execution context for the validation run.
 */
public class ExecutionContext {
    private ExecutionConfig executionConfig;
    private CollectorContext collectorContext = null;
    private Stack<DiscriminatorContext> discriminatorContexts = null;
    private JsonNodeAnnotations annotations = null;
    private JsonNodeResults results = null;
    
    /**
     * This is used during the execution to determine if the validator should fail fast.
     * <p>
     * This valid is determined by the previous validator.
     */
    private Boolean failFast = null;

    /**
     * Creates an execution context.
     */
    public ExecutionContext() {
        this(new ExecutionConfig(), null);
    }

    /**
     * Creates an execution context.
     * 
     * @param collectorContext the collector context
     */
    public ExecutionContext(CollectorContext collectorContext) {
        this(new ExecutionConfig(), collectorContext);
    }

    /**
     * Creates an execution context.
     * 
     * @param executionConfig the execution configuration
     */
    public ExecutionContext(ExecutionConfig executionConfig) {
        this(executionConfig, null);
    }

    /**
     * Creates an execution context.
     * 
     * @param executionConfig  the execution configuration
     * @param collectorContext the collector context
     */
    public ExecutionContext(ExecutionConfig executionConfig, CollectorContext collectorContext) {
        this.collectorContext = collectorContext;
        this.executionConfig = executionConfig;
    }

    /**
     * Gets the collector context.
     * 
     * @return the collector context
     */
    public CollectorContext getCollectorContext() {
        if (this.collectorContext == null) {
            this.collectorContext = new CollectorContext();
        }
        return this.collectorContext;
    }

    /**
     * Sets the collector context.
     * 
     * @param collectorContext the collector context
     */
    public void setCollectorContext(CollectorContext collectorContext) {
        this.collectorContext = collectorContext;
    }

    /**
     * Gets the execution configuration.
     * 
     * @return the execution configuration
     */
    public ExecutionConfig getExecutionConfig() {
        return executionConfig;
    }

    /**
     * Sets the execution configuration.
     * 
     * @param executionConfig the execution configuration
     */
    public void setExecutionConfig(ExecutionConfig executionConfig) {
        this.executionConfig = executionConfig;
    }

    public JsonNodeAnnotations getAnnotations() {
        if (this.annotations == null) {
            this.annotations = new JsonNodeAnnotations();
        }
        return annotations;
    }

    public JsonNodeResults getResults() {
        if (this.results == null) {
            this.results = new JsonNodeResults();
        }
        return results;
    }

    /**
     * Determines if the validator should immediately throw a fail fast exception if
     * an error has occurred.
     * <p>
     * This defaults to the execution config fail fast at the start of the execution.
     * 
     * @return true if fail fast
     */
    public boolean isFailFast() {
        if (this.failFast == null) {
            this.failFast = getExecutionConfig().isFailFast();
        }
        return failFast;
    }

    /**
     * Sets if the validator should immediately throw a fail fast exception if an
     * error has occurred.
     * 
     * @param failFast true to fail fast
     */
    public void setFailFast(boolean failFast) {
        this.failFast = failFast;
    }

    public DiscriminatorContext getCurrentDiscriminatorContext() {
        if (this.discriminatorContexts == null) {
            return null;
        }

        if (!this.discriminatorContexts.empty()) {
            return this.discriminatorContexts.peek();
        }
        return null; // this is the case when we get on a schema that has a discriminator, but it's not used in anyOf
    }

    public void enterDiscriminatorContext(final DiscriminatorContext ctx, @SuppressWarnings("unused") JsonNodePath instanceLocation) {
        if (this.discriminatorContexts == null) {
            this.discriminatorContexts = new Stack<>();
        }
        this.discriminatorContexts.push(ctx);
    }

    public void leaveDiscriminatorContextImmediately(@SuppressWarnings("unused") JsonNodePath instanceLocation) {
        this.discriminatorContexts.pop();
    }
}
