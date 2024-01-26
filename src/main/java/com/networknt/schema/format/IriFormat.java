package com.networknt.schema.format;

import java.net.URI;
import java.util.regex.Pattern;

import com.networknt.schema.JsonMetaSchema;

public class IriFormat extends AbstractRFC3986Format {
    private static final Pattern IPV6_PATTERN = Pattern.compile(JsonMetaSchema.IPV6_PATTERN);

    public IriFormat() {
        super("iri", "must be a valid RFC 3987 IRI");
    }

    @Override
    protected boolean validate(URI uri) {
        boolean result = uri.isAbsolute();
        if (result) {
            String authority = uri.getAuthority();
            if (authority != null) {
                if (IPV6_PATTERN.matcher(authority).matches() ) {
                    return false;
                }
            }
        }
        return result;
    }

}
