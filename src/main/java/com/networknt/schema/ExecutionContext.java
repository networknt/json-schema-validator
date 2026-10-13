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
    // Contexts are confined to one execution at a time. Cached schemas own no accounting.
    private EvaluationState evaluationState;
    private ValidationLimitExceededException evaluationAbort;

    private static final class EvaluationState {
        private final long maxSteps;
        private final int maxDepth;
        private long admittedSteps;
        private long activeDepth;

        private EvaluationState(ExecutionConfig config) {
            this.maxSteps = config.getMaxEvaluationSteps();
            this.maxDepth = config.getMaxEvaluationDepth();
        }
    }

    void enterEvaluation(SchemaLocation schemaLocation, NodePath instanceLocation, NodePath rootPath) {
        checkEvaluationAborted();
        if (evaluationState == null) {
            if (!evaluationSchema.isEmpty()) {
                // An execution that started unlimited stays unlimited, even if a
                // callback changes configuration. No per-execution state is allocated.
                return;
            }
            if (getExecutionConfig().getMaxEvaluationSteps() == 0
                    && getExecutionConfig().getMaxEvaluationDepth() == 0) {
                evaluationPath = rootPath;
                return;
            }
            evaluationState = new EvaluationState(getExecutionConfig());
        }
        EvaluationState state = evaluationState;
        NodePath path = state.activeDepth == 0 || evaluationPath == null ? rootPath : evaluationPath;
        if (state.maxDepth != 0 && state.activeDepth >= state.maxDepth) {
            abortEvaluation(ValidationLimitExceededException.LimitKind.EVALUATION_DEPTH, state.maxDepth,
                    schemaLocation, instanceLocation, path);
        }
        admitStep(schemaLocation, instanceLocation, path, null);
        state.activeDepth++;
        // Only the outermost admitted entry initializes the path. Reentrant convenience
        // calls must retain the active path and budget.
        evaluationPath = path;
    }

    void admitKeyword(SchemaLocation schemaLocation, NodePath instanceLocation, String keyword) {
        checkEvaluationAborted();
        admitStep(schemaLocation, instanceLocation, evaluationPath, keyword);
    }

    boolean isEvaluationLimited() {
        return evaluationState != null;
    }

    private void admitStep(SchemaLocation schemaLocation, NodePath instanceLocation, NodePath path, String keyword) {
        EvaluationState state = evaluationState;
        if (state.maxSteps != 0 && state.admittedSteps >= state.maxSteps) {
            abortEvaluation(ValidationLimitExceededException.LimitKind.EVALUATION_STEPS, state.maxSteps,
                    schemaLocation, instanceLocation, keyword == null ? path : path.append(keyword));
        }
        if (state.admittedSteps != Long.MAX_VALUE) {
            state.admittedSteps++;
        }
    }

    private void abortEvaluation(ValidationLimitExceededException.LimitKind kind, long limit,
            SchemaLocation schemaLocation, NodePath instanceLocation, NodePath path) {
        evaluationAbort = new ValidationLimitExceededException(kind, limit, evaluationState.admittedSteps,
                evaluationState.activeDepth, schemaLocation, instanceLocation, path);
        throw evaluationAbort;
    }

    void exitEvaluation() {
        if (evaluationState == null) {
            return;
        }
        if (--evaluationState.activeDepth == 0) {
            evaluationState = null;
        }
        // Also catches extensions that swallowed exhaustion on the last dispatch.
        checkEvaluationAborted();
    }

    void checkEvaluationAborted() {
        if (evaluationAbort != null) {
            throw evaluationAbort;
        }
    }

    /**
     * Indicates that evaluation stopped without a validity verdict. In particular,
     * post-walk listeners must check this before interpreting their error slice.
     *
     * @return true once this context has exhausted a limit
     */
    public boolean isEvaluationAborted() {
        return evaluationAbort != null;
    }

    /**
     * Gets the incomplete-execution outcome, including the denied attempt's locations.
     * This remains available during unwinding and after the context becomes terminal.
     *
     * @return the limit exception, or null if no limit was exhausted
     */
    public ValidationLimitExceededException getEvaluationAbort() {
        return evaluationAbort;
    }

    void preserveEvaluationAbort(Throwable failure) {
        if (evaluationAbort != null) {
            if (failure != evaluationAbort) {
                evaluationAbort.addSuppressed(failure);
            }
            throw evaluationAbort;
        }
    }

    private ExecutionConfig executionConfig;
    private WalkConfig walkConfig = null;
    private CollectorContext collectorContext = null;

    private Annotations annotations = null;
//    private InstanceResults instanceResults = null;
    private List<Error> errors = new ArrayList<>();

    private final Map<NodePath, DiscriminatorState> discriminatorMapping = new HashMap<>();
    
    NodePath evaluationPath;
    final ArrayDeque<Schema> evaluationSchema = new ArrayDeque<>();
    final ArrayDeque<Object> evaluationSchemaPath = new ArrayDeque<>();
    
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
        checkEvaluationAborted();
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

    /**
     * Gets the current assertion diagnostics. Exhausted contexts have no result.
     * Previously obtained references must be discarded if evaluation aborts.
     *
     * @return the assertion diagnostics
     * @throws ValidationLimitExceededException if evaluation was aborted
     */
    public List<Error> getErrors() {
        checkEvaluationAborted();
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
