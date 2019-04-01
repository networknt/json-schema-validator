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

package com.networknt.schema;

import java.text.MessageFormat;

public class CustomErrorMessageType implements ErrorMessageType {
    private final String errorCode;
    private final MessageFormat messageFormat;
    
    private CustomErrorMessageType(String errorCode, MessageFormat messageFormat) {
        this.errorCode = errorCode;
        this.messageFormat = messageFormat;
    }
    
    public static ErrorMessageType of(String errorCode) {
        return new CustomErrorMessageType(errorCode, null);
    }
    public static ErrorMessageType of(String errorCode, MessageFormat messageFormat) {
        return new CustomErrorMessageType(errorCode, messageFormat);
    }

    @Override
    public String getErrorCode() {
        return errorCode;
    }

    @Override
    public MessageFormat getMessageFormat() {
        return messageFormat;
    }

}