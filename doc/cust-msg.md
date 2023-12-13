# Custom Message Support
Users can add their own custom messages for schema validation using the instructions in this page.

The json schema itself has a place for the customised message.

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
  "message": {
    "maxItems" : "MaxItem must be 3 only",
    "type" : "Invalid type"
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
      "message": {
        "format": "Keep date format yyyy-mm-dd"
      }
    },
    "uuid": {
      "type": "string",
      "format": "uuid",
      "message": {
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
  "message": {
    "type" : "should be an object",
    "required": {
      "foo" : "'foo' is required",
      "bar" : "'bar' is required"
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
  "message": {
    "type" : "should be an object",
    "required": {
      "foo" : "{0}: ''foo'' is required",
      "bar" : "{0}: ''bar'' is required"
    }
  }
}
```

## Format
```json
"message": {
    [validationType] : [customMessage]
  }
```
Users can express custom message in the **'message'** field. 
The **'validation type'** should be the key and the **'custom message'** should be the value.

Also, we can make format the dynamic message with properties returned from [ValidationMessage.java](https://github.com/networknt/json-schema-validator/blob/master/src/main/java/com/networknt/schema/ValidationMessage.java) class such as **arguments, path e.t.c.**



Take a look at the [PR1](https://github.com/networknt/json-schema-validator/pull/438) and [PR2](https://github.com/networknt/json-schema-validator/pull/632)
