package com.networknt.schema.format;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.regex.Pattern;

import com.networknt.schema.ExecutionContext;

/**
 * {@link Format} for RFC 3986 Uniform Resource Identifier (URI): Generic Syntax.
 */
public abstract class AbstractRFC3986Format implements Format {
    private static final Pattern VALID = Pattern.compile("([A-Za-z0-9+-\\.]*:)?//|[A-Za-z0-9+-\\.]+:");

    @Override
    public final boolean matches(ExecutionContext executionContext, String value) {
        try {
            URI uri = new URI(value);
            String authority = uri.getRawAuthority();
            if (authority != null && !isValidAuthority(authority)) {
                return false;
            }
            return validate(uri);
        } catch (URISyntaxException e) {
            return handleException(e);
        }
    }

    /**
     * Determines if the uri matches the format.
     *
     * @param uri the uri to match
     * @return true if matches
     */
    protected abstract boolean validate(URI uri);

    /**
     * Determines if the uri matches the format.
     *
     * @param e the URISyntaxException
     * @return false if it does not match
     */
    protected boolean handleException(URISyntaxException e) {
	    return VALID.matcher(e.getInput()).matches();
    }

    /**
     * Determines if the userinfo and the host may contain the non-ASCII characters RFC 3987 allows in an IRI
     * ({@code ucschar}).
     *
     * @return true for the IRI formats
     */
    protected boolean isInternationalized() {
        return false;
    }

    /**
     * Checks the authority against RFC 3986 section 3.2:
     * {@code authority = [ userinfo "@" ] host [ ":" port ]}.
     * <p>
     * {@link URI} falls back to a registry-based authority when it cannot parse the authority as a server, and then
     * checks neither the port, the userinfo nor the host. A registry-based authority such as {@code a_b.example} is
     * still a valid reg-name, so {@link URI#parseServerAuthority()} cannot be used instead.
     *
     * @param authority the raw authority
     * @return true if the authority is valid
     */
    private boolean isValidAuthority(String authority) {
        String hostAndPort = authority;
        int at = authority.indexOf('@');
        if (at != -1) {
            // userinfo = *( unreserved / pct-encoded / sub-delims / ":" ), so it cannot contain another "@"
            if (authority.indexOf('@', at + 1) != -1 || !isValid(authority.substring(0, at), true)) {
                return false;
            }
            hostAndPort = authority.substring(at + 1);
        }
        String port = null;
        if (hostAndPort.startsWith("[")) {
            // IP-literal = "[" ( IPv6address / IPvFuture ) "]"
            int end = hostAndPort.indexOf(']');
            if (end == -1 || !isValidIpLiteral(hostAndPort.substring(1, end))) {
                return false;
            }
            String rest = hostAndPort.substring(end + 1);
            if (!rest.isEmpty()) {
                if (rest.charAt(0) != ':') {
                    return false;
                }
                port = rest.substring(1);
            }
        } else {
            // A reg-name or an IPv4address cannot contain ":", so the first ":" starts the port; an IPv6 address
            // without brackets leaves a port that is not a number
            int colon = hostAndPort.indexOf(':');
            String host = colon == -1 ? hostAndPort : hostAndPort.substring(0, colon);
            if (!isValid(host, false)) {
                return false;
            }
            if (colon != -1) {
                port = hostAndPort.substring(colon + 1);
            }
        }
        // port = *DIGIT
        return port == null || port.chars().allMatch(ch -> ch >= '0' && ch <= '9');
    }

    /**
     * Checks a userinfo or a reg-name: {@code *( unreserved / pct-encoded / sub-delims )}, with {@code ":"} in a
     * userinfo, and {@code ucschar} in an IRI.
     */
    private boolean isValid(String value, boolean colonAllowed) {
        int i = 0;
        while (i < value.length()) {
            int cp = value.codePointAt(i);
            if (cp == '%') {
                if (i + 2 >= value.length() || !isHexDigit(value.charAt(i + 1)) || !isHexDigit(value.charAt(i + 2))) {
                    return false;
                }
                i += 3;
                continue;
            }
            if (!(isUnreserved(cp) || isSubDelim(cp) || (colonAllowed && cp == ':')
                    || (isInternationalized() && isUcschar(cp)))) {
                return false;
            }
            i += Character.charCount(cp);
        }
        return true;
    }

    private static boolean isValidIpLiteral(String literal) {
        // java.net.URI only parses an IPv6address between the brackets, so an IPvFuture never gets here.
        // RFC 3986 has no zone identifier, and its dec-octet has no leading zeros, as in the ipv6 format
        for (int i = 0; i < literal.length(); i++) {
            char ch = literal.charAt(i);
            if (ch == '%' || Character.isWhitespace(ch)) {
                return false;
            }
        }
        return IPv6Format.PATTERN.matcher(literal).matches();
    }

    private static boolean isUnreserved(int cp) {
        return (cp >= 'A' && cp <= 'Z') || (cp >= 'a' && cp <= 'z') || (cp >= '0' && cp <= '9') || cp == '-'
                || cp == '.' || cp == '_' || cp == '~';
    }

    private static boolean isSubDelim(int cp) {
        return "!$&'()*+,;=".indexOf(cp) != -1;
    }

    private static boolean isHexDigit(char ch) {
        return (ch >= '0' && ch <= '9') || (ch >= 'A' && ch <= 'F') || (ch >= 'a' && ch <= 'f');
    }

    /**
     * RFC 3987 {@code ucschar}.
     */
    private static boolean isUcschar(int cp) {
        if (cp < 0x10000) {
            return (cp >= 0xA0 && cp <= 0xD7FF) || (cp >= 0xF900 && cp <= 0xFDCF) || (cp >= 0xFDF0 && cp <= 0xFFEF);
        }
        // %x10000-1FFFD / %x20000-2FFFD / ... / %xD0000-DFFFD / %xE1000-EFFFD
        return cp <= 0xEFFFD && (cp & 0xFFFF) <= 0xFFFD && !(cp >= 0xE0000 && cp < 0xE1000);
    }
}
