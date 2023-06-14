/*
 * Copyright (c) 2020 Network New Technologies Inc.
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

import java.util.Optional;

public class SpecVersion {

    public enum VersionFlag {

        V4(1 << 0, "https://json-schema.org/draft-04/schema"),
        V6(1 << 1, "https://json-schema.org/draft-06/schema"),
        V7(1 << 2, "https://json-schema.org/draft-07/schema"),
        V201909(1 << 3, "https://json-schema.org/draft/2019-09/schema"),
        V202012(1 << 4, "https://json-schema.org/draft/2020-12/schema");


        private final long versionFlagValue;
        private final String id;

        VersionFlag(long versionFlagValue, String id) {
            this.versionFlagValue = versionFlagValue;
            this.id = id;
        }

        public String getId() {
            return this.id;
        }

        public long getVersionFlagValue() {
            return this.versionFlagValue;
        }

        public static Optional<VersionFlag> fromId(String id) {
            for (VersionFlag v: VersionFlag.values()) {
                if (v.id.equals(id)) return Optional.of(v);
            }
            return Optional.empty();
        }
    }

}
