package com.networknt.schema;

public abstract class AbstractFormat implements Format{
    private final String name;
    private final String errorMessageDescription;
    
    public AbstractFormat(String name) {
        this(name, "");
    }
    
    public AbstractFormat(String name, String errorMessageDescription) {
        this.name = name;
        this.errorMessageDescription = errorMessageDescription;
    }
    
    @Override
    public String getName() {
        return name;
    }
    
    @Override
    public String getErrorMessageDescription() {
        return errorMessageDescription;
    }
}
