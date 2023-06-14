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

import com.networknt.schema.utils.StringUtils;

import java.text.MessageFormat;
import java.util.Arrays;
import java.util.Map;

public class ValidationMessage {
    private String type;
    private String code;
    private String path;
    private String schemaPath;
    private String[] arguments;
    private Map<String, Object> details;
    private String message;

    ValidationMessage() {
    }

    public String getCode() {
        return code;
    }

    void setCode(String code) {
        this.code = code;
    }

    /**
     * @return The path to the input json
     */
    public String getPath() {
        return path;
    }

    void setPath(String path) {
        this.path = path;
    }

    /**
     * @return The path to the schema
     */
    public String getSchemaPath() {
        return schemaPath;
    }

    public void setSchemaPath(String schemaPath) {
        this.schemaPath = schemaPath;
    }

    public String[] getArguments() {
        return arguments;
    }

    void setArguments(String[] arguments) {
        this.arguments = arguments;
    }

    void setDetails(Map<String, Object> details) {
        this.details = details;
    }

    public Map<String, Object> getDetails() {
        return details;
    }

    public String getMessage() {
        return message;
    }

    void setMessage(String message) {
        this.message = message;
    }

    @Override
    public String toString() {
        return message;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ValidationMessage that = (ValidationMessage) o;

        if (type != null ? !type.equals(that.type) : that.type != null) return false;
        if (code != null ? !code.equals(that.code) : that.code != null) return false;
        if (path != null ? !path.equals(that.path) : that.path != null) return false;
        if (schemaPath != null ? !schemaPath.equals(that.schemaPath) : that.schemaPath != null) return false;
        if (details != null ? !details.equals(that.details) : that.details != null) return false;
        if (!Arrays.equals(arguments, that.arguments)) return false;
        return !(message != null ? !message.equals(that.message) : that.message != null);

    }

    @Override
    public int hashCode() {
        int result = type != null ? type.hashCode() : 0;
        result = 31 * result + (code != null ? code.hashCode() : 0);
        result = 31 * result + (path != null ? path.hashCode() : 0);
        result = 31 * result + (schemaPath != null ? schemaPath.hashCode() : 0);
        result = 31 * result + (details != null ? details.hashCode() : 0);
        result = 31 * result + (arguments != null ? Arrays.hashCode(arguments) : 0);
        result = 31 * result + (message != null ? message.hashCode() : 0);
        return result;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public static ValidationMessage ofWithCustom(String type, ErrorMessageType errorMessageType, MessageFormat messageFormat, String customMessage, String at, String schemaPath, String... arguments) {
        ValidationMessage.Builder builder = new ValidationMessage.Builder();
        builder.code(errorMessageType.getErrorCode()).path(at).schemaPath(schemaPath).arguments(arguments)
                .format(messageFormat).type(type)
                .customMessage(customMessage);
        return builder.build();
    }

    public static ValidationMessage of(String type, ErrorMessageType errorMessageType, MessageFormat messageFormat, String at, String schemaPath, String... arguments) {
        return ofWithCustom(type, errorMessageType, messageFormat, errorMessageType.getCustomMessage(), at, schemaPath, arguments);
    }

    public static ValidationMessage of(String type, ErrorMessageType errorMessageType, MessageFormat messageFormat, String at, String schemaPath, Map<String, Object> details) {
        ValidationMessage.Builder builder = new ValidationMessage.Builder();
        builder.code(errorMessageType.getErrorCode()).path(at).schemaPath(schemaPath).details(details)
                .format(messageFormat).type(type);
        return builder.build();
    }

    public static class Builder {
        private String type;
        private String code;
        private String path;
        private String schemaPath;
        private String[] arguments;
        private Map<String, Object> details;
        private MessageFormat format;
        private String customMessage;

        public Builder type(String type) {
            this.type = type;
            return this;
        }

        public Builder code(String code) {
            this.code = code;
            return this;
        }

        public Builder path(String path) {
            this.path = path;
            return this;
        }

        public Builder schemaPath(String schemaPath) {
            this.schemaPath = schemaPath;
            return this;
        }

        public Builder arguments(String... arguments) {
            this.arguments = arguments;
            return this;
        }

        public Builder details(Map<String, Object> details) {
            this.details = details;
            return this;
        }

        public Builder format(MessageFormat format) {
            this.format = format;
            return this;
        }

        public Builder customMessage(String customMessage) {
            this.customMessage = customMessage;
            return this;
        }

        public ValidationMessage build() {
            ValidationMessage msg = new ValidationMessage();
            msg.setType(type);
            msg.setCode(code);
            msg.setPath(path);
            msg.setSchemaPath(schemaPath);
            msg.setArguments(arguments);
            msg.setDetails(details);

            if (format != null) {
                String[] objs = new String[(arguments == null ? 0 : arguments.length) + 1];
                objs[0] = path;
                if (arguments != null) {
                    for (int i = 1; i < objs.length; i++) {
                        objs[i] = arguments[i - 1];
                    }
                }
                if(StringUtils.isNotBlank(customMessage)) {
                    msg.setMessage(customMessage);
                } else {
                    msg.setMessage(format.format(objs));
                }

            }

            return msg;
        }
    }
}
