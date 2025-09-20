window.BENCHMARK_DATA = {
  "lastUpdate": 1758381708565,
  "repoUrl": "https://github.com/networknt/json-schema-validator",
  "entries": {
    "JSON Schema Validator Benchmark": [
      {
        "commit": {
          "author": {
            "email": "49700559+justin-tay@users.noreply.github.com",
            "name": "Justin Tay",
            "username": "justin-tay"
          },
          "committer": {
            "email": "noreply@github.com",
            "name": "GitHub",
            "username": "web-flow"
          },
          "distinct": true,
          "id": "db7178048d124e908effc2b42b52f07d894f05ee",
          "message": "Add benchmark for performance regression testing (#1195)",
          "timestamp": "2025-09-20T11:15:24-04:00",
          "tree_id": "1fafdf13bac312107c25a6c6163be5b5cfbdf0c4",
          "url": "https://github.com/networknt/json-schema-validator/commit/db7178048d124e908effc2b42b52f07d894f05ee"
        },
        "date": 1758381708186,
        "tool": "jmh",
        "benches": [
          {
            "name": "com.networknt.schema.benchmark.NetworkntBenchmark.basic",
            "value": 8126.562931014215,
            "unit": "ops/s",
            "extra": "iterations: 3\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.networknt.schema.benchmark.NetworkntTestSuiteOptionalBenchmark.testsuite ( {\"specification\":\"2020-12\"} )",
            "value": 1295.2228235989185,
            "unit": "ops/s",
            "extra": "iterations: 3\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.networknt.schema.benchmark.NetworkntTestSuiteOptionalBenchmark.testsuite ( {\"specification\":\"2019-09\"} )",
            "value": 1342.2402784701956,
            "unit": "ops/s",
            "extra": "iterations: 3\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.networknt.schema.benchmark.NetworkntTestSuiteOptionalBenchmark.testsuite ( {\"specification\":\"7\"} )",
            "value": 1391.7815116590875,
            "unit": "ops/s",
            "extra": "iterations: 3\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.networknt.schema.benchmark.NetworkntTestSuiteOptionalBenchmark.testsuite ( {\"specification\":\"6\"} )",
            "value": 3181.039574918374,
            "unit": "ops/s",
            "extra": "iterations: 3\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.networknt.schema.benchmark.NetworkntTestSuiteOptionalBenchmark.testsuite ( {\"specification\":\"4\"} )",
            "value": 3988.437126077213,
            "unit": "ops/s",
            "extra": "iterations: 3\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.networknt.schema.benchmark.NetworkntTestSuiteRequiredBenchmark.testsuite ( {\"specification\":\"2020-12\"} )",
            "value": 1226.6073630697217,
            "unit": "ops/s",
            "extra": "iterations: 3\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.networknt.schema.benchmark.NetworkntTestSuiteRequiredBenchmark.testsuite ( {\"specification\":\"2019-09\"} )",
            "value": 1286.660195980455,
            "unit": "ops/s",
            "extra": "iterations: 3\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.networknt.schema.benchmark.NetworkntTestSuiteRequiredBenchmark.testsuite ( {\"specification\":\"7\"} )",
            "value": 3258.7442390960823,
            "unit": "ops/s",
            "extra": "iterations: 3\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.networknt.schema.benchmark.NetworkntTestSuiteRequiredBenchmark.testsuite ( {\"specification\":\"6\"} )",
            "value": 3542.6968313343946,
            "unit": "ops/s",
            "extra": "iterations: 3\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.networknt.schema.benchmark.NetworkntTestSuiteRequiredBenchmark.testsuite ( {\"specification\":\"4\"} )",
            "value": 4973.350553720758,
            "unit": "ops/s",
            "extra": "iterations: 3\nforks: 1\nthreads: 1"
          }
        ]
      }
    ]
  }
}