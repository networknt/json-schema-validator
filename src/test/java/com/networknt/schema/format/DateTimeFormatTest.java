/*
 * Copyright (c) 2026 the original author or authors.
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
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.junit.jupiter.params.provider.CsvSource;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.util.List;

import com.networknt.schema.InputFormat;
import com.networknt.schema.Schema;
import com.networknt.schema.SchemaRegistry;
import com.networknt.schema.SpecificationVersion;
import com.networknt.schema.utils.Classes;

class DateTimeFormatTest {
    private static final Schema SCHEMA = SchemaRegistry.withDefaultDialect(SpecificationVersion.DRAFT_2020_12)
            .getSchema("{\"type\":\"string\",\"format\":\"date-time\"}");

    @ParameterizedTest
    @ValueSource(strings = {
            "2020-01-01T12:00:00Zjunk",
            "2020-01-01T12:00:00zjunk",
            "2020-01-01T12:00:00ZjunkZ",
            "2020-01-01T12:00:00+01:00junk",
            "2020-01-01T12:00:00+01x00",
            "2020-01-01T12:00:00+0100",
            "2020-01-01T12:00:00+01:0",
            "2016-12-31T23:59:60Zjunk"
    })
    void rejectsTrailingTextAfterOffset(String value) {
        assertFalse(SCHEMA.validate("\"" + value + "\"", InputFormat.JSON,
                ec -> ec.executionConfig(config -> config.formatAssertionsEnabled(true))).isEmpty(), value);
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "2020-01-01T12:00:00Z",
            "2020-01-01t12:00:00Z",
            "2020-01-01t12:00:00z",
            "2020-01-01T12:00:00.123456789+01:00",
            "2020-01-01t12:00:00-05:30",
            "2016-12-31t23:59:60Z",
            "2020-01-01T12:00:00z",
            "2020-01-01T12:00:00+01:00",
            "2016-12-31T23:59:60Z",
            "2017-01-01T00:59:60+01:00"
    })
    void acceptsValidOffsetsAndLeapSecond(String value) {
        assertTrue(SCHEMA.validate("\"" + value + "\"", InputFormat.JSON,
                ec -> ec.executionConfig(config -> config.formatAssertionsEnabled(true))).isEmpty(), value);
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "",
            "2020-01-01",
            "2020-01-01 12:00:00Z",
            "2020-01-01 12:00:00+01:00",
            "2020-01-01 12:00:00.123456789Z",
            "2016-12-31 23:59:60Z",
            "2020-01-01_12:00:00Z",
            "2020-01-01  12:00:00Z"
    })
    void rejectsNonTSeparators(String value) {
        assertFalse(SCHEMA.validate("\"" + value + "\"", InputFormat.JSON,
                ec -> ec.executionConfig(config -> config.formatAssertionsEnabled(true))).isEmpty(), value);
        assertFalse(DateTimeFormat.JavaTimeOffsetDateTime.isValid(value), value);
    }

    /**
     * OffsetDateTime.parse(..) accepts these three non-RFC 3339 values; both validation paths reject them.
     */
    @ParameterizedTest
    @ValueSource(strings = {
            "2020-01-01T12:00Z",
            "2020-01-01T12:00:00+01",
            "2020-01-01T12:00:00.Z",
            "2016-12-31T23:59:60",
            "2015-06-30T23:59:60.123456789"
    })
    void bothPathsRejectValuesThatAreNotRfc3339(String value) {
        // The schema takes the Ethlo path, as long as Ethlo is on the test classpath
        assertTrue(Classes.isPresent("com.ethlo.time.ITU", DateTimeFormatTest.class.getClassLoader()), "Ethlo is expected on the test classpath");
        assertFalse(SCHEMA.validate("\"" + value + "\"", InputFormat.JSON,
                ec -> ec.executionConfig(config -> config.formatAssertionsEnabled(true))).isEmpty(), value);
        assertFalse(DateTimeFormat.JavaTimeOffsetDateTime.isValid(value), value);
    }

    @Test
    void formatAnnotationDoesNotRejectSpaceSeparator() {
        assertTrue(SCHEMA.validate("\"2020-01-01 12:00:00Z\"", InputFormat.JSON,
                ec -> ec.executionConfig(config -> config.formatAssertionsEnabled(false))).isEmpty());
    }

    @Test
    void bothPathsRejectSpaceSeparator() {
        String value = "2020-01-01 12:00:00Z";
        assertFalse(DateTimeFormat.Ethlo.isValid(value));
        assertFalse(DateTimeFormat.JavaTimeOffsetDateTime.isValid(value));
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "2020-01-01T12:00:00.+01:00",
            "2020-01-01T12:00:00+01:00:00",
            "2020-01-01 12:00:00Z",
            "2020-01-01T12:00:00",
            "2020-01-01T",
            "2020-01-01",
            "2020-1-01T12:00:00Z",
            "+2020-01-01T12:00:00Z",
            "2020-02-30T12:00:00Z",
            "2021-02-29T12:00:00Z",
            "2020-13-01T12:00:00Z",
            "2020-01-01T24:00:00Z",
            "2020-01-01T23:59:61Z",
            "2020-01-01T12:59:60Z",
            "2020-01-01T24:59:60+01:00",
            "2020-01-01T23:89:60+00:30",
            "2020-01-01T45:59:60+22:00",
            "2021-03-15T23:59:60Z",
            "2016-12-30T23:59:60Z",
            "2017-01-02T23:58:60+23:59",
            "2016-12-30T00:00:60-23:59",
            "2016-12-31T23:59:60+01:00",
            "1998-12-31T15:59:60.123-07:00",
            "-001-01-01T00:00:00Z",
            "10000-01-01T00:00:00Z",
            "-0001-12-31T23:59:60Z",
            "0000-01-01T00:59:60+00:59",
            "9999-12-31T23:59:60-00:01",
            "2020-01-01T12:00:00+24:00",
            "2020-01-01T12:00:00+0100",
            "2020-01-01T12:00:00+01:0",
            "2020-01-01T12:00:00Zjunk"
    })
    void fallbackRejectsValuesThatAreNotRfc3339(String value) {
        assertFalse(DateTimeFormat.JavaTimeOffsetDateTime.isValid(value), value);
    }

    /**
     * Valid RFC 3339 date-times, including ones that OffsetDateTime.parse(..) rejects. A leap second
     * has to be at the end of a June 30 or a December 31 in UTC, whatever the offset.
     */
    @ParameterizedTest
    @ValueSource(strings = {
            "2020-01-01T12:00:00Z",
            "2020-01-01t12:00:00z",
            "2020-01-01T12:00:00.5+01:00",
            "2016-12-31T23:59:60Z",
            "2016-12-31T23:59:60.5Z",
            "2015-06-30T23:59:60Z",
            "2017-01-01T00:59:60+01:00",
            "2017-01-01T01:59:60+02:00",
            "2017-01-01T23:58:60+23:59",
            "2016-12-31T00:00:60-23:59",
            "2016-12-31T18:59:60-05:00",
            "1998-12-31T15:59:60.123-08:00",
            "2015-07-01T08:59:60+09:00",
            "0000-01-01T00:00:00Z",
            // The four-digit year applies to the written date, even when UTC is in year -1.
            "0000-01-01T00:59:60+01:00",
            "9999-12-31T23:59:60Z",
            "9999-12-31T23:59:59Z",
            "2020-01-01T12:00:00.1234567890Z",
            "2020-01-01T12:00:00+23:59",
            "2020-01-01T12:00:00-00:00"
    })
    void fallbackAcceptsRfc3339DateTimes(String value) {
        assertTrue(DateTimeFormat.JavaTimeOffsetDateTime.isValid(value), value);
    }

    /**
     * Loads the library a second time through a class loader without Ethlo, so that the date-time
     * format is wired to the java.time fallback, and validates through a schema like a user would.
     * Draft 7 asserts formats by default.
     */
    @Test
    @SuppressWarnings({"unchecked", "rawtypes"})
    void validatesWithTheJavaTimeFallbackWhenEthloIsAbsent() throws Exception {
        ClassLoader withoutEthlo = new ClassLoader(DateTimeFormatTest.class.getClassLoader()) {
            @Override
            protected Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException {
                if (name.startsWith("com.ethlo.")) {
                    throw new ClassNotFoundException(name);
                }
                if (!name.startsWith("com.networknt.")) {
                    return super.loadClass(name, resolve);
                }
                synchronized (getClassLoadingLock(name)) {
                    Class<?> loaded = findLoadedClass(name);
                    if (loaded == null) {
                        byte[] bytes = read(name);
                        loaded = defineClass(name, bytes, 0, bytes.length);
                    }
                    return loaded;
                }
            }

            private byte[] read(String name) throws ClassNotFoundException {
                try (InputStream in = getParent().getResourceAsStream(name.replace('.', '/') + ".class")) {
                    if (in == null) {
                        throw new ClassNotFoundException(name);
                    }
                    ByteArrayOutputStream out = new ByteArrayOutputStream();
                    byte[] buffer = new byte[8192];
                    for (int n = in.read(buffer); n != -1; n = in.read(buffer)) {
                        out.write(buffer, 0, n);
                    }
                    return out.toByteArray();
                } catch (IOException e) {
                    throw new ClassNotFoundException(name, e);
                }
            }
        };
        assertThrows(ClassNotFoundException.class, () -> Class.forName("com.ethlo.time.ITU", false, withoutEthlo));

        Class<?> registry = withoutEthlo.loadClass("com.networknt.schema.SchemaRegistry");
        Class<?> version = withoutEthlo.loadClass("com.networknt.schema.SpecificationVersion");
        Class<?> inputFormat = withoutEthlo.loadClass("com.networknt.schema.InputFormat");
        Object draft7 = Enum.valueOf((Class) version, "DRAFT_7");
        Object json = Enum.valueOf((Class) inputFormat, "JSON");
        Object schema = registry.getMethod("getSchema", String.class)
                .invoke(registry.getMethod("withDefaultDialect", version).invoke(null, draft7), "{\"type\":\"string\",\"format\":\"date-time\"}");
        Method validate = schema.getClass().getMethod("validate", String.class, inputFormat);

        // These RFC 3339 values must also work through the actual no-Ethlo class-loading path.
        assertTrue(((List<?>) validate.invoke(schema, "\"2020-01-01T12:00:00+23:59\"", json)).isEmpty());
        assertTrue(((List<?>) validate.invoke(schema, "\"2004-09-25T09:55:11-00:00\"", json)).isEmpty());
        assertTrue(((List<?>) validate.invoke(schema, "\"2020-01-01T12:00:00.12345678901234567890Z\"", json)).isEmpty());
        assertFalse(((List<?>) validate.invoke(schema, "\"2020-01-01 12:00:00Z\"", json)).isEmpty());
        assertTrue(((List<?>) validate.invoke(schema, "\"2017-01-01T00:59:60+01:00\"", json)).isEmpty());
        assertFalse(((List<?>) validate.invoke(schema, "\"2020-01-01T12:00Z\"", json)).isEmpty());
        assertFalse(((List<?>) validate.invoke(schema, "\"2020-01-01T24:59:60+01:00\"", json)).isEmpty());
        assertFalse(((List<?>) validate.invoke(schema, "\"2016-12-30T23:59:60Z\"", json)).isEmpty());
        for (String value : new String[] { "1979-12-31T23:59:60Z", "1987-12-31T23:59:60Z",
                "1971-12-31T23:59:60Z", "1988-12-31T23:59:60Z" }) {
            assertTrue(((List<?>) validate.invoke(schema, "\"" + value + "\"", json)).isEmpty(), value);
        }
        assertFalse(((List<?>) validate.invoke(schema, "\"2016-12-31T23:59:60\"", json)).isEmpty());
    }
    @ParameterizedTest
    @ValueSource(strings = {
            "2004-09-25T09:55:11-00:00",
            "2020-01-01T12:00:00.1234567890Z",
            "2020-01-01T12:00:00.123456789012345678901234567890-00:00",
            "2020-01-01t12:00:00.12345678901234567890z",
            "2020-01-01T12:00:00+18:01",
            "2020-01-01T12:00:00-18:01",
            "2020-01-01T12:00:00+23:59",
            "2020-01-01T12:00:00-23:59",
            "0000-01-01T00:00:00+23:59",
            "9999-12-31T23:59:59-23:59",
            "2016-12-31T23:59:60-00:00",
            "2016-12-31T23:59:60.12345678901234567890Z",
            "2017-01-01T23:58:60.12345678901234567890+23:59",
            "2016-12-31T00:00:60.12345678901234567890-23:59"
    })
    void bothPathsAcceptTheRemainingIssue1316Cases(String value) {
        assertTrue(DateTimeFormat.Ethlo.isValid(value), value);
        assertTrue(DateTimeFormat.JavaTimeOffsetDateTime.isValid(value), value);
        assertTrue(SCHEMA.validate("\"" + value + "\"", InputFormat.JSON,
                ec -> ec.executionConfig(config -> config.formatAssertionsEnabled(true))).isEmpty(), value);
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "2003-02-03 12:07:54.27+06:43",
            "2020-01-01T12:00:00.Z",
            "2020-01-01T12:00:00.+23:59",
            "2020-01-01T12:00Z",
            "2020-01-01T12:00:00+01",
            "2020-01-01T12:00:00+24:00",
            "2020-01-01T12:00:00-24:00",
            "2020-01-01T12:00:00+23:60",
            "2020-01-01T12:00:00-23:60",
            "2020-01-01T12:00:00+01:00:00",
            "2020-01-01T12:00:00.1234567890Zjunk",
            "2020-02-30T12:00:00.1234567890+23:59",
            "2017-01-01T23:59:60+23:59",
            "2016-12-30T00:00:60-23:59",
            "2016-12-31T23:59:61Z"
    })
    void bothPathsRejectMalformedAndInvalidBoundaryCases(String value) {
        assertFalse(SCHEMA.validate("\"" + value + "\"", InputFormat.JSON,
                ec -> ec.executionConfig(config -> config.formatAssertionsEnabled(true))).isEmpty(), value);
        assertFalse(DateTimeFormat.JavaTimeOffsetDateTime.isValid(value), value);
    }

    @Test
    void ethloStillRequiresAnActuallyRecordedLeapSecond() {
        for (String value : new String[] { "2020-06-30T23:59:60Z", "2021-01-01T23:58:60+23:59",
                "0000-01-01T00:59:60+01:00", "9999-12-31T23:59:60Z" }) {
            assertTrue(DateTimeFormat.JavaTimeOffsetDateTime.isValid(value), value);
            assertFalse(SCHEMA.validate("\"" + value + "\"", InputFormat.JSON,
                    ec -> ec.executionConfig(config -> config.formatAssertionsEnabled(true))).isEmpty(), value);
        }
    }

    @ParameterizedTest
    @CsvSource({
            "1979-12-31T23:59:60Z,false",
            "1987-12-31T23:59:60Z,false",
            "1971-12-31T23:59:60Z,true",
            "1988-12-31T23:59:60Z,true"
    })
    void preservesInheritedEthloLeapTableDifferences(String value, boolean ethloAccepts) {
        assertTrue(Classes.isPresent("com.ethlo.time.ITU", DateTimeFormatTest.class.getClassLoader()));
        assertEquals(ethloAccepts, SCHEMA.validate("\"" + value + "\"", InputFormat.JSON,
                ec -> ec.executionConfig(config -> config.formatAssertionsEnabled(true))).isEmpty(), value);
        assertTrue(DateTimeFormat.JavaTimeOffsetDateTime.isValid(value), value);
    }

}
