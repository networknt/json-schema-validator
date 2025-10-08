package com.networknt.schema.format;

import java.util.Map;

import com.fasterxml.jackson.databind.JsonNode;
import com.networknt.schema.ExecutionContext;
import com.networknt.schema.Schema;
import com.networknt.schema.SchemaLocation;
import com.networknt.schema.SchemaContext;
import com.networknt.schema.SpecificationVersion;
import com.networknt.schema.keyword.BaseKeywordValidator;
import com.networknt.schema.keyword.Keyword;
import com.networknt.schema.path.NodePath;

public abstract class BaseFormatValidator extends BaseKeywordValidator {
    protected final boolean assertionsEnabled;
    
    public BaseFormatValidator(SchemaLocation schemaLocation, NodePath evaluationPath, JsonNode schemaNode,
            Schema parentSchema, Keyword keyword,
            SchemaContext schemaContext) {
        super(keyword, schemaNode, schemaLocation, parentSchema, schemaContext, evaluationPath);
        SpecificationVersion dialect = this.schemaContext.getDialect().getSpecificationVersion();
        if (dialect == null || dialect.getOrder() < SpecificationVersion.DRAFT_2019_09.getOrder()) {
            assertionsEnabled = true;
        } else {
            // Check vocabulary
            assertionsEnabled = isFormatAssertionVocabularyEnabled(dialect,
                    this.schemaContext.getDialect().getVocabularies());
        }
    }

    protected boolean isFormatAssertionVocabularyEnabled() {
        return isFormatAssertionVocabularyEnabled(this.schemaContext.getDialect().getSpecificationVersion(),
                this.schemaContext.getDialect().getVocabularies());
    }

    protected boolean isFormatAssertionVocabularyEnabled(SpecificationVersion specification, Map<String, Boolean> vocabularies) {
        if (SpecificationVersion.DRAFT_2020_12.equals(specification)) {
            String vocabulary = "https://json-schema.org/draft/2020-12/vocab/format-assertion";
            return vocabularies.containsKey(vocabulary); // doesn't matter if it is true or false
        } else if (SpecificationVersion.DRAFT_2019_09.equals(specification)) {
            String vocabulary = "https://json-schema.org/draft/2019-09/vocab/format";
            return vocabularies.getOrDefault(vocabulary, false);
        }
        return false;
    }

    protected boolean isAssertionsEnabled(ExecutionContext executionContext) {
        if (Boolean.TRUE.equals(executionContext.getExecutionConfig().getFormatAssertionsEnabled())) {
            return true;
        } else if (Boolean.FALSE.equals(executionContext.getExecutionConfig().getFormatAssertionsEnabled())) {
            return false;
        }
        return this.assertionsEnabled;
    }
}
