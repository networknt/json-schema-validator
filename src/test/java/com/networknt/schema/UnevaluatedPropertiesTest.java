package com.networknt.schema;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class UnevaluatedPropertiesTest extends BaseSuiteJsonSchemaTest {

    protected UnevaluatedPropertiesTest() {
        super(SpecVersion.VersionFlag.V201909);
    }


    @Test
    public void testUnevaluatedProperties() throws Exception {
        runTestFile("schema/unevaluatedTests/unevaluated-tests.json");
    }
}
