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

package com.networknt.schema;

import com.fasterxml.jackson.databind.node.DecimalNode;
import org.junit.Test;

import java.math.BigDecimal;

import static com.networknt.schema.TypeFactory.getValueNodeType;
import static org.junit.Assert.assertSame;

public class TypeFactoryTest {

    private static final String[] validIntegralValues = {
            "1", "-1", "0E+1", "0E1", "-0E+1", "-0E1", "10.1E+1", "10.1E1", "-10.1E+1", "-10.1E1", "1E+0", "1E-0",
            "1E0", "1E18", "9223372036854775807", "-9223372036854775808"
    };

    private final SchemaValidatorsConfig schemaValidatorsConfig = new SchemaValidatorsConfig();

    @Test
    public void testValidIntegralValuesWithJavaSemantics() {
        schemaValidatorsConfig.setJavaSemantics(true);
        for (String validValue : validIntegralValues) {
            assertSame(validValue, JsonType.INTEGER,
                    getValueNodeType(DecimalNode.valueOf(new BigDecimal(validValue)), schemaValidatorsConfig));
        }
    }

    @Test
    public void testValidIntegralValuesWithoutJavaSemantics() {
        schemaValidatorsConfig.setJavaSemantics(false);
        for (String validValue : validIntegralValues) {
            assertSame(validValue, JsonType.NUMBER,
                    getValueNodeType(DecimalNode.valueOf(new BigDecimal(validValue)), schemaValidatorsConfig));
        }
    }


    @Test
    public void testWithLosslessNarrowing() {
        schemaValidatorsConfig.setLosslessNarrowing(true);
        for (String validValue : validIntegralValues) {
            assertSame(validValue, JsonType.INTEGER,
                    getValueNodeType(DecimalNode.valueOf(new BigDecimal("1.0")), schemaValidatorsConfig));

            assertSame(validValue, JsonType.NUMBER,
                    getValueNodeType(DecimalNode.valueOf(new BigDecimal("1.5")), schemaValidatorsConfig));
        }
    }

    @Test
    public void testWithoutLosslessNarrowing() {
        schemaValidatorsConfig.setLosslessNarrowing(false);
        for (String validValue : validIntegralValues) {
            assertSame(validValue, JsonType.NUMBER,
                    getValueNodeType(DecimalNode.valueOf(new BigDecimal("1.0")), schemaValidatorsConfig));

            assertSame(validValue, JsonType.NUMBER,
                    getValueNodeType(DecimalNode.valueOf(new BigDecimal("1.5")), schemaValidatorsConfig));
        }

}
