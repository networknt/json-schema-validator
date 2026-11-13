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

        for (String fieldName : fieldNames) {
            JsonNode propertyNode = node.get(fieldName);

            if (propertyNode == null) {
                Boolean readOnly = executionContext.getExecutionConfig().getReadOnly();
                Boolean writeOnly = executionContext.getExecutionConfig().getWriteOnly();
                // Mirror the readOnly and writeOnly execution policies: each asserts for
                // every value except the boolean false, so a property it would assert for
                // must not be demanded here. The two are checked independently so that
                // both are honoured when both policies are on.
                if (Boolean.TRUE.equals(readOnly)
                        && isExemptFromRequired(readOnly, getFieldKeyword(fieldName, "readOnly"))) {
                    continue;
                }
                if (Boolean.TRUE.equals(writeOnly)
                        && isExemptFromRequired(writeOnly, getFieldKeyword(fieldName, "writeOnly"))) {
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

    /**
     * Whether the readOnly or writeOnly keyword of a property exempts it from required.
     * <p>
     * The question is asked of {@link ReadOnlyWriteOnlyPolicy}, so the rule the two
     * validators apply and the rule here cannot drift apart. A keyword that is not
     * declared directly on the property schema does not exempt it, because this only
     * looks at {@code properties/<name>} and does not resolve {@code $ref} or
     * {@code allOf} into the property schema.
     *
     * @param policy      the config value of the policy
     * @param keywordNode the value of the readOnly or writeOnly keyword, may be null
     * @return whether the property is exempt from required
     */
    private static boolean isExemptFromRequired(Boolean policy, JsonNode keywordNode) {
        return keywordNode != null && ReadOnlyWriteOnlyPolicy.asserts(policy, keywordNode);
    }
}
