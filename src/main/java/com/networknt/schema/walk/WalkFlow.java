package com.networknt.schema.walk;

public enum WalkFlow {

    SKIP("SkipWalk", "Skip only the walk method, but continue invoking the other listeners"),

    ABORT("Abort", "Aborts all the walk listeners and walk method itself"),

    CONTINUE("ContinueToWalk", "continue to invoke the walk method and other listeners");

    private final String name;

    private final String description;

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
