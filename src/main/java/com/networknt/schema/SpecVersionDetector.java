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

    private static final String SCHEMA_TAG = "$schema";

    /**
     * Detects schema version based on the schema tag
     *
     * @param jsonNode Json Node to read from
     * @return Spec version
     */
    public static SpecVersion.VersionFlag detect(JsonNode jsonNode) {
        JsonNode schemaTag = jsonNode.get(SCHEMA_TAG);
        if (schemaTag == null) {
            throw new JsonSchemaException("'" + SCHEMA_TAG + "' tag is not present");
        }

        final boolean forceHttps = true;
        final boolean removeEmptyFragmentSuffix = true;

        String schemaTagValue = schemaTag.asText();
        String schemaUri = JsonSchemaFactory.normalizeMetaSchemaUri(schemaTagValue, forceHttps, removeEmptyFragmentSuffix);
        if (schemaUri.equals(JsonMetaSchema.getV4().getUri())) {
            return SpecVersion.VersionFlag.V4;
        }
        if (schemaUri.equals(JsonMetaSchema.getV6().getUri())) {
            return SpecVersion.VersionFlag.V6;
        }
        if (schemaUri.equals(JsonMetaSchema.getV7().getUri())) {
            return SpecVersion.VersionFlag.V7;
        }
        if (schemaUri.equals(JsonMetaSchema.getV201909().getUri())) {
            return SpecVersion.VersionFlag.V201909;
        }
        if (schemaUri.equals(JsonMetaSchema.getV202012().getUri())) {
            return SpecVersion.VersionFlag.V202012;
        }
        throw new JsonSchemaException("'" + schemaTagValue + "' is unrecognizable schema");
    }

}
