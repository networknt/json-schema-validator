/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.networknt.schema.format;

import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import com.networknt.schema.ExecutionContext;
import com.networknt.schema.Format;

/**
 * Format for regex.
 */
public class RegexFormat implements Format {
    @Override
    public boolean matches(ExecutionContext executionContext, String value) {
        if (null == value) return true;
        try {
            Pattern.compile(value);
            return true;

        } catch (PatternSyntaxException e) {
            return false;
        }
    }

    @Override
    public String getName() {
        return "regex";
    }

    @Override
    public String getMessageKey() {
        return "format.regex";
    }
}
