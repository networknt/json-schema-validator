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
import com.networknt.schema.dialect.DialectId;

/**
 * MetaSchemaMapperTest.
 */
class MetaSchemaIdResolverTest {

    enum MapInput {
        V4(DialectId.DRAFT_4),
        V6(DialectId.DRAFT_6),
        V7(DialectId.DRAFT_7),
        V201909(DialectId.DRAFT_2019_09),
        V202012(DialectId.DRAFT_2020_12);

        String iri;

        MapInput(String iri) {
            this.iri = iri;
        }
    }

    @ParameterizedTest
    @EnumSource(MapInput.class)
    void map(MapInput input) throws IOException {
        MetaSchemaIdResolver mapper = new MetaSchemaIdResolver();
        AbsoluteIri result = mapper.resolve(AbsoluteIri.of(input.iri));
        ClasspathResourceLoader loader = new ClasspathResourceLoader();
        InputStreamSource source = loader.getResource(result);
        assertNotNull(source);
        try (InputStream inputStream = source.getInputStream()) {
            inputStream.read();
        }
    }

}
