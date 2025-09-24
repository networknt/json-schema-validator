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

    public static final List<Format> DEFAULT;
    
    static {
        List<Format> formats = new ArrayList<>();
        
        formats.add(pattern("hostname", "^([a-zA-Z0-9]|[a-zA-Z0-9][a-zA-Z0-9\\-]{0,61}[a-zA-Z0-9])(\\.([a-zA-Z0-9]|[a-zA-Z0-9][a-zA-Z0-9\\-]{0,61}[a-zA-Z0-9]))*$", "format.hostname"));
        formats.add(pattern("ipv4", "^(([0-9]|[1-9][0-9]|1[0-9][0-9]|2[0-4][0-9]|25[0-5])\\.){3}([0-9]|[1-9][0-9]|1[0-9][0-9]|2[0-4][0-9]|25[0-5])$", "format.ipv4"));
        formats.add(new IPv6Format());
        formats.add(pattern("json-pointer", "^(/([^/#~]|[~](?=[01]))*)*$", "format.json-pointer"));
        formats.add(pattern("relative-json-pointer", "^(0|([1-9]\\d*))(#|(/([^/#~]|[~](?=[01]))*)*)$", "format.relative-json-pointer"));
        formats.add(pattern("uri-template", "^([^\\p{Cntrl}\"'%<>\\^`\\{|\\}]|%\\p{XDigit}{2}|\\{[+#./;?&=,!@|]?((\\w|%\\p{XDigit}{2})(\\.?(\\w|%\\p{XDigit}{2}))*(:[1-9]\\d{0,3}|\\*)?)(,((\\w|%\\p{XDigit}{2})(\\.?(\\w|%\\p{XDigit}{2}))*(:[1-9]\\d{0,3}|\\*)?))*\\})*$", "format.uri-template"));
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
