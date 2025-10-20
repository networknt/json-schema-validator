package com.networknt.schema.walk;

public class ApplyDefaultsStrategy {
    public static final ApplyDefaultsStrategy EMPTY_APPLY_DEFAULTS_STRATEGY = new ApplyDefaultsStrategy(false, false, false);

    private final boolean applyPropertyDefaults;
    private final boolean applyPropertyDefaultsIfNull;
    private final boolean applyArrayDefaults;

    /**
     * Specify which default values to apply.
     * We can apply property defaults only if they are missing or if they are declared to be null in the input json,
     * and we can apply array defaults if they are declared to be null in the input json.
     *
     * <p>Note that the walker changes the input object in place.
     * If validation fails, the input object will be changed.
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

    public static Builder builder(ApplyDefaultsStrategy copy) {
        if (copy == null) {
            return new Builder();
        }
        Builder builder = new Builder();
        builder.applyArrayDefaults = copy.applyArrayDefaults;
        builder.applyPropertyDefaults = copy.applyPropertyDefaults;
        builder.applyPropertyDefaultsIfNull = copy.applyPropertyDefaultsIfNull;
        return builder;
    }

    public static Builder builder() {
        return new Builder();
    }
    
    public static class Builder {
        private boolean applyPropertyDefaults = false;
        private boolean applyPropertyDefaultsIfNull = false;
        private boolean applyArrayDefaults = false;

        public Builder applyArrayDefaults(boolean applyArrayDefaults) {
            this.applyArrayDefaults = applyArrayDefaults;
            return this;
        }
        public Builder applyPropertyDefaults(boolean applyPropertyDefaults) {
            this.applyPropertyDefaults = applyPropertyDefaults;
            if (!applyPropertyDefaults) {
                this.applyPropertyDefaultsIfNull = false;
            }
            return this;
        }
        public Builder applyPropertyDefaultsIfNull(boolean applyPropertyDefaultsIfNull) {
            this.applyPropertyDefaultsIfNull = applyPropertyDefaultsIfNull;
            if (applyPropertyDefaultsIfNull) {
                this.applyPropertyDefaults = true;
            }
            return this;
        }

        public ApplyDefaultsStrategy build() {
            return new ApplyDefaultsStrategy(applyPropertyDefaults, applyPropertyDefaultsIfNull, applyArrayDefaults);
        }
    }
}
