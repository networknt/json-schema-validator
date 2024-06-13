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
import org.graalvm.polyglot.Value;

/**
 * GraalJSRegularExpressionContext.
 */
public class GraalJSRegularExpressionContext {
    private static final String SOURCE = "pattern => {\n"
            + "    const regex = new RegExp(pattern, 'u');\n"
            + "    return text => text.match(regex)\n"
            + "};";

    private final Context context;
    private final Value regExpBuilder;

    /**
     * Constructor.
     * <p>
     * It is the caller's responsibility to release the context when it is no longer
     * required.
     * 
     * @param context the context
     */
    public GraalJSRegularExpressionContext(Context context) {
        this.context = context;
        synchronized(this.context) {
            this.regExpBuilder = this.context.eval("js", SOURCE);
        }
    }

    /**
     * Operations must synchronize on the {@link Context} as only a single thread
     * can access the {@link Context} and {@link #getRegExpBuilder()} at one time.
     * 
     * @return the context
     */
    public Context getContext() {
        return context;
    }

    /**
     * Gets the RegExp builder.
     * 
     * @return the regexp builder
     */
    public Value getRegExpBuilder() {
        return regExpBuilder;
    }
}
