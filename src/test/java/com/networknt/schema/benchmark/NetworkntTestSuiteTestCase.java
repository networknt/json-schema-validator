package com.networknt.schema.benchmark;

import com.networknt.schema.JsonSchema;
import com.networknt.schema.suite.TestCase;

public class NetworkntTestSuiteTestCase {
    private final JsonSchema schema;
    private final TestCase testCase;
    private final Boolean formatAssertionsEnabled;

    public NetworkntTestSuiteTestCase(JsonSchema schema, TestCase testCase, Boolean formatAssertionsEnabled) {
        this.schema = schema;
        this.testCase = testCase;
        this.formatAssertionsEnabled = formatAssertionsEnabled;
    }

    public JsonSchema getSchema() {
        return schema;
    }

    public TestCase getTestCase() {
        return testCase;
    }

    public Boolean getFormatAssertionsEnabled() {
        return formatAssertionsEnabled;
    }
}
