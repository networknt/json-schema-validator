package com.networknt.schema;

import com.fasterxml.jackson.databind.JsonNode;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Tests the validity of {@literal maxContains} and {@literal minContains} in a schema.
 * <p>
 * This validator only checks that the schema is valid. The functionality for
 * testing whether an instance array conforms to the {@literal maxContains}
 * and {@literal minContains} constraints exists within {@code ContainsValidator}.
 */
public class MinMaxContainsValidator extends BaseJsonValidator {
    private final Set<Analysis> analysis;

    public MinMaxContainsValidator(String schemaPath, JsonNode schemaNode, JsonSchema parentSchema,
            ValidationContext validationContext) {
        super(schemaPath, schemaNode, parentSchema, ValidatorTypeCode.MAX_CONTAINS, validationContext);

        Set<Analysis> analysis = null;
        int min = 1;
        int max = Integer.MAX_VALUE;

        JsonNode minNode = parentSchema.getSchemaNode().get("minContains");
        if (null != minNode) {
            if (!minNode.isNumber() || !minNode.canConvertToExactIntegral() || minNode.intValue() < 0) {
                if (analysis == null) {
                    analysis = new LinkedHashSet<>();
                }
                analysis.add(new Analysis("minContains", schemaPath));
            } else {
                min = minNode.intValue();
            }
        }

        JsonNode maxNode = parentSchema.getSchemaNode().get("maxContains");
        if (null != maxNode) {
            if (!maxNode.isNumber() || !maxNode.canConvertToExactIntegral() || maxNode.intValue() < 0) {
                if (analysis == null) {
                    analysis = new LinkedHashSet<>();
                }
                analysis.add(new Analysis("maxContains", schemaPath));
            } else {
                max = maxNode.intValue();
            }
        }

        if (max < min) {
            if (analysis == null) {
                analysis = new LinkedHashSet<>();
            }
            analysis.add(new Analysis("minContainsVsMaxContains", schemaPath));
        }
        this.analysis = analysis;
    }

    @Override
    public Set<ValidationMessage> validate(ExecutionContext executionContext, JsonNode node, JsonNode rootNode,
            String at) {
        return this.analysis != null ? this.analysis.stream()
                .map(analysis -> buildValidationMessage(analysis.getAt(), analysis.getMessageKey(),
                        executionContext.getExecutionConfig().getLocale(), parentSchema.getSchemaNode().toString()))
                .collect(Collectors.toCollection(LinkedHashSet::new)) : Collections.emptySet();
    }
    
    public static class Analysis {
        public String getMessageKey() {
            return messageKey;
        }

        public String getAt() {
            return at;
        }

        private final String messageKey;
        private final String at;

        public Analysis(String messageKey, String at) {
            super();
            this.messageKey = messageKey;
            this.at = at;
        }
    }
}
