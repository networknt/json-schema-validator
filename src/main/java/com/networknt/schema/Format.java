package com.networknt.schema;

public interface Format {
    /**
     * @return the format name as referred to in a json schema format node.
     */
    String getName();
    
    boolean matches(String value);
    
    String getErrorMessageDescription();
}
