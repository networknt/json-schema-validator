/*
 * Copyright (c) 2016 Network New Technologies Inc.
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

package com.networknt.schema.serialization;

import static org.junit.jupiter.api.Assertions.*;

import java.math.BigDecimal;
import org.junit.jupiter.api.Test;
import tools.jackson.core.JsonParser;
import tools.jackson.core.JsonToken;
import tools.jackson.databind.json.JsonMapper;

class Issue1362Test {
    @Test
    void optimizedAdvancementInvalidatesPrecision() throws Exception {
        try (JsonParser parser = SchemaNodeReader.precisionParser(JsonMapper.builder().build()
                .createParser("[0.1234567890123456789,123.25,0.1234567890123456789,7,0.1234567890123456789,8,0.1234567890123456789,true,0.1234567890123456789,{},123.25]"), false)) {
            assertEquals(JsonToken.START_ARRAY, parser.nextToken());
            parser.nextToken();
            exact(parser);
            parser.nextStringValue();
            lossless(parser);
            parser.nextValue();
            exact(parser);
            assertEquals(7, parser.nextIntValue(-1));
            parser.nextValue();
            exact(parser);
            assertEquals(8, parser.nextLongValue(-1));
            parser.nextToken();
            exact(parser);
            assertEquals(Boolean.TRUE, parser.nextBooleanValue());
            parser.nextToken();
            exact(parser);
            assertEquals(JsonToken.START_OBJECT, parser.nextToken());
            assertSame(parser, parser.skipChildren());
            parser.nextToken();
            lossless(parser);
        }
    }

    @Test
    void optimizedNamesInvalidatePrecision() throws Exception {
        try (JsonParser parser = SchemaNodeReader.precisionParser(JsonMapper.builder().build()
                .createParser("{\"a\":0.1234567890123456789,\"b\":123.25,\"c\":0.1234567890123456789}"), false)) {
            parser.nextToken();
            assertEquals("a", parser.nextName());
            parser.nextToken();
            exact(parser);
            assertTrue(parser.nextName(new tools.jackson.core.io.SerializedString("b")));
            parser.nextToken();
            lossless(parser);
            assertEquals("c", parser.nextName());
            parser.nextToken();
            exact(parser);
        }
    }

    private static void exact(JsonParser parser) throws Exception {
        assertEquals(JsonParser.NumberTypeFP.BIG_DECIMAL, parser.getNumberTypeFP());
        assertEquals(JsonParser.NumberType.BIG_DECIMAL, parser.getNumberType());
        assertEquals(new BigDecimal("0.1234567890123456789"), parser.getDecimalValue());
    }

    private static void lossless(JsonParser parser) throws Exception {
        assertNotEquals(JsonParser.NumberTypeFP.BIG_DECIMAL, parser.getNumberTypeFP());
        assertEquals(JsonParser.NumberType.DOUBLE, parser.getNumberType());
        assertEquals(123.25, parser.getDoubleValue());
    }
}
