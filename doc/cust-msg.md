This document explains how users can create their custom message for schema validation.


We can provide the custom message in the json schema itself.

<b> Example of schema with default message: </b>

````
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
  }
}
````


<b> Example of schema with a custom message: </b>

````
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
````



````
"message": {
    [validationType] : [customMessage]
  }
````

In the message field users can declare their custom message. The key should be the <b>validation type</b>, and the value should be the <b>custom message</b>.


Also, we can make format the dynamic message with properties returned from [ValidationMessage.java](https://github.com/networknt/json-schema-validator/blob/master/src/main/java/com/networknt/schema/ValidationMessage.java) class such as <b>arguments, path e.t.c.</b>



Take a look at the [PR](https://github.com/networknt/json-schema-validator/pull/438)