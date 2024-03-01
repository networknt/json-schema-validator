The library supports V4, V6, V7, V2019-09 and V2020-12 JSON schema specifications. By default, V4 is used for backward compatibility. 

### For Users

#### To create a draft V4 JsonSchemaFactory

```java
ObjectMapper mapper = new ObjectMapper();
JsonSchemaFactory validatorFactory = JsonSchemaFactory.builder(JsonSchemaFactory.getInstance(SpecVersion.VersionFlag.V4)).objectMapper(mapper).build();
```
or with default configuration
```java
JsonSchemaFactory validatorFactory = JsonSchemaFactory.getInstance(SpecVersion.VersionFlag.V4));
```
 
Please avoid using default `getInstance()`, which, internally, defaults to the `SpecVersion.VersionFlag.V4` as the parameter. This is deprecated.

#### To create a draft V6 JsonSchemaFactory

```java
ObjectMapper mapper = new ObjectMapper();
JsonSchemaFactory validatorFactory = JsonSchemaFactory.builder(JsonSchemaFactory.getInstance(SpecVersion.VersionFlag.V6)).objectMapper(mapper).build();
```
or with default configuration
```java
JsonSchemaFactory validatorFactory = JsonSchemaFactory.getInstance(SpecVersion.VersionFlag.V6));
```

#### To create a draft V7 JsonSchemaFactory

```java
ObjectMapper mapper = new ObjectMapper();
JsonSchemaFactory validatorFactory = JsonSchemaFactory.builder(JsonSchemaFactory.getInstance(SpecVersion.VersionFlag.V7)).objectMapper(mapper).build();
```
or with default configuration
```java
JsonSchemaFactory validatorFactory = JsonSchemaFactory.getInstance(SpecVersion.VersionFlag.V7));
```

#### To create a draft 2019-09 JsonSchemaFactory

```java
ObjectMapper mapper = new ObjectMapper();
JsonSchemaFactory validatorFactory = JsonSchemaFactory.builder(JsonSchemaFactory.getInstance(SpecVersion.VersionFlag.V201909)).objectMapper(mapper).build();
```
or with default configuration
```java
JsonSchemaFactory validatorFactory = JsonSchemaFactory.getInstance(SpecVersion.VersionFlag.V201909));
```

#### To create a draft 2020-12 JsonSchemaFactory

```java
ObjectMapper mapper = new ObjectMapper();
JsonSchemaFactory validatorFactory = JsonSchemaFactory.builder(JsonSchemaFactory.getInstance(SpecVersion.VersionFlag.V202012)).objectMapper(mapper).build();
```
or with default configuration
```java
JsonSchemaFactory validatorFactory = JsonSchemaFactory.getInstance(SpecVersion.VersionFlag.V202012));
```

#### To create a JsonSchemaFactory, automatically detecting schema version

```java
ObjectMapper mapper = new ObjectMapper();
JsonNode jsonNode = mapper.readTree(/* schema / schema input steam etc. */);
JsonSchemaFactory validatorFactory = JsonSchemaFactory.builder(JsonSchemaFactory.getInstance(SpecVersionDetector.detect(jsonNode))).objectMapper(mapper).build();
```
or with default configuration
```java
ObjectMapper mapper = new ObjectMapper();
JsonNode jsonNode = mapper.readTree(/* schema / schema input steam etc. */);
JsonSchemaFactory validatorFactory = JsonSchemaFactory.getInstance(SpecVersionDetector.detect(jsonNode));
```

### For Developers

#### SpecVersion

A new class `SpecVersion` has been introduced to indicate which version of the specification is used when creating the `JsonSchemaFactory`. The `SpecVersion` has an enum and two methods to convert a long to an `EnumSet` or a set of `VersionFlags` to a long value. 

```java
public enum VersionFlag {

    V4(1<<0),
    V6(1<<1),
    V7(1<<2),
    V201909(1<<3),
    V202012(1<<4);

```

In the long value, we are using 5 bits now as we are supporting 5 versions at the moment. 

V4 -> 00001 -> 1
V6 -> 00010 -> 2
V7 -> 00100 -> 4
V201909 -> 01000 -> 8
V202012 -> 10000 --> 16

If we have a new version added, it should be 

V202209 -> 100000 -> 32

#### ValidatorTypeCode

A new field versionCode is added to indicate which version the validator is supported. 

For most of the validators, the version code should be 31, which is 11111. This means the validator will be loaded for every version of the specification. 

For example.

```java
MAXIMUM("maximum", "1011", new MessageFormat("{0}: must have a maximum value of {1}"), MaximumValidator.class, 31),
```

Since if-then-else was introduced in the V7, it only works for V7, V2019-09 and V2020-12

```java
IF_THEN_ELSE("if", "1037", null, IfValidator.class, 28),  // V7|V201909|V202012 11100
```

For exclusiveMaximum, it was introduced from V6

```java
EXCLUSIVE_MAXIMUM("exclusiveMaximum", "1038", new MessageFormat("{0}: must have a exclusive maximum value of {1}"), ExclusiveMaximumValidator.class, 30),  // V6|V7|V201909|V202012
```

The getNonFormatKeywords method is updated to accept a SpecVersion.VersionFlag so that only the keywords supported by the specification will be loaded. 

```java
public static List<ValidatorTypeCode> getNonFormatKeywords(SpecVersion.VersionFlag versionFlag) {
    final List<ValidatorTypeCode> result = new ArrayList<ValidatorTypeCode>();
    for (ValidatorTypeCode keyword: values()) {
        if (!FORMAT.equals(keyword) && specVersion.getVersionFlags(keyword.versionCode).contains(versionFlag)) {
            result.add(keyword);
        }
    }
    return result;
}
```

#### JsonMetaSchema

We have created four different static classes V4, V6, V7, V201909 and V202012 to build different `JsonMetaSchema` instances. 

For the BUILDIN_FORMATS, there is a common section, and each static class has its version-specific BUILDIN_FORMATS section. 

#### JsonSchemaFactory

The getInstance supports a parameter `SpecVersion.VersionFlag` to get the right instance of the `JsonMetaShema` to create the factory. If there is no parameter, then V4 is used by default. 

```java
@Deprecated
public static JsonSchemaFactory getInstance() {
    return getInstance(SpecVersion.VersionFlag.V4);
}

public static JsonSchemaFactory getInstance(SpecVersion.VersionFlag versionFlag) {
    JsonSchemaVersion jsonSchemaVersion = checkVersion(versionFlag);
    JsonMetaSchema metaSchema = jsonSchemaVersion.getInstance();
    return builder()
            .defaultMetaSchemaIri(metaSchema.getIri())
            .metaSchema(metaSchema)
            .build();
}
```

#### SpecVersionDetector

This class detects schema version based on the schema tag.

```java
private static final String SCHEMA_TAG = "$schema";

public static SpecVersion.VersionFlag detect(JsonNode jsonNode) {
    if (!jsonNode.has(SCHEMA_TAG))
        throw new JsonSchemaException("Schema tag not present");

    final boolean forceHttps = true;
    final boolean removeEmptyFragmentSuffix = true;

    String schemaUri = JsonSchemaFactory.normalizeMetaSchemaUri(jsonNode.get(SCHEMA_TAG).asText(), forceHttps, removeEmptyFragmentSuffix);
    if (schemaUri.equals(JsonMetaSchema.getV4().getIri()))
        return SpecVersion.VersionFlag.V4;
    else if (schemaUri.equals(JsonMetaSchema.getV6().getIri()))
        return SpecVersion.VersionFlag.V6;
    else if (schemaUri.equals(JsonMetaSchema.getV7().getIri()))
        return SpecVersion.VersionFlag.V7;
    else if (schemaUri.equals(JsonMetaSchema.getV201909().getIri()))
        return SpecVersion.VersionFlag.V201909;
    else if (schemaUri.equals(JsonMetaSchema.getV202012().getIri()))
        return SpecVersion.VersionFlag.V202012;
    else
        throw new JsonSchemaException("Unrecognizable schema");
}
```

### For Testers

In the test resource folder, we have created and copied all draft version's test suite. They are located in draft4, draft6, draft7, draft2019-09 and draft2020-12 folders.

The existing JsonSchemaTest has been renamed to V4JsonSchemaTest, and the following test classes are added. 

```
V6JsonSchemaTest
V7JsonSchemaTest
V201909JsonSchemaTest
V202012JsonSchemaTest
```

These new test classes are not completed yet, and only some sample test cases are added. 

