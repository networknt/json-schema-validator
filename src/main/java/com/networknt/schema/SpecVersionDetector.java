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
import com.networknt.schema.SpecVersion.VersionFlag;

import java.util.Optional;

/**
 * This class is used to detect schema version
 *
 * @author Subhajitdas298
 * @since 25/06/20
 */
public final class SpecVersionDetector {

    private static final String SCHEMA_TAG = "$schema";

    private SpecVersionDetector() {
        // Prevent instantiation of this utility class
    }

    /**
     * Detects schema version based on the schema tag: if the schema tag is not present, throws
     * {@link JsonSchemaException} with the corresponding message, otherwise - returns the detected spec version.
     *
     * @param jsonNode JSON Node to read from
     * @return Spec version if present, otherwise throws an exception
     */
    public static VersionFlag detect(JsonNode jsonNode) {
        return detectOptionalVersion(jsonNode).orElseThrow(
                () -> new JsonSchemaException("'" + SCHEMA_TAG + "' tag is not present")
        );
    }

    /**
     * Detects schema version based on the schema tag: if the schema tag is not present, returns an empty {@link
     * Optional} value, otherwise - returns the detected spec version wrapped into {@link Optional}.
     *
     * @param jsonNode JSON Node to read from
     * @return Spec version if present, otherwise empty
     */
    public static Optional<VersionFlag> detectOptionalVersion(JsonNode jsonNode) {
        return Optional.ofNullable(jsonNode.get(SCHEMA_TAG)).map(schemaTag -> {

            final boolean forceHttps = true;
            final boolean removeEmptyFragmentSuffix = true;

            String schemaTagValue = schemaTag.asText();
            String schemaUri = JsonSchemaFactory.normalizeMetaSchemaUri(schemaTagValue, forceHttps,
                    removeEmptyFragmentSuffix);

            VersionFlag version = detect(schemaUri);
            if (null == version) {
                throw new JsonSchemaException("'" + schemaTagValue + "' is unrecognizable schema");
            }
            return version;
        });
    }

    public static Optional<VersionFlag> detectOptionalVersion(String schemaUri) {
        return Optional.ofNullable(detect(schemaUri));
    }

    private static VersionFlag detect(String schemaUri) {
        if (null != schemaUri) {
            if (schemaUri.equals(JsonMetaSchema.getV4().getUri())) {
                return VersionFlag.V4;
            }
            if (schemaUri.equals(JsonMetaSchema.getV6().getUri())) {
                return VersionFlag.V6;
            }
            if (schemaUri.equals(JsonMetaSchema.getV7().getUri())) {
                return VersionFlag.V7;
            }
            if (schemaUri.equals(JsonMetaSchema.getV201909().getUri())) {
                return VersionFlag.V201909;
            }
            if (schemaUri.equals(JsonMetaSchema.getV202012().getUri())) {
                return VersionFlag.V202012;
            }
        }
        return null;
    }

}
