/*
 * Copyright (c) 2016 Network New Technologies Inc.
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

package com.networknt.schema.internal;

import java.lang.ref.ReferenceQueue;
import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.Map;
import com.fasterxml.jackson.databind.JsonNode;

/**
 * Internal count lexemes, owned by schema-object identity, not numeric-node value.
 * Weak keys keep this read/compile-time sidecar from retaining schema documents.
 * Values retain only strings and weak numeric-node references, never their owners.
 */
public final class CountTokens {
    private static final ReferenceQueue<JsonNode> QUEUE = new ReferenceQueue<>();
    private static final Map<Identity, Map<String, Token>> TOKENS = new HashMap<>();

    private CountTokens() {
    }

    private static void clean() {
        Identity expired;
        while ((expired = (Identity) QUEUE.poll()) != null) {
            TOKENS.remove(expired);
        }
    }

    public static synchronized void put(JsonNode owner, String keyword, JsonNode number, String token) {
        clean();
        Identity lookup = new Identity(owner, null);
        Map<String, Token> fields = TOKENS.get(lookup);
        if (fields == null) {
            fields = new HashMap<>();
            TOKENS.put(new Identity(owner, QUEUE), fields);
        }
        fields.put(keyword, new Token(number, token));
    }

    public static synchronized String get(JsonNode owner, String keyword, JsonNode number) {
        if (owner == null || number == null || !number.isNumber()) {
            return null;
        }
        clean();
        Map<String, Token> fields = TOKENS.get(new Identity(owner, null));
        Token captured = fields == null ? null : fields.get(keyword);
        if (captured == null) {
            return null;
        }
        if (owner.get(keyword) != number || captured.number.get() != number
                || !captured.value.equals(number.numberValue().toString())) {
            fields.remove(keyword); // Replaced or mutated numbers invalidate their old provenance.
            return null;
        }
        return captured.text;
    }

    private static final class Token {
        private final WeakReference<JsonNode> number;
        private final String value;
        private final String text;

        Token(JsonNode number, String text) {
            this.number = new WeakReference<>(number);
            String snapshot = number.numberValue().toString();
            // Share the captured String when equal, avoiding a second retained String per entry.
            this.value = snapshot.equals(text) ? text : snapshot;
            this.text = text;
        }
    }

    private static final class Identity extends WeakReference<JsonNode> {
        private final int hash;

        Identity(JsonNode node, ReferenceQueue<JsonNode> queue) {
            super(node, queue);
            this.hash = System.identityHashCode(node);
        }

        @Override
        public int hashCode() {
            return this.hash;
        }

        @Override
        public boolean equals(Object other) {
            if (this == other) {
                return true;
            }
            JsonNode node = get();
            return node != null && other instanceof Identity && node == ((Identity) other).get();
        }
    }
}
