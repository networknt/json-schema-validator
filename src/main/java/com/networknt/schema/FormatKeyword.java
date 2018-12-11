package com.networknt.schema;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;

import com.fasterxml.jackson.databind.JsonNode;

public class FormatKeyword implements Keyword {
    private final ValidatorTypeCode type;
    private final Map<String, Format> formats;

    public FormatKeyword(ValidatorTypeCode type, Map<String, Format> formats) {
        this.type = type;
        this.formats = formats;
    }

    Collection<Format> getFormats() {
        return Collections.unmodifiableCollection(formats.values());
    }
    
    @Override
    public JsonValidator newValidator(String schemaPath, JsonNode schemaNode, JsonSchema parentSchema, ValidationContext validationContext)
            throws Exception {
        Format format = null;
        if (schemaNode != null && schemaNode.isTextual()) {
            String formatName = schemaNode.textValue();
            format = formats.get(formatName);
        }

        return new FormatValidator(schemaPath, schemaNode, parentSchema, validationContext, format);
    }
    
    @Override
    public String getValue() {
        return type.getValue();
    }
    
    
}
