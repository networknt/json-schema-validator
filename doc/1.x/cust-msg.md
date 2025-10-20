# Custom Error Messages
Schema authors can provide their own custom messages within the schema using a specified keyword.

This is not enabled by default and the `SchemaValidatorsConfig` must be configured with the `errorMessageKeyword`.

```java
SchemaValidatorsConfig config = SchemaValidatorsConfig.builder().errorMessageKeyword("errorMessage").build();
```

## Examples
### Example 1 :
The custom message can be provided outside properties for each type, as shown in the schema below.
```json
{
  "type": "object",
  "properties": {
    "firstName": {
      "type": "string",
      "description": "The person's first name."
    },
    "foo": {
      "type": "array",
      "maxItems": 3
    }
  },
  "errorMessage": {
    "maxItems": "MaxItem must be 3 only",
    "type": "Invalid type"
  }
}
```
### Example 2 :
To keep custom messages distinct for each type, one can even give them in each property.
```json
{
  "type": "object",
  "properties": {
    "dateTime": {
      "type": "string",
      "format": "date",
      "errorMessage": {
        "format": "Keep date format yyyy-mm-dd"
      }
    },
    "uuid": {
      "type": "string",
      "format": "uuid",
      "errorMessage": {
        "format": "Input should be uuid"
      }
    }
  }
}
```
### Example 3 :
For the keywords `required` and `dependencies`, different messages can be specified for different properties.

```json
{
  "type": "object",
  "properties": {
    "foo": {
      "type": "number"
    },
    "bar": {
      "type": "string"
    }
  },
  "required": ["foo", "bar"],
  "errorMessage": {
    "type": "should be an object",
    "required": {
      "foo": "'foo' is required",
      "bar": "'bar' is required"
    }
  }
}
```
### Example 4 :
The message can use arguments but note that single quotes need to be escaped as `java.text.MessageFormat` will be used to format the message.

```json
{
  "type": "object",
  "properties": {
    "foo": {
      "type": "number"
    },
    "bar": {
      "type": "string"
    }
  },
  "required": ["foo", "bar"],
  "errorMessage": {
    "type": "should be an object",
    "required": {
      "foo": "{0}: ''foo'' is required",
      "bar": "{0}: ''bar'' is required"
    }
  }
}
```

## Format
```json
"errorMessage": {
  "[keyword]": "[customMessage]"
}
```
Users can provide custom messages in the configured keyword, typically `errorMessage` or `message` field. 
The `keyword` should be the key and the `customMessage` should be the value.