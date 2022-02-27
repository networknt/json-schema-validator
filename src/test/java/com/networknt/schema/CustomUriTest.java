package com.networknt.schema;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.networknt.schema.JsonSchema;
import com.networknt.schema.JsonSchemaFactory;
import com.networknt.schema.SpecVersion;
import com.networknt.schema.ValidationMessage;
import com.networknt.schema.uri.URIFactory;
import com.networknt.schema.uri.URIFetcher;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.Set;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

public class CustomUriTest {
    @Test
    public void customUri() throws Exception {
        /* Given */
        final JsonSchemaFactory factory = buildJsonSchemaFactory();
        final JsonSchema schema = factory.getSchema(
                "{\"$schema\": \"https://json-schema.org/draft/2019-09/schema\",\"type\": \"object\",\"additionalProperties\": false,\"properties\": {\"customAnyOf\": {\"anyOf\": [{\"type\": \"null\"},{\"$ref\": \"custom:date\"}]},\"customOneOf\": {\"oneOf\": [{\"type\": \"null\"},{\"$ref\": \"custom:date\"}]}}}");
        final ObjectMapper mapper = new ObjectMapper();
        final JsonNode value = mapper.readTree("{\"customAnyOf\": null,\"customOneOf\": null}");

        /* When */
        final Set<ValidationMessage> errors = schema.validate(value);

        /* Then */
        assertThat(errors.isEmpty(), is(true));
    }

    private JsonSchemaFactory buildJsonSchemaFactory() {
        return JsonSchemaFactory.builder(JsonSchemaFactory.getInstance(SpecVersion.VersionFlag.V201909))
                .uriFetcher(new CustomUriFetcher(), "custom").uriFactory(new CustomUriFactory(), "custom").build();
    }

    private static class CustomUriFetcher implements URIFetcher {
        private static final String SCHEMA = "{\"$schema\": \"https://json-schema.org/draft/2019-09/schema\",\"$id\":\"custom:date\",\"type\":\"string\",\"format\":\"date\"}";

        @Override
        public InputStream fetch(final URI uri) throws IOException {
            return new ByteArrayInputStream(SCHEMA.getBytes(StandardCharsets.UTF_8));
        }
    }

    private static class CustomUriFactory implements URIFactory {
        @Override
        public URI create(final String uri) {
            return URI.create(uri);
        }

        @Override
        public URI create(final URI baseURI, final String segment) {
            return baseURI.resolve(segment);
        }
    }
}
