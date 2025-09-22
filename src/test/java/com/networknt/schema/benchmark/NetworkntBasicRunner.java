package com.networknt.schema.benchmark;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import com.networknt.schema.Schema;
import com.networknt.schema.SchemaRegistry;
import com.networknt.schema.Specification.Version;

/**
 * Basic Benchmark.
 */
public class NetworkntBasicRunner implements Callable<Object> {
    private static final Logger logger = LoggerFactory.getLogger(NetworkntBasicRunner.class);
    private Schema jsonSchema;
    private JsonNode schemas;
    private List<String> schemaNames;

    public NetworkntBasicRunner() {
        ObjectMapper objectMapper = new ObjectMapper();
        SchemaRegistry factory = SchemaRegistry.getInstance(Version.DRAFT_4);
        try {
            ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
            ObjectReader reader = objectMapper.reader();
            JsonNode schemaNode = reader.readTree(classLoader.getResourceAsStream("benchmark/basic/schema-draft4.json"));
            jsonSchema = factory.getSchema(schemaNode);

            JsonNode root = reader.readTree(classLoader.getResourceAsStream("benchmark/basic/perftest.json"));
            schemas = root.get("schemas");

            List<String> names = new ArrayList<>();
            schemas.fieldNames().forEachRemaining(names::add);
            schemaNames = names;
        } catch (IOException e) {
            logger.error("Failed to initialize NetworkntBasicRunner", e);
        }
    }

    @Override
    public Object call() {
        List<Object> results = new ArrayList<>();
        for (String name : schemaNames) {
            JsonNode json = schemas.get(name);
            results.add(jsonSchema.validate(json));
        }
        return results;
    }
}
