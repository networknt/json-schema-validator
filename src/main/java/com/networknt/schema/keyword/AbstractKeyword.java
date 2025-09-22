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

package com.networknt.schema.keyword;

import java.util.Objects;

/**
 * Abstract keyword.
 */
public abstract class AbstractKeyword implements Keyword {
    private final String value;

    /**
     * Create abstract keyword.
     * 
     * @param value the keyword
     */
    public AbstractKeyword(String value) {
        this.value = value;
    }

    /**
     * Gets the keyword.
     * 
     * @return the keyword
     */
    public String getValue() {
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
        AbstractKeyword other = (AbstractKeyword) obj;
        return Objects.equals(value, other.value);
    }

    @Override
    public String toString() {
        return getValue();
    }
}
