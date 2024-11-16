package com.networknt.schema;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Draft 2019-09 dialect.
 */
public class Version201909 implements JsonSchemaVersion {
    private static final String IRI = SchemaId.V201909;
    private static final String ID = "$id";
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
        private static final JsonMetaSchema INSTANCE;
        static {
            INSTANCE = JsonMetaSchema.builder(IRI)
                    .specification(SpecVersion.VersionFlag.V201909)
                    .idKeyword(ID)
                    .formats(Formats.DEFAULT)
                    .keywords(ValidatorTypeCode.getKeywords(SpecVersion.VersionFlag.V201909))
                    // keywords that may validly exist, but have no validation aspect to them
                    .keywords(Collections.singletonList(
		                    new NonValidationKeyword("definitions")
                    ))
                    .vocabularies(VOCABULARY)
                    .build(); 
        }
    }

    @Override
    public JsonMetaSchema getInstance() {
        return Holder.INSTANCE;
    }
}
