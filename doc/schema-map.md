While working with JSON schema validation, we have to use external references sometimes. However, there are two issues to have references to schemas on the Internet. 

* Some applications are running inside a corporate network without Internet access. 
* Some of the Internet resources are not reliable

One solution is to change all the external reference to internal in JSON schemas, but this is error-prone and hard to maintain in a long run. 

A smart solution is to map the external references to internal ones in a configuration file. This allows us to use the resources as they are without any modification. In the JSON schema specification, it is not allowed to use local filesystem resource directly. With the mapping, we can use the local resources without worrying about breaking the specification as the references are still in URL format in schemas. In addition, the mapped URL can be a different external URL, or embbeded within a JAR file with a lot more flexibility. 

Note that when using a mapping, the local copy is always used, and the external reference is not queried.

### Usage

Basically, you can specify a mapping in the builder. For more details, please take a look at the test cases and the [PR](https://github.com/networknt/json-schema-validator/pull/125). 


### Real Example

https://github.com/JMRI/JMRI/blob/master/java/src/jmri/server/json/schema-map.json

In case you provide the schema through an `InputStream` or a `String` to resolve `$ref` with URN (relative path), you need to provide the `URNFactory` to the `JsonSchemaFactory.Builder.
URNFactory` interface will allow you to resolve URN to URI.

please take a look at the test cases and the [PR](https://github.com/networknt/json-schema-validator/pull/274).
