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

import com.fasterxml.jackson.databind.JsonNode;
import com.networknt.schema.ExecutionContext;
import com.networknt.schema.Schema;
import com.networknt.schema.keyword.KeywordValidator;
import com.networknt.schema.path.NodePath;
import com.networknt.schema.Error;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/**
 * A {@link WalkHandler} for walking properties.
 */
public class PropertyWalkHandler extends AbstractWalkHandler {

    private final List<WalkListener> propertyWalkListeners;

    public PropertyWalkHandler(List<WalkListener> propertyWalkListeners) {
        this.propertyWalkListeners = propertyWalkListeners;
    }

    @Override
    public boolean preWalk(ExecutionContext executionContext, String keyword, JsonNode instanceNode,
            JsonNode rootNode, NodePath instanceLocation, Schema schema, KeywordValidator validator) {
        WalkEvent walkEvent = constructWalkEvent(executionContext, keyword, instanceNode, rootNode, instanceLocation,
                schema, validator);
        return runPreWalkListeners(propertyWalkListeners, walkEvent);
    }

    @Override
    public void postWalk(ExecutionContext executionContext, String keyword, JsonNode instanceNode,
            JsonNode rootNode, NodePath instanceLocation, Schema schema, KeywordValidator validator,
            List<Error> errors) {
        WalkEvent walkEvent = constructWalkEvent(executionContext, keyword, instanceNode, rootNode, instanceLocation,
                schema, validator);
        runPostWalkListeners(propertyWalkListeners, walkEvent, errors);
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private List<WalkListener> propertyWalkListeners = new ArrayList<>();

        public Builder propertyWalkListener(WalkListener propertyWalkListener) {
            this.propertyWalkListeners.add(propertyWalkListener);
            return this;
        }

        public Builder propertyWalkListeners(Consumer<List<WalkListener>> propertyWalkListeners) {
            propertyWalkListeners.accept(this.propertyWalkListeners);
            return this;
        }

        public PropertyWalkHandler build() {
            return new PropertyWalkHandler(propertyWalkListeners);
        }
    }
}
