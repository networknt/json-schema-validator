The library supports V4, V6, V7, and V2019-09 JSON schema specifications. By default, V4 is used for backward compatibility. 

### For Users

To create a draft V4 JsonSchemaFactory

```
protected ObjectMapper mapper = new ObjectMapper();
protected JsonSchemaFactory validatorFactory = JsonSchemaFactory.builder(JsonSchemaFactory.getInstance()).objectMapper(mapper).build();
```
 
The above code is exactly the same as before. Internally, it will default to the SpecVersion.VersionFlag.V4 as the parameter. 

To create a draft V6 JsonSchemaFactory

```
protected ObjectMapper mapper = new ObjectMapper();
protected JsonSchemaFactory validatorFactory = JsonSchemaFactory.builder(JsonSchemaFactory.getInstance(SpecVersion.VersionFlag.V6)).objectMapper(mapper).build();

```

To create a draft V7 JsonSchemaFactory

```
protected ObjectMapper mapper = new ObjectMapper();
protected JsonSchemaFactory validatorFactory = JsonSchemaFactory.builder(JsonSchemaFactory.getInstance(SpecVersion.VersionFlag.V7)).objectMapper(mapper).build();
```

To create a draft 2019-09 JsonSchemaFactory

```
protected ObjectMapper mapper = new ObjectMapper();
protected JsonSchemaFactory validatorFactory = JsonSchemaFactory.builder(JsonSchemaFactory.getInstance(SpecVersion.VersionFlag.V201909)).objectMapper(mapper).build();
```

### For Developers

#### SpecVersion

A new class SpecVersion has been introduced to indicate which version of the specification is used when creating the JsonSchemaFactory. The SpecVersion has an enum and two methods to convert a long to an EnumSet or a set of VersionFlags to a long value. 

```
    public enum VersionFlag {

        V4(1<<0),
        V6(1<<1),
        V7(1<<2),
        V201909(1<<3);

```

In the long value, we are using 4 bits now as we are supporting 4 versions at the moment. 

V4 -> 0001 -> 1
V6 -> 0010 -> 2
V7 -> 0100 -> 4
V201909 -> 1000 -> 8

If we have a new version added, it should be 

V202009 -> 10000 -> 16

#### ValidatorTypeCode

A new field versionCode is added to indicate which version the validator is supported. 

For most of the validators, the version code should be 15, which is 1111. This means the validator will be loaded for every version of the specification. 

For example.

```
    MAXIMUM("maximum", "1011", new MessageFormat("{0}: must have a maximum value of {1}"), MaximumValidator.class, 15),
```

Since if-then-else was introduced in the V7, it only works for V7 and V2019-09

```
    IF_THEN_ELSE("if", "1037", null, IfValidator.class, 12),  // V7|V201909 1100
```

For exclusiveMaximum, it was introduced from V6

```
    EXCLUSIVE_MAXIMUM("exclusiveMaximum", "1038", new MessageFormat("{0}: must have a exclusive maximum value of {1}"), ExclusiveMaximumValidator.class, 14),  // V6|V7|V201909
```

The getNonFormatKeywords method is updated to accept a SpecVersion.VersionFlag so that only the keywords supported by the specification will be loaded. 

```
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

### JsonMetaSchema

We have created four different static classes V4, V6, V7, and V201909 to build different JsonMetaSchema instances. 

For the BUILDIN_FORMATS, there is a common section, and each static class has its version-specific BUILDIN_FORMATS section. 


### JsonSchemaFactory

The getInstance supports a parameter SpecVersion.VersionFlag to get the right instance of the JsonMetaShema to create the factory. If there is no parameter, then V4 is used by default. 

```
    public static JsonSchemaFactory getInstance() {
        return getInstance(SpecVersion.VersionFlag.V4);
    }

    public static JsonSchemaFactory getInstance(SpecVersion.VersionFlag versionFlag) {
        if(versionFlag == SpecVersion.VersionFlag.V201909) {
            JsonMetaSchema v201909 = JsonMetaSchema.getV201909();
            return builder()
                    .defaultMetaSchemaURI(v201909.getUri())
                    .addMetaSchema(v201909)
                    .build();
        } else if(versionFlag == SpecVersion.VersionFlag.V7) {
            JsonMetaSchema v7 = JsonMetaSchema.getV7();
            return builder()
                    .defaultMetaSchemaURI(v7.getUri())
                    .addMetaSchema(v7)
                    .build();
        } else if(versionFlag == SpecVersion.VersionFlag.V6) {
            JsonMetaSchema v6 = JsonMetaSchema.getV6();
            return builder()
                    .defaultMetaSchemaURI(v6.getUri())
                    .addMetaSchema(v6)
                    .build();
        } else if(versionFlag == SpecVersion.VersionFlag.V4) {
            JsonMetaSchema v4 = JsonMetaSchema.getV4();
            return builder()
                    .defaultMetaSchemaURI(v4.getUri())
                    .addMetaSchema(v4)
                    .build();
        }
        return null;
    }

```

### For Testers

In the test resource folder, we have created and copied all draft version's test suite. They are located in draft4, draft6, draft7, and draft2019-09 folder. 

The existing JsonSchemaTest has been renamed to V4JsonSchemaTest, and the following test classes are added. 

```
V6JsonSchemaTest
V7JsonSchemaTest
V201909JsonSchemaTest
```

These new test classes are not completed yet, and only some sample test cases are added. 

