package com.networknt.schema;

import java.util.Arrays;

import com.networknt.schema.keyword.AnnotationKeyword;
import com.networknt.schema.keyword.NonValidationKeyword;
import com.networknt.schema.keyword.ValidatorTypeCode;

/**
 * Draft 6 dialect.
 */
public class Version6 implements JsonSchemaVersion {
    private static final String IRI = DialectId.DRAFT_6;
    // Draft 6 uses "$id"
    private static final String ID = "$id";

    private static class Holder {
        private static final Dialect INSTANCE;
        static {
            INSTANCE = Dialect.builder(IRI)
                    .specification(Specification.Version.DRAFT_6)
                    .idKeyword(ID)
                    .formats(Formats.DEFAULT)
                    .keywords(ValidatorTypeCode.getKeywords(Specification.Version.DRAFT_6))
                    // keywords that may validly exist, but have no validation aspect to them
                    .keywords(Arrays.asList(
                            new NonValidationKeyword("$schema"),
                            new NonValidationKeyword("$id"),
                            new AnnotationKeyword("title"),
                            new AnnotationKeyword("description"),
                            new AnnotationKeyword("default"),
                            new NonValidationKeyword("additionalItems"),
                            new NonValidationKeyword("definitions"),
                            new AnnotationKeyword("examples")
                    ))
                    .build();
        }
    }

    public Dialect getInstance() {
        return Holder.INSTANCE;
    }
}
