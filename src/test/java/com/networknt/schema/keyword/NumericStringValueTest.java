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

package com.networknt.schema.keyword;

import static org.junit.jupiter.api.Assertions.*;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Random;
import java.util.regex.Pattern;
import org.junit.jupiter.api.Test;

class NumericStringValueTest {
    @Test
    void decimalComparisonAgreesWithBigDecimalAcrossSpellings() {
        Random random = new Random(1349);
        int count = 1000;
        BigDecimal[] numbers = new BigDecimal[count];
        NumericStringValue[] keys = new NumericStringValue[count];
        for (int i = 0; i < count; i++) {
            numbers[i] = new BigDecimal(BigInteger.valueOf(random.nextLong()), random.nextInt(801) - 400);
            String text = numbers[i].toEngineeringString();
            keys[i] = NumericStringValue.parse(text);
            assertEquals(NumericStringValue.of(numbers[i]), keys[i], text);
            assertEquals(NumericStringValue.of(numbers[i]).hashCode(), keys[i].hashCode(), text);
            BigDecimal rescaled = numbers[i].setScale(numbers[i].scale() + 3);
            assertEquals(keys[i], NumericStringValue.parse(rescaled.toString()));
        }
        for (int i = 0; i < count; i++) {
            for (int j = 0; j < count; j++) {
                assertEquals(numbers[i].compareTo(numbers[j]) == 0, keys[i].equals(keys[j]));
            }
        }
    }

    @Test
    void acceptedGrammarAgreesWithJsonNumberSyntax() {
        Pattern grammar = Pattern.compile("-?(0|[1-9][0-9]*)(\\.[0-9]+)?([eE][+-]?[0-9]+)?");
        Random random = new Random(1349);
        String alphabet = "00123456789eE+-. x_";
        for (int i = 0; i < 20000; i++) {
            StringBuilder text = new StringBuilder();
            for (int j = random.nextInt(20); j > 0; j--) {
                text.append(alphabet.charAt(random.nextInt(alphabet.length())));
            }
            String value = text.toString();
            assertEquals(grammar.matcher(value).matches(), NumericStringValue.parse(value) != null, value);
        }
    }
}
