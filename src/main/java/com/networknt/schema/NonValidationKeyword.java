package com.networknt.schema;

import java.util.Collections;
import java.util.Set;

import com.fasterxml.jackson.databind.JsonNode;

/**
 * Used for Keywords that have no validation aspect, but are part of the metaschema.
 */
public class NonValidationKeyword extends AbstractKeyword {

    private static final class Validator extends AbstractJsonValidator {
        private Validator(String keyword) {
            super(keyword);
        }

        @Override
        public Set<ValidationMessage> validate(JsonNode node, JsonNode rootNode, String at) {
            return Collections.emptySet();
        }
    }

    public NonValidationKeyword(String keyword) {
        super(keyword);
    }
    
    @Override
    public JsonValidator newValidator(String schemaPath, JsonNode schemaNode, JsonSchema parentSchema,
            ValidationContext validationContext) throws JsonSchemaException, Exception {
        return new Validator(getValue());
    }
}
