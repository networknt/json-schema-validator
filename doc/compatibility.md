
### Legend

Symbol | Meaning |
:-----:|---------|
🟢 | Fully implemented
🟡 | Partially implemented
🔴 | Not implemented
🚫 | Not defined in Schema Version.

### Compatibility with JSON Schema versions

 Validation Keyword/Schema      |  Draft 4       | Draft 6    | Draft 7     | Draft 2019-09 |
----------------      |:--------------:|:-------:   |:-------:    |:-------------:|
$ref | 🟢 | 🟢 | 🟢 | 🟢
additionalProperties | 🟢 | 🟢 | 🟢 | 🟢
additionalItems | 🟢 | 🟢 | 🟢 | 🟢
allOf | 🟢 | 🟢 | 🟢  | 🟢
anyOf | 🟢 | 🟢 | 🟢 | 🟢
const | 🚫 | 🟢 | 🟢 | 🟢
contains | 🚫 | 🟢 | 🟢 | 🟢
contentEncoding | 🚫 | 🚫 | 🔴 | 🔴
contentMediaType | 🚫 | 🚫 | 🔴 | 🔴
dependencies | 🟢 | 🟢 |🟢 | 🟢
enum | 🟢 | 🟢 | 🟢 | 🟢
exclusiveMaximum (boolean) | 🟢 | 🚫 | 🚫 | 🚫
exclusiveMaximum (numeric) | 🚫 | 🟢 | 🟢 | 🟢
exclusiveMinimum (boolean) | 🟢 | 🚫 | 🚫 | 🚫
exclusiveMinimum (numeric) | 🚫 | 🟢 | 🟢 | 🟢
items | 🟢 | 🟢 | 🟢 | 🟢
maximum | 🟢 | 🟢 | 🟢 | 🟢
maxItems | 🟢 | 🟢 | 🟢 | 🟢
maxLength | 🟢 | 🟢 | 🟢 | 🟢
maxProperties | 🟢 | 🟢 | 🟢 | 🟢
minimum | 🟢 | 🟢 | 🟢 | 🟢
minItems | 🟢 | 🟢 | 🟢 | 🟢
minLength | 🟢 | 🟢 | 🟢 | 🟢
minProperties | 🟢 | 🟢 | 🟢 | 🟢
multipleOf | 🟢 | 🟢 | 🟢 | 🟢
not | 🟢 | 🟢 | 🟢 | 🟢
oneOf | 🟢 | 🟢 | 🟢 | 🟢
pattern | 🟢 | 🟢 | 🟢 | 🟢
patternProperties | 🟢 | 🟢 | 🟢 | 🟢
properties | 🟢 | 🟢 | 🟢 | 🟢
propertyNames | 🚫 | 🔴 | 🔴 | 🔴
required | 🟢 | 🟢 | 🟢 | 🟢 
type | 🟢 | 🟢 | 🟢 | 🟢
uniqueItems | 🟢 | 🟢 | 🟢 | 🟢

### Semantic Validation (Format)

Format | Draft 4 | Draft 6 | Draft 7 | Draft 2019-09 |
-------|---------|---------|---------|---------------|
date |🚫 | 🚫 | 🟢 | 🟢
date-time | 🟢 | 🟢 | 🟢 | 🟢
duration | 🚫 | 🚫 | 🔴 | 🔴
email | 🟢 | 🟢 | 🟢 | 🟢
hostname | 🟢 | 🟢 | 🟢 | 🟢
idn-email | 🚫 | 🚫 | 🔴 | 🔴
idn-hostname | 🚫 | 🚫 | 🔴 | 🔴
ipv4 | 🟢 | 🟢 | 🟢 | 🟢
ipv6 | 🟢 | 🟢 | 🟢 | 🟢
iri | 🚫 | 🚫 | 🔴 | 🔴
iri-reference | 🚫 | 🚫 | 🔴 | 🔴
json-pointer | 🚫 | 🔴 | 🔴 | 🔴
relative-json-pointer | 🚫 | 🔴 | 🔴 | 🔴
regex | 🚫 | 🚫 | 🔴 | 🔴
time | 🚫 | 🚫 | 🟢 | 🟢
uri | 🟢 | 🟢 | 🟢 | 🟢
uri-reference | 🚫 | 🔴 | 🔴 | 🔴
uri-template | 🚫 | 🔴 | 🔴 | 🔴
uuid | 🚫 | 🚫 | 🟢 | 🟢

### Footnotes
1. Note that the validation are only optional for some of the keywords/formats.
2. Refer to the corresponding JSON schema for more information on whether the keyword/format is optional or not.
