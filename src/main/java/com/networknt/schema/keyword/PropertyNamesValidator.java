/*
 * Copyright (c) 2020 Network New Technologies Inc.
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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.TextNode;
import com.networknt.schema.Error;
import com.networknt.schema.ExecutionContext;
import com.networknt.schema.JsonNodePath;
import com.networknt.schema.Schema;
import com.networknt.schema.SchemaLocation;
import com.networknt.schema.SchemaContext;

public class PropertyNamesValidator extends BaseKeywordValidator implements KeywordValidator {
    private final Schema innerSchema;
    public PropertyNamesValidator(SchemaLocation schemaLocation, JsonNodePath evaluationPath, JsonNode schemaNode, Schema parentSchema, SchemaContext schemaContext) {
        super(ValidatorTypeCode.PROPERTYNAMES, schemaNode, schemaLocation, parentSchema, schemaContext, evaluationPath);
        innerSchema = schemaContext.newSchema(schemaLocation, evaluationPath, schemaNode, parentSchema);
    }

    public void validate(ExecutionContext executionContext, JsonNode node, JsonNode rootNode, JsonNodePath instanceLocation) {
        

        List<Error> existingErrors = executionContext.getErrors();
        List<Error> schemaErrors = new ArrayList<>();
        executionContext.setErrors(schemaErrors);
        for (Iterator<String> it = node.fieldNames(); it.hasNext(); ) {
            final String pname = it.next();
            final TextNode pnameText = TextNode.valueOf(pname);
            innerSchema.validate(executionContext, pnameText, node, instanceLocation.append(pname));
            for (final Error schemaError : schemaErrors) {
                final String path = schemaError.getInstanceLocation().toString();
                String msg = schemaError.getMessage();
                if (msg.startsWith(path)) {
                    msg = msg.substring(path.length()).replaceFirst("^:\\s*", "");
                }
                existingErrors.add(
                        error().property(pname).instanceNode(node).instanceLocation(instanceLocation)
                                .locale(executionContext.getExecutionConfig().getLocale())
                                .arguments(pname, msg).build());
            }
            schemaErrors.clear();
        }
        executionContext.setErrors(existingErrors);
    }


    @Override
    public void preloadJsonSchema() {
        innerSchema.initializeValidators();
    }
}
