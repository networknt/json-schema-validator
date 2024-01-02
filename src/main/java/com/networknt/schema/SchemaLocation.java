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

import java.util.Objects;

/**
 * The schema location is the canonical IRI of the schema object plus a JSON
 * Pointer fragment indicating the subschema that produced a result. In contrast
 * with the evaluation path, the schema location MUST NOT include by-reference
 * applicators such as $ref or $dynamicRef.
 */
public class SchemaLocation {
    private static final JsonNodePath JSON_POINTER = new JsonNodePath(PathType.JSON_POINTER);
    private static final JsonNodePath ANCHOR = new JsonNodePath(PathType.URI_REFERENCE);

    /**
     * Represents a relative schema location to the current document.
     */
    public static final SchemaLocation DOCUMENT = new SchemaLocation(null, JSON_POINTER);

    private final AbsoluteIri absoluteIri;
    private final JsonNodePath fragment;

    private volatile String value = null; // computed lazily

    /**
     * Constructs a new {@link SchemaLocation}.
     * 
     * @param absoluteIri canonical absolute IRI of the schema object
     * @param fragment    the fragment
     */
    public SchemaLocation(AbsoluteIri absoluteIri, JsonNodePath fragment) {
        this.absoluteIri = absoluteIri;
        this.fragment = fragment;
    }

    /**
     * Gets the canonical absolute IRI of the schema object.
     * <p>
     * This is a unique identifier indicated by the $id property or id property in
     * Draft 4 and earlier. This does not have to be network accessible.
     *
     * @return the canonical absolute IRI of the schema object.
     */
    public AbsoluteIri getAbsoluteIri() {
        return this.absoluteIri;
    }

    /**
     * Gets the fragment.
     * 
     * @return the fragment
     */
    public JsonNodePath getFragment() {
        return this.fragment;
    }

    /**
     * Appends the token to the fragment.
     * 
     * @param token the segment
     * @return a new schema location with the segment
     */
    public SchemaLocation append(String token) {
        return new SchemaLocation(this.absoluteIri, this.fragment.append(token));
    }

    /**
     * Appends the index to the fragment.
     * 
     * @param index the segment
     * @return a new schema location with the segment
     */
    public SchemaLocation append(int index) {
        return new SchemaLocation(this.absoluteIri, this.fragment.append(index));
    }

    /**
     * Parses a string representing an IRI of the schema location.
     * 
     * @param iri the IRI
     * @return the schema location
     */
    public static SchemaLocation of(String iri) {
        String[] iriParts = iri.split("#");
        AbsoluteIri absoluteIri = null;
        JsonNodePath fragment = JSON_POINTER;
        if (iriParts.length > 0) {
            absoluteIri = new AbsoluteIri(iriParts[0]);
        }
        if (iriParts.length > 1) {
            String[] fragmentParts = iriParts[1].split("/");

            boolean jsonPointer = false;
            if (iriParts[1].startsWith("/")) {
                // json pointer
                jsonPointer = true;
            } else {
                // anchor
                fragment = ANCHOR;
            }

            int index = -1;
            for (int fragmentPartIndex = 0; fragmentPartIndex < fragmentParts.length; fragmentPartIndex++) {
                if (fragmentPartIndex == 0 && jsonPointer) {
                    continue;
                }
                String fragmentPart = fragmentParts[fragmentPartIndex];
                for (int x = 0; x < fragmentPart.length(); x++) {
                    char ch = fragmentPart.charAt(x);
                    if (ch >= '0' && ch <= '9') {
                        if (x == 0) {
                            index = 0;
                        } else {
                            index = index * 10;
                        }
                        index += (ch - '0');
                    } else {
                        index = -1; // Not an index
                        break;
                    }
                }
                if (index != -1) {
                    fragment = fragment.append(index);
                } else {
                    fragment = fragment.append(fragmentPart.toString());
                }
            }
        }
        return new SchemaLocation(absoluteIri, fragment);
    }

    /**
     * Returns a builder for building {@link SchemaLocation}.
     * 
     * @return the builder
     */
    public static Builder builder() {
        return new Builder();
    }

    /**
     * Builder for building {@link SchemaLocation}.
     */
    public static class Builder {
        private AbsoluteIri absoluteIri;
        private JsonNodePath fragment;

        /**
         * Sets the canonical absolute IRI of the schema object.
         * <p>
         * This is a unique identifier indicated by the $id property or id property in
         * Draft 4 and earlier. This does not have to be network accessible.
         *
         * @param absoluteIri the canonical IRI of the schema object
         * @return the builder
         */
        protected Builder absoluteIri(AbsoluteIri absoluteIri) {
            this.absoluteIri = absoluteIri;
            return this;
        }

        /**
         * Sets the fragment.
         * 
         * @param fragment the fragment
         * @return the builder
         */
        protected Builder fragment(JsonNodePath fragment) {
            this.fragment = fragment;
            return this;
        }

        /**
         * Builds a {@link SchemaLocation}.
         * 
         * @return the schema location
         */
        public SchemaLocation build() {
            return new SchemaLocation(absoluteIri, fragment);
        }

    }

    @Override
    public String toString() {
        if (this.value == null) {
            if (this.absoluteIri != null && this.fragment == null) {
                this.value = this.absoluteIri.toString();
            } else {
                StringBuilder result = new StringBuilder();
                if (this.absoluteIri != null) {
                    result.append(this.absoluteIri.toString());
                }
                result.append("#");
                if (this.fragment != null) {
                    result.append(this.fragment.toString());
                }
                this.value = result.toString();
            }
        }
        return this.value;
    }

    @Override
    public int hashCode() {
        return Objects.hash(fragment, absoluteIri);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        SchemaLocation other = (SchemaLocation) obj;
        return Objects.equals(fragment, other.fragment) && Objects.equals(absoluteIri, other.absoluteIri);
    }
}
