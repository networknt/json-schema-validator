package com.networknt.schema.benchmark;

import java.util.concurrent.Callable;

import com.networknt.schema.Specification.Version;

public class NetworkntTestSuite202012RequiredPerf {
    public static void main(String[] args) throws Exception {
        Callable<Object> runner = new NetworkntTestSuiteRunner(NetworkntTestSuiteTestCases.findTestCases(
                Version.DRAFT_2020_12, "src/test/suite/tests/draft2020-12", TestCaseFilter.requiredType()));
        runner.call();
    }
}
