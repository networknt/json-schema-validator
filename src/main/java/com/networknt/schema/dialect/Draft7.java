package com.networknt.schema.dialect;

import java.util.Arrays;

import com.networknt.schema.Formats;
import com.networknt.schema.SpecificationVersion;
import com.networknt.schema.keyword.AnnotationKeyword;
import com.networknt.schema.keyword.NonValidationKeyword;
import com.networknt.schema.keyword.Keywords;

/**
 * Draft 7 dialect.
 */
public class Draft7 {
    private static final String ID = DialectId.DRAFT_7;
    private static final String ID_KEYWORD = "$id";

    private static class Holder {
        private static final Dialect INSTANCE;
        static {
            INSTANCE = Dialect.builder(ID)
                    .specificationVersion(SpecificationVersion.DRAFT_7)
                    .idKeyword(ID_KEYWORD)
                    .formats(Formats.DEFAULT)
                    .keywords(Keywords.getKeywords(SpecificationVersion.DRAFT_7))
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
