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

import com.networknt.schema.i18n.MessageFormatter;
import com.networknt.schema.utils.CachingSupplier;
import com.networknt.schema.utils.StringUtils;

import java.text.MessageFormat;
import java.util.Arrays;
import java.util.Map;
import java.util.function.Supplier;

/**
 * The output format.
 * 
 * @see <a href=
 *      "https://github.com/json-schema-org/json-schema-spec/blob/main/jsonschema-validation-output-machines.md">JSON
 *      Schema</a>
 */
public class ValidationMessage {
    private final String type;
    private final String code;
    private final JsonNodePath evaluationPath;
    private final JsonNodePath schemaLocation;
    private final JsonNodePath instanceLocation;
    private final String property;
    private final Object[] arguments;
    private final Map<String, Object> details;
    private final String messageKey;
    private final Supplier<String> messageSupplier;

    ValidationMessage(String type, String code, JsonNodePath evaluationPath, JsonNodePath schemaLocation,
            JsonNodePath instanceLocation, String property, Object[] arguments, Map<String, Object> details,
            String messageKey, Supplier<String> messageSupplier) {
        super();
        this.type = type;
        this.code = code;
        this.instanceLocation = instanceLocation;
        this.schemaLocation = schemaLocation;
        this.evaluationPath = evaluationPath;
        this.property = property;
        this.arguments = arguments;
        this.details = details;
        this.messageKey = messageKey;
        this.messageSupplier = messageSupplier;
    }

    public String getCode() {
        return code;
    }

    /**
     * The instance location is the location of the JSON value within the root
     * instance being validated.
     * 
     * @return The path to the input json
     */
    public JsonNodePath getInstanceLocation() {
        return instanceLocation;
    }

    /**
     * The evaluation path is the set of keys, starting from the schema root,
     * through which evaluation passes to reach the schema object that produced a
     * specific result.
     * 
     * @return the evaluation path
     */
    public JsonNodePath getEvaluationPath() {
        return evaluationPath;
    }
    
    /**
     * The schema location is the canonical URI of the schema object plus a JSON
     * Pointer fragment indicating the subschema that produced a result. In contrast
     * with the evaluation path, the schema location MUST NOT include by-reference
     * applicators such as $ref or $dynamicRef.
     * 
     * @return the schema location
     */
    public JsonNodePath getSchemaLocation() {
        return schemaLocation;
    }
    
    public String getProperty() {
        return property;
    }

    public Object[] getArguments() {
        return arguments;
    }

    public Map<String, Object> getDetails() {
        return details;
    }

    public String getMessage() {
        return messageSupplier.get();
    }

    public String getMessageKey() {
        return messageKey;
    }
    
    public boolean isValid() {
        return messageSupplier != null;
    }

    @Override
    public String toString() {
        return messageSupplier.get();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ValidationMessage that = (ValidationMessage) o;

        if (type != null ? !type.equals(that.type) : that.type != null) return false;
        if (code != null ? !code.equals(that.code) : that.code != null) return false;
        if (instanceLocation != null ? !instanceLocation.equals(that.instanceLocation) : that.instanceLocation != null) return false;
        if (evaluationPath != null ? !evaluationPath.equals(that.evaluationPath) : that.evaluationPath != null) return false;
        if (details != null ? !details.equals(that.details) : that.details != null) return false;
        if (messageKey != null ? !messageKey.equals(that.messageKey) : that.messageKey != null) return false;
        if (!Arrays.equals(arguments, that.arguments)) return false;
        return true;
    }

    @Override
    public int hashCode() {
        int result = type != null ? type.hashCode() : 0;
        result = 31 * result + (code != null ? code.hashCode() : 0);
        result = 31 * result + (instanceLocation != null ? instanceLocation.hashCode() : 0);
        result = 31 * result + (evaluationPath != null ? evaluationPath.hashCode() : 0);
        result = 31 * result + (details != null ? details.hashCode() : 0);
        result = 31 * result + (arguments != null ? Arrays.hashCode(arguments) : 0);
        result = 31 * result + (messageKey != null ? messageKey.hashCode() : 0);
        return result;
    }

    public String getType() {
        return type;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder extends BuilderSupport<Builder> {
        @Override
        public Builder self() {
            return this;
        }
    }

    public static abstract class BuilderSupport<S> {
        public abstract S self();

        protected String type;
        protected String code;
        protected JsonNodePath evaluationPath;
        protected JsonNodePath schemaLocation;
        protected JsonNodePath instanceLocation;
        protected String property;
        protected Object[] arguments;
        protected Map<String, Object> details;
        protected MessageFormat format;
        protected String message;
        protected Supplier<String> messageSupplier;
        protected MessageFormatter messageFormatter;
        protected String messageKey;

        public S type(String type) {
            this.type = type;
            return self();
        }

        public S code(String code) {
            this.code = code;
            return self();
        }

        /**
         * The instance location is the location of the JSON value within the root
         * instance being validated.
         * 
         * @return The path to the input json
         */
        public S instanceLocation(JsonNodePath instanceLocation) {
            this.instanceLocation = instanceLocation;
            return self();
        }
        
        /**
         * The schema location is the canonical URI of the schema object plus a JSON
         * Pointer fragment indicating the subschema that produced a result. In contrast
         * with the evaluation path, the schema location MUST NOT include by-reference
         * applicators such as $ref or $dynamicRef.
         */
        public S schemaLocation(JsonNodePath schemaLocation) {
            this.schemaLocation = schemaLocation;
            return self();
        }

        /**
         * The evaluation path is the set of keys, starting from the schema root,
         * through which evaluation passes to reach the schema object that produced a
         * specific result.
         * 
         * @return the evaluation path
         */
        public S evaluationPath(JsonNodePath evaluationPath) {
            this.evaluationPath = evaluationPath;
            return self();
        }
        
        public S property(String property) {
            this.property = property;
            return self();
        }

        public S arguments(Object... arguments) {
            this.arguments = arguments;
            return self();
        }

        public S details(Map<String, Object> details) {
            this.details = details;
            return self();
        }

        public S format(MessageFormat format) {
            this.format = format;
            return self();
        }

        @Deprecated
        public S customMessage(String message) {
            return message(message);
        }

        /**
         * Explicitly sets the message pattern to be used.
         * <p>
         * If set the message supplier and message formatter will be ignored.
         * 
         * @param message the message pattern
         * @return the builder
         */
        public S message(String message) {
            this.message = message;
            return self();
        }

        public S messageSupplier(Supplier<String> messageSupplier) {
            this.messageSupplier = messageSupplier;
            return self();
        }

        public S messageFormatter(MessageFormatter messageFormatter) {
            this.messageFormatter = messageFormatter;
            return self();
        }

        public S messageKey(String messageKey) {
            this.messageKey = messageKey;
            return self();
        }

        public ValidationMessage build() {
            Supplier<String> messageSupplier = this.messageSupplier;
            String messageKey = this.messageKey;
            
            if (StringUtils.isNotBlank(this.message)) {
                messageKey = this.message;
                if (this.message.contains("{")) {
                    Object[] objs = getMessageArguments();
                    MessageFormat format = new MessageFormat(this.message);
                    messageSupplier = new CachingSupplier<>(() -> format.format(objs));
                } else {
                    messageSupplier = message::toString;
                }
            } else if (messageSupplier == null) {
                Object[] objs = getMessageArguments();
                MessageFormatter formatter = this.messageFormatter != null ? this.messageFormatter : format::format;
                messageSupplier = new CachingSupplier<>(() -> formatter.format(objs));
            }
            return new ValidationMessage(type, code, evaluationPath, schemaLocation, instanceLocation,
                    property, arguments, details, messageKey, messageSupplier);
        }
        
        protected Object[] getMessageArguments() {
            Object[] objs = new Object[(arguments == null ? 0 : arguments.length) + 1];
            objs[0] = instanceLocation;
            if (arguments != null) {
                for (int i = 1; i < objs.length; i++) {
                    objs[i] = arguments[i - 1];
                }
            }
            return objs;
        }

        protected String getType() {
            return type;
        }

        protected String getCode() {
            return code;
        }

        protected JsonNodePath getEvaluationPath() {
            return evaluationPath;
        }

        protected JsonNodePath getSchemaLocation() {
            return schemaLocation;
        }

        protected JsonNodePath getInstanceLocation() {
            return instanceLocation;
        }

        protected String getProperty() {
            return property;
        }

        protected Object[] getArguments() {
            return arguments;
        }

        protected Map<String, Object> getDetails() {
            return details;
        }

        protected MessageFormat getFormat() {
            return format;
        }

        protected String getMessage() {
            return message;
        }

        protected Supplier<String> getMessageSupplier() {
            return messageSupplier;
        }

        protected MessageFormatter getMessageFormatter() {
            return messageFormatter;
        }

        protected String getMessageKey() {
            return messageKey;
        }
    }
}
