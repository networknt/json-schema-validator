## Migration to 2.0.0 from 1.5.x

### Compatibility

| Version           | Java Compatibility | Jackson Version | Comments                                                                                           |
| ----------------- | ------------------ | --------------- | -------------------------------------------------------------------------------------------------- |
| `2.0.0`           | Java 8             | Jackson 2       | This allows clients that still require to use Java 8 to have an incremental upgrade path to 3.0.0. |
| `3.0.0` (Planned) | Java 17            | Jackson 3       | The change to Java compatibility is because Jackson 3 requires Java 17.                            |

### Major Changes

- Configuration on a per Schema basis is no longer possible.
- Removal of deprecated methods and functionality from 1.x.
- Major renaming of many of the public APIs and moving of classes into sub-packages.
- Errors are returned as a `List` instead of a `Set`.
- Error messages do not have the `instanceLocation` as part of the message.
- Error codes have been removed.
- External resources will not be automatically fetched by default. This now requires opt-in via configuration.
  - This is to conform to the specification that requires such functionality to be disabled by default to prefer offline operation. Note however that classpath resources will still be automatically loaded.

#### Renaming and Refactoring

| Old                                                   | New                                                  | Comments                                                                                                                                                                                                                                                                                                                                                                                                                                                                |
| ----------------------------------------------------- | ---------------------------------------------------- | ----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| `com.networknt.schema.JsonMetaSchema`                 | `com.networknt.schema.dialect.Dialect`               | Renamed to convey that this represents the dialect which has a set of keywords and vocabularies with precise semantics. The dialect id is used to identify the meta-schema which can be used to validate that a schema conforms to this dialect.                                                                                                                                                                                                                        |
| `com.networknt.schema.JsonMetaSchemaFactory`          | `com.networknt.schema.dialect.DialectRegistry`       | Renamed to convey that this stores a set of registered dialects that will be used when `$schema` is found in a schema. For instance it is possible to override a standard dialect specified by a specification by registering a dialect using that dialect id.                                                                                                                                                                                                          |
| `com.networknt.schema.JsonSchema`                     | `com.networknt.schema.Schema`                        | Simplify naming. It is no longer possible to associate a configuration with a specific schema.                                                                                                                                                                                                                                                                                                                                                                          |
| `com.networknt.schema.JsonSchemaFactory`              | `com.networknt.schema.SchemaRegistry`                | Renamed to convey that this stores a set of registered schema resources. All schemas created will use the same configuration used for the registry. Therefore all the keywords of the schemas in the registry will be consistent configured. If there is a need for different configuration, a separate schema registry should be used.                                                                                                                                 |
| `com.networknt.schema.ValidationMessage`              | `com.networknt.schema.Error`                         | Renamed to convey the intent better as a validation error raised by assertion keywords when processing instance data, or as a parse error when processing the schema data. The instance location has also been removed from the message. Therefore calling `error.getMessage()` no longer has the instance location pre-pended to the message. Calling `error.toString()` will return the message with the instance location prepended if the instance location exists. |
| `com.networknt.schema.SchemaValidatorsConfig`         | `com.networknt.schema.SchemaRegistryConfig`          | Renamed to convey that this configuration is shared by all schemas from the same schema registry. The walk configuration has been moved out.                                                                                                                                                                                                                                                                                                                            |
| `com.networknt.schema.SchemaValidatorsConfig`         | `com.networknt.schema.walk.WalkConfig`               | The walk configuration has been moved to a separate class.                                                                                                                                                                                                                                                                                                                                                                                                              |
| `com.networknt.schema.ErrorMessageType`               | No replacement                                       | The concept of error codes have been removed, instead the message keys used for generating the localised messages can be used instead to distinguish the error messages.                                                                                                                                                                                                                                                                                                |
| `com.networknt.schema.ValidationContext`              | `com.networknt.schema.SchemaContext`                 | Renamed to convey that this is the schema context shared for all the schemas and validators for the same overall schema with the same dialect.                                                                                                                                                                                                                                                                                                                          |
| `com.networknt.schema.ValidatorTypeCode`              | `com.networknt.schema.keyword.KeywordType`           | Renamed to convey that these are keywords as the error codes have been removed.                                                                                                                                                                                                                                                                                                                                                                                         |
| `com.networknt.schema.JsonSchemaValidator`            | `com.networknt.schema.Validator`                     | Simplify naming.                                                                                                                                                                                                                                                                                                                                                                                                                                                        |
| `com.networknt.schema.JsonValidator`                  | `com.networknt.schema.keyword.KeywordValidator`      | Renamed to convey the intent that this is the validator created for keywords.                                                                                                                                                                                                                                                                                                                                                                                           |
| `com.networknt.schema.walk.JsonSchemaWalker`          | `com.networknt.schema.walk.Walker`                   | Simplify naming.                                                                                                                                                                                                                                                                                                                                                                                                                                                        |
| `com.networknt.schema.SpecVersion.VersionFlag`        | `com.networknt.schema.SpecificationVersion`          | Renamed and flatten the hierarchy.                                                                                                                                                                                                                                                                                                                                                                                                                                      |
| `com.networknt.schema.SchemaId`                       | `com.networknt.schema.dialect.DialectId`             | Renamed to convey that this is the dialect id used for the `$schema` keyword in schemas and `$id` keyword in meta-schemas.                                                                                                                                                                                                                                                                                                                                              |
| `com.networknt.schema.VocabularyFactory`              | `com.networknt.schema.vocabulary.VocabularyRegistry` | Renamed to convey that this stores a set of registered vocabularies that contain keywords.                                                                                                                                                                                                                                                                                                                                                                              |
| `com.networknt.schema.JsonSchemaIdValidator`          | `com.networknt.schema.SchemaIdValidator`             | Simplify naming.                                                                                                                                                                                                                                                                                                                                                                                                                                                        |
| `com.networknt.schema.JsonSchemaRef`                  | `com.networknt.schema.SchemaRef`                     | Simplify naming.                                                                                                                                                                                                                                                                                                                                                                                                                                                        |
| `com.networknt.schema.JsonSchemaException`            | `com.networknt.schema.SchemaException`               | Simplify naming.                                                                                                                                                                                                                                                                                                                                                                                                                                                        |
| `com.networknt.schema.JsonNodePath`                   | `com.networknt.schema.NodePath`                      | Simplify naming.                                                                                                                                                                                                                                                                                                                                                                                                                                                        |
| `com.networknt.schema.serialization.JsonNodeReader`   | `com.networknt.schema.serialization.NodeReader`      | Simplify naming.                                                                                                                                                                                                                                                                                                                                                                                                                                                        |
| `com.networknt.schema.annotation.JsonNodeAnnotation`  | `com.networknt.schema.annotation.Annotation`         | Simplify naming.                                                                                                                                                                                                                                                                                                                                                                                                                                                        |
| `com.networknt.schema.annotation.JsonNodeAnnotations` | `com.networknt.schema.annotation.Annotations`        | Simplify naming.                                                                                                                                                                                                                                                                                                                                                                                                                                                        |
| `com.networknt.schema.ValidationResult`               | `com.networknt.schema.Result`                        | Renamed to convey that this stores not just validation results but the output from walking.                                                                                                                                                                                                                                                                                                                                                                             |
| `com.networknt.schema.VersionCode`                    | `com.networknt.schema.SpecificationVersionRange`     | Renamed to convey that this contains specification version ranges.                                                                                                                                                                                                                                                                                                                                                                                                      |

#### Configuration

##### Schema Validators Configuration

The `com.networknt.schema.SchemaValidatorsConfig` file has been replaced by either `com.networknt.schema.SchemaRegistryConfig` or `com.networknt.schema.walk.WalkConfig` or moved to `com.networknt.schema.ExecutionConfig` and can no longer be configured on a per schema basis.

| Name                                  | Migration                                                                               |
| ------------------------------------- | --------------------------------------------------------------------------------------- |
| `applyDefaultsStrategy`               | `com.networknt.schema.walk.WalkConfig`                                                  |
| `cacheRefs`                           | `com.networknt.schema.SchemaRegistryConfig`                                             |
| `discriminatorKeywordEnabled`         | Removed. Dialect must contain a `discriminator` keyword.                                |
| `errorMessageKeyword`                 | `com.networknt.schema.SchemaRegistryConfig`                                             |
| `executionContextCustomizer`          | `com.networknt.schema.SchemaRegistryConfig`                                             |
| `failFast`                            | `com.networknt.schema.SchemaRegistryConfig`                                             |
| `formatAssertionsEnabled`             | `com.networknt.schema.SchemaRegistryConfig`                                             |
| `javaSemantics`                       | Removed as this did the same thing as `losslessNarrowing`.                              |
| `locale`                              | `com.networknt.schema.SchemaRegistryConfig`                                             |
| `losslessNarrowing`                   | `com.networknt.schema.SchemaRegistryConfig`                                             |
| `messageSource`                       | `com.networknt.schema.SchemaRegistryConfig`                                             |
| `nullableKeywordEnabled`              | Removed. Dialect must contain a `nullable` keyword.                                     |
| `pathType`                            | `com.networknt.schema.SchemaRegistryConfig`                                             |
| `preloadJsonSchema`                   | `com.networknt.schema.SchemaRegistryConfig`                                             |
| `preloadJsonSchemaRefMaxNestingDepth` | Removed. No longer needed as evaluation context is no longer stored as validator state. |
| `readOnly`                            | `com.networknt.schema.ExecutionConfig`                                                  |
| `regularExpressionFactory`            | `com.networknt.schema.SchemaRegistryConfig`                                             |
| `schemaIdValidator`                   | `com.networknt.schema.SchemaRegistryConfig`                                             |
| `strict`                              | `com.networknt.schema.SchemaRegistryConfig`                                             |
| `typeLoose`                           | `com.networknt.schema.SchemaRegistryConfig`                                             |
| `writeOnly`                           | `com.networknt.schema.ExecutionConfig`                                                  |
| `itemWalkListeners`                   | `com.networknt.schema.walk.WalkConfig`                                                  |
| `keywordWalkListeners`                | `com.networknt.schema.walk.WalkConfig`                                                  |
| `propertyWalkListeners`               | `com.networknt.schema.walk.WalkConfig`                                                  |

#### API

```java
package com.example.demo;

import java.util.List;
import java.util.Map;

import com.networknt.schema.Error;
import com.networknt.schema.InputFormat;
import com.networknt.schema.Schema;
import com.networknt.schema.SchemaLocation;
import com.networknt.schema.SchemaRegistry;
import com.networknt.schema.dialect.Dialects;

public class Demo {
	public static void main(String[] args) {
		String schemaData = """
			{
			  "$id": "https://example.com/address.schema.json",
			  "$schema": "https://json-schema.org/draft/2020-12/schema",
			  "type": "object",
			  "properties": {
			    "streetAddress": {
			      "type": "string"
			    },
			    "locality": {
			      "type": "string"
			    },
			    "region": {
			      "type": "string"
			    },
			    "postalCode": {
			      "type": "string"
			    },
			    "countryName": {
			      "type": "string"
			    }
			  },
			  "required": [ "locality", "region", "countryName" ]
			}
			""";
		String instanceData = """
			{
			  "streetAddress": "456 Main St",
			  "region": "State",
			  "postalCode": "12345",
			  "countryName": "Country"
			}
			""";
		SchemaRegistry schemaRegistry = SchemaRegistry.withDialect(Dialects.getDraft202012(),
				builder -> builder.schemas(Map.of("https://example.com/address.schema.json", schemaData)));
		Schema schema = schemaRegistry.getSchema(SchemaLocation.of("https://example.com/address.schema.json"));
		List<Error> errors = schema.validate(instanceData, InputFormat.JSON);
		System.out.println(errors);
	}
}
```

#### Configuration Examples

##### Support all standard dialects but defaults to Draft 2020-12 when not specified using $schema

```java
SchemaRegistry schemaRegistry = SchemaRegistry.withDefaultDialect(SpecificationVersion.DRAFT_2020_12);
```

##### Only supports Draft 2020-12 and defaults to it when not specified using $schema

```java
SchemaRegistry schemaRegistry = SchemaRegistry.withDialect(Dialects.getDraft202012());
```

##### Only supports OpenAPI 3.1 and defaults to it when not specified using $schema

```java
SchemaRegistry schemaRegistry = SchemaRegistry.withDialect(Dialects.getOpenApi31());
```

##### Provide schema resource using string

```java
Map<String, String> schemas = new HashMap<>();
    schemas.put("https://example.com/address.schema.json", schemaData);
    SchemaRegistry schemaRegistry = SchemaRegistry.withDialect(Dialects.getDraft202012(),
        builder -> builder.schemas(schemas));
```

##### Map schema resource id to classpath

```java
SchemaRegistry schemaRegistry = SchemaRegistry.withDialect(Dialects.getDraft202012(),
    builder -> builder.schemaIdResolvers(schemaIdResolvers -> schemaIdResolvers
        .mapPrefix("https://spec.openapis.org/oas/3.1", "classpath:oas/3.1")));
```

##### Fetch remote schema resources

```java
SchemaRegistry schemaRegistry = SchemaRegistry.withDialect(Dialects.getDraft202012(),
    builder -> builder.schemaLoader(schemaLoader -> schemaLoader.fetchRemoteResources()));
```

##### Force the format keyword always behave as an assertion even after Draft 2019-09

```java
SchemaRegistryConfig schemaRegistryConfig = SchemaRegistryConfig.builder().formatAssertionsEnabled(true)
        .build();
SchemaRegistry schemaRegistry = SchemaRegistry.withDialect(Dialects.getDraft202012(),
        builder -> builder.schemaRegistryConfig(schemaRegistryConfig));
```

##### Use [joni](https://github.com/jruby/joni) regular expression implementation instead of JDK which has better ECMA compliance

```java
SchemaRegistryConfig schemaRegistryConfig = SchemaRegistryConfig.builder()
        .regularExpressionFactory(JoniRegularExpressionFactory.getInstance()).build();
SchemaRegistry schemaRegistry = SchemaRegistry.withDialect(Dialects.getDraft202012(),
        builder -> builder.schemaRegistryConfig(schemaRegistryConfig));
```

##### Get schema using schema location

```java
Schema schema = schemaRegistry.getSchema(SchemaLocation.of("https://example.com/address.schema.json"));
```

##### Get schema using schema location

```java
Schema schema = schemaRegistry.getSchema(SchemaLocation.of("https://example.com/address.schema.json"));
```

##### Get schema using a string with the schema data

**_NOTE_**: The schema may not have a base `$id` to properly resolve references to other schema documents.

```java
Schema schema = schemaRegistry.getSchema(schemaData, InputFormat.JSON);
```
