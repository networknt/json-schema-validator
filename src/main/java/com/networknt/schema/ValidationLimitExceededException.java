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

import java.io.InvalidObjectException;
import java.io.ObjectInputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.networknt.schema.path.NodePath;
import com.networknt.schema.path.PathType;

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

    // Serialize only location metadata, without making the shared path/location
    // model Serializable or rendering paths eagerly when the exception is thrown.
    private Object writeReplace() {
        return new SerializationProxy(this);
    }

    private void readObject(ObjectInputStream stream) throws InvalidObjectException {
        throw new InvalidObjectException("Serialization proxy required");
    }

    private static final class PathData implements Serializable {
        private static final long serialVersionUID = 1L;
        private final PathType type;
        private final List<Object> segments = new ArrayList<>();

        private PathData(NodePath path) {
            type = path.getPathType();
            for (NodePath current = path; current.getParent() != null; current = current.getParent()) {
                segments.add(current.getElement(-1));
            }
            Collections.reverse(segments);
        }

        private NodePath toPath() {
            NodePath path = new NodePath(type);
            for (Object segment : segments) {
                path = segment instanceof Integer ? path.append((Integer) segment) : path.append((String) segment);
            }
            return path;
        }

        private static PathData from(NodePath path) {
            return path == null ? null : new PathData(path);
        }

        private static NodePath toPath(PathData data) {
            return data == null ? null : data.toPath();
        }
    }

    private static final class SerializationProxy implements Serializable {
        private static final long serialVersionUID = 1L;
        private final LimitKind kind;
        private final long limit;
        private final long steps;
        private final long depth;
        private final String absoluteIri;
        private final PathData schemaFragment;
        private final PathData instance;
        private final PathData evaluation;
        private final StackTraceElement[] stackTrace;
        private final Throwable cause;
        private final Throwable[] suppressed;

        private SerializationProxy(ValidationLimitExceededException exception) {
            kind = exception.limitKind;
            limit = exception.limit;
            steps = exception.admittedSteps;
            depth = exception.activeDepth;
            SchemaLocation location = exception.schemaLocation;
            absoluteIri = location == null || location.getAbsoluteIri() == null ? null
                    : location.getAbsoluteIri().toString();
            schemaFragment = location == null ? null : PathData.from(location.getFragment());
            instance = PathData.from(exception.instanceLocation);
            evaluation = PathData.from(exception.evaluationPath);
            stackTrace = exception.getStackTrace();
            cause = exception.getCause();
            suppressed = exception.getSuppressed();
        }

        private Object readResolve() {
            SchemaLocation location = schemaFragment == null ? null : new SchemaLocation(
                    absoluteIri == null ? null : AbsoluteIri.of(absoluteIri), schemaFragment.toPath());
            ValidationLimitExceededException exception = new ValidationLimitExceededException(kind, limit, steps,
                    depth, location, PathData.toPath(instance), PathData.toPath(evaluation));
            exception.setStackTrace(stackTrace);
            exception.initCause(cause);
            for (Throwable failure : suppressed) {
                exception.addSuppressed(failure);
            }
            return exception;
        }
    }
}
