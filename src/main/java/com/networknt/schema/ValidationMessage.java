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

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
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
 *      "https://github.com/json-schema-org/json-schema-spec/blob/main/output/jsonschema-validation-output-machines.md">JSON
 *      Schema</a>
 */
@JsonIgnoreProperties({ "messageSupplier", "schemaNode", "instanceNode", "valid", "error" })
@JsonPropertyOrder({ "type", "code", "message", "instanceLocation", "property", "evaluationPath", "schemaLocation",
        "messageKey", "arguments", "details" })
@JsonInclude(Include.NON_NULL)
public class ValidationMessage {
    private final String type;
    private final String code;
    @JsonSerialize(using = ToStringSerializer.class)
    private final JsonNodePath evaluationPath;
    @JsonSerialize(using = ToStringSerializer.class)
    private final SchemaLocation schemaLocation;
    @JsonSerialize(using = ToStringSerializer.class)
    private final JsonNodePath instanceLocation;
    private final String property;
    private final Object[] arguments;
    private final String messageKey;
    private final Supplier<String> messageSupplier;
    private final Map<String, Object> details;
    private final JsonNode instanceNode;
    private final JsonNode schemaNode;

    ValidationMessage(String type, String code, JsonNodePath evaluationPath, SchemaLocation schemaLocation,
            JsonNodePath instanceLocation, String property, Object[] arguments, Map<String, Object> details,
            String messageKey, Supplier<String> messageSupplier, JsonNode instanceNode, JsonNode schemaNode) {
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
        this.instanceNode = instanceNode;
        this.schemaNode = schemaNode;
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
     * The schema location is the canonical IRI of the schema object plus a JSON
     * Pointer fragment indicating the subschema that produced a result. In contrast
     * with the evaluation path, the schema location MUST NOT include by-reference
     * applicators such as $ref or $dynamicRef.
     * 
     * @return the schema location
     */
    public SchemaLocation getSchemaLocation() {
        return schemaLocation;
    }
    
    /**
     * Returns the instance node which was evaluated.
     * <p>
     * This corresponds with the instance location.
     * 
     * @return the instance node
     */
    public JsonNode getInstanceNode() {
        return instanceNode;
    }
    
    /**
     * Returns the schema node which was evaluated.
     * <p>
     * This corresponds with the schema location.
     * 
     * @return the schema node
     */
    public JsonNode getSchemaNode() {
        return schemaNode;
    }
    
    /**
     * Returns the property with the error.
     * <p>
     * For instance, for the required validator the instance location does not
     * contain the missing property name as the instance must refer to the input
     * data.
     * 
     * @return the property name
     */
    public String getProperty() {
        return property;
    }

    public Object[] getArguments() {
        return arguments;
    }

    public Map<String, Object> getDetails() {
        return details;
    }

    /**
     * Gets the formatted error message.
     * 
     * @return the error message
     */
    public String getMessage() {
        return messageSupplier.get();
    }

    public String getMessageKey() {
        return messageKey;
    }
    
    public boolean isValid() {
        return messageSupplier != null;
    }

    /**
     * Gets the error.
     *
     * @return the error
     */
    public String getError() {
        String message = getMessage();
        int index = message.indexOf(':');
        if (index != -1) {
            int length = message.length();
            while (index + 1 < length) {
                if (message.charAt(index + 1) == ' ') {
                    index++;
                } else {
                    break;
                }
            }
            return message.substring(index + 1);
        }
        return message;
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
	    return Arrays.equals(arguments, that.arguments);
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
        protected SchemaLocation schemaLocation;
        protected JsonNodePath instanceLocation;
        protected String property;
        protected Object[] arguments;
        protected Map<String, Object> details;
        protected MessageFormat format;
        protected String message;
        protected Supplier<String> messageSupplier;
        protected MessageFormatter messageFormatter;
        protected String messageKey;
        protected JsonNode instanceNode;
        protected JsonNode schemaNode;

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
         * @param instanceLocation the instance location
         * @return the builder
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
         * 
         * @param schemaLocation the schema location
         * @return the builder
         */
        public S schemaLocation(SchemaLocation schemaLocation) {
            this.schemaLocation = schemaLocation;
            return self();
        }

        /**
         * The evaluation path is the set of keys, starting from the schema root,
         * through which evaluation passes to reach the schema object that produced a
         * specific result.
         *
         * @param evaluationPath the evaluation path
         * @return the builder
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
        
        public S instanceNode(JsonNode instanceNode) {
            this.instanceNode = instanceNode;
            return self();
        }
        
        public S schemaNode(JsonNode schemaNode) {
            this.schemaNode = schemaNode;
            return self();
        }

        public ValidationMessage build() {
            Supplier<String> messageSupplier = this.messageSupplier;
            String messageKey = this.messageKey;
            
            if (StringUtils.isNotBlank(this.message)) {
                messageKey = this.message;
                if (this.message.contains("{")) {
                    messageSupplier = new CachingSupplier<>(() -> {
                        MessageFormat format = new MessageFormat(this.message);
                        return format.format(getMessageArguments());
                    });
                } else {
                    messageSupplier = message::toString;
                }
            } else if (messageSupplier == null) {
                messageSupplier = new CachingSupplier<>(() -> {
                    MessageFormatter formatter = this.messageFormatter != null ? this.messageFormatter : format::format;
                    return formatter.format(getMessageArguments());
                });
            }
            return new ValidationMessage(type, code, evaluationPath, schemaLocation, instanceLocation,
                    property, arguments, details, messageKey, messageSupplier, this.instanceNode, this.schemaNode);
        }
        
        protected Object[] getMessageArguments() {
            Object[] objs = new Object[(arguments == null ? 0 : arguments.length) + 1];
            objs[0] = instanceLocation;
            if (arguments != null) {
	            System.arraycopy(arguments, 0, objs, 1, objs.length - 1);
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

        protected SchemaLocation getSchemaLocation() {
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
