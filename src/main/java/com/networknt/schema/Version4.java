package com.networknt.schema;

import java.util.Arrays;

import com.networknt.schema.keyword.AnnotationKeyword;
import com.networknt.schema.keyword.NonValidationKeyword;
import com.networknt.schema.keyword.ValidatorTypeCode;

/**
 * Draft 4 dialect.
 */
public class Version4 implements JsonSchemaVersion {
    private static final String IRI = DialectId.DRAFT_4;
    private static final String ID = "id";
    
    private static class Holder {
        private static final JsonMetaSchema INSTANCE;
        static {
            INSTANCE = JsonMetaSchema.builder(IRI)
                    .specification(Specification.Version.DRAFT_4)
                    .idKeyword(ID)
                    .formats(Formats.DEFAULT)
                    .keywords(ValidatorTypeCode.getKeywords(Specification.Version.DRAFT_4))
                    // keywords that may validly exist, but have no validation aspect to them
                    .keywords(Arrays.asList(
                            new NonValidationKeyword("$schema"),
                            new NonValidationKeyword("id"),
                            new AnnotationKeyword("title"),
                            new AnnotationKeyword("description"),
                            new AnnotationKeyword("default"),
                            new NonValidationKeyword("definitions"),
                            new NonValidationKeyword("additionalItems"),
                            new AnnotationKeyword("exampleSetFlag"),
                            new NonValidationKeyword("exclusiveMinimum"), // exclusiveMinimum boolean handled by minimum validator
                            new NonValidationKeyword("exclusiveMaximum")  // exclusiveMaximum boolean handled by maximum validator
                    ))
                    .build(); 
        }
    }

    public JsonMetaSchema getInstance() {
        return Holder.INSTANCE;
    }
}
