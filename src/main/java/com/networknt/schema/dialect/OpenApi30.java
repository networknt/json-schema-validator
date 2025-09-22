package com.networknt.schema.dialect;

import java.util.Arrays;

import com.networknt.schema.Formats;
import com.networknt.schema.Specification;
import com.networknt.schema.keyword.AnnotationKeyword;
import com.networknt.schema.keyword.NonValidationKeyword;
import com.networknt.schema.keyword.ValidatorTypeCode;

/**
 * OpenAPI 3.0.
 */
public class OpenApi30 {
    private static final String IRI = DialectId.OPENAPI_3_0;
    private static final String ID = "id";
    
    private static class Holder {
        private static final Dialect INSTANCE;
        static {
            INSTANCE = Dialect.builder(IRI)
                    .specification(Specification.Version.DRAFT_4)
                    .idKeyword(ID)
                    .formats(Formats.DEFAULT)
                    .keywords(Arrays.asList(
                            new AnnotationKeyword("title"),
                            ValidatorTypeCode.PATTERN,
                            ValidatorTypeCode.REQUIRED,
                            ValidatorTypeCode.ENUM,
                            ValidatorTypeCode.MINIMUM,
                            ValidatorTypeCode.MAXIMUM,
                            ValidatorTypeCode.MULTIPLE_OF,
                            ValidatorTypeCode.MIN_LENGTH,
                            ValidatorTypeCode.MAX_LENGTH,
                            ValidatorTypeCode.MIN_ITEMS,
                            ValidatorTypeCode.MAX_ITEMS,
                            ValidatorTypeCode.UNIQUE_ITEMS,
                            ValidatorTypeCode.MIN_PROPERTIES,
                            ValidatorTypeCode.MAX_PROPERTIES,
                            
                            ValidatorTypeCode.TYPE,
                            ValidatorTypeCode.FORMAT,
                            new AnnotationKeyword("description"),
                            ValidatorTypeCode.ITEMS,
                            ValidatorTypeCode.PROPERTIES,
                            ValidatorTypeCode.ADDITIONAL_PROPERTIES,
                            new AnnotationKeyword("default"),
                            ValidatorTypeCode.ALL_OF,
                            ValidatorTypeCode.ONE_OF,
                            ValidatorTypeCode.ANY_OF,
                            ValidatorTypeCode.NOT,

                            new AnnotationKeyword("deprecated"),
                            ValidatorTypeCode.DISCRIMINATOR,
                            new AnnotationKeyword("example"),
                            new AnnotationKeyword("externalDocs"),
                            new NonValidationKeyword("nullable"),
                            ValidatorTypeCode.READ_ONLY,
                            ValidatorTypeCode.WRITE_ONLY,
                            new AnnotationKeyword("xml"),
                            
                            ValidatorTypeCode.REF
                    ))
                    .build(); 
        }
    }

    public static Dialect getInstance() {
        return Holder.INSTANCE;
    }
}
