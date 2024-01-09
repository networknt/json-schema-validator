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
package com.networknt.schema.uri;

import java.util.List;

import com.networknt.schema.AbsoluteIri;
import com.networknt.schema.SchemaLocation;

/**
 * Default {@link SchemaLoader}.
 */
public class DefaultSchemaLoader implements SchemaLoader {
    private final List<SchemaLoader> schemaLoaders;
    private final List<AbsoluteIriMapper> absoluteIriMappers;

    public DefaultSchemaLoader(List<SchemaLoader> schemaLoaders, List<AbsoluteIriMapper> absoluteIriMappers) {
        this.schemaLoaders = schemaLoaders;
        this.absoluteIriMappers = absoluteIriMappers;
    }

    @Override
    public InputStreamSource getSchema(SchemaLocation schemaLocation) {
        AbsoluteIri absoluteIri = schemaLocation.getAbsoluteIri();
        SchemaLocation mappedSchemaLocation = schemaLocation;
        for (AbsoluteIriMapper mapper : absoluteIriMappers) {
            AbsoluteIri mapped = mapper.map(absoluteIri);
            if (mapped != null) {
                mappedSchemaLocation = new SchemaLocation(mapped, schemaLocation.getFragment());
                break;
            }
        }
        for (SchemaLoader loader : schemaLoaders) {
            InputStreamSource result = loader.getSchema(mappedSchemaLocation);
            if (result != null) {
                return result;
            }
        }
        return null;
    }

}
