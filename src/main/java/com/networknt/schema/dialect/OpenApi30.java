package com.networknt.schema.dialect;

import java.util.Arrays;

import com.networknt.schema.Formats;
import com.networknt.schema.SpecificationVersion;
import com.networknt.schema.keyword.AnnotationKeyword;
import com.networknt.schema.keyword.NonValidationKeyword;
import com.networknt.schema.keyword.Keywords;

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
                            Keywords.PATTERN,
                            Keywords.REQUIRED,
                            Keywords.ENUM,
                            Keywords.MINIMUM,
                            Keywords.MAXIMUM,
                            Keywords.MULTIPLE_OF,
                            Keywords.MIN_LENGTH,
                            Keywords.MAX_LENGTH,
                            Keywords.MIN_ITEMS,
                            Keywords.MAX_ITEMS,
                            Keywords.UNIQUE_ITEMS,
                            Keywords.MIN_PROPERTIES,
                            Keywords.MAX_PROPERTIES,
                            
                            Keywords.TYPE,
                            Keywords.FORMAT,
                            new AnnotationKeyword("description"),
                            Keywords.ITEMS,
                            Keywords.PROPERTIES,
                            Keywords.ADDITIONAL_PROPERTIES,
                            new AnnotationKeyword("default"),
                            Keywords.ALL_OF,
                            Keywords.ONE_OF,
                            Keywords.ANY_OF,
                            Keywords.NOT,

                            new AnnotationKeyword("deprecated"),
                            Keywords.DISCRIMINATOR,
                            new AnnotationKeyword("example"),
                            new AnnotationKeyword("externalDocs"),
                            new NonValidationKeyword("nullable"),
                            Keywords.READ_ONLY,
                            Keywords.WRITE_ONLY,
                            new AnnotationKeyword("xml"),
                            
                            Keywords.REF
                    ))
                    .build(); 
        }
    }

    public static Dialect getInstance() {
        return Holder.INSTANCE;
    }
}
