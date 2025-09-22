package com.networknt.schema.oas;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import com.networknt.schema.Formats;
import com.networknt.schema.JsonMetaSchema;
import com.networknt.schema.DialectId;
import com.networknt.schema.Specification;
import com.networknt.schema.keyword.NonValidationKeyword;
import com.networknt.schema.keyword.ValidatorTypeCode;

/**
 * OpenAPI 3.1.
 */
public class OpenApi31 {
    private static final String IRI = DialectId.OPENAPI_3_1;
    private static final String ID = "$id";
    private static final Map<String, Boolean> VOCABULARY;

    static {
        Map<String, Boolean> vocabulary = new HashMap<>();
        vocabulary.put("https://json-schema.org/draft/2020-12/vocab/core", true);
        vocabulary.put("https://json-schema.org/draft/2020-12/vocab/applicator", true);
        vocabulary.put("https://json-schema.org/draft/2020-12/vocab/unevaluated", true);
        vocabulary.put("https://json-schema.org/draft/2020-12/vocab/validation", true);
        vocabulary.put("https://json-schema.org/draft/2020-12/vocab/meta-data", true);
        vocabulary.put("https://json-schema.org/draft/2020-12/vocab/format-annotation", true);
        vocabulary.put("https://json-schema.org/draft/2020-12/vocab/content", true);
        vocabulary.put("https://spec.openapis.org/oas/3.1/vocab/base", false);
        VOCABULARY = vocabulary;
    }

    private static class Holder {
        private static final JsonMetaSchema INSTANCE;
        static {
            INSTANCE = JsonMetaSchema.builder(IRI)
                    .specification(Specification.Version.DRAFT_2020_12)
                    .idKeyword(ID)
                    .formats(Formats.DEFAULT)
                    .keywords(ValidatorTypeCode.getKeywords(Specification.Version.DRAFT_2020_12))
                    // keywords that may validly exist, but have no validation aspect to them
                    .keywords(Collections.singletonList(
                        new NonValidationKeyword("definitions")
                    ))
                    .vocabularies(VOCABULARY)
                    .build(); 
        }
    }

    public static JsonMetaSchema getInstance() {
        return Holder.INSTANCE;
    }
}
