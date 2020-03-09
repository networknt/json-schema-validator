package com.networknt.schema;

import java.util.Set;

public class ValidationResult {

    private Set<ValidationMessage> validationMessages;

    private CollectorContext collectorContext;

    public ValidationResult(Set<ValidationMessage> validationMessages, CollectorContext collectorContext) {
        super();
        this.validationMessages = validationMessages;
        this.collectorContext = collectorContext;
    }

    public Set<ValidationMessage> getValidationMessages() {
        return validationMessages;
    }

    public CollectorContext getCollectorContext() {
        return collectorContext;
    }

}
