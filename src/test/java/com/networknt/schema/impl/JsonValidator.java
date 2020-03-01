package com.networknt.schema.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.networknt.schema.JsonSchema;
import com.networknt.schema.JsonSchemaFactory;
import com.networknt.schema.SpecVersion;
import com.networknt.schema.ValidationMessage;
import com.networknt.schema.uri.URIFactory;
import com.networknt.schema.uri.URIFetcher;

import javax.xml.bind.ValidationException;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.URI;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Iterator;
import java.util.Set;

public class JsonValidator {

    public enum Version {
        V4,
        V6,
        V7,
        V2019_09
    }

    public static final Version DEFAULT_VERSION = Version.V4;


    private static final String URISCHEME = "test";

    private final JsonSchemaFactory factory;
    private final ObjectMapper mapper;

    public JsonValidator() {
        this(false);
    }

    public JsonValidator(boolean includeWarnings) {
        this(Version.V4);
    }

    public JsonValidator(Version version) {

        final URIFactory factory = new URIFactory() {
            @Override
            public URI create(String uri) {
                return URI.create(uri);
            }

            @Override
            public URI create(URI baseURI, String segment) {
                return URI.create(baseURI.getScheme() + ":" + segment);
            }
        };

        final URIFetcher fetcher = source -> {
            String typeIdentifier = source.getPath();
            Schema schema = getSchema(typeIdentifier);
            return new ByteArrayInputStream(schema.schema.getBytes("UTF-8"));
        };

        if (version == null) {
            version = DEFAULT_VERSION;
        }

        SpecVersion.VersionFlag v;
        switch (version) {
            case V4:
                v = SpecVersion.VersionFlag.V4;
                break;
            case V6:
                v = SpecVersion.VersionFlag.V6;
                break;
            case V7:
                v = SpecVersion.VersionFlag.V7;
                break;
            case V2019_09:
                v = SpecVersion.VersionFlag.V201909;
                break;
            default:
                throw new IllegalArgumentException("Unsupported schema version: " + version);
        }

        this.factory = JsonSchemaFactory.builder(JsonSchemaFactory.getInstance(v))
                .uriFactory(factory, URISCHEME)
                .uriFetcher(fetcher, URISCHEME)
                .build();

        this.mapper = new ObjectMapper();
    }

    public boolean validate(String json, String type) throws ValidationException {
        Schema schema = getSchema(type);

        try {
            JsonSchema jsonSchema = factory.getSchema(new URI(URISCHEME + ":" + type), schema.node);
            JsonNode jsonInstanceNode = this.mapper.readTree(json);
            Set<ValidationMessage> errors = jsonSchema.validate(jsonInstanceNode);
            if (!errors.isEmpty()) {
                throw new ValidationException(createValidationErrorMessage(type, errors));
            }
            return true;
        } catch (Exception e) {
            throw new ValidationException("Unexpected error during validation", e);
        }
    }

    private String createValidationErrorMessage(String typeIdentifier, Set<ValidationMessage> validationErrors) {
        Iterator<ValidationMessage> iterator = validationErrors.iterator();
        StringBuilder validationMessage = new StringBuilder("Invalid json. Could not validate type \"")
                .append(typeIdentifier)
                .append("\":\n");
        while (iterator.hasNext()) {
            validationMessage.append(iterator.next().getMessage());
        }
        return validationMessage.toString();
    }

    private Schema getSchema(String typeLocation) {
        String schema = loadSchemaFileContent(typeLocation);

        try {

            JsonNode schemaNode = this.mapper.readTree(schema);
            return new Schema(schema, schemaNode);
        } catch (IOException exn) {
            throw new RuntimeException("Schema " + typeLocation + " could not be parsed: " + exn);
        }
    }

    private String loadSchemaFileContent(String typeLocation) {
        String fileName = typeLocation.substring(1) + ".json";
        try {
            return new String(Files.readAllBytes(Paths.get(this.getClass().getClassLoader().getResource(fileName).getPath())), Charset.defaultCharset());
        } catch (IOException e) {
            throw new RuntimeException("Invalid file: " + fileName, e);
        }
    }


    private static class Schema {
        private final String schema;
        private final JsonNode node;

        public Schema(String schema, JsonNode node) {
            this.schema = schema;
            this.node = node;
        }
    }
}
