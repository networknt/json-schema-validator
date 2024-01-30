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

import java.util.Set;

import com.networknt.schema.output.HierarchicalOutputUnitFormatter;
import com.networknt.schema.output.ListOutputUnitFormatter;
import com.networknt.schema.output.OutputFlag;
import com.networknt.schema.output.OutputUnit;

/**
 * Formats the validation results.
 * 
 * @param <T> the result type
 */
public interface OutputFormat<T> {
    /**
     * Customize the execution context before validation.
     * <p>
     * The validation context should only be used for reference as it is shared.
     * 
     * @param executionContext  the execution context
     * @param validationContext the validation context for reference
     */
    default void customize(ExecutionContext executionContext, ValidationContext validationContext) {
    }

    /**
     * Formats the validation results.
     * 
     * @param jsonSchema         the schema
     * @param validationMessages the validation messages
     * @param executionContext   the execution context
     * @param validationContext  the validation context
     * 
     * @return the result
     */
    T format(JsonSchema jsonSchema, Set<ValidationMessage> validationMessages,
            ExecutionContext executionContext, ValidationContext validationContext);

    /**
     * The Default output format.
     */
    public static final Default DEFAULT = new Default();

    /**
     * The Boolean output format.
     */
    public static final Flag BOOLEAN = new Flag();

    /**
     * The Flag output format.
     */
    public static final Flag FLAG = new Flag();
    
    /**
     * The List output format.
     */
    public static final List LIST = new List();

    
    /**
     * The Hierarchical output format.
     */
    public static final Hierarchical HIERARCHICAL = new Hierarchical();

    /**
     * The Default output format.
     */
    public static class Default implements OutputFormat<Set<ValidationMessage>> {
        @Override
        public void customize(ExecutionContext executionContext, ValidationContext validationContext) {
            executionContext.getExecutionConfig().setAnnotationCollectionEnabled(false);
        }

        @Override
        public Set<ValidationMessage> format(JsonSchema jsonSchema,
                Set<ValidationMessage> validationMessages, ExecutionContext executionContext, ValidationContext validationContext) {
            return validationMessages;
        }
    }

    /**
     * The Flag output format.
     */
    public static class Flag implements OutputFormat<OutputFlag> {
        @Override
        public void customize(ExecutionContext executionContext, ValidationContext validationContext) {
            executionContext.getExecutionConfig().setAnnotationCollectionEnabled(false);
            executionContext.getExecutionConfig().setFailFast(true);
        }

        @Override
        public OutputFlag format(JsonSchema jsonSchema, Set<ValidationMessage> validationMessages,
                ExecutionContext executionContext, ValidationContext validationContext) {
            return new OutputFlag(validationMessages.isEmpty());
        }
    }

    /**
     * The Boolean output format.
     */
    public static class Boolean implements OutputFormat<java.lang.Boolean> {
        @Override
        public void customize(ExecutionContext executionContext, ValidationContext validationContext) {
            executionContext.getExecutionConfig().setAnnotationCollectionEnabled(false);
            executionContext.getExecutionConfig().setFailFast(true);
        }

        @Override
        public java.lang.Boolean format(JsonSchema jsonSchema, Set<ValidationMessage> validationMessages,
                ExecutionContext executionContext, ValidationContext validationContext) {
            return validationMessages.isEmpty();
        }
    }
    
    /**
     * The List output format.
     */
    public static class List implements OutputFormat<OutputUnit> {
        @Override
        public void customize(ExecutionContext executionContext, ValidationContext validationContext) {
        }

        @Override
        public OutputUnit format(JsonSchema jsonSchema, Set<ValidationMessage> validationMessages,
                ExecutionContext executionContext, ValidationContext validationContext) {
            return ListOutputUnitFormatter.format(validationMessages, executionContext, validationContext);
        }
    }

    /**
     * The Hierarchical output format.
     */
    public static class Hierarchical implements OutputFormat<OutputUnit> {
        @Override
        public void customize(ExecutionContext executionContext, ValidationContext validationContext) {
        }

        @Override
        public OutputUnit format(JsonSchema jsonSchema, Set<ValidationMessage> validationMessages,
                ExecutionContext executionContext, ValidationContext validationContext) {
            return HierarchicalOutputUnitFormatter.format(validationMessages, executionContext, validationContext);
        }
    }
}
