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

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

import com.networknt.schema.annotation.Annotations;
import com.networknt.schema.keyword.DiscriminatorState;
import com.networknt.schema.path.NodePath;
//import com.networknt.schema.result.InstanceResults;
import com.networknt.schema.walk.WalkConfig;

/**
 * Stores the execution context for the validation run.
 */
public class ExecutionContext {
    private ExecutionConfig executionConfig;
    private WalkConfig walkConfig = null;
    private CollectorContext collectorContext = null;

    private Annotations annotations = null;
//    private InstanceResults instanceResults = null;
    private List<Error> errors = new ArrayList<>();

    private final Map<NodePath, DiscriminatorState> discriminatorMapping = new HashMap<>();
    
    NodePath evaluationPath;
    final ArrayDeque<Schema> evaluationSchema = new ArrayDeque<>(64);
    final ArrayDeque<Object> evaluationSchemaPath = new ArrayDeque<>(64);
    
    public NodePath getEvaluationPath() {
        return evaluationPath;
    }

    public void evaluationPathAddLast(String token) {
        this.evaluationPath = evaluationPath.append(token);
    }
    
    public void evaluationPathAddLast(int token) {
        this.evaluationPath = evaluationPath.append(token);
    }

    public void evaluationPathRemoveLast() {
        this.evaluationPath = evaluationPath.getParent();
    }


    public ArrayDeque<Schema> getEvaluationSchema() {
        return evaluationSchema;
    }
    
    public ArrayDeque<Object> getEvaluationSchemaPath() {
        return evaluationSchemaPath;
    }

    public Map<NodePath, DiscriminatorState> getDiscriminatorMapping() {
		return discriminatorMapping;
	}

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
        this(ExecutionConfig.getInstance(), null);
    }

    /**
     * Creates an execution context.
     * 
     * @param collectorContext the collector context
     */
    public ExecutionContext(CollectorContext collectorContext) {
        this(ExecutionConfig.getInstance(), collectorContext);
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
     * Sets the walk configuration.
     * 
     * @param walkConfig the walk configuration
     */
    public void setWalkConfig(WalkConfig walkConfig) {
        this.walkConfig = walkConfig;
    }

    /**
     * Gets the walk configuration.
     * 
     * @return the walk configuration
     */
    public WalkConfig getWalkConfig() {
        if (this.walkConfig == null) {
            this.walkConfig = WalkConfig.getInstance();
        }
        return this.walkConfig;
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

    public Annotations getAnnotations() {
        if (this.annotations == null) {
            this.annotations = new Annotations();
        }
        return annotations;
    }

//    public InstanceResults getInstanceResults() {
//        if (this.instanceResults == null) {
//            this.instanceResults = new InstanceResults();
//        }
//        return instanceResults;
//    }

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

    public List<Error> getErrors() {
        return this.errors;
    }

    public void addError(Error error) {
        this.errors.add(error);
        if (this.isFailFast()) {
            throw new FailFastAssertionException(error);
        }
    }

    public void setErrors(List<Error> errors) {
        this.errors = errors;
    }

    /**
     * Customize the execution configuration.
     *
     * @param customizer the customizer
     */
    public void executionConfig(Consumer<ExecutionConfig.Builder> customizer) {
    	ExecutionConfig.Builder builder = ExecutionConfig.builder(this.getExecutionConfig());
    	customizer.accept(builder);
    	this.executionConfig = builder.build();
    }

    /**
     * Customize the walk configuration.
     * 
     * @param customizer the customizer
     */
    public void walkConfig(Consumer<WalkConfig.Builder> customizer) {
    	WalkConfig.Builder builder = WalkConfig.builder(this.getWalkConfig());
    	customizer.accept(builder);
    	this.walkConfig = builder.build();
    }
    
    boolean unevaluatedPropertiesPresent = false;
    
    boolean unevaluatedItemsPresent = false;
    
    public boolean isUnevaluatedPropertiesPresent() {
        return this.unevaluatedPropertiesPresent;
    }
    
    public boolean isUnevaluatedItemsPresent() {
        return this.unevaluatedItemsPresent;
    }
    
    public void setUnevaluatedPropertiesPresent(boolean set) {
        this.unevaluatedPropertiesPresent = set;
    }
    
    public void setUnevaluatedItemsPresent(boolean set) {
        this.unevaluatedItemsPresent = set;
    }
}
