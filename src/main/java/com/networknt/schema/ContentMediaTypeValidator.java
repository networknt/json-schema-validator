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

package com.networknt.schema;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Collections;
import java.util.Set;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.networknt.schema.serialization.JsonMapperFactory;

/**
 * {@link JsonValidator} for contentMediaType.
 * <p>
 * Note that since 2019-09 this keyword only generates annotations and not assertions.
 */
public class ContentMediaTypeValidator extends BaseJsonValidator {
    private static final Logger logger = LoggerFactory.getLogger(ContentMediaTypeValidator.class);
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
            JsonSchema parentSchema, ValidationContext validationContext) {
        super(schemaLocation, evaluationPath, schemaNode, parentSchema, ValidatorTypeCode.CONTENT_MEDIA_TYPE, validationContext);
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
    public Set<ValidationMessage> validate(ExecutionContext executionContext, JsonNode node, JsonNode rootNode,
            JsonNodePath instanceLocation) {
        debug(logger, executionContext, node, rootNode, instanceLocation);

        // Ignore non-strings
        JsonType nodeType = TypeFactory.getValueNodeType(node, this.validationContext.getConfig());
        if (nodeType != JsonType.STRING) {
            return Collections.emptySet();
        }

        if (collectAnnotations(executionContext)) {
            putAnnotation(executionContext,
                    annotation -> annotation.instanceLocation(instanceLocation).value(this.contentMediaType));
        }

        if (!matches(node.asText())) {
            return Collections.singleton(message().instanceNode(node).instanceLocation(instanceLocation)
                    .locale(executionContext.getExecutionConfig().getLocale())
                    .failFast(executionContext.isFailFast()).arguments(this.contentMediaType)
                    .build());
        }
        return Collections.emptySet();
    }
}
