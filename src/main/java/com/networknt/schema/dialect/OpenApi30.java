package com.networknt.schema.dialect;

import java.util.Arrays;

import com.networknt.schema.Formats;
import com.networknt.schema.SpecificationVersion;
import com.networknt.schema.keyword.AnnotationKeyword;
import com.networknt.schema.keyword.NonValidationKeyword;
import com.networknt.schema.keyword.KeywordType;

/**
 * OpenAPI 3.0.
 */
public class OpenApi30 {
    private static final String ID = DialectId.OPENAPI_3_0;
    private static final String ID_KEYWORD = "id";
    
    private static class Holder {
        private static final Dialect INSTANCE;
        static {
            INSTANCE = Dialect.builder(ID)
                    .specificationVersion(SpecificationVersion.DRAFT_4)
                    .idKeyword(ID_KEYWORD)
                    .formats(Formats.DEFAULT)
                    .keywords(Arrays.asList(
                            new AnnotationKeyword("title"),
                            KeywordType.PATTERN,
                            KeywordType.REQUIRED,
                            KeywordType.ENUM,
                            KeywordType.MINIMUM,
                            KeywordType.MAXIMUM,
                            KeywordType.MULTIPLE_OF,
                            KeywordType.MIN_LENGTH,
                            KeywordType.MAX_LENGTH,
                            KeywordType.MIN_ITEMS,
                            KeywordType.MAX_ITEMS,
                            KeywordType.UNIQUE_ITEMS,
                            KeywordType.MIN_PROPERTIES,
                            KeywordType.MAX_PROPERTIES,
                            
                            KeywordType.TYPE,
                            KeywordType.FORMAT,
                            new AnnotationKeyword("description"),
                            KeywordType.ITEMS,
                            KeywordType.PROPERTIES,
                            KeywordType.ADDITIONAL_PROPERTIES,
                            new AnnotationKeyword("default"),
                            KeywordType.ALL_OF,
                            KeywordType.ONE_OF,
                            KeywordType.ANY_OF,
                            KeywordType.NOT,

                            new AnnotationKeyword("deprecated"),
                            KeywordType.DISCRIMINATOR,
                            new AnnotationKeyword("example"),
                            new AnnotationKeyword("externalDocs"),
                            new NonValidationKeyword("nullable"),
                            KeywordType.READ_ONLY,
                            KeywordType.WRITE_ONLY,
                            new AnnotationKeyword("xml"),
                            
                            KeywordType.REF
                    ))
                    .build(); 
        }
    }

    public static Dialect getInstance() {
        return Holder.INSTANCE;
    }
}
