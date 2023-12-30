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
 */
public class AbsoluteIri {
    private final String value;

    public AbsoluteIri(String value) {
        this.value = value;
    }

    public static AbsoluteIri of(String iri) {
        return new AbsoluteIri(iri);
    }

    @Override
    public String toString() {
        return this.value;
    }

    @Override
    public int hashCode() {
        return Objects.hash(value);
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
        return Objects.equals(value, other.value);
    }

}
