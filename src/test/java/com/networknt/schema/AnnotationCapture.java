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

import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;

import com.networknt.schema.annotation.Annotation;
import com.networknt.schema.path.NodePath;

/**
 * Collects annotations through the public API for the tests that assert on them.
 * <p>
 * The readOnly and writeOnly tests and the annotation keyword tests both need to
 * validate an instance and then inspect what was collected at an instance location.
 * This holds the part that is the same for both: the validation callback that enables
 * collection for every keyword, and the lookup of the annotation of a keyword. They
 * build their schemas differently, so the schema is passed in.
 */
public final class AnnotationCapture {
    private AnnotationCapture() {
    }

    /**
     * Enables annotation collection for every keyword on a configuration builder.
     *
     * @param builder the configuration builder
     */
    public static void collectEverything(ExecutionConfig.Builder builder) {
        builder.annotationCollectionEnabled(true).annotationCollectionFilter(keyword -> true);
    }

    /**
     * Validates an instance and captures the execution context, with annotation
     * collection enabled for every keyword and the given configuration applied on
     * top of that.
     *
     * @param schema       the schema to validate against
     * @param instanceText the instance
     * @param config       the configuration to apply, or null for collection only
     * @return the execution context, which holds the collected annotations
     */
    public static ExecutionContext validate(Schema schema, String instanceText, Consumer<ExecutionConfig.Builder> config) {
        ExecutionContext[] holder = new ExecutionContext[1];
        schema.validate(instanceText, InputFormat.JSON, context -> {
            context.executionConfig(builder -> {
                collectEverything(builder);
                if (config != null) {
                    config.accept(builder);
                }
            });
            holder[0] = context;
        });
        return holder[0];
    }

    /**
     * Gets the annotations collected at an instance location.
     *
     * @param executionContext the execution context
     * @param instanceLocation the instance location
     * @return the annotations, which is empty if there are none at that location
     */
    public static List<Annotation> at(ExecutionContext executionContext, NodePath instanceLocation) {
        return executionContext.getAnnotations().asMap().getOrDefault(instanceLocation, Collections.emptyList());
    }

    /**
     * Gets the annotation collected for a keyword at an instance location.
     *
     * @param executionContext the execution context
     * @param instanceLocation the instance location
     * @param keyword          the keyword
     * @return the annotation
     * @throws AssertionError if no annotation was collected for the keyword
     */
    public static Annotation of(ExecutionContext executionContext, NodePath instanceLocation, String keyword) {
        List<Annotation> annotations = at(executionContext, instanceLocation);
        for (Annotation annotation : annotations) {
            if (keyword.equals(annotation.getKeyword())) {
                return annotation;
            }
        }
        throw new AssertionError("No " + keyword + " annotation was collected: " + annotations);
    }
}
