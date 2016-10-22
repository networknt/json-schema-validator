# json-schema-validator
A Java json schema validator that support json schema draft v4. It is a key component in our
[light-java](https://github.com/networknt/light-java) microservices framework to validate request
against OpenAPI specification at runtime.


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
    <version>0.1.2</version>
</dependency>
```

## Quickstart

To use the validator, you need to have both JsonSchema object and JsonNode object constructed and there are many ways to do that. Here is my
base test class that shows you several way to construct these from String, Stream, Url and JsonNode. Pay attention on JsonSchemaFactory class
as it is the way to construct JsonSchema object.


```
/*
 * Copyright (c) 2016 Network New Technologies Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * You may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.networknt.schema;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.InputStream;
import java.net.URL;

/**
 * Created by steve on 22/10/16.
 */
public class BaseJsonSchemaValidatorTest {
    protected JsonNode getJsonNodeFromClasspath(String name) throws Exception {
        InputStream is1 = Thread.currentThread().getContextClassLoader()
                .getResourceAsStream(name);

        ObjectMapper mapper = new ObjectMapper();
        JsonNode node = mapper.readTree(is1);
        return node;
    }

    protected JsonNode getJsonNodeFromStringContent(String content) throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode node = mapper.readTree(content);
        return node;
    }

    protected JsonNode getJsonNodeFromUrl(String url) throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode node = mapper.readTree(new URL(url));
        return node;
    }

    protected JsonSchema getJsonSchemaFromClasspath(String name) throws Exception {
        JsonSchemaFactory factory = new JsonSchemaFactory();
        InputStream is = Thread.currentThread().getContextClassLoader()
                .getResourceAsStream(name);
        JsonSchema schema = factory.getSchema(is);
        return schema;
    }

    protected JsonSchema getJsonSchemaFromStringContent(String schemaContent) throws Exception {
        JsonSchemaFactory factory = new JsonSchemaFactory();
        JsonSchema schema = factory.getSchema(schemaContent);
        return schema;
    }

    protected JsonSchema getJsonSchemaFromUrl(String url) throws Exception {
        JsonSchemaFactory factory = new JsonSchemaFactory();
        JsonSchema schema = factory.getSchema(new URL(url));
        return schema;
    }

    protected JsonSchema getJsonSchemaFromJsonNode(JsonNode jsonNode) throws Exception {
        JsonSchemaFactory factory = new JsonSchemaFactory();
        JsonSchema schema = factory.getSchema(jsonNode);
        return schema;
    }
}

```
And the following is one of the test case in one of the test class extends from above base class. As you can see, it constructs JsonSchema
and JsonNode from String.

```
		JsonSchema schema = getJsonSchemaFromStringContent("{\"enum\":[1, 2, 3, 4],\"enumErrorCode\":\"Not in the list\"}");
		JsonNode node = getJsonNodeFromStringContent("7");
		Set<ValidationMessage> errors = schema.validate(node);
		assertThat(errors.size(), is(1));

```

## Known issues

I have just updated the test suites from the [official website](https://github.com/json-schema-org/JSON-Schema-Test-Suite) as the old ones were copied from another
Java validator. Now there are several issues that need to be addressed. All of them are edge cases
in my opinion but need to be investigated.

[#7](https://github.com/networknt/json-schema-validator/issues/7)

[#6](https://github.com/networknt/json-schema-validator/issues/6)

[#5](https://github.com/networknt/json-schema-validator/issues/5)

[#4](https://github.com/networknt/json-schema-validator/issues/4)


