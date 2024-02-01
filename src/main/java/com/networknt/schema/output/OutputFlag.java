package com.networknt.schema.output;

import java.util.Objects;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.networknt.schema.serialization.JsonMapperFactory;

/**
 * The Flag output results.
 */
public class OutputFlag {
    private final boolean valid;

    public OutputFlag(boolean valid) {
        this.valid = valid;
    }

    public boolean isValid() {
        return this.valid;
    }

    @Override
    public int hashCode() {
        return Objects.hash(valid);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        OutputFlag other = (OutputFlag) obj;
        return valid == other.valid;
    }

    @Override
    public String toString() {
        try {
            return JsonMapperFactory.getInstance().writerWithDefaultPrettyPrinter().writeValueAsString(this);
        } catch (JsonProcessingException e) {
            return "OutputFlag [valid=" + valid + "]";
        }
    }
}