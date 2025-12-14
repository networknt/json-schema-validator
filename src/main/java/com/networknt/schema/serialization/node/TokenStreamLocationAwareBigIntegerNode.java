/*
 * Copyright (c) 2024 the original author or authors.
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
package com.networknt.schema.serialization.node;

import java.math.BigInteger;

import tools.jackson.core.TokenStreamLocation;
import tools.jackson.databind.node.BigIntegerNode;

/**
 * {@link BigIntegerNode} that is {@link TokenStreamLocationAware}.
 */
public class TokenStreamLocationAwareBigIntegerNode extends BigIntegerNode implements TokenStreamLocationAware {
    /**
     * 
     */
    private static final long serialVersionUID = 1L;
    private final TokenStreamLocation tokenStreamLocation;

    public TokenStreamLocationAwareBigIntegerNode(BigInteger v, TokenStreamLocation tokenStreamLocation) {
        super(v);
        this.tokenStreamLocation = tokenStreamLocation;
    }

    @Override
    public TokenStreamLocation tokenStreamLocation() {
        return this.tokenStreamLocation;
    }
}
