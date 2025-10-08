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
     * The schema context should only be used for reference as it is shared.
     * 
     * @param executionContext  the execution context
     * @param schemaContext     the schema context for reference
     */
    default void customize(ExecutionContext executionContext, SchemaContext schemaContext) {
    }

    /**
     * Formats the validation results.
     * 
     * @param jsonSchema         the schema
     * @param executionContext   the execution context
     * @param schemaContext      the schema context
     * 
     * @return the result
     */
    T format(Schema jsonSchema, 
            ExecutionContext executionContext, SchemaContext schemaContext);

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
    class Default implements OutputFormat<java.util.List<Error>> {
        @Override
		public void customize(ExecutionContext executionContext, SchemaContext schemaContext) {
			executionContext.executionConfig(executionConfig -> executionConfig.annotationCollectionEnabled(false));
		}

        @Override
        public java.util.List<Error> format(Schema jsonSchema,
                ExecutionContext executionContext, SchemaContext schemaContext) {
            return executionContext.getErrors();
        }
    }

    /**
     * The Flag output format.
     */
    class Flag implements OutputFormat<OutputFlag> {
        @Override
		public void customize(ExecutionContext executionContext, SchemaContext schemaContext) {
			executionContext.executionConfig(
					executionConfig -> executionConfig.annotationCollectionEnabled(false).failFast(true));
		}

        @Override
        public OutputFlag format(Schema jsonSchema, 
                ExecutionContext executionContext, SchemaContext schemaContext) {
            return new OutputFlag(executionContext.getErrors().isEmpty());
        }
    }

    /**
     * The Boolean output format.
     */
    class Boolean implements OutputFormat<java.lang.Boolean> {
        @Override
        public void customize(ExecutionContext executionContext, SchemaContext schemaContext) {
			executionContext.executionConfig(
					executionConfig -> executionConfig.annotationCollectionEnabled(false).failFast(true));
        }

        @Override
        public java.lang.Boolean format(Schema jsonSchema, 
                ExecutionContext executionContext, SchemaContext schemaContext) {
            return executionContext.getErrors().isEmpty();
        }
    }
    
    /**
     * The List output format.
     */
    class List implements OutputFormat<OutputUnit> {
        private final Function<Error, Object> errorMapper;

        public List() {
            this(OutputUnitData::formatError);
        }

        /**
         * Constructor.
         * 
         * @param errorMapper to map the error
         */
        public List(Function<Error, Object> errorMapper) {
            this.errorMapper = errorMapper;
        }

        @Override
        public void customize(ExecutionContext executionContext, SchemaContext schemaContext) {
        }

        @Override
        public OutputUnit format(Schema jsonSchema,
                ExecutionContext executionContext, SchemaContext schemaContext) {
            return ListOutputUnitFormatter.format(executionContext.getErrors(), executionContext, schemaContext,
                    this.errorMapper);
        }
    }

    /**
     * The Hierarchical output format.
     */
    class Hierarchical implements OutputFormat<OutputUnit> {
        private final Function<Error, Object> errorMapper;

        public Hierarchical() {
            this(OutputUnitData::formatError);
        }

        /**
         * Constructor.
         * 
         * @param errorMapper to map the error
         */
        public Hierarchical(Function<Error, Object> errorMapper) {
            this.errorMapper = errorMapper;
        }

        @Override
        public void customize(ExecutionContext executionContext, SchemaContext schemaContext) {
        }

        @Override
        public OutputUnit format(Schema jsonSchema, 
                ExecutionContext executionContext, SchemaContext schemaContext) {
            return HierarchicalOutputUnitFormatter.format(jsonSchema, executionContext.getErrors(), executionContext,
            		schemaContext, this.errorMapper);
        }
    }

    /**
     * The Result output format.
     * <p>
     * This is currently not exposed to consumers.
     */
    class Result implements OutputFormat<com.networknt.schema.Result> {
        @Override
        public void customize(ExecutionContext executionContext, SchemaContext schemaContext) {
        }

        @Override
        public com.networknt.schema.Result format(Schema jsonSchema, ExecutionContext executionContext,
                SchemaContext schemaContext) {
            return new com.networknt.schema.Result(executionContext);
        }
    }
}
