package com.networknt.schema;
import com.fasterxml.jackson.databind.JsonNode;

abstract class AnnotationValueHandler {
    public abstract Object getAnnotationValue(JsonNode schemaNode);
}

class TextualValueHandler extends AnnotationValueHandler {
    @Override
    public Object getAnnotationValue(JsonNode schemaNode) {
        return schemaNode.textValue();
    }
}

class NumericValueHandler extends AnnotationValueHandler {
    @Override
    public Object getAnnotationValue(JsonNode schemaNode) {
        return schemaNode.numberValue();
    }
}

class ObjectValueHandler extends AnnotationValueHandler {
    @Override
    public Object getAnnotationValue(JsonNode schemaNode) {
        return schemaNode;
    }
} 

