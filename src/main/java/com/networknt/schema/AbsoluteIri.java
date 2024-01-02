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
 * The absolute IRI is an IRI without the fragment.
 * <p>
 * absolute-IRI = scheme ":" ihier-part [ "?" iquery ]
 * <p>
 * This does not attempt to validate whether the value really conforms to an
 * absolute IRI format as in earlier drafts the IDs are not defined as such.
 */
public class AbsoluteIri {
    private final String value;

    /**
     * Constructs a new IRI given the value.
     * 
     * @param value
     */
    public AbsoluteIri(String value) {
        this.value = value;
    }

    /**
     * Constructs a new IRI given the value.
     * 
     * @param iri the value
     * @return the new absolute IRI
     */
    public static AbsoluteIri of(String iri) {
        return new AbsoluteIri(iri);
    }

    /**
     * Constructs a new IRI by parsing the given string and then resolving it
     * against this IRI.
     * 
     * @param iri to resolve
     * @return the new absolute IRI
     */
    public AbsoluteIri resolve(String iri) {
        if (iri.contains(":")) {
            // IRI is absolute
            return new AbsoluteIri(iri);
        } else {
            // IRI is relative to this
            if (this.value == null) {
                return new AbsoluteIri(iri);
            }
            if (iri.startsWith("/")) {
                // IRI is relative to this root
                return new AbsoluteIri(getSchemeAuthority() + iri);
            } else {
                // IRI is relative to this path
                String base = this.value;
                int scheme = this.value.indexOf("://");
                if (scheme == -1) {
                    scheme = 0;
                } else {
                    scheme = scheme + 3;
                }
                int slash = this.value.lastIndexOf('/');
                if (slash != -1 && slash > scheme) {
                    base = this.value.substring(0, slash);
                }
                return new AbsoluteIri(base + "/" + iri);
            }
        }
    }

    /**
     * Returns the scheme and authority components of the IRI.
     * 
     * @return the scheme and authority components
     */
    protected String getSchemeAuthority() {
        if (this.value == null) {
            return "";
        }
        // iri refers to root
        int start = this.value.indexOf("://");
        if (start == -1) {
            start = 0;
        } else {
            start = start + 3;
        }
        int end = this.value.indexOf('/', start);
        return end != -1 ? this.value.substring(0, end) : this.value;
    }

    @Override
    public String toString() {
        return this.value;
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.value);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        AbsoluteIri other = (AbsoluteIri) obj;
        return Objects.equals(this.value, other.value);
    }

}
