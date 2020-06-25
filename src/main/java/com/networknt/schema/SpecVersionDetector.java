/*
 * Copyright (c) 2016 Network New Technologies Inc.
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

package com.networknt.schema;

import com.fasterxml.jackson.databind.JsonNode;

/**
 * This class is used to detect schema version
 *
 * @author Subhajitdas298
 * @since 25/06/20
 */
public class SpecVersionDetector {

    // Schema tag
    private static final String SCHEMA_TAG = "$schema";

    /**
     * Detects schema version based on the schema tag
     *
     * @param jsonNode Json Node to read from
     * @return Spec version
     */
    public static SpecVersion.VersionFlag detect(JsonNode jsonNode) {
        if (!jsonNode.has(SCHEMA_TAG))
            throw new JsonSchemaException("Schema tag not present");

        String schemaUri = JsonSchemaFactory.normalizeMetaSchemaUri(jsonNode.get(SCHEMA_TAG).asText());
        if (schemaUri.equals(JsonMetaSchema.getV4().getUri()))
            return SpecVersion.VersionFlag.V4;
        else if (schemaUri.equals(JsonMetaSchema.getV6().getUri()))
            return SpecVersion.VersionFlag.V6;
        else if (schemaUri.equals(JsonMetaSchema.getV7().getUri()))
            return SpecVersion.VersionFlag.V7;
        else if (schemaUri.equals(JsonMetaSchema.getV201909().getUri()))
            return SpecVersion.VersionFlag.V201909;
        else
            throw new JsonSchemaException("Unrecognizable schema");
    }

}
