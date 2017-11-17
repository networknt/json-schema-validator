package com.networknt.schema;


public abstract class AbstractKeyword implements Keyword {
    private final String value;
    
    public AbstractKeyword(String value) {
        this.value = value;
    }
    
    public String getValue() {
        return value;
    }
    
}
