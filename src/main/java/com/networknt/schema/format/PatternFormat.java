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

package com.networknt.schema.format;

import java.util.regex.Pattern;

public class PatternFormat extends AbstractFormat {
    private final Pattern pattern;

    public PatternFormat(String name, String regex, String errorMessageDescription) {
        super(name, null != errorMessageDescription ? errorMessageDescription : regex);
        this.pattern = Pattern.compile(regex);
    }

    @Override
    public boolean matches(String value) {
        return this.pattern.matcher(value).matches();
    }

}
