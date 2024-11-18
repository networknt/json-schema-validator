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

import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

/**
 * This class is used to detect schema version
 *
 * @author Subhajitdas298
 * @since 25/06/20
 */
public final class SpecVersionDetector {

    private static final Map<String, VersionFlag> supportedVersions = new HashMap<>();
    private static final String SCHEMA_TAG = "$schema";

    static {
        supportedVersions.put("draft2019-09", VersionFlag.V201909);
        supportedVersions.put("draft2020-12", VersionFlag.V202012);
        supportedVersions.put("draft4", VersionFlag.V4);
        supportedVersions.put("draft6", VersionFlag.V6);
        supportedVersions.put("draft7", VersionFlag.V7);
    }

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
        return detectOptionalVersion(jsonNode, true).orElseThrow(
                () -> new JsonSchemaException("'" + SCHEMA_TAG + "' tag is not present")
        );
    }

    /**
     * Detects schema version based on the schema tag: if the schema tag is not present, returns an empty {@link
     * Optional} value, otherwise - returns the detected spec version wrapped into {@link Optional}.
     *
     * @param jsonNode JSON Node to read from
     * @param throwIfUnsupported whether to throw an exception if the version is not supported
     * @return Spec version if present, otherwise empty
     */
    public static Optional<VersionFlag> detectOptionalVersion(JsonNode jsonNode, boolean throwIfUnsupported) {
        return Optional.ofNullable(jsonNode.get(SCHEMA_TAG)).map(schemaTag -> {

            String schemaTagValue = schemaTag.asText();
            String schemaUri = JsonSchemaFactory.normalizeMetaSchemaUri(schemaTagValue);

            if (throwIfUnsupported) {
                return VersionFlag.fromId(schemaUri)
                        .orElseThrow(() -> new JsonSchemaException("'" + schemaTagValue + "' is unrecognizable schema"));
            } else {
                return VersionFlag.fromId(schemaUri).orElse(null);
            }
        });
    }


    // For 2019-09 and later published drafts, implementations that are able to
    // detect the draft of each schema via $schema SHOULD be configured to do so
    public static VersionFlag detectVersion(JsonNode jsonNode, Path specification, VersionFlag defaultVersion, boolean throwIfUnsupported) {
        return Stream.of(
                        detectOptionalVersion(jsonNode, throwIfUnsupported),
                        detectVersionFromPath(specification)
                )
                .filter(Optional::isPresent)
                .map(Optional::get)
                .findFirst()
                .orElse(defaultVersion);
    }

    // For draft-07 and earlier, draft-next, and implementations unable to
    // detect via $schema, implementations MUST be configured to expect the
    // draft matching the test directory name
    public static Optional<VersionFlag> detectVersionFromPath(Path path) {
        return StreamSupport.stream(path.spliterator(), false)
                .map(Path::toString)
                .map(supportedVersions::get)
                .filter(Objects::nonNull)
                .findAny();
    }

    public static Optional<VersionFlag> detectOptionalVersion(String schemaUri) {
        return VersionFlag.fromId(schemaUri);
    }

}
