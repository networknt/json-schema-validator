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

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.networknt.schema.ValidationContext.DiscriminatorContext;
import com.networknt.schema.utils.StringUtils;
import com.networknt.schema.walk.DefaultKeywordWalkListenerRunner;
import com.networknt.schema.walk.JsonSchemaWalker;
import com.networknt.schema.walk.WalkListenerRunner;

import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLDecoder;
import java.text.MessageFormat;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
    private URI currentUri;
    private JsonValidator requiredValidator = null;
    private TypeValidator typeValidator;

    WalkListenerRunner keywordWalkListenerRunner = null;

    static JsonSchema from(ValidationContext validationContext, String schemaPath, URI currentUri, JsonNode schemaNode, JsonSchema parent, boolean suppressSubSchemaRetrieval) {
        return new JsonSchema(validationContext, schemaPath, currentUri, schemaNode, parent, suppressSubSchemaRetrieval);
    }

    /**
     * @param validationContext validation context
     * @param baseUri           base URL
     * @param schemaNode        schema node
     * @deprecated Use {@code JsonSchemaFactory#create(ValidationContext, String, JsonNode, JsonSchema)}
     */
    @Deprecated
    public JsonSchema(ValidationContext validationContext, URI baseUri, JsonNode schemaNode) {
        this(validationContext, "#", baseUri, schemaNode, null);
    }

    /**
     * @param validationContext validation context
     * @param schemaPath        schema path
     * @param currentUri        current URI
     * @param schemaNode        schema node
     * @param parent            parent schema
     * @deprecated Use {@code JsonSchemaFactory#create(ValidationContext, String, JsonNode, JsonSchema)}
     */
    @Deprecated
    public JsonSchema(ValidationContext validationContext, String schemaPath, URI currentUri, JsonNode schemaNode,
                      JsonSchema parent) {
        this(validationContext, schemaPath, currentUri, schemaNode, parent, false);
    }

    /**
     * @param validationContext validation context
     * @param baseUri           base URI
     * @param schemaNode        schema node
     * @param suppressSubSchemaRetrieval suppress sub schema retrieval
     * @deprecated Use {@code JsonSchemaFactory#create(ValidationContext, String, JsonNode, JsonSchema)}
     */
    @Deprecated
    public JsonSchema(ValidationContext validationContext, URI baseUri, JsonNode schemaNode, boolean suppressSubSchemaRetrieval) {
        this(validationContext, "#", baseUri, schemaNode, null, suppressSubSchemaRetrieval);
    }

    private JsonSchema(ValidationContext validationContext, String schemaPath, URI currentUri, JsonNode schemaNode,
                       JsonSchema parent, boolean suppressSubSchemaRetrieval) {
        super(schemaPath, schemaNode, parent, null, validationContext, suppressSubSchemaRetrieval);
        this.validationContext = validationContext;
        this.metaSchema = validationContext.getMetaSchema();
        this.currentUri = combineCurrentUriWithIds(currentUri, schemaNode);
        if (uriRefersToSubschema(currentUri, schemaPath)) {
            updateThisAsSubschema(currentUri);
        }
        if (validationContext.getConfig() != null) {
            this.keywordWalkListenerRunner = new DefaultKeywordWalkListenerRunner(this.validationContext.getConfig().getKeywordWalkListenersMap());
            if (validationContext.getConfig().isOpenAPI3StyleDiscriminators()) {
                ObjectNode discriminator = (ObjectNode) schemaNode.get("discriminator");
                if (null != discriminator && null != validationContext.getCurrentDiscriminatorContext()) {
                    validationContext.getCurrentDiscriminatorContext().registerDiscriminator(schemaPath, discriminator);
                }
            }
        }
    }

    public JsonSchema createChildSchema(String schemaPath, JsonNode schemaNode) {
        return getValidationContext().newSchema(schemaPath, schemaNode, this);
    }

    ValidationContext getValidationContext() {
        return this.validationContext;
    }

    private URI combineCurrentUriWithIds(URI currentUri, JsonNode schemaNode) {
        final String id = this.validationContext.resolveSchemaId(schemaNode);
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
                        new MessageFormat(validationContext.getConfig().getResourceBundle().getString(ValidatorTypeCode.ID.getErrorCodeValue())),
                        id,
                        this.schemaPath,
                        currentUri == null ? "null" : currentUri.toString()));
            }
        }
    }

    private static boolean isUriFragmentWithNoContext(URI currentUri, String id) {
        return id.startsWith("#") && currentUri == null;
    }

    private static boolean uriRefersToSubschema(URI originalUri, String schemaPath) {
        return originalUri != null
            && StringUtils.isNotBlank(originalUri.getRawFragment())  // Original currentUri parameter has a fragment, so it refers to a subschema
            && (StringUtils.isBlank(schemaPath) || "#".equals(schemaPath)); // We aren't already in a subschema
    }

    /**
     * Creates a new parent schema from the current state and updates this object to refer to the subschema instead.
     */
    private void updateThisAsSubschema(URI originalUri) {
        String fragment = "#" + originalUri.getFragment();
        JsonNode fragmentSchemaNode = getRefSchemaNode(fragment);
        if (fragmentSchemaNode == null) {
            throw new JsonSchemaException("Fragment " + fragment + " cannot be resolved");
        }
        // We need to strip the fragment off of the new parent schema's currentUri, so that its constructor
        // won't also end up in this method and get stuck in an infinite recursive loop.
        URI currentUriWithoutFragment;
        try {
            currentUriWithoutFragment = new URI(this.currentUri.getScheme(), this.currentUri.getSchemeSpecificPart(), null);
        } catch (URISyntaxException ex) {
            throw new JsonSchemaException("Unable to create URI without fragment from " + this.currentUri + ": " + ex.getMessage());
        }
        this.parentSchema = new JsonSchema(this.validationContext, this.schemaPath, currentUriWithoutFragment, this.schemaNode, this.parentSchema, super.suppressSubSchemaRetrieval); // TODO: Should this be delegated to the factory?
        this.schemaPath = fragment;
        this.schemaNode = fragmentSchemaNode;
        this.currentUri = combineCurrentUriWithIds(this.currentUri, fragmentSchemaNode);
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
                    // ignored
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
            node = this.metaSchema.getNodeByFragmentRef(ref, node);
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
        JsonSchema subSchema = schema.fetchSubSchemaNode(this.validationContext);
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
                JsonValidator validator = this.validationContext.newValidator(getSchemaPath(), "true", schemaNode, this, customMessage);
                validators.put(getSchemaPath() + "/true", validator);
            } else {
                final String customMessage = getCustomMessage(schemaNode, "false");
                JsonValidator validator = this.validationContext.newValidator(getSchemaPath(), "false", schemaNode, this, customMessage);
                validators.put(getSchemaPath() + "/false", validator);
            }
        } else {
            Iterator<String> pnames = schemaNode.fieldNames();
            while (pnames.hasNext()) {
                String pname = pnames.next();
                JsonNode nodeToUse = pname.equals("if") ? schemaNode : schemaNode.get(pname);
                String customMessage = getCustomMessage(schemaNode, pname);

                JsonValidator validator = this.validationContext.newValidator(getSchemaPath(), pname, nodeToUse, this, customMessage);
                if (validator != null) {
                    validators.put(getSchemaPath() + "/" + pname, validator);

                    if ("required".equals(pname)) {
                        this.requiredValidator = validator;
                    }

                    if ("type".equals(pname)) {
                        this.typeValidator = (TypeValidator) validator;
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
        if (lhs.equals(rhs)) return 0;
        if (lhs.endsWith("/properties")) return -1;
        if (rhs.endsWith("/properties")) return 1;
        if (lhs.endsWith("/patternProperties")) return -1;
        if (rhs.endsWith("/patternProperties")) return 1;
        if (lhs.endsWith("/unevaluatedItems")) return 1;
        if (rhs.endsWith("/unevaluatedItems")) return -1;
        if (lhs.endsWith("/unevaluatedProperties")) return 1;
        if (rhs.endsWith("/unevaluatedProperties")) return -1;

        return lhs.compareTo(rhs); // TODO: This smells. We are performing a lexicographical ordering of paths of unknown depth.
    };

    private String getCustomMessage(JsonNode schemaNode, String pname) {
        final JsonSchema parentSchema = getParentSchema();
        final JsonNode message = getMessageNode(schemaNode, parentSchema, pname);
        if (message != null && message.get(pname) != null) {
            return message.get(pname).asText();
        }
        return null;
    }

    private JsonNode getMessageNode(JsonNode schemaNode, JsonSchema parentSchema, String pname) {
        if (schemaNode.get("message") != null && schemaNode.get("message").get(pname) != null) {
            return schemaNode.get("message");
        }
        JsonNode messageNode;
        messageNode = schemaNode.get("message");
        if (messageNode == null && parentSchema != null) {
            messageNode = parentSchema.schemaNode.get("message");
            if (messageNode == null) {
                return getMessageNode(parentSchema.schemaNode, parentSchema.getParentSchema(), pname);
            }
        }
        return messageNode;
    }

    /************************ START OF VALIDATE METHODS **********************************/

    @Override
    public Set<ValidationMessage> validate(JsonNode node) {
        try {
            Set<ValidationMessage> errors = validate(node, node, atRoot());
            return errors;
        } finally {
            if (this.validationContext.getConfig().isResetCollectorContext()) {
                CollectorContext.getInstance().reset();
            }
        }
    }

    @Override
    public Set<ValidationMessage> validate(JsonNode jsonNode, JsonNode rootNode, String at) {
        SchemaValidatorsConfig config = this.validationContext.getConfig();
        Set<ValidationMessage> errors = new LinkedHashSet<>();
        // Get the collector context.
        getCollectorContext();
        // Set the walkEnabled and isValidationEnabled flag in internal validator state.
        setValidatorState(false, true);
        for (JsonValidator v : getValidators().values()) {
            errors.addAll(v.validate(jsonNode, rootNode, at));
        }

        if (null != config && config.isOpenAPI3StyleDiscriminators()) {
            ObjectNode discriminator = (ObjectNode) this.schemaNode.get("discriminator");
            if (null != discriminator) {
                final DiscriminatorContext discriminatorContext = this.validationContext.getCurrentDiscriminatorContext();
                if (null != discriminatorContext) {
                    final ObjectNode discriminatorToUse;
                    final ObjectNode discriminatorFromContext = discriminatorContext.getDiscriminatorForPath(this.schemaPath);
                    if (null == discriminatorFromContext) {
                        // register the current discriminator. This can only happen when the current context discriminator
                        // was not registered via allOf. In that case we have a $ref to the schema with discriminator that gets
                        // used for validation before allOf validation has kicked in
                        discriminatorContext.registerDiscriminator(this.schemaPath, discriminator);
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
        return validateAndCollect(node, node, atRoot());
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
            SchemaValidatorsConfig config = this.validationContext.getConfig();
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
            if (this.validationContext.getConfig().isResetCollectorContext()) {
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
        return walkAtNodeInternal(node, node, atRoot(), shouldValidateSchema);
    }

    public ValidationResult walkAtNode(JsonNode node, JsonNode rootNode, String at, boolean shouldValidateSchema) {
        return walkAtNodeInternal(node, rootNode, at, shouldValidateSchema);
    }

    private ValidationResult walkAtNodeInternal(JsonNode node, JsonNode rootNode, String at, boolean shouldValidateSchema) {
        try {
            // Get the config.
            SchemaValidatorsConfig config = this.validationContext.getConfig();
            // Get the collector context from the thread local.
            CollectorContext collectorContext = getCollectorContext();
            // Set the walkEnabled flag in internal validator state.
            setValidatorState(true, shouldValidateSchema);
            // Walk through the schema.
            Set<ValidationMessage> errors = walk(node, rootNode, at, shouldValidateSchema);
            // When walk is called in series of nested call we don't want to load the collectors every time. Leave to the API to decide when to call collectors.
            if (config.doLoadCollectors()) {
                // Load all the data from collectors into the context.
                collectorContext.loadCollectors();
            }

            ValidationResult validationResult = new ValidationResult(errors, collectorContext);
            return validationResult;
        } finally {
            if (this.validationContext.getConfig().isResetCollectorContext()) {
                CollectorContext.getInstance().reset();
            }
        }
    }

    @Override
    public Set<ValidationMessage> walk(JsonNode node, JsonNode rootNode, String at, boolean shouldValidateSchema) {
        Set<ValidationMessage> validationMessages = new LinkedHashSet<>();
        // Walk through all the JSONWalker's.
        getValidators().forEach((String schemaPathWithKeyword, JsonSchemaWalker jsonWalker) -> {
            try {
                // Call all the pre-walk listeners. If at least one of the pre walk listeners
                // returns SKIP, then skip the walk.
                if (this.keywordWalkListenerRunner.runPreWalkListeners(schemaPathWithKeyword,
                        node,
                        rootNode,
                        at,
                        this.schemaPath,
                        this.schemaNode,
                        this.parentSchema,
                        this.validationContext,
                        this.validationContext.getJsonSchemaFactory())) {
                    validationMessages.addAll(jsonWalker.walk(node, rootNode, at, shouldValidateSchema));
                }
            } finally {
                // Call all the post-walk listeners.
                this.keywordWalkListenerRunner.runPostWalkListeners(schemaPathWithKeyword,
                        node,
                        rootNode,
                        at,
                        this.schemaPath,
                        this.schemaNode,
                        this.parentSchema,
                        this.validationContext,
                        this.validationContext.getJsonSchemaFactory(),
                        validationMessages);
            }
        });

        return validationMessages;
    }

    /************************ END OF WALK METHODS **********************************/

    private static void setValidatorState(boolean isWalkEnabled, boolean shouldValidateSchema) {
        CollectorContext collectorContext = CollectorContext.getInstance();

        // Get the Validator state object storing validation data
        Object stateObj = collectorContext.get(ValidatorState.VALIDATOR_STATE_KEY);
        // if one has not been created, instantiate one
        if (stateObj == null) {
            ValidatorState state = new ValidatorState();
            state.setWalkEnabled(isWalkEnabled);
            state.setValidationEnabled(shouldValidateSchema);
            collectorContext.add(ValidatorState.VALIDATOR_STATE_KEY, state);
        }
    }

    public CollectorContext getCollectorContext() {
        SchemaValidatorsConfig config = this.validationContext.getConfig();
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
        return this.requiredValidator != null;
    }

    public JsonValidator getRequiredValidator() {
        return this.requiredValidator;
    }

    public boolean hasTypeValidator() {
        return this.typeValidator != null;
    }

    public TypeValidator getTypeValidator() {
        return this.typeValidator;
    }

    private Map<String, JsonValidator> getValidators() {
        if (this.validators == null) {
            this.validators = Collections.unmodifiableMap(read(getSchemaNode()));
        }
        return this.validators;
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
        if (!this.validatorsLoaded) {
            this.validatorsLoaded = true;
            for (final JsonValidator validator : getValidators().values()) {
                validator.preloadJsonSchema();
            }
        }
    }

}
