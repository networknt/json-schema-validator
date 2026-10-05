/*
 * Copyright (c) 2026 the original author or authors.
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

import com.networknt.schema.path.NodePath;

/**
 * Validation or walking did not complete because an execution limit was exceeded.
 * This is an operational outcome, not an assertion that the instance is invalid.
 * The exhausted execution context is terminal; retry with a new context.
 */
public class ValidationLimitExceededException extends RuntimeException {
    private static final long serialVersionUID = 1L;

    /** The execution capacity that refused the attempt. */
    public enum LimitKind {
        /** Schema-entry and keyword-dispatch capacity. */
        EVALUATION_STEPS,
        /** Simultaneously active schema-frame capacity. */
        EVALUATION_DEPTH
    }

    private final LimitKind limitKind;
    private final long limit;
    private final long admittedSteps;
    private final long activeDepth;
    private final SchemaLocation schemaLocation;
    private final NodePath instanceLocation;
    private final NodePath evaluationPath;

    /**
     * Creates an incomplete-execution outcome without retaining instance or schema trees.
     *
     * @param limitKind the exhausted capacity
     * @param limit the configured capacity
     * @param admittedSteps steps admitted before refusal
     * @param activeDepth schema frames already active at refusal
     * @param schemaLocation schema location of the denied attempt
     * @param instanceLocation instance location of the denied attempt
     * @param evaluationPath evaluation path of the denied attempt
     */
    public ValidationLimitExceededException(LimitKind limitKind, long limit, long admittedSteps, long activeDepth,
            SchemaLocation schemaLocation, NodePath instanceLocation, NodePath evaluationPath) {
        // Do not eagerly format potentially large paths or retain schema/instance trees.
        super("Validation did not complete: " + limitKind + " limit " + limit + " exceeded");
        this.limitKind = limitKind;
        this.limit = limit;
        this.admittedSteps = admittedSteps;
        this.activeDepth = activeDepth;
        this.schemaLocation = schemaLocation;
        this.instanceLocation = instanceLocation;
        this.evaluationPath = evaluationPath;
    }

    /** @return the exhausted capacity */
    public LimitKind getLimitKind() {
        return limitKind;
    }

    /** @return the configured capacity */
    public long getLimit() {
        return limit;
    }

    /** @return steps admitted before refusal, saturating at {@link Long#MAX_VALUE} */
    public long getAdmittedSteps() {
        return admittedSteps;
    }

    /** @return frames already active at refusal; a denied schema entry would add one */
    public long getActiveDepth() {
        return activeDepth;
    }

    /** @return the schema location of the denied attempt */
    public SchemaLocation getSchemaLocation() {
        return schemaLocation;
    }

    /** @return the instance location of the denied attempt */
    public NodePath getInstanceLocation() {
        return instanceLocation;
    }

    /** @return the evaluation path of the denied attempt */
    public NodePath getEvaluationPath() {
        return evaluationPath;
    }
}
