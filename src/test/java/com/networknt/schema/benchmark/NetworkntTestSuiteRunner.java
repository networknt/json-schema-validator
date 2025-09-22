package com.networknt.schema.benchmark;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;

import com.networknt.schema.OutputFormat;
import com.networknt.schema.suite.TestSpec;

/**
 * Test Suite Test Case Benchmark.
 */
public class NetworkntTestSuiteRunner implements Callable<Object> {
    private final List<NetworkntTestSuiteTestCase> testCases;

    public NetworkntTestSuiteRunner(List<NetworkntTestSuiteTestCase> testCases) {
        this.testCases = testCases;
    }

    @Override
    public Object call() {
        List<Object> results = new ArrayList<>();
        for (NetworkntTestSuiteTestCase testCase : testCases) {
			for (TestSpec testSpec : testCase.getTestCase().getTests()) {
				results.add(
						testCase.getSchema().validate(testSpec.getData(), OutputFormat.DEFAULT, executionContext -> {
							if (testCase.getFormatAssertionsEnabled() != null) {
								executionContext.executionConfig(executionConfig -> executionConfig
										.formatAssertionsEnabled(testCase.getFormatAssertionsEnabled()));
							}
						}));
			}
        }
        return results;
    }
}
