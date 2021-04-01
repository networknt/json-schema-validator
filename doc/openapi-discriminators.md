[//]: # (Copyright 2021, Oracle and/or its affiliates.)

## OpenAPI 3.x discriminator support

Starting with `1.0.51`, `json-schema-validator` partly supports the use of discriminators as described under
https://github.com/OAI/OpenAPI-Specification/blame/master/versions/3.0.3.md#L2693 and following.

## How to use

1. Configure `SchemaValidatorsConfig` accordingly:
   ```java
   class Demo{ 
    void demo() {
        SchemaValidatorsConfig config = new SchemaValidatorsConfig();
        config.setOpenAPI3StyleDiscriminators(true); // defaults to false
    }
   }
   ```
2. Use the configured `SchemaValidatorsConfig` with the `JSONSchemaFactory` when creating the `JSONSchema`
   ```java
   class Demo{ 
    void demo() {
        SchemaValidatorsConfig config = new SchemaValidatorsConfig();
        config.setOpenAPI3StyleDiscriminators(true); // defaults to false
        JsonSchema schema = validatorFactory.getSchema(schemaURI, schemaJacksonJsonNode, config);
    }
   }
   ```
3. Ensure that the type field that you want to use as discriminator `propertyName` is required in your schema

## Scope of Support

Discriminators are unfortunately somewhat vague in their definition, especially in regard to JSON Schema validation. So, only
those parts that are indisputable are considered at this moment.

### Supported:

* Polymorphism using `allOf` and `anyOf` with implicit and explicit `mapping`
* `discriminator` on base types and types derived
  thereof `A(with base discriminator) -> B(with optional additive discriminator) -> C(with optional additive discriminator)`

### Not supported:

* `propertyName` redefinition is prohibited on additive discriminators
* `mapping` key redefinition is also prohibited on additive discriminators
* `oneOf` ignores discriminators as today it is not clear from the spec whether `oneOf` + `discriminator` should be equal to
  `anyOf` + `discriminator` or not. Especially if `oneOf` should respect the discriminator and skip the other schemas, it's
  functionally not JSON Schema `oneOf` anymore as multiple matches would not make the validation fail anymore.
* the specification indicates that inline properties should be ignored.
  So, this example would respect `foo`
    ```yaml
    allOf:
        - $ref: otherSchema
        - type: object
          properties:
            foo:
            type: string
          required: ["foo"]
    ```
  while
    ```yaml
    properties:
      foo:
        type: string
    required: ["foo"]
    allOf:
      - $ref: otherSchema
    ```
  should ignore `foo`. **Ignoring `foo` in the second example is currently not implemented**
* You won't get a warning if your `discriminator` uses a field for `propertyName` that is not `required`

## Schema Examples

more examples in https://github.com/networknt/json-schema-validator/blob/master/src/test/resources/openapi3/discriminator.json

### Base type and extended type (the `anyOf` forward references are required)

#### the simplest example:

```json
{
    "anyOf": [
        {
            "$ref": "#/components/schemas/Room"
        },
        {
            "$ref": "#/components/schemas/BedRoom"
        }
    ],
    "components": {
        "schemas": {
            "Room": {
                "type": "object",
                "properties": {
                    "@type": {
                        "type": "string"
                    },
                    "floor": {
                        "type": "integer"
                    }
                },
                "required": [
                    "@type"
                ],
                "discriminator": {
                    "propertyName": "@type"
                }
            },
            "BedRoom": {
                "type": "object",
                "allOf": [
                    {
                        "$ref": "#/components/schemas/Room"
                    },
                    {
                        "type": "object",
                        "properties": {
                            "numberOfBeds": {
                                "type": "integer"
                            }
                        },
                        "required": [
                            "numberOfBeds"
                        ]
                    }
                ]
            }
        }
    }
}
```

#### Here the default mapping key for `BedRoom` is overridden with `bed` from `Room`

```json
{
    "anyOf": [
        {
            "$ref": "#/components/schemas/Room"
        },
        {
            "$ref": "#/components/schemas/BedRoom"
        }
    ],
    "components": {
        "schemas": {
            "Room": {
                "type": "object",
                "properties": {
                    "@type": {
                        "type": "string"
                    },
                    "floor": {
                        "type": "integer"
                    }
                },
                "required": [
                    "@type"
                ],
                "discriminator": {
                    "propertyName": "@type",
                    "mapping": {
                        "bed": "#/components/schemas/BedRoom"
                    }
                }
            },
            "BedRoom": {
                "type": "object",
                "allOf": [
                    {
                        "$ref": "#/components/schemas/Room"
                    },
                    {
                        "type": "object",
                        "properties": {
                            "numberOfBeds": {
                                "type": "integer"
                            }
                        },
                        "required": [
                            "numberOfBeds"
                        ]
                    }
                ]
            }
        }
    }
}
```

###
