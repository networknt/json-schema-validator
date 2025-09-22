package com.networknt.schema;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.List;
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

    private static final SchemaRegistry factory = SchemaRegistry.withDefaultDialect(Specification.Version.DRAFT_2019_09);
    private static final Schema schema = factory.getSchema(SCHEMA);
    private static final ObjectMapper mapper = new ObjectMapper();

    @Test
    void shouldReturnNoErrorMessagesForObjectWithoutOptionalField() throws IOException {

        List<Error> messages = whenValidate("{}");

        assertThat(messages, empty());
    }

    @Test
    void shouldReturnErrorMessageForObjectWithoutDependentRequiredField() throws IOException {

        List<Error> messages = whenValidate("{ \"optional\": \"present\" }");

        assertThat(
            messages.stream().map(Error::toString).collect(Collectors.toList()),
            contains("$: has a missing property 'requiredWhenOptionalPresent' which is dependent required because 'optional' is present"));
    }

    @Test
    void shouldReturnNoErrorMessagesForObjectWithOptionalAndDependentRequiredFieldSet() throws JsonProcessingException {

        List<Error> messages =
            whenValidate("{ \"optional\": \"present\", \"requiredWhenOptionalPresent\": \"present\" }");

        assertThat(messages, empty());
    }

    private static List<Error> whenValidate(String content) throws JsonProcessingException {
        return schema.validate(mapper.readTree(content));
    }

}