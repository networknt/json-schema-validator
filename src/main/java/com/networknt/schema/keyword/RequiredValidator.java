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

import tools.jackson.databind.JsonNode;
import com.networknt.schema.ExecutionContext;
import com.networknt.schema.Schema;
import com.networknt.schema.SchemaLocation;
import com.networknt.schema.path.NodePath;
import com.networknt.schema.SchemaContext;

import java.util.*;

/**
 * {@link KeywordValidator} for required.
 */
public class RequiredValidator extends BaseKeywordValidator implements KeywordValidator {
    private final List<String> fieldNames;
    private final Map<String, JsonNode> readOnlyNodes;
    private final Map<String, JsonNode> writeOnlyNodes;

    public RequiredValidator(SchemaLocation schemaLocation, JsonNode schemaNode, Schema parentSchema, SchemaContext schemaContext) {
        super(KeywordType.REQUIRED, schemaNode, schemaLocation, parentSchema, schemaContext);
        if (schemaNode.isArray()) {
            this.fieldNames = new ArrayList<>(schemaNode.size());
            for (JsonNode fieldNme : schemaNode) {
                fieldNames.add(fieldNme.asString());
            }
        } else {
            this.fieldNames = Collections.emptyList();
        }
        // The keyword values are immutable, so resolve them once. Only a declaration
        // directly on the property schema is seen; $ref and allOf are not resolved.
        this.readOnlyNodes = resolveFieldKeywords("readOnly");
        this.writeOnlyNodes = resolveFieldKeywords("writeOnly");
    }

    public void validate(ExecutionContext executionContext, JsonNode node, JsonNode rootNode, NodePath instanceLocation) {
        

        if (!node.isObject()) {
            return;
        }

        for (String fieldName : fieldNames) {
            JsonNode propertyNode = node.get(fieldName);

            if (propertyNode == null) {
                Boolean readOnly = executionContext.getExecutionConfig().getReadOnly();
                Boolean writeOnly = executionContext.getExecutionConfig().getWriteOnly();
                // Mirror the readOnly and writeOnly execution policies: each asserts for
                // every value except the boolean false, so a property it would assert for
                // must not be demanded here. The two are checked independently so that
                // both are honoured when both policies are on.
                if (ReadOnlyWriteOnlyPolicy.asserts(readOnly, this.readOnlyNodes.get(fieldName))
                        || ReadOnlyWriteOnlyPolicy.asserts(writeOnly, this.writeOnlyNodes.get(fieldName))) {
                    continue;
                }
                /**
                 * Note that for the required validation the instanceLocation does not contain the missing property
                 * <p>
                 * @see <a href="https://json-schema.org/draft/2020-12/draft-bhutton-json-schema-01#name-basic">Basic</a>
                 */
                executionContext.addError(error().instanceNode(node).property(fieldName).instanceLocation(instanceLocation)
                        .evaluationPath(executionContext.getEvaluationPath()).locale(executionContext.getExecutionConfig().getLocale())
                        .arguments(fieldName).build());
            }
        }
    }

    /**
     * Resolves the keyword value of each required property from its property schema.
     * <p>
     * Only a declaration directly on the property schema is seen; {@code $ref} and
     * {@code allOf} are not resolved into the property schema. A property that does not
     * declare the keyword does not get an entry, so the map is empty when no required
     * property declares it.
     *
     * @param keyword the readOnly or writeOnly keyword
     * @return the keyword value by property name
     */
    private Map<String, JsonNode> resolveFieldKeywords(String keyword) {
        Map<String, JsonNode> keywordNodes = new HashMap<>();
        for (String fieldName : this.fieldNames) {
            JsonNode keywordNode = getFieldKeyword(fieldName, keyword);
            if (keywordNode != null) {
                keywordNodes.put(fieldName, keywordNode);
            }
        }
        return keywordNodes.isEmpty() ? Collections.emptyMap() : keywordNodes;
    }

    /**
     * Gets the keyword value of a required property from its property schema.
     * <p>
     * This is called once for each required property when the validator is constructed,
     * so that validation only does map lookups.
     *
     * @param fieldName the property name
     * @param keyword   the readOnly or writeOnly keyword
     * @return the keyword value, or null when the property schema does not declare it
     * @deprecated this hook is called from the constructor, before a subclass is
     *             initialized; do not override it to resolve a keyword that is reached
     *             through {@code $ref} or {@code allOf}
     */
    @Deprecated
    protected JsonNode getFieldKeyword(String fieldName, String keyword) {
        JsonNode propertiesNode = this.parentSchema.getSchemaNode().get("properties");
        if (propertiesNode != null) {
            JsonNode fieldNode = propertiesNode.get(fieldName);
            if (fieldNode != null) {
                return fieldNode.get(keyword);
            }
        }
        return null;
    }
}
