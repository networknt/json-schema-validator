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
package com.networknt.schema.regex;

import org.graalvm.polyglot.Context;

/**
 * GraalJS {@link RegularExpressionFactory}.
 * <p>
 * This requires a dependency on org.graalvm.js:js which along with its
 * dependency libraries are 50 MB.
 */
public class GraalJSRegularExpressionFactory implements RegularExpressionFactory {
    private static class Holder {
        private static final GraalJSRegularExpressionFactory INSTANCE = new GraalJSRegularExpressionFactory();
    }

    private final GraalJSRegularExpressionContext context;

    public static GraalJSRegularExpressionFactory getInstance() {
        return Holder.INSTANCE;
    }

    /**
     * Constructor.
     * <p>
     * This uses the context from {@link GraalJSContextFactory#getInstance()}.
     * <p>
     * It is the caller's responsibility to release the context when it is no longer
     * required by using GraalJSContextFactory.getInstance().close().
     */
    public GraalJSRegularExpressionFactory() {
        this(GraalJSContextFactory.getInstance());
    }

    /**
     * Constructor.
     * <p>
     * It is the caller's responsibility to release the context when it is no longer
     * required.
     * 
     * @param context the context
     */
    public GraalJSRegularExpressionFactory(Context context) {
        this.context = new GraalJSRegularExpressionContext(context);
    }

    @Override
    public RegularExpression getRegularExpression(String regex) {
        return new GraalJSRegularExpression(regex, this.context);
    }
}
