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
 * Factory for the js {@link Context}.
 */
public class GraalJSContextFactory {
    /**
     * The holder defers the classloading until it is used.
     */
    private static class Holder {
        private static final Context INSTANCE = Context.newBuilder("js").option("engine.WarnInterpreterOnly", "false")
                .build();
    }

    /**
     * Gets the singleton instance of the Context.
     * <p>
     * This may need to be closed to release resources if no longer needed.
     *
     * @return the Context
     */
    public static Context getInstance() {
        return Holder.INSTANCE;
    }
}
