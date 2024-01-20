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
        return new AbsoluteIri(resolve(this.value, iri));
    }

    /**
     * Gets the scheme of the IRI.
     * 
     * @return the scheme
     */
    public String getScheme() {
        return getScheme(this.value);
    }

    /**
     * Returns the scheme and authority components of the IRI.
     * 
     * @return the scheme and authority components
     */
    protected String getSchemeAuthority() {
        return getSchemeAuthority(this.value);
    }

    /**
     * Constructs a new IRI by parsing the given string and then resolving it
     * against this IRI.
     *
     * @param parent the parent absolute IRI
     * @param iri    to resolve
     * @return the new absolute IRI
     */
    public static String resolve(String parent, String iri) {
        if (iri.contains(":")) {
            // IRI is absolute
            return iri;
        } else {
            // IRI is relative to this
            if (parent == null) {
                return iri;
            }
            if (iri.startsWith("/")) {
                // IRI is relative to this root
                return getSchemeAuthority(parent) + iri;
            } else {
                // IRI is relative to this path
                String base = parent;
                int scheme = parent.indexOf("://");
                if (scheme == -1) {
                    scheme = 0;
                } else {
                    scheme = scheme + 3;
                }
                base = parent(base, scheme);
                while (iri.startsWith("../")) {
                    base = parent(base, scheme);
                    iri = iri.substring(3);
                }
                return base + "/" + iri;
            }
        }
    }
    
    protected static String parent(String iri, int scheme) {
        int slash = iri.lastIndexOf('/');
        if (slash != -1 && slash > scheme) {
            return iri.substring(0, slash);
        }
        return iri;
    }

    /**
     * Returns the scheme and authority components of the IRI.
     * 
     * @param iri to parse
     * @return the scheme and authority components
     */
    protected static String getSchemeAuthority(String iri) {
        if (iri == null) {
            return "";
        }
        // iri refers to root
        int start = iri.indexOf("://");
        if (start == -1) {
            start = 0;
        } else {
            start = start + 3;
        }
        int end = iri.indexOf('/', start);
        return end != -1 ? iri.substring(0, end) : iri;
    }

    /**
     * Returns the scheme of the IRI.
     * 
     * @param iri to parse
     * @return the scheme
     */
    public static String getScheme(String iri) {
        if (iri == null) {
            return "";
        }
        // iri refers to root
        int start = iri.indexOf(":");
        if (start == -1) {
            return "";
        } else {
            return iri.substring(0, start);
        }
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
