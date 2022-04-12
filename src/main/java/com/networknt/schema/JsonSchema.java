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

import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URLDecoder;
import java.util.*;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.networknt.schema.ValidationContext.DiscriminatorContext;
import com.networknt.schema.walk.DefaultKeywordWalkListenerRunner;
import com.networknt.schema.walk.JsonSchemaWalker;
import com.networknt.schema.walk.WalkListenerRunner;

/**
 * This is the core of json constraint implementation. It parses json constraint
 * file and generates JsonValidators. The class is thread safe, once it is
 * constructed, it can be used to validate multiple json data concurrently.
 */
public class JsonSchema extends BaseJsonValidator {
    private static final Pattern intPattern = Pattern.compile("^[0-9]+$");
    private Map<String, JsonValidator> validators;
    private final JsonMetaSchema metaSchema;
    private boolean validatorsLoaded = false;

    /**
     * This is the current uri of this schema. This uri could refer to the uri of this schema's file
     * or it could potentially be a uri that has been altered by an id. An 'id' is able to completely overwrite
     * the current uri or add onto it. This is necessary so that '$ref's are able to be relative to a
     * combination of the current schema file's uri and 'id' uris visible to this schema.
     * <p>
     * This can be null. If it is null, then the creation of relative uris will fail. However, an absolute
     * 'id' would still be able to specify an absolute uri.
     */
    private final URI currentUri;

    private JsonValidator requiredValidator = null;

    private JsonValidator unevaluatedPropertiesValidator = null;

    WalkListenerRunner keywordWalkListenerRunner = null;

    public JsonSchema(ValidationContext validationContext, URI baseUri, JsonNode schemaNode) {
        this(validationContext, "#", baseUri, schemaNode, null);
    }

    public JsonSchema(ValidationContext validationContext, String schemaPath, URI currentUri, JsonNode schemaNode,
                      JsonSchema parent) {
        this(validationContext, schemaPath, currentUri, schemaNode, parent, false);
    }

    public JsonSchema(ValidationContext validationContext, URI baseUri, JsonNode schemaNode, boolean suppressSubSchemaRetrieval) {
        this(validationContext, "#", baseUri, schemaNode, null, suppressSubSchemaRetrieval);
    }

    private JsonSchema(ValidationContext validationContext, String schemaPath, URI currentUri, JsonNode schemaNode,
                       JsonSchema parent, boolean suppressSubSchemaRetrieval) {
        super(schemaPath, schemaNode, parent, null, suppressSubSchemaRetrieval,
              validationContext.getConfig() != null && validationContext.getConfig().isFailFast(),
              validationContext.getConfig() != null ? validationContext.getConfig().getApplyDefaultsStrategy() : null);
        this.validationContext = validationContext;
        this.metaSchema = validationContext.getMetaSchema();
        this.currentUri = this.combineCurrentUriWithIds(currentUri, schemaNode);
        if (validationContext.getConfig() != null) {
            keywordWalkListenerRunner = new DefaultKeywordWalkListenerRunner(this.validationContext.getConfig().getKeywordWalkListenersMap());
            if (validationContext.getConfig().isOpenAPI3StyleDiscriminators()) {
                ObjectNode discriminator = (ObjectNode) schemaNode.get("discriminator");
                if (null != discriminator && null != validationContext.getCurrentDiscriminatorContext()) {
                    validationContext.getCurrentDiscriminatorContext().registerDiscriminator(schemaPath, discriminator);
                }
            }
        }
    }

    ValidationContext getValidationContext() {
        return this.validationContext;
    }

    private URI combineCurrentUriWithIds(URI currentUri, JsonNode schemaNode) {
        final String id = validationContext.resolveSchemaId(schemaNode);
        if (id == null) {
            return currentUri;
        } else if (isUriFragmentWithNoContext(currentUri, id)) {
            return null;
        } else {
            try {
                return this.validationContext.getURIFactory().create(currentUri, id);
            } catch (IllegalArgumentException e) {
                throw new JsonSchemaException(ValidationMessage.of(ValidatorTypeCode.ID.getValue(),
                        ValidatorTypeCode.ID,
                        id,
                        schemaPath,
                        currentUri == null ? "null" : currentUri.toString()));
            }
        }
    }

    private boolean isUriFragmentWithNoContext(URI currentUri, String id) {
        return id.startsWith("#") && currentUri == null;
    }

    public URI getCurrentUri() {
        return this.currentUri;
    }

    /**
     * Find the schema node for $ref attribute.
     *
     * @param ref String
     * @return JsonNode
     */
    public JsonNode getRefSchemaNode(String ref) {
        JsonSchema schema = findAncestor();
        JsonNode node = schema.getSchemaNode();

        if (ref.startsWith("#/")) {
            // handle local ref
            String[] keys = ref.substring(2).split("/");
            for (String key : keys) {
                try {
                    key = URLDecoder.decode(key, "utf-8");
                } catch (UnsupportedEncodingException e) {
                }
                Matcher matcher = intPattern.matcher(key);
                if (matcher.matches()) {
                    node = node.get(Integer.parseInt(key));
                } else {
                    node = node.get(key);
                }
                if (node == null) {
                    node = handleNullNode(ref, schema);
                }
                if (node == null) {
                    break;
                }
            }
        } else if (ref.startsWith("#") && ref.length() > 1) {
            node = metaSchema.getNodeByFragmentRef(ref, node);
            if (node == null) {
                node = handleNullNode(ref, schema);
            }
        }
        return node;
    }

    public JsonSchema findAncestor() {
        JsonSchema ancestor = this;
        if (this.getParentSchema() != null) {
            ancestor = this.getParentSchema().findAncestor();
        }
        return ancestor;
    }

    private JsonNode handleNullNode(String ref, JsonSchema schema) {
        JsonSchema subSchema = schema.fetchSubSchemaNode(validationContext);
        if (subSchema != null) {
            return subSchema.getRefSchemaNode(ref);
        }
        return null;
    }

    /**
     * Please note that the key in {@link #validators} map is a schema path. It is
     * used in {@link com.networknt.schema.walk.DefaultKeywordWalkListenerRunner} to derive the keyword.
     */
    private Map<String, JsonValidator> read(JsonNode schemaNode) {
        Map<String, JsonValidator> validators = new TreeMap<>(VALIDATOR_SORT);
        if (schemaNode.isBoolean()) {
            if (schemaNode.booleanValue()) {
                final String customMessage = getCustomMessage(schemaNode, "true");
                JsonValidator validator = validationContext.newValidator(getSchemaPath(), "true", schemaNode, this, customMessage);
                validators.put(getSchemaPath() + "/true", validator);
            } else {
                final String customMessage = getCustomMessage(schemaNode, "false");
                JsonValidator validator = validationContext.newValidator(getSchemaPath(), "false", schemaNode, this, customMessage);
                validators.put(getSchemaPath() + "/false", validator);
            }
        } else {
            Iterator<String> pnames = schemaNode.fieldNames();
            while (pnames.hasNext()) {
                String pname = pnames.next();
                JsonNode nodeToUse = pname.equals("if") ? schemaNode : schemaNode.get(pname);
                String customMessage = getCustomMessage(schemaNode, pname);
                JsonValidator validator = validationContext.newValidator(getSchemaPath(), pname, nodeToUse, this, customMessage);
                // Don't add UnevaluatedProperties Validator. This Keyword should exist only at the root level of the schema.
                // This validator should be called after we evaluate all other validators.
                if (ValidatorTypeCode.UNEVALUATED_PROPERTIES.getValue().equals(pname)) {
                    unevaluatedPropertiesValidator = validator;
                }
                if (validator != null && !ValidatorTypeCode.UNEVALUATED_PROPERTIES.getValue().equals(pname)) {
                    validators.put(getSchemaPath() + "/" + pname, validator);

                    if (pname.equals("required")) {
                        requiredValidator = validator;
                    }
                }

            }
        }
        return validators;
    }

    /**
     * A comparator that sorts validators, such that 'properties' comes before 'required',
     * so that we can apply default values before validating required.
     */
    private static Comparator<String> VALIDATOR_SORT = (lhs, rhs) -> {
        if (lhs.equals(rhs)) {
            return 0;
        }
        if (lhs.endsWith("/properties")) {
            return -1;
        }
        if (rhs.endsWith("/properties")) {
            return 1;
        }
        return lhs.compareTo(rhs);
    };

    private String getCustomMessage(JsonNode schemaNode, String pname) {
        final JsonSchema parentSchema = getParentSchema();
        final JsonNode message = getMessageNode(schemaNode, parentSchema);
        if (message != null && message.get(pname) != null) {
            return message.get(pname).asText();
        }
        return null;
    }

    private JsonNode getMessageNode(JsonNode schemaNode, JsonSchema parentSchema) {
        JsonNode nodeContainingMessage;
        if (parentSchema == null)
            nodeContainingMessage = schemaNode;
        else
            nodeContainingMessage = parentSchema.schemaNode;
        return nodeContainingMessage.get("message");
    }

    /************************ START OF VALIDATE METHODS **********************************/

    @Override
    public Set<ValidationMessage> validate(JsonNode node) {
        try {
            Set<ValidationMessage> errors = validate(node, node, AT_ROOT);
            return errors;
        } finally {
            if (validationContext.getConfig().isResetCollectorContext()) {
                CollectorContext.getInstance().reset();
            }
        }
    }

    public Set<ValidationMessage> validate(JsonNode jsonNode, JsonNode rootNode, String at) {
        SchemaValidatorsConfig config = validationContext.getConfig();
        Set<ValidationMessage> errors = new LinkedHashSet<ValidationMessage>();
        // Get the collector context.
        getCollectorContext();
        // Set the walkEnabled and isValidationEnabled flag in internal validator state.
        setValidatorState(false, true);
        for (JsonValidator v : getValidators().values()) {
            errors.addAll(v.validate(jsonNode, rootNode, at));
        }

        // Process UnEvaluatedProperties after all the validators are called if there are no errors.
        errors.addAll(processUnEvaluatedProperties(jsonNode, rootNode, at, true, true));

        if (null != config && config.isOpenAPI3StyleDiscriminators()) {
            ObjectNode discriminator = (ObjectNode) schemaNode.get("discriminator");
            if (null != discriminator) {
                final DiscriminatorContext discriminatorContext = validationContext.getCurrentDiscriminatorContext();
                if (null != discriminatorContext) {
                    final ObjectNode discriminatorToUse;
                    final ObjectNode discriminatorFromContext = discriminatorContext.getDiscriminatorForPath(schemaPath);
                    if (null == discriminatorFromContext) {
                        // register the current discriminator. This can only happen when the current context discriminator
                        // was not registered via allOf. In that case we have a $ref to the schema with discriminator that gets
                        // used for validation before allOf validation has kicked in
                        discriminatorContext.registerDiscriminator(schemaPath, discriminator);
                        discriminatorToUse = discriminator;
                    } else {
                        discriminatorToUse = discriminatorFromContext;
                    }

                    final String discriminatorPropertyName = discriminatorToUse.get("propertyName").asText();
                    final JsonNode discriminatorNode = jsonNode.get(discriminatorPropertyName);
                    final String discriminatorPropertyValue = discriminatorNode == null ? null : discriminatorNode.asText();
                    checkDiscriminatorMatch(discriminatorContext,
                            discriminatorToUse,
                            discriminatorPropertyValue,
                            this);
                }
            }
        }
        return errors;
    }

    public ValidationResult validateAndCollect(JsonNode node) {
        return validateAndCollect(node, node, AT_ROOT);
    }

    /**
     * This method both validates and collects the data in a CollectorContext.
     * Unlike others this methods cleans and removes everything from collector
     * context before returning.
     *
     * @param jsonNode JsonNode
     * @param rootNode JsonNode
     * @param at       String path
     * @return ValidationResult
     */
    private ValidationResult validateAndCollect(JsonNode jsonNode, JsonNode rootNode, String at) {
        try {
            // Get the config.
            SchemaValidatorsConfig config = validationContext.getConfig();
            // Get the collector context from the thread local.
            CollectorContext collectorContext = getCollectorContext();
            // Set the walkEnabled and isValidationEnabled flag in internal validator state.
            setValidatorState(false, true);
            // Validate.
            Set<ValidationMessage> errors = validate(jsonNode, rootNode, at);
            // When walk is called in series of nested call we don't want to load the collectors every time. Leave to the API to decide when to call collectors.
            if (config.doLoadCollectors()) {
                // Load all the data from collectors into the context.
                collectorContext.loadCollectors();
            }
            // Collect errors and collector context into validation result.
            ValidationResult validationResult = new ValidationResult(errors, collectorContext);
            return validationResult;
        } finally {
            if (validationContext.getConfig().isResetCollectorContext()) {
                CollectorContext.getInstance().reset();
            }
        }
    }

    /************************ END OF VALIDATE METHODS **********************************/

    /*********************** START OF WALK METHODS **********************************/

    /**
     * Walk the JSON node
     *
     * @param node                 JsonNode
     * @param shouldValidateSchema indicator on validation
     * @return result of ValidationResult
     */
    public ValidationResult walk(JsonNode node, boolean shouldValidateSchema) {
        try {
            // Get the config.
            SchemaValidatorsConfig config = validationContext.getConfig();
            // Get the collector context from the thread local.
            CollectorContext collectorContext = getCollectorContext();
            // Set the walkEnabled flag in internal validator state.
            setValidatorState(true, shouldValidateSchema);
            // Walk through the schema.
            Set<ValidationMessage> errors = walk(node, node, AT_ROOT, shouldValidateSchema);
            // When walk is called in series of nested call we don't want to load the collectors every time. Leave to the API to decide when to call collectors.
            if (config.doLoadCollectors()) {
                // Load all the data from collectors into the context.
                collectorContext.loadCollectors();
            }
            // Process UnEvaluatedProperties after all the validators are called.
            errors.addAll(processUnEvaluatedProperties(node, node, AT_ROOT, shouldValidateSchema, false));
            // Collect errors and collector context into validation result.
            ValidationResult validationResult = new ValidationResult(errors, collectorContext);
            return validationResult;
        } finally {
            if (validationContext.getConfig().isResetCollectorContext()) {
                CollectorContext.getInstance().reset();
            }
        }
    }

    @Override
    public Set<ValidationMessage> walk(JsonNode node, JsonNode rootNode, String at, boolean shouldValidateSchema) {
        Set<ValidationMessage> validationMessages = new LinkedHashSet<ValidationMessage>();
        // Walk through all the JSONWalker's.
        for (Entry<String, JsonValidator> entry : getValidators().entrySet()) {
            JsonSchemaWalker jsonWalker = entry.getValue();
            String schemaPathWithKeyword = entry.getKey();
            try {
                // Call all the pre-walk listeners. If at least one of the pre walk listeners
                // returns SKIP, then skip the walk.
                if (keywordWalkListenerRunner.runPreWalkListeners(schemaPathWithKeyword,
                        node,
                        rootNode,
                        at,
                        schemaPath,
                        schemaNode,
                        parentSchema,
                        validationContext,
                        validationContext.getJsonSchemaFactory())) {
                    validationMessages.addAll(jsonWalker.walk(node, rootNode, at, shouldValidateSchema));
                }
            } finally {
                // Call all the post-walk listeners.
                keywordWalkListenerRunner.runPostWalkListeners(schemaPathWithKeyword,
                        node,
                        rootNode,
                        at,
                        schemaPath,
                        schemaNode,
                        parentSchema,
                        validationContext,
                        validationContext.getJsonSchemaFactory(),
                        validationMessages);
            }
        }
        if (shouldValidateSchema) {
            // Process UnEvaluatedProperties after all the validators are called if there are no errors.
            validationMessages.addAll(processUnEvaluatedProperties(node, rootNode, at, true, true));
        }
        return validationMessages;
    }

    /************************ END OF WALK METHODS **********************************/

    private void setValidatorState(boolean isWalkEnabled, boolean shouldValidateSchema) {
        // Get the Validator state object storing validation data
        Object stateObj = CollectorContext.getInstance().get(ValidatorState.VALIDATOR_STATE_KEY);
        // if one has not been created, instantiate one
        if (stateObj == null) {
            ValidatorState state = new ValidatorState();
            state.setWalkEnabled(isWalkEnabled);
            state.setValidationEnabled(shouldValidateSchema);
            CollectorContext.getInstance().add(ValidatorState.VALIDATOR_STATE_KEY, state);
        }
    }

    public CollectorContext getCollectorContext() {
        SchemaValidatorsConfig config = validationContext.getConfig();
        CollectorContext collectorContext = (CollectorContext) ThreadInfo
                .get(CollectorContext.COLLECTOR_CONTEXT_THREAD_LOCAL_KEY);
        if (collectorContext == null) {
            if (config != null && config.getCollectorContext() != null) {
                collectorContext = config.getCollectorContext();
            } else {
                collectorContext = new CollectorContext();
            }
            // Set the collector context in thread info, this is unique for every thread.
            ThreadInfo.set(CollectorContext.COLLECTOR_CONTEXT_THREAD_LOCAL_KEY, collectorContext);
        }
        return collectorContext;
    }

    @Override
    public String toString() {
        return "\"" + getSchemaPath() + "\" : " + getSchemaNode().toString();
    }

    public boolean hasRequiredValidator() {
        return requiredValidator != null;
    }

    public JsonValidator getRequiredValidator() {
        return requiredValidator;
    }

    public Map<String, JsonValidator> getValidators() {
        if (validators == null) {
            validators = Collections.unmodifiableMap(this.read(getSchemaNode()));
        }
        return validators;
    }

    /**
     * Initializes the validators' {@link com.networknt.schema.JsonSchema} instances.
     * For avoiding issues with concurrency, in 1.0.49 the {@link com.networknt.schema.JsonSchema} instances affiliated with
     * validators were modified to no more preload the schema and lazy loading is used instead.
     * <p>This comes with the issue that this way you cannot rely on validating important schema features, in particular
     * <code>$ref</code> resolution at instantiation from {@link com.networknt.schema.JsonSchemaFactory}.</p>
     * <p>By calling <code>initializeValidators</code> you can enforce preloading of the {@link com.networknt.schema.JsonSchema}
     * instances of the validators.</p>
     */
    public void initializeValidators() {
        if (!validatorsLoaded) {
            validatorsLoaded = true;
            for (final JsonValidator validator : getValidators().values()) {
                validator.preloadJsonSchema();
            }
        }
    }

    private Set<ValidationMessage> processUnEvaluatedProperties(JsonNode jsonNode, JsonNode rootNode, String at, boolean shouldValidateSchema,
                                                                boolean fromValidate) {
        if (unevaluatedPropertiesValidator == null) {
            return Collections.emptySet();
        }
        if (!fromValidate) {
            Set<ValidationMessage> validationMessages = new HashSet<>();
            try {
                // Call all the pre walk listeners.
                if (keywordWalkListenerRunner.runPreWalkListeners(getSchemaPath() + "/" + ValidatorTypeCode.UNEVALUATED_PROPERTIES.getValue(),
                        jsonNode,
                        rootNode,
                        at,
                        schemaPath,
                        schemaNode,
                        parentSchema,
                        validationContext,
                        validationContext.getJsonSchemaFactory())) {
                    validationMessages = unevaluatedPropertiesValidator.walk(jsonNode, rootNode, at, shouldValidateSchema);
                }
            } finally {
                // Call all the post-walk listeners.
                keywordWalkListenerRunner.runPostWalkListeners(getSchemaPath() + "/" + ValidatorTypeCode.UNEVALUATED_PROPERTIES.getValue(),
                        jsonNode,
                        rootNode,
                        at,
                        schemaPath,
                        schemaNode,
                        parentSchema,
                        validationContext,
                        validationContext.getJsonSchemaFactory(),
                        validationMessages);
            }
            return validationMessages;
        } else {
            return unevaluatedPropertiesValidator.walk(jsonNode, rootNode, at, shouldValidateSchema);
        }
    }
}
