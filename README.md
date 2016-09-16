# json-schema-validator
A Java json schema validator that support json schema draft v4

* [Why to use this library?](#why-to-use-this-library)
* [Maven installation](#maven-installation)
* [Quickstart](#quickstart)


This project is an implementation of the [JSON Schema Core Draft v4](http://json-schema.org/latest/json-schema-core.html)
specification. It uses the [Jackson](https://github.com/FasterXML/jackson) for json parsing.

# Why to use this library?

 * It is the fastest Java Json Schema Validator as far as I know. Here is the testing result compare with other two open
 source implementations. It is about 32 times faster than fge and 5 times faster than everit.


 fge: 7130ms

 everit-org: 1168ms

 networknt: 223ms

You can run the performance tests for three libraries from [https://github.com/networknt/json-schema-validator-perftest](https://github.com/networknt/json-schema-validator-perftest)

* It uses jackson which is the most popular JSON parser in Java.



## Maven installation

Add the following to your `pom.xml`:

```xml
<dependency>
    <groupId>com.networknt</groupId>
    <artifactId>json-schema-validator</artifactId>
    <version>0.1.0</version>
</dependency>
```

## Quickstart


```
		JsonSchema schema = getJsonSchemaFromStringContent("{\"enum\":[1, 2, 3, 4],\"enumErrorCode\":\"Not in the list\"}");
		JsonNode node = getJsonNodeFromStringContent("7");
		Set<ValidationMessage> errors = schema.validate(node);
		assertThat(errors.size(), is(1));

```

