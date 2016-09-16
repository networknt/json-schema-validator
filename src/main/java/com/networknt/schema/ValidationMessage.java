package com.networknt.schema;

import java.text.MessageFormat;
import java.util.Arrays;

public class ValidationMessage {
    private String type;
    private String code;
    private String path;
    private String[] arguments;
    private String message;

    ValidationMessage() {
    }

    public String getCode() {
        return code;
    }

    void setCode(String code) {
        this.code = code;
    }

    public String getPath() {
        return path;
    }

    void setPath(String path) {
        this.path = path;
    }

    String[] getArguments() {
        return arguments;
    }

    public void setArguments(String[] arguments) {
        this.arguments = arguments;
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
        // Probably incorrect - comparing Object[] arrays with Arrays.equals
        if (!Arrays.equals(arguments, that.arguments)) return false;
        return !(message != null ? !message.equals(that.message) : that.message != null);

    }

    @Override
    public int hashCode() {
        int result = type != null ? type.hashCode() : 0;
        result = 31 * result + (code != null ? code.hashCode() : 0);
        result = 31 * result + (path != null ? path.hashCode() : 0);
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

    public static class Builder {
        private String type;
        private String code;
        private String path;
        private String[] arguments;
        private MessageFormat format;

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

        public Builder arguments(String... arguments) {
            this.arguments = arguments;
            return this;
        }

        public Builder format(MessageFormat format) {
            this.format = format;
            return this;
        }

        public ValidationMessage build() {
            ValidationMessage msg = new ValidationMessage();
            msg.setType(type);
            msg.setCode(code);
            msg.setPath(path);
            msg.setArguments(arguments);

            if (format != null) {
                String[] objs = new String[(arguments == null ? 0 : arguments.length) + 1];
                objs[0] = path;
                if (arguments != null) {
                    for (int i = 1; i < objs.length; i++) {
                        objs[i] = arguments[i - 1];
                    }
                }
                msg.setMessage(format.format(objs));
            }

            return msg;
        }
    }
}
