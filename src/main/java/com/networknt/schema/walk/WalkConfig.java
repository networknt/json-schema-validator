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
import java.util.function.Consumer;

import com.fasterxml.jackson.databind.JsonNode;
import com.networknt.schema.Error;
import com.networknt.schema.ExecutionContext;
import com.networknt.schema.Schema;
import com.networknt.schema.keyword.KeywordValidator;
import com.networknt.schema.path.NodePath;

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
     * {@link WalkHandler} that performs no operations but indicates that it
     * should walk.
     */
    public static class NoOpWalkHandler implements WalkHandler {
        private static class Holder {
            private static final NoOpWalkHandler INSTANCE = new NoOpWalkHandler();
        }

        public static NoOpWalkHandler getInstance() {
            return Holder.INSTANCE;
        }

        @Override
        public boolean preWalk(ExecutionContext executionContext, String keyword, JsonNode instanceNode,
                JsonNode rootNode, NodePath instanceLocation, Schema schema, KeywordValidator validator) {
            // Always walk
            return true;
        }

        @Override
        public void postWalk(ExecutionContext executionContext, String keyword, JsonNode instanceNode,
                JsonNode rootNode, NodePath instanceLocation, Schema schema, KeywordValidator validator,
                List<Error> errors) {
        }
    }

    /**
     * The strategy the walker uses to sets nodes that are missing or NullNode to
     * the default value, if any, and mutate the input json.
     */
    private final ApplyDefaultsStrategy applyDefaultsStrategy;

    private final WalkHandler itemWalkHandler;

    private final WalkHandler keywordWalkHandler;

    private final WalkHandler propertyWalkHandler;

    WalkConfig(ApplyDefaultsStrategy applyDefaultsStrategy, WalkHandler itemWalkHandler,
            WalkHandler keywordWalkHandler, WalkHandler propertyWalkHandler) {
        super();
        this.applyDefaultsStrategy = applyDefaultsStrategy;
        this.itemWalkHandler = itemWalkHandler;
        this.keywordWalkHandler = keywordWalkHandler;
        this.propertyWalkHandler = propertyWalkHandler;
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
     * Gets the property walk handler.
     * 
     * @return the property walk handler
     */

    public WalkHandler getPropertyWalkHandler() {
        return this.propertyWalkHandler;
    }

    /**
     * Gets the item walk handler.
     * 
     * @return the item walk handler
     */
    public WalkHandler getItemWalkHandler() {
        return this.itemWalkHandler;
    }

    /**
     * Gets the keyword walk handler.
     * 
     * @return the keyword walk handler
     */
    public WalkHandler getKeywordWalkHandler() {
        return this.keywordWalkHandler;
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
        builder.itemWalkHandler = config.itemWalkHandler;
        builder.keywordWalkHandler = config.keywordWalkHandler;
        builder.propertyWalkHandler = config.propertyWalkHandler;
        return builder;
    }

    /**
     * Builder for {@link WalkConfig}.
     */
    public static class Builder {
        private ApplyDefaultsStrategy applyDefaultsStrategy = null;
        private WalkHandler itemWalkHandler = null;
        private WalkHandler keywordWalkHandler = null;
        private WalkHandler propertyWalkHandler = null;

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

        public Builder applyDefaultsStrategy(Consumer<ApplyDefaultsStrategy.Builder> customizer) {
            ApplyDefaultsStrategy.Builder builder = ApplyDefaultsStrategy.builder(applyDefaultsStrategy);
            customizer.accept(builder);
            return applyDefaultsStrategy(builder.build());
        }

        public Builder itemWalkHandler(WalkHandler itemWalkHandler) {
            this.itemWalkHandler = itemWalkHandler;
            return this;
        }

        public Builder keywordWalkHandler(WalkHandler keywordWalkHandler) {
            this.keywordWalkHandler = keywordWalkHandler;
            return this;
        }

        public Builder propertyWalkHandler(WalkHandler propertyWalkHandler) {
            this.propertyWalkHandler = propertyWalkHandler;
            return this;
        }

        public WalkConfig build() {
            return new WalkConfig(
                    applyDefaultsStrategy != null ? applyDefaultsStrategy
                            : ApplyDefaultsStrategy.EMPTY_APPLY_DEFAULTS_STRATEGY,
                    itemWalkHandler != null ? itemWalkHandler : NoOpWalkHandler.getInstance(),
                    keywordWalkHandler != null ? keywordWalkHandler
                            : NoOpWalkHandler.getInstance(),
                    propertyWalkHandler != null ? propertyWalkHandler
                            : NoOpWalkHandler.getInstance());
        }
    }
}
