package com.networknt.schema;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * Draft 2020-12 dialect.
 */
public class Version202012 extends JsonSchemaVersion {
    private static final String IRI = SchemaId.V202012;
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
        VOCABULARY = vocabulary;
    }

    @Override
    public JsonMetaSchema getInstance() {
        return new JsonMetaSchema.Builder(IRI)
                .specification(SpecVersion.VersionFlag.V202012)
                .idKeyword(ID)
                .addFormats(Formats.DEFAULT)
                .addKeywords(ValidatorTypeCode.getKeywords(SpecVersion.VersionFlag.V202012))
                // keywords that may validly exist, but have no validation aspect to them
                .addKeywords(Arrays.asList(
                        new NonValidationKeyword("definitions")
                ))
                .vocabularies(VOCABULARY)
                .build();
    }
}
