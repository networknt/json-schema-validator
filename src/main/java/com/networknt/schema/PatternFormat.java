package com.networknt.schema;

import java.util.regex.Pattern;

public class PatternFormat implements Format {
    private final String name; 
    private final Pattern pattern;
    
    public PatternFormat(String name, String regex) {
        this.name = name;
        this.pattern = Pattern.compile(regex);
    }
    
    @Override
    public boolean matches(String value) {
        return pattern.matcher(value).matches();
    }
    
    @Override
    public String getName() {
        return name;
    }
    @Override
    public String getErrorMessageDescription() {
        return pattern.pattern();
    }
}