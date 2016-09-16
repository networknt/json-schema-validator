package com.networknt.schema;

public class JsonSchemaException extends RuntimeException {
    private static final long serialVersionUID = -7805792737596582110L;

    public JsonSchemaException(String message) {
        super(message);
    }

    public JsonSchemaException(Throwable throwable) {
        super(throwable);
    }

}
