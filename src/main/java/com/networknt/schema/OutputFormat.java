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
import java.util.function.Function;

import com.networknt.schema.output.HierarchicalOutputUnitFormatter;
import com.networknt.schema.output.ListOutputUnitFormatter;
import com.networknt.schema.output.OutputFlag;
import com.networknt.schema.output.OutputUnit;
import com.networknt.schema.output.OutputUnitData;

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
    Default DEFAULT = new Default();

    /**
     * The Boolean output format.
     */
    Boolean BOOLEAN = new Boolean();

    /**
     * The Flag output format.
     */
    Flag FLAG = new Flag();
    
    /**
     * The List output format.
     */
    List LIST = new List();

    
    /**
     * The Hierarchical output format.
     */
    Hierarchical HIERARCHICAL = new Hierarchical();

    /**
     * The Result output format.
     * <p>
     * This is currently not exposed to consumers.
     */
    Result RESULT = new Result();

    /**
     * The Default output format.
     */
    class Default implements OutputFormat<Set<ValidationMessage>> {
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
    class Flag implements OutputFormat<OutputFlag> {
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
    class Boolean implements OutputFormat<java.lang.Boolean> {
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
    class List implements OutputFormat<OutputUnit> {
        private final Function<ValidationMessage, Object> assertionMapper;

        public List() {
            this(OutputUnitData::formatAssertion);
        }

        /**
         * Constructor.
         * 
         * @param assertionMapper to map the assertion
         */
        public List(Function<ValidationMessage, Object> assertionMapper) {
            this.assertionMapper = assertionMapper;
        }

        @Override
        public void customize(ExecutionContext executionContext, ValidationContext validationContext) {
        }

        @Override
        public OutputUnit format(JsonSchema jsonSchema, Set<ValidationMessage> validationMessages,
                ExecutionContext executionContext, ValidationContext validationContext) {
            return ListOutputUnitFormatter.format(validationMessages, executionContext, validationContext,
                    this.assertionMapper);
        }
    }

    /**
     * The Hierarchical output format.
     */
    class Hierarchical implements OutputFormat<OutputUnit> {
        private final Function<ValidationMessage, Object> assertionMapper;

        public Hierarchical() {
            this(OutputUnitData::formatAssertion);
        }

        /**
         * Constructor.
         * 
         * @param assertionMapper to map the assertion
         */
        public Hierarchical(Function<ValidationMessage, Object> assertionMapper) {
            this.assertionMapper = assertionMapper;
        }

        @Override
        public void customize(ExecutionContext executionContext, ValidationContext validationContext) {
        }

        @Override
        public OutputUnit format(JsonSchema jsonSchema, Set<ValidationMessage> validationMessages,
                ExecutionContext executionContext, ValidationContext validationContext) {
            return HierarchicalOutputUnitFormatter.format(jsonSchema, validationMessages, executionContext,
                    validationContext, this.assertionMapper);
        }
    }

    /**
     * The Result output format.
     * <p>
     * This is currently not exposed to consumers.
     */
    class Result implements OutputFormat<ValidationResult> {
        @Override
        public void customize(ExecutionContext executionContext, ValidationContext validationContext) {
        }

        @Override
        public ValidationResult format(JsonSchema jsonSchema,
                Set<ValidationMessage> validationMessages, ExecutionContext executionContext, ValidationContext validationContext) {
            return new ValidationResult(validationMessages, executionContext);
        }
    }
}
