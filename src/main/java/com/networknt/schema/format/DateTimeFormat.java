package com.networknt.schema.format;

import java.util.Collections;
import java.util.Set;
import java.util.function.Supplier;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ethlo.time.ITU;
import com.ethlo.time.LeapSecondException;
import com.fasterxml.jackson.databind.JsonNode;
import com.networknt.schema.ExecutionContext;
import com.networknt.schema.FormatValidator;
import com.networknt.schema.JsonNodePath;
import com.networknt.schema.JsonType;
import com.networknt.schema.TypeFactory;
import com.networknt.schema.MessageSourceValidationMessage.Builder;
import com.networknt.schema.ValidationContext;
import com.networknt.schema.ValidationMessage;

/**
 * DateTimeFormat.
 */
public class DateTimeFormat extends BaseFormat {
    private static final Logger logger = LoggerFactory.getLogger(DateTimeFormat.class);
    private static final String DATETIME = "date-time";

    public DateTimeFormat() {
        super(DATETIME, null);
    }

    @Override
    public Set<ValidationMessage> validate(ExecutionContext executionContext, ValidationContext validationContext,
            JsonNode node, JsonNode rootNode, JsonNodePath instanceLocation, boolean assertionsEnabled,
            Supplier<Builder> message, FormatValidator formatValidator) {
        JsonType nodeType = TypeFactory.getValueNodeType(node, validationContext.getConfig());
        if (nodeType != JsonType.STRING) {
            return Collections.emptySet();
        }

        if (assertionsEnabled) {
            if (!isLegalDateTime(node.textValue())) {
                return Collections.singleton(message.get().arguments(node.textValue(), DATETIME)
                        .messageKey("dateTime").build());
            }
        }
        return Collections.emptySet();
    }

    private static boolean isLegalDateTime(String string) {
        try {
            try {
                ITU.parseDateTime(string);
            } catch (LeapSecondException ex) {
                if (!ex.isVerifiedValidLeapYearMonth()) {
                    return false;
                }
            }

            return true;
        } catch (Exception ex) {
            logger.debug("Invalid {}: {}", DATETIME, ex.getMessage());
            return false;
        }
    }
}
