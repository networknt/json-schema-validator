package com.networknt.schema;

import com.fasterxml.jackson.databind.JsonNode;
import org.junit.Test;

/**
 * Created by stevehu on 2016-12-20.
 */
public class SelfRefTest extends BaseJsonSchemaValidatorTest {
    @Test
    public void testSelfRef() throws Exception {
        JsonNode node = getJsonNodeFromClasspath("selfref.json");
        System.out.println("node = " + node);
    }
}
