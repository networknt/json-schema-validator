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
package com.networknt.schema.format;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Formats.
 */
public class Formats {
    private Formats() {
    }
    
    static PatternFormat pattern(String name, String regex, String messageKey) {
        return PatternFormat.of(name, regex, messageKey);
    }

    static PatternFormat pattern(String name, String regex) {
        return pattern(name, regex, null);
    }

    /**
     * A run of characters allowed in a uri-template literal by RFC 6570 section 2.1: the listed ASCII
     * characters, ucschar and iprivate. This excludes the space, {@code " < > \ ^ ` { | }}, control characters,
     * surrogates, noncharacters, the Specials block U+FFF0 to U+FFFD and the range U+E0000 to U+E0FFF (tag
     * characters and variation selectors). The apostrophe is also outside the RFC's set but stays valid, as in
     * the JSON-Schema-Test-Suite. {@code %} starts a pct-encoded triplet and is matched separately.
     * <p>
     * Each class is matched possessively, so a run of literal characters costs no stack, and long templates
     * cannot overflow it. The ASCII characters are listed one by one rather than as ranges, because
     * {@code java.util.regex} then compiles that class to a bit set, which is faster.
     */
    private static final String URI_TEMPLATE_LITERALS = "(?:"
            + "[!#$&'()*+,\\-./0123456789:;=?@ABCDEFGHIJKLMNOPQRSTUVWXYZ\\[\\]_abcdefghijklmnopqrstuvwxyz~]++"
            // ucschar and iprivate in the BMP
            + "|[\\x{A0}-\\x{D7FF}\\x{E000}-\\x{FDCF}\\x{FDF0}-\\x{FFEF}]++"
            // ucschar in planes 1 to 13, without each plane's last two code points (noncharacters), ucschar in
            // plane 14, and iprivate in planes 15 and 16
            + "|[\\x{10000}-\\x{1FFFD}\\x{20000}-\\x{2FFFD}\\x{30000}-\\x{3FFFD}\\x{40000}-\\x{4FFFD}"
            + "\\x{50000}-\\x{5FFFD}\\x{60000}-\\x{6FFFD}\\x{70000}-\\x{7FFFD}\\x{80000}-\\x{8FFFD}"
            + "\\x{90000}-\\x{9FFFD}\\x{A0000}-\\x{AFFFD}\\x{B0000}-\\x{BFFFD}\\x{C0000}-\\x{CFFFD}"
            + "\\x{D0000}-\\x{DFFFD}\\x{E1000}-\\x{EFFFD}\\x{F0000}-\\x{FFFFD}\\x{100000}-\\x{10FFFD}]++)";

    /**
     * A uri-template varspec: a varname, whose varchars may be separated by single dots, and an optional
     * prefix or explode modifier. Runs of varchars are matched possessively, as in
     * {@link #URI_TEMPLATE_LITERALS}.
     */
    private static final String URI_TEMPLATE_VARSPEC = "(?:\\w++|%\\p{XDigit}{2})++"
            + "(?:\\.(?:\\w++|%\\p{XDigit}{2})++)*+(?::[1-9]\\d{0,3}|\\*)?";

    public static final List<Format> DEFAULT;
    
    static {
        List<Format> formats = new ArrayList<>();
        
        formats.add(new HostnameFormat());
        formats.add(pattern("ipv4", "^(([0-9]|[1-9][0-9]|1[0-9][0-9]|2[0-4][0-9]|25[0-5])\\.){3}([0-9]|[1-9][0-9]|1[0-9][0-9]|2[0-4][0-9]|25[0-5])$", "format.ipv4"));
        formats.add(new IPv6Format());
        formats.add(pattern("json-pointer", "^(/([^/#~]|[~](?=[01]))*)*$", "format.json-pointer"));
        formats.add(pattern("relative-json-pointer", "^(0|([1-9]\\d*))(#|(/([^/#~]|[~](?=[01]))*)*)$", "format.relative-json-pointer"));
        formats.add(pattern("uri-template", "^(?:" + URI_TEMPLATE_LITERALS + "|%\\p{XDigit}{2}|\\{[+#./;?&=,!@|]?" + URI_TEMPLATE_VARSPEC + "(?:," + URI_TEMPLATE_VARSPEC + ")*+\\})*+$", "format.uri-template"));
        formats.add(pattern("uuid", "^\\p{XDigit}{8}-\\p{XDigit}{4}-\\p{XDigit}{4}-\\p{XDigit}{4}-\\p{XDigit}{12}$", "format.uuid"));
        formats.add(new DateFormat());
        formats.add(new DateTimeFormat());
        formats.add(new EmailFormat());
        formats.add(new IdnEmailFormat());
        formats.add(new IdnHostnameFormat());
        formats.add(new IriFormat());
        formats.add(new IriReferenceFormat());
        formats.add(new RegexFormat());
        formats.add(new TimeFormat());
        formats.add(new UriFormat());
        formats.add(new UriReferenceFormat());
        formats.add(new DurationFormat());
        
        // The following formats do not appear in any draft
        formats.add(pattern("alpha", "^[a-zA-Z]+$"));
        formats.add(pattern("alphanumeric", "^[a-zA-Z0-9]+$"));
        formats.add(pattern("color", "(#?([0-9A-Fa-f]{3,6})\\b)|(aqua)|(black)|(blue)|(fuchsia)|(gray)|(green)|(lime)|(maroon)|(navy)|(olive)|(orange)|(purple)|(red)|(silver)|(teal)|(white)|(yellow)|(rgb\\(\\s*\\b([0-9]|[1-9][0-9]|1[0-9][0-9]|2[0-4][0-9]|25[0-5])\\b\\s*,\\s*\\b([0-9]|[1-9][0-9]|1[0-9][0-9]|2[0-4][0-9]|25[0-5])\\b\\s*,\\s*\\b([0-9]|[1-9][0-9]|1[0-9][0-9]|2[0-4][0-9]|25[0-5])\\b\\s*\\))|(rgb\\(\\s*(\\d?\\d%|100%)+\\s*,\\s*(\\d?\\d%|100%)+\\s*,\\s*(\\d?\\d%|100%)+\\s*\\))"));
        formats.add(pattern("ip-address", "^(?:(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.){3}(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)$"));
        formats.add(pattern("phone", "^\\+(?:[0-9] ?){6,14}[0-9]$"));
        formats.add(pattern("style", "\\s*(.+?):\\s*([^;]+);?"));
        formats.add(pattern("utc-millisec", "^[0-9]+(\\.?[0-9]+)?$"));
        
        DEFAULT = Collections.unmodifiableList(formats);
    }

}
