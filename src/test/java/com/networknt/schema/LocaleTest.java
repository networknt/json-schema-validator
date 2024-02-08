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
import java.util.Set;

import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.networknt.schema.SpecVersion.VersionFlag;
import com.networknt.schema.i18n.Locales;
import com.networknt.schema.serialization.JsonMapperFactory;

public class LocaleTest {
    private JsonSchema getSchema(SchemaValidatorsConfig config) {
        JsonSchemaFactory factory = JsonSchemaFactory.getInstance(SpecVersion.VersionFlag.V201909);
        return factory.getSchema(
                "{ \"$schema\": \"https://json-schema.org/draft/2019-09/schema\", \"$id\": \"https://json-schema.org/draft/2019-09/schema\", \"type\": \"object\", \"properties\": { \"foo\": { \"type\": \"string\" } } } }",
                config);
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
        SchemaValidatorsConfig config = new SchemaValidatorsConfig();
        JsonSchema jsonSchema = getSchema(config);

        Locale locale = Locales.findSupported("it;q=0.9,fr;q=1.0"); // fr
        ExecutionContext executionContext = jsonSchema.createExecutionContext();
        assertEquals(config.getLocale(), executionContext.getExecutionConfig().getLocale());
        executionContext.getExecutionConfig().setLocale(locale);
        Set<ValidationMessage> messages = jsonSchema.validate(executionContext, rootNode);
        assertEquals(1, messages.size());
        assertEquals("$.foo: integer trouvé, string attendu", messages.iterator().next().getMessage());

        locale = Locales.findSupported("it;q=1.0,fr;q=0.9"); // it
        executionContext = jsonSchema.createExecutionContext();
        assertEquals(config.getLocale(), executionContext.getExecutionConfig().getLocale());
        executionContext.getExecutionConfig().setLocale(locale);
        messages = jsonSchema.validate(executionContext, rootNode);
        assertEquals(1, messages.size());
        assertEquals("$.foo: integer trovato, string previsto", messages.iterator().next().getMessage());
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
            JsonSchema jsonSchema = JsonSchemaFactory.getInstance(VersionFlag.V7)
                    .getSchema(JsonMapperFactory.getInstance().readTree(schema));
            String input = "1";
            Set<ValidationMessage> messages = jsonSchema.validate(input, InputFormat.JSON);
            assertEquals(1, messages.size());
            assertEquals("$: integer gefunden, object erwartet", messages.iterator().next().toString());
            
            SchemaValidatorsConfig config = new SchemaValidatorsConfig();
            config.setLocale(Locale.ENGLISH);
            jsonSchema = JsonSchemaFactory.getInstance(VersionFlag.V7)
                    .getSchema(JsonMapperFactory.getInstance().readTree(schema), config);
            messages = jsonSchema.validate(input, InputFormat.JSON);
            assertEquals(1, messages.size());
            assertEquals("$: integer found, object expected", messages.iterator().next().toString());
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
        expected.put("ar_EG","$: يجب أن يكون طوله 5 حرفًا على الأكثر");
        expected.put("cs_CZ","$: musí mít maximálně 5 znaků");
        expected.put("da_DK","$: må højst være på 5 tegn");
        expected.put("de","$: darf höchstens 5 Zeichen lang sein");
        expected.put("fa_IR","$: باید حداکثر 5 کاراکتر باشد");
        expected.put("fi_FI","$: saa olla enintään 5 merkkiä pitkä");
        expected.put("fr_CA","$: ne peut contenir que 5 caractères");
        expected.put("fr","$: doit contenir au plus 5 caractères");
        expected.put("iw_IL","$: must be at most 5 characters long");
        expected.put("hr_HR","$: mora imati najviše 5 znakova");
        expected.put("hu_HU","$: legfeljebb 5 karakter hosszúságú lehet");
        expected.put("it","$: deve contenere al massimo 5 caratteri");
        expected.put("ja_JP","$: 長さは最大 5 文字でなければなりません");
        expected.put("ko_KR","$: 길이는 최대 5자여야 합니다.");
        expected.put("nb_NO","$: må bestå av maksimalt 5 tegn");
        expected.put("nl_NL","$: mag maximaal 5 tekens lang zijn");
        expected.put("pl_PL","$: musi mieć maksymalnie 5 znaków");
        expected.put("pt_BR","$: deve ter no máximo 5 caracteres");
        expected.put("ro_RO","$: trebuie să aibă cel mult 5 caractere");
        expected.put("ru_RU","$: длина должна быть не более 5 символов.");
        expected.put("sk_SK","$: musí mať maximálne 5 znakov");
        expected.put("sv_SE","$: får vara högst 5 tecken lång");
        expected.put("th_TH","$: ต้องมีความยาวสูงสุด 5 อักขระ");
        expected.put("tr_TR","$: en fazla 5 karakter uzunluğunda olmalıdır");
        expected.put("uk_UA","$: не більше ніж 5 символів");
        expected.put("vi_VN","$: phải dài tối đa 5 ký tự");
        expected.put("zh_CN","$: 长度不得超过 5 个字符");
        expected.put("zh_TW","$: 長度不得超過 5 個字元");

        String schemaData = "{\r\n"
                + "  \"type\": \"string\",\r\n"
                + "  \"maxLength\": 5\r\n"
                + "}";
        JsonSchema schema = JsonSchemaFactory.getInstance(VersionFlag.V7).getSchema(schemaData);
        List<Locale> locales = Locales.getSupportedLocales();
        for (Locale locale : locales) {
            Set<ValidationMessage> messages = schema.validate("\"aaaaaa\"", InputFormat.JSON, executionContext -> {
                executionContext.getExecutionConfig().setLocale(locale);
            });
            assertEquals(expected.get(locale.toString()), messages.iterator().next().toString());
            //System.out.println(messages.iterator().next().toString());
            //System.out.println("expected.put(\"" +locale.toString() + "\",\"" + messages.iterator().next().toString() + "\");");
            
//            OutputUnit outputUnit = schema.validate("\"aaaaaa\"", InputFormat.JSON, OutputFormat.HIERARCHICAL, executionContext -> {
//                executionContext.getExecutionConfig().setLocale(locale);
//            });
//            System.out.println(outputUnit);

        }
    }
}
