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

import java.util.List;

import com.fasterxml.jackson.databind.JsonNode;
import com.networknt.schema.ApplyDefaultsStrategy;
import com.networknt.schema.Error;
import com.networknt.schema.ExecutionContext;
import com.networknt.schema.JsonNodePath;
import com.networknt.schema.Schema;
import com.networknt.schema.keyword.KeywordValidator;

/**
 * Configuration used when walking a schema.
 */
public class WalkConfig {
    private static class Holder {
        private static final WalkConfig INSTANCE = WalkConfig.builder().build();
    }

    public static WalkConfig getInstance() {
        return Holder.INSTANCE;
    }

    /**
     * {@link WalkListenerRunner} that performs no operations but indicates that it
     * should walk.
     */
    public static class NoOpWalkListenerRunner implements WalkListenerRunner {
        private static class Holder {
            private static final NoOpWalkListenerRunner INSTANCE = new NoOpWalkListenerRunner();
        }

        public static NoOpWalkListenerRunner getInstance() {
            return Holder.INSTANCE;
        }

        @Override
        public boolean runPreWalkListeners(ExecutionContext executionContext, String keyword, JsonNode instanceNode,
                JsonNode rootNode, JsonNodePath instanceLocation, Schema schema, KeywordValidator validator) {
            // Always walk
            return true;
        }

        @Override
        public void runPostWalkListeners(ExecutionContext executionContext, String keyword, JsonNode instanceNode,
                JsonNode rootNode, JsonNodePath instanceLocation, Schema schema, KeywordValidator validator,
                List<Error> errors) {
        }
    }

    /**
     * The strategy the walker uses to sets nodes that are missing or NullNode to
     * the default value, if any, and mutate the input json.
     */
    private final ApplyDefaultsStrategy applyDefaultsStrategy;

    private final WalkListenerRunner itemWalkListenerRunner;

    private final WalkListenerRunner keywordWalkListenerRunner;

    private final WalkListenerRunner propertyWalkListenerRunner;

    WalkConfig(ApplyDefaultsStrategy applyDefaultsStrategy, WalkListenerRunner itemWalkListenerRunner,
            WalkListenerRunner keywordWalkListenerRunner, WalkListenerRunner propertyWalkListenerRunner) {
        super();
        this.applyDefaultsStrategy = applyDefaultsStrategy;
        this.itemWalkListenerRunner = itemWalkListenerRunner;
        this.keywordWalkListenerRunner = keywordWalkListenerRunner;
        this.propertyWalkListenerRunner = propertyWalkListenerRunner;
    }

    /**
     * Gets the strategy for applying defaults.
     * 
     * @return the strategy for applying defaults
     */
    public ApplyDefaultsStrategy getApplyDefaultsStrategy() {
        return this.applyDefaultsStrategy;
    }

    /**
     * Gets the property walk listener runner.
     * 
     * @return the property walk listener runner
     */

    public WalkListenerRunner getPropertyWalkListenerRunner() {
        return this.propertyWalkListenerRunner;
    }

    /**
     * Gets the item walk listener runner.
     * 
     * @return the item walk listener runner
     */
    public WalkListenerRunner getItemWalkListenerRunner() {
        return this.itemWalkListenerRunner;
    }

    /**
     * Gets the keyword walk listener runner.
     * 
     * @return the keyword walk listener runner
     */
    public WalkListenerRunner getKeywordWalkListenerRunner() {
        return this.keywordWalkListenerRunner;
    }

    /**
     * Creates a new builder for {@link WalkConfig}.
     * 
     * @return the builder
     */
    public static Builder builder() {
        return new Builder();
    }

    /**
     * Creates a new builder for {@link WalkConfig} and copies the configuration.
     * 
     * @param config the configuration to copy
     * @return the builder
     */
    public static Builder builder(WalkConfig config) {
        Builder builder = new Builder();
        builder.applyDefaultsStrategy = config.applyDefaultsStrategy;
        builder.itemWalkListenerRunner = config.itemWalkListenerRunner;
        builder.keywordWalkListenerRunner = config.keywordWalkListenerRunner;
        builder.propertyWalkListenerRunner = config.propertyWalkListenerRunner;
        return builder;
    }

    /**
     * Builder for {@link WalkConfig}.
     */
    public static class Builder {
        private ApplyDefaultsStrategy applyDefaultsStrategy = null;
        private WalkListenerRunner itemWalkListenerRunner = null;
        private WalkListenerRunner keywordWalkListenerRunner = null;
        private WalkListenerRunner propertyWalkListenerRunner = null;

        /**
         * Sets the strategy the walker uses to sets nodes to the default value.
         * <p>
         * Defaults to {@link ApplyDefaultsStrategy#EMPTY_APPLY_DEFAULTS_STRATEGY}.
         *
         * @param applyDefaultsStrategy the strategy
         * @return the builder
         */
        public Builder applyDefaultsStrategy(ApplyDefaultsStrategy applyDefaultsStrategy) {
            this.applyDefaultsStrategy = applyDefaultsStrategy;
            return this;
        }

        public Builder itemWalkListenerRunner(WalkListenerRunner itemWalkListenerRunner) {
            this.itemWalkListenerRunner = itemWalkListenerRunner;
            return this;
        }

        public Builder keywordWalkListenerRunner(WalkListenerRunner keywordWalkListenerRunner) {
            this.keywordWalkListenerRunner = keywordWalkListenerRunner;
            return this;
        }

        public Builder propertyWalkListenerRunner(WalkListenerRunner propertyWalkListenerRunner) {
            this.propertyWalkListenerRunner = propertyWalkListenerRunner;
            return this;
        }

        public WalkConfig build() {
            return new WalkConfig(
                    applyDefaultsStrategy != null ? applyDefaultsStrategy
                            : ApplyDefaultsStrategy.EMPTY_APPLY_DEFAULTS_STRATEGY,
                    itemWalkListenerRunner != null ? itemWalkListenerRunner : NoOpWalkListenerRunner.getInstance(),
                    keywordWalkListenerRunner != null ? keywordWalkListenerRunner
                            : NoOpWalkListenerRunner.getInstance(),
                    propertyWalkListenerRunner != null ? propertyWalkListenerRunner
                            : NoOpWalkListenerRunner.getInstance());
        }
    }
}
