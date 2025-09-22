package com.networknt.schema.dialect;

import java.util.Arrays;

import com.networknt.schema.Formats;
import com.networknt.schema.Specification;
import com.networknt.schema.keyword.AnnotationKeyword;
import com.networknt.schema.keyword.NonValidationKeyword;
import com.networknt.schema.keyword.ValidatorTypeCode;

/**
 * Draft 7 dialect.
 */
public class Draft7 {
    private static final String IRI = DialectId.DRAFT_7;
    private static final String ID = "$id";

    private static class Holder {
        private static final Dialect INSTANCE;
        static {
            INSTANCE = Dialect.builder(IRI)
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

    public static Dialect getInstance() {
        return Holder.INSTANCE; 
    }
}
