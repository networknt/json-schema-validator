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

    /**
     * The resolved keyword values, built on the first validation.
     * <p>
     * They are not built in the constructor because {@link #getFieldKeyword} is an
     * overridable hook: an override that resolves a reference needs the parent
     * schema's validators, and those are not installed until the schema has been
     * read.
     */
    private volatile FieldKeywords fieldKeywords;

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
    }

    public void validate(ExecutionContext executionContext, JsonNode node, JsonNode rootNode, NodePath instanceLocation) {
        

        if (!node.isObject()) {
            return;
        }

        FieldKeywords keywords = getFieldKeywords();
        for (String fieldName : fieldNames) {
            JsonNode propertyNode = node.get(fieldName);

            if (propertyNode == null) {
                Boolean readOnly = executionContext.getExecutionConfig().getReadOnly();
                Boolean writeOnly = executionContext.getExecutionConfig().getWriteOnly();
                // Mirror the readOnly and writeOnly execution policies: each asserts for
                // every value except the boolean false, so a property it would assert for
                // must not be demanded here. The two are checked independently so that
                // both are honoured when both policies are on.
                if (ReadOnlyWriteOnlyPolicy.asserts(readOnly, keywords.readOnlyNodes.get(fieldName))
                        || ReadOnlyWriteOnlyPolicy.asserts(writeOnly, keywords.writeOnlyNodes.get(fieldName))) {
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
     * Gets the resolved keyword values, building them on the first call.
     * <p>
     * The double-checked lock keeps concurrent validations from resolving the maps
     * more than once while publishing a fully initialized holder.
     *
     * @return the resolved keyword values
     */
    private FieldKeywords getFieldKeywords() {
        FieldKeywords keywords = this.fieldKeywords;
        if (keywords == null) {
            synchronized (this) {
                keywords = this.fieldKeywords;
                if (keywords == null) {
                    keywords = new FieldKeywords(resolveFieldKeywords("readOnly"),
                            resolveFieldKeywords("writeOnly"));
                    this.fieldKeywords = keywords;
                }
            }
        }
        return keywords;
    }

    /**
     * Gets the keyword value of a required property from its property schema.
     * <p>
     * This is called for each required property and keyword on the first validation,
     * whether or not a policy is enabled, and the result is cached so that later
     * validations only do map lookups. An override may resolve a reference because
     * the parent schema's validators are installed by then.
     *
     * @param fieldName the property name
     * @param keyword   the readOnly or writeOnly keyword
     * @return the keyword value, or null when the property schema does not declare it
     * @deprecated this hook is called once on the first validation and its result is
     *             cached; it will be replaced by a lookup that cannot be overridden
     */
    @Deprecated(since = "3.0.10", forRemoval = true)
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

    private static final class FieldKeywords {
        private final Map<String, JsonNode> readOnlyNodes;
        private final Map<String, JsonNode> writeOnlyNodes;

        private FieldKeywords(Map<String, JsonNode> readOnlyNodes, Map<String, JsonNode> writeOnlyNodes) {
            this.readOnlyNodes = readOnlyNodes;
            this.writeOnlyNodes = writeOnlyNodes;
        }
    }
}
