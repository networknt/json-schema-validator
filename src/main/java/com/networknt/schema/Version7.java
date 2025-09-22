package com.networknt.schema;

import java.util.Arrays;

import com.networknt.schema.keyword.AnnotationKeyword;
import com.networknt.schema.keyword.NonValidationKeyword;
import com.networknt.schema.keyword.ValidatorTypeCode;

/**
 * Draft 7 dialect.
 */
public class Version7 implements JsonSchemaVersion {
    private static final String IRI = SchemaId.V7;
    private static final String ID = "$id";

    private static class Holder {
        private static final JsonMetaSchema INSTANCE;
        static {
            INSTANCE = JsonMetaSchema.builder(IRI)
                    .specification(Specification.Version.DRAFT_7)
                    .idKeyword(ID)
                    .formats(Formats.DEFAULT)
                    .keywords(ValidatorTypeCode.getKeywords(Specification.Version.DRAFT_7))
                    // keywords that may validly exist, but have no validation aspect to them
                    .keywords(Arrays.asList(
                            new NonValidationKeyword("$schema"),
                            new NonValidationKeyword("$id"),
                            new AnnotationKeyword("title"),
                            new AnnotationKeyword("description"),
                            new AnnotationKeyword("default"),
                            new NonValidationKeyword("definitions"),
                            new NonValidationKeyword("$comment"),
                            new AnnotationKeyword("examples"),
                            new NonValidationKeyword("then"),
                            new NonValidationKeyword("else"),
                            new NonValidationKeyword("additionalItems")))
                    .build();
        }
    }

    @Override
    public JsonMetaSchema getInstance() {
        return Holder.INSTANCE; 
    }
}
