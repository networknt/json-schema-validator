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

import com.networknt.schema.dialect.Dialect;
import com.networknt.schema.dialect.DialectId;
import com.networknt.schema.dialect.Dialects;

/**
 * The JSON Schema specification which defines the standard dialects.
 */
public class Specification {

    /**
     * The JSON Schema specification version.
     */
    public enum Version {
        /**
         * Draft 4.
         */
        DRAFT_4(4, DialectId.DRAFT_4),
        /**
         * Draft 6.
         */
        DRAFT_6(6, DialectId.DRAFT_6),
        /**
         * Draft 7.
         */
        DRAFT_7(7, DialectId.DRAFT_7),
        /**
         * Draft 2019-09.
         */
        DRAFT_2019_09(8, DialectId.DRAFT_2019_09),
        /**
         * Draft 2020-12.
         */
        DRAFT_2020_12(9, DialectId.DRAFT_2020_12);

        private final int order;
        private final String dialectId;

        Version(int order, String dialectId) {
            this.order = order;
            this.dialectId = dialectId;
        }

        /**
         * Gets the dialect id used for the $schema keyword. The dialect id is an IRI
         * that identifies the meta schema used to validate the dialect.
         * 
         * @return the dialect id
         */
        public String getDialectId() {
            return this.dialectId;
        }

        /**
         * Gets the unique release order of the specification version used that
         * indicates when the specification was released. Lower numbers indicate the
         * specification was released earlier.
         *
         * @return the order when the specification was released
         */
        public int getOrder() {
            return this.order;
        }

        /**
         * Gets the specification version that matches the dialect id indicated by
         * $schema keyword. The dialect id is an IRI that identifies the meta schema
         * used to validate the dialect.
         *
         * @param dialectId the dialect id specified by $schema keyword
         * @return the specification version if it matches the dialect id
         */
        public static Optional<Version> fromDialectId(String dialectId) {
            for (Version version : Version.values()) {
                if (version.dialectId.equals(dialectId)) {
                    return Optional.of(version);
                }
            }
            return Optional.empty();
        }
    }

    /**
     * Gets the dialect given the specification version.
     * 
     * @param version the schema specification version
     * @return the dialect or null if not found
     */
    public static Dialect getDialect(Specification.Version version) {
        if (null == version) {
            return null;
        }
        switch (version) {
        case DRAFT_2020_12:
            return Dialects.getDraft202012();
        case DRAFT_2019_09:
            return Dialects.getDraft201909();
        case DRAFT_7:
            return Dialects.getDraft7();
        case DRAFT_6:
            return Dialects.getDraft6();
        case DRAFT_4:
            return Dialects.getDraft4();
        default:
            return null;
        }
    }

    /**
     * Gets the dialect given the dialect id.
     * 
     * @param dialectId the schema specification version
     * @return the dialect or null if not found
     */
    public static Dialect getDialect(String dialectId) {
        if (null == dialectId) {
            return null;
        }
        switch (dialectId) {
        case DialectId.DRAFT_2020_12:
            return Dialects.getDraft202012();
        case DialectId.DRAFT_2019_09:
            return Dialects.getDraft201909();
        case DialectId.DRAFT_7:
            return Dialects.getDraft7();
        case DialectId.DRAFT_6:
            return Dialects.getDraft6();
        case DialectId.DRAFT_4:
            return Dialects.getDraft4();
        default:
            return null;
        }
    }
}
