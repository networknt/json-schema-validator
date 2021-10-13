package com.networknt.schema;

public class ApplyDefaultsStrategy {
    private final boolean applyPropertyDefaults;
    private final boolean applyPropertyDefaultsIfNull;
    private final boolean applyArrayDefaults;

    /**
     * Specify which default values to apply.
     *
     * @param applyPropertyDefaults if true then apply defaults inside json objects if the attribute is missing
     * @param applyPropertyDefaultsIfNull if true then apply defaults inside json objects if the attribute is explicitly null
     * @param applyArrayDefaults if true then apply defaults inside json arrays if the attribute is explicitly null
     * @throws IllegalArgumentException if applyPropertyDefaults is false and applyPropertyDefaultsIfNull is true
     */
    public ApplyDefaultsStrategy(boolean applyPropertyDefaults, boolean applyPropertyDefaultsIfNull, boolean applyArrayDefaults) {
        if (!applyPropertyDefaults && applyPropertyDefaultsIfNull) {
            throw new IllegalArgumentException();
        }
        this.applyPropertyDefaults = applyPropertyDefaults;
        this.applyPropertyDefaultsIfNull = applyPropertyDefaultsIfNull;
        this.applyArrayDefaults = applyArrayDefaults;
    }

    public boolean shouldApplyPropertyDefaults() {
        return applyPropertyDefaults;
    }

    public boolean shouldApplyPropertyDefaultsIfNull() {
        return applyPropertyDefaultsIfNull;
    }

    public boolean shouldApplyArrayDefaults() {
        return applyArrayDefaults;
    }
}
