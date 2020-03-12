This document lists all the validators supported and gives users are guideline on how to use them. 

### if-then-else

* Specification(s): draft7
* Contributor(s): @andersonf
* Reference: https://json-schema.org/understanding-json-schema/reference/conditionals.html
* Issues and PRs: https://github.com/networknt/json-schema-validator/pull/206

The `if`, `then` and `else` keywords allow the application of a subschema based on the outcome of another schema, much like the `if/then/else` constructs you’ve probably seen in traditional programming languages.

If `if` is valid, `then` must also be valid (and `else` is ignored.) If `if` is invalid, `else` must also be valid (and `then` is ignored).

For usage, please refer to the test cases at https://github.com/networknt/json-schema-validator/blob/master/src/test/resources/draft7/if-then-else.json

