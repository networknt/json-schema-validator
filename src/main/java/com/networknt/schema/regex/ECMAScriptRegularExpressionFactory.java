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

import com.networknt.schema.utils.Classes;

/**
 * ECMAScript regular expression factory that chooses between GraalJS or Joni
 * implementations depending on which is on the classpath.
 */
public class ECMAScriptRegularExpressionFactory implements RegularExpressionFactory {
    private static final boolean JONI_PRESENT = Classes.isPresent("org.joni.Regex",
            ECMAScriptRegularExpressionFactory.class.getClassLoader());
    private static final boolean GRAALJS_PRESENT = Classes.isPresent("com.oracle.truffle.js.parser.GraalJSEvaluator",
            ECMAScriptRegularExpressionFactory.class.getClassLoader());

    private static final RegularExpressionFactory DELEGATE = GRAALJS_PRESENT
            ? GraalJSRegularExpressionFactory.getInstance()
            : JoniRegularExpressionFactory.getInstance();
    
    public static final ECMAScriptRegularExpressionFactory INSTANCE = new ECMAScriptRegularExpressionFactory();

    public static ECMAScriptRegularExpressionFactory getInstance() {
        if (!JONI_PRESENT && !GRAALJS_PRESENT) {
            throw new IllegalArgumentException(
                    "Either org.jruby.joni:joni or org.graalvm.js:js needs to be present in the classpath");
        }
        return INSTANCE;
    }

    @Override
    public RegularExpression getRegularExpression(String regex) {
        return DELEGATE.getRegularExpression(regex);
    }
}
