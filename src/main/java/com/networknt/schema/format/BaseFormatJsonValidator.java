package com.networknt.schema.format;

import java.util.Map;

import com.fasterxml.jackson.databind.JsonNode;
import com.networknt.schema.ExecutionContext;
import com.networknt.schema.JsonNodePath;
import com.networknt.schema.Schema;
import com.networknt.schema.SchemaLocation;
import com.networknt.schema.ValidationContext;
import com.networknt.schema.Specification.Version;
import com.networknt.schema.keyword.BaseKeywordValidator;
import com.networknt.schema.keyword.Keyword;

public abstract class BaseFormatJsonValidator extends BaseKeywordValidator {
    protected final boolean assertionsEnabled;
    
    public BaseFormatJsonValidator(SchemaLocation schemaLocation, JsonNodePath evaluationPath, JsonNode schemaNode,
            Schema parentSchema, Keyword keyword,
            ValidationContext validationContext) {
        super(keyword, schemaNode, schemaLocation, parentSchema, validationContext, evaluationPath);
        Version dialect = this.validationContext.getMetaSchema().getSpecification();
        if (dialect == null || dialect.getOrder() < Version.DRAFT_2019_09.getOrder()) {
            assertionsEnabled = true;
        } else {
            // Check vocabulary
            assertionsEnabled = isFormatAssertionVocabularyEnabled(dialect,
                    this.validationContext.getMetaSchema().getVocabularies());
        }
    }

    protected boolean isFormatAssertionVocabularyEnabled() {
        return isFormatAssertionVocabularyEnabled(this.validationContext.getMetaSchema().getSpecification(),
                this.validationContext.getMetaSchema().getVocabularies());
    }

    protected boolean isFormatAssertionVocabularyEnabled(Version specification, Map<String, Boolean> vocabularies) {
        if (Version.DRAFT_2020_12.equals(specification)) {
            String vocabulary = "https://json-schema.org/draft/2020-12/vocab/format-assertion";
            return vocabularies.containsKey(vocabulary); // doesn't matter if it is true or false
        } else if (Version.DRAFT_2019_09.equals(specification)) {
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
