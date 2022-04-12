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

package com.networknt.schema;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.fasterxml.jackson.databind.JsonNode;
import com.networknt.schema.walk.JsonSchemaWalkListener;

public class SchemaValidatorsConfig {
    /**
     * when validate type, if TYPE_LOOSE = true, will try to convert string to different types to match the type defined in
     * schema.
     */
    private boolean typeLoose;

    /**
     * When set to true, validator process is stop immediately when a very first validation error is discovered.
     */
    private boolean failFast;

    /**
     * When set to true, walker sets nodes that are missing or NullNode to the default value, if any, and mutate the input json.
     */
    private ApplyDefaultsStrategy applyDefaultsStrategy;

    /**
     * When set to true, use ECMA-262 compatible validator
     */
    private boolean ecma262Validator;

    /**
     * When set to true, use Java-specific semantics rather than native JavaScript semantics
     */
    private boolean javaSemantics;

    /**
     * When set to true, can interpret round doubles as integers
     */
    private boolean losslessNarrowing;

    /**
     * When set to true, support for discriminators is enabled for validations of oneOf, anyOf and allOf as described
     * on <a href="https://github.com/OAI/OpenAPI-Specification/blob/master/versions/3.0.3.md#discriminatorObject">GitHub</a>.
     */
    private boolean openAPI3StyleDiscriminators = false;

    /**
     * Map of public, normally internet accessible schema URLs to alternate locations; this allows for offline
     * validation of schemas that refer to public URLs. This is merged with any mappings the {@link JsonSchemaFactory}
     * may have been built with.
     */
    private Map<String, String> uriMappings = new HashMap<String, String>();

    /**
     * When a field is set as nullable in the OpenAPI specification, the schema validator validates that it is nullable
     * however continues with validation against the nullable field
     * <p>
     * If handleNullableField is set to true && incoming field is nullable && value is field: null --> succeed
     * If handleNullableField is set to false && incoming field is nullable && value is field: null --> it is up to the type
     * validator using the SchemaValidator to handle it.
     */
    private boolean handleNullableField = true;

    /**
     * When set to true resets the {@link CollectorContext} by calling {@link CollectorContext#reset()}.
     */
    private boolean resetCollectorContext = true;

    // This is just a constant for listening to all Keywords.
    public static final String ALL_KEYWORD_WALK_LISTENER_KEY = "com.networknt.AllKeywordWalkListener";

    private final Map<String, List<JsonSchemaWalkListener>> keywordWalkListenersMap = new HashMap<String,
            List<JsonSchemaWalkListener>>();

    private final List<JsonSchemaWalkListener> propertyWalkListeners = new ArrayList<JsonSchemaWalkListener>();

    private final List<JsonSchemaWalkListener> itemWalkListeners = new ArrayList<JsonSchemaWalkListener>();

    private CollectorContext collectorContext;

    private boolean loadCollectors = true;

    public boolean isTypeLoose() {
        return typeLoose;
    }

    public void setTypeLoose(boolean typeLoose) {
        this.typeLoose = typeLoose;
    }

    /**
     * When enabled, {@link JsonValidator#validate(JsonNode, JsonNode, String)}
     * or {@link JsonValidator#validate(JsonNode)} doesn't return any {@link Set}&lt;{@link ValidationMessage}&gt;,
     * instead a {@link JsonSchemaException} is thrown as soon as a validation errors is discovered.
     *
     * @param failFast boolean
     */
    public void setFailFast(final boolean failFast) {
        this.failFast = failFast;
    }

    public boolean isFailFast() {
        return this.failFast;
    }

    public void setApplyDefaultsStrategy(ApplyDefaultsStrategy applyDefaultsStrategy) {
        this.applyDefaultsStrategy = applyDefaultsStrategy;
    }

    public ApplyDefaultsStrategy getApplyDefaultsStrategy() {
        return applyDefaultsStrategy;
    }

    public Map<String, String> getUriMappings() {
        // return a copy of the mappings
        return new HashMap<String, String>(uriMappings);
    }

    public void setUriMappings(Map<String, String> uriMappings) {
        this.uriMappings = uriMappings;
    }

    public boolean isHandleNullableField() {
        return handleNullableField;
    }

    public void setHandleNullableField(boolean handleNullableField) {
        this.handleNullableField = handleNullableField;
    }

    public boolean isEcma262Validator() {
        return ecma262Validator;
    }

    public void setEcma262Validator(boolean ecma262Validator) {
        this.ecma262Validator = ecma262Validator;
    }

    public boolean isJavaSemantics() {
        return javaSemantics;
    }

    public void setJavaSemantics(boolean javaSemantics) {
        this.javaSemantics = javaSemantics;
    }

    public void addKeywordWalkListener(JsonSchemaWalkListener keywordWalkListener) {
        if (keywordWalkListenersMap.get(ALL_KEYWORD_WALK_LISTENER_KEY) == null) {
            List<JsonSchemaWalkListener> keywordWalkListeners = new ArrayList<JsonSchemaWalkListener>();
            keywordWalkListenersMap.put(ALL_KEYWORD_WALK_LISTENER_KEY, keywordWalkListeners);
        }
        keywordWalkListenersMap.get(ALL_KEYWORD_WALK_LISTENER_KEY).add(keywordWalkListener);
    }

    public void addKeywordWalkListener(String keyword, JsonSchemaWalkListener keywordWalkListener) {
        if (keywordWalkListenersMap.get(keyword) == null) {
            List<JsonSchemaWalkListener> keywordWalkListeners = new ArrayList<JsonSchemaWalkListener>();
            keywordWalkListenersMap.put(keyword, keywordWalkListeners);
        }
        keywordWalkListenersMap.get(keyword).add(keywordWalkListener);
    }

    public void addKeywordWalkListeners(List<JsonSchemaWalkListener> keywordWalkListeners) {
        if (keywordWalkListenersMap.get(ALL_KEYWORD_WALK_LISTENER_KEY) == null) {
            List<JsonSchemaWalkListener> ikeywordWalkListeners = new ArrayList<JsonSchemaWalkListener>();
            keywordWalkListenersMap.put(ALL_KEYWORD_WALK_LISTENER_KEY, ikeywordWalkListeners);
        }
        keywordWalkListenersMap.get(ALL_KEYWORD_WALK_LISTENER_KEY).addAll(keywordWalkListeners);
    }

    public void addKeywordWalkListeners(String keyword, List<JsonSchemaWalkListener> keywordWalkListeners) {
        if (keywordWalkListenersMap.get(keyword) == null) {
            List<JsonSchemaWalkListener> ikeywordWalkListeners = new ArrayList<JsonSchemaWalkListener>();
            keywordWalkListenersMap.put(keyword, ikeywordWalkListeners);
        }
        keywordWalkListenersMap.get(keyword).addAll(keywordWalkListeners);
    }

    public void addPropertyWalkListeners(List<JsonSchemaWalkListener> propertyWalkListeners) {
        this.propertyWalkListeners.addAll(propertyWalkListeners);
    }

    public void addPropertyWalkListener(JsonSchemaWalkListener propertyWalkListener) {
        this.propertyWalkListeners.add(propertyWalkListener);
    }

    public void addItemWalkListener(JsonSchemaWalkListener itemWalkListener) {
        this.itemWalkListeners.add(itemWalkListener);
    }

    public void addItemWalkListeners(List<JsonSchemaWalkListener> itemWalkListeners) {
        this.itemWalkListeners.addAll(itemWalkListeners);
    }

    public List<JsonSchemaWalkListener> getPropertyWalkListeners() {
        return this.propertyWalkListeners;
    }

    public Map<String, List<JsonSchemaWalkListener>> getKeywordWalkListenersMap() {
        return this.keywordWalkListenersMap;
    }

    public List<JsonSchemaWalkListener> getArrayItemWalkListeners() {
        return this.itemWalkListeners;
    }

    public SchemaValidatorsConfig() {
    }

    public CollectorContext getCollectorContext() {
        return collectorContext;
    }

    public void setCollectorContext(CollectorContext collectorContext) {
        this.collectorContext = collectorContext;
    }

    public boolean isLosslessNarrowing() {
        return losslessNarrowing;
    }

    public void setLosslessNarrowing(boolean losslessNarrowing) {
        this.losslessNarrowing = losslessNarrowing;
    }

    /**
     * Indicates whether OpenAPI 3 style discriminators should be supported
     * @return true in case discriminators are enabled
     * @since 1.0.51
     */
    public boolean isOpenAPI3StyleDiscriminators() {
        return openAPI3StyleDiscriminators;
    }

    /**
     * When enabled, the validation of <code>anyOf</code> and <code>allOf</code> in polymorphism will respect
     * OpenAPI 3 style discriminators as described in the
     * <a href="https://github.com/OAI/OpenAPI-Specification/blob/master/versions/3.0.3.md#discriminatorObject">OpenAPI 3.0.3 spec</a>.
     * The presence of a discriminator configuration on the schema will lead to the following changes in the behavior:
     * <ul>
     *     <li>for <code>oneOf</code> the spec is unfortunately very vague. Whether <code>oneOf</code> semantics should be
     *     affected by discriminators or not is not even 100% clear within the members of the OAS steering committee. Therefore
     *     <code>oneOf</code> at the moment ignores discriminators</li>
     *     <li>for <code>anyOf</code> the validation will choose one of the candidate schemas for validation based on the
     *     discriminator property value and will pass validation when this specific schema passes. This is in particular useful
     *     when the payload could match multiple candidates in the <code>anyOf</code> list and could lead to ambiguity. Example:
     *     type B has all mandatory properties of A and adds more mandatory ones. Whether the payload is an A or B is determined
     *     via the discriminator property name. A payload indicating it is an instance of B then requires passing the validation
     *     of B and passing the validation of A would not be sufficient anymore.</li>
     *     <li>for <code>allOf</code> use cases with discriminators defined on the copied-in parent type, it is possible to
     *     automatically validate against a subtype. Example: some schema specifies that there is a field of type A. A carries
     *     a discriminator field and B inherits from A. Then B is automatically a candidate for validation as well and will be
     *     chosen in case the discriminator property matches</li>
     * </ul>
     * @param openAPI3StyleDiscriminators whether or not discriminators should be used. Defaults to <code>false</code>
     * @since 1.0.51
     */
    public void setOpenAPI3StyleDiscriminators(boolean openAPI3StyleDiscriminators) {
        this.openAPI3StyleDiscriminators = openAPI3StyleDiscriminators;
    }

    public void setLoadCollectors(boolean loadCollectors) {
        this.loadCollectors = loadCollectors;
    }

    public boolean doLoadCollectors() {
        return loadCollectors;
    }

    public boolean isResetCollectorContext() {
        return resetCollectorContext;
    }

    public void setResetCollectorContext(boolean resetCollectorContext) {
        this.resetCollectorContext = resetCollectorContext;
    }
}
