package com.networknt.schema;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * Draft 2019-09 dialect.
 */
public class Version201909 extends JsonSchemaVersion {
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

    @Override
    public JsonMetaSchema getInstance() {
        return new JsonMetaSchema.Builder(IRI)
                .specification(SpecVersion.VersionFlag.V201909)
                .idKeyword(ID)
                .formats(Formats.DEFAULT)
                .keywords(ValidatorTypeCode.getKeywords(SpecVersion.VersionFlag.V201909))
                // keywords that may validly exist, but have no validation aspect to them
                .keywords(Arrays.asList(
                        new NonValidationKeyword("definitions")
                ))
                .vocabularies(VOCABULARY)
                .build();
    }
}
