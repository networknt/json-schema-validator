/*
 * Copyright (c) 2024 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
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

package com.networknt.schema.resource;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.io.IOException;
import java.io.InputStream;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import com.networknt.schema.AbsoluteIri;
import com.networknt.schema.SchemaId;

/**
 * MetaSchemaMapperTest.
 */
class MetaSchemaMapperTest {

    enum MapInput {
        V4(SchemaId.V4),
        V6(SchemaId.V6),
        V7(SchemaId.V7),
        V201909(SchemaId.V201909),
        V202012(SchemaId.V202012);

        String iri;

        MapInput(String iri) {
            this.iri = iri;
        }
    }

    @ParameterizedTest
    @EnumSource(MapInput.class)
    void map(MapInput input) throws IOException {
        MetaSchemaMapper mapper = new MetaSchemaMapper();
        AbsoluteIri result = mapper.map(AbsoluteIri.of(input.iri));
        ClasspathSchemaLoader loader = new ClasspathSchemaLoader();
        InputStreamSource source = loader.getSchema(result);
        assertNotNull(source);
        try (InputStream inputStream = source.getInputStream()) {
            inputStream.read();
        }
    }

}
