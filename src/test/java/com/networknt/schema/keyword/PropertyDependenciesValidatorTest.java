package com.networknt.schema.keyword;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;

import org.junit.jupiter.api.Test;

import com.networknt.schema.Error;
import com.networknt.schema.InputFormat;
import com.networknt.schema.Schema;
import com.networknt.schema.SchemaRegistry;
import com.networknt.schema.dialect.Dialect;
import com.networknt.schema.dialect.Dialects;

/**
 * Test for propertyDependencies.
 */
public class PropertyDependenciesValidatorTest {
    @Test
    void evaluationPath() {
        Dialect dialect = Dialect.builder(Dialects.getDraft202012()).keyword(KeywordType.PROPERTY_DEPENDENCIES).build();
        SchemaRegistry schemaRegistry = SchemaRegistry.withDialect(dialect);
        String schemaData = "{\r\n"
                + "  \"propertyDependencies\": {\r\n"
                + "    \"foo\": {\r\n"
                + "      \"aaa\": {\r\n"
                + "        \"$ref\": \"#/$defs/foo-aaa\"\r\n"
                + "      }\r\n"
                + "    }\r\n"
                + "  },\r\n"
                + "  \"$defs\": {\r\n"
                + "    \"foo-aaa\": {\r\n"
                + "      \"type\": \"object\",\r\n"
                + "      \"properties\": {\r\n"
                + "        \"foo\": {\r\n"
                + "          \"type\": \"string\"\r\n"
                + "        },\r\n"
                + "        \"bar\": {\r\n"
                + "          \"type\": \"string\"\r\n"
                + "        }\r\n"
                + "      }\r\n"
                + "    }\r\n"
                + "  }\r\n"
                + "}";
        String instanceData = "{\r\n"
                + "  \"foo\": \"aaa\",\r\n"
                + "  \"bar\": 1\r\n"
                + "}";
        Schema schema = schemaRegistry.getSchema(schemaData, InputFormat.JSON);
        List<Error> errors = schema.validate(instanceData, InputFormat.JSON);
        assertEquals(1, errors.size());
        assertEquals("/propertyDependencies/foo/aaa/$ref/properties/bar/type", errors.get(0).getEvaluationPath().toString());
        assertEquals("#/$defs/foo-aaa/properties/bar/type", errors.get(0).getSchemaLocation().toString());
        assertEquals("type", errors.get(0).getKeyword());
    }

}
