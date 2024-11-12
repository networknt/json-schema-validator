package com.networknt.schema;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.Set;
import java.util.stream.Collectors;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.empty;

class DependentRequiredTest {

    static final String SCHEMA =
        "{ " +
            "   \"$schema\":\"https://json-schema.org/draft/2019-09/schema\"," +
            "   \"type\": \"object\"," +
            "   \"properties\": {" +
            "       \"optional\": \"string\"," +
            "       \"requiredWhenOptionalPresent\": \"string\"" +
            "   }," +
            "   \"dependentRequired\": {" +
            "       \"optional\": [ \"requiredWhenOptionalPresent\" ]," +
            "       \"otherOptional\": [ \"otherDependentRequired\" ]" +
            "   }" +
            "}";

    private static final JsonSchemaFactory factory = JsonSchemaFactory.getInstance(SpecVersion.VersionFlag.V201909);
    private static final JsonSchema schema = factory.getSchema(SCHEMA);
    private static final ObjectMapper mapper = new ObjectMapper();

    @Test
    void shouldReturnNoErrorMessagesForObjectWithoutOptionalField() throws IOException {

        Set<ValidationMessage> messages = whenValidate("{}");

        assertThat(messages, empty());
    }

    @Test
    void shouldReturnErrorMessageForObjectWithoutDependentRequiredField() throws IOException {

        Set<ValidationMessage> messages = whenValidate("{ \"optional\": \"present\" }");

        assertThat(
            messages.stream().map(ValidationMessage::getMessage).collect(Collectors.toList()),
            contains("$: has a missing property 'requiredWhenOptionalPresent' which is dependent required because 'optional' is present"));
    }

    @Test
    void shouldReturnNoErrorMessagesForObjectWithOptionalAndDependentRequiredFieldSet() throws JsonProcessingException {

        Set<ValidationMessage> messages =
            whenValidate("{ \"optional\": \"present\", \"requiredWhenOptionalPresent\": \"present\" }");

        assertThat(messages, empty());
    }

    private static Set<ValidationMessage> whenValidate(String content) throws JsonProcessingException {
        return schema.validate(mapper.readTree(content));
    }

}