package com.networknt.schema.format;

import java.util.Map;

import com.fasterxml.jackson.databind.JsonNode;
import com.networknt.schema.BaseJsonValidator;
import com.networknt.schema.ErrorMessageType;
import com.networknt.schema.ExecutionContext;
import com.networknt.schema.JsonNodePath;
import com.networknt.schema.JsonSchema;
import com.networknt.schema.Keyword;
import com.networknt.schema.SchemaLocation;
import com.networknt.schema.ValidationContext;
import com.networknt.schema.SpecVersion.VersionFlag;

public abstract class BaseFormatJsonValidator extends BaseJsonValidator {
    protected final boolean assertionsEnabled;
    
    public BaseFormatJsonValidator(SchemaLocation schemaLocation, JsonNodePath evaluationPath, JsonNode schemaNode,
            JsonSchema parentSchema, ErrorMessageType errorMessageType, Keyword keyword,
            ValidationContext validationContext) {
        super(schemaLocation, evaluationPath, schemaNode, parentSchema, errorMessageType, keyword, validationContext, false);
        VersionFlag dialect = this.validationContext.getMetaSchema().getSpecification();
        if (dialect == null || dialect.getVersionFlagValue() < VersionFlag.V201909.getVersionFlagValue()) {
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

    protected boolean isFormatAssertionVocabularyEnabled(VersionFlag specification, Map<String, Boolean> vocabularies) {
        if (VersionFlag.V202012.equals(specification)) {
            String vocabulary = "https://json-schema.org/draft/2020-12/vocab/format-assertion";
            return vocabularies.containsKey(vocabulary); // doesn't matter if it is true or false
        } else if (VersionFlag.V201909.equals(specification)) {
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
