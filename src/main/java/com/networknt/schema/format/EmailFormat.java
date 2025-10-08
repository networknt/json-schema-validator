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

import com.networknt.org.apache.commons.validator.routines.EmailValidator;
import com.networknt.schema.ExecutionContext;

/**
 * Format for email.
 */
public class EmailFormat implements Format {
    private final EmailValidator emailValidator;

    public EmailFormat() {
        this(new IPv6AwareEmailValidator(true, true));
    }
    
    public EmailFormat(EmailValidator emailValidator) {
        this.emailValidator = emailValidator;
    }

    @Override
    public boolean matches(ExecutionContext executionContext, String value) {
        return this.emailValidator.isValid(value);
    }
    
    @Override
    public String getName() {
        return "email";
    }

    @Override
    public String getMessageKey() {
        return "format.email";
    }
}
