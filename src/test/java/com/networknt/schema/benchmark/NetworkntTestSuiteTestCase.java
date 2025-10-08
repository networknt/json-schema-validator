package com.networknt.schema.benchmark;

import com.networknt.schema.Schema;
import com.networknt.schema.suite.TestCase;

public class NetworkntTestSuiteTestCase {
    private final Schema schema;
    private final TestCase testCase;
    private final Boolean formatAssertionsEnabled;

    public NetworkntTestSuiteTestCase(Schema schema, TestCase testCase, Boolean formatAssertionsEnabled) {
        this.schema = schema;
        this.testCase = testCase;
        this.formatAssertionsEnabled = formatAssertionsEnabled;
    }

    public Schema getSchema() {
        return schema;
    }

    public TestCase getTestCase() {
        return testCase;
    }

    public Boolean getFormatAssertionsEnabled() {
        return formatAssertionsEnabled;
    }
}
