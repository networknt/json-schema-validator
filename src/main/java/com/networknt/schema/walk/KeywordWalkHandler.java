/*
 * Copyright (c) 2025 the original author or authors.
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

package com.networknt.schema.walk;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

import com.fasterxml.jackson.databind.JsonNode;
import com.networknt.schema.Error;
import com.networknt.schema.ExecutionContext;
import com.networknt.schema.Schema;
import com.networknt.schema.keyword.Keyword;
import com.networknt.schema.keyword.KeywordValidator;
import com.networknt.schema.path.NodePath;

/**
 * A {@link WalkHandler} for walking keywords.
 */
public class KeywordWalkHandler extends AbstractWalkHandler {
    private final List<WalkListener> allKeywordWalkListeners;
    private final Map<String, List<WalkListener>> keywordWalkListenersMap;

    public KeywordWalkHandler(List<WalkListener> allKeywordWalkListeners,
            Map<String, List<WalkListener>> keywordWalkListenersMap) {
        this.allKeywordWalkListeners = allKeywordWalkListeners;
        this.keywordWalkListenersMap = keywordWalkListenersMap;
    }

    @Override
    public boolean preWalk(ExecutionContext executionContext, String keyword, JsonNode instanceNode,
            JsonNode rootNode, NodePath instanceLocation, Schema schema, KeywordValidator validator) {
        boolean continueRunningListenersAndWalk = true;
        WalkEvent keywordWalkEvent = constructWalkEvent(executionContext, keyword, instanceNode, rootNode,
                instanceLocation, schema, validator);
        // Run Listeners that are setup only for this keyword.
        List<WalkListener> currentKeywordListeners = keywordWalkListenersMap.get(keyword);
        continueRunningListenersAndWalk = runPreWalkListeners(currentKeywordListeners, keywordWalkEvent);
        if (continueRunningListenersAndWalk) {
            // Run Listeners that are setup for all keywords.
            runPreWalkListeners(allKeywordWalkListeners, keywordWalkEvent);
        }
        return continueRunningListenersAndWalk;
    }

    @Override
    public void postWalk(ExecutionContext executionContext, String keyword, JsonNode instanceNode,
            JsonNode rootNode, NodePath instanceLocation, Schema schema, KeywordValidator validator,
            List<Error> errors) {
        WalkEvent keywordWalkEvent = constructWalkEvent(executionContext, keyword, instanceNode, rootNode,
                instanceLocation, schema, validator);
        // Run Listeners that are setup only for this keyword.
        List<WalkListener> currentKeywordListeners = keywordWalkListenersMap.get(keyword);
        runPostWalkListeners(currentKeywordListeners, keywordWalkEvent, errors);
        // Run Listeners that are setup for all keywords.
        runPostWalkListeners(allKeywordWalkListeners, keywordWalkEvent, errors);
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private Map<String, List<WalkListener>> keywordWalkListeners = new HashMap<>();
        private List<WalkListener> allKeywordWalkListeners = new ArrayList<>();

        public Builder keywordWalkListener(String keyword, WalkListener keywordWalkListener) {
            this.keywordWalkListeners.computeIfAbsent(keyword, key -> new ArrayList<>()).add(keywordWalkListener);
            return this;
        }

        public Builder keywordWalkListener(Keyword keyword, WalkListener keywordWalkListener) {
            return keywordWalkListener(keyword.getValue(), keywordWalkListener);
        }

        public Builder keywordWalkListener(WalkListener keywordWalkListener) {
            allKeywordWalkListeners.add(keywordWalkListener);
            return this;
        }

        public Builder keywordWalkListeners(Consumer<Map<String, List<WalkListener>>> keywordWalkListeners) {
            keywordWalkListeners.accept(this.keywordWalkListeners);
            return this;
        }

        public KeywordWalkHandler build() {
            return new KeywordWalkHandler(allKeywordWalkListeners, keywordWalkListeners);
        }
    }
}
