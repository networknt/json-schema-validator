package com.networknt.schema;

import org.junit.jupiter.api.Test;

public class CustomMessageTest extends BaseSuiteJsonSchemaTest {

    protected CustomMessageTest() {
        super(SpecVersion.VersionFlag.V201909);
    }

    @Test
    public void testCustomMessages() throws Exception {
        runTestFile("schema/customMessageTests/custom-message-tests.json");
    }
}
