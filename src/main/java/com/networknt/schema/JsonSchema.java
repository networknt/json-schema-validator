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
import com.networknt.schema.CollectorContext.Scope;
import com.networknt.schema.SpecVersion.VersionFlag;
import com.networknt.schema.ValidationContext.DiscriminatorContext;
import com.networknt.schema.utils.StringUtils;
import com.networknt.schema.walk.DefaultKeywordWalkListenerRunner;
import com.networknt.schema.walk.WalkListenerRunner;

import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLDecoder;
import java.util.*;

/**
 * This is the core of json constraint implementation. It parses json constraint
 * file and generates JsonValidators. The class is thread safe, once it is
 * constructed, it can be used to validate multiple json data concurrently.
 */
public class JsonSchema extends BaseJsonValidator {
    private static final long V201909_VALUE = VersionFlag.V201909.getVersionFlagValue();

    /**
     * The validators sorted and indexed by evaluation path.
     */
    private List<JsonValidator> validators;
    private final JsonMetaSchema metaSchema;
    private boolean validatorsLoaded = false;
    private boolean dynamicAnchor = false;

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
    private boolean hasId = false;
    private JsonValidator requiredValidator = null;
    private TypeValidator typeValidator;

    WalkListenerRunner keywordWalkListenerRunner = null;

    static JsonSchema from(ValidationContext validationContext, SchemaLocation schemaLocation, JsonNodePath evaluationPath, URI currentUri, JsonNode schemaNode, JsonSchema parent, boolean suppressSubSchemaRetrieval) {
        return new JsonSchema(validationContext, schemaLocation, evaluationPath, currentUri, schemaNode, parent, suppressSubSchemaRetrieval);
    }

    private JsonSchema(ValidationContext validationContext, SchemaLocation schemaLocation, JsonNodePath evaluationPath, URI currentUri,
                       JsonNode schemaNode, JsonSchema parent, boolean suppressSubSchemaRetrieval) {
        super(schemaLocation, evaluationPath, schemaNode, parent, null, null, validationContext,
                suppressSubSchemaRetrieval);
        this.validationContext = validationContext;
        this.metaSchema = validationContext.getMetaSchema();
        this.currentUri = combineCurrentUriWithIds(currentUri, schemaNode);
        if (uriRefersToSubschema(currentUri, schemaLocation)) {
            updateThisAsSubschema(currentUri);
        }
        if (validationContext.getConfig() != null) {
            this.keywordWalkListenerRunner = new DefaultKeywordWalkListenerRunner(this.validationContext.getConfig().getKeywordWalkListenersMap());
            if (validationContext.getConfig().isOpenAPI3StyleDiscriminators()) {
                ObjectNode discriminator = (ObjectNode) schemaNode.get("discriminator");
                if (null != discriminator && null != validationContext.getCurrentDiscriminatorContext()) {
                    validationContext.getCurrentDiscriminatorContext().registerDiscriminator(schemaLocation, discriminator);
                }
            }
        }
    }

    public JsonSchema createChildSchema(SchemaLocation schemaLocation, JsonNode schemaNode) {
        return getValidationContext().newSchema(schemaLocation, evaluationPath, schemaNode, this);
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
                SchemaLocation path = schemaLocation.append(this.metaSchema.getIdKeyword());
                ValidationMessage validationMessage = ValidationMessage.builder().code(ValidatorTypeCode.ID.getValue())
                        .type(ValidatorTypeCode.ID.getValue()).instanceLocation(path.getFragment())
                        .evaluationPath(path.getFragment())
                        .arguments(currentUri == null ? "null" : currentUri.toString(), id)
                        .messageFormatter(args -> this.validationContext.getConfig().getMessageSource().getMessage(
                                ValidatorTypeCode.ID.getValue(), this.validationContext.getConfig().getLocale(), args))
                        .build();
                throw new JsonSchemaException(validationMessage);
            }
        }
    }

    private static boolean isUriFragmentWithNoContext(URI currentUri, String id) {
        return id.startsWith("#") && currentUri == null;
    }

    private static boolean uriRefersToSubschema(URI originalUri, SchemaLocation schemaLocation) {
        return originalUri != null
            && StringUtils.isNotBlank(originalUri.getRawFragment())  // Original currentUri parameter has a fragment, so it refers to a subschema
            && (schemaLocation.getFragment().getNameCount() == 0); // We aren't already in a subschema
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
        this.parentSchema = new JsonSchema(this.validationContext, SchemaLocation.of(currentUriWithoutFragment.toString()), this.evaluationPath, currentUriWithoutFragment, this.schemaNode, this.parentSchema, super.suppressSubSchemaRetrieval); // TODO: Should this be delegated to the factory?
        this.schemaLocation = SchemaLocation.of(originalUri.toString());
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

        String jsonPointer = ref;
        if (jsonPointer.startsWith("#/")) {
            jsonPointer = ref.substring(1);
        }

        if (jsonPointer.startsWith("/")) {
            try {
                jsonPointer = URLDecoder.decode(jsonPointer, "utf-8");
            } catch (UnsupportedEncodingException e) {
                // ignored
            }

            node = node.at(jsonPointer);
            if (node.isMissingNode()) {
                node = handleNullNode(ref, schema);
            }
        } else if ((ref.startsWith("#") && ref.length() > 1) || (ref.startsWith("urn:") && ref.length() > 4)) {
            node = this.metaSchema.getNodeByFragmentRef(ref, node);
            if (node == null) {
                node = handleNullNode(ref, schema);
            }
        }

        return node;
    }

    // This represents the lexical scope
    JsonSchema findLexicalRoot() {
        JsonSchema ancestor = this;
        while (!ancestor.hasId) {
            if (null == ancestor.getParentSchema()) break;
            ancestor = ancestor.getParentSchema();
        }
        return ancestor;
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
     * Please note that the key in {@link #validators} map is the evaluation path.
     */
    private List<JsonValidator> read(JsonNode schemaNode) {
        List<JsonValidator> validators = new ArrayList<>();
        if (schemaNode.isBoolean()) {
            if (schemaNode.booleanValue()) {
                JsonNodePath path = getEvaluationPath().append("true");
                JsonValidator validator = this.validationContext.newValidator(getSchemaLocation().append("true"), path,
                        "true", schemaNode, this);
                validators.add(validator);
            } else {
                JsonNodePath path = getEvaluationPath().append("false");
                JsonValidator validator = this.validationContext.newValidator(getSchemaLocation().append("false"),
                        path, "false", schemaNode, this);
                validators.add(validator);
            }
        } else {

            this.hasId = schemaNode.has(this.validationContext.getMetaSchema().getIdKeyword());

            JsonValidator refValidator = null;

            Iterator<String> pnames = schemaNode.fieldNames();
            while (pnames.hasNext()) {
                String pname = pnames.next();
                JsonNode nodeToUse = schemaNode.get(pname);

                JsonNodePath path = getEvaluationPath().append(pname);
                SchemaLocation schemaPath = getSchemaLocation().append(pname);

                if ("$recursiveAnchor".equals(pname)) {
                    if (!nodeToUse.isBoolean()) {
                        ValidationMessage validationMessage = ValidationMessage.builder().type("$recursiveAnchor")
                                .code("internal.invalidRecursiveAnchor")
                                .message(
                                        "{0}: The value of a $recursiveAnchor must be a Boolean literal but is {1}")
                                .instanceLocation(path)
                                .evaluationPath(path)
                                .schemaLocation(schemaPath)
                                .arguments(nodeToUse.getNodeType().toString())
                                .build();
                        throw new JsonSchemaException(validationMessage);
                    }
                    this.dynamicAnchor = nodeToUse.booleanValue();
                }

                JsonValidator validator = this.validationContext.newValidator(schemaPath, path,
                        pname, nodeToUse, this);
                if (validator != null) {
                    validators.add(validator);

                    if ("$ref".equals(pname)) {
                        refValidator = validator;
                    } else if ("required".equals(pname)) {
                        this.requiredValidator = validator;
                    } else if ("type".equals(pname)) {
                        this.typeValidator = (TypeValidator) validator;
                    }
                }

            }

            // Ignore siblings for older drafts
            if (null != refValidator && activeDialect() < V201909_VALUE) {
                validators.clear();
                validators.add(refValidator);
            }
        }
        if (validators.size() > 1) {
            Collections.sort(validators, VALIDATOR_SORT);
        }
        return validators;
    }

    private long activeDialect() {
        return this.validationContext
            .activeDialect()
            .map(VersionFlag::getVersionFlagValue)
            .orElse(Long.MAX_VALUE);
    }

    /**
     * A comparator that sorts validators, such that 'properties' comes before 'required',
     * so that we can apply default values before validating required.
     */
    private static Comparator<JsonValidator> VALIDATOR_SORT = (lhs, rhs) -> {
        String lhsName = lhs.getEvaluationPath().getName(-1);
        String rhsName = rhs.getEvaluationPath().getName(-1);

        if (lhsName.equals(rhsName)) return 0;

        if (lhsName.equals("properties")) return -1;
        if (rhsName.equals("properties")) return 1;
        if (lhsName.equals("patternProperties")) return -1;
        if (rhsName.equals("patternProperties")) return 1;
        if (lhsName.equals("unevaluatedItems")) return 1;
        if (rhsName.equals("unevaluatedItems")) return -1;
        if (lhsName.equals("unevaluatedProperties")) return 1;
        if (rhsName.equals("unevaluatedProperties")) return -1;

        return 0; // retain original schema definition order
    };

    /************************ START OF VALIDATE METHODS **********************************/

    @Override
    public Set<ValidationMessage> validate(ExecutionContext executionContext, JsonNode jsonNode, JsonNode rootNode, JsonNodePath instanceLocation) {
        SchemaValidatorsConfig config = this.validationContext.getConfig();
        Set<ValidationMessage> errors = null;
        // Get the collector context.
        CollectorContext collectorContext = executionContext.getCollectorContext();
        // Set the walkEnabled and isValidationEnabled flag in internal validator state.
        setValidatorState(executionContext, false, true);

        for (JsonValidator v : getValidators()) {
            Set<ValidationMessage> results = null;

            Scope parentScope = collectorContext.enterDynamicScope(this);
            try {
                results = v.validate(executionContext, jsonNode, rootNode, instanceLocation);
            } finally {
                Scope scope = collectorContext.exitDynamicScope();
                if (results == null || results.isEmpty()) {
                    parentScope.mergeWith(scope);
                } else {
                    if (errors == null) {
                        errors = new LinkedHashSet<>();
                    }
                    errors.addAll(results);
                    if (v instanceof PrefixItemsValidator || v instanceof ItemsValidator
                            || v instanceof ItemsValidator202012 || v instanceof ContainsValidator) {
                        collectorContext.getEvaluatedItems().addAll(scope.getEvaluatedItems());
                    }
                    if (v instanceof PropertiesValidator || v instanceof AdditionalPropertiesValidator
                            || v instanceof PatternPropertiesValidator) {
                        collectorContext.getEvaluatedProperties().addAll(scope.getEvaluatedProperties());
                    }
                }

            }
        }

        if (config.isOpenAPI3StyleDiscriminators()) {
            ObjectNode discriminator = (ObjectNode) this.schemaNode.get("discriminator");
            if (null != discriminator) {
                final DiscriminatorContext discriminatorContext = this.validationContext
                        .getCurrentDiscriminatorContext();
                if (null != discriminatorContext) {
                    final ObjectNode discriminatorToUse;
                    final ObjectNode discriminatorFromContext = discriminatorContext
                            .getDiscriminatorForPath(this.schemaLocation);
                    if (null == discriminatorFromContext) {
                        // register the current discriminator. This can only happen when the current context discriminator
                        // was not registered via allOf. In that case we have a $ref to the schema with discriminator that gets
                        // used for validation before allOf validation has kicked in
                        discriminatorContext.registerDiscriminator(this.schemaLocation, discriminator);
                        discriminatorToUse = discriminator;
                    } else {
                        discriminatorToUse = discriminatorFromContext;
                    }

                    final String discriminatorPropertyName = discriminatorToUse.get("propertyName").asText();
                    final JsonNode discriminatorNode = jsonNode.get(discriminatorPropertyName);
                    final String discriminatorPropertyValue = discriminatorNode == null ? null
                            : discriminatorNode.asText();
                    checkDiscriminatorMatch(discriminatorContext, discriminatorToUse, discriminatorPropertyValue,
                            this);
                }
            }
        }

        return errors == null ? Collections.emptySet() : errors;
    }

    /**
     * Validate the given root JsonNode, starting at the root of the data path.
     * @param rootNode JsonNode
     *
     * @return A list of ValidationMessage if there is any validation error, or an empty
     * list if there is no error.
     */
    public Set<ValidationMessage> validate(JsonNode rootNode) {
        return validate(rootNode, OutputFormat.DEFAULT);
    }

    /**
     * Validates the given root JsonNode, starting at the root of the data path. The
     * output will be formatted using the formatter specified.
     * 
     * @param <T>      the result type
     * @param rootNode the root note
     * @param format   the formatter
     * @return the result
     */
    public <T> T validate(JsonNode rootNode, OutputFormat<T> format) {
        return validate(rootNode, format, null);
    }

    /**
     * Validates the given root JsonNode, starting at the root of the data path. The
     * output will be formatted using the formatter specified.
     * 
     * @param <T>                 the result type
     * @param rootNode            the root note
     * @param format              the formatter
     * @param executionCustomizer the execution customizer
     * @return the result
     */
    public <T> T validate(JsonNode rootNode, OutputFormat<T> format, ExecutionCustomizer executionCustomizer) {
        return validate(createExecutionContext(), rootNode, format, executionCustomizer);
    }

    public ValidationResult validateAndCollect(ExecutionContext executionContext, JsonNode node) {
        return validateAndCollect(executionContext, node, node, atRoot());
    }

    /**
     * This method both validates and collects the data in a CollectorContext.
     * Unlike others this methods cleans and removes everything from collector
     * context before returning.
     * @param executionContext ExecutionContext
     * @param jsonNode JsonNode
     * @param rootNode JsonNode
     * @param instanceLocation JsonNodePath
     *
     * @return ValidationResult
     */
    private ValidationResult validateAndCollect(ExecutionContext executionContext, JsonNode jsonNode, JsonNode rootNode, JsonNodePath instanceLocation) {
        // Get the config.
        SchemaValidatorsConfig config = this.validationContext.getConfig();
        // Get the collector context from the thread local.
        CollectorContext collectorContext = executionContext.getCollectorContext();
        // Set the walkEnabled and isValidationEnabled flag in internal validator state.
        setValidatorState(executionContext, false, true);
        // Validate.
        Set<ValidationMessage> errors = validate(executionContext, jsonNode, rootNode, instanceLocation);
        // When walk is called in series of nested call we don't want to load the collectors every time. Leave to the API to decide when to call collectors.
        if (config.doLoadCollectors()) {
            // Load all the data from collectors into the context.
            collectorContext.loadCollectors();
        }
        // Collect errors and collector context into validation result.
        ValidationResult validationResult = new ValidationResult(errors, executionContext);
        return validationResult;
    }

    public ValidationResult validateAndCollect(JsonNode node) {
        return validateAndCollect(createExecutionContext(), node, node, atRoot());
    }

    /************************ END OF VALIDATE METHODS **********************************/

    /*********************** START OF WALK METHODS **********************************/

    /**
     * Walk the JSON node
     * @param executionContext     ExecutionContext
     * @param node                 JsonNode
     * @param shouldValidateSchema indicator on validation
     *
     * @return result of ValidationResult
     */
    public ValidationResult walk(ExecutionContext executionContext, JsonNode node, boolean shouldValidateSchema) {
        return walkAtNodeInternal(executionContext, node, node, atRoot(), shouldValidateSchema);
    }
    
    public ValidationResult walk(JsonNode node, boolean shouldValidateSchema) {
        return walk(createExecutionContext(), node, shouldValidateSchema);
    }

    public ValidationResult walkAtNode(ExecutionContext executionContext, JsonNode node, JsonNode rootNode,
            JsonNodePath instanceLocation, boolean shouldValidateSchema) {
        return walkAtNodeInternal(executionContext, node, rootNode, instanceLocation, shouldValidateSchema);
    }

    private ValidationResult walkAtNodeInternal(ExecutionContext executionContext, JsonNode node, JsonNode rootNode,
            JsonNodePath instanceLocation, boolean shouldValidateSchema) {
        // Get the config.
        SchemaValidatorsConfig config = this.validationContext.getConfig();
        // Get the collector context.
        CollectorContext collectorContext = executionContext.getCollectorContext();
        // Set the walkEnabled flag in internal validator state.
        setValidatorState(executionContext, true, shouldValidateSchema);
        // Walk through the schema.
        Set<ValidationMessage> errors = walk(executionContext, node, rootNode, instanceLocation, shouldValidateSchema);
        // When walk is called in series of nested call we don't want to load the collectors every time. Leave to the API to decide when to call collectors.
        if (config.doLoadCollectors()) {
            // Load all the data from collectors into the context.
            collectorContext.loadCollectors();
        }

        ValidationResult validationResult = new ValidationResult(errors, executionContext);
        return validationResult;
    }

    @Override
    public Set<ValidationMessage> walk(ExecutionContext executionContext, JsonNode node, JsonNode rootNode, JsonNodePath instanceLocation, boolean shouldValidateSchema) {
        Set<ValidationMessage> validationMessages = new LinkedHashSet<>();
        // Walk through all the JSONWalker's.
        getValidators().forEach(jsonWalker -> {
            JsonNodePath evaluationPathWithKeyword = jsonWalker.getEvaluationPath();
            try {
                // Call all the pre-walk listeners. If at least one of the pre walk listeners
                // returns SKIP, then skip the walk.
                if (this.keywordWalkListenerRunner.runPreWalkListeners(executionContext,
                        evaluationPathWithKeyword.getName(-1),
                        node,
                        rootNode,
                        instanceLocation,
                        jsonWalker.getEvaluationPath(),
                        jsonWalker.getSchemaLocation(),
                        this.schemaNode,
                        this.parentSchema, this.validationContext, this.validationContext.getJsonSchemaFactory())) {
                    validationMessages.addAll(jsonWalker.walk(executionContext, node, rootNode, instanceLocation, shouldValidateSchema));
                }
            } finally {
                // Call all the post-walk listeners.
                this.keywordWalkListenerRunner.runPostWalkListeners(executionContext,
                        evaluationPathWithKeyword.getName(-1),
                        node,
                        rootNode,
                        instanceLocation,
                        jsonWalker.getEvaluationPath(),
                        jsonWalker.getSchemaLocation(),
                        this.schemaNode,
                        this.parentSchema,
                        this.validationContext, this.validationContext.getJsonSchemaFactory(), validationMessages);
            }
        });

        return validationMessages;
    }

    /************************ END OF WALK METHODS **********************************/

    private static void setValidatorState(ExecutionContext executionContext, boolean isWalkEnabled,
            boolean shouldValidateSchema) {
        // Get the Validator state object storing validation data
        ValidatorState validatorState = executionContext.getValidatorState();
        if (validatorState == null) {
            // If one has not been created, instantiate one
            executionContext.setValidatorState(new ValidatorState(isWalkEnabled, shouldValidateSchema));
        }
    }

    @Override
    public String toString() {
        return "\"" + getEvaluationPath() + "\" : " + getSchemaNode().toString();
    }

    public boolean hasRequiredValidator() {
        return this.requiredValidator != null;
    }

    public JsonValidator getRequiredValidator() {
        return this.requiredValidator;
    }

    public boolean hasTypeValidator() {
        return getTypeValidator() != null;
    }

    public TypeValidator getTypeValidator() {
        // As the validators are lazy loaded the typeValidator is only known if the
        // validators are not null
        if (this.validators == null) {
            getValidators();
        }
        return this.typeValidator;
    }

    public List<JsonValidator> getValidators() {
        if (this.validators == null) {
            this.validators = Collections.unmodifiableList(read(getSchemaNode()));
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
            for (final JsonValidator validator : getValidators()) {
                validator.preloadJsonSchema();
            }
        }
    }

    public boolean isDynamicAnchor() {
        return this.dynamicAnchor;
    }

    /**
     * Creates an execution context.
     * 
     * @return the execution context
     */
    public ExecutionContext createExecutionContext() {
        SchemaValidatorsConfig config = validationContext.getConfig();
        if(config.getExecutionContextSupplier() != null) {
            return config.getExecutionContextSupplier().get();
        }
        CollectorContext collectorContext = new CollectorContext(config.isUnevaluatedItemsAnalysisDisabled(),
                config.isUnevaluatedPropertiesAnalysisDisabled());
        ExecutionConfig executionConfig = new ExecutionConfig();
        executionConfig.setLocale(config.getLocale());
        return new ExecutionContext(executionConfig, collectorContext);
    }
}
