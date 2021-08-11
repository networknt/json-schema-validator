package com.networknt.schema.utils;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.networknt.schema.SchemaValidatorsConfig;

import java.util.Iterator;

public class JsonNodeUtil {

    public static boolean isNodeNullable(JsonNode schema){
        JsonNode nullable = schema.get("nullable");
        if (nullable != null && nullable.asBoolean()) {
            return true;
        }
        return false;
    }

    //Check to see if a JsonNode is nullable with checking the isHandleNullableField
    public static boolean isNodeNullable(JsonNode schema, SchemaValidatorsConfig config){
        // check if the parent schema declares the fields as nullable
        if (config.isHandleNullableField()) {
            return isNodeNullable(schema);
        }
        return false;
    }

    //Check to see if any child node for the OneOf SchemaNode is nullable
    public static boolean isChildNodeNullable(ArrayNode oneOfSchemaNode,SchemaValidatorsConfig config){
        Iterator iterator = oneOfSchemaNode.elements();
        while(iterator.hasNext()){
            //If one of the child Node for oneOf is nullable, it means the whole oneOf is nullable
            if (isNodeNullable((JsonNode)iterator.next(),config)) return true;
        }
        return false;
    }
}
