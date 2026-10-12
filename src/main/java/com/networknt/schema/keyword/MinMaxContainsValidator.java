package com.networknt.schema.keyword;

import tools.jackson.databind.JsonNode;
import com.networknt.schema.ExecutionContext;
import com.networknt.schema.Schema;
import com.networknt.schema.SchemaLocation;
import com.networknt.schema.path.NodePath;
import com.networknt.schema.SchemaContext;

/**
 * {@link KeywordValidator} for {@literal maxContains} and {@literal minContains} in a schema.
 * <p>
 * This validator only checks that the schema is valid. The functionality for
 * testing whether an instance array conforms to the {@literal maxContains}
 * and {@literal minContains} constraints exists within {@code ContainsValidator}.
 */
public class MinMaxContainsValidator extends BaseKeywordValidator {
    /**
     * The parent schema, used as the message argument, or null if the value is valid.
     */
    private final String malformedParentSchema;

    public MinMaxContainsValidator(Keyword keyword, SchemaLocation schemaLocation, JsonNode schemaNode,
            Schema parentSchema, SchemaContext schemaContext) {
        super(keyword, schemaNode, schemaLocation, parentSchema, schemaContext);

        // minContains and maxContains each create their own validator, so each one only
        // checks its own value. The specification does not require minContains <=
        // maxContains. Contradictory bounds only mean that no array can satisfy contains,
        // which ContainsValidator already reports, so they are not a schema error.
        if (!schemaNode.isNumber() || !schemaNode.canConvertToExactIntegral() || schemaNode.intValue() < 0) {
            this.malformedParentSchema = parentSchema.getSchemaNode().toString();
        } else {
            this.malformedParentSchema = null;
        }
    }

    static MinMaxContainsValidator minContains(SchemaLocation schemaLocation, JsonNode schemaNode,
            Schema parentSchema, SchemaContext schemaContext) {
        return new MinMaxContainsValidator(KeywordType.MIN_CONTAINS, schemaLocation, schemaNode, parentSchema,
                schemaContext);
    }

    static MinMaxContainsValidator maxContains(SchemaLocation schemaLocation, JsonNode schemaNode,
            Schema parentSchema, SchemaContext schemaContext) {
        return new MinMaxContainsValidator(KeywordType.MAX_CONTAINS, schemaLocation, schemaNode, parentSchema,
                schemaContext);
    }

    @Override
    public void validate(ExecutionContext executionContext, JsonNode node, JsonNode rootNode,
            NodePath instanceLocation) {
        if (this.malformedParentSchema != null) {
            executionContext.addError(error().instanceNode(node)
                    .instanceLocation(instanceLocation)
                    .evaluationPath(executionContext.getEvaluationPath())
                    .messageKey(getKeyword()).locale(executionContext.getExecutionConfig().getLocale())
                    .keyword(getKeyword())
                    .arguments(this.malformedParentSchema).build());
        }
    }
}
