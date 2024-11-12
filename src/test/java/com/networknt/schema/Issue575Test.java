package com.networknt.schema;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.InputStream;
import java.util.Set;
import java.util.stream.Stream;

/**
 * This project uses a dependency (com.ethlo.time:itu) to validate time representations. Version 1.51 of this library
 * has a problem dealing with certain time zones having a negative offset; for example "-2:30" (Newfoundland time, NDT).
 * Moving to version 1.7.0 of this library resolves the issue.
 * <p>
 * This test class confirms that valid negative offsets do not result in a JSON validation error if the ITU library is
 * updated to version 1.7.0 or later.
 */
class Issue575Test {
    private static JsonSchema schema;

    @BeforeAll
    static void init() {
        JsonSchemaFactory factory = JsonSchemaFactory.getInstance(SpecVersion.VersionFlag.V201909);
        String schemaPath = "/schema/issue575-2019-09.json";
        InputStream schemaInputStream = Issue575Test.class.getResourceAsStream(schemaPath);
        schema = factory.getSchema(schemaInputStream);
    }

    static Stream<Arguments> validTimeZoneOffsets() {
        String json1 = "{\"testDateTime\":\"2022-05-18T08:27:53-05:00\"}";         // America/New_York
        String json2 = "{\"testDateTime\":\"2022-05-18T08:27:53-04:00\"}";         // America/New_York (DST)
        String json3 = "{\"testDateTime\":\"2022-05-18T08:27:53-03:30\"}";         // America/St_Johns
        String json4 = "{\"testDateTime\":\"2022-05-18T08:27:53-02:30\"}";         // America/St_Johns (DST)
        String json5 = "{\"testDateTime\":\"2022-05-18T08:27:53+02:00\"}";         // Africa/Cairo
        String json6 = "{\"testDateTime\":\"2022-05-18T08:27:53+03:30\"}";         // Asia/Tehran
        String json7 = "{\"testDateTime\":\"2022-05-18T08:27:53+04:30\"}";         // Asia/Tehran (DST)
        String json8 = "{\"testDateTime\":\"2022-05-18T08:27:53+04:00\"}";         // Asia/Dubai
        String json9 = "{\"testDateTime\":\"2022-05-18T08:27:53+05:00\"}";         // Indian/Maldives
        String json10 = "{\"testDateTime\":\"2022-05-18T08:27:53+05:30\"}";        // Asia/Kolkata
        String json11 = "{\"testDateTime\":\"2022-05-18T08:27:53+10:00\"}";        // Australia/Sydney
        String json12 = "{\"testDateTime\":\"2022-05-18T08:27:53+11:00\"}";        // Australia/Sydney (DST)
        String json13 = "{\"testDateTime\":\"2022-05-18T08:27:53+14:00\"}";        // Pacific/Kiritimati
        String json14 = "{\"testDateTime\":\"2022-05-18T18:45:32.123-05:00\"}";    // America/New_York
        String json15 = "{\"testDateTime\":\"2022-05-18T18:45:32.123456-05:00\"}"; // America/New_York
        String json16 = "{\"testDateTime\":\"2022-05-18T08:27:53Z\"}";             // UTC
        String json17 = "{\"testDateTime\":\"2022-05-18T08:27:53+00:00\"}";        // UTC

        return Stream.of(
                Arguments.of(json1),
                Arguments.of(json2),
                Arguments.of(json3),
                Arguments.of(json4),
                Arguments.of(json5),
                Arguments.of(json6),
                Arguments.of(json7),
                Arguments.of(json8),
                Arguments.of(json9),
                Arguments.of(json10),
                Arguments.of(json11),
                Arguments.of(json12),
                Arguments.of(json13),
                Arguments.of(json14),
                Arguments.of(json15),
                Arguments.of(json16),
                Arguments.of(json17)
        );
    }

    /**
     * Confirms that valid time zone offsets do not result in a JSON validation error.
     *
     * @param jsonObject a sample JSON payload to test
     */
    @ParameterizedTest
    @MethodSource("validTimeZoneOffsets")
    void testValidTimeZoneOffsets(String jsonObject) throws JsonProcessingException {
        Set<ValidationMessage> errors = schema.validate(new ObjectMapper().readTree(jsonObject));
        Assertions.assertTrue(errors.isEmpty());
    }

    static Stream<Arguments> invalidTimeRepresentations() {
        // Invalid JSON payload: 30 days in April
        String json1 = "{\"testDateTime\":\"2022-04-31T08:27:53+05:00\"}";
        // Invalid JSON payload: Invalid date/time separator
        String json2 = "{\"testDateTime\":\"2022-05-18X08:27:53+05:00\"}";
        // Invalid JSON payload: Time zone details are missing
        String json3 = "{\"testDateTime\":\"2022-05-18T08:27:53\"}";
        // Invalid JSON payload: seconds missing from time
        String json4 = "{\"testDateTime\":\"2022-05-18T11:23Z\"}";
        // Invalid JSON payload: Text instead of date-time value
        String json5 = "{\"testDateTime\":\"Orlando\"}";
        // Invalid JSON payload: A time zone offset of +23:00 is not valid
        String json6 = "{\"testDateTime\":\"2022-05-18T08:27:53+23:00\"}";
        // Invalid JSON payload: A time zone offset of -23:00 is not valid
        String json7 = "{\"testDateTime\":\"2022-05-18T08:27:53-23:00\"}";
        // Invalid JSON payload: com.ethlo.time:itu does not allow offset -00:00 (Valid per RFC3339 section 4.3. but prohibited in ISO-8601)
        String json8 = "{\"testDateTime\":\"2022-05-18T08:27:53-00:00\"}";

        return Stream.of(
                Arguments.of(json1),
                Arguments.of(json2),
                Arguments.of(json3),
                Arguments.of(json4),
                Arguments.of(json5),
                Arguments.of(json6),
                Arguments.of(json7),
                Arguments.of(json8)
        );
    }

    /**
     * Confirms that invalid time representations result in one or more a JSON validation errors.
     *
     * @param jsonObject a sample JSON payload to test
     */
    @ParameterizedTest
    @MethodSource("invalidTimeRepresentations")
    void testInvalidTimeRepresentations(String jsonObject) throws JsonProcessingException {
        Set<ValidationMessage> errors = schema.validate(new ObjectMapper().readTree(jsonObject), OutputFormat.DEFAULT, (executionContext, validationContext) -> {
            executionContext.getExecutionConfig().setFormatAssertionsEnabled(true);
        });
        Assertions.assertFalse(errors.isEmpty());
    }
}
