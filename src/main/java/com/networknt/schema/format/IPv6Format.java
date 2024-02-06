package com.networknt.schema.format;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.function.Supplier;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.fasterxml.jackson.databind.JsonNode;
import com.networknt.schema.ExecutionContext;
import com.networknt.schema.FormatValidator;
import com.networknt.schema.JsonNodePath;
import com.networknt.schema.MessageSourceValidationMessage.Builder;
import com.networknt.schema.ValidationContext;
import com.networknt.schema.ValidationMessage;

public class IPv6Format extends BaseFormat {
    public static final String IPV6_PATTERN = "^\\s*((([0-9A-Fa-f]{1,4}:){7}([0-9A-Fa-f]{1,4}|:))|(([0-9A-Fa-f]{1,4}:){6}(:[0-9A-Fa-f]{1,4}|((25[0-5]|2[0-4]\\d|1\\d\\d|[1-9]?\\d)(\\.(25[0-5]|2[0-4]\\d|1\\d\\d|[1-9]?\\d)){3})|:))|(([0-9A-Fa-f]{1,4}:){5}(((:[0-9A-Fa-f]{1,4}){1,2})|:((25[0-5]|2[0-4]\\d|1\\d\\d|[1-9]?\\d)(\\.(25[0-5]|2[0-4]\\d|1\\d\\d|[1-9]?\\d)){3})|:))|(([0-9A-Fa-f]{1,4}:){4}(((:[0-9A-Fa-f]{1,4}){1,3})|((:[0-9A-Fa-f]{1,4})?:((25[0-5]|2[0-4]\\d|1\\d\\d|[1-9]?\\d)(\\.(25[0-5]|2[0-4]\\d|1\\d\\d|[1-9]?\\d)){3}))|:))|(([0-9A-Fa-f]{1,4}:){3}(((:[0-9A-Fa-f]{1,4}){1,4})|((:[0-9A-Fa-f]{1,4}){0,2}:((25[0-5]|2[0-4]\\d|1\\d\\d|[1-9]?\\d)(\\.(25[0-5]|2[0-4]\\d|1\\d\\d|[1-9]?\\d)){3}))|:))|(([0-9A-Fa-f]{1,4}:){2}(((:[0-9A-Fa-f]{1,4}){1,5})|((:[0-9A-Fa-f]{1,4}){0,3}:((25[0-5]|2[0-4]\\d|1\\d\\d|[1-9]?\\d)(\\.(25[0-5]|2[0-4]\\d|1\\d\\d|[1-9]?\\d)){3}))|:))|(([0-9A-Fa-f]{1,4}:){1}(((:[0-9A-Fa-f]{1,4}){1,6})|((:[0-9A-Fa-f]{1,4}){0,4}:((25[0-5]|2[0-4]\\d|1\\d\\d|[1-9]?\\d)(\\.(25[0-5]|2[0-4]\\d|1\\d\\d|[1-9]?\\d)){3}))|:))|(:(((:[0-9A-Fa-f]{1,4}){1,7})|((:[0-9A-Fa-f]{1,4}){0,5}:((25[0-5]|2[0-4]\\d|1\\d\\d|[1-9]?\\d)(\\.(25[0-5]|2[0-4]\\d|1\\d\\d|[1-9]?\\d)){3}))|:)))(%.+)?\\s*$";

    private static final Pattern PATTERN = Pattern.compile(IPV6_PATTERN);

    public IPv6Format() {
        super("ipv6", "must be a valid RFC 4291 IP address");
    }

    @Override
    public Set<ValidationMessage> validate(ExecutionContext executionContext, ValidationContext validationContext,
            JsonNode node, JsonNode rootNode, JsonNodePath instanceLocation, boolean assertionsEnabled,
            Supplier<Builder> message, FormatValidator formatValidator) {
        
        Set<ValidationMessage> errors = null;
        String value = node.textValue();
        if(!value.trim().equals(node.textValue())) {
            if (assertionsEnabled) {
                // leading and trailing spaces
                if (errors == null) {
                    errors = new LinkedHashSet<>();
                }
                errors.add(message.get().instanceNode(node).instanceLocation(instanceLocation)
                        .locale(executionContext.getExecutionConfig().getLocale())
                        .failFast(executionContext.isFailFast())
                        .arguments(this.getName(), this.getErrorMessageDescription()).build());
            }
        } else if(value.contains("%")) {
            if (assertionsEnabled) {
                // zone id is not part of the ipv6
                if (errors == null) {
                    errors = new LinkedHashSet<>();
                }
                errors.add(message.get().instanceNode(node).instanceLocation(instanceLocation)
                        .locale(executionContext.getExecutionConfig().getLocale())
                        .failFast(executionContext.isFailFast())
                        .arguments(this.getName(), this.getErrorMessageDescription()).build());
            }
        }
        
        if (!PATTERN.matcher(value).matches()) {
            if (errors == null) {
                errors = new LinkedHashSet<>();
            }
            errors.add(message.get().instanceNode(node).instanceLocation(instanceLocation)
                    .locale(executionContext.getExecutionConfig().getLocale())
                    .failFast(executionContext.isFailFast())
                    .arguments(this.getName(), this.getErrorMessageDescription()).build());
        }
        return errors == null ? Collections.emptySet() : errors;
    }
}
