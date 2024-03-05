/*
 * Copyright (c) 2024 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.networknt.schema.utils;

import java.io.UnsupportedEncodingException;
import java.net.IDN;
import java.net.URI;
import java.net.URLEncoder;

import com.networknt.schema.AbsoluteIri;

/**
 * Utility functions for AbsoluteIri.
 */
public class AbsoluteIris {
    /**
     * Converts an IRI to a URI.
     * 
     * @param iri the IRI to convert
     * @return the URI string
     */
    public static String toUri(AbsoluteIri iri) {
        String iriString = iri.toString();
        boolean ascii = isAscii(iriString);
        if (ascii) {
            int index = iriString.indexOf('?');
            if (index == -1) {
                return iriString;
            }
            String rest = iriString.substring(0, index + 1);
            String query = iriString.substring(index + 1);
            StringBuilder result = new StringBuilder(rest);
            handleQuery(result, query);
            return result.toString();
        }
        String[] parts = iriString.split(":"); // scheme + rest
        if (parts.length == 2) {
            StringBuilder result = new StringBuilder(parts[0]);
            result.append(":");

            String rest = parts[1];
            if (rest.startsWith("//")) {
                rest = rest.substring(2);
                result.append("//");
            } else if (rest.startsWith("/")) {
                rest = rest.substring(1);
                result.append("/");
            }
            String[] query = rest.split("\\?"); // rest ? query
            String[] restParts = query[0].split("/");
            for (int x = 0; x < restParts.length; x++) {
                String p = restParts[x];
                if (x == 0) {
                    // Domain
                    if (isAscii(p)) {
                        result.append(p);
                    } else {
                        result.append(unicodeToASCII(p));
                    }
                } else {
                    result.append(p);
                }
                if (x != restParts.length - 1) {
                    result.append("/");
                }
            }
            if (query[0].endsWith("/")) {
                result.append("/");
            }
            if (query.length == 2) {
                // handle query string
                result.append("?");
                handleQuery(result, query[1]);
            }

            return URI.create(result.toString()).toASCIIString();
        }
        return iriString;
    }

    /**
     * Determine if a string is US ASCII.
     * 
     * @param value to test
     * @return true if ASCII
     */
    static boolean isAscii(String value) {
        return value.codePoints().allMatch(ch -> ch < 0x7F);
    }

    /**
     * Ensures that the query parameters are properly URL encoded.
     * 
     * @param result the string builder to add to
     * @param query the query string
     */
    static void handleQuery(StringBuilder result, String query) {
        String[] queryParts = query.split("&");
        for (int y = 0; y < queryParts.length; y++) {
            String queryPart = queryParts[y];

            String[] nameValue = queryPart.split("=");
            try {
                result.append(URLEncoder.encode(nameValue[0], "UTF-8"));
                if (nameValue.length == 2) {
                    result.append("=");
                    result.append(URLEncoder.encode(nameValue[1], "UTF-8"));
                }
            } catch (UnsupportedEncodingException e) {
                throw new IllegalArgumentException(e);
            }
            if (y != queryParts.length - 1) {
                result.append("&");
            }
        }
    }

    // The following routines are from apache commons validator routines
    // DomainValidator
    static String unicodeToASCII(final String input) {
        try {
            final String ascii = IDN.toASCII(input);
            if (IDNBUGHOLDER.IDN_TOASCII_PRESERVES_TRAILING_DOTS) {
                return ascii;
            }
            final int length = input.length();
            if (length == 0) { // check there is a last character
                return input;
            }
            // RFC3490 3.1. 1)
            // Whenever dots are used as label separators, the following
            // characters MUST be recognized as dots: U+002E (full stop), U+3002
            // (ideographic full stop), U+FF0E (fullwidth full stop), U+FF61
            // (halfwidth ideographic full stop).
            final char lastChar = input.charAt(length - 1);// fetch original last char
            switch (lastChar) {
            case '\u002E': // "." full stop
            case '\u3002': // ideographic full stop
            case '\uFF0E': // fullwidth full stop
            case '\uFF61': // halfwidth ideographic full stop
                return ascii + "."; // restore the missing stop
            default:
                return ascii;
            }
        } catch (final IllegalArgumentException e) { // input is not valid
            return input;
        }
    }

    private static class IDNBUGHOLDER {
        private static final boolean IDN_TOASCII_PRESERVES_TRAILING_DOTS = keepsTrailingDot();

        private static boolean keepsTrailingDot() {
            final String input = "a."; // must be a valid name
            return input.equals(IDN.toASCII(input));
        }
    }

}
