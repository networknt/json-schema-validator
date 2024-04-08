This document lists all the validators supported and gives users are guideline on how to use them. 

### if-then-else

* Specification(s): draft7
* Contributor(s): @andersonf
* Reference: https://json-schema.org/understanding-json-schema/reference/conditionals.html
* Issues and PRs: https://github.com/networknt/json-schema-validator/pull/206

The `if`, `then` and `else` keywords allow the application of a subschema based on the outcome of another schema, much like the `if/then/else` constructs you’ve probably seen in traditional programming languages.

If `if` is valid, `then` must also be valid (and `else` is ignored.) If `if` is invalid, `else` must also be valid (and `then` is ignored).

For usage, please refer to the test cases at https://github.com/networknt/json-schema-validator/blob/master/src/test/suite/tests/draft7/if-then-else.json

### Custom Validators
````java
@Bean
public JsonSchemaFactory mySchemaFactory() {
    // base on JsonMetaSchema.V201909 copy code below
    String URI = "https://json-schema.org/draft/2019-09/schema";
    String ID = "$id";
    List<Format> BUILTIN_FORMATS = new ArrayList<Format>(JsonMetaSchema.COMMON_BUILTIN_FORMATS);

    JsonMetaSchema myJsonMetaSchema = new JsonMetaSchema.Builder(URI)
            .idKeyword(ID)
            .formats(BUILTIN_FORMATS)
            .keywords(ValidatorTypeCode.getFormatKeywords(SpecVersion.VersionFlag.V201909))
            // keywords that may validly exist, but have no validation aspect to them
            .keywords(Arrays.asList(
                    new NonValidationKeyword("$schema"),
                    new NonValidationKeyword("$id"),
                    new NonValidationKeyword("title"),
                    new NonValidationKeyword("description"),
                    new NonValidationKeyword("default"),
                    new NonValidationKeyword("definitions"),
                    new NonValidationKeyword("$defs")  // newly added in 2018-09 release.
            ))
            // add your custom keyword
            .keyword(new GroovyKeyword())
            .build();

    return new JsonSchemaFactory.Builder().defaultMetaSchemaIri(myJsonMetaSchema.getIri())
            .metaSchema(myJsonMetaSchema)
            .build();
}

public class GroovyKeyword extends AbstractKeyword {
    private static final Logger logger = LoggerFactory.getLogger(GroovyKeyword.class);

    public GroovyKeyword() {
        super("groovy");
    }

    @Override
    public AbstractJsonValidator newValidator(String schemaPath, JsonNode schemaNode, JsonSchema parentSchema, ValidationContext validationContext) throws JsonSchemaException, Exception {
        // you can read validator config here
        String config = schemaNode.asText();
        return new AbstractJsonValidator(this.getValue()) {
            @Override
            public Set<ValidationMessage> validate(JsonNode node, JsonNode rootNode, String at) {
                // you can do validate here
                logger.info("config:{} path:{} node:{}", config, at, node);

                return Collections.emptySet();
            }
        };
    }
}
````
You can use GroovyKeyword like below:
````json
{
  "type": "object",
  "properties": {
    "someProperty": {
      "type": "string",
      "groovy": "SomeScript.groovy"
    }
  }
}
````

### Override Email/UUID/DateTime Validator

In this library, if the format keyword is "email", "uuid", "date", "date-time", default validator provided by the library will be used.

If you want to override this behavior, do as below.

```java
public JsonSchemaFactory mySchemaFactory() {
    // base on JsonMetaSchema.V201909 copy code below
    String URI = "https://json-schema.org/draft/2019-09/schema";
    String ID = "$id";

    JsonMetaSchema overrideEmailValidatorMetaSchema = new JsonMetaSchema.Builder(URI)
            .idKeyword(ID)
            // Override EmailValidator
            .format(new PatternFormat("email", "^[a-zA-Z0-9.!#$%&'*+\\/=?^_`{|}~-]+@[a-zA-Z0-9-]+(?:\\.[a-zA-Z0-9-]+)*$"))
            .build();

    return new JsonSchemaFactory.Builder().defaultMetaSchemaIri(overrideEmailValidatorMetaSchema.getIri())
            .metaSchema(overrideEmailValidatorMetaSchema)
            .build();
}
```
