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

package com.networknt.schema.serialization;

import java.math.BigDecimal;
import tools.jackson.core.TokenStreamLocation;
import tools.jackson.databind.node.DoubleNode;
import com.networknt.schema.serialization.node.TokenStreamLocationAware;

/**
 * Internal schema-reader metadata. All ordinary numeric operations, equality and
 * serialization retain DoubleNode semantics; only count validators use countValue.
 */
public class CountTokenNode extends DoubleNode {
    private static final long serialVersionUID = 1L;
    private final BigDecimal countValue;

    private CountTokenNode(double value, BigDecimal countValue) {
        super(value);
        this.countValue = countValue;
    }

    static CountTokenNode of(DoubleNode ordinary, BigDecimal countValue) {
        return ordinary instanceof TokenStreamLocationAware
                ? new Located(ordinary.doubleValue(), countValue,
                        ((TokenStreamLocationAware) ordinary).tokenStreamLocation())
                : new CountTokenNode(ordinary.doubleValue(), countValue);
    }

    public BigDecimal countValue() {
        return this.countValue;
    }

    private static final class Located extends CountTokenNode implements TokenStreamLocationAware {
        private static final long serialVersionUID = 1L;
        private final TokenStreamLocation location;

        Located(double value, BigDecimal countValue, TokenStreamLocation location) {
            super(value, countValue);
            this.location = location;
        }

        @Override
        public TokenStreamLocation tokenStreamLocation() {
            return this.location;
        }
    }
}
