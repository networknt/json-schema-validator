package com.networknt.schema.walk;

public enum WalkFlow {

    SKIP("SkipWalk", "Skip the walk methods"),

    CONTINUE("ContinueToWalk", "continue to invoke the walk method");

    private String name;

    private String description;

    WalkFlow(String name, String description) {
        this.name = name;
        this.description = description;
    }

    public String getName() {
        return this.name;
    }

    public String getDescription() {
        return this.description;
    }

}
