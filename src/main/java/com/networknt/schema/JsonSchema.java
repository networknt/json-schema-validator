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
import com.networknt.schema.walk.DefaultKeywordWalkListenerRunner;
import com.networknt.schema.walk.WalkListenerRunner;

import java.io.UnsupportedEncodingException;
import java.net.URI;
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

    private JsonValidator requiredValidator = null;
    private TypeValidator typeValidator;

    WalkListenerRunner keywordWalkListenerRunner = null;

    private final String id;
    private final String anchor;

    static JsonSchema from(ValidationContext validationContext, SchemaLocation schemaLocation, JsonNodePath evaluationPath, JsonNode schemaNode, JsonSchema parent, boolean suppressSubSchemaRetrieval) {
        return new JsonSchema(validationContext, schemaLocation, evaluationPath, schemaNode, parent, suppressSubSchemaRetrieval);
    }

    private JsonSchema(ValidationContext validationContext, SchemaLocation schemaLocation, JsonNodePath evaluationPath,
            JsonNode schemaNode, JsonSchema parent, boolean suppressSubSchemaRetrieval) {
        super(schemaLocation.resolve(validationContext.resolveSchemaId(schemaNode)), evaluationPath, schemaNode, parent,
                null, null, validationContext, suppressSubSchemaRetrieval);
        this.validationContext = validationContext;
        this.metaSchema = validationContext.getMetaSchema();
        initializeConfig();
        this.id = validationContext.resolveSchemaId(this.schemaNode);
        this.anchor = validationContext.getMetaSchema().readAnchor(this.schemaNode);
        if (this.id != null) {
            this.validationContext.getSchemaResources()
                    .putIfAbsent(this.schemaLocation != null ? this.schemaLocation.toString() : this.id, this);
        }
        if (this.anchor != null) {
            this.validationContext.getSchemaResources()
                    .putIfAbsent(this.schemaLocation.getAbsoluteIri().toString() + "#" + anchor, this);
        }
        getValidators();
    }
    
    private void initializeConfig() {
        if (validationContext.getConfig() != null) {
            this.keywordWalkListenerRunner = new DefaultKeywordWalkListenerRunner(
                    this.validationContext.getConfig().getKeywordWalkListenersMap());
        }
    }

    /**
     * Copy constructor.
     * 
     * @param copy to copy from
     */
    protected JsonSchema(JsonSchema copy) {
        super(copy);
        this.validators = copy.validators;
        this.metaSchema = copy.metaSchema;
        this.validatorsLoaded = copy.validatorsLoaded;
        this.dynamicAnchor = copy.dynamicAnchor;
        this.requiredValidator = copy.requiredValidator;
        this.typeValidator = copy.typeValidator;
        this.keywordWalkListenerRunner = copy.keywordWalkListenerRunner;
        this.id = copy.id;
        this.anchor = copy.anchor;
    }

    /**
     * Creates a schema using the current one as a template with the parent as the
     * ref.
     * <p>
     * This is typically used if this schema is a schema resource that can be
     * pointed to by various references.
     *
     * @param refEvaluationParentSchema the parent ref
     * @param refEvaluationPath the ref evaluation path
     * @return the schema
     */
    public JsonSchema fromRef(JsonSchema refEvaluationParentSchema, JsonNodePath refEvaluationPath) {
        JsonSchema copy = new JsonSchema(this);
        copy.validationContext = new ValidationContext(copy.getValidationContext().getMetaSchema(),
                copy.getValidationContext().getJsonSchemaFactory(),
                refEvaluationParentSchema.validationContext.getConfig(),
                copy.getValidationContext().getSchemaReferences(), copy.getValidationContext().getSchemaResources());
        copy.evaluationPath = refEvaluationPath;
        copy.evaluationParentSchema = refEvaluationParentSchema;
        // Validator state is reset due to the changes in evaluation path
        copy.validatorsLoaded = false;
        copy.requiredValidator = null;
        copy.typeValidator = null;
        copy.validators = null;
        copy.initializeConfig();
        return copy;
    }

    public JsonSchema withConfig(SchemaValidatorsConfig config) {
        if (!this.getValidationContext().getConfig().equals(config)) {
            JsonSchema copy = new JsonSchema(this);
            copy.validationContext = new ValidationContext(copy.getValidationContext().getMetaSchema(),
                    copy.getValidationContext().getJsonSchemaFactory(), config,
                    copy.getValidationContext().getSchemaReferences(),
                    copy.getValidationContext().getSchemaResources());
            copy.validatorsLoaded = false;
            copy.requiredValidator = null;
            copy.typeValidator = null;
            copy.validators = null;
            copy.initializeConfig();
            return copy;
        }
        return this;
    }

    ValidationContext getValidationContext() {
        return this.validationContext;
    }

    /**
     * Find the schema node for $ref attribute.
     *
     * @param ref String
     * @return JsonNode
     */
    public JsonNode getRefSchemaNode(String ref) {
        JsonSchema schema = findSchemaResourceRoot();
        JsonNode node = schema.getSchemaNode();

        String jsonPointer = ref;
        if (schema.getId() != null && ref.startsWith(schema.getId())) {
            String refValue = ref.substring(schema.getId().length());
            jsonPointer = refValue;
        }
        if (jsonPointer.startsWith("#/")) {
            jsonPointer = jsonPointer.substring(1);
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

    public JsonSchema findLexicalRoot() {
        JsonSchema ancestor = this;
        while (ancestor.getId() == null) {
            if (null == ancestor.getParentSchema()) break;
            ancestor = ancestor.getParentSchema();
        }
        return ancestor;
    }

    /**
     * Finds the root of the schema resource.
     * <p>
     * This is either the schema document root or the subschema resource root.
     *
     * @return the root of the schema
     */
    public JsonSchema findSchemaResourceRoot() {
        JsonSchema ancestor = this;
        while (!ancestor.isSchemaResourceRoot()) {
            ancestor = ancestor.getParentSchema();
        }
        return ancestor;
    }

    /**
     * Determines if this schema resource is a schema resource root.
     * <p>
     * This is either the schema document root or the subschema resource root.
     *
     * @return if this schema is a schema resource root
     */
    public boolean isSchemaResourceRoot() {
        if (getId() != null) {
            return true;
        }
        if (getParentSchema() == null) {
            return true;
        }
        // The schema should not cross
        if (!Objects.equals(getSchemaLocation().getAbsoluteIri(),
                getParentSchema().getSchemaLocation().getAbsoluteIri())) {
            return true;
        }
        return false;
    }

    public String getId() {
        return this.id;
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
        if (validationContext.getConfig().isOpenAPI3StyleDiscriminators()) {
            ObjectNode discriminator = (ObjectNode) schemaNode.get("discriminator");
            if (null != discriminator && null != executionContext.getCurrentDiscriminatorContext()) {
                executionContext.getCurrentDiscriminatorContext().registerDiscriminator(schemaLocation,
                        discriminator);
            }
        }

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
                final DiscriminatorContext discriminatorContext = executionContext
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
