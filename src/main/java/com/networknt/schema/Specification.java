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

import com.networknt.schema.dialect.Dialect;
import com.networknt.schema.dialect.DialectId;
import com.networknt.schema.dialect.Dialects;

/**
 * The version of the JSON Schema specification that defines the standard
 * dialects.
 */
public class Specification {

    /**
     * Gets the standard dialect given the specification version.
     * <p>
     * This should only be used if the standard dialect is required, otherwise the
     * dialect should be retrieved from the dialect registry.
     * 
     * @param version the schema specification version
     * @return the dialect or null if not found
     */
    public static Dialect getDialect(SpecificationVersion version) {
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
     * Gets the standard dialect given the dialect id.
     * <p>
     * This should only be used if the standard dialect is required, otherwise the
     * dialect should be retrieved from the dialect registry.
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
