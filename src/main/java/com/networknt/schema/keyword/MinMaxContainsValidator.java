package com.networknt.schema.keyword;

import tools.jackson.databind.JsonNode;
import com.networknt.schema.ExecutionContext;
import com.networknt.schema.Schema;
import com.networknt.schema.SchemaLocation;
import com.networknt.schema.path.NodePath;
import com.networknt.schema.SchemaContext;

import java.util.Collections;
import java.util.Set;

/**
 * {@link KeywordValidator} for {@literal maxContains} and {@literal minContains} in a schema.
 * <p>
 * This validator only checks that the schema is valid. The functionality for
 * testing whether an instance array conforms to the {@literal maxContains}
 * and {@literal minContains} constraints exists within {@code ContainsValidator}.
 */
public class MinMaxContainsValidator extends BaseKeywordValidator {
    private final Set<Analysis> analysis;

    public MinMaxContainsValidator(SchemaLocation schemaLocation, JsonNode schemaNode, Schema parentSchema,
            SchemaContext schemaContext) {
        super(keywordOf(schemaLocation), schemaNode, schemaLocation, parentSchema, schemaContext);

        // minContains and maxContains each create their own validator, so each one only
        // checks its own value. The specification does not require minContains <=
        // maxContains. Contradictory bounds only mean that no array can satisfy contains,
        // which ContainsValidator already reports, so they are not a schema error.
        if (!schemaNode.isNumber() || !schemaNode.canConvertToExactIntegral() || schemaNode.intValue() < 0) {
            this.analysis = Collections.singleton(new Analysis(getKeyword(), schemaLocation));
        } else {
            this.analysis = null;
        }
    }

    private static KeywordType keywordOf(SchemaLocation schemaLocation) {
        return KeywordType.MIN_CONTAINS.getValue().equals(schemaLocation.getFragment().getName(-1))
                ? KeywordType.MIN_CONTAINS
                : KeywordType.MAX_CONTAINS;
    }

    @Override
    public void validate(ExecutionContext executionContext, JsonNode node, JsonNode rootNode,
            NodePath instanceLocation) {
        if (this.analysis != null) {
            this.analysis.stream()
            .map(analysis -> error().instanceNode(node)
                    .instanceLocation(instanceLocation)
                    .evaluationPath(executionContext.getEvaluationPath())
                    .messageKey(analysis.getMessageKey()).locale(executionContext.getExecutionConfig().getLocale())
                    .keyword(analysis.getMessageKey())
                    .arguments(parentSchema.getSchemaNode().toString()).build())
            .forEach(executionContext::addError);
        }
    }
    
    public static class Analysis {
        public String getMessageKey() {
            return messageKey;
        }

        public SchemaLocation getSchemaLocation() {
            return schemaLocation;
        }

        private final String messageKey;
        private final SchemaLocation schemaLocation;

        public Analysis(String messageKey, SchemaLocation schemaLocation) {
            super();
            this.messageKey = messageKey;
            this.schemaLocation = schemaLocation;
        }
    }
}
