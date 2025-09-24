package com.networknt.schema.dialect;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import com.networknt.schema.Formats;
import com.networknt.schema.SpecificationVersion;
import com.networknt.schema.keyword.NonValidationKeyword;
import com.networknt.schema.keyword.ValidatorTypeCode;

/**
 * Draft 2019-09 dialect.
 */
public class Draft201909 {
    private static final String ID = DialectId.DRAFT_2019_09;
    private static final String ID_KEYWORD = "$id";
    private static final Map<String, Boolean> VOCABULARY;

    static {
        Map<String, Boolean> vocabulary = new HashMap<>();
        vocabulary.put("https://json-schema.org/draft/2019-09/vocab/core", true);
        vocabulary.put("https://json-schema.org/draft/2019-09/vocab/applicator", true);
        vocabulary.put("https://json-schema.org/draft/2019-09/vocab/validation", true);
        vocabulary.put("https://json-schema.org/draft/2019-09/vocab/meta-data", true);
        vocabulary.put("https://json-schema.org/draft/2019-09/vocab/format", false);
        vocabulary.put("https://json-schema.org/draft/2019-09/vocab/content", true);
        VOCABULARY = vocabulary;
    }
    
    private static class Holder {
        private static final Dialect INSTANCE;
        static {
            INSTANCE = Dialect.builder(ID)
                    .specificationVersion(SpecificationVersion.DRAFT_2019_09)
                    .idKeyword(ID_KEYWORD)
                    .formats(Formats.DEFAULT)
                    .keywords(ValidatorTypeCode.getKeywords(SpecificationVersion.DRAFT_2019_09))
                    // keywords that may validly exist, but have no validation aspect to them
                    .keywords(Collections.singletonList(
		                    new NonValidationKeyword("definitions")
                    ))
                    .vocabularies(VOCABULARY)
                    .build(); 
        }
    }

    public static Dialect getInstance() {
        return Holder.INSTANCE;
    }
}
