package com.networknt.schema.walk;

public enum WalkMethodInvocation {

    SKIP_WALK("SkipWalk", "Skip the walk methods"),

    CONTINUE_TO_WALK("ContinueToWalk", "continue to invoke the walk method");

    private String name;

    private String description;

    WalkMethodInvocation(String name, String description) {
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
