/*
 * Copyright (c) 2016 Network New Technologies Inc.
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

package com.networknt.schema.keyword;

import com.fasterxml.jackson.databind.JsonNode;
import com.networknt.schema.NodePath;
import com.networknt.schema.Schema;
import com.networknt.schema.SchemaException;
import com.networknt.schema.SchemaLocation;
import com.networknt.schema.SchemaContext;

/**
 * Represents a keyword.
 */
public interface Keyword {
    /**
     * Gets the keyword value.
     * 
     * @return the keyword value
     */
    String getValue();

    /**
     * Creates a new validator for the keyword.
     * 
     * @param schemaLocation the schema location
     * @param evaluationPath the evaluation path
     * @param schemaNode the schema node
     * @param parentSchema the parent schema
     * @param schemaContext the schema context
     * @return the validation
     * @throws SchemaException the exception
     * @throws Exception the exception
     */
    KeywordValidator newValidator(SchemaLocation schemaLocation, NodePath evaluationPath, JsonNode schemaNode,
            Schema parentSchema, SchemaContext schemaContext) throws SchemaException, Exception;
}
