window.BENCHMARK_DATA = {
  "lastUpdate": 1792790309649,
  "repoUrl": "https://github.com/networknt/json-schema-validator",
  "entries": {
    "JSON Schema Validator Benchmark": [
      {
        "commit": {
          "author": {
            "email": "el.psy.kongroo.d@gmail.com",
            "name": "el-psy-kongroo-d",
            "username": "el-psy-kongroo-d"
          },
          "committer": {
            "email": "noreply@github.com",
            "name": "GitHub",
            "username": "web-flow"
          },
          "distinct": true,
          "id": "73eb8f2a0553c7c5ba4269940dbf311e1b82e90d",
          "message": "Reject non-ASCII whitespace in idn-email and strengthen email format tests (#1268)\n\n* Reject non-ASCII whitespace in idn-email and strengthen email format tests\n\nFollow-up to #1267 applying the review suggestions:\n\n- Extract the non-ASCII-whitespace check to Strings.containsNonAsciiWhitespace\n  and reuse it from both EmailFormat and IdnEmailFormat.\n- idn-email shares EmailFormat's delegation and had the same bug (a leading\n  U+00A0 was accepted); it now rejects non-ASCII whitespace too. Adds a license\n  header to IdnEmailFormat (it had none) and a new IdnEmailFormatTest that also\n  verifies a non-ASCII letter is still accepted.\n- Strengthen EmailFormatTest: assert a quoted local part with an ASCII space\n  (\"joe bloggs\"@example.com) stays valid, and generalize the negative case over\n  U+00A0, U+2003 and U+3000.\n\nThe change stays deliberately narrow (whitespace only); non-ASCII letters are\nstill accepted in idn-email. Zero-width format characters (e.g. U+200B, U+FEFF)\nare out of scope and unchanged.\n\n* Generalize idn-email whitespace test over U+2003 and U+3000\n\n---------\n\nCo-authored-by: el-psy-kongroo-d <307969302+el-psy-kongroo-d@users.noreply.github.com>",
          "timestamp": "2026-07-28T09:26:04-04:00",
          "tree_id": "22e52f27a7bc7ae84cb57da3864ce88511dcca1f",
          "url": "https://github.com/networknt/json-schema-validator/commit/73eb8f2a0553c7c5ba4269940dbf311e1b82e90d"
        },
        "date": 1785245546456,
        "tool": "jmh",
        "benches": [
          {
            "name": "com.networknt.schema.benchmark.NetworkntBenchmark.basic",
            "value": 4469.881494379617,
            "unit": "ops/s",
            "extra": "iterations: 3\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.networknt.schema.benchmark.NetworkntTestSuiteOptionalBenchmark.testsuite ( {\"specification\":\"2020-12\"} )",
            "value": 1242.557571727064,
            "unit": "ops/s",
            "extra": "iterations: 3\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.networknt.schema.benchmark.NetworkntTestSuiteOptionalBenchmark.testsuite ( {\"specification\":\"2019-09\"} )",
            "value": 1307.937920020741,
            "unit": "ops/s",
            "extra": "iterations: 3\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.networknt.schema.benchmark.NetworkntTestSuiteOptionalBenchmark.testsuite ( {\"specification\":\"7\"} )",
            "value": 1395.000244864529,
            "unit": "ops/s",
            "extra": "iterations: 3\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.networknt.schema.benchmark.NetworkntTestSuiteOptionalBenchmark.testsuite ( {\"specification\":\"6\"} )",
            "value": 3152.0968412491943,
            "unit": "ops/s",
            "extra": "iterations: 3\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.networknt.schema.benchmark.NetworkntTestSuiteOptionalBenchmark.testsuite ( {\"specification\":\"4\"} )",
            "value": 3898.81248406867,
            "unit": "ops/s",
            "extra": "iterations: 3\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.networknt.schema.benchmark.NetworkntTestSuiteRequiredBenchmark.testsuite ( {\"specification\":\"2020-12\"} )",
            "value": 1072.2750730820228,
            "unit": "ops/s",
            "extra": "iterations: 3\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.networknt.schema.benchmark.NetworkntTestSuiteRequiredBenchmark.testsuite ( {\"specification\":\"2019-09\"} )",
            "value": 1165.4181624760886,
            "unit": "ops/s",
            "extra": "iterations: 3\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.networknt.schema.benchmark.NetworkntTestSuiteRequiredBenchmark.testsuite ( {\"specification\":\"7\"} )",
            "value": 3138.815532834987,
            "unit": "ops/s",
            "extra": "iterations: 3\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.networknt.schema.benchmark.NetworkntTestSuiteRequiredBenchmark.testsuite ( {\"specification\":\"6\"} )",
            "value": 3338.0528023563406,
            "unit": "ops/s",
            "extra": "iterations: 3\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.networknt.schema.benchmark.NetworkntTestSuiteRequiredBenchmark.testsuite ( {\"specification\":\"4\"} )",
            "value": 4842.265542401422,
            "unit": "ops/s",
            "extra": "iterations: 3\nforks: 1\nthreads: 1"
          }
        ]
      },
      {
        "commit": {
          "author": {
            "email": "kalayciburak1996@gmail.com",
            "name": "Burak Kalaycı",
            "username": "kalayciburak"
          },
          "committer": {
            "email": "noreply@github.com",
            "name": "GitHub",
            "username": "web-flow"
          },
          "distinct": true,
          "id": "e877866f973fc94b2a7ab2e500b6de6ba5bee4c9",
          "message": "Honor PathType when formatting SchemaException locations (#1272)\n\n* Honor PathType when formatting SchemaException locations\n\nSchema locations are stored as JSON Pointer fragments. SchemaException\nmessages from SchemaRegistry now render that fragment with the\nconfigured PathType so JSON_PATH users see $.properties.required\ninstead of #/properties/required.\n\nFixes #1270\n\n* Keep anchors and empty names when formatting schema locations\n\nAnchor fragments stay URI references. Empty JSON Pointer segments\nrender as $[''] under PathType.JSON_PATH so SchemaException keeps\nthe original diagnostic.",
          "timestamp": "2026-08-16T09:37:01-04:00",
          "tree_id": "85cc7764e4cd77116ffcedca80c1d3d9a889778d",
          "url": "https://github.com/networknt/json-schema-validator/commit/e877866f973fc94b2a7ab2e500b6de6ba5bee4c9"
        },
        "date": 1786887808936,
        "tool": "jmh",
        "benches": [
          {
            "name": "com.networknt.schema.benchmark.NetworkntBenchmark.basic",
            "value": 4423.145569078015,
            "unit": "ops/s",
            "extra": "iterations: 3\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.networknt.schema.benchmark.NetworkntTestSuiteOptionalBenchmark.testsuite ( {\"specification\":\"2020-12\"} )",
            "value": 1240.484118792354,
            "unit": "ops/s",
            "extra": "iterations: 3\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.networknt.schema.benchmark.NetworkntTestSuiteOptionalBenchmark.testsuite ( {\"specification\":\"2019-09\"} )",
            "value": 1296.9460281531674,
            "unit": "ops/s",
            "extra": "iterations: 3\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.networknt.schema.benchmark.NetworkntTestSuiteOptionalBenchmark.testsuite ( {\"specification\":\"7\"} )",
            "value": 1388.734455682341,
            "unit": "ops/s",
            "extra": "iterations: 3\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.networknt.schema.benchmark.NetworkntTestSuiteOptionalBenchmark.testsuite ( {\"specification\":\"6\"} )",
            "value": 3134.651577554714,
            "unit": "ops/s",
            "extra": "iterations: 3\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.networknt.schema.benchmark.NetworkntTestSuiteOptionalBenchmark.testsuite ( {\"specification\":\"4\"} )",
            "value": 3886.0136241626865,
            "unit": "ops/s",
            "extra": "iterations: 3\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.networknt.schema.benchmark.NetworkntTestSuiteRequiredBenchmark.testsuite ( {\"specification\":\"2020-12\"} )",
            "value": 1071.346430992341,
            "unit": "ops/s",
            "extra": "iterations: 3\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.networknt.schema.benchmark.NetworkntTestSuiteRequiredBenchmark.testsuite ( {\"specification\":\"2019-09\"} )",
            "value": 1178.3347476056358,
            "unit": "ops/s",
            "extra": "iterations: 3\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.networknt.schema.benchmark.NetworkntTestSuiteRequiredBenchmark.testsuite ( {\"specification\":\"7\"} )",
            "value": 3091.4835201855694,
            "unit": "ops/s",
            "extra": "iterations: 3\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.networknt.schema.benchmark.NetworkntTestSuiteRequiredBenchmark.testsuite ( {\"specification\":\"6\"} )",
            "value": 3379.486261238261,
            "unit": "ops/s",
            "extra": "iterations: 3\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.networknt.schema.benchmark.NetworkntTestSuiteRequiredBenchmark.testsuite ( {\"specification\":\"4\"} )",
            "value": 4698.061374929146,
            "unit": "ops/s",
            "extra": "iterations: 3\nforks: 1\nthreads: 1"
          }
        ]
      },
      {
        "commit": {
          "author": {
            "email": "denisgregor2@protonmail.com",
            "name": "Denis Gregor",
            "username": "dngr2"
          },
          "committer": {
            "email": "noreply@github.com",
            "name": "GitHub",
            "username": "web-flow"
          },
          "distinct": true,
          "id": "285170f885ea78c81a81659c1ecb7292c710aaa0",
          "message": "fix: uniqueItems compares numbers by value, not by node type (#1273)\n\n* fix: uniqueItems compares numbers by value, not by node type\n\nuniqueItems put each item into a HashSet<JsonNode> and relied on JsonNode\nequality, which is type-sensitive: IntNode(1) does not equal\nDoubleNode(1.0). Two mathematically equal numbers written differently were\ntherefore accepted as distinct items, and [1, 1.0] validated.\n\nThe suite's case for this rule is [1.0, 1.0, 1] (\"numbers are unique if\nmathematically unequal\"). It passes either way, because the two identical\ndecimals are caught before an integer is ever compared against a decimal,\nso the gap stayed hidden.\n\nItems are now compared by a derived key. Numbers reduce to a\nscale-independent BigDecimal, so 1, 1.0 and 1.00 are one value, and\n10000000000 matches 1.0e10. Only numbers are folded together: a number and\na boolean stay distinct, as do a number and its string form, which the\nsuite requires with nested [1] vs [true] and [0] vs [false]. Objects and\narrays are walked so the rule holds wherever the number sits, and map\nequality ignores property order.\n\nTests cover the integer/decimal pairs, differing scales, a large value in\nexponent form, nesting inside an object and an array, and the\nnumber-versus-boolean and number-versus-string cases that must stay\nunique. Four of them fail against the previous implementation.\n\nFull suite: 8487 tests, 0 failures.\n\n* fix: keep non-finite numbers out of the uniqueItems comparison\n\ndecimalValue() has no BigDecimal to return for NaN, Infinity or -Infinity and\nthrows, so folding numbers by value made uniqueItems raise instead of producing\na validation result for an instance a mapper had been configured to read with\nALLOW_NON_NUMERIC_NUMBERS.\n\nGuard with JsonNodeTypes.isNonFiniteNumber, as MultipleOfValidator.getDividend\nalready does. Those values fall through to comparing as nodes, which is what\nevery item did before numbers were folded, so their behaviour is unchanged.",
          "timestamp": "2026-08-16T19:06:11-04:00",
          "tree_id": "2e98e075246be9fb013a9c6f2ec24d88994c3cb7",
          "url": "https://github.com/networknt/json-schema-validator/commit/285170f885ea78c81a81659c1ecb7292c710aaa0"
        },
        "date": 1786921949939,
        "tool": "jmh",
        "benches": [
          {
            "name": "com.networknt.schema.benchmark.NetworkntBenchmark.basic",
            "value": 5904.3624304153645,
            "unit": "ops/s",
            "extra": "iterations: 3\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.networknt.schema.benchmark.NetworkntTestSuiteOptionalBenchmark.testsuite ( {\"specification\":\"2020-12\"} )",
            "value": 1475.6266379943063,
            "unit": "ops/s",
            "extra": "iterations: 3\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.networknt.schema.benchmark.NetworkntTestSuiteOptionalBenchmark.testsuite ( {\"specification\":\"2019-09\"} )",
            "value": 1499.2211884276928,
            "unit": "ops/s",
            "extra": "iterations: 3\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.networknt.schema.benchmark.NetworkntTestSuiteOptionalBenchmark.testsuite ( {\"specification\":\"7\"} )",
            "value": 1575.431180880444,
            "unit": "ops/s",
            "extra": "iterations: 3\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.networknt.schema.benchmark.NetworkntTestSuiteOptionalBenchmark.testsuite ( {\"specification\":\"6\"} )",
            "value": 3781.5315707404993,
            "unit": "ops/s",
            "extra": "iterations: 3\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.networknt.schema.benchmark.NetworkntTestSuiteOptionalBenchmark.testsuite ( {\"specification\":\"4\"} )",
            "value": 4542.905017363509,
            "unit": "ops/s",
            "extra": "iterations: 3\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.networknt.schema.benchmark.NetworkntTestSuiteRequiredBenchmark.testsuite ( {\"specification\":\"2020-12\"} )",
            "value": 1111.9014162410344,
            "unit": "ops/s",
            "extra": "iterations: 3\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.networknt.schema.benchmark.NetworkntTestSuiteRequiredBenchmark.testsuite ( {\"specification\":\"2019-09\"} )",
            "value": 1226.0007337372579,
            "unit": "ops/s",
            "extra": "iterations: 3\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.networknt.schema.benchmark.NetworkntTestSuiteRequiredBenchmark.testsuite ( {\"specification\":\"7\"} )",
            "value": 3425.381051839515,
            "unit": "ops/s",
            "extra": "iterations: 3\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.networknt.schema.benchmark.NetworkntTestSuiteRequiredBenchmark.testsuite ( {\"specification\":\"6\"} )",
            "value": 3851.4172318973215,
            "unit": "ops/s",
            "extra": "iterations: 3\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.networknt.schema.benchmark.NetworkntTestSuiteRequiredBenchmark.testsuite ( {\"specification\":\"4\"} )",
            "value": 5977.749915950643,
            "unit": "ops/s",
            "extra": "iterations: 3\nforks: 1\nthreads: 1"
          }
        ]
      },
      {
        "commit": {
          "author": {
            "email": "stevehu@gmail.com",
            "name": "Steve Hu",
            "username": "stevehu"
          },
          "committer": {
            "email": "stevehu@gmail.com",
            "name": "Steve Hu",
            "username": "stevehu"
          },
          "distinct": true,
          "id": "f92d938ae4fe0b4b42fbf20c6966e9514b26cfb9",
          "message": "fixes #1277 upgrade jackson to 3.2.1",
          "timestamp": "2026-08-20T13:30:20-04:00",
          "tree_id": "fdee7b5771ff9d9c31c83708c1045adfd1668a0d",
          "url": "https://github.com/networknt/json-schema-validator/commit/f92d938ae4fe0b4b42fbf20c6966e9514b26cfb9"
        },
        "date": 1787247415084,
        "tool": "jmh",
        "benches": [
          {
            "name": "com.networknt.schema.benchmark.NetworkntBenchmark.basic",
            "value": 4557.156088246996,
            "unit": "ops/s",
            "extra": "iterations: 3\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.networknt.schema.benchmark.NetworkntTestSuiteOptionalBenchmark.testsuite ( {\"specification\":\"2020-12\"} )",
            "value": 1250.7296304512636,
            "unit": "ops/s",
            "extra": "iterations: 3\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.networknt.schema.benchmark.NetworkntTestSuiteOptionalBenchmark.testsuite ( {\"specification\":\"2019-09\"} )",
            "value": 1302.026256737632,
            "unit": "ops/s",
            "extra": "iterations: 3\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.networknt.schema.benchmark.NetworkntTestSuiteOptionalBenchmark.testsuite ( {\"specification\":\"7\"} )",
            "value": 1394.1301726787613,
            "unit": "ops/s",
            "extra": "iterations: 3\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.networknt.schema.benchmark.NetworkntTestSuiteOptionalBenchmark.testsuite ( {\"specification\":\"6\"} )",
            "value": 3097.721608200261,
            "unit": "ops/s",
            "extra": "iterations: 3\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.networknt.schema.benchmark.NetworkntTestSuiteOptionalBenchmark.testsuite ( {\"specification\":\"4\"} )",
            "value": 3926.337420685881,
            "unit": "ops/s",
            "extra": "iterations: 3\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.networknt.schema.benchmark.NetworkntTestSuiteRequiredBenchmark.testsuite ( {\"specification\":\"2020-12\"} )",
            "value": 1039.5256887269193,
            "unit": "ops/s",
            "extra": "iterations: 3\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.networknt.schema.benchmark.NetworkntTestSuiteRequiredBenchmark.testsuite ( {\"specification\":\"2019-09\"} )",
            "value": 1153.4680492282534,
            "unit": "ops/s",
            "extra": "iterations: 3\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.networknt.schema.benchmark.NetworkntTestSuiteRequiredBenchmark.testsuite ( {\"specification\":\"7\"} )",
            "value": 3054.8690844486096,
            "unit": "ops/s",
            "extra": "iterations: 3\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.networknt.schema.benchmark.NetworkntTestSuiteRequiredBenchmark.testsuite ( {\"specification\":\"6\"} )",
            "value": 3287.2120716432187,
            "unit": "ops/s",
            "extra": "iterations: 3\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.networknt.schema.benchmark.NetworkntTestSuiteRequiredBenchmark.testsuite ( {\"specification\":\"4\"} )",
            "value": 4625.976798321728,
            "unit": "ops/s",
            "extra": "iterations: 3\nforks: 1\nthreads: 1"
          }
        ]
      },
      {
        "commit": {
          "author": {
            "email": "stevehu@gmail.com",
            "name": "Steve Hu",
            "username": "stevehu"
          },
          "committer": {
            "email": "stevehu@gmail.com",
            "name": "Steve Hu",
            "username": "stevehu"
          },
          "distinct": true,
          "id": "18539a8265f79a9ceda45be6e42b36eba5f377e5",
          "message": "upgrade to 3.0.7 and update changelog",
          "timestamp": "2026-08-20T13:36:01-04:00",
          "tree_id": "4b8b79e59afe84e27e9c6eb4816bb792785ff71a",
          "url": "https://github.com/networknt/json-schema-validator/commit/18539a8265f79a9ceda45be6e42b36eba5f377e5"
        },
        "date": 1787247763774,
        "tool": "jmh",
        "benches": [
          {
            "name": "com.networknt.schema.benchmark.NetworkntBenchmark.basic",
            "value": 6368.755205021974,
            "unit": "ops/s",
            "extra": "iterations: 3\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.networknt.schema.benchmark.NetworkntTestSuiteOptionalBenchmark.testsuite ( {\"specification\":\"2020-12\"} )",
            "value": 1521.5105716067435,
            "unit": "ops/s",
            "extra": "iterations: 3\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.networknt.schema.benchmark.NetworkntTestSuiteOptionalBenchmark.testsuite ( {\"specification\":\"2019-09\"} )",
            "value": 1568.291775530263,
            "unit": "ops/s",
            "extra": "iterations: 3\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.networknt.schema.benchmark.NetworkntTestSuiteOptionalBenchmark.testsuite ( {\"specification\":\"7\"} )",
            "value": 1696.2781787845104,
            "unit": "ops/s",
            "extra": "iterations: 3\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.networknt.schema.benchmark.NetworkntTestSuiteOptionalBenchmark.testsuite ( {\"specification\":\"6\"} )",
            "value": 4128.730481984939,
            "unit": "ops/s",
            "extra": "iterations: 3\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.networknt.schema.benchmark.NetworkntTestSuiteOptionalBenchmark.testsuite ( {\"specification\":\"4\"} )",
            "value": 5006.164817735391,
            "unit": "ops/s",
            "extra": "iterations: 3\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.networknt.schema.benchmark.NetworkntTestSuiteRequiredBenchmark.testsuite ( {\"specification\":\"2020-12\"} )",
            "value": 1172.1341296658213,
            "unit": "ops/s",
            "extra": "iterations: 3\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.networknt.schema.benchmark.NetworkntTestSuiteRequiredBenchmark.testsuite ( {\"specification\":\"2019-09\"} )",
            "value": 1311.561754287916,
            "unit": "ops/s",
            "extra": "iterations: 3\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.networknt.schema.benchmark.NetworkntTestSuiteRequiredBenchmark.testsuite ( {\"specification\":\"7\"} )",
            "value": 3665.1521754619976,
            "unit": "ops/s",
            "extra": "iterations: 3\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.networknt.schema.benchmark.NetworkntTestSuiteRequiredBenchmark.testsuite ( {\"specification\":\"6\"} )",
            "value": 4073.546517910217,
            "unit": "ops/s",
            "extra": "iterations: 3\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.networknt.schema.benchmark.NetworkntTestSuiteRequiredBenchmark.testsuite ( {\"specification\":\"4\"} )",
            "value": 6526.488745963398,
            "unit": "ops/s",
            "extra": "iterations: 3\nforks: 1\nthreads: 1"
          }
        ]
      },
      {
        "commit": {
          "author": {
            "email": "stevehu@gmail.com",
            "name": "Steve Hu",
            "username": "stevehu"
          },
          "committer": {
            "email": "stevehu@gmail.com",
            "name": "Steve Hu",
            "username": "stevehu"
          },
          "distinct": true,
          "id": "680d7b3d93880b9a7a01875b04fd0e5ef0490d27",
          "message": "add custom error message accessor",
          "timestamp": "2026-08-20T16:55:49-04:00",
          "tree_id": "0e6b1dbec9488a894acbb83ffe56860ed554e58a",
          "url": "https://github.com/networknt/json-schema-validator/commit/680d7b3d93880b9a7a01875b04fd0e5ef0490d27"
        },
        "date": 1787259758658,
        "tool": "jmh",
        "benches": [
          {
            "name": "com.networknt.schema.benchmark.NetworkntBenchmark.basic",
            "value": 4712.494784037794,
            "unit": "ops/s",
            "extra": "iterations: 3\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.networknt.schema.benchmark.NetworkntTestSuiteOptionalBenchmark.testsuite ( {\"specification\":\"2020-12\"} )",
            "value": 1229.0820229751084,
            "unit": "ops/s",
            "extra": "iterations: 3\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.networknt.schema.benchmark.NetworkntTestSuiteOptionalBenchmark.testsuite ( {\"specification\":\"2019-09\"} )",
            "value": 1285.5613652551385,
            "unit": "ops/s",
            "extra": "iterations: 3\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.networknt.schema.benchmark.NetworkntTestSuiteOptionalBenchmark.testsuite ( {\"specification\":\"7\"} )",
            "value": 1352.0365049237073,
            "unit": "ops/s",
            "extra": "iterations: 3\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.networknt.schema.benchmark.NetworkntTestSuiteOptionalBenchmark.testsuite ( {\"specification\":\"6\"} )",
            "value": 3036.5296318562764,
            "unit": "ops/s",
            "extra": "iterations: 3\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.networknt.schema.benchmark.NetworkntTestSuiteOptionalBenchmark.testsuite ( {\"specification\":\"4\"} )",
            "value": 3744.147708176875,
            "unit": "ops/s",
            "extra": "iterations: 3\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.networknt.schema.benchmark.NetworkntTestSuiteRequiredBenchmark.testsuite ( {\"specification\":\"2020-12\"} )",
            "value": 1017.7947195925894,
            "unit": "ops/s",
            "extra": "iterations: 3\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.networknt.schema.benchmark.NetworkntTestSuiteRequiredBenchmark.testsuite ( {\"specification\":\"2019-09\"} )",
            "value": 1118.8830036881557,
            "unit": "ops/s",
            "extra": "iterations: 3\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.networknt.schema.benchmark.NetworkntTestSuiteRequiredBenchmark.testsuite ( {\"specification\":\"7\"} )",
            "value": 3060.598187461774,
            "unit": "ops/s",
            "extra": "iterations: 3\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.networknt.schema.benchmark.NetworkntTestSuiteRequiredBenchmark.testsuite ( {\"specification\":\"6\"} )",
            "value": 3140.599274159838,
            "unit": "ops/s",
            "extra": "iterations: 3\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.networknt.schema.benchmark.NetworkntTestSuiteRequiredBenchmark.testsuite ( {\"specification\":\"4\"} )",
            "value": 4671.230611788837,
            "unit": "ops/s",
            "extra": "iterations: 3\nforks: 1\nthreads: 1"
          }
        ]
      },
      {
        "commit": {
          "author": {
            "email": "stevehu@gmail.com",
            "name": "Steve Hu",
            "username": "stevehu"
          },
          "committer": {
            "email": "stevehu@gmail.com",
            "name": "Steve Hu",
            "username": "stevehu"
          },
          "distinct": true,
          "id": "f8c76599c0bda42a72ff1c905e8d99ab4fcf59db",
          "message": "preserve Error constructor binary compatibility",
          "timestamp": "2026-08-20T17:12:15-04:00",
          "tree_id": "86203836a7d2a50d9f70949fb0097786e2e9422b",
          "url": "https://github.com/networknt/json-schema-validator/commit/f8c76599c0bda42a72ff1c905e8d99ab4fcf59db"
        },
        "date": 1787260716978,
        "tool": "jmh",
        "benches": [
          {
            "name": "com.networknt.schema.benchmark.NetworkntBenchmark.basic",
            "value": 5729.549327257816,
            "unit": "ops/s",
            "extra": "iterations: 3\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.networknt.schema.benchmark.NetworkntTestSuiteOptionalBenchmark.testsuite ( {\"specification\":\"2020-12\"} )",
            "value": 1643.4978832135846,
            "unit": "ops/s",
            "extra": "iterations: 3\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.networknt.schema.benchmark.NetworkntTestSuiteOptionalBenchmark.testsuite ( {\"specification\":\"2019-09\"} )",
            "value": 1710.9851681060184,
            "unit": "ops/s",
            "extra": "iterations: 3\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.networknt.schema.benchmark.NetworkntTestSuiteOptionalBenchmark.testsuite ( {\"specification\":\"7\"} )",
            "value": 1826.5007306870923,
            "unit": "ops/s",
            "extra": "iterations: 3\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.networknt.schema.benchmark.NetworkntTestSuiteOptionalBenchmark.testsuite ( {\"specification\":\"6\"} )",
            "value": 4129.281414558344,
            "unit": "ops/s",
            "extra": "iterations: 3\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.networknt.schema.benchmark.NetworkntTestSuiteOptionalBenchmark.testsuite ( {\"specification\":\"4\"} )",
            "value": 5009.463430080533,
            "unit": "ops/s",
            "extra": "iterations: 3\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.networknt.schema.benchmark.NetworkntTestSuiteRequiredBenchmark.testsuite ( {\"specification\":\"2020-12\"} )",
            "value": 1373.9514638740638,
            "unit": "ops/s",
            "extra": "iterations: 3\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.networknt.schema.benchmark.NetworkntTestSuiteRequiredBenchmark.testsuite ( {\"specification\":\"2019-09\"} )",
            "value": 1456.685792161471,
            "unit": "ops/s",
            "extra": "iterations: 3\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.networknt.schema.benchmark.NetworkntTestSuiteRequiredBenchmark.testsuite ( {\"specification\":\"7\"} )",
            "value": 3905.860002151336,
            "unit": "ops/s",
            "extra": "iterations: 3\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.networknt.schema.benchmark.NetworkntTestSuiteRequiredBenchmark.testsuite ( {\"specification\":\"6\"} )",
            "value": 4156.495409230265,
            "unit": "ops/s",
            "extra": "iterations: 3\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.networknt.schema.benchmark.NetworkntTestSuiteRequiredBenchmark.testsuite ( {\"specification\":\"4\"} )",
            "value": 5962.494379073517,
            "unit": "ops/s",
            "extra": "iterations: 3\nforks: 1\nthreads: 1"
          }
        ]
      },
      {
        "commit": {
          "author": {
            "email": "denisgregor2@protonmail.com",
            "name": "Denis Gregor",
            "username": "dngr2"
          },
          "committer": {
            "email": "noreply@github.com",
            "name": "GitHub",
            "username": "web-flow"
          },
          "distinct": true,
          "id": "2a28f87e35ed8a77898d4ebffcb4745dc23f5364",
          "message": "Allow apostrophe in uri-template literal (#1275)\n\nThe uri-template format regex listed ' in its excluded character set, so\n\"a'b\" was reported invalid. An apostrophe in a literal is valid; remove it\nfrom the exclusion set.",
          "timestamp": "2026-08-25T21:17:25-04:00",
          "tree_id": "7cc8325d939a7ee15a126c17f3745e1a312a1643",
          "url": "https://github.com/networknt/json-schema-validator/commit/2a28f87e35ed8a77898d4ebffcb4745dc23f5364"
        },
        "date": 1787707417753,
        "tool": "jmh",
        "benches": [
          {
            "name": "com.networknt.schema.benchmark.NetworkntBenchmark.basic",
            "value": 5725.495541374502,
            "unit": "ops/s",
            "extra": "iterations: 3\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.networknt.schema.benchmark.NetworkntTestSuiteOptionalBenchmark.testsuite ( {\"specification\":\"2020-12\"} )",
            "value": 1605.415634351425,
            "unit": "ops/s",
            "extra": "iterations: 3\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.networknt.schema.benchmark.NetworkntTestSuiteOptionalBenchmark.testsuite ( {\"specification\":\"2019-09\"} )",
            "value": 1731.998632125291,
            "unit": "ops/s",
            "extra": "iterations: 3\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.networknt.schema.benchmark.NetworkntTestSuiteOptionalBenchmark.testsuite ( {\"specification\":\"7\"} )",
            "value": 1815.6799164338718,
            "unit": "ops/s",
            "extra": "iterations: 3\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.networknt.schema.benchmark.NetworkntTestSuiteOptionalBenchmark.testsuite ( {\"specification\":\"6\"} )",
            "value": 3987.517785554573,
            "unit": "ops/s",
            "extra": "iterations: 3\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.networknt.schema.benchmark.NetworkntTestSuiteOptionalBenchmark.testsuite ( {\"specification\":\"4\"} )",
            "value": 5049.727824515074,
            "unit": "ops/s",
            "extra": "iterations: 3\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.networknt.schema.benchmark.NetworkntTestSuiteRequiredBenchmark.testsuite ( {\"specification\":\"2020-12\"} )",
            "value": 1319.768810184064,
            "unit": "ops/s",
            "extra": "iterations: 3\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.networknt.schema.benchmark.NetworkntTestSuiteRequiredBenchmark.testsuite ( {\"specification\":\"2019-09\"} )",
            "value": 1471.313909485041,
            "unit": "ops/s",
            "extra": "iterations: 3\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.networknt.schema.benchmark.NetworkntTestSuiteRequiredBenchmark.testsuite ( {\"specification\":\"7\"} )",
            "value": 4004.7808654293117,
            "unit": "ops/s",
            "extra": "iterations: 3\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.networknt.schema.benchmark.NetworkntTestSuiteRequiredBenchmark.testsuite ( {\"specification\":\"6\"} )",
            "value": 4259.558201013545,
            "unit": "ops/s",
            "extra": "iterations: 3\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.networknt.schema.benchmark.NetworkntTestSuiteRequiredBenchmark.testsuite ( {\"specification\":\"4\"} )",
            "value": 5995.369957362144,
            "unit": "ops/s",
            "extra": "iterations: 3\nforks: 1\nthreads: 1"
          }
        ]
      },
      {
        "commit": {
          "author": {
            "email": "denisgregor2@protonmail.com",
            "name": "Denis Gregor",
            "username": "dngr2"
          },
          "committer": {
            "email": "noreply@github.com",
            "name": "GitHub",
            "username": "web-flow"
          },
          "distinct": true,
          "id": "a9c96385d4d19eed227f919b9837d2a0724d8414",
          "message": "Reject empty labels in idn-hostname format (#1274)\n\n* Reject empty labels in idn-hostname format\n\nRFC5892.isValid skipped empty labels with continue, so a leading or interior\nempty label -- \".example\" or \"a..b\" -- validated as a valid idn-hostname.\nString.split already drops trailing empty labels, so the skip only ever hit\nleading/interior empties, which are invalid. Reject them.\n\n* Reject a hostname made up only of label separators\n\nString.split returns an empty array for a value that is all separators\n(\"...\" or \"。。\"), so the loop was skipped and it validated as true. Reject\nwhen there are no labels; this also subsumes the single-separator case.\n\n* Fix comment wording to reference label separators generically, not just '.'",
          "timestamp": "2026-08-25T22:00:57-04:00",
          "tree_id": "716c7a9a888c9f2805c16b9e63d64c4f219ea492",
          "url": "https://github.com/networknt/json-schema-validator/commit/a9c96385d4d19eed227f919b9837d2a0724d8414"
        },
        "date": 1787710035071,
        "tool": "jmh",
        "benches": [
          {
            "name": "com.networknt.schema.benchmark.NetworkntBenchmark.basic",
            "value": 4665.833836967012,
            "unit": "ops/s",
            "extra": "iterations: 3\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.networknt.schema.benchmark.NetworkntTestSuiteOptionalBenchmark.testsuite ( {\"specification\":\"2020-12\"} )",
            "value": 1254.8595220056447,
            "unit": "ops/s",
            "extra": "iterations: 3\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.networknt.schema.benchmark.NetworkntTestSuiteOptionalBenchmark.testsuite ( {\"specification\":\"2019-09\"} )",
            "value": 1269.2259440706891,
            "unit": "ops/s",
            "extra": "iterations: 3\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.networknt.schema.benchmark.NetworkntTestSuiteOptionalBenchmark.testsuite ( {\"specification\":\"7\"} )",
            "value": 1352.9491296830854,
            "unit": "ops/s",
            "extra": "iterations: 3\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.networknt.schema.benchmark.NetworkntTestSuiteOptionalBenchmark.testsuite ( {\"specification\":\"6\"} )",
            "value": 2994.7791698195474,
            "unit": "ops/s",
            "extra": "iterations: 3\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.networknt.schema.benchmark.NetworkntTestSuiteOptionalBenchmark.testsuite ( {\"specification\":\"4\"} )",
            "value": 3701.7572482677374,
            "unit": "ops/s",
            "extra": "iterations: 3\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.networknt.schema.benchmark.NetworkntTestSuiteRequiredBenchmark.testsuite ( {\"specification\":\"2020-12\"} )",
            "value": 1030.0179374280108,
            "unit": "ops/s",
            "extra": "iterations: 3\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.networknt.schema.benchmark.NetworkntTestSuiteRequiredBenchmark.testsuite ( {\"specification\":\"2019-09\"} )",
            "value": 1116.05116698891,
            "unit": "ops/s",
            "extra": "iterations: 3\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.networknt.schema.benchmark.NetworkntTestSuiteRequiredBenchmark.testsuite ( {\"specification\":\"7\"} )",
            "value": 3022.4689236885984,
            "unit": "ops/s",
            "extra": "iterations: 3\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.networknt.schema.benchmark.NetworkntTestSuiteRequiredBenchmark.testsuite ( {\"specification\":\"6\"} )",
            "value": 3156.315246357574,
            "unit": "ops/s",
            "extra": "iterations: 3\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.networknt.schema.benchmark.NetworkntTestSuiteRequiredBenchmark.testsuite ( {\"specification\":\"4\"} )",
            "value": 4521.924948069954,
            "unit": "ops/s",
            "extra": "iterations: 3\nforks: 1\nthreads: 1"
          }
        ]
      },
      {
        "commit": {
          "author": {
            "email": "tom@wiremock.org",
            "name": "Tom Akehurst",
            "username": "tomakehurst"
          },
          "committer": {
            "email": "noreply@github.com",
            "name": "GitHub",
            "username": "web-flow"
          },
          "distinct": true,
          "id": "8716b5b6359031589bb2db99815cd0b1cefe51f0",
          "message": "Fix nullable allOf ref type check (#1279)\n\n* Fix nullable check for allOf + $ref compositions\n\nA property declared nullable: true and composed via allOf containing\nonly a $ref (e.g. OpenAPI 3.0 style composition) incorrectly rejected\nnull values. The schema owning the failing type keyword is resolved\nthrough $ref and is a cached object whose lexical parent reflects\nwhere it is declared in the document, not where it was referenced\nfrom, so the existing one-hop parent/grandparent nullable check never\nfound the nullable declaration on the referencing schema.\n\nWalk the dynamic evaluation stack instead, following through any\nchain of $ref schemas, to find the referencing schema and check it\n(and its lexical parent) for nullable: true.\n\nCo-Authored-By: Claude Sonnet 5 <noreply@anthropic.com>\n\n* Add regression test confirming nullable allOf+$ref fix is dialect-scoped\n\nConfirms, per maintainer feedback on the upstream issue, that the\ndynamic evaluation stack walk added for nullable allOf + $ref\ncompositions has no effect outside dialects that enable the nullable\nkeyword (currently only OpenAPI 3.0.x) — null is still rejected for\nthe same schema shape under draft-07.\n\nCo-Authored-By: Claude Sonnet 5 <noreply@anthropic.com>\n\n* Ignore nullable declared directly beside $ref\n\nPer PR review feedback (networknt/json-schema-validator#1279), the\ndynamic evaluation stack walk added to support nullable allOf + $ref\ncompositions incorrectly honored nullable: true declared as a direct\nsibling of $ref. That's a Reference Object, and the OpenAPI 3.0\nspecification requires siblings of $ref to be ignored, so it must\nnever grant nullability on its own.\n\nTrack whether the current frame in the walk was reached via $ref and,\nif so, skip checking that frame's own nullable — only its lexical\nparent (the schema actually composing it, e.g. the allOf owner) is\nconsulted. The allOf + $ref case that motivated the original fix\nstill passes.\n\nCo-Authored-By: Claude Sonnet 5 <noreply@anthropic.com>\n\n---------\n\nCo-authored-by: Claude Sonnet 5 <noreply@anthropic.com>",
          "timestamp": "2026-08-27T16:20:46-04:00",
          "tree_id": "382fc976564c49945db6cc667be24889d1917445",
          "url": "https://github.com/networknt/json-schema-validator/commit/8716b5b6359031589bb2db99815cd0b1cefe51f0"
        },
        "date": 1787862438452,
        "tool": "jmh",
        "benches": [
          {
            "name": "com.networknt.schema.benchmark.NetworkntBenchmark.basic",
            "value": 4645.157659509513,
            "unit": "ops/s",
            "extra": "iterations: 3\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.networknt.schema.benchmark.NetworkntTestSuiteOptionalBenchmark.testsuite ( {\"specification\":\"2020-12\"} )",
            "value": 1220.9050651153138,
            "unit": "ops/s",
            "extra": "iterations: 3\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.networknt.schema.benchmark.NetworkntTestSuiteOptionalBenchmark.testsuite ( {\"specification\":\"2019-09\"} )",
            "value": 1295.5166086567351,
            "unit": "ops/s",
            "extra": "iterations: 3\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.networknt.schema.benchmark.NetworkntTestSuiteOptionalBenchmark.testsuite ( {\"specification\":\"7\"} )",
            "value": 1363.8123798891336,
            "unit": "ops/s",
            "extra": "iterations: 3\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.networknt.schema.benchmark.NetworkntTestSuiteOptionalBenchmark.testsuite ( {\"specification\":\"6\"} )",
            "value": 2991.760529302656,
            "unit": "ops/s",
            "extra": "iterations: 3\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.networknt.schema.benchmark.NetworkntTestSuiteOptionalBenchmark.testsuite ( {\"specification\":\"4\"} )",
            "value": 3725.3455907779467,
            "unit": "ops/s",
            "extra": "iterations: 3\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.networknt.schema.benchmark.NetworkntTestSuiteRequiredBenchmark.testsuite ( {\"specification\":\"2020-12\"} )",
            "value": 1024.130708739529,
            "unit": "ops/s",
            "extra": "iterations: 3\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.networknt.schema.benchmark.NetworkntTestSuiteRequiredBenchmark.testsuite ( {\"specification\":\"2019-09\"} )",
            "value": 1109.4281789458375,
            "unit": "ops/s",
            "extra": "iterations: 3\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.networknt.schema.benchmark.NetworkntTestSuiteRequiredBenchmark.testsuite ( {\"specification\":\"7\"} )",
            "value": 3013.6050064807027,
            "unit": "ops/s",
            "extra": "iterations: 3\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.networknt.schema.benchmark.NetworkntTestSuiteRequiredBenchmark.testsuite ( {\"specification\":\"6\"} )",
            "value": 3189.4322093940846,
            "unit": "ops/s",
            "extra": "iterations: 3\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.networknt.schema.benchmark.NetworkntTestSuiteRequiredBenchmark.testsuite ( {\"specification\":\"4\"} )",
            "value": 4567.011642184328,
            "unit": "ops/s",
            "extra": "iterations: 3\nforks: 1\nthreads: 1"
          }
        ]
      },
      {
        "commit": {
          "author": {
            "email": "youdie006@naver.com",
            "name": "KBS",
            "username": "youdie006"
          },
          "committer": {
            "email": "noreply@github.com",
            "name": "GitHub",
            "username": "web-flow"
          },
          "distinct": true,
          "id": "81efb27647c14f42fad0712a10f9b730ddcf0f68",
          "message": "Honor PathType when formatting DiscriminatorValidator schema locations (#1281)\n\n#1272 made SchemaException render the schema location with the configured\nPathType, through SchemaRegistry.formatSchemaLocation, and applied it to\nthe two \"Schema at ...\" throws in that class. DiscriminatorValidator\nthrows the same message at three sites and still concatenates the raw\nSchemaLocation.\n\nWith pathType(PathType.JSON_PATH) the message mixes the two notations in\none sentence, because instanceLocation is already a NodePath built from\nthe configured PathType:\n\n  Schema at #/allOf/1/discriminator is redefining the discriminator\n  property that has already been set for at $\n\nAdds the same private helper the registry uses, so the default\nJSON_POINTER output is unchanged.",
          "timestamp": "2026-09-15T10:18:53-04:00",
          "tree_id": "ebc6390f9f8108b6f4558803dc08dbd593ba3853",
          "url": "https://github.com/networknt/json-schema-validator/commit/81efb27647c14f42fad0712a10f9b730ddcf0f68"
        },
        "date": 1789482326646,
        "tool": "jmh",
        "benches": [
          {
            "name": "com.networknt.schema.benchmark.NetworkntBenchmark.basic",
            "value": 4664.239158052733,
            "unit": "ops/s",
            "extra": "iterations: 3\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.networknt.schema.benchmark.NetworkntTestSuiteOptionalBenchmark.testsuite ( {\"specification\":\"2020-12\"} )",
            "value": 1246.9095255311022,
            "unit": "ops/s",
            "extra": "iterations: 3\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.networknt.schema.benchmark.NetworkntTestSuiteOptionalBenchmark.testsuite ( {\"specification\":\"2019-09\"} )",
            "value": 1272.0969828425575,
            "unit": "ops/s",
            "extra": "iterations: 3\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.networknt.schema.benchmark.NetworkntTestSuiteOptionalBenchmark.testsuite ( {\"specification\":\"7\"} )",
            "value": 1353.789606565263,
            "unit": "ops/s",
            "extra": "iterations: 3\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.networknt.schema.benchmark.NetworkntTestSuiteOptionalBenchmark.testsuite ( {\"specification\":\"6\"} )",
            "value": 3084.105967842032,
            "unit": "ops/s",
            "extra": "iterations: 3\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.networknt.schema.benchmark.NetworkntTestSuiteOptionalBenchmark.testsuite ( {\"specification\":\"4\"} )",
            "value": 3763.648742339808,
            "unit": "ops/s",
            "extra": "iterations: 3\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.networknt.schema.benchmark.NetworkntTestSuiteRequiredBenchmark.testsuite ( {\"specification\":\"2020-12\"} )",
            "value": 1032.555034967825,
            "unit": "ops/s",
            "extra": "iterations: 3\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.networknt.schema.benchmark.NetworkntTestSuiteRequiredBenchmark.testsuite ( {\"specification\":\"2019-09\"} )",
            "value": 1109.9324838328148,
            "unit": "ops/s",
            "extra": "iterations: 3\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.networknt.schema.benchmark.NetworkntTestSuiteRequiredBenchmark.testsuite ( {\"specification\":\"7\"} )",
            "value": 3002.648712924293,
            "unit": "ops/s",
            "extra": "iterations: 3\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.networknt.schema.benchmark.NetworkntTestSuiteRequiredBenchmark.testsuite ( {\"specification\":\"6\"} )",
            "value": 3247.280548589201,
            "unit": "ops/s",
            "extra": "iterations: 3\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.networknt.schema.benchmark.NetworkntTestSuiteRequiredBenchmark.testsuite ( {\"specification\":\"4\"} )",
            "value": 4655.276547824673,
            "unit": "ops/s",
            "extra": "iterations: 3\nforks: 1\nthreads: 1"
          }
        ]
      },
      {
        "commit": {
          "author": {
            "email": "stevehu@gmail.com",
            "name": "Steve Hu",
            "username": "stevehu"
          },
          "committer": {
            "email": "noreply@github.com",
            "name": "GitHub",
            "username": "web-flow"
          },
          "distinct": true,
          "id": "3c433c440b9baf8339929fb8abbcd95eac9fa503",
          "message": "Add YAML non-finite number regression test for #1228 (#1282)\n\nJackson 3.2 parses YAML .nan/.inf/-.inf as numeric values, and #1241\nalready handles non-finite numbers during validation. Cover the default\nYAML mapper path so these inputs keep producing a type error instead of\na parser exception.\n\nTest contributed by @patpatpat123 in #1228.\n\n\nClaude-Session: https://claude.ai/code/session_01XJ3SkCprrZVpASPFMADg1h\n\nCo-authored-by: Claude Opus 5 <noreply@anthropic.com>",
          "timestamp": "2026-09-15T10:32:58-04:00",
          "tree_id": "8e2ef8c713fde85f546f6c9e445bd0d24425d861",
          "url": "https://github.com/networknt/json-schema-validator/commit/3c433c440b9baf8339929fb8abbcd95eac9fa503"
        },
        "date": 1789483163604,
        "tool": "jmh",
        "benches": [
          {
            "name": "com.networknt.schema.benchmark.NetworkntBenchmark.basic",
            "value": 4650.651917923489,
            "unit": "ops/s",
            "extra": "iterations: 3\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.networknt.schema.benchmark.NetworkntTestSuiteOptionalBenchmark.testsuite ( {\"specification\":\"2020-12\"} )",
            "value": 1244.4049930925094,
            "unit": "ops/s",
            "extra": "iterations: 3\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.networknt.schema.benchmark.NetworkntTestSuiteOptionalBenchmark.testsuite ( {\"specification\":\"2019-09\"} )",
            "value": 1284.701700088972,
            "unit": "ops/s",
            "extra": "iterations: 3\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.networknt.schema.benchmark.NetworkntTestSuiteOptionalBenchmark.testsuite ( {\"specification\":\"7\"} )",
            "value": 1364.6897134169203,
            "unit": "ops/s",
            "extra": "iterations: 3\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.networknt.schema.benchmark.NetworkntTestSuiteOptionalBenchmark.testsuite ( {\"specification\":\"6\"} )",
            "value": 3026.64078112248,
            "unit": "ops/s",
            "extra": "iterations: 3\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.networknt.schema.benchmark.NetworkntTestSuiteOptionalBenchmark.testsuite ( {\"specification\":\"4\"} )",
            "value": 3868.964054684077,
            "unit": "ops/s",
            "extra": "iterations: 3\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.networknt.schema.benchmark.NetworkntTestSuiteRequiredBenchmark.testsuite ( {\"specification\":\"2020-12\"} )",
            "value": 1021.8699690906702,
            "unit": "ops/s",
            "extra": "iterations: 3\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.networknt.schema.benchmark.NetworkntTestSuiteRequiredBenchmark.testsuite ( {\"specification\":\"2019-09\"} )",
            "value": 1115.2621073550447,
            "unit": "ops/s",
            "extra": "iterations: 3\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.networknt.schema.benchmark.NetworkntTestSuiteRequiredBenchmark.testsuite ( {\"specification\":\"7\"} )",
            "value": 2916.5014920714307,
            "unit": "ops/s",
            "extra": "iterations: 3\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.networknt.schema.benchmark.NetworkntTestSuiteRequiredBenchmark.testsuite ( {\"specification\":\"6\"} )",
            "value": 3177.469774340027,
            "unit": "ops/s",
            "extra": "iterations: 3\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.networknt.schema.benchmark.NetworkntTestSuiteRequiredBenchmark.testsuite ( {\"specification\":\"4\"} )",
            "value": 4564.453409796776,
            "unit": "ops/s",
            "extra": "iterations: 3\nforks: 1\nthreads: 1"
          }
        ]
      },
      {
        "commit": {
          "author": {
            "email": "tom@wiremock.org",
            "name": "Tom Akehurst",
            "username": "tomakehurst"
          },
          "committer": {
            "email": "noreply@github.com",
            "name": "GitHub",
            "username": "web-flow"
          },
          "distinct": true,
          "id": "dc11f655819b405654769031f89897cd3f6fae56",
          "message": "Port the nullable ancestor walk from the 2.x branch (#1283)\n\nResolves the OpenAPI nullable keyword by descending the engine's own\nevaluation stacks rather than reconstructing provenance from schema\nstructure. The previous walk climbed lexical parents and sniffed for\n$ref members, which misattributed nullable across reference sites,\nwas sensitive to schema-node identity, and missed $dynamicRef and\n$recursiveRef entirely.\n\nThe walk pairs evaluationSchema with evaluationSchemaPath to recover\nthe keyword each schema was entered by, propagating nullable through\nallOf/oneOf/anyOf, if/then/else and the three referencing keywords,\nand stopping at any keyword that moves to a different value. As a\nresult nullable no longer leaks into a container's properties, items\nor additionalProperties children, and no longer reaches inside not.\n\nApplies the same walk to enum, which previously decided at\nconstruction time whether null was accepted, and relaxes only the\noneOf cardinality check for a nullable null, leaving branch errors\nreported when nothing matches.\n\nPorted from networknt/json-schema-validator#1280. Two adaptations\nwere needed for this branch rather than a straight copy:\n\n  - isNodeNullable now calls asBoolean(false). Jackson 3 throws on a\n    node it cannot coerce where Jackson 2 returns false, and the walk\n    reads nullable on every composing ancestor, so a malformed member\n    anywhere in the chain would abort the validation.\n\n  - The typeLoose enum change concerns the empty string here, not the\n    string \"null\". NullNode.asString() is \"\" in Jackson 3, so that is\n    the value the accepted set silently permitted.\n\nCo-authored-by: Claude Opus 5 (1M context) <noreply@anthropic.com>",
          "timestamp": "2026-09-16T20:18:34-04:00",
          "tree_id": "c8ae1f152e8b7332ebf4c8cbace4e14a48d60761",
          "url": "https://github.com/networknt/json-schema-validator/commit/dc11f655819b405654769031f89897cd3f6fae56"
        },
        "date": 1789604697565,
        "tool": "jmh",
        "benches": [
          {
            "name": "com.networknt.schema.benchmark.NetworkntBenchmark.basic",
            "value": 4723.094369957511,
            "unit": "ops/s",
            "extra": "iterations: 3\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.networknt.schema.benchmark.NetworkntTestSuiteOptionalBenchmark.testsuite ( {\"specification\":\"2020-12\"} )",
            "value": 1229.6775348222138,
            "unit": "ops/s",
            "extra": "iterations: 3\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.networknt.schema.benchmark.NetworkntTestSuiteOptionalBenchmark.testsuite ( {\"specification\":\"2019-09\"} )",
            "value": 1297.8416340434153,
            "unit": "ops/s",
            "extra": "iterations: 3\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.networknt.schema.benchmark.NetworkntTestSuiteOptionalBenchmark.testsuite ( {\"specification\":\"7\"} )",
            "value": 1346.8937728259916,
            "unit": "ops/s",
            "extra": "iterations: 3\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.networknt.schema.benchmark.NetworkntTestSuiteOptionalBenchmark.testsuite ( {\"specification\":\"6\"} )",
            "value": 3056.253236908696,
            "unit": "ops/s",
            "extra": "iterations: 3\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.networknt.schema.benchmark.NetworkntTestSuiteOptionalBenchmark.testsuite ( {\"specification\":\"4\"} )",
            "value": 3825.9651388253365,
            "unit": "ops/s",
            "extra": "iterations: 3\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.networknt.schema.benchmark.NetworkntTestSuiteRequiredBenchmark.testsuite ( {\"specification\":\"2020-12\"} )",
            "value": 1020.3800488480965,
            "unit": "ops/s",
            "extra": "iterations: 3\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.networknt.schema.benchmark.NetworkntTestSuiteRequiredBenchmark.testsuite ( {\"specification\":\"2019-09\"} )",
            "value": 1100.457929528938,
            "unit": "ops/s",
            "extra": "iterations: 3\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.networknt.schema.benchmark.NetworkntTestSuiteRequiredBenchmark.testsuite ( {\"specification\":\"7\"} )",
            "value": 2954.6294131179925,
            "unit": "ops/s",
            "extra": "iterations: 3\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.networknt.schema.benchmark.NetworkntTestSuiteRequiredBenchmark.testsuite ( {\"specification\":\"6\"} )",
            "value": 3167.4450491546772,
            "unit": "ops/s",
            "extra": "iterations: 3\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.networknt.schema.benchmark.NetworkntTestSuiteRequiredBenchmark.testsuite ( {\"specification\":\"4\"} )",
            "value": 4510.998621626637,
            "unit": "ops/s",
            "extra": "iterations: 3\nforks: 1\nthreads: 1"
          }
        ]
      },
      {
        "commit": {
          "author": {
            "email": "stevehu@gmail.com",
            "name": "Steve Hu",
            "username": "stevehu"
          },
          "committer": {
            "email": "stevehu@gmail.com",
            "name": "Steve Hu",
            "username": "stevehu"
          },
          "distinct": true,
          "id": "4649209d5d2764a52e47eb5b735eee1b68bdb29c",
          "message": "upgrade central-publishing-maven to 0.11.0 from 0.7.0\n\nCo-Authored-By: Claude Sonnet 5 <noreply@anthropic.com>",
          "timestamp": "2026-09-18T10:18:11-04:00",
          "tree_id": "2529b2a71c39055027e24bf2d414af1dffab282b",
          "url": "https://github.com/networknt/json-schema-validator/commit/4649209d5d2764a52e47eb5b735eee1b68bdb29c"
        },
        "date": 1789741497426,
        "tool": "jmh",
        "benches": [
          {
            "name": "com.networknt.schema.benchmark.NetworkntBenchmark.basic",
            "value": 5777.093211156117,
            "unit": "ops/s",
            "extra": "iterations: 3\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.networknt.schema.benchmark.NetworkntTestSuiteOptionalBenchmark.testsuite ( {\"specification\":\"2020-12\"} )",
            "value": 1639.9532180153547,
            "unit": "ops/s",
            "extra": "iterations: 3\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.networknt.schema.benchmark.NetworkntTestSuiteOptionalBenchmark.testsuite ( {\"specification\":\"2019-09\"} )",
            "value": 1737.7444270575718,
            "unit": "ops/s",
            "extra": "iterations: 3\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.networknt.schema.benchmark.NetworkntTestSuiteOptionalBenchmark.testsuite ( {\"specification\":\"7\"} )",
            "value": 1796.703530834627,
            "unit": "ops/s",
            "extra": "iterations: 3\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.networknt.schema.benchmark.NetworkntTestSuiteOptionalBenchmark.testsuite ( {\"specification\":\"6\"} )",
            "value": 4056.670963168792,
            "unit": "ops/s",
            "extra": "iterations: 3\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.networknt.schema.benchmark.NetworkntTestSuiteOptionalBenchmark.testsuite ( {\"specification\":\"4\"} )",
            "value": 5177.975785275522,
            "unit": "ops/s",
            "extra": "iterations: 3\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.networknt.schema.benchmark.NetworkntTestSuiteRequiredBenchmark.testsuite ( {\"specification\":\"2020-12\"} )",
            "value": 1358.330122322267,
            "unit": "ops/s",
            "extra": "iterations: 3\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.networknt.schema.benchmark.NetworkntTestSuiteRequiredBenchmark.testsuite ( {\"specification\":\"2019-09\"} )",
            "value": 1427.0488514730896,
            "unit": "ops/s",
            "extra": "iterations: 3\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.networknt.schema.benchmark.NetworkntTestSuiteRequiredBenchmark.testsuite ( {\"specification\":\"7\"} )",
            "value": 3924.2535530233577,
            "unit": "ops/s",
            "extra": "iterations: 3\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.networknt.schema.benchmark.NetworkntTestSuiteRequiredBenchmark.testsuite ( {\"specification\":\"6\"} )",
            "value": 4246.668009253693,
            "unit": "ops/s",
            "extra": "iterations: 3\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.networknt.schema.benchmark.NetworkntTestSuiteRequiredBenchmark.testsuite ( {\"specification\":\"4\"} )",
            "value": 6086.748967133994,
            "unit": "ops/s",
            "extra": "iterations: 3\nforks: 1\nthreads: 1"
          }
        ]
      },
      {
        "commit": {
          "author": {
            "email": "stevehu@gmail.com",
            "name": "Steve Hu",
            "username": "stevehu"
          },
          "committer": {
            "email": "stevehu@gmail.com",
            "name": "Steve Hu",
            "username": "stevehu"
          },
          "distinct": true,
          "id": "6f5d6c180d5931fd6920e64e12d7f17dcfa0dd05",
          "message": "upgrade actions/checkout",
          "timestamp": "2026-09-18T10:28:45-04:00",
          "tree_id": "2f9d986391d1cd9697325e0cb8b5bed57be9587f",
          "url": "https://github.com/networknt/json-schema-validator/commit/6f5d6c180d5931fd6920e64e12d7f17dcfa0dd05"
        },
        "date": 1789742114248,
        "tool": "jmh",
        "benches": [
          {
            "name": "com.networknt.schema.benchmark.NetworkntBenchmark.basic",
            "value": 4645.758655862809,
            "unit": "ops/s",
            "extra": "iterations: 3\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.networknt.schema.benchmark.NetworkntTestSuiteOptionalBenchmark.testsuite ( {\"specification\":\"2020-12\"} )",
            "value": 1236.2892511903603,
            "unit": "ops/s",
            "extra": "iterations: 3\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.networknt.schema.benchmark.NetworkntTestSuiteOptionalBenchmark.testsuite ( {\"specification\":\"2019-09\"} )",
            "value": 1286.6065137961505,
            "unit": "ops/s",
            "extra": "iterations: 3\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.networknt.schema.benchmark.NetworkntTestSuiteOptionalBenchmark.testsuite ( {\"specification\":\"7\"} )",
            "value": 1360.5487527454559,
            "unit": "ops/s",
            "extra": "iterations: 3\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.networknt.schema.benchmark.NetworkntTestSuiteOptionalBenchmark.testsuite ( {\"specification\":\"6\"} )",
            "value": 3047.514301850233,
            "unit": "ops/s",
            "extra": "iterations: 3\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.networknt.schema.benchmark.NetworkntTestSuiteOptionalBenchmark.testsuite ( {\"specification\":\"4\"} )",
            "value": 3785.949911376232,
            "unit": "ops/s",
            "extra": "iterations: 3\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.networknt.schema.benchmark.NetworkntTestSuiteRequiredBenchmark.testsuite ( {\"specification\":\"2020-12\"} )",
            "value": 990.3269016867185,
            "unit": "ops/s",
            "extra": "iterations: 3\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.networknt.schema.benchmark.NetworkntTestSuiteRequiredBenchmark.testsuite ( {\"specification\":\"2019-09\"} )",
            "value": 1078.275433227371,
            "unit": "ops/s",
            "extra": "iterations: 3\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.networknt.schema.benchmark.NetworkntTestSuiteRequiredBenchmark.testsuite ( {\"specification\":\"7\"} )",
            "value": 2878.444938049826,
            "unit": "ops/s",
            "extra": "iterations: 3\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.networknt.schema.benchmark.NetworkntTestSuiteRequiredBenchmark.testsuite ( {\"specification\":\"6\"} )",
            "value": 3148.7942466839595,
            "unit": "ops/s",
            "extra": "iterations: 3\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.networknt.schema.benchmark.NetworkntTestSuiteRequiredBenchmark.testsuite ( {\"specification\":\"4\"} )",
            "value": 4591.010039935337,
            "unit": "ops/s",
            "extra": "iterations: 3\nforks: 1\nthreads: 1"
          }
        ]
      },
      {
        "commit": {
          "author": {
            "email": "stevehu@gmail.com",
            "name": "Steve Hu",
            "username": "stevehu"
          },
          "committer": {
            "email": "stevehu@gmail.com",
            "name": "Steve Hu",
            "username": "stevehu"
          },
          "distinct": true,
          "id": "3d9971da7eb8a87421813d972f2ebfcf1994e924",
          "message": "upgrade logback to 1.5.37",
          "timestamp": "2026-09-18T11:51:57-04:00",
          "tree_id": "a2da6bbfcc532316f5ce6e30d6682ccee52089ed",
          "url": "https://github.com/networknt/json-schema-validator/commit/3d9971da7eb8a87421813d972f2ebfcf1994e924"
        },
        "date": 1789747117595,
        "tool": "jmh",
        "benches": [
          {
            "name": "com.networknt.schema.benchmark.NetworkntBenchmark.basic",
            "value": 6323.820702610189,
            "unit": "ops/s",
            "extra": "iterations: 3\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.networknt.schema.benchmark.NetworkntTestSuiteOptionalBenchmark.testsuite ( {\"specification\":\"2020-12\"} )",
            "value": 1474.9544049726799,
            "unit": "ops/s",
            "extra": "iterations: 3\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.networknt.schema.benchmark.NetworkntTestSuiteOptionalBenchmark.testsuite ( {\"specification\":\"2019-09\"} )",
            "value": 1585.6800454979727,
            "unit": "ops/s",
            "extra": "iterations: 3\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.networknt.schema.benchmark.NetworkntTestSuiteOptionalBenchmark.testsuite ( {\"specification\":\"7\"} )",
            "value": 1699.0895987074844,
            "unit": "ops/s",
            "extra": "iterations: 3\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.networknt.schema.benchmark.NetworkntTestSuiteOptionalBenchmark.testsuite ( {\"specification\":\"6\"} )",
            "value": 4117.076746205684,
            "unit": "ops/s",
            "extra": "iterations: 3\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.networknt.schema.benchmark.NetworkntTestSuiteOptionalBenchmark.testsuite ( {\"specification\":\"4\"} )",
            "value": 5010.12049272668,
            "unit": "ops/s",
            "extra": "iterations: 3\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.networknt.schema.benchmark.NetworkntTestSuiteRequiredBenchmark.testsuite ( {\"specification\":\"2020-12\"} )",
            "value": 1173.1691205190064,
            "unit": "ops/s",
            "extra": "iterations: 3\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.networknt.schema.benchmark.NetworkntTestSuiteRequiredBenchmark.testsuite ( {\"specification\":\"2019-09\"} )",
            "value": 1288.4276009789228,
            "unit": "ops/s",
            "extra": "iterations: 3\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.networknt.schema.benchmark.NetworkntTestSuiteRequiredBenchmark.testsuite ( {\"specification\":\"7\"} )",
            "value": 3612.6150274285196,
            "unit": "ops/s",
            "extra": "iterations: 3\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.networknt.schema.benchmark.NetworkntTestSuiteRequiredBenchmark.testsuite ( {\"specification\":\"6\"} )",
            "value": 4117.7091317549275,
            "unit": "ops/s",
            "extra": "iterations: 3\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.networknt.schema.benchmark.NetworkntTestSuiteRequiredBenchmark.testsuite ( {\"specification\":\"4\"} )",
            "value": 6363.279595695646,
            "unit": "ops/s",
            "extra": "iterations: 3\nforks: 1\nthreads: 1"
          }
        ]
      },
      {
        "commit": {
          "author": {
            "email": "stevehu@gmail.com",
            "name": "Steve Hu",
            "username": "stevehu"
          },
          "committer": {
            "email": "stevehu@gmail.com",
            "name": "Steve Hu",
            "username": "stevehu"
          },
          "distinct": true,
          "id": "2575a025007dca1028df1105bb225e5f4397c3ea",
          "message": "upgrade logback to 1.6.3 from 1.5.37",
          "timestamp": "2026-09-18T11:59:10-04:00",
          "tree_id": "4627defe475c7463321d7d2cb36033ddb0fd4f38",
          "url": "https://github.com/networknt/json-schema-validator/commit/2575a025007dca1028df1105bb225e5f4397c3ea"
        },
        "date": 1789747544341,
        "tool": "jmh",
        "benches": [
          {
            "name": "com.networknt.schema.benchmark.NetworkntBenchmark.basic",
            "value": 4640.638604827695,
            "unit": "ops/s",
            "extra": "iterations: 3\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.networknt.schema.benchmark.NetworkntTestSuiteOptionalBenchmark.testsuite ( {\"specification\":\"2020-12\"} )",
            "value": 1233.7832015062768,
            "unit": "ops/s",
            "extra": "iterations: 3\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.networknt.schema.benchmark.NetworkntTestSuiteOptionalBenchmark.testsuite ( {\"specification\":\"2019-09\"} )",
            "value": 1305.1856231411748,
            "unit": "ops/s",
            "extra": "iterations: 3\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.networknt.schema.benchmark.NetworkntTestSuiteOptionalBenchmark.testsuite ( {\"specification\":\"7\"} )",
            "value": 1328.9573246751872,
            "unit": "ops/s",
            "extra": "iterations: 3\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.networknt.schema.benchmark.NetworkntTestSuiteOptionalBenchmark.testsuite ( {\"specification\":\"6\"} )",
            "value": 3042.8438528912816,
            "unit": "ops/s",
            "extra": "iterations: 3\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.networknt.schema.benchmark.NetworkntTestSuiteOptionalBenchmark.testsuite ( {\"specification\":\"4\"} )",
            "value": 3758.4138788359473,
            "unit": "ops/s",
            "extra": "iterations: 3\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.networknt.schema.benchmark.NetworkntTestSuiteRequiredBenchmark.testsuite ( {\"specification\":\"2020-12\"} )",
            "value": 992.5359909531384,
            "unit": "ops/s",
            "extra": "iterations: 3\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.networknt.schema.benchmark.NetworkntTestSuiteRequiredBenchmark.testsuite ( {\"specification\":\"2019-09\"} )",
            "value": 1107.893887168265,
            "unit": "ops/s",
            "extra": "iterations: 3\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.networknt.schema.benchmark.NetworkntTestSuiteRequiredBenchmark.testsuite ( {\"specification\":\"7\"} )",
            "value": 2935.0814591349076,
            "unit": "ops/s",
            "extra": "iterations: 3\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.networknt.schema.benchmark.NetworkntTestSuiteRequiredBenchmark.testsuite ( {\"specification\":\"6\"} )",
            "value": 3161.3206242022547,
            "unit": "ops/s",
            "extra": "iterations: 3\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.networknt.schema.benchmark.NetworkntTestSuiteRequiredBenchmark.testsuite ( {\"specification\":\"4\"} )",
            "value": 4686.527922057421,
            "unit": "ops/s",
            "extra": "iterations: 3\nforks: 1\nthreads: 1"
          }
        ]
      },
      {
        "commit": {
          "author": {
            "email": "stevehu@gmail.com",
            "name": "Steve Hu",
            "username": "stevehu"
          },
          "committer": {
            "email": "stevehu@gmail.com",
            "name": "Steve Hu",
            "username": "stevehu"
          },
          "distinct": true,
          "id": "f26327c64430a93ee2765c7877e9fd377a958bd2",
          "message": "upgrade maven-surefire to 3.6.0",
          "timestamp": "2026-09-28T09:30:46-04:00",
          "tree_id": "f488b9bc54b4f3e52c7d3942ecb5ec15df49afb9",
          "url": "https://github.com/networknt/json-schema-validator/commit/f26327c64430a93ee2765c7877e9fd377a958bd2"
        },
        "date": 1790602644891,
        "tool": "jmh",
        "benches": [
          {
            "name": "com.networknt.schema.benchmark.NetworkntBenchmark.basic",
            "value": 8245.643756458025,
            "unit": "ops/s",
            "extra": "iterations: 3\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.networknt.schema.benchmark.NetworkntTestSuiteOptionalBenchmark.testsuite ( {\"specification\":\"2020-12\"} )",
            "value": 2242.8735987436744,
            "unit": "ops/s",
            "extra": "iterations: 3\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.networknt.schema.benchmark.NetworkntTestSuiteOptionalBenchmark.testsuite ( {\"specification\":\"2019-09\"} )",
            "value": 2393.4000815083023,
            "unit": "ops/s",
            "extra": "iterations: 3\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.networknt.schema.benchmark.NetworkntTestSuiteOptionalBenchmark.testsuite ( {\"specification\":\"7\"} )",
            "value": 2520.978294124108,
            "unit": "ops/s",
            "extra": "iterations: 3\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.networknt.schema.benchmark.NetworkntTestSuiteOptionalBenchmark.testsuite ( {\"specification\":\"6\"} )",
            "value": 5697.408904420551,
            "unit": "ops/s",
            "extra": "iterations: 3\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.networknt.schema.benchmark.NetworkntTestSuiteOptionalBenchmark.testsuite ( {\"specification\":\"4\"} )",
            "value": 6973.692810882029,
            "unit": "ops/s",
            "extra": "iterations: 3\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.networknt.schema.benchmark.NetworkntTestSuiteRequiredBenchmark.testsuite ( {\"specification\":\"2020-12\"} )",
            "value": 1916.7330329324031,
            "unit": "ops/s",
            "extra": "iterations: 3\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.networknt.schema.benchmark.NetworkntTestSuiteRequiredBenchmark.testsuite ( {\"specification\":\"2019-09\"} )",
            "value": 2062.6647321230594,
            "unit": "ops/s",
            "extra": "iterations: 3\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.networknt.schema.benchmark.NetworkntTestSuiteRequiredBenchmark.testsuite ( {\"specification\":\"7\"} )",
            "value": 5193.318277787302,
            "unit": "ops/s",
            "extra": "iterations: 3\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.networknt.schema.benchmark.NetworkntTestSuiteRequiredBenchmark.testsuite ( {\"specification\":\"6\"} )",
            "value": 5659.802560258452,
            "unit": "ops/s",
            "extra": "iterations: 3\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.networknt.schema.benchmark.NetworkntTestSuiteRequiredBenchmark.testsuite ( {\"specification\":\"4\"} )",
            "value": 8915.348409970786,
            "unit": "ops/s",
            "extra": "iterations: 3\nforks: 1\nthreads: 1"
          }
        ]
      },
      {
        "commit": {
          "author": {
            "email": "stevehu@gmail.com",
            "name": "Steve Hu",
            "username": "stevehu"
          },
          "committer": {
            "email": "stevehu@gmail.com",
            "name": "Steve Hu",
            "username": "stevehu"
          },
          "distinct": true,
          "id": "759445b6d613e591d19c37b3e715c34cb2b69865",
          "message": "upgrade slf4j to 2.0.19 from 2.0.17",
          "timestamp": "2026-09-28T13:37:02-04:00",
          "tree_id": "98c0ae2712b5db12eeca7ed99f4cfc30e3f86a04",
          "url": "https://github.com/networknt/json-schema-validator/commit/759445b6d613e591d19c37b3e715c34cb2b69865"
        },
        "date": 1790617420578,
        "tool": "jmh",
        "benches": [
          {
            "name": "com.networknt.schema.benchmark.NetworkntBenchmark.basic",
            "value": 4706.122396027996,
            "unit": "ops/s",
            "extra": "iterations: 3\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.networknt.schema.benchmark.NetworkntTestSuiteOptionalBenchmark.testsuite ( {\"specification\":\"2020-12\"} )",
            "value": 1217.8512477752745,
            "unit": "ops/s",
            "extra": "iterations: 3\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.networknt.schema.benchmark.NetworkntTestSuiteOptionalBenchmark.testsuite ( {\"specification\":\"2019-09\"} )",
            "value": 1255.1161677482887,
            "unit": "ops/s",
            "extra": "iterations: 3\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.networknt.schema.benchmark.NetworkntTestSuiteOptionalBenchmark.testsuite ( {\"specification\":\"7\"} )",
            "value": 1327.4595418265767,
            "unit": "ops/s",
            "extra": "iterations: 3\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.networknt.schema.benchmark.NetworkntTestSuiteOptionalBenchmark.testsuite ( {\"specification\":\"6\"} )",
            "value": 2989.5436128906535,
            "unit": "ops/s",
            "extra": "iterations: 3\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.networknt.schema.benchmark.NetworkntTestSuiteOptionalBenchmark.testsuite ( {\"specification\":\"4\"} )",
            "value": 3751.288724529562,
            "unit": "ops/s",
            "extra": "iterations: 3\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.networknt.schema.benchmark.NetworkntTestSuiteRequiredBenchmark.testsuite ( {\"specification\":\"2020-12\"} )",
            "value": 1036.1446266066866,
            "unit": "ops/s",
            "extra": "iterations: 3\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.networknt.schema.benchmark.NetworkntTestSuiteRequiredBenchmark.testsuite ( {\"specification\":\"2019-09\"} )",
            "value": 1113.5336166772167,
            "unit": "ops/s",
            "extra": "iterations: 3\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.networknt.schema.benchmark.NetworkntTestSuiteRequiredBenchmark.testsuite ( {\"specification\":\"7\"} )",
            "value": 2945.227934291815,
            "unit": "ops/s",
            "extra": "iterations: 3\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.networknt.schema.benchmark.NetworkntTestSuiteRequiredBenchmark.testsuite ( {\"specification\":\"6\"} )",
            "value": 3228.0615534126823,
            "unit": "ops/s",
            "extra": "iterations: 3\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.networknt.schema.benchmark.NetworkntTestSuiteRequiredBenchmark.testsuite ( {\"specification\":\"4\"} )",
            "value": 4572.132073636443,
            "unit": "ops/s",
            "extra": "iterations: 3\nforks: 1\nthreads: 1"
          }
        ]
      },
      {
        "commit": {
          "author": {
            "email": "stevehu@gmail.com",
            "name": "Steve Hu",
            "username": "stevehu"
          },
          "committer": {
            "email": "stevehu@gmail.com",
            "name": "Steve Hu",
            "username": "stevehu"
          },
          "distinct": true,
          "id": "12596e6c94c3f41804356c9b7664c374e85abd07",
          "message": "upgrade to 3.0.8 and update changelog",
          "timestamp": "2026-09-30T08:44:38-04:00",
          "tree_id": "8f22a2c1d2c9a350d5238adca65e41ada79f930f",
          "url": "https://github.com/networknt/json-schema-validator/commit/12596e6c94c3f41804356c9b7664c374e85abd07"
        },
        "date": 1790772693828,
        "tool": "jmh",
        "benches": [
          {
            "name": "com.networknt.schema.benchmark.NetworkntBenchmark.basic",
            "value": 4691.398025039242,
            "unit": "ops/s",
            "extra": "iterations: 3\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.networknt.schema.benchmark.NetworkntTestSuiteOptionalBenchmark.testsuite ( {\"specification\":\"2020-12\"} )",
            "value": 1209.6028117710612,
            "unit": "ops/s",
            "extra": "iterations: 3\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.networknt.schema.benchmark.NetworkntTestSuiteOptionalBenchmark.testsuite ( {\"specification\":\"2019-09\"} )",
            "value": 1258.8638629643851,
            "unit": "ops/s",
            "extra": "iterations: 3\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.networknt.schema.benchmark.NetworkntTestSuiteOptionalBenchmark.testsuite ( {\"specification\":\"7\"} )",
            "value": 1309.8686865443306,
            "unit": "ops/s",
            "extra": "iterations: 3\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.networknt.schema.benchmark.NetworkntTestSuiteOptionalBenchmark.testsuite ( {\"specification\":\"6\"} )",
            "value": 3031.9882472383665,
            "unit": "ops/s",
            "extra": "iterations: 3\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.networknt.schema.benchmark.NetworkntTestSuiteOptionalBenchmark.testsuite ( {\"specification\":\"4\"} )",
            "value": 3817.4490149285543,
            "unit": "ops/s",
            "extra": "iterations: 3\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.networknt.schema.benchmark.NetworkntTestSuiteRequiredBenchmark.testsuite ( {\"specification\":\"2020-12\"} )",
            "value": 1018.0558686595526,
            "unit": "ops/s",
            "extra": "iterations: 3\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.networknt.schema.benchmark.NetworkntTestSuiteRequiredBenchmark.testsuite ( {\"specification\":\"2019-09\"} )",
            "value": 1104.0432196645363,
            "unit": "ops/s",
            "extra": "iterations: 3\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.networknt.schema.benchmark.NetworkntTestSuiteRequiredBenchmark.testsuite ( {\"specification\":\"7\"} )",
            "value": 2875.242429747041,
            "unit": "ops/s",
            "extra": "iterations: 3\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.networknt.schema.benchmark.NetworkntTestSuiteRequiredBenchmark.testsuite ( {\"specification\":\"6\"} )",
            "value": 3139.771945164106,
            "unit": "ops/s",
            "extra": "iterations: 3\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.networknt.schema.benchmark.NetworkntTestSuiteRequiredBenchmark.testsuite ( {\"specification\":\"4\"} )",
            "value": 4569.212165646072,
            "unit": "ops/s",
            "extra": "iterations: 3\nforks: 1\nthreads: 1"
          }
        ]
      },
      {
        "commit": {
          "author": {
            "email": "stevehu@gmail.com",
            "name": "Steve Hu",
            "username": "stevehu"
          },
          "committer": {
            "email": "stevehu@gmail.com",
            "name": "Steve Hu",
            "username": "stevehu"
          },
          "distinct": true,
          "id": "231c34bf99b5f0c9c3f59a2bc6d9f43d1d0cac71",
          "message": "fixes #1285 upgrade jackson to 3.2.3 from 3.2.1",
          "timestamp": "2026-09-30T16:29:50-04:00",
          "tree_id": "31ccad2376c90ee103b3efda07dc8f488b18c522",
          "url": "https://github.com/networknt/json-schema-validator/commit/231c34bf99b5f0c9c3f59a2bc6d9f43d1d0cac71"
        },
        "date": 1790800588623,
        "tool": "jmh",
        "benches": [
          {
            "name": "com.networknt.schema.benchmark.NetworkntBenchmark.basic",
            "value": 4719.298067359864,
            "unit": "ops/s",
            "extra": "iterations: 3\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.networknt.schema.benchmark.NetworkntTestSuiteOptionalBenchmark.testsuite ( {\"specification\":\"2020-12\"} )",
            "value": 1202.6985639615532,
            "unit": "ops/s",
            "extra": "iterations: 3\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.networknt.schema.benchmark.NetworkntTestSuiteOptionalBenchmark.testsuite ( {\"specification\":\"2019-09\"} )",
            "value": 1264.7387400590424,
            "unit": "ops/s",
            "extra": "iterations: 3\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.networknt.schema.benchmark.NetworkntTestSuiteOptionalBenchmark.testsuite ( {\"specification\":\"7\"} )",
            "value": 1319.137075263269,
            "unit": "ops/s",
            "extra": "iterations: 3\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.networknt.schema.benchmark.NetworkntTestSuiteOptionalBenchmark.testsuite ( {\"specification\":\"6\"} )",
            "value": 3079.581422372243,
            "unit": "ops/s",
            "extra": "iterations: 3\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.networknt.schema.benchmark.NetworkntTestSuiteOptionalBenchmark.testsuite ( {\"specification\":\"4\"} )",
            "value": 3797.714119132159,
            "unit": "ops/s",
            "extra": "iterations: 3\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.networknt.schema.benchmark.NetworkntTestSuiteRequiredBenchmark.testsuite ( {\"specification\":\"2020-12\"} )",
            "value": 1042.3632502236608,
            "unit": "ops/s",
            "extra": "iterations: 3\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.networknt.schema.benchmark.NetworkntTestSuiteRequiredBenchmark.testsuite ( {\"specification\":\"2019-09\"} )",
            "value": 1126.0449645428046,
            "unit": "ops/s",
            "extra": "iterations: 3\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.networknt.schema.benchmark.NetworkntTestSuiteRequiredBenchmark.testsuite ( {\"specification\":\"7\"} )",
            "value": 2962.4298692798197,
            "unit": "ops/s",
            "extra": "iterations: 3\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.networknt.schema.benchmark.NetworkntTestSuiteRequiredBenchmark.testsuite ( {\"specification\":\"6\"} )",
            "value": 3211.269651015167,
            "unit": "ops/s",
            "extra": "iterations: 3\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.networknt.schema.benchmark.NetworkntTestSuiteRequiredBenchmark.testsuite ( {\"specification\":\"4\"} )",
            "value": 4538.220911018525,
            "unit": "ops/s",
            "extra": "iterations: 3\nforks: 1\nthreads: 1"
          }
        ]
      },
      {
        "commit": {
          "author": {
            "email": "stevehu@gmail.com",
            "name": "Steve Hu",
            "username": "stevehu"
          },
          "committer": {
            "email": "stevehu@gmail.com",
            "name": "Steve Hu",
            "username": "stevehu"
          },
          "distinct": true,
          "id": "95825f33f76b533cea43362d50f27e3f9c22105b",
          "message": "upgrade slf4j to 2.0.20 from 2.0.19",
          "timestamp": "2026-10-02T13:27:30-04:00",
          "tree_id": "31d8845ecb66f6fafabf7ac87c213ab967d3e409",
          "url": "https://github.com/networknt/json-schema-validator/commit/95825f33f76b533cea43362d50f27e3f9c22105b"
        },
        "date": 1790962438201,
        "tool": "jmh",
        "benches": [
          {
            "name": "com.networknt.schema.benchmark.NetworkntBenchmark.basic",
            "value": 5705.221814070082,
            "unit": "ops/s",
            "extra": "iterations: 3\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.networknt.schema.benchmark.NetworkntTestSuiteOptionalBenchmark.testsuite ( {\"specification\":\"2020-12\"} )",
            "value": 1642.0467078403237,
            "unit": "ops/s",
            "extra": "iterations: 3\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.networknt.schema.benchmark.NetworkntTestSuiteOptionalBenchmark.testsuite ( {\"specification\":\"2019-09\"} )",
            "value": 1717.5707892007765,
            "unit": "ops/s",
            "extra": "iterations: 3\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.networknt.schema.benchmark.NetworkntTestSuiteOptionalBenchmark.testsuite ( {\"specification\":\"7\"} )",
            "value": 1838.6350456942007,
            "unit": "ops/s",
            "extra": "iterations: 3\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.networknt.schema.benchmark.NetworkntTestSuiteOptionalBenchmark.testsuite ( {\"specification\":\"6\"} )",
            "value": 3992.4231962976605,
            "unit": "ops/s",
            "extra": "iterations: 3\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.networknt.schema.benchmark.NetworkntTestSuiteOptionalBenchmark.testsuite ( {\"specification\":\"4\"} )",
            "value": 5042.271088845551,
            "unit": "ops/s",
            "extra": "iterations: 3\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.networknt.schema.benchmark.NetworkntTestSuiteRequiredBenchmark.testsuite ( {\"specification\":\"2020-12\"} )",
            "value": 1393.5006169265114,
            "unit": "ops/s",
            "extra": "iterations: 3\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.networknt.schema.benchmark.NetworkntTestSuiteRequiredBenchmark.testsuite ( {\"specification\":\"2019-09\"} )",
            "value": 1472.3022044084219,
            "unit": "ops/s",
            "extra": "iterations: 3\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.networknt.schema.benchmark.NetworkntTestSuiteRequiredBenchmark.testsuite ( {\"specification\":\"7\"} )",
            "value": 3878.993135240538,
            "unit": "ops/s",
            "extra": "iterations: 3\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.networknt.schema.benchmark.NetworkntTestSuiteRequiredBenchmark.testsuite ( {\"specification\":\"6\"} )",
            "value": 4208.605221346313,
            "unit": "ops/s",
            "extra": "iterations: 3\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.networknt.schema.benchmark.NetworkntTestSuiteRequiredBenchmark.testsuite ( {\"specification\":\"4\"} )",
            "value": 5978.844796812086,
            "unit": "ops/s",
            "extra": "iterations: 3\nforks: 1\nthreads: 1"
          }
        ]
      },
      {
        "commit": {
          "author": {
            "email": "stevehu@gmail.com",
            "name": "Steve Hu",
            "username": "stevehu"
          },
          "committer": {
            "email": "noreply@github.com",
            "name": "GitHub",
            "username": "web-flow"
          },
          "distinct": true,
          "id": "51dab248335c4e738652b7c88d5ed7b6bf9fb71c",
          "message": "Document and characterize recursive applicator cost (#1276) (#1287)",
          "timestamp": "2026-10-05T12:52:53-04:00",
          "tree_id": "f20ba5a78745812bfbafd3cd5317bc7de756e8db",
          "url": "https://github.com/networknt/json-schema-validator/commit/51dab248335c4e738652b7c88d5ed7b6bf9fb71c"
        },
        "date": 1791219559049,
        "tool": "jmh",
        "benches": [
          {
            "name": "com.networknt.schema.benchmark.NetworkntBenchmark.basic",
            "value": 4759.112296388736,
            "unit": "ops/s",
            "extra": "iterations: 3\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.networknt.schema.benchmark.NetworkntTestSuiteOptionalBenchmark.testsuite ( {\"specification\":\"2020-12\"} )",
            "value": 1222.559365997559,
            "unit": "ops/s",
            "extra": "iterations: 3\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.networknt.schema.benchmark.NetworkntTestSuiteOptionalBenchmark.testsuite ( {\"specification\":\"2019-09\"} )",
            "value": 1263.2918354241228,
            "unit": "ops/s",
            "extra": "iterations: 3\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.networknt.schema.benchmark.NetworkntTestSuiteOptionalBenchmark.testsuite ( {\"specification\":\"7\"} )",
            "value": 1318.3090212073275,
            "unit": "ops/s",
            "extra": "iterations: 3\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.networknt.schema.benchmark.NetworkntTestSuiteOptionalBenchmark.testsuite ( {\"specification\":\"6\"} )",
            "value": 3041.7903626282236,
            "unit": "ops/s",
            "extra": "iterations: 3\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.networknt.schema.benchmark.NetworkntTestSuiteOptionalBenchmark.testsuite ( {\"specification\":\"4\"} )",
            "value": 3704.7672766281867,
            "unit": "ops/s",
            "extra": "iterations: 3\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.networknt.schema.benchmark.NetworkntTestSuiteRequiredBenchmark.testsuite ( {\"specification\":\"2020-12\"} )",
            "value": 1024.8116523611716,
            "unit": "ops/s",
            "extra": "iterations: 3\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.networknt.schema.benchmark.NetworkntTestSuiteRequiredBenchmark.testsuite ( {\"specification\":\"2019-09\"} )",
            "value": 1105.4764399688868,
            "unit": "ops/s",
            "extra": "iterations: 3\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.networknt.schema.benchmark.NetworkntTestSuiteRequiredBenchmark.testsuite ( {\"specification\":\"7\"} )",
            "value": 3017.1359094951026,
            "unit": "ops/s",
            "extra": "iterations: 3\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.networknt.schema.benchmark.NetworkntTestSuiteRequiredBenchmark.testsuite ( {\"specification\":\"6\"} )",
            "value": 3179.89273303892,
            "unit": "ops/s",
            "extra": "iterations: 3\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.networknt.schema.benchmark.NetworkntTestSuiteRequiredBenchmark.testsuite ( {\"specification\":\"4\"} )",
            "value": 4512.815153031559,
            "unit": "ops/s",
            "extra": "iterations: 3\nforks: 1\nthreads: 1"
          }
        ]
      },
      {
        "commit": {
          "author": {
            "email": "stevehu@gmail.com",
            "name": "Steve Hu",
            "username": "stevehu"
          },
          "committer": {
            "email": "noreply@github.com",
            "name": "GitHub",
            "username": "web-flow"
          },
          "distinct": true,
          "id": "448c3d24d507dfb0898e53b034ab8b04aaf3a460",
          "message": "Add opt-in validation execution limits (#1289)\n\n* Add opt-in validation execution limits\n\n* Fix execution-limit review regressions and document compatibility",
          "timestamp": "2026-10-05T14:36:52-04:00",
          "tree_id": "0c7ef1be00133d1eb09961337d4bfd5b14e55909",
          "url": "https://github.com/networknt/json-schema-validator/commit/448c3d24d507dfb0898e53b034ab8b04aaf3a460"
        },
        "date": 1791225784821,
        "tool": "jmh",
        "benches": [
          {
            "name": "com.networknt.schema.benchmark.NetworkntBenchmark.basic",
            "value": 6596.08925691533,
            "unit": "ops/s",
            "extra": "iterations: 3\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.networknt.schema.benchmark.NetworkntTestSuiteOptionalBenchmark.testsuite ( {\"specification\":\"2020-12\"} )",
            "value": 1447.5786918318743,
            "unit": "ops/s",
            "extra": "iterations: 3\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.networknt.schema.benchmark.NetworkntTestSuiteOptionalBenchmark.testsuite ( {\"specification\":\"2019-09\"} )",
            "value": 1522.8079453858682,
            "unit": "ops/s",
            "extra": "iterations: 3\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.networknt.schema.benchmark.NetworkntTestSuiteOptionalBenchmark.testsuite ( {\"specification\":\"7\"} )",
            "value": 1620.338905103009,
            "unit": "ops/s",
            "extra": "iterations: 3\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.networknt.schema.benchmark.NetworkntTestSuiteOptionalBenchmark.testsuite ( {\"specification\":\"6\"} )",
            "value": 3900.888287080475,
            "unit": "ops/s",
            "extra": "iterations: 3\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.networknt.schema.benchmark.NetworkntTestSuiteOptionalBenchmark.testsuite ( {\"specification\":\"4\"} )",
            "value": 4856.035210503166,
            "unit": "ops/s",
            "extra": "iterations: 3\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.networknt.schema.benchmark.NetworkntTestSuiteRequiredBenchmark.testsuite ( {\"specification\":\"2020-12\"} )",
            "value": 1113.3880299038249,
            "unit": "ops/s",
            "extra": "iterations: 3\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.networknt.schema.benchmark.NetworkntTestSuiteRequiredBenchmark.testsuite ( {\"specification\":\"2019-09\"} )",
            "value": 1205.7319431236065,
            "unit": "ops/s",
            "extra": "iterations: 3\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.networknt.schema.benchmark.NetworkntTestSuiteRequiredBenchmark.testsuite ( {\"specification\":\"7\"} )",
            "value": 3336.32977088676,
            "unit": "ops/s",
            "extra": "iterations: 3\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.networknt.schema.benchmark.NetworkntTestSuiteRequiredBenchmark.testsuite ( {\"specification\":\"6\"} )",
            "value": 3753.915433489748,
            "unit": "ops/s",
            "extra": "iterations: 3\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.networknt.schema.benchmark.NetworkntTestSuiteRequiredBenchmark.testsuite ( {\"specification\":\"4\"} )",
            "value": 5966.649829959974,
            "unit": "ops/s",
            "extra": "iterations: 3\nforks: 1\nthreads: 1"
          }
        ]
      },
      {
        "commit": {
          "author": {
            "email": "stevehu@gmail.com",
            "name": "Steve Hu",
            "username": "stevehu"
          },
          "committer": {
            "email": "stevehu@gmail.com",
            "name": "Steve Hu",
            "username": "stevehu"
          },
          "distinct": true,
          "id": "83905ab3d056218103c72d5b6a3eab8a8ecf5591",
          "message": "upgrade logback to 1.6.5 from 1.6.3",
          "timestamp": "2026-10-08T15:18:31-04:00",
          "tree_id": "bacd52d62442441e6c0f6f8e79b1ab247d854459",
          "url": "https://github.com/networknt/json-schema-validator/commit/83905ab3d056218103c72d5b6a3eab8a8ecf5591"
        },
        "date": 1791487496667,
        "tool": "jmh",
        "benches": [
          {
            "name": "com.networknt.schema.benchmark.NetworkntBenchmark.basic",
            "value": 5286.842887921209,
            "unit": "ops/s",
            "extra": "iterations: 3\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.networknt.schema.benchmark.NetworkntTestSuiteOptionalBenchmark.testsuite ( {\"specification\":\"2020-12\"} )",
            "value": 1167.6668560425578,
            "unit": "ops/s",
            "extra": "iterations: 3\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.networknt.schema.benchmark.NetworkntTestSuiteOptionalBenchmark.testsuite ( {\"specification\":\"2019-09\"} )",
            "value": 1249.8328551029326,
            "unit": "ops/s",
            "extra": "iterations: 3\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.networknt.schema.benchmark.NetworkntTestSuiteOptionalBenchmark.testsuite ( {\"specification\":\"7\"} )",
            "value": 1269.0790169214695,
            "unit": "ops/s",
            "extra": "iterations: 3\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.networknt.schema.benchmark.NetworkntTestSuiteOptionalBenchmark.testsuite ( {\"specification\":\"6\"} )",
            "value": 2906.3942424993147,
            "unit": "ops/s",
            "extra": "iterations: 3\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.networknt.schema.benchmark.NetworkntTestSuiteOptionalBenchmark.testsuite ( {\"specification\":\"4\"} )",
            "value": 3675.4197600256575,
            "unit": "ops/s",
            "extra": "iterations: 3\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.networknt.schema.benchmark.NetworkntTestSuiteRequiredBenchmark.testsuite ( {\"specification\":\"2020-12\"} )",
            "value": 1001.8736675922404,
            "unit": "ops/s",
            "extra": "iterations: 3\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.networknt.schema.benchmark.NetworkntTestSuiteRequiredBenchmark.testsuite ( {\"specification\":\"2019-09\"} )",
            "value": 1093.1936513723622,
            "unit": "ops/s",
            "extra": "iterations: 3\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.networknt.schema.benchmark.NetworkntTestSuiteRequiredBenchmark.testsuite ( {\"specification\":\"7\"} )",
            "value": 2758.56034154708,
            "unit": "ops/s",
            "extra": "iterations: 3\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.networknt.schema.benchmark.NetworkntTestSuiteRequiredBenchmark.testsuite ( {\"specification\":\"6\"} )",
            "value": 2993.502430274226,
            "unit": "ops/s",
            "extra": "iterations: 3\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.networknt.schema.benchmark.NetworkntTestSuiteRequiredBenchmark.testsuite ( {\"specification\":\"4\"} )",
            "value": 4300.155192419324,
            "unit": "ops/s",
            "extra": "iterations: 3\nforks: 1\nthreads: 1"
          }
        ]
      },
      {
        "commit": {
          "author": {
            "email": "stevehu@gmail.com",
            "name": "Steve Hu",
            "username": "stevehu"
          },
          "committer": {
            "email": "stevehu@gmail.com",
            "name": "Steve Hu",
            "username": "stevehu"
          },
          "distinct": true,
          "id": "f2d22d157b7867dc5e0ef61c8666f657b71ac036",
          "message": "build: upgrade JUnit to 6.1.3\n\nAlign JUnit artifacts through the 6.1.3 BOM.",
          "timestamp": "2026-10-08T21:18:07-04:00",
          "tree_id": "3a5e4061840d04293d7b1fc518830a8e99294662",
          "url": "https://github.com/networknt/json-schema-validator/commit/f2d22d157b7867dc5e0ef61c8666f657b71ac036"
        },
        "date": 1791509207800,
        "tool": "jmh",
        "benches": [
          {
            "name": "com.networknt.schema.benchmark.NetworkntBenchmark.basic",
            "value": 5792.595732797373,
            "unit": "ops/s",
            "extra": "iterations: 3\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.networknt.schema.benchmark.NetworkntTestSuiteOptionalBenchmark.testsuite ( {\"specification\":\"2020-12\"} )",
            "value": 1278.2907434676606,
            "unit": "ops/s",
            "extra": "iterations: 3\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.networknt.schema.benchmark.NetworkntTestSuiteOptionalBenchmark.testsuite ( {\"specification\":\"2019-09\"} )",
            "value": 1348.4785157643623,
            "unit": "ops/s",
            "extra": "iterations: 3\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.networknt.schema.benchmark.NetworkntTestSuiteOptionalBenchmark.testsuite ( {\"specification\":\"7\"} )",
            "value": 1413.8641202653853,
            "unit": "ops/s",
            "extra": "iterations: 3\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.networknt.schema.benchmark.NetworkntTestSuiteOptionalBenchmark.testsuite ( {\"specification\":\"6\"} )",
            "value": 3488.9876806957413,
            "unit": "ops/s",
            "extra": "iterations: 3\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.networknt.schema.benchmark.NetworkntTestSuiteOptionalBenchmark.testsuite ( {\"specification\":\"4\"} )",
            "value": 4111.592155848378,
            "unit": "ops/s",
            "extra": "iterations: 3\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.networknt.schema.benchmark.NetworkntTestSuiteRequiredBenchmark.testsuite ( {\"specification\":\"2020-12\"} )",
            "value": 1012.2142486994126,
            "unit": "ops/s",
            "extra": "iterations: 3\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.networknt.schema.benchmark.NetworkntTestSuiteRequiredBenchmark.testsuite ( {\"specification\":\"2019-09\"} )",
            "value": 1112.7764584404888,
            "unit": "ops/s",
            "extra": "iterations: 3\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.networknt.schema.benchmark.NetworkntTestSuiteRequiredBenchmark.testsuite ( {\"specification\":\"7\"} )",
            "value": 3053.5618061485807,
            "unit": "ops/s",
            "extra": "iterations: 3\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.networknt.schema.benchmark.NetworkntTestSuiteRequiredBenchmark.testsuite ( {\"specification\":\"6\"} )",
            "value": 3351.8902634506935,
            "unit": "ops/s",
            "extra": "iterations: 3\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.networknt.schema.benchmark.NetworkntTestSuiteRequiredBenchmark.testsuite ( {\"specification\":\"4\"} )",
            "value": 5227.963635335004,
            "unit": "ops/s",
            "extra": "iterations: 3\nforks: 1\nthreads: 1"
          }
        ]
      },
      {
        "commit": {
          "author": {
            "email": "stevehu@gmail.com",
            "name": "Steve Hu",
            "username": "stevehu"
          },
          "committer": {
            "email": "stevehu@gmail.com",
            "name": "Steve Hu",
            "username": "stevehu"
          },
          "distinct": true,
          "id": "55153152bdabb41ff55d77f3edc6a6c11a6e3d90",
          "message": "upgrade maven-gpg to 3.2.8 from 3.2.7",
          "timestamp": "2026-10-09T12:00:40-04:00",
          "tree_id": "017770699c8b1b7b5b577131c00b6b2862f2402b",
          "url": "https://github.com/networknt/json-schema-validator/commit/55153152bdabb41ff55d77f3edc6a6c11a6e3d90"
        },
        "date": 1791562029268,
        "tool": "jmh",
        "benches": [
          {
            "name": "com.networknt.schema.benchmark.NetworkntBenchmark.basic",
            "value": 5322.031951054387,
            "unit": "ops/s",
            "extra": "iterations: 3\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.networknt.schema.benchmark.NetworkntTestSuiteOptionalBenchmark.testsuite ( {\"specification\":\"2020-12\"} )",
            "value": 1217.7113183268075,
            "unit": "ops/s",
            "extra": "iterations: 3\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.networknt.schema.benchmark.NetworkntTestSuiteOptionalBenchmark.testsuite ( {\"specification\":\"2019-09\"} )",
            "value": 1233.9453430051326,
            "unit": "ops/s",
            "extra": "iterations: 3\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.networknt.schema.benchmark.NetworkntTestSuiteOptionalBenchmark.testsuite ( {\"specification\":\"7\"} )",
            "value": 1292.9408220313624,
            "unit": "ops/s",
            "extra": "iterations: 3\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.networknt.schema.benchmark.NetworkntTestSuiteOptionalBenchmark.testsuite ( {\"specification\":\"6\"} )",
            "value": 2953.770867602782,
            "unit": "ops/s",
            "extra": "iterations: 3\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.networknt.schema.benchmark.NetworkntTestSuiteOptionalBenchmark.testsuite ( {\"specification\":\"4\"} )",
            "value": 3663.156218732786,
            "unit": "ops/s",
            "extra": "iterations: 3\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.networknt.schema.benchmark.NetworkntTestSuiteRequiredBenchmark.testsuite ( {\"specification\":\"2020-12\"} )",
            "value": 1015.7887257754106,
            "unit": "ops/s",
            "extra": "iterations: 3\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.networknt.schema.benchmark.NetworkntTestSuiteRequiredBenchmark.testsuite ( {\"specification\":\"2019-09\"} )",
            "value": 1090.153601239839,
            "unit": "ops/s",
            "extra": "iterations: 3\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.networknt.schema.benchmark.NetworkntTestSuiteRequiredBenchmark.testsuite ( {\"specification\":\"7\"} )",
            "value": 2750.782280968108,
            "unit": "ops/s",
            "extra": "iterations: 3\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.networknt.schema.benchmark.NetworkntTestSuiteRequiredBenchmark.testsuite ( {\"specification\":\"6\"} )",
            "value": 3089.56659929239,
            "unit": "ops/s",
            "extra": "iterations: 3\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.networknt.schema.benchmark.NetworkntTestSuiteRequiredBenchmark.testsuite ( {\"specification\":\"4\"} )",
            "value": 4292.877118437685,
            "unit": "ops/s",
            "extra": "iterations: 3\nforks: 1\nthreads: 1"
          }
        ]
      },
      {
        "commit": {
          "author": {
            "email": "105151063+PHJ2000@users.noreply.github.com",
            "name": "박재홍",
            "username": "PHJ2000"
          },
          "committer": {
            "email": "noreply@github.com",
            "name": "GitHub",
            "username": "web-flow"
          },
          "distinct": true,
          "id": "45c1fc65c113b51bd9ec693bc9b7193e0739f56c",
          "message": "Fix uniqueItems validation for non-array inputs (#1297)\n\n* Fix uniqueItems validation for non-array inputs\n\n* Refine uniqueItems applicability tests",
          "timestamp": "2026-10-11T23:02:14-04:00",
          "tree_id": "cc447af624f5d7ab04ee7459cf8210f1a0fff3a3",
          "url": "https://github.com/networknt/json-schema-validator/commit/45c1fc65c113b51bd9ec693bc9b7193e0739f56c"
        },
        "date": 1791774510848,
        "tool": "jmh",
        "benches": [
          {
            "name": "com.networknt.schema.benchmark.NetworkntBenchmark.basic",
            "value": 6628.031652721163,
            "unit": "ops/s",
            "extra": "iterations: 3\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.networknt.schema.benchmark.NetworkntTestSuiteOptionalBenchmark.testsuite ( {\"specification\":\"2020-12\"} )",
            "value": 1614.7602542526931,
            "unit": "ops/s",
            "extra": "iterations: 3\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.networknt.schema.benchmark.NetworkntTestSuiteOptionalBenchmark.testsuite ( {\"specification\":\"2019-09\"} )",
            "value": 1708.7054113474687,
            "unit": "ops/s",
            "extra": "iterations: 3\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.networknt.schema.benchmark.NetworkntTestSuiteOptionalBenchmark.testsuite ( {\"specification\":\"7\"} )",
            "value": 1748.0429822551935,
            "unit": "ops/s",
            "extra": "iterations: 3\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.networknt.schema.benchmark.NetworkntTestSuiteOptionalBenchmark.testsuite ( {\"specification\":\"6\"} )",
            "value": 3907.5125115899677,
            "unit": "ops/s",
            "extra": "iterations: 3\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.networknt.schema.benchmark.NetworkntTestSuiteOptionalBenchmark.testsuite ( {\"specification\":\"4\"} )",
            "value": 4983.536360138178,
            "unit": "ops/s",
            "extra": "iterations: 3\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.networknt.schema.benchmark.NetworkntTestSuiteRequiredBenchmark.testsuite ( {\"specification\":\"2020-12\"} )",
            "value": 1318.680416021746,
            "unit": "ops/s",
            "extra": "iterations: 3\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.networknt.schema.benchmark.NetworkntTestSuiteRequiredBenchmark.testsuite ( {\"specification\":\"2019-09\"} )",
            "value": 1403.8634824571557,
            "unit": "ops/s",
            "extra": "iterations: 3\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.networknt.schema.benchmark.NetworkntTestSuiteRequiredBenchmark.testsuite ( {\"specification\":\"7\"} )",
            "value": 3703.781405608092,
            "unit": "ops/s",
            "extra": "iterations: 3\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.networknt.schema.benchmark.NetworkntTestSuiteRequiredBenchmark.testsuite ( {\"specification\":\"6\"} )",
            "value": 3964.1725164894287,
            "unit": "ops/s",
            "extra": "iterations: 3\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.networknt.schema.benchmark.NetworkntTestSuiteRequiredBenchmark.testsuite ( {\"specification\":\"4\"} )",
            "value": 5781.072936562486,
            "unit": "ops/s",
            "extra": "iterations: 3\nforks: 1\nthreads: 1"
          }
        ]
      },
      {
        "commit": {
          "author": {
            "email": "147650395+ItsDeadlyProgrammer@users.noreply.github.com",
            "name": "harshuuuu",
            "username": "ItsDeadlyProgrammer"
          },
          "committer": {
            "email": "noreply@github.com",
            "name": "GitHub",
            "username": "web-flow"
          },
          "distinct": true,
          "id": "b3d25b8660a215c044694982e869577ef61c8139",
          "message": "Do not treat maxContains < minContains as a schema error (#1295)\n\n* Do not treat maxContains < minContains as a schema error\n\nThe specification requires minContains and maxContains to be\nnon-negative integers but does not require minContains <= maxContains.\nMinMaxContainsValidator reported contradictory bounds as an error for\nevery instance, including non-arrays and schemas without contains.\nContainsValidator already enforces both bounds on arrays, where\ncontradictory bounds can never be satisfied.\n\nFixes #1292\n\n* Report each malformed contains bound once, under its own keyword\n\nminContains and maxContains each create a MinMaxContainsValidator, but every\ninstance checked both values and was built as maxContains. A malformed bound\nnext to the other keyword was reported twice, and minContains errors had the\nevaluation path /maxContains. Each validator now checks only its own value and\nuses its own keyword.\n\nAlso remove the unused minContainsVsMaxContains message and the valid-schema\ncase left in invalid-min-max-contains.json, which issue1292.json already covers.\n\n* Pass the keyword to MinMaxContainsValidator explicitly\n\nEach of minContains and maxContains now builds the validator with its own\nKeywordType instead of inferring it from the schema location. The validator\nkeeps a single precomputed message argument in place of a one-element set,\nso the parent schema is no longer serialized on every validation.\n\nIssue1292Test now runs under Draft 2019-09 and 2020-12 and checks that walk\nlisteners fire once for each keyword.\n\n* Keep the 4-argument MinMaxContainsValidator constructor as deprecated",
          "timestamp": "2026-10-12T10:45:56-04:00",
          "tree_id": "5a182924e062731dfb5b2a83517c369db020dfe3",
          "url": "https://github.com/networknt/json-schema-validator/commit/b3d25b8660a215c044694982e869577ef61c8139"
        },
        "date": 1791816730203,
        "tool": "jmh",
        "benches": [
          {
            "name": "com.networknt.schema.benchmark.NetworkntBenchmark.basic",
            "value": 9585.418932199444,
            "unit": "ops/s",
            "extra": "iterations: 3\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.networknt.schema.benchmark.NetworkntTestSuiteOptionalBenchmark.testsuite ( {\"specification\":\"2020-12\"} )",
            "value": 2290.063289984804,
            "unit": "ops/s",
            "extra": "iterations: 3\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.networknt.schema.benchmark.NetworkntTestSuiteOptionalBenchmark.testsuite ( {\"specification\":\"2019-09\"} )",
            "value": 2413.147428643388,
            "unit": "ops/s",
            "extra": "iterations: 3\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.networknt.schema.benchmark.NetworkntTestSuiteOptionalBenchmark.testsuite ( {\"specification\":\"7\"} )",
            "value": 2567.155028519931,
            "unit": "ops/s",
            "extra": "iterations: 3\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.networknt.schema.benchmark.NetworkntTestSuiteOptionalBenchmark.testsuite ( {\"specification\":\"6\"} )",
            "value": 5497.187985932389,
            "unit": "ops/s",
            "extra": "iterations: 3\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.networknt.schema.benchmark.NetworkntTestSuiteOptionalBenchmark.testsuite ( {\"specification\":\"4\"} )",
            "value": 7149.125556558676,
            "unit": "ops/s",
            "extra": "iterations: 3\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.networknt.schema.benchmark.NetworkntTestSuiteRequiredBenchmark.testsuite ( {\"specification\":\"2020-12\"} )",
            "value": 1952.4707950844847,
            "unit": "ops/s",
            "extra": "iterations: 3\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.networknt.schema.benchmark.NetworkntTestSuiteRequiredBenchmark.testsuite ( {\"specification\":\"2019-09\"} )",
            "value": 2146.9604402603986,
            "unit": "ops/s",
            "extra": "iterations: 3\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.networknt.schema.benchmark.NetworkntTestSuiteRequiredBenchmark.testsuite ( {\"specification\":\"7\"} )",
            "value": 5615.420048139554,
            "unit": "ops/s",
            "extra": "iterations: 3\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.networknt.schema.benchmark.NetworkntTestSuiteRequiredBenchmark.testsuite ( {\"specification\":\"6\"} )",
            "value": 5794.9407343181265,
            "unit": "ops/s",
            "extra": "iterations: 3\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.networknt.schema.benchmark.NetworkntTestSuiteRequiredBenchmark.testsuite ( {\"specification\":\"4\"} )",
            "value": 8949.590532360617,
            "unit": "ops/s",
            "extra": "iterations: 3\nforks: 1\nthreads: 1"
          }
        ]
      },
      {
        "commit": {
          "author": {
            "email": "147650395+ItsDeadlyProgrammer@users.noreply.github.com",
            "name": "harshuuuu",
            "username": "ItsDeadlyProgrammer"
          },
          "committer": {
            "email": "noreply@github.com",
            "name": "GitHub",
            "username": "web-flow"
          },
          "distinct": true,
          "id": "b3e5a0938ae27504002216487fc161081a6350c2",
          "message": "Ignore minContains and maxContains in contains before Draft 2019-09 (#1303)\n\n* Ignore minContains and maxContains in contains before Draft 2019-09\n\n* Read minContains and maxContains when the dialect has them\n\nCheck the dialect's keywords instead of the spec version, so a custom\ndialect based on Draft 6 or 7 that adds the keywords still enforces\nthem. Replace the two Draft 6/7 tests that also passed without the fix,\nadd a test for such a dialect, and rename the Issue769ContainsTest Draft\n7 tests to say they cover a missing match.",
          "timestamp": "2026-10-12T12:52:49-04:00",
          "tree_id": "8a0a18dff2ae82fc24e8ce208c085838d98c0444",
          "url": "https://github.com/networknt/json-schema-validator/commit/b3e5a0938ae27504002216487fc161081a6350c2"
        },
        "date": 1791824363225,
        "tool": "jmh",
        "benches": [
          {
            "name": "com.networknt.schema.benchmark.NetworkntBenchmark.basic",
            "value": 6328.50695145979,
            "unit": "ops/s",
            "extra": "iterations: 3\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.networknt.schema.benchmark.NetworkntTestSuiteOptionalBenchmark.testsuite ( {\"specification\":\"2020-12\"} )",
            "value": 1397.7744317066124,
            "unit": "ops/s",
            "extra": "iterations: 3\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.networknt.schema.benchmark.NetworkntTestSuiteOptionalBenchmark.testsuite ( {\"specification\":\"2019-09\"} )",
            "value": 1430.210563934732,
            "unit": "ops/s",
            "extra": "iterations: 3\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.networknt.schema.benchmark.NetworkntTestSuiteOptionalBenchmark.testsuite ( {\"specification\":\"7\"} )",
            "value": 1523.6748488632682,
            "unit": "ops/s",
            "extra": "iterations: 3\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.networknt.schema.benchmark.NetworkntTestSuiteOptionalBenchmark.testsuite ( {\"specification\":\"6\"} )",
            "value": 3660.9932951108835,
            "unit": "ops/s",
            "extra": "iterations: 3\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.networknt.schema.benchmark.NetworkntTestSuiteOptionalBenchmark.testsuite ( {\"specification\":\"4\"} )",
            "value": 4414.38349567239,
            "unit": "ops/s",
            "extra": "iterations: 3\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.networknt.schema.benchmark.NetworkntTestSuiteRequiredBenchmark.testsuite ( {\"specification\":\"2020-12\"} )",
            "value": 1109.054082763804,
            "unit": "ops/s",
            "extra": "iterations: 3\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.networknt.schema.benchmark.NetworkntTestSuiteRequiredBenchmark.testsuite ( {\"specification\":\"2019-09\"} )",
            "value": 1185.3297131040479,
            "unit": "ops/s",
            "extra": "iterations: 3\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.networknt.schema.benchmark.NetworkntTestSuiteRequiredBenchmark.testsuite ( {\"specification\":\"7\"} )",
            "value": 3207.425393413332,
            "unit": "ops/s",
            "extra": "iterations: 3\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.networknt.schema.benchmark.NetworkntTestSuiteRequiredBenchmark.testsuite ( {\"specification\":\"6\"} )",
            "value": 3539.954260783496,
            "unit": "ops/s",
            "extra": "iterations: 3\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.networknt.schema.benchmark.NetworkntTestSuiteRequiredBenchmark.testsuite ( {\"specification\":\"4\"} )",
            "value": 5671.924028348563,
            "unit": "ops/s",
            "extra": "iterations: 3\nforks: 1\nthreads: 1"
          }
        ]
      },
      {
        "commit": {
          "author": {
            "email": "66793913+Shaisolaris@users.noreply.github.com",
            "name": "Shai",
            "username": "Shaisolaris"
          },
          "committer": {
            "email": "noreply@github.com",
            "name": "GitHub",
            "username": "web-flow"
          },
          "distinct": true,
          "id": "81cffd32ece515dff721cd83b846852897724f13",
          "message": "Reject trailing text in the time format (#1308)\n\n* Reject trailing text in the time format\n\nTimeFormat used parseUnresolved without checking how much of the value\nwas consumed, so \"12:00:00+01:00junk\" and \"12:00:00+01:00:00\" were\naccepted as a valid time. Require the parser to consume the whole\nvalue.\n\n* Require an exact +HH:MM offset in the time format for Java 8",
          "timestamp": "2026-10-12T23:56:14-04:00",
          "tree_id": "e8ad228fbfe99ee6cb76c216b6eaf1166bf62907",
          "url": "https://github.com/networknt/json-schema-validator/commit/81cffd32ece515dff721cd83b846852897724f13"
        },
        "date": 1791864154441,
        "tool": "jmh",
        "benches": [
          {
            "name": "com.networknt.schema.benchmark.NetworkntBenchmark.basic",
            "value": 5268.337460085736,
            "unit": "ops/s",
            "extra": "iterations: 3\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.networknt.schema.benchmark.NetworkntTestSuiteOptionalBenchmark.testsuite ( {\"specification\":\"2020-12\"} )",
            "value": 1201.0864905014146,
            "unit": "ops/s",
            "extra": "iterations: 3\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.networknt.schema.benchmark.NetworkntTestSuiteOptionalBenchmark.testsuite ( {\"specification\":\"2019-09\"} )",
            "value": 1241.4117146538904,
            "unit": "ops/s",
            "extra": "iterations: 3\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.networknt.schema.benchmark.NetworkntTestSuiteOptionalBenchmark.testsuite ( {\"specification\":\"7\"} )",
            "value": 1284.0664849134744,
            "unit": "ops/s",
            "extra": "iterations: 3\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.networknt.schema.benchmark.NetworkntTestSuiteOptionalBenchmark.testsuite ( {\"specification\":\"6\"} )",
            "value": 2922.52413170062,
            "unit": "ops/s",
            "extra": "iterations: 3\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.networknt.schema.benchmark.NetworkntTestSuiteOptionalBenchmark.testsuite ( {\"specification\":\"4\"} )",
            "value": 3626.6179621231154,
            "unit": "ops/s",
            "extra": "iterations: 3\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.networknt.schema.benchmark.NetworkntTestSuiteRequiredBenchmark.testsuite ( {\"specification\":\"2020-12\"} )",
            "value": 1015.4757067324998,
            "unit": "ops/s",
            "extra": "iterations: 3\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.networknt.schema.benchmark.NetworkntTestSuiteRequiredBenchmark.testsuite ( {\"specification\":\"2019-09\"} )",
            "value": 1104.5391378282554,
            "unit": "ops/s",
            "extra": "iterations: 3\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.networknt.schema.benchmark.NetworkntTestSuiteRequiredBenchmark.testsuite ( {\"specification\":\"7\"} )",
            "value": 2770.76581469644,
            "unit": "ops/s",
            "extra": "iterations: 3\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.networknt.schema.benchmark.NetworkntTestSuiteRequiredBenchmark.testsuite ( {\"specification\":\"6\"} )",
            "value": 2983.893510783677,
            "unit": "ops/s",
            "extra": "iterations: 3\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.networknt.schema.benchmark.NetworkntTestSuiteRequiredBenchmark.testsuite ( {\"specification\":\"4\"} )",
            "value": 4201.249707474634,
            "unit": "ops/s",
            "extra": "iterations: 3\nforks: 1\nthreads: 1"
          }
        ]
      },
      {
        "commit": {
          "author": {
            "email": "102353277+MoChiUaena@users.noreply.github.com",
            "name": "MoChiUaena",
            "username": "MoChiUaena"
          },
          "committer": {
            "email": "noreply@github.com",
            "name": "GitHub",
            "username": "web-flow"
          },
          "distinct": true,
          "id": "eaf4611ef79402bd03c7685ca39e45a67a2d0f3a",
          "message": "Validate fractional seconds in time format (#1314)\n\nSigned-off-by: dlwlrma <965810157@qq.com>",
          "timestamp": "2026-10-13T08:02:17-04:00",
          "tree_id": "cc521078cd3fe4f77e6722d08deb9bfed1617402",
          "url": "https://github.com/networknt/json-schema-validator/commit/eaf4611ef79402bd03c7685ca39e45a67a2d0f3a"
        },
        "date": 1791893318949,
        "tool": "jmh",
        "benches": [
          {
            "name": "com.networknt.schema.benchmark.NetworkntBenchmark.basic",
            "value": 5238.24218985604,
            "unit": "ops/s",
            "extra": "iterations: 3\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.networknt.schema.benchmark.NetworkntTestSuiteOptionalBenchmark.testsuite ( {\"specification\":\"2020-12\"} )",
            "value": 1173.2976645330427,
            "unit": "ops/s",
            "extra": "iterations: 3\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.networknt.schema.benchmark.NetworkntTestSuiteOptionalBenchmark.testsuite ( {\"specification\":\"2019-09\"} )",
            "value": 1238.607382326941,
            "unit": "ops/s",
            "extra": "iterations: 3\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.networknt.schema.benchmark.NetworkntTestSuiteOptionalBenchmark.testsuite ( {\"specification\":\"7\"} )",
            "value": 1326.226782915218,
            "unit": "ops/s",
            "extra": "iterations: 3\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.networknt.schema.benchmark.NetworkntTestSuiteOptionalBenchmark.testsuite ( {\"specification\":\"6\"} )",
            "value": 2929.771272959962,
            "unit": "ops/s",
            "extra": "iterations: 3\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.networknt.schema.benchmark.NetworkntTestSuiteOptionalBenchmark.testsuite ( {\"specification\":\"4\"} )",
            "value": 3705.283370581216,
            "unit": "ops/s",
            "extra": "iterations: 3\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.networknt.schema.benchmark.NetworkntTestSuiteRequiredBenchmark.testsuite ( {\"specification\":\"2020-12\"} )",
            "value": 1020.6777768849109,
            "unit": "ops/s",
            "extra": "iterations: 3\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.networknt.schema.benchmark.NetworkntTestSuiteRequiredBenchmark.testsuite ( {\"specification\":\"2019-09\"} )",
            "value": 1105.843771422147,
            "unit": "ops/s",
            "extra": "iterations: 3\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.networknt.schema.benchmark.NetworkntTestSuiteRequiredBenchmark.testsuite ( {\"specification\":\"7\"} )",
            "value": 2811.687293306925,
            "unit": "ops/s",
            "extra": "iterations: 3\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.networknt.schema.benchmark.NetworkntTestSuiteRequiredBenchmark.testsuite ( {\"specification\":\"6\"} )",
            "value": 2978.5497913497215,
            "unit": "ops/s",
            "extra": "iterations: 3\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.networknt.schema.benchmark.NetworkntTestSuiteRequiredBenchmark.testsuite ( {\"specification\":\"4\"} )",
            "value": 4272.347882462025,
            "unit": "ops/s",
            "extra": "iterations: 3\nforks: 1\nthreads: 1"
          }
        ]
      },
      {
        "commit": {
          "author": {
            "email": "147650395+ItsDeadlyProgrammer@users.noreply.github.com",
            "name": "harshuuuu",
            "username": "ItsDeadlyProgrammer"
          },
          "committer": {
            "email": "noreply@github.com",
            "name": "GitHub",
            "username": "web-flow"
          },
          "distinct": true,
          "id": "87e855441becce94430ec3da7d2075f023834e05",
          "message": "Compare uniqueItems items without copying them (#1305)\n\n* Compare uniqueItems items without copying them\n\ncomparisonKey built a BigDecimal for every number and copied every\nnested array and object, so all arrays paid for the numeric-equality\nfix, not only arrays that mix integers and decimals. Wrap each item in\na key that walks the node and compares numbers by value instead: whole\nnumbers that fit in a long as longs, other doubles as doubles, and only\nother BigIntegers and BigDecimals as BigDecimals.\n\nWhole doubles at or above 2^63 are compared by their exact value, the\nsame as smaller whole doubles, rather than by their shortest decimal\nform.\n\nMake decimalsWithDifferentScaleAreDuplicates read BigDecimals so it no\nlonger passes without numeric folding, fix the comparisonKey Javadoc\nindentation by rewriting it, and add tests for the new comparison paths.\n\nFixes #1302\n\n* Hash big numbers by exact value and keep shortest-form comparison for doubles\n\nBigIntegerNode and DecimalNode throw from doubleValue() in Jackson 3 when the\nvalue is outside the double range, so hash a BIG number from its BigDecimal.\nA BIG number that isn't exactly a double's value now hashes its own exact\nvalue, so integers that round to the same double no longer share a bucket.\n\nWhole doubles of 2^53 and above go back to comparing by decimalValue(), their\nshortest decimal form, as before. Changing that belongs with const and enum\nin #1293.\n\n* Compare the double -2^63 by its shortest decimal form",
          "timestamp": "2026-10-13T08:20:20-04:00",
          "tree_id": "de9d72e4fa8816bfdda27ff16eb434e6bb3eb496",
          "url": "https://github.com/networknt/json-schema-validator/commit/87e855441becce94430ec3da7d2075f023834e05"
        },
        "date": 1791894395317,
        "tool": "jmh",
        "benches": [
          {
            "name": "com.networknt.schema.benchmark.NetworkntBenchmark.basic",
            "value": 6404.761551034785,
            "unit": "ops/s",
            "extra": "iterations: 3\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.networknt.schema.benchmark.NetworkntTestSuiteOptionalBenchmark.testsuite ( {\"specification\":\"2020-12\"} )",
            "value": 1622.8397856516733,
            "unit": "ops/s",
            "extra": "iterations: 3\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.networknt.schema.benchmark.NetworkntTestSuiteOptionalBenchmark.testsuite ( {\"specification\":\"2019-09\"} )",
            "value": 1699.4809239909373,
            "unit": "ops/s",
            "extra": "iterations: 3\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.networknt.schema.benchmark.NetworkntTestSuiteOptionalBenchmark.testsuite ( {\"specification\":\"7\"} )",
            "value": 1792.6159888135626,
            "unit": "ops/s",
            "extra": "iterations: 3\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.networknt.schema.benchmark.NetworkntTestSuiteOptionalBenchmark.testsuite ( {\"specification\":\"6\"} )",
            "value": 3916.312690310468,
            "unit": "ops/s",
            "extra": "iterations: 3\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.networknt.schema.benchmark.NetworkntTestSuiteOptionalBenchmark.testsuite ( {\"specification\":\"4\"} )",
            "value": 4923.631577089368,
            "unit": "ops/s",
            "extra": "iterations: 3\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.networknt.schema.benchmark.NetworkntTestSuiteRequiredBenchmark.testsuite ( {\"specification\":\"2020-12\"} )",
            "value": 1386.3083696121755,
            "unit": "ops/s",
            "extra": "iterations: 3\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.networknt.schema.benchmark.NetworkntTestSuiteRequiredBenchmark.testsuite ( {\"specification\":\"2019-09\"} )",
            "value": 1442.489101972323,
            "unit": "ops/s",
            "extra": "iterations: 3\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.networknt.schema.benchmark.NetworkntTestSuiteRequiredBenchmark.testsuite ( {\"specification\":\"7\"} )",
            "value": 3793.1616376811103,
            "unit": "ops/s",
            "extra": "iterations: 3\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.networknt.schema.benchmark.NetworkntTestSuiteRequiredBenchmark.testsuite ( {\"specification\":\"6\"} )",
            "value": 4101.905016057223,
            "unit": "ops/s",
            "extra": "iterations: 3\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.networknt.schema.benchmark.NetworkntTestSuiteRequiredBenchmark.testsuite ( {\"specification\":\"4\"} )",
            "value": 5937.875840427337,
            "unit": "ops/s",
            "extra": "iterations: 3\nforks: 1\nthreads: 1"
          }
        ]
      },
      {
        "commit": {
          "author": {
            "email": "stevehu@gmail.com",
            "name": "Steve Hu",
            "username": "stevehu"
          },
          "committer": {
            "email": "noreply@github.com",
            "name": "GitHub",
            "username": "web-flow"
          },
          "distinct": true,
          "id": "0db2bc93094234e31ce31ee5aabf8e2a7166cb13",
          "message": "Add an error reference document (#1317)\n\nLists the message keys reported by the built-in keywords and formats,\nwhat each argument means, and how to identify an error in code. The\nnumeric error codes were removed in 2.0.0 and their replacement was not\ndocumented.\n\nFixes #862\n\nCo-authored-by: Claude Opus 5.5 <noreply@anthropic.com>",
          "timestamp": "2026-10-13T09:43:05-04:00",
          "tree_id": "b774da1d1e68f07ad1895f2885bb7d804d7aa0ff",
          "url": "https://github.com/networknt/json-schema-validator/commit/0db2bc93094234e31ce31ee5aabf8e2a7166cb13"
        },
        "date": 1791899367806,
        "tool": "jmh",
        "benches": [
          {
            "name": "com.networknt.schema.benchmark.NetworkntBenchmark.basic",
            "value": 5283.175787429837,
            "unit": "ops/s",
            "extra": "iterations: 3\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.networknt.schema.benchmark.NetworkntTestSuiteOptionalBenchmark.testsuite ( {\"specification\":\"2020-12\"} )",
            "value": 1193.9735621012014,
            "unit": "ops/s",
            "extra": "iterations: 3\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.networknt.schema.benchmark.NetworkntTestSuiteOptionalBenchmark.testsuite ( {\"specification\":\"2019-09\"} )",
            "value": 1235.0161297641305,
            "unit": "ops/s",
            "extra": "iterations: 3\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.networknt.schema.benchmark.NetworkntTestSuiteOptionalBenchmark.testsuite ( {\"specification\":\"7\"} )",
            "value": 1285.770170645156,
            "unit": "ops/s",
            "extra": "iterations: 3\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.networknt.schema.benchmark.NetworkntTestSuiteOptionalBenchmark.testsuite ( {\"specification\":\"6\"} )",
            "value": 2937.7548684525086,
            "unit": "ops/s",
            "extra": "iterations: 3\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.networknt.schema.benchmark.NetworkntTestSuiteOptionalBenchmark.testsuite ( {\"specification\":\"4\"} )",
            "value": 3603.891632559976,
            "unit": "ops/s",
            "extra": "iterations: 3\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.networknt.schema.benchmark.NetworkntTestSuiteRequiredBenchmark.testsuite ( {\"specification\":\"2020-12\"} )",
            "value": 1036.477706320821,
            "unit": "ops/s",
            "extra": "iterations: 3\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.networknt.schema.benchmark.NetworkntTestSuiteRequiredBenchmark.testsuite ( {\"specification\":\"2019-09\"} )",
            "value": 1107.1024573198163,
            "unit": "ops/s",
            "extra": "iterations: 3\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.networknt.schema.benchmark.NetworkntTestSuiteRequiredBenchmark.testsuite ( {\"specification\":\"7\"} )",
            "value": 2796.278340736572,
            "unit": "ops/s",
            "extra": "iterations: 3\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.networknt.schema.benchmark.NetworkntTestSuiteRequiredBenchmark.testsuite ( {\"specification\":\"6\"} )",
            "value": 3076.201378866779,
            "unit": "ops/s",
            "extra": "iterations: 3\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.networknt.schema.benchmark.NetworkntTestSuiteRequiredBenchmark.testsuite ( {\"specification\":\"4\"} )",
            "value": 4453.178626737469,
            "unit": "ops/s",
            "extra": "iterations: 3\nforks: 1\nthreads: 1"
          }
        ]
      },
      {
        "commit": {
          "author": {
            "email": "stevehu@gmail.com",
            "name": "Steve Hu",
            "username": "stevehu"
          },
          "committer": {
            "email": "noreply@github.com",
            "name": "GitHub",
            "username": "web-flow"
          },
          "distinct": true,
          "id": "826adb439b3e97c7df8b77b29b53320887e2164e",
          "message": "Keep the message key when an error has a custom message (#1320)\n\nError.Builder.build() replaced the message key with the message pattern\nwhenever a message was set. So a schema-supplied custom message and the\nunresolved reference errors returned their message text from\ngetMessageKey() instead of the key. The key is now only replaced when\nnone was set.\n\nFixes #1319\n\nCo-authored-by: Claude Opus 5.5 <noreply@anthropic.com>",
          "timestamp": "2026-10-13T10:06:42-04:00",
          "tree_id": "20ad2a7374c3d490de21255f10c789ff90bf9cd2",
          "url": "https://github.com/networknt/json-schema-validator/commit/826adb439b3e97c7df8b77b29b53320887e2164e"
        },
        "date": 1791900863526,
        "tool": "jmh",
        "benches": [
          {
            "name": "com.networknt.schema.benchmark.NetworkntBenchmark.basic",
            "value": 4920.292210111512,
            "unit": "ops/s",
            "extra": "iterations: 3\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.networknt.schema.benchmark.NetworkntTestSuiteOptionalBenchmark.testsuite ( {\"specification\":\"2020-12\"} )",
            "value": 1216.3136782166696,
            "unit": "ops/s",
            "extra": "iterations: 3\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.networknt.schema.benchmark.NetworkntTestSuiteOptionalBenchmark.testsuite ( {\"specification\":\"2019-09\"} )",
            "value": 1271.4046568365598,
            "unit": "ops/s",
            "extra": "iterations: 3\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.networknt.schema.benchmark.NetworkntTestSuiteOptionalBenchmark.testsuite ( {\"specification\":\"7\"} )",
            "value": 1356.111134142697,
            "unit": "ops/s",
            "extra": "iterations: 3\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.networknt.schema.benchmark.NetworkntTestSuiteOptionalBenchmark.testsuite ( {\"specification\":\"6\"} )",
            "value": 3006.0339876781673,
            "unit": "ops/s",
            "extra": "iterations: 3\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.networknt.schema.benchmark.NetworkntTestSuiteOptionalBenchmark.testsuite ( {\"specification\":\"4\"} )",
            "value": 3849.368962846076,
            "unit": "ops/s",
            "extra": "iterations: 3\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.networknt.schema.benchmark.NetworkntTestSuiteRequiredBenchmark.testsuite ( {\"specification\":\"2020-12\"} )",
            "value": 1048.142500493396,
            "unit": "ops/s",
            "extra": "iterations: 3\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.networknt.schema.benchmark.NetworkntTestSuiteRequiredBenchmark.testsuite ( {\"specification\":\"2019-09\"} )",
            "value": 1143.2745787677975,
            "unit": "ops/s",
            "extra": "iterations: 3\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.networknt.schema.benchmark.NetworkntTestSuiteRequiredBenchmark.testsuite ( {\"specification\":\"7\"} )",
            "value": 2806.4051900988766,
            "unit": "ops/s",
            "extra": "iterations: 3\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.networknt.schema.benchmark.NetworkntTestSuiteRequiredBenchmark.testsuite ( {\"specification\":\"6\"} )",
            "value": 3088.6090250336183,
            "unit": "ops/s",
            "extra": "iterations: 3\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.networknt.schema.benchmark.NetworkntTestSuiteRequiredBenchmark.testsuite ( {\"specification\":\"4\"} )",
            "value": 4530.220023454489,
            "unit": "ops/s",
            "extra": "iterations: 3\nforks: 1\nthreads: 1"
          }
        ]
      },
      {
        "commit": {
          "author": {
            "email": "stevehu@gmail.com",
            "name": "Steve Hu",
            "username": "stevehu"
          },
          "committer": {
            "email": "noreply@github.com",
            "name": "GitHub",
            "username": "web-flow"
          },
          "distinct": true,
          "id": "3c99bf44dcd2ef2a52aa477ee366416898ed0beb",
          "message": "Fix unknown keyword diagnostics for document fragment loads (#1205) (#1323)",
          "timestamp": "2026-10-13T12:15:13-04:00",
          "tree_id": "383dd30877661068a6e8175183b8ef2965fcf2c3",
          "url": "https://github.com/networknt/json-schema-validator/commit/3c99bf44dcd2ef2a52aa477ee366416898ed0beb"
        },
        "date": 1791908501928,
        "tool": "jmh",
        "benches": [
          {
            "name": "com.networknt.schema.benchmark.NetworkntBenchmark.basic",
            "value": 5202.538488101457,
            "unit": "ops/s",
            "extra": "iterations: 3\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.networknt.schema.benchmark.NetworkntTestSuiteOptionalBenchmark.testsuite ( {\"specification\":\"2020-12\"} )",
            "value": 1174.4668659643555,
            "unit": "ops/s",
            "extra": "iterations: 3\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.networknt.schema.benchmark.NetworkntTestSuiteOptionalBenchmark.testsuite ( {\"specification\":\"2019-09\"} )",
            "value": 1237.7726353775836,
            "unit": "ops/s",
            "extra": "iterations: 3\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.networknt.schema.benchmark.NetworkntTestSuiteOptionalBenchmark.testsuite ( {\"specification\":\"7\"} )",
            "value": 1306.105086209602,
            "unit": "ops/s",
            "extra": "iterations: 3\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.networknt.schema.benchmark.NetworkntTestSuiteOptionalBenchmark.testsuite ( {\"specification\":\"6\"} )",
            "value": 2982.7730904118657,
            "unit": "ops/s",
            "extra": "iterations: 3\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.networknt.schema.benchmark.NetworkntTestSuiteOptionalBenchmark.testsuite ( {\"specification\":\"4\"} )",
            "value": 3726.5068237645824,
            "unit": "ops/s",
            "extra": "iterations: 3\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.networknt.schema.benchmark.NetworkntTestSuiteRequiredBenchmark.testsuite ( {\"specification\":\"2020-12\"} )",
            "value": 1022.4605588027842,
            "unit": "ops/s",
            "extra": "iterations: 3\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.networknt.schema.benchmark.NetworkntTestSuiteRequiredBenchmark.testsuite ( {\"specification\":\"2019-09\"} )",
            "value": 1105.1915202184935,
            "unit": "ops/s",
            "extra": "iterations: 3\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.networknt.schema.benchmark.NetworkntTestSuiteRequiredBenchmark.testsuite ( {\"specification\":\"7\"} )",
            "value": 2830.2061448447057,
            "unit": "ops/s",
            "extra": "iterations: 3\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.networknt.schema.benchmark.NetworkntTestSuiteRequiredBenchmark.testsuite ( {\"specification\":\"6\"} )",
            "value": 2994.2536844935953,
            "unit": "ops/s",
            "extra": "iterations: 3\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.networknt.schema.benchmark.NetworkntTestSuiteRequiredBenchmark.testsuite ( {\"specification\":\"4\"} )",
            "value": 4353.863039674611,
            "unit": "ops/s",
            "extra": "iterations: 3\nforks: 1\nthreads: 1"
          }
        ]
      },
      {
        "commit": {
          "author": {
            "email": "stevehu@gmail.com",
            "name": "Steve Hu",
            "username": "stevehu"
          },
          "committer": {
            "email": "noreply@github.com",
            "name": "GitHub",
            "username": "web-flow"
          },
          "distinct": true,
          "id": "0a6755dc3e34d009767899e0545c9c0aadbbe635",
          "message": "Fix container diagnostics for external JSON Pointer references (#1325) (#1326)",
          "timestamp": "2026-10-13T12:46:43-04:00",
          "tree_id": "96f74fcafc4950d95195dc1fb20198e3b944836f",
          "url": "https://github.com/networknt/json-schema-validator/commit/0a6755dc3e34d009767899e0545c9c0aadbbe635"
        },
        "date": 1791910375336,
        "tool": "jmh",
        "benches": [
          {
            "name": "com.networknt.schema.benchmark.NetworkntBenchmark.basic",
            "value": 8993.956320832358,
            "unit": "ops/s",
            "extra": "iterations: 3\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.networknt.schema.benchmark.NetworkntTestSuiteOptionalBenchmark.testsuite ( {\"specification\":\"2020-12\"} )",
            "value": 2100.194733451625,
            "unit": "ops/s",
            "extra": "iterations: 3\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.networknt.schema.benchmark.NetworkntTestSuiteOptionalBenchmark.testsuite ( {\"specification\":\"2019-09\"} )",
            "value": 2216.6279391826374,
            "unit": "ops/s",
            "extra": "iterations: 3\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.networknt.schema.benchmark.NetworkntTestSuiteOptionalBenchmark.testsuite ( {\"specification\":\"7\"} )",
            "value": 2361.5434133657704,
            "unit": "ops/s",
            "extra": "iterations: 3\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.networknt.schema.benchmark.NetworkntTestSuiteOptionalBenchmark.testsuite ( {\"specification\":\"6\"} )",
            "value": 5342.21010034722,
            "unit": "ops/s",
            "extra": "iterations: 3\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.networknt.schema.benchmark.NetworkntTestSuiteOptionalBenchmark.testsuite ( {\"specification\":\"4\"} )",
            "value": 6604.241193062441,
            "unit": "ops/s",
            "extra": "iterations: 3\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.networknt.schema.benchmark.NetworkntTestSuiteRequiredBenchmark.testsuite ( {\"specification\":\"2020-12\"} )",
            "value": 1840.7287274802645,
            "unit": "ops/s",
            "extra": "iterations: 3\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.networknt.schema.benchmark.NetworkntTestSuiteRequiredBenchmark.testsuite ( {\"specification\":\"2019-09\"} )",
            "value": 1999.8272761130381,
            "unit": "ops/s",
            "extra": "iterations: 3\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.networknt.schema.benchmark.NetworkntTestSuiteRequiredBenchmark.testsuite ( {\"specification\":\"7\"} )",
            "value": 5259.256156197104,
            "unit": "ops/s",
            "extra": "iterations: 3\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.networknt.schema.benchmark.NetworkntTestSuiteRequiredBenchmark.testsuite ( {\"specification\":\"6\"} )",
            "value": 5709.970058980628,
            "unit": "ops/s",
            "extra": "iterations: 3\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.networknt.schema.benchmark.NetworkntTestSuiteRequiredBenchmark.testsuite ( {\"specification\":\"4\"} )",
            "value": 8595.406439612021,
            "unit": "ops/s",
            "extra": "iterations: 3\nforks: 1\nthreads: 1"
          }
        ]
      },
      {
        "commit": {
          "author": {
            "email": "105151063+PHJ2000@users.noreply.github.com",
            "name": "박재홍",
            "username": "PHJ2000"
          },
          "committer": {
            "email": "noreply@github.com",
            "name": "GitHub",
            "username": "web-flow"
          },
          "distinct": true,
          "id": "47d2fe638f39227b6e6cbee2b5c3461da8f161d8",
          "message": "Preserve exact multipleOf operands (#1290)\n\n* Preserve exact multipleOf operands\n\n* Avoid expanding exponents during multipleOf checks\n\n* Complete multipleOf precision, schema checks and integral fast path\n\n* Fix numeric edge cases and preserve mapper compatibility\n\nHandle extreme numeric scales and trailing-zero inputs without\nnormalization overflow or excessive computation.\n\nPreserve exact numeric values in default validation readers while\nretaining public mapper defaults and compact doubles where lossless.\n\nFix related enum, uniqueness, bounds, and count-limit handling.\nPreserve custom-mapper behavior and protected validator hooks.\n\nAdd regression coverage and update the Numeric Precision documentation.\n\nValidation: mvn -B verify on JDK 17; 8,647 tests, 30 skipped, 0 failures or errors.\n\n* Fix numeric reader and validator review regressions\n\n* Address numeric conversion and parser follow-up review\n\n* Narrow multipleOf fix to exact operands and arithmetic\n\n* Fix mixed multipleOf regressions and address review feedback\n\n* Fix multipleOf conversion and test regressions\n\nPreserve overridden dividend conversions and report the divisor used for validation. Remove redundant conversion checks and retain coverage and locale settings in forked tests.\n\n* Avoid redundant conversions for built-in integer validation\n\nUse direct integer remainder for the default validator and concrete IntNode/LongNode inputs. Keep subclass conversion hooks and their existing integer fast path. Add boundary and custom-node regression coverage.\n\nLocal JDK 17 JMH measurements keep varying int/long inputs near 2 ns/op. A getDividend override returning 6.0 changes from about 6.9 to 7.5 ns/op.\n\n* Simplify multipleOf validation around conversion hook results\n\n* Preserve default divisor scale and custom error messages\n\n---------\n\nCo-authored-by: Steve Hu <stevehu@gmail.com>\nCo-authored-by: Claude Opus 5.5 <noreply@anthropic.com>",
          "timestamp": "2026-10-13T13:00:41-04:00",
          "tree_id": "1635598847f3d1ce75b0d584617ec139b4118177",
          "url": "https://github.com/networknt/json-schema-validator/commit/47d2fe638f39227b6e6cbee2b5c3461da8f161d8"
        },
        "date": 1791911227566,
        "tool": "jmh",
        "benches": [
          {
            "name": "com.networknt.schema.benchmark.NetworkntBenchmark.basic",
            "value": 5294.119854960832,
            "unit": "ops/s",
            "extra": "iterations: 3\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.networknt.schema.benchmark.NetworkntTestSuiteOptionalBenchmark.testsuite ( {\"specification\":\"2020-12\"} )",
            "value": 1257.9710889720925,
            "unit": "ops/s",
            "extra": "iterations: 3\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.networknt.schema.benchmark.NetworkntTestSuiteOptionalBenchmark.testsuite ( {\"specification\":\"2019-09\"} )",
            "value": 1344.014221757184,
            "unit": "ops/s",
            "extra": "iterations: 3\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.networknt.schema.benchmark.NetworkntTestSuiteOptionalBenchmark.testsuite ( {\"specification\":\"7\"} )",
            "value": 1398.621325992758,
            "unit": "ops/s",
            "extra": "iterations: 3\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.networknt.schema.benchmark.NetworkntTestSuiteOptionalBenchmark.testsuite ( {\"specification\":\"6\"} )",
            "value": 3306.7269652447526,
            "unit": "ops/s",
            "extra": "iterations: 3\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.networknt.schema.benchmark.NetworkntTestSuiteOptionalBenchmark.testsuite ( {\"specification\":\"4\"} )",
            "value": 4335.666260988376,
            "unit": "ops/s",
            "extra": "iterations: 3\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.networknt.schema.benchmark.NetworkntTestSuiteRequiredBenchmark.testsuite ( {\"specification\":\"2020-12\"} )",
            "value": 1037.441853127262,
            "unit": "ops/s",
            "extra": "iterations: 3\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.networknt.schema.benchmark.NetworkntTestSuiteRequiredBenchmark.testsuite ( {\"specification\":\"2019-09\"} )",
            "value": 1135.1153730272981,
            "unit": "ops/s",
            "extra": "iterations: 3\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.networknt.schema.benchmark.NetworkntTestSuiteRequiredBenchmark.testsuite ( {\"specification\":\"7\"} )",
            "value": 2982.8858447179296,
            "unit": "ops/s",
            "extra": "iterations: 3\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.networknt.schema.benchmark.NetworkntTestSuiteRequiredBenchmark.testsuite ( {\"specification\":\"6\"} )",
            "value": 3214.4269963459774,
            "unit": "ops/s",
            "extra": "iterations: 3\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.networknt.schema.benchmark.NetworkntTestSuiteRequiredBenchmark.testsuite ( {\"specification\":\"4\"} )",
            "value": 4563.485920239148,
            "unit": "ops/s",
            "extra": "iterations: 3\nforks: 1\nthreads: 1"
          }
        ]
      },
      {
        "commit": {
          "author": {
            "email": "105151063+PHJ2000@users.noreply.github.com",
            "name": "박재홍",
            "username": "PHJ2000"
          },
          "committer": {
            "email": "noreply@github.com",
            "name": "GitHub",
            "username": "web-flow"
          },
          "distinct": true,
          "id": "1c6762d848fc3e016dc5db6700446aa5a9d2abf2",
          "message": "Use plain notation for short multipleOf divisors in error messages (#1329)\n\n* Format multipleOf divisors consistently in error messages\n\n* test: cover multi-digit multipleOf message boundaries\n\n* fix: prefer plain multipleOf messages when no longer\n\n* docs: align multipleOf formatting Javadoc with behavior\n\n* fix: ignore zero multipleOf divisors returned by overrides\n\n* refactor: avoid redundant multipleOf formatting and test coverage",
          "timestamp": "2026-10-18T22:02:49-04:00",
          "tree_id": "3a7eb2c155a38cb673f3c0714c9139895dc843b6",
          "url": "https://github.com/networknt/json-schema-validator/commit/1c6762d848fc3e016dc5db6700446aa5a9d2abf2"
        },
        "date": 1792375741840,
        "tool": "jmh",
        "benches": [
          {
            "name": "com.networknt.schema.benchmark.NetworkntBenchmark.basic",
            "value": 9000.942932550046,
            "unit": "ops/s",
            "extra": "iterations: 3\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.networknt.schema.benchmark.NetworkntTestSuiteOptionalBenchmark.testsuite ( {\"specification\":\"2020-12\"} )",
            "value": 2406.331556675119,
            "unit": "ops/s",
            "extra": "iterations: 3\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.networknt.schema.benchmark.NetworkntTestSuiteOptionalBenchmark.testsuite ( {\"specification\":\"2019-09\"} )",
            "value": 2547.750438744135,
            "unit": "ops/s",
            "extra": "iterations: 3\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.networknt.schema.benchmark.NetworkntTestSuiteOptionalBenchmark.testsuite ( {\"specification\":\"7\"} )",
            "value": 2742.644478333989,
            "unit": "ops/s",
            "extra": "iterations: 3\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.networknt.schema.benchmark.NetworkntTestSuiteOptionalBenchmark.testsuite ( {\"specification\":\"6\"} )",
            "value": 6808.596949371381,
            "unit": "ops/s",
            "extra": "iterations: 3\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.networknt.schema.benchmark.NetworkntTestSuiteOptionalBenchmark.testsuite ( {\"specification\":\"4\"} )",
            "value": 8751.537995327517,
            "unit": "ops/s",
            "extra": "iterations: 3\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.networknt.schema.benchmark.NetworkntTestSuiteRequiredBenchmark.testsuite ( {\"specification\":\"2020-12\"} )",
            "value": 1851.7575841816054,
            "unit": "ops/s",
            "extra": "iterations: 3\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.networknt.schema.benchmark.NetworkntTestSuiteRequiredBenchmark.testsuite ( {\"specification\":\"2019-09\"} )",
            "value": 2054.758440882128,
            "unit": "ops/s",
            "extra": "iterations: 3\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.networknt.schema.benchmark.NetworkntTestSuiteRequiredBenchmark.testsuite ( {\"specification\":\"7\"} )",
            "value": 5268.691859094193,
            "unit": "ops/s",
            "extra": "iterations: 3\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.networknt.schema.benchmark.NetworkntTestSuiteRequiredBenchmark.testsuite ( {\"specification\":\"6\"} )",
            "value": 5617.709562792229,
            "unit": "ops/s",
            "extra": "iterations: 3\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.networknt.schema.benchmark.NetworkntTestSuiteRequiredBenchmark.testsuite ( {\"specification\":\"4\"} )",
            "value": 8447.99901119456,
            "unit": "ops/s",
            "extra": "iterations: 3\nforks: 1\nthreads: 1"
          }
        ]
      },
      {
        "commit": {
          "author": {
            "email": "stevehu@gmail.com",
            "name": "Steve Hu",
            "username": "stevehu"
          },
          "committer": {
            "email": "noreply@github.com",
            "name": "GitHub",
            "username": "web-flow"
          },
          "distinct": true,
          "id": "23ef88ad8b79a3f09a32bb0a40a9899094e69af1",
          "message": "Align master multipleOf non-finite docs and tests with 2.x (#1332)\n\nDocument that getDivisor and getDividend return null for non-finite\nfloating-point nodes, and add the failing-override case to\nconvertedNonFiniteInputDoesNotRoundTheDivisor. Follow-up to #1329.\n\nCo-authored-by: Claude Opus 5.5 <noreply@anthropic.com>",
          "timestamp": "2026-10-18T22:07:25-04:00",
          "tree_id": "f129020bc2d1accfccc9be6551003e9c1e67de9a",
          "url": "https://github.com/networknt/json-schema-validator/commit/23ef88ad8b79a3f09a32bb0a40a9899094e69af1"
        },
        "date": 1792376028496,
        "tool": "jmh",
        "benches": [
          {
            "name": "com.networknt.schema.benchmark.NetworkntBenchmark.basic",
            "value": 5219.550769483639,
            "unit": "ops/s",
            "extra": "iterations: 3\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.networknt.schema.benchmark.NetworkntTestSuiteOptionalBenchmark.testsuite ( {\"specification\":\"2020-12\"} )",
            "value": 1250.4391128084474,
            "unit": "ops/s",
            "extra": "iterations: 3\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.networknt.schema.benchmark.NetworkntTestSuiteOptionalBenchmark.testsuite ( {\"specification\":\"2019-09\"} )",
            "value": 1334.538154301944,
            "unit": "ops/s",
            "extra": "iterations: 3\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.networknt.schema.benchmark.NetworkntTestSuiteOptionalBenchmark.testsuite ( {\"specification\":\"7\"} )",
            "value": 1374.4112804086253,
            "unit": "ops/s",
            "extra": "iterations: 3\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.networknt.schema.benchmark.NetworkntTestSuiteOptionalBenchmark.testsuite ( {\"specification\":\"6\"} )",
            "value": 3367.2103275357404,
            "unit": "ops/s",
            "extra": "iterations: 3\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.networknt.schema.benchmark.NetworkntTestSuiteOptionalBenchmark.testsuite ( {\"specification\":\"4\"} )",
            "value": 4336.722863451752,
            "unit": "ops/s",
            "extra": "iterations: 3\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.networknt.schema.benchmark.NetworkntTestSuiteRequiredBenchmark.testsuite ( {\"specification\":\"2020-12\"} )",
            "value": 1056.669715733569,
            "unit": "ops/s",
            "extra": "iterations: 3\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.networknt.schema.benchmark.NetworkntTestSuiteRequiredBenchmark.testsuite ( {\"specification\":\"2019-09\"} )",
            "value": 1136.8651864770848,
            "unit": "ops/s",
            "extra": "iterations: 3\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.networknt.schema.benchmark.NetworkntTestSuiteRequiredBenchmark.testsuite ( {\"specification\":\"7\"} )",
            "value": 2939.503830831605,
            "unit": "ops/s",
            "extra": "iterations: 3\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.networknt.schema.benchmark.NetworkntTestSuiteRequiredBenchmark.testsuite ( {\"specification\":\"6\"} )",
            "value": 3143.0933455086056,
            "unit": "ops/s",
            "extra": "iterations: 3\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.networknt.schema.benchmark.NetworkntTestSuiteRequiredBenchmark.testsuite ( {\"specification\":\"4\"} )",
            "value": 4495.962886530279,
            "unit": "ops/s",
            "extra": "iterations: 3\nforks: 1\nthreads: 1"
          }
        ]
      },
      {
        "commit": {
          "author": {
            "email": "48196425+Obrams@users.noreply.github.com",
            "name": "Nikita Ozerov",
            "username": "Obrams"
          },
          "committer": {
            "email": "noreply@github.com",
            "name": "GitHub",
            "username": "web-flow"
          },
          "distinct": true,
          "id": "9c757382e0f820153165364748cc099a25c375c8",
          "message": "Validate the date-time format without Ethlo with the time format rules (#1316) (#1335)\n\n* Validate the date-time format without Ethlo with the time format rules\n\nThe java.time fallback accepted a time without seconds, an offset\nwithout minutes or with seconds and an empty fraction, and rejected a\nleap second and an offset beyond 18:00. It now parses the date on its\nown and checks the time with the rules of the time format.\n\nRefs #1316\n\n* Check the leap second date and the time structure in the date-time fallback\n\nReview of #1335: a leap second has to be at the end of a June 30 or a\nDecember 31 in UTC, the hour and minute of a leap second are range\nchecked, a time without seconds is rejected explicitly, and the date\ngoes through DateFormat.\n\nRefs #1316\n\n* Add the review's calendar and offset edge cases to DateTimeFormatTest\n\n* Take the leap second date from the parsed time, test the fallback without Ethlo\n\nReview of #1335: TimeFormat.parse returns the fields of a full-time and the\ndate-time fallback uses them for the leap second date instead of reading the\nstring again. The time format change gets its own CHANGELOG line, and a test\nloads the library without Ethlo and validates date-time through a schema.\n\nRefs #1316\n\n---------\n\nCo-authored-by: Obrams <nikitarull@gmail.com>",
          "timestamp": "2026-10-20T19:28:31-04:00",
          "tree_id": "d69456dd8bed0f514cefb1cb4d908572a887e3cf",
          "url": "https://github.com/networknt/json-schema-validator/commit/9c757382e0f820153165364748cc099a25c375c8"
        },
        "date": 1792539298265,
        "tool": "jmh",
        "benches": [
          {
            "name": "com.networknt.schema.benchmark.NetworkntBenchmark.basic",
            "value": 5243.706439599492,
            "unit": "ops/s",
            "extra": "iterations: 3\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.networknt.schema.benchmark.NetworkntTestSuiteOptionalBenchmark.testsuite ( {\"specification\":\"2020-12\"} )",
            "value": 1235.7679559714159,
            "unit": "ops/s",
            "extra": "iterations: 3\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.networknt.schema.benchmark.NetworkntTestSuiteOptionalBenchmark.testsuite ( {\"specification\":\"2019-09\"} )",
            "value": 1304.0747769745496,
            "unit": "ops/s",
            "extra": "iterations: 3\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.networknt.schema.benchmark.NetworkntTestSuiteOptionalBenchmark.testsuite ( {\"specification\":\"7\"} )",
            "value": 1413.7176172163051,
            "unit": "ops/s",
            "extra": "iterations: 3\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.networknt.schema.benchmark.NetworkntTestSuiteOptionalBenchmark.testsuite ( {\"specification\":\"6\"} )",
            "value": 3268.401975632672,
            "unit": "ops/s",
            "extra": "iterations: 3\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.networknt.schema.benchmark.NetworkntTestSuiteOptionalBenchmark.testsuite ( {\"specification\":\"4\"} )",
            "value": 4268.803672314418,
            "unit": "ops/s",
            "extra": "iterations: 3\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.networknt.schema.benchmark.NetworkntTestSuiteRequiredBenchmark.testsuite ( {\"specification\":\"2020-12\"} )",
            "value": 1013.8408290121732,
            "unit": "ops/s",
            "extra": "iterations: 3\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.networknt.schema.benchmark.NetworkntTestSuiteRequiredBenchmark.testsuite ( {\"specification\":\"2019-09\"} )",
            "value": 1076.7966802781405,
            "unit": "ops/s",
            "extra": "iterations: 3\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.networknt.schema.benchmark.NetworkntTestSuiteRequiredBenchmark.testsuite ( {\"specification\":\"7\"} )",
            "value": 2907.0431033110804,
            "unit": "ops/s",
            "extra": "iterations: 3\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.networknt.schema.benchmark.NetworkntTestSuiteRequiredBenchmark.testsuite ( {\"specification\":\"6\"} )",
            "value": 3074.665334598905,
            "unit": "ops/s",
            "extra": "iterations: 3\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.networknt.schema.benchmark.NetworkntTestSuiteRequiredBenchmark.testsuite ( {\"specification\":\"4\"} )",
            "value": 4494.982932681784,
            "unit": "ops/s",
            "extra": "iterations: 3\nforks: 1\nthreads: 1"
          }
        ]
      },
      {
        "commit": {
          "author": {
            "email": "stevehu@gmail.com",
            "name": "Steve Hu",
            "username": "stevehu"
          },
          "committer": {
            "email": "noreply@github.com",
            "name": "GitHub",
            "username": "web-flow"
          },
          "distinct": true,
          "id": "d0005e694695af9078289aea0fc2e6daf462a948",
          "message": "Clarify date-time fallback docs and simplify parsing (#1337) (#1338)\n\n* Clarify date-time fallback docs and simplify parsing (#1337)\n\n* Document remaining Ethlo differences and test year boundaries (#1337)",
          "timestamp": "2026-10-20T20:07:39-04:00",
          "tree_id": "c0f402aa8f753c8d332e90d82b823669d218fa73",
          "url": "https://github.com/networknt/json-schema-validator/commit/d0005e694695af9078289aea0fc2e6daf462a948"
        },
        "date": 1792541637612,
        "tool": "jmh",
        "benches": [
          {
            "name": "com.networknt.schema.benchmark.NetworkntBenchmark.basic",
            "value": 6556.606929317891,
            "unit": "ops/s",
            "extra": "iterations: 3\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.networknt.schema.benchmark.NetworkntTestSuiteOptionalBenchmark.testsuite ( {\"specification\":\"2020-12\"} )",
            "value": 1563.3306896253546,
            "unit": "ops/s",
            "extra": "iterations: 3\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.networknt.schema.benchmark.NetworkntTestSuiteOptionalBenchmark.testsuite ( {\"specification\":\"2019-09\"} )",
            "value": 1623.2795965212151,
            "unit": "ops/s",
            "extra": "iterations: 3\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.networknt.schema.benchmark.NetworkntTestSuiteOptionalBenchmark.testsuite ( {\"specification\":\"7\"} )",
            "value": 1766.9568948833373,
            "unit": "ops/s",
            "extra": "iterations: 3\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.networknt.schema.benchmark.NetworkntTestSuiteOptionalBenchmark.testsuite ( {\"specification\":\"6\"} )",
            "value": 4656.948542882222,
            "unit": "ops/s",
            "extra": "iterations: 3\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.networknt.schema.benchmark.NetworkntTestSuiteOptionalBenchmark.testsuite ( {\"specification\":\"4\"} )",
            "value": 6065.5469263147215,
            "unit": "ops/s",
            "extra": "iterations: 3\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.networknt.schema.benchmark.NetworkntTestSuiteRequiredBenchmark.testsuite ( {\"specification\":\"2020-12\"} )",
            "value": 1145.4573747715244,
            "unit": "ops/s",
            "extra": "iterations: 3\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.networknt.schema.benchmark.NetworkntTestSuiteRequiredBenchmark.testsuite ( {\"specification\":\"2019-09\"} )",
            "value": 1245.7773985972954,
            "unit": "ops/s",
            "extra": "iterations: 3\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.networknt.schema.benchmark.NetworkntTestSuiteRequiredBenchmark.testsuite ( {\"specification\":\"7\"} )",
            "value": 3428.889908071218,
            "unit": "ops/s",
            "extra": "iterations: 3\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.networknt.schema.benchmark.NetworkntTestSuiteRequiredBenchmark.testsuite ( {\"specification\":\"6\"} )",
            "value": 3850.528486557872,
            "unit": "ops/s",
            "extra": "iterations: 3\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.networknt.schema.benchmark.NetworkntTestSuiteRequiredBenchmark.testsuite ( {\"specification\":\"4\"} )",
            "value": 6098.2800127187265,
            "unit": "ops/s",
            "extra": "iterations: 3\nforks: 1\nthreads: 1"
          }
        ]
      },
      {
        "commit": {
          "author": {
            "email": "stevehu@gmail.com",
            "name": "Steve Hu",
            "username": "stevehu"
          },
          "committer": {
            "email": "noreply@github.com",
            "name": "GitHub",
            "username": "web-flow"
          },
          "distinct": true,
          "id": "1b57102e7c13acbfc9c82be20693efafb3983256",
          "message": "Preserve count and length limits beyond int range (#1341)\n\n* Preserve count and length limits beyond int range (#1334)\n\n* Fix count limit review findings for #1334",
          "timestamp": "2026-10-23T02:53:08-04:00",
          "tree_id": "2d7eadf4423520b475cc3ef4934cafadd285cf01",
          "url": "https://github.com/networknt/json-schema-validator/commit/1b57102e7c13acbfc9c82be20693efafb3983256"
        },
        "date": 1792738772693,
        "tool": "jmh",
        "benches": [
          {
            "name": "com.networknt.schema.benchmark.NetworkntBenchmark.basic",
            "value": 5802.032988402847,
            "unit": "ops/s",
            "extra": "iterations: 3\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.networknt.schema.benchmark.NetworkntTestSuiteOptionalBenchmark.testsuite ( {\"specification\":\"2020-12\"} )",
            "value": 1422.4264996769846,
            "unit": "ops/s",
            "extra": "iterations: 3\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.networknt.schema.benchmark.NetworkntTestSuiteOptionalBenchmark.testsuite ( {\"specification\":\"2019-09\"} )",
            "value": 1456.0084851122047,
            "unit": "ops/s",
            "extra": "iterations: 3\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.networknt.schema.benchmark.NetworkntTestSuiteOptionalBenchmark.testsuite ( {\"specification\":\"7\"} )",
            "value": 1589.4063973662098,
            "unit": "ops/s",
            "extra": "iterations: 3\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.networknt.schema.benchmark.NetworkntTestSuiteOptionalBenchmark.testsuite ( {\"specification\":\"6\"} )",
            "value": 4181.780382757718,
            "unit": "ops/s",
            "extra": "iterations: 3\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.networknt.schema.benchmark.NetworkntTestSuiteOptionalBenchmark.testsuite ( {\"specification\":\"4\"} )",
            "value": 5460.0360032173985,
            "unit": "ops/s",
            "extra": "iterations: 3\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.networknt.schema.benchmark.NetworkntTestSuiteRequiredBenchmark.testsuite ( {\"specification\":\"2020-12\"} )",
            "value": 1025.270262063237,
            "unit": "ops/s",
            "extra": "iterations: 3\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.networknt.schema.benchmark.NetworkntTestSuiteRequiredBenchmark.testsuite ( {\"specification\":\"2019-09\"} )",
            "value": 1123.2728439422335,
            "unit": "ops/s",
            "extra": "iterations: 3\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.networknt.schema.benchmark.NetworkntTestSuiteRequiredBenchmark.testsuite ( {\"specification\":\"7\"} )",
            "value": 3240.013953748849,
            "unit": "ops/s",
            "extra": "iterations: 3\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.networknt.schema.benchmark.NetworkntTestSuiteRequiredBenchmark.testsuite ( {\"specification\":\"6\"} )",
            "value": 3447.0758165772313,
            "unit": "ops/s",
            "extra": "iterations: 3\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.networknt.schema.benchmark.NetworkntTestSuiteRequiredBenchmark.testsuite ( {\"specification\":\"4\"} )",
            "value": 5526.888097012518,
            "unit": "ops/s",
            "extra": "iterations: 3\nforks: 1\nthreads: 1"
          }
        ]
      },
      {
        "commit": {
          "author": {
            "email": "stevehu@gmail.com",
            "name": "Steve Hu",
            "username": "stevehu"
          },
          "committer": {
            "email": "noreply@github.com",
            "name": "GitHub",
            "username": "web-flow"
          },
          "distinct": true,
          "id": "b4e1b8cce0b36dc5a3f55c1389409e8714fcddba",
          "message": "Share numeric value equality across validation keywords (#1307) (#1343)\n\n* Share numeric equality and cache uniqueItems number values (#1307)\n\n* Address numeric equality review findings (#1307)",
          "timestamp": "2026-10-23T10:25:06-04:00",
          "tree_id": "a5cc384a3887ec8ceb49450b0e2014d56e335bff",
          "url": "https://github.com/networknt/json-schema-validator/commit/b4e1b8cce0b36dc5a3f55c1389409e8714fcddba"
        },
        "date": 1792766377678,
        "tool": "jmh",
        "benches": [
          {
            "name": "com.networknt.schema.benchmark.NetworkntBenchmark.basic",
            "value": 5062.63816194066,
            "unit": "ops/s",
            "extra": "iterations: 3\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.networknt.schema.benchmark.NetworkntTestSuiteOptionalBenchmark.testsuite ( {\"specification\":\"2020-12\"} )",
            "value": 1288.0082762330376,
            "unit": "ops/s",
            "extra": "iterations: 3\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.networknt.schema.benchmark.NetworkntTestSuiteOptionalBenchmark.testsuite ( {\"specification\":\"2019-09\"} )",
            "value": 1330.2688429638044,
            "unit": "ops/s",
            "extra": "iterations: 3\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.networknt.schema.benchmark.NetworkntTestSuiteOptionalBenchmark.testsuite ( {\"specification\":\"7\"} )",
            "value": 1383.5294934155925,
            "unit": "ops/s",
            "extra": "iterations: 3\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.networknt.schema.benchmark.NetworkntTestSuiteOptionalBenchmark.testsuite ( {\"specification\":\"6\"} )",
            "value": 3359.884923749669,
            "unit": "ops/s",
            "extra": "iterations: 3\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.networknt.schema.benchmark.NetworkntTestSuiteOptionalBenchmark.testsuite ( {\"specification\":\"4\"} )",
            "value": 4316.570296782466,
            "unit": "ops/s",
            "extra": "iterations: 3\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.networknt.schema.benchmark.NetworkntTestSuiteRequiredBenchmark.testsuite ( {\"specification\":\"2020-12\"} )",
            "value": 1020.7440722902021,
            "unit": "ops/s",
            "extra": "iterations: 3\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.networknt.schema.benchmark.NetworkntTestSuiteRequiredBenchmark.testsuite ( {\"specification\":\"2019-09\"} )",
            "value": 1145.5293217639276,
            "unit": "ops/s",
            "extra": "iterations: 3\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.networknt.schema.benchmark.NetworkntTestSuiteRequiredBenchmark.testsuite ( {\"specification\":\"7\"} )",
            "value": 2930.000731671664,
            "unit": "ops/s",
            "extra": "iterations: 3\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.networknt.schema.benchmark.NetworkntTestSuiteRequiredBenchmark.testsuite ( {\"specification\":\"6\"} )",
            "value": 3107.4451303314454,
            "unit": "ops/s",
            "extra": "iterations: 3\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.networknt.schema.benchmark.NetworkntTestSuiteRequiredBenchmark.testsuite ( {\"specification\":\"4\"} )",
            "value": 4608.451792994788,
            "unit": "ops/s",
            "extra": "iterations: 3\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.networknt.schema.benchmark.NumericUniqueItemsBenchmark.validate ( {\"duplicate\":\"false\",\"kind\":\"decimal\"} )",
            "value": 7566.303575519135,
            "unit": "ops/s",
            "extra": "iterations: 3\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.networknt.schema.benchmark.NumericUniqueItemsBenchmark.validate ( {\"duplicate\":\"false\",\"kind\":\"bigInteger\"} )",
            "value": 3597.654799046059,
            "unit": "ops/s",
            "extra": "iterations: 3\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.networknt.schema.benchmark.NumericUniqueItemsBenchmark.validate ( {\"duplicate\":\"false\",\"kind\":\"double\"} )",
            "value": 23048.01948253847,
            "unit": "ops/s",
            "extra": "iterations: 3\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.networknt.schema.benchmark.NumericUniqueItemsBenchmark.validate ( {\"duplicate\":\"false\",\"kind\":\"nestedDecimal\"} )",
            "value": 6224.774224466256,
            "unit": "ops/s",
            "extra": "iterations: 3\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.networknt.schema.benchmark.NumericUniqueItemsBenchmark.validate ( {\"duplicate\":\"true\",\"kind\":\"decimal\"} )",
            "value": 6884.068093222511,
            "unit": "ops/s",
            "extra": "iterations: 3\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.networknt.schema.benchmark.NumericUniqueItemsBenchmark.validate ( {\"duplicate\":\"true\",\"kind\":\"bigInteger\"} )",
            "value": 3496.1733328527866,
            "unit": "ops/s",
            "extra": "iterations: 3\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.networknt.schema.benchmark.NumericUniqueItemsBenchmark.validate ( {\"duplicate\":\"true\",\"kind\":\"double\"} )",
            "value": 23200.491930383392,
            "unit": "ops/s",
            "extra": "iterations: 3\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.networknt.schema.benchmark.NumericUniqueItemsBenchmark.validate ( {\"duplicate\":\"true\",\"kind\":\"nestedDecimal\"} )",
            "value": 6296.154161494095,
            "unit": "ops/s",
            "extra": "iterations: 3\nforks: 1\nthreads: 1"
          }
        ]
      },
      {
        "commit": {
          "author": {
            "email": "stevehu@gmail.com",
            "name": "Steve Hu",
            "username": "stevehu"
          },
          "committer": {
            "email": "noreply@github.com",
            "name": "GitHub",
            "username": "web-flow"
          },
          "distinct": true,
          "id": "991016bb5d067f2a24611604e05a35a4f9e57511",
          "message": "Preserve literal numeric behavior for count-named fields (#1346) (#1347)\n\n* Preserve literal numeric behavior for count-named fields (#1346)\n\n* Preserve exact count tokens and stabilize bound metadata (#1346)\n\n* Preserve opaque count literals and plain integer diagnostics (#1346)\n\n* Invalidate stale count tokens and verify metadata boundaries (#1346)\n\n* Explain retained snapshot string sharing (#1346)",
          "timestamp": "2026-10-23T14:10:21-04:00",
          "tree_id": "e10e7150e5cc73ac6d92fdde043a364e49b05d4f",
          "url": "https://github.com/networknt/json-schema-validator/commit/991016bb5d067f2a24611604e05a35a4f9e57511"
        },
        "date": 1792779901437,
        "tool": "jmh",
        "benches": [
          {
            "name": "com.networknt.schema.benchmark.NetworkntBenchmark.basic",
            "value": 5217.190207218214,
            "unit": "ops/s",
            "extra": "iterations: 3\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.networknt.schema.benchmark.NetworkntTestSuiteOptionalBenchmark.testsuite ( {\"specification\":\"2020-12\"} )",
            "value": 1261.5897259450437,
            "unit": "ops/s",
            "extra": "iterations: 3\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.networknt.schema.benchmark.NetworkntTestSuiteOptionalBenchmark.testsuite ( {\"specification\":\"2019-09\"} )",
            "value": 1343.8012687661765,
            "unit": "ops/s",
            "extra": "iterations: 3\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.networknt.schema.benchmark.NetworkntTestSuiteOptionalBenchmark.testsuite ( {\"specification\":\"7\"} )",
            "value": 1415.7204760874301,
            "unit": "ops/s",
            "extra": "iterations: 3\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.networknt.schema.benchmark.NetworkntTestSuiteOptionalBenchmark.testsuite ( {\"specification\":\"6\"} )",
            "value": 3361.2430146286624,
            "unit": "ops/s",
            "extra": "iterations: 3\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.networknt.schema.benchmark.NetworkntTestSuiteOptionalBenchmark.testsuite ( {\"specification\":\"4\"} )",
            "value": 4240.77126528284,
            "unit": "ops/s",
            "extra": "iterations: 3\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.networknt.schema.benchmark.NetworkntTestSuiteRequiredBenchmark.testsuite ( {\"specification\":\"2020-12\"} )",
            "value": 1076.507486635519,
            "unit": "ops/s",
            "extra": "iterations: 3\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.networknt.schema.benchmark.NetworkntTestSuiteRequiredBenchmark.testsuite ( {\"specification\":\"2019-09\"} )",
            "value": 1129.7583319577266,
            "unit": "ops/s",
            "extra": "iterations: 3\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.networknt.schema.benchmark.NetworkntTestSuiteRequiredBenchmark.testsuite ( {\"specification\":\"7\"} )",
            "value": 2942.35853273043,
            "unit": "ops/s",
            "extra": "iterations: 3\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.networknt.schema.benchmark.NetworkntTestSuiteRequiredBenchmark.testsuite ( {\"specification\":\"6\"} )",
            "value": 3121.9525169944477,
            "unit": "ops/s",
            "extra": "iterations: 3\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.networknt.schema.benchmark.NetworkntTestSuiteRequiredBenchmark.testsuite ( {\"specification\":\"4\"} )",
            "value": 4627.424967344084,
            "unit": "ops/s",
            "extra": "iterations: 3\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.networknt.schema.benchmark.NumericUniqueItemsBenchmark.validate ( {\"duplicate\":\"false\",\"kind\":\"decimal\"} )",
            "value": 7436.760456530948,
            "unit": "ops/s",
            "extra": "iterations: 3\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.networknt.schema.benchmark.NumericUniqueItemsBenchmark.validate ( {\"duplicate\":\"false\",\"kind\":\"bigInteger\"} )",
            "value": 3644.314865729107,
            "unit": "ops/s",
            "extra": "iterations: 3\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.networknt.schema.benchmark.NumericUniqueItemsBenchmark.validate ( {\"duplicate\":\"false\",\"kind\":\"double\"} )",
            "value": 23089.56992308515,
            "unit": "ops/s",
            "extra": "iterations: 3\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.networknt.schema.benchmark.NumericUniqueItemsBenchmark.validate ( {\"duplicate\":\"false\",\"kind\":\"nestedDecimal\"} )",
            "value": 6188.826762060496,
            "unit": "ops/s",
            "extra": "iterations: 3\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.networknt.schema.benchmark.NumericUniqueItemsBenchmark.validate ( {\"duplicate\":\"true\",\"kind\":\"decimal\"} )",
            "value": 7321.762937366788,
            "unit": "ops/s",
            "extra": "iterations: 3\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.networknt.schema.benchmark.NumericUniqueItemsBenchmark.validate ( {\"duplicate\":\"true\",\"kind\":\"bigInteger\"} )",
            "value": 3442.9263429111948,
            "unit": "ops/s",
            "extra": "iterations: 3\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.networknt.schema.benchmark.NumericUniqueItemsBenchmark.validate ( {\"duplicate\":\"true\",\"kind\":\"double\"} )",
            "value": 23593.935929087154,
            "unit": "ops/s",
            "extra": "iterations: 3\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.networknt.schema.benchmark.NumericUniqueItemsBenchmark.validate ( {\"duplicate\":\"true\",\"kind\":\"nestedDecimal\"} )",
            "value": 6397.340682132865,
            "unit": "ops/s",
            "extra": "iterations: 3\nforks: 1\nthreads: 1"
          }
        ]
      },
      {
        "commit": {
          "author": {
            "email": "stevehu@gmail.com",
            "name": "Steve Hu",
            "username": "stevehu"
          },
          "committer": {
            "email": "noreply@github.com",
            "name": "GitHub",
            "username": "web-flow"
          },
          "distinct": true,
          "id": "947702fafe78f62134a8462b8e43e02b62ac5c46",
          "message": "Preserve exact count values in copied schema trees (#1350) (#1351)",
          "timestamp": "2026-10-23T14:53:43-04:00",
          "tree_id": "3ccec3e051325908b39e9b8d4d29d207e10d7b12",
          "url": "https://github.com/networknt/json-schema-validator/commit/947702fafe78f62134a8462b8e43e02b62ac5c46"
        },
        "date": 1792782487359,
        "tool": "jmh",
        "benches": [
          {
            "name": "com.networknt.schema.benchmark.NetworkntBenchmark.basic",
            "value": 6548.086880949984,
            "unit": "ops/s",
            "extra": "iterations: 3\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.networknt.schema.benchmark.NetworkntTestSuiteOptionalBenchmark.testsuite ( {\"specification\":\"2020-12\"} )",
            "value": 1754.7552400803481,
            "unit": "ops/s",
            "extra": "iterations: 3\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.networknt.schema.benchmark.NetworkntTestSuiteOptionalBenchmark.testsuite ( {\"specification\":\"2019-09\"} )",
            "value": 1841.0874754150816,
            "unit": "ops/s",
            "extra": "iterations: 3\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.networknt.schema.benchmark.NetworkntTestSuiteOptionalBenchmark.testsuite ( {\"specification\":\"7\"} )",
            "value": 1951.797949133222,
            "unit": "ops/s",
            "extra": "iterations: 3\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.networknt.schema.benchmark.NetworkntTestSuiteOptionalBenchmark.testsuite ( {\"specification\":\"6\"} )",
            "value": 4566.150994933556,
            "unit": "ops/s",
            "extra": "iterations: 3\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.networknt.schema.benchmark.NetworkntTestSuiteOptionalBenchmark.testsuite ( {\"specification\":\"4\"} )",
            "value": 6287.555050523409,
            "unit": "ops/s",
            "extra": "iterations: 3\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.networknt.schema.benchmark.NetworkntTestSuiteRequiredBenchmark.testsuite ( {\"specification\":\"2020-12\"} )",
            "value": 1408.5575612926452,
            "unit": "ops/s",
            "extra": "iterations: 3\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.networknt.schema.benchmark.NetworkntTestSuiteRequiredBenchmark.testsuite ( {\"specification\":\"2019-09\"} )",
            "value": 1484.733584470536,
            "unit": "ops/s",
            "extra": "iterations: 3\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.networknt.schema.benchmark.NetworkntTestSuiteRequiredBenchmark.testsuite ( {\"specification\":\"7\"} )",
            "value": 3933.7469417054276,
            "unit": "ops/s",
            "extra": "iterations: 3\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.networknt.schema.benchmark.NetworkntTestSuiteRequiredBenchmark.testsuite ( {\"specification\":\"6\"} )",
            "value": 4208.942722014864,
            "unit": "ops/s",
            "extra": "iterations: 3\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.networknt.schema.benchmark.NetworkntTestSuiteRequiredBenchmark.testsuite ( {\"specification\":\"4\"} )",
            "value": 6130.579196954704,
            "unit": "ops/s",
            "extra": "iterations: 3\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.networknt.schema.benchmark.NumericUniqueItemsBenchmark.validate ( {\"duplicate\":\"false\",\"kind\":\"decimal\"} )",
            "value": 8927.537974839004,
            "unit": "ops/s",
            "extra": "iterations: 3\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.networknt.schema.benchmark.NumericUniqueItemsBenchmark.validate ( {\"duplicate\":\"false\",\"kind\":\"bigInteger\"} )",
            "value": 5062.146807969427,
            "unit": "ops/s",
            "extra": "iterations: 3\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.networknt.schema.benchmark.NumericUniqueItemsBenchmark.validate ( {\"duplicate\":\"false\",\"kind\":\"double\"} )",
            "value": 29800.283710986987,
            "unit": "ops/s",
            "extra": "iterations: 3\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.networknt.schema.benchmark.NumericUniqueItemsBenchmark.validate ( {\"duplicate\":\"false\",\"kind\":\"nestedDecimal\"} )",
            "value": 7735.034466158278,
            "unit": "ops/s",
            "extra": "iterations: 3\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.networknt.schema.benchmark.NumericUniqueItemsBenchmark.validate ( {\"duplicate\":\"true\",\"kind\":\"decimal\"} )",
            "value": 8875.680392175092,
            "unit": "ops/s",
            "extra": "iterations: 3\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.networknt.schema.benchmark.NumericUniqueItemsBenchmark.validate ( {\"duplicate\":\"true\",\"kind\":\"bigInteger\"} )",
            "value": 5145.107728033051,
            "unit": "ops/s",
            "extra": "iterations: 3\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.networknt.schema.benchmark.NumericUniqueItemsBenchmark.validate ( {\"duplicate\":\"true\",\"kind\":\"double\"} )",
            "value": 28426.34189984648,
            "unit": "ops/s",
            "extra": "iterations: 3\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.networknt.schema.benchmark.NumericUniqueItemsBenchmark.validate ( {\"duplicate\":\"true\",\"kind\":\"nestedDecimal\"} )",
            "value": 7773.630500029489,
            "unit": "ops/s",
            "extra": "iterations: 3\nforks: 1\nthreads: 1"
          }
        ]
      },
      {
        "commit": {
          "author": {
            "email": "stevehu@gmail.com",
            "name": "Steve Hu",
            "username": "stevehu"
          },
          "committer": {
            "email": "noreply@github.com",
            "name": "GitHub",
            "username": "web-flow"
          },
          "distinct": true,
          "id": "ed06b2a41ed03173d175ec708187bde5036bc57a",
          "message": "Match loose enum numeric strings by exact value (#1353)\n\n* Match loose enum numeric strings by exact value (#1349)\n\n* Handle absent loose enum text and cover reader precision (#1349)",
          "timestamp": "2026-10-23T15:52:40-04:00",
          "tree_id": "63321468ccbd23038ac0c9c3f22cf2337982c191",
          "url": "https://github.com/networknt/json-schema-validator/commit/ed06b2a41ed03173d175ec708187bde5036bc57a"
        },
        "date": 1792786026147,
        "tool": "jmh",
        "benches": [
          {
            "name": "com.networknt.schema.benchmark.NetworkntBenchmark.basic",
            "value": 5056.509200628722,
            "unit": "ops/s",
            "extra": "iterations: 3\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.networknt.schema.benchmark.NetworkntTestSuiteOptionalBenchmark.testsuite ( {\"specification\":\"2020-12\"} )",
            "value": 1304.0912768979535,
            "unit": "ops/s",
            "extra": "iterations: 3\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.networknt.schema.benchmark.NetworkntTestSuiteOptionalBenchmark.testsuite ( {\"specification\":\"2019-09\"} )",
            "value": 1394.4428341294108,
            "unit": "ops/s",
            "extra": "iterations: 3\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.networknt.schema.benchmark.NetworkntTestSuiteOptionalBenchmark.testsuite ( {\"specification\":\"7\"} )",
            "value": 1492.0347763207083,
            "unit": "ops/s",
            "extra": "iterations: 3\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.networknt.schema.benchmark.NetworkntTestSuiteOptionalBenchmark.testsuite ( {\"specification\":\"6\"} )",
            "value": 3575.26927748575,
            "unit": "ops/s",
            "extra": "iterations: 3\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.networknt.schema.benchmark.NetworkntTestSuiteOptionalBenchmark.testsuite ( {\"specification\":\"4\"} )",
            "value": 4760.165669338543,
            "unit": "ops/s",
            "extra": "iterations: 3\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.networknt.schema.benchmark.NetworkntTestSuiteRequiredBenchmark.testsuite ( {\"specification\":\"2020-12\"} )",
            "value": 1075.9579307753945,
            "unit": "ops/s",
            "extra": "iterations: 3\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.networknt.schema.benchmark.NetworkntTestSuiteRequiredBenchmark.testsuite ( {\"specification\":\"2019-09\"} )",
            "value": 1133.7786308765765,
            "unit": "ops/s",
            "extra": "iterations: 3\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.networknt.schema.benchmark.NetworkntTestSuiteRequiredBenchmark.testsuite ( {\"specification\":\"7\"} )",
            "value": 3013.857552332533,
            "unit": "ops/s",
            "extra": "iterations: 3\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.networknt.schema.benchmark.NetworkntTestSuiteRequiredBenchmark.testsuite ( {\"specification\":\"6\"} )",
            "value": 3319.9600797026437,
            "unit": "ops/s",
            "extra": "iterations: 3\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.networknt.schema.benchmark.NetworkntTestSuiteRequiredBenchmark.testsuite ( {\"specification\":\"4\"} )",
            "value": 4633.813146701991,
            "unit": "ops/s",
            "extra": "iterations: 3\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.networknt.schema.benchmark.NumericUniqueItemsBenchmark.validate ( {\"duplicate\":\"false\",\"kind\":\"decimal\"} )",
            "value": 7435.217902917187,
            "unit": "ops/s",
            "extra": "iterations: 3\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.networknt.schema.benchmark.NumericUniqueItemsBenchmark.validate ( {\"duplicate\":\"false\",\"kind\":\"bigInteger\"} )",
            "value": 4006.776445930392,
            "unit": "ops/s",
            "extra": "iterations: 3\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.networknt.schema.benchmark.NumericUniqueItemsBenchmark.validate ( {\"duplicate\":\"false\",\"kind\":\"double\"} )",
            "value": 23370.95155071408,
            "unit": "ops/s",
            "extra": "iterations: 3\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.networknt.schema.benchmark.NumericUniqueItemsBenchmark.validate ( {\"duplicate\":\"false\",\"kind\":\"nestedDecimal\"} )",
            "value": 6304.143535332732,
            "unit": "ops/s",
            "extra": "iterations: 3\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.networknt.schema.benchmark.NumericUniqueItemsBenchmark.validate ( {\"duplicate\":\"true\",\"kind\":\"decimal\"} )",
            "value": 7200.319241784254,
            "unit": "ops/s",
            "extra": "iterations: 3\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.networknt.schema.benchmark.NumericUniqueItemsBenchmark.validate ( {\"duplicate\":\"true\",\"kind\":\"bigInteger\"} )",
            "value": 3992.4557058513105,
            "unit": "ops/s",
            "extra": "iterations: 3\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.networknt.schema.benchmark.NumericUniqueItemsBenchmark.validate ( {\"duplicate\":\"true\",\"kind\":\"double\"} )",
            "value": 21949.494947422936,
            "unit": "ops/s",
            "extra": "iterations: 3\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.networknt.schema.benchmark.NumericUniqueItemsBenchmark.validate ( {\"duplicate\":\"true\",\"kind\":\"nestedDecimal\"} )",
            "value": 6253.929380106176,
            "unit": "ops/s",
            "extra": "iterations: 3\nforks: 1\nthreads: 1"
          }
        ]
      },
      {
        "commit": {
          "author": {
            "email": "77817413+curforever@users.noreply.github.com",
            "name": "curforever",
            "username": "curforever"
          },
          "committer": {
            "email": "noreply@github.com",
            "name": "GitHub",
            "username": "web-flow"
          },
          "distinct": true,
          "id": "6e36ee17460909bafc9390f79fd5c90ba386bed8",
          "message": "Reject space separators in the date-time format (#1331)\n\nRefs #1316.\n\nCo-authored-by: Steve Hu <stevehu@gmail.com>\nCo-authored-by: Claude Sonnet 5.5 <noreply@anthropic.com>",
          "timestamp": "2026-10-23T16:34:12-04:00",
          "tree_id": "3c2d6104e667f0b8445eaab568835a9c7372c6a2",
          "url": "https://github.com/networknt/json-schema-validator/commit/6e36ee17460909bafc9390f79fd5c90ba386bed8"
        },
        "date": 1792788518196,
        "tool": "jmh",
        "benches": [
          {
            "name": "com.networknt.schema.benchmark.NetworkntBenchmark.basic",
            "value": 6576.016320174909,
            "unit": "ops/s",
            "extra": "iterations: 3\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.networknt.schema.benchmark.NetworkntTestSuiteOptionalBenchmark.testsuite ( {\"specification\":\"2020-12\"} )",
            "value": 1772.5962298161064,
            "unit": "ops/s",
            "extra": "iterations: 3\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.networknt.schema.benchmark.NetworkntTestSuiteOptionalBenchmark.testsuite ( {\"specification\":\"2019-09\"} )",
            "value": 1877.8969071540275,
            "unit": "ops/s",
            "extra": "iterations: 3\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.networknt.schema.benchmark.NetworkntTestSuiteOptionalBenchmark.testsuite ( {\"specification\":\"7\"} )",
            "value": 2009.4833928362714,
            "unit": "ops/s",
            "extra": "iterations: 3\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.networknt.schema.benchmark.NetworkntTestSuiteOptionalBenchmark.testsuite ( {\"specification\":\"6\"} )",
            "value": 4915.593444290749,
            "unit": "ops/s",
            "extra": "iterations: 3\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.networknt.schema.benchmark.NetworkntTestSuiteOptionalBenchmark.testsuite ( {\"specification\":\"4\"} )",
            "value": 6497.353591587012,
            "unit": "ops/s",
            "extra": "iterations: 3\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.networknt.schema.benchmark.NetworkntTestSuiteRequiredBenchmark.testsuite ( {\"specification\":\"2020-12\"} )",
            "value": 1394.0696233926321,
            "unit": "ops/s",
            "extra": "iterations: 3\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.networknt.schema.benchmark.NetworkntTestSuiteRequiredBenchmark.testsuite ( {\"specification\":\"2019-09\"} )",
            "value": 1503.997109805657,
            "unit": "ops/s",
            "extra": "iterations: 3\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.networknt.schema.benchmark.NetworkntTestSuiteRequiredBenchmark.testsuite ( {\"specification\":\"7\"} )",
            "value": 3909.074301491721,
            "unit": "ops/s",
            "extra": "iterations: 3\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.networknt.schema.benchmark.NetworkntTestSuiteRequiredBenchmark.testsuite ( {\"specification\":\"6\"} )",
            "value": 4257.169176788845,
            "unit": "ops/s",
            "extra": "iterations: 3\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.networknt.schema.benchmark.NetworkntTestSuiteRequiredBenchmark.testsuite ( {\"specification\":\"4\"} )",
            "value": 6067.911254902919,
            "unit": "ops/s",
            "extra": "iterations: 3\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.networknt.schema.benchmark.NumericUniqueItemsBenchmark.validate ( {\"duplicate\":\"false\",\"kind\":\"decimal\"} )",
            "value": 9081.170237126897,
            "unit": "ops/s",
            "extra": "iterations: 3\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.networknt.schema.benchmark.NumericUniqueItemsBenchmark.validate ( {\"duplicate\":\"false\",\"kind\":\"bigInteger\"} )",
            "value": 5171.22906971532,
            "unit": "ops/s",
            "extra": "iterations: 3\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.networknt.schema.benchmark.NumericUniqueItemsBenchmark.validate ( {\"duplicate\":\"false\",\"kind\":\"double\"} )",
            "value": 29264.664617997954,
            "unit": "ops/s",
            "extra": "iterations: 3\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.networknt.schema.benchmark.NumericUniqueItemsBenchmark.validate ( {\"duplicate\":\"false\",\"kind\":\"nestedDecimal\"} )",
            "value": 7787.786980428707,
            "unit": "ops/s",
            "extra": "iterations: 3\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.networknt.schema.benchmark.NumericUniqueItemsBenchmark.validate ( {\"duplicate\":\"true\",\"kind\":\"decimal\"} )",
            "value": 8762.683390339744,
            "unit": "ops/s",
            "extra": "iterations: 3\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.networknt.schema.benchmark.NumericUniqueItemsBenchmark.validate ( {\"duplicate\":\"true\",\"kind\":\"bigInteger\"} )",
            "value": 5176.363222163574,
            "unit": "ops/s",
            "extra": "iterations: 3\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.networknt.schema.benchmark.NumericUniqueItemsBenchmark.validate ( {\"duplicate\":\"true\",\"kind\":\"double\"} )",
            "value": 28730.305356328667,
            "unit": "ops/s",
            "extra": "iterations: 3\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.networknt.schema.benchmark.NumericUniqueItemsBenchmark.validate ( {\"duplicate\":\"true\",\"kind\":\"nestedDecimal\"} )",
            "value": 7825.476043443489,
            "unit": "ops/s",
            "extra": "iterations: 3\nforks: 1\nthreads: 1"
          }
        ]
      },
      {
        "commit": {
          "author": {
            "email": "stevehu@gmail.com",
            "name": "Steve Hu",
            "username": "stevehu"
          },
          "committer": {
            "email": "noreply@github.com",
            "name": "GitHub",
            "username": "web-flow"
          },
          "distinct": true,
          "id": "1f7d80e5ca9df07d2ed63a77d8f159d64edf5639",
          "message": "Fix RFC 3339 date-time syntax and offset handling (#1355)\n\n* Validate RFC 3339 date-time boundaries consistently (#1316)\n\n* Document date-time breaks and reuse parsed UTC leap date (#1316)",
          "timestamp": "2026-10-23T16:39:58-04:00",
          "tree_id": "2427395387d8071cab4a9e5f8562c79c3270d1ae",
          "url": "https://github.com/networknt/json-schema-validator/commit/1f7d80e5ca9df07d2ed63a77d8f159d64edf5639"
        },
        "date": 1792788867414,
        "tool": "jmh",
        "benches": [
          {
            "name": "com.networknt.schema.benchmark.NetworkntBenchmark.basic",
            "value": 5071.073459340335,
            "unit": "ops/s",
            "extra": "iterations: 3\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.networknt.schema.benchmark.NetworkntTestSuiteOptionalBenchmark.testsuite ( {\"specification\":\"2020-12\"} )",
            "value": 1302.7090070133452,
            "unit": "ops/s",
            "extra": "iterations: 3\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.networknt.schema.benchmark.NetworkntTestSuiteOptionalBenchmark.testsuite ( {\"specification\":\"2019-09\"} )",
            "value": 1363.1031146474859,
            "unit": "ops/s",
            "extra": "iterations: 3\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.networknt.schema.benchmark.NetworkntTestSuiteOptionalBenchmark.testsuite ( {\"specification\":\"7\"} )",
            "value": 1458.9252809469674,
            "unit": "ops/s",
            "extra": "iterations: 3\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.networknt.schema.benchmark.NetworkntTestSuiteOptionalBenchmark.testsuite ( {\"specification\":\"6\"} )",
            "value": 3563.3430513788117,
            "unit": "ops/s",
            "extra": "iterations: 3\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.networknt.schema.benchmark.NetworkntTestSuiteOptionalBenchmark.testsuite ( {\"specification\":\"4\"} )",
            "value": 4619.9003206613415,
            "unit": "ops/s",
            "extra": "iterations: 3\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.networknt.schema.benchmark.NetworkntTestSuiteRequiredBenchmark.testsuite ( {\"specification\":\"2020-12\"} )",
            "value": 1057.1362428794303,
            "unit": "ops/s",
            "extra": "iterations: 3\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.networknt.schema.benchmark.NetworkntTestSuiteRequiredBenchmark.testsuite ( {\"specification\":\"2019-09\"} )",
            "value": 1134.6814313372593,
            "unit": "ops/s",
            "extra": "iterations: 3\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.networknt.schema.benchmark.NetworkntTestSuiteRequiredBenchmark.testsuite ( {\"specification\":\"7\"} )",
            "value": 2891.0902658238933,
            "unit": "ops/s",
            "extra": "iterations: 3\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.networknt.schema.benchmark.NetworkntTestSuiteRequiredBenchmark.testsuite ( {\"specification\":\"6\"} )",
            "value": 3138.5373964802893,
            "unit": "ops/s",
            "extra": "iterations: 3\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.networknt.schema.benchmark.NetworkntTestSuiteRequiredBenchmark.testsuite ( {\"specification\":\"4\"} )",
            "value": 4482.954159996707,
            "unit": "ops/s",
            "extra": "iterations: 3\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.networknt.schema.benchmark.NumericUniqueItemsBenchmark.validate ( {\"duplicate\":\"false\",\"kind\":\"decimal\"} )",
            "value": 7487.070207374985,
            "unit": "ops/s",
            "extra": "iterations: 3\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.networknt.schema.benchmark.NumericUniqueItemsBenchmark.validate ( {\"duplicate\":\"false\",\"kind\":\"bigInteger\"} )",
            "value": 3736.912364011818,
            "unit": "ops/s",
            "extra": "iterations: 3\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.networknt.schema.benchmark.NumericUniqueItemsBenchmark.validate ( {\"duplicate\":\"false\",\"kind\":\"double\"} )",
            "value": 23165.264004689987,
            "unit": "ops/s",
            "extra": "iterations: 3\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.networknt.schema.benchmark.NumericUniqueItemsBenchmark.validate ( {\"duplicate\":\"false\",\"kind\":\"nestedDecimal\"} )",
            "value": 6279.22164034168,
            "unit": "ops/s",
            "extra": "iterations: 3\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.networknt.schema.benchmark.NumericUniqueItemsBenchmark.validate ( {\"duplicate\":\"true\",\"kind\":\"decimal\"} )",
            "value": 7465.313982451473,
            "unit": "ops/s",
            "extra": "iterations: 3\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.networknt.schema.benchmark.NumericUniqueItemsBenchmark.validate ( {\"duplicate\":\"true\",\"kind\":\"bigInteger\"} )",
            "value": 3623.433406491308,
            "unit": "ops/s",
            "extra": "iterations: 3\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.networknt.schema.benchmark.NumericUniqueItemsBenchmark.validate ( {\"duplicate\":\"true\",\"kind\":\"double\"} )",
            "value": 23427.491290793056,
            "unit": "ops/s",
            "extra": "iterations: 3\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.networknt.schema.benchmark.NumericUniqueItemsBenchmark.validate ( {\"duplicate\":\"true\",\"kind\":\"nestedDecimal\"} )",
            "value": 6394.488458991916,
            "unit": "ops/s",
            "extra": "iterations: 3\nforks: 1\nthreads: 1"
          }
        ]
      },
      {
        "commit": {
          "author": {
            "email": "stevehu@gmail.com",
            "name": "Steve Hu",
            "username": "stevehu"
          },
          "committer": {
            "email": "noreply@github.com",
            "name": "GitHub",
            "username": "web-flow"
          },
          "distinct": true,
          "id": "c83e4ebb3e9de439af0d697e3a24ab0f9cc98265",
          "message": "Document invalid multipleOf divisor and bound behavior (#1358)\n\n* Document invalid multipleOf divisor and bound behavior\n\nRecord that zero, negative and out-of-double-range multipleOf divisors\nare accepted when a schema is loaded, how each is treated during\nvalidation, and that numeric bounds beyond the double range compare as\ninfinite bounds. Strict checking is left to meta-schema validation.\n\nCo-Authored-By: Claude Sonnet 5.5 <noreply@anthropic.com>\n\n* Qualify invalid multipleOf notes by number representation\n\nDistinguish the default double-parsing outcomes from the exact-decimal\npath (BigDecimal nodes and USE_BIG_DECIMAL_FOR_FLOATS), where out-of-range\ndivisors and bounds keep their exact size.\n\nCo-Authored-By: Claude Sonnet 5.5 <noreply@anthropic.com>\n\n* Restore the $schema reference in the multipleOf notes\n\nCo-Authored-By: Claude Sonnet 5.5 <noreply@anthropic.com>\n\n---------\n\nCo-authored-by: Claude Sonnet 5.5 <noreply@anthropic.com>",
          "timestamp": "2026-10-23T17:03:59-04:00",
          "tree_id": "6effc45300df5ffe8d9582b3111293d7c9c57221",
          "url": "https://github.com/networknt/json-schema-validator/commit/c83e4ebb3e9de439af0d697e3a24ab0f9cc98265"
        },
        "date": 1792790309094,
        "tool": "jmh",
        "benches": [
          {
            "name": "com.networknt.schema.benchmark.NetworkntBenchmark.basic",
            "value": 5137.177436599104,
            "unit": "ops/s",
            "extra": "iterations: 3\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.networknt.schema.benchmark.NetworkntTestSuiteOptionalBenchmark.testsuite ( {\"specification\":\"2020-12\"} )",
            "value": 1309.95694131384,
            "unit": "ops/s",
            "extra": "iterations: 3\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.networknt.schema.benchmark.NetworkntTestSuiteOptionalBenchmark.testsuite ( {\"specification\":\"2019-09\"} )",
            "value": 1351.3015510604534,
            "unit": "ops/s",
            "extra": "iterations: 3\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.networknt.schema.benchmark.NetworkntTestSuiteOptionalBenchmark.testsuite ( {\"specification\":\"7\"} )",
            "value": 1405.3159331417974,
            "unit": "ops/s",
            "extra": "iterations: 3\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.networknt.schema.benchmark.NetworkntTestSuiteOptionalBenchmark.testsuite ( {\"specification\":\"6\"} )",
            "value": 3475.391988759846,
            "unit": "ops/s",
            "extra": "iterations: 3\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.networknt.schema.benchmark.NetworkntTestSuiteOptionalBenchmark.testsuite ( {\"specification\":\"4\"} )",
            "value": 4690.687283157133,
            "unit": "ops/s",
            "extra": "iterations: 3\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.networknt.schema.benchmark.NetworkntTestSuiteRequiredBenchmark.testsuite ( {\"specification\":\"2020-12\"} )",
            "value": 1047.3374892377333,
            "unit": "ops/s",
            "extra": "iterations: 3\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.networknt.schema.benchmark.NetworkntTestSuiteRequiredBenchmark.testsuite ( {\"specification\":\"2019-09\"} )",
            "value": 1116.9571106211354,
            "unit": "ops/s",
            "extra": "iterations: 3\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.networknt.schema.benchmark.NetworkntTestSuiteRequiredBenchmark.testsuite ( {\"specification\":\"7\"} )",
            "value": 2961.061297712798,
            "unit": "ops/s",
            "extra": "iterations: 3\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.networknt.schema.benchmark.NetworkntTestSuiteRequiredBenchmark.testsuite ( {\"specification\":\"6\"} )",
            "value": 3123.3541160191635,
            "unit": "ops/s",
            "extra": "iterations: 3\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.networknt.schema.benchmark.NetworkntTestSuiteRequiredBenchmark.testsuite ( {\"specification\":\"4\"} )",
            "value": 4565.222883403207,
            "unit": "ops/s",
            "extra": "iterations: 3\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.networknt.schema.benchmark.NumericUniqueItemsBenchmark.validate ( {\"duplicate\":\"false\",\"kind\":\"decimal\"} )",
            "value": 6899.707224835091,
            "unit": "ops/s",
            "extra": "iterations: 3\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.networknt.schema.benchmark.NumericUniqueItemsBenchmark.validate ( {\"duplicate\":\"false\",\"kind\":\"bigInteger\"} )",
            "value": 3587.541440236431,
            "unit": "ops/s",
            "extra": "iterations: 3\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.networknt.schema.benchmark.NumericUniqueItemsBenchmark.validate ( {\"duplicate\":\"false\",\"kind\":\"double\"} )",
            "value": 23213.391835473114,
            "unit": "ops/s",
            "extra": "iterations: 3\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.networknt.schema.benchmark.NumericUniqueItemsBenchmark.validate ( {\"duplicate\":\"false\",\"kind\":\"nestedDecimal\"} )",
            "value": 6285.783528885117,
            "unit": "ops/s",
            "extra": "iterations: 3\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.networknt.schema.benchmark.NumericUniqueItemsBenchmark.validate ( {\"duplicate\":\"true\",\"kind\":\"decimal\"} )",
            "value": 7402.264574214082,
            "unit": "ops/s",
            "extra": "iterations: 3\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.networknt.schema.benchmark.NumericUniqueItemsBenchmark.validate ( {\"duplicate\":\"true\",\"kind\":\"bigInteger\"} )",
            "value": 3359.5080697220055,
            "unit": "ops/s",
            "extra": "iterations: 3\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.networknt.schema.benchmark.NumericUniqueItemsBenchmark.validate ( {\"duplicate\":\"true\",\"kind\":\"double\"} )",
            "value": 23493.06227135692,
            "unit": "ops/s",
            "extra": "iterations: 3\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.networknt.schema.benchmark.NumericUniqueItemsBenchmark.validate ( {\"duplicate\":\"true\",\"kind\":\"nestedDecimal\"} )",
            "value": 6381.042443251467,
            "unit": "ops/s",
            "extra": "iterations: 3\nforks: 1\nthreads: 1"
          }
        ]
      }
    ]
  }
}