package com.networknt.schema.dialect;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import com.networknt.schema.Formats;
import com.networknt.schema.SpecificationVersion;
import com.networknt.schema.keyword.NonValidationKeyword;
import com.networknt.schema.keyword.Keywords;

/**
 * Draft 2020-12 dialect.
 */
public class Draft202012 {
    private static final String ID = DialectId.DRAFT_2020_12;
    private static final String ID_KEYWORD = "$id";
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
        VOCABULARY = vocabulary;
    }

    private static class Holder {
        private static final Dialect INSTANCE;
        static {
            INSTANCE = Dialect.builder(ID)
                    .specificationVersion(SpecificationVersion.DRAFT_2020_12)
                    .idKeyword(ID_KEYWORD)
                    .formats(Formats.DEFAULT)
                    .keywords(Keywords.getKeywords(SpecificationVersion.DRAFT_2020_12))
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
