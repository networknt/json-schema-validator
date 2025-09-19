package com.networknt.schema.benchmark;

import java.util.concurrent.Callable;

import com.networknt.schema.SpecVersion.VersionFlag;

public class NetworkntTestSuite202012OptionalPerf {
    public static void main(String[] args) throws Exception {
        Callable<Object> runner = new NetworkntTestSuiteRunner(NetworkntTestSuiteTestCases.findTestCases(
                VersionFlag.V202012, "src/test/suite/tests/draft2020-12", TestCaseFilter.optionalType()));
        runner.call();
    }
}
