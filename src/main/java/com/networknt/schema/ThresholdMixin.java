package com.networknt.schema;

import com.fasterxml.jackson.databind.JsonNode;

public interface ThresholdMixin {
    boolean crossesThreshold(JsonNode node);
    String thresholdValue();
}
