/*
 * Copyright (c) 2024 the original author or authors.
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

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.regex.Pattern;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.networknt.schema.ExecutionContext;
import com.networknt.schema.JsonNodePath;
import com.networknt.schema.Schema;
import com.networknt.schema.JsonType;
import com.networknt.schema.SchemaLocation;
import com.networknt.schema.TypeFactory;
import com.networknt.schema.ValidationContext;
import com.networknt.schema.serialization.JsonMapperFactory;

/**
 * {@link KeywordValidator} for contentMediaType.
 * <p>
 * Note that since 2019-09 this keyword only generates annotations and not errors.
 */
public class ContentMediaTypeValidator extends BaseKeywordValidator {
    private static final String PATTERN_STRING = "(application|audio|font|example|image|message|model|multipart|text|video|x-(?:[0-9A-Za-z!#$%&'*+.^_`|~-]+))/([0-9A-Za-z!#$%&'*+.^_`|~-]+)((?:[ \t]*;[ \t]*[0-9A-Za-z!#$%&'*+.^_`|~-]+=(?:[0-9A-Za-z!#$%&'*+.^_`|~-]+|\"(?:[^\"\\\\]|\\.)*\"))*)";
    private static final Pattern PATTERN = Pattern.compile(PATTERN_STRING);
    private final String contentMediaType;

    /**
     * Constructor.
     * 
     * @param schemaLocation    the schema location
     * @param evaluationPath    the evaluation path
     * @param schemaNode        the schema node
     * @param parentSchema      the parent schema
     * @param validationContext the validation context
     */
    public ContentMediaTypeValidator(SchemaLocation schemaLocation, JsonNodePath evaluationPath, JsonNode schemaNode,
            Schema parentSchema, ValidationContext validationContext) {
        super(ValidatorTypeCode.CONTENT_MEDIA_TYPE, schemaNode, schemaLocation, parentSchema, validationContext, evaluationPath);
        this.contentMediaType = schemaNode.textValue();
    }

    private boolean matches(String value) {
        if ("application/json".equals(this.contentMediaType)) {
            // Validate content
            JsonNode node = this.parentSchema.getSchemaNode().get("contentEncoding");
            String encoding = null;
            if (node != null && node.isTextual()) {
                encoding = node.asText();
            }
            String data = value;
            if ("base64".equals(encoding)) {
                try {
                    data = new String(Base64.getDecoder().decode(value), StandardCharsets.UTF_8);
                } catch(IllegalArgumentException e) {
                    return true; // The contentEncoding keyword will report the failure
                }
            }
            // Validate the json
            try {
                JsonMapperFactory.getInstance().readTree(data);
            } catch (JsonProcessingException e) {
                return false;
            }
            return true;
        }
        else if (!PATTERN.matcher(this.contentMediaType).matches()) {
            return false;
        } else {
            // validate data
        }
        return true;
    }

    @Override
    public void validate(ExecutionContext executionContext, JsonNode node, JsonNode rootNode,
            JsonNodePath instanceLocation) {
        // Ignore non-strings
        JsonType nodeType = TypeFactory.getValueNodeType(node, this.validationContext.getSchemaRegistryConfig());
        if (nodeType != JsonType.STRING) {
            return;
        }

        if (collectAnnotations(executionContext)) {
            putAnnotation(executionContext,
                    annotation -> annotation.instanceLocation(instanceLocation).value(this.contentMediaType));
        }

        if (!matches(node.asText())) {
            executionContext.addError(error().instanceNode(node).instanceLocation(instanceLocation)
                    .locale(executionContext.getExecutionConfig().getLocale())
                    .arguments(this.contentMediaType)
                    .build());
        }
    }
}
