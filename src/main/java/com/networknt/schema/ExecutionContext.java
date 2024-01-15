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

import java.util.Stack;

/**
 * Stores the execution context for the validation run.
 */
public class ExecutionContext {
    private ExecutionConfig executionConfig;
    private CollectorContext collectorContext;
    private ValidatorState validatorState = null;
    private Stack<DiscriminatorContext> discriminatorContexts = new Stack<>();

    /**
     * Creates an execution context.
     */
    public ExecutionContext() {
        this(new CollectorContext());
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
        this(executionConfig, new CollectorContext());
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
        return collectorContext;
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

    /**
     * Gets the validator state.
     * 
     * @return the validator state
     */
    public ValidatorState getValidatorState() {
        return validatorState;
    }

    /**
     * Sets the validator state.
     * 
     * @param validatorState the validator state
     */
    public void setValidatorState(ValidatorState validatorState) {
        this.validatorState = validatorState;
    }

    public DiscriminatorContext getCurrentDiscriminatorContext() {
        if (!this.discriminatorContexts.empty()) {
            return this.discriminatorContexts.peek();
        }
        return null; // this is the case when we get on a schema that has a discriminator, but it's not used in anyOf
    }

    public void enterDiscriminatorContext(final DiscriminatorContext ctx, @SuppressWarnings("unused") JsonNodePath instanceLocation) {
        this.discriminatorContexts.push(ctx);
    }

    public void leaveDiscriminatorContextImmediately(@SuppressWarnings("unused") JsonNodePath instanceLocation) {
        this.discriminatorContexts.pop();
    }
}
