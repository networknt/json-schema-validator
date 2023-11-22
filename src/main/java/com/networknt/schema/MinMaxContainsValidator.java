package com.networknt.schema;

import com.fasterxml.jackson.databind.JsonNode;

import java.util.LinkedHashSet;
import java.util.Set;

/**
 * Tests the validity of {@literal maxContains} and {@literal minContains} in a schema.
 * <p>
 * This validator only checks that the schema is valid. The functionality for
 * testing whether an instance array conforms to the {@literal maxContains}
 * and {@literal minContains} constraints exists within {@code ContainsValidator}.
 */
public class MinMaxContainsValidator extends BaseJsonValidator {
    private final Set<ValidationMessage> analysis = new LinkedHashSet<>();

    public MinMaxContainsValidator(String schemaPath, JsonNode schemaNode, JsonSchema parentSchema, ValidationContext validationContext) {
        super(schemaPath, schemaNode, parentSchema, ValidatorTypeCode.MAX_CONTAINS, validationContext);

        int min = 1;
        int max = Integer.MAX_VALUE;

        JsonNode minNode = parentSchema.getSchemaNode().get("minContains");
        if (null != minNode) {
            if (!minNode.isNumber() || !minNode.canConvertToExactIntegral() || minNode.intValue() < 0) {
                report("minContains", schemaPath);
            } else {
                min = minNode.intValue();
            }
        }

        JsonNode maxNode = parentSchema.getSchemaNode().get("maxContains");
        if (null != maxNode) {
            if (!maxNode.isNumber() || !maxNode.canConvertToExactIntegral() || maxNode.intValue() < 0) {
                report("maxContains", schemaPath);
            } else {
                max = maxNode.intValue();
            }
        }

        if (max < min) {
            report("minContainsVsMaxContains", schemaPath);
        }
    }

    private void report(String messageKey, String at) {
        this.analysis.add(constructValidationMessage(messageKey, at, parentSchema.getSchemaNode().toString()));
    }

    @Override
    public Set<ValidationMessage> validate(ExecutionContext executionContext, JsonNode node, JsonNode rootNode, String at) {
        return this.analysis;
    }

}
