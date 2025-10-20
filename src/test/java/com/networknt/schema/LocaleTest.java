/*
 * Copyright (c) 2023 the original author or authors.
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

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.networknt.schema.i18n.Locales;
import com.networknt.schema.serialization.JsonMapperFactory;

class LocaleTest {
    private Schema getSchema(SchemaRegistryConfig config) {
        SchemaRegistry factory = SchemaRegistry.withDefaultDialect(SpecificationVersion.DRAFT_2019_09, builder -> builder.schemaRegistryConfig(config));
        return factory.getSchema(
                "{ \"$schema\": \"https://json-schema.org/draft/2019-09/schema\", \"$id\": \"https://json-schema.org/draft/2019-09/schema\", \"type\": \"object\", \"properties\": { \"foo\": { \"type\": \"string\" } } } }"
                );
    }

    /**
     * Tests that the validation messages are generated based on the execution
     * context locale.
     * 
     * @throws JsonMappingException    the error
     * @throws JsonProcessingException the error
     */
    @Test
    void executionContextLocale() throws JsonMappingException, JsonProcessingException {
        JsonNode rootNode = new ObjectMapper().readTree(" { \"foo\": 123 } ");
        SchemaRegistryConfig config = SchemaRegistryConfig.builder().build();
        Schema jsonSchema = getSchema(config);

        List<Error> messages = jsonSchema.validate(rootNode, executionContext -> {
            Locale locale = Locales.findSupported("it;q=0.9,fr;q=1.0"); // fr
            executionContext.executionConfig(executionConfig -> executionConfig.locale(locale));
        });
        assertEquals(1, messages.size());
        assertEquals("/foo: integer trouvé, string attendu", messages.iterator().next().toString());
        
        messages = jsonSchema.validate(rootNode, executionContext -> {
            Locale locale = Locales.findSupported("it;q=1.0,fr;q=0.9"); // it
            executionContext.executionConfig(executionConfig -> executionConfig.locale(locale));
        });
        assertEquals(1, messages.size());
        assertEquals("/foo: integer trovato, string previsto", messages.iterator().next().toString());
    }

    /**
     * Issue 949.
     * <p>
     * Locale.ENGLISH should work despite Locale.getDefault setting.
     * 
     * @throws JsonMappingException the exception
     * @throws JsonProcessingException the exception
     */
    @Test
    void englishLocale() throws JsonMappingException, JsonProcessingException {
        Locale locale = Locale.getDefault();
        try {
            Locale.setDefault(Locale.GERMAN);
            String schema = "{\r\n"
                    + "  \"$schema\": \"http://json-schema.org/draft-07/schema#\",\r\n"
                    + "  \"$id\": \"https://www.example.com\",\r\n"
                    + "  \"type\": \"object\"\r\n"
                    + "}";
            Schema jsonSchema = SchemaRegistry.withDefaultDialect(SpecificationVersion.DRAFT_7)
                    .getSchema(JsonMapperFactory.getInstance().readTree(schema));
            String input = "1";
            List<Error> messages = jsonSchema.validate(input, InputFormat.JSON);
            assertEquals(1, messages.size());
            assertEquals(": integer gefunden, object erwartet", messages.iterator().next().toString());
            
            SchemaRegistryConfig config = SchemaRegistryConfig.builder().locale(Locale.ENGLISH).build();
            jsonSchema = SchemaRegistry.withDefaultDialect(SpecificationVersion.DRAFT_7, builder -> builder.schemaRegistryConfig(config))
                    .getSchema(JsonMapperFactory.getInstance().readTree(schema));
            messages = jsonSchema.validate(input, InputFormat.JSON);
            assertEquals(1, messages.size());
            assertEquals(": integer found, object expected", messages.iterator().next().toString());
        } finally {
            Locale.setDefault(locale);
        }
    }

    /**
     * Tests that the file encoding for the locale files are okay.
     * <p>
     * Java 8 does not support UTF-8 encoded resource bundles. That is only
     * supported in Java 9 and above.
     */
    @Test
    void encoding() {
        Map<String, String> expected = new HashMap<>();
        expected.put("ar",": يجب أن يكون طوله 5 حرفًا على الأكثر");
        expected.put("cs",": musí mít maximálně 5 znaků");
        expected.put("da",": må højst være på 5 tegn");
        expected.put("de",": darf höchstens 5 Zeichen lang sein");
        expected.put("es",": debe tener como máximo 5 caracteres");
        expected.put("fa",": باید حداکثر 5 کاراکتر باشد");
        expected.put("fi",": saa olla enintään 5 merkkiä pitkä");
        expected.put("fr",": doit contenir au plus 5 caractères");
        expected.put("iw",": חייב להיות באורך של 5 תווים לכל היותר");
        expected.put("he",": חייב להיות באורך של 5 תווים לכל היותר");
        expected.put("hr",": mora imati najviše 5 znakova");
        expected.put("hu",": legfeljebb 5 karakter hosszúságú lehet");
        expected.put("it",": deve contenere al massimo 5 caratteri");
        expected.put("ja",": 長さは最大 5 文字でなければなりません");
        expected.put("ko",": 길이는 최대 5자여야 합니다.");
        expected.put("nb",": må bestå av maksimalt 5 tegn");
        expected.put("nl",": mag maximaal 5 tekens lang zijn");
        expected.put("pl",": musi mieć maksymalnie 5 znaków");
        expected.put("pt",": deve ter no máximo 5 caracteres");
        expected.put("ro",": trebuie să aibă cel mult 5 caractere");
        expected.put("ru",": длина должна быть не более 5 символов.");
        expected.put("sk",": musí mať maximálne 5 znakov");
        expected.put("sv",": får vara högst 5 tecken lång");
        expected.put("th",": ต้องมีความยาวสูงสุด 5 อักขระ");
        expected.put("tr",": en fazla 5 karakter uzunluğunda olmalıdır");
        expected.put("uk",": не більше ніж 5 символів");
        expected.put("vi",": phải dài tối đa 5 ký tự");
        expected.put("zh_CN",": 长度不得超过 5 个字符");
        expected.put("zh_TW",": 長度不得超過 5 個字元");

        // In later JDK versions the numbers will be formatted
        Map<String, String> expectedAlternate = new HashMap<>();
        expectedAlternate.put("ar",": يجب أن يكون طوله ٥ حرفًا على الأكثر");
        expectedAlternate.put("fa",": باید حداکثر ۵ کاراکتر باشد");

        String schemaData = "{\r\n"
                + "  \"type\": \"string\",\r\n"
                + "  \"maxLength\": 5\r\n"
                + "}";
        Schema schema = SchemaRegistry.withDefaultDialect(SpecificationVersion.DRAFT_7).getSchema(schemaData);
        List<Locale> locales = Locales.getSupportedLocales();
        for (Locale locale : locales) {
            List<Error> messages = schema.validate("\"aaaaaa\"", InputFormat.JSON, executionContext -> {
                executionContext.executionConfig(executionConfig -> executionConfig.locale(locale));
            });
            String msg = messages.iterator().next().toString();
            String expectedMsg = expected.get(locale.toString());
            String expectedMsgAlternate = expectedAlternate.get(locale.toString());
            if (msg.equals(expectedMsg) || msg.equals(expectedMsgAlternate)) {
                continue;
            }
            if ("iw".equals(locale.toString()) || "he".equals(locale.toString())) {
                // There are changes in the iso codes across JDK versions that make this
                // troublesome to handle
                continue;
            }
            assertEquals(expectedMsg, msg);
//            System.out.println(messages.iterator().next().toString());
//            System.out.println("expected.put(\"" +locale.toString() + "\",\"" + messages.iterator().next().toString() + "\");");
            
//            OutputUnit outputUnit = schema.validate("\"aaaaaa\"", InputFormat.JSON, OutputFormat.HIERARCHICAL, executionContext -> {
//                executionContext.getExecutionConfig().setLocale(locale);
//            });
//            System.out.println(outputUnit);

        }
    }
}
