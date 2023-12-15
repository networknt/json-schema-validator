/*
 * Copyright (c) 2023 the original author or authors.
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

public class UriReference {
    public static final JsonNodePath ROOT = new JsonNodePath(PathType.URI_REFERENCE);

    /**
     * The relative uri reference to the document.
     */
    public static final JsonNodePath DOCUMENT = ROOT.resolve("#");

    /**
     * Converts a path from a uri reference {@link JsonNodePath}.
     *
     * @param uriReference the path
     * @return the path
     */
    public static JsonNodePath get(String uriReference) {
        JsonNodePath path = null;
        int schemeSeparator = uriReference.indexOf("://");
        if (schemeSeparator != -1) {
            int pathIndex = uriReference.indexOf('/', schemeSeparator + 3);
            if (pathIndex == -1) {
                return ROOT.resolve(uriReference);
            } else {
                path = ROOT.resolve(uriReference.substring(0, pathIndex));
                uriReference = uriReference.substring(pathIndex);
                if (uriReference.charAt(0) == '/' && uriReference.length() > 1) {
                    uriReference = uriReference.substring(1);
                }
            }
        }
        
        String[] values = uriReference.split("/");
        for (int x = 0; x < values.length; x++) {
            if (x == 0 && path == null) {
                if ("#".equals(values[x])) {
                    path = DOCUMENT;
                } else {
                    path = ROOT.resolve(values[x]);
                }
            } else {
                path = path.resolve(values[x]);
            }
        }
        return path;
    }
}
