/*
 * Copyright (c) 2025 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.networknt.schema.format;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.Test;

import com.networknt.schema.Error;
import com.networknt.schema.InputFormat;
import com.networknt.schema.Schema;
import com.networknt.schema.SchemaRegistry;
import com.networknt.schema.SpecificationVersion;
import com.networknt.schema.serialization.JsonMapperFactory;

class IdnEmailFormatTest {

    /** U+00A0 NON-BREAKING SPACE, built from its code point so no invisible character sits in the source. */
    private static final String NBSP = new String(Character.toChars(0x00A0));

    /**
     * Validates {@code email} against a {@code {"format": "idn-email"}} schema with
     * format assertions turned on, and returns the validation errors. The email is
     * serialized via the mapper so a value containing quotes is escaped correctly.
     */
    private List<Error> validateIdnEmail(String email) {
        String schemaData = "{\r\n"
                + "  \"format\": \"idn-email\"\r\n"
                + "}";
        String inputData = JsonMapperFactory.getInstance().writeValueAsString(email);
        Schema schema = SchemaRegistry.withDefaultDialect(SpecificationVersion.DRAFT_2020_12).getSchema(schemaData);
        return schema.validate(inputData, InputFormat.JSON,
                executionContext -> executionContext.executionConfig(executionConfig -> executionConfig.formatAssertionsEnabled(true)));
    }

    /** Sanity check that the test harness accepts a plainly valid idn-email. */
    @Test
    void validIdnEmailShouldPass() {
        List<Error> messages = validateIdnEmail("name@email.com");
        assertTrue(messages.isEmpty(), "a plainly valid idn-email should pass");
    }

    /**
     * idn-email allows non-ASCII letters (unlike ASCII-only email), so the narrow
     * whitespace-only guard must not reject a non-ASCII letter in the local part.
     */
    @Test
    void idnEmailWithNonAsciiLetterShouldPass() {
        // U+00FC LATIN SMALL LETTER U WITH DIAERESIS, built from its code point.
        String localPart = "m" + new String(Character.toChars(0x00FC)) + "nchen";
        List<Error> messages = validateIdnEmail(localPart + "@example.com");
        assertTrue(messages.isEmpty(), "idn-email should allow a non-ASCII letter in the local part");
    }

    /**
     * idn-email shares EmailFormat's delegation, so a leading non-breaking space
     * (U+00A0) must be rejected here too.
     */
    @Test
    void idnEmailWithLeadingNbspShouldFail() {
        List<Error> messages = validateIdnEmail(NBSP + "name@email.com");
        assertFalse(messages.isEmpty(), "idn-email with a leading non-breaking space (U+00A0) should be invalid");
    }
}
