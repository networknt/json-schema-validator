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
import com.networknt.schema.SpecVersion.VersionFlag;
import com.networknt.schema.i18n.MessageSource;
import com.networknt.schema.utils.JsonNodes;
import com.networknt.schema.utils.SetView;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Set;
import java.util.function.Consumer;

/**
 * Used for creating a schema with validators for validating inputs. This is
 * created using {@link JsonSchemaFactory#getInstance(VersionFlag, Consumer)}
 * and should be cached for performance.
 * <p>
 * This is the core of json constraint implementation. It parses json constraint
 * file and generates JsonValidators. The class is thread safe, once it is
 * constructed, it can be used to validate multiple json data concurrently.
 * <p>
 * JsonSchema instances are thread-safe provided its configuration is not
 * modified.
 */
public class JsonSchema extends BaseJsonValidator {
    private static final long V201909_VALUE = VersionFlag.V201909.getVersionFlagValue();
    private final String id;

    /**
     * The validators sorted and indexed by evaluation path.
     */
    private List<JsonValidator> validators = null;
    private boolean validatorsLoaded = false;
    private boolean recursiveAnchor = false;
    private TypeValidator typeValidator = null;

    static JsonSchema from(ValidationContext validationContext, SchemaLocation schemaLocation, JsonNodePath evaluationPath, JsonNode schemaNode, JsonSchema parent, boolean suppressSubSchemaRetrieval) {
        return new JsonSchema(validationContext, schemaLocation, evaluationPath, schemaNode, parent, suppressSubSchemaRetrieval);
    }

    private boolean hasNoFragment(SchemaLocation schemaLocation) {
        JsonNodePath fragment = this.schemaLocation.getFragment();
        return fragment == null || (fragment.getParent() == null && fragment.getNameCount() == 0);
    }

    private static SchemaLocation resolve(SchemaLocation schemaLocation, JsonNode schemaNode, boolean rootSchema,
            ValidationContext validationContext) {
        String id = validationContext.resolveSchemaId(schemaNode);
        if (id != null) {
            String resolve = id;
            int fragment = id.indexOf('#');
            // Check if there is a non-empty fragment
            if (fragment != -1 && !(fragment + 1 >= id.length())) {
                // strip the fragment when resolving
                resolve = id.substring(0, fragment);
            }
            SchemaLocation result = !"".equals(resolve) ? schemaLocation.resolve(resolve) : schemaLocation;
            JsonSchemaIdValidator validator = validationContext.getConfig().getSchemaIdValidator();
            if (validator != null) {
                if (!validator.validate(id, rootSchema, schemaLocation, result, validationContext)) {
                    SchemaLocation idSchemaLocation = schemaLocation.append(validationContext.getMetaSchema().getIdKeyword());
                    ValidationMessage validationMessage = ValidationMessage.builder()
                            .code(ValidatorTypeCode.ID.getValue()).type(ValidatorTypeCode.ID.getValue())
                            .instanceLocation(idSchemaLocation.getFragment())
                            .arguments(id, validationContext.getMetaSchema().getIdKeyword(), idSchemaLocation)
                            .schemaLocation(idSchemaLocation)
                            .schemaNode(schemaNode)
                            .messageFormatter(args -> validationContext.getConfig().getMessageSource().getMessage(
                                    ValidatorTypeCode.ID.getValue(), validationContext.getConfig().getLocale(), args))
                            .build();
                    throw new InvalidSchemaException(validationMessage);
                }
            }
            return result;
        } else {
            return schemaLocation;
        }
    }

    private JsonSchema(ValidationContext validationContext, SchemaLocation schemaLocation, JsonNodePath evaluationPath,
            JsonNode schemaNode, JsonSchema parent, boolean suppressSubSchemaRetrieval) {
        super(resolve(schemaLocation, schemaNode, parent == null, validationContext), evaluationPath, schemaNode, parent,
                null, null, validationContext, suppressSubSchemaRetrieval);
        String id = this.validationContext.resolveSchemaId(this.schemaNode);
        if (id != null) {
            // In earlier drafts $id may contain an anchor fragment see draft4/idRef.json
            // Note that json pointer fragments in $id are not allowed
            SchemaLocation result = id.indexOf('#') != -1 ? schemaLocation.resolve(id) : this.schemaLocation;
            if (hasNoFragment(result)) {
                this.id = id;
            } else {
                // This is an anchor fragment and is not a document
                // This will be added to schema resources later
                this.id = null;
            }
            this.validationContext.getSchemaResources().putIfAbsent(result != null ? result.toString() : id, this);
        } else {
            if (hasNoFragment(schemaLocation)) {
                // No $id but there is no fragment and is thus a schema resource
                this.id = schemaLocation.getAbsoluteIri() != null ? schemaLocation.getAbsoluteIri().toString() : "";
                this.validationContext.getSchemaResources()
                        .putIfAbsent(schemaLocation != null ? schemaLocation.toString() : this.id, this);
            } else {
                this.id = null;
            }
        }
        String anchor = this.validationContext.getMetaSchema().readAnchor(this.schemaNode);
        if (anchor != null) {
            String absoluteIri = this.schemaLocation.getAbsoluteIri() != null
                    ? this.schemaLocation.getAbsoluteIri().toString()
                    : "";
            this.validationContext.getSchemaResources()
                    .putIfAbsent(absoluteIri + "#" + anchor, this);
        }
        String dynamicAnchor = this.validationContext.getMetaSchema().readDynamicAnchor(schemaNode);
        if (dynamicAnchor != null) {
            String absoluteIri = this.schemaLocation.getAbsoluteIri() != null
                    ? this.schemaLocation.getAbsoluteIri().toString()
                    : "";
            this.validationContext.getDynamicAnchors()
                    .putIfAbsent(absoluteIri + "#" + dynamicAnchor, this);
        }
        getValidators();
    }

    /**
     * Constructor to create a copy using fields.
     * 
     * @param validators the validators
     * @param validatorsLoaded whether the validators are preloaded
     * @param recursiveAnchor whether this is has a recursive anchor
     * @param typeValidator the type validator
     * @param id the id
     * @param suppressSubSchemaRetrieval to suppress sub schema retrieval
     * @param schemaNode the schema node
     * @param validationContext the validation context
     * @param errorMessageType the error message type
     * @param errorMessageKeyword the error message keyword
     * @param messageSource the message source
     * @param keyword the keyword
     * @param parentSchema the parent schema
     * @param schemaLocation the schema location
     * @param evaluationPath the evaluation path
     * @param evaluationParentSchema the evaluation parent schema
     * @param errorMessage the error message
     */
    protected JsonSchema(
            /* Below from JsonSchema */
            List<JsonValidator> validators,
            boolean validatorsLoaded,
            boolean recursiveAnchor,
            TypeValidator typeValidator,
            String id,            
            /* Below from BaseJsonValidator */
            boolean suppressSubSchemaRetrieval,
            JsonNode schemaNode,
            ValidationContext validationContext,
            /* Below from ValidationMessageHandler */
            ErrorMessageType errorMessageType,
            String errorMessageKeyword,
            MessageSource messageSource,
            Keyword keyword,
            JsonSchema parentSchema,
            SchemaLocation schemaLocation,
            JsonNodePath evaluationPath,
            JsonSchema evaluationParentSchema,
            Map<String, String> errorMessage) {
        super(suppressSubSchemaRetrieval, schemaNode, validationContext, errorMessageType, errorMessageKeyword, messageSource, keyword,
                parentSchema, schemaLocation, evaluationPath, evaluationParentSchema, errorMessage);
        this.validators = validators;
        this.validatorsLoaded = validatorsLoaded;
        this.recursiveAnchor = recursiveAnchor;
        this.typeValidator = typeValidator;
        this.id = id;
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
        ValidationContext validationContext = new ValidationContext(this.getValidationContext().getMetaSchema(),
                this.getValidationContext().getJsonSchemaFactory(),
                refEvaluationParentSchema.validationContext.getConfig(),
                refEvaluationParentSchema.getValidationContext().getSchemaReferences(),
                refEvaluationParentSchema.getValidationContext().getSchemaResources(),
                refEvaluationParentSchema.getValidationContext().getDynamicAnchors());
        JsonNodePath evaluationPath = refEvaluationPath;
        JsonSchema evaluationParentSchema = refEvaluationParentSchema;
        // Validator state is reset due to the changes in evaluation path
        boolean validatorsLoaded = false;
        TypeValidator typeValidator = null;
        List<JsonValidator> validators = null;

        return new JsonSchema(
                /* Below from JsonSchema */
                validators,
                validatorsLoaded,
                recursiveAnchor,
                typeValidator,
                id,            
                /* Below from BaseJsonValidator */
                suppressSubSchemaRetrieval,
                schemaNode,
                validationContext,
                /* Below from ValidationMessageHandler */
                errorMessageType, errorMessageKeyword, messageSource,
                keyword, parentSchema, schemaLocation, evaluationPath,
                evaluationParentSchema, errorMessage);
    }

    public JsonSchema withConfig(SchemaValidatorsConfig config) {
        if (this.getValidationContext().getMetaSchema().getKeywords().containsKey("discriminator")
                && !config.isDiscriminatorKeywordEnabled()) {
            config = SchemaValidatorsConfig.builder(config)
                    .discriminatorKeywordEnabled(true)
                    .nullableKeywordEnabled(true)
                    .build();
        }
        if (!this.getValidationContext().getConfig().equals(config)) {
            ValidationContext validationContext = new ValidationContext(this.getValidationContext().getMetaSchema(),
                    this.getValidationContext().getJsonSchemaFactory(), config,
                    this.getValidationContext().getSchemaReferences(),
                    this.getValidationContext().getSchemaResources(),
                    this.getValidationContext().getDynamicAnchors());
            boolean validatorsLoaded = false;
            TypeValidator typeValidator = null;
            List<JsonValidator> validators = null;
            return new JsonSchema(
                    /* Below from JsonSchema */
                    validators,
                    validatorsLoaded,
                    recursiveAnchor,
                    typeValidator,
                    id,            
                    /* Below from BaseJsonValidator */
                    suppressSubSchemaRetrieval,
                    schemaNode,
                    validationContext,
                    /* Below from ValidationMessageHandler */
                    errorMessageType,
                    errorMessageKeyword,
                    messageSource,
                    keyword,
                    parentSchema,
                    schemaLocation,
                    evaluationPath,
                    evaluationParentSchema,
                    errorMessage);

        }
        return this;
    }

    public ValidationContext getValidationContext() {
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
        }
        return node;
    }

    public JsonSchema getRefSchema(JsonNodePath fragment) {
        if (PathType.JSON_POINTER.equals(fragment.getPathType())) {
            // Json Pointer
            return getSubSchema(fragment);
        } else {
            // Anchor
            String base = this.getSchemaLocation().getAbsoluteIri() != null ? this.schemaLocation.getAbsoluteIri().toString() : "";
            String anchor = base + "#" + fragment;
            JsonSchema result = this.validationContext.getSchemaResources().get(anchor);
            if (result == null) {
                result  = this.validationContext.getDynamicAnchors().get(anchor);
            }
            if (result == null) {
                throw new JsonSchemaException("Unable to find anchor "+anchor);
            }
            return result;
        }
    }

    /**
     * Gets the sub schema given the json pointer fragment.
     * 
     * @param fragment the json pointer fragment
     * @return the schema
     */
    public JsonSchema getSubSchema(JsonNodePath fragment) {
        JsonSchema document = findSchemaResourceRoot(); 
        JsonSchema parent = document; 
        JsonSchema subSchema = null;
        JsonNode parentNode = parent.getSchemaNode();
        SchemaLocation schemaLocation = document.getSchemaLocation();
        JsonNodePath evaluationPath = document.getEvaluationPath();
        int nameCount = fragment.getNameCount();
        for (int x = 0; x < nameCount; x++) {
            /*
             * The sub schema is created by iterating through the parents in order to
             * maintain the lexical parent schema context.
             * 
             * If this is created directly from the schema node pointed to by the json
             * pointer, the lexical context is lost and this will affect $ref resolution due
             * to $id changes in the lexical scope.
             */
            Object segment = fragment.getElement(x);
            JsonNode subSchemaNode = getNode(parentNode, segment);
            if (subSchemaNode != null) {
                if (segment instanceof Number && parentNode.isArray()) {
                    int index = ((Number) segment).intValue();
                    schemaLocation = schemaLocation.append(index);
                    evaluationPath = evaluationPath.append(index);
                } else {
                    schemaLocation = schemaLocation.append(segment.toString());
                    evaluationPath = evaluationPath.append(segment.toString());
                }
                /*
                 * The parent validation context is used to create as there can be changes in
                 * $schema is later drafts which means the validation context can change.
                 */
                // This may need a redesign see Issue 939 and 940
                String id = parent.getValidationContext().resolveSchemaId(subSchemaNode);
//                if (!("definitions".equals(segment.toString()) || "$defs".equals(segment.toString())
//                        )) {
                if (id != null || x == nameCount - 1) {
                    subSchema = parent.getValidationContext().newSchema(schemaLocation, evaluationPath, subSchemaNode,
                            parent);
                    parent = subSchema;
                    schemaLocation = subSchema.getSchemaLocation();
                }
                parentNode = subSchemaNode;
            } else {
                /*
                 * This means that the fragment wasn't found in the document.
                 * 
                 * In Draft 4-7 the $id indicates a base uri change and not a schema resource so this might not be the right document.
                 * 
                 * See test for draft4\extra\classpath\schema.json
                 */
                JsonSchema found = document.findSchemaResourceRoot().fetchSubSchemaNode(this.validationContext);
                if (found != null) {
                    found = found.getSubSchema(fragment);
                }
                if (found == null) {
                    ValidationMessage validationMessage = ValidationMessage.builder()
                            .type(ValidatorTypeCode.REF.getValue()).code("internal.unresolvedRef")
                            .message("{0}: Reference {1} cannot be resolved")
                            .instanceLocation(schemaLocation.getFragment())
                            .schemaLocation(schemaLocation)
                            .evaluationPath(evaluationPath)
                            .arguments(fragment).build();
                    throw new InvalidSchemaRefException(validationMessage);
                }
                return found;
            }
        }
        return subSchema;
    }

    protected JsonNode getNode(Object propertyOrIndex) {
        return getNode(this.schemaNode, propertyOrIndex);
    }

    protected JsonNode getNode(JsonNode node, Object propertyOrIndex) {
        return JsonNodes.get(node, propertyOrIndex);
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
	    return !Objects.equals(getSchemaLocation().getAbsoluteIri(),
			    getParentSchema().getSchemaLocation().getAbsoluteIri());
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
        List<JsonValidator> validators;
        if (schemaNode.isBoolean()) {
            validators = new ArrayList<>(1);
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

            Iterator<Entry<String, JsonNode>> iterator = schemaNode.fields();
            validators = new ArrayList<>(schemaNode.size());
            while (iterator.hasNext()) {
                Entry<String, JsonNode> entry = iterator.next();
                String pname = entry.getKey();
                JsonNode nodeToUse = entry.getValue();

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
                    this.recursiveAnchor = nodeToUse.booleanValue();
                }

                JsonValidator validator = this.validationContext.newValidator(schemaPath, path,
                        pname, nodeToUse, this);
                if (validator != null) {
                    validators.add(validator);

                    if ("$ref".equals(pname)) {
                        refValidator = validator;
                    } else if ("type".equals(pname)) {
                        if (validator instanceof TypeValidator) {
                            this.typeValidator = (TypeValidator) validator;
                        }
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
    private static final Comparator<JsonValidator> VALIDATOR_SORT = (lhs, rhs) -> {
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
        if (this.validationContext.getConfig().isDiscriminatorKeywordEnabled()) {
            ObjectNode discriminator = (ObjectNode) schemaNode.get("discriminator");
            if (null != discriminator && null != executionContext.getCurrentDiscriminatorContext()) {
                executionContext.getCurrentDiscriminatorContext().registerDiscriminator(schemaLocation,
                        discriminator);
            }
        }

        SetView<ValidationMessage> errors = null;
        for (JsonValidator v : getValidators()) {
            Set<ValidationMessage> results = null;

            try {
                results = v.validate(executionContext, jsonNode, rootNode, instanceLocation);
            } finally {
                if (results != null && !results.isEmpty()) {
                    if (errors == null) {
                        errors = new SetView<>();
                    }
                    errors.union(results);
                }
            }
        }

        if (this.validationContext.getConfig().isDiscriminatorKeywordEnabled()) {
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

        if (errors != null && !errors.isEmpty()) {
            // Failed with assertion set result and drop all annotations from this schema
            // and all subschemas
            executionContext.getResults().setResult(instanceLocation, getSchemaLocation(), getEvaluationPath(), false);
        }
        return errors == null ? Collections.emptySet() : errors;
    }

    /**
     * Validate the given root JsonNode, starting at the root of the data path.
     * <p>
     * Note that since Draft 2019-09 by default format generates only annotations
     * and not assertions.
     * <p>
     * Use {@link ExecutionConfig#setFormatAssertionsEnabled(Boolean)} to override
     * the default.
     * 
     * @param rootNode the root node
     * @return A list of ValidationMessage if there is any validation error, or an
     *         empty list if there is no error.
     */
    public Set<ValidationMessage> validate(JsonNode rootNode) {
        return validate(rootNode, OutputFormat.DEFAULT);
    }

    /**
     * Validate the given root JsonNode, starting at the root of the data path.
     * <p>
     * Note that since Draft 2019-09 by default format generates only annotations
     * and not assertions.
     * <p>
     * Use {@link ExecutionConfig#setFormatAssertionsEnabled(Boolean)} to override
     * the default.
     *
     * @param rootNode            the root node
     * @param executionCustomizer the execution customizer
     * @return the assertions
     */
    public Set<ValidationMessage> validate(JsonNode rootNode, ExecutionContextCustomizer executionCustomizer) {
        return validate(rootNode, OutputFormat.DEFAULT, executionCustomizer);
    }

    /**
     * Validate the given root JsonNode, starting at the root of the data path.
     * <p>
     * Note that since Draft 2019-09 by default format generates only annotations
     * and not assertions.
     * <p>
     * Use {@link ExecutionConfig#setFormatAssertionsEnabled(Boolean)} to override
     * the default.
     * 
     * @param rootNode            the root node
     * @param executionCustomizer the execution customizer
     * @return the assertions
     */
    public Set<ValidationMessage> validate(JsonNode rootNode, Consumer<ExecutionContext> executionCustomizer) {
        return validate(rootNode, OutputFormat.DEFAULT, executionCustomizer);
    }

    /**
     * Validates the given root JsonNode, starting at the root of the data path. The
     * output will be formatted using the formatter specified.
     * <p>
     * Note that since Draft 2019-09 by default format generates only annotations
     * and not assertions.
     * <p>
     * Use {@link ExecutionConfig#setFormatAssertionsEnabled(Boolean)} to override
     * the default.
     * 
     * @param <T>      the result type
     * @param rootNode the root node
     * @param format   the formatter
     * @return the result
     */
    public <T> T validate(JsonNode rootNode, OutputFormat<T> format) {
        return validate(rootNode, format, (ExecutionContextCustomizer) null);
    }

    /**
     * Validates the given root JsonNode, starting at the root of the data path. The
     * output will be formatted using the formatter specified.
     * <p>
     * Note that since Draft 2019-09 by default format generates only annotations
     * and not assertions.
     * <p>
     * Use {@link ExecutionConfig#setFormatAssertionsEnabled(Boolean)} to override
     * the default.
     * 
     * @param <T>                 the result type
     * @param rootNode            the root node
     * @param format              the formatter
     * @param executionCustomizer the execution customizer
     * @return the result
     */
    public <T> T validate(JsonNode rootNode, OutputFormat<T> format, ExecutionContextCustomizer executionCustomizer) {
        return validate(createExecutionContext(), rootNode, format, executionCustomizer);
    }

    /**
     * Validates the given root JsonNode, starting at the root of the data path. The
     * output will be formatted using the formatter specified.
     * <p>
     * Note that since Draft 2019-09 by default format generates only annotations
     * and not assertions.
     * <p>
     * Use {@link ExecutionConfig#setFormatAssertionsEnabled(Boolean)} to override
     * the default.
     * 
     * @param <T>                 the result type
     * @param rootNode            the root node
     * @param format              the formatter
     * @param executionCustomizer the execution customizer
     * @return the result
     */
    public <T> T validate(JsonNode rootNode, OutputFormat<T> format, Consumer<ExecutionContext> executionCustomizer) {
        return validate(createExecutionContext(), rootNode, format, (executionContext, validationContext) -> executionCustomizer.accept(executionContext));
    }

    /**
     * Validate the given input string using the input format, starting at the root
     * of the data path.
     * <p>
     * Note that since Draft 2019-09 by default format generates only annotations
     * and not assertions.
     * <p>
     * Use {@link ExecutionConfig#setFormatAssertionsEnabled(Boolean)} to override
     * the default.
     * 
     * @param input       the input
     * @param inputFormat the inputFormat
     * @return A list of ValidationMessage if there is any validation error, or an
     *         empty list if there is no error.
     */
    public Set<ValidationMessage> validate(String input, InputFormat inputFormat) {
        return validate(deserialize(input, inputFormat), OutputFormat.DEFAULT);
    }

    /**
     * Validate the given input string using the input format, starting at the root
     * of the data path.
     * <p>
     * Note that since Draft 2019-09 by default format generates only annotations
     * and not assertions.
     * <p>
     * Use {@link ExecutionConfig#setFormatAssertionsEnabled(Boolean)} to override
     * the default.
     *
     * @param input               the input
     * @param inputFormat         the inputFormat
     * @param executionCustomizer the execution customizer
     * @return the assertions
     */
    public Set<ValidationMessage> validate(String input, InputFormat inputFormat, ExecutionContextCustomizer executionCustomizer) {
        return validate(deserialize(input, inputFormat), OutputFormat.DEFAULT, executionCustomizer);
    }

    /**
     * Validate the given input string using the input format, starting at the root
     * of the data path.
     * <p>
     * Note that since Draft 2019-09 by default format generates only annotations
     * and not assertions.
     * <p>
     * Use {@link ExecutionConfig#setFormatAssertionsEnabled(Boolean)} to override
     * the default.
     * 
     * @param input               the input
     * @param inputFormat         the inputFormat
     * @param executionCustomizer the execution customizer
     * @return the assertions
     */
    public Set<ValidationMessage> validate(String input, InputFormat inputFormat, Consumer<ExecutionContext> executionCustomizer) {
        return validate(deserialize(input, inputFormat), OutputFormat.DEFAULT, executionCustomizer);
    }

    /**
     * Validates the given input string using the input format, starting at the root
     * of the data path. The output will be formatted using the formatter specified.
     * <p>
     * Note that since Draft 2019-09 by default format generates only annotations
     * and not assertions.
     * <p>
     * Use {@link ExecutionConfig#setFormatAssertionsEnabled(Boolean)} to override
     * the default.
     * 
     * @param <T>         the result type
     * @param input       the input
     * @param inputFormat the inputFormat
     * @param format      the formatter
     * @return the result
     */
    public <T> T validate(String input, InputFormat inputFormat, OutputFormat<T> format) {
        return validate(deserialize(input, inputFormat), format, (ExecutionContextCustomizer) null);
    }

    /**
     * Validates the given input string using the input format, starting at the root
     * of the data path. The output will be formatted using the formatter specified.
     * <p>
     * Note that since Draft 2019-09 by default format generates only annotations
     * and not assertions.
     * <p>
     * Use {@link ExecutionConfig#setFormatAssertionsEnabled(Boolean)} to override
     * the default.
     * 
     * @param <T>                 the result type
     * @param input               the input
     * @param inputFormat         the inputFormat
     * @param format              the formatter
     * @param executionCustomizer the execution customizer
     * @return the result
     */
    public <T> T validate(String input, InputFormat inputFormat, OutputFormat<T> format, ExecutionContextCustomizer executionCustomizer) {
        return validate(createExecutionContext(), deserialize(input, inputFormat), format, executionCustomizer);
    }

    /**
     * Validates the given input string using the input format, starting at the root
     * of the data path. The output will be formatted using the formatter specified.
     * <p>
     * Note that since Draft 2019-09 by default format generates only annotations
     * and not assertions.
     * <p>
     * Use {@link ExecutionConfig#setFormatAssertionsEnabled(Boolean)} to override
     * the default.
     * 
     * @param <T>                 the result type
     * @param input               the input
     * @param inputFormat         the inputFormat
     * @param format              the formatter
     * @param executionCustomizer the execution customizer
     * @return the result
     */
    public <T> T validate(String input, InputFormat inputFormat, OutputFormat<T> format, Consumer<ExecutionContext> executionCustomizer) {
        return validate(createExecutionContext(), deserialize(input, inputFormat), format, (executionContext, validationContext) -> executionCustomizer.accept(executionContext));
    }

    /**
     * Validates to a format.
     * 
     * @param <T>              the result type
     * @param executionContext the execution context
     * @param node             the node
     * @param format           the format
     * @return the result
     */
    public <T> T validate(ExecutionContext executionContext, JsonNode node, OutputFormat<T> format) {
        return validate(executionContext, node, format, null);
    }

    /**
     * Validates to a format.
     * 
     * @param <T>                 the result type
     * @param executionContext    the execution context
     * @param node                the node
     * @param format              the format
     * @param executionCustomizer the customizer
     * @return the result
     */
    public <T> T validate(ExecutionContext executionContext, JsonNode node, OutputFormat<T> format,
            ExecutionContextCustomizer executionCustomizer) {
        format.customize(executionContext, this.validationContext);
        if (executionCustomizer != null) {
            executionCustomizer.customize(executionContext, this.validationContext);
        }
        Set<ValidationMessage> validationMessages = null;
        try {
            validationMessages = validate(executionContext, node);
        } catch (FailFastAssertionException e) {
            validationMessages = e.getValidationMessages();
        }
        return format.format(this, validationMessages, executionContext, this.validationContext);
    }

    /**
     * Deserialize string to JsonNode.
     * 
     * @param input the input
     * @param inputFormat the format
     * @return the JsonNode.
     */
    private JsonNode deserialize(String input, InputFormat inputFormat) {
        try {
            return this.getValidationContext().getJsonSchemaFactory().readTree(input, inputFormat);
        } catch (IOException e) {
            throw new IllegalArgumentException("Invalid input", e);
        }
    }

    /**
     * Deprecated. Initialize the CollectorContext externally and call loadCollectors when done.
     * <p>
     * @param executionContext ExecutionContext
     * @param node JsonNode
     * @return ValidationResult
     */
    @Deprecated
    public ValidationResult validateAndCollect(ExecutionContext executionContext, JsonNode node) {
        return validateAndCollect(executionContext, node, node, atRoot());
    }

    /**
     * Deprecated. Initialize the CollectorContext externally and call loadCollectors when done.
     * <p>
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
    @Deprecated
    private ValidationResult validateAndCollect(ExecutionContext executionContext, JsonNode jsonNode, JsonNode rootNode, JsonNodePath instanceLocation) {
        // Validate.
        Set<ValidationMessage> errors = validate(executionContext, jsonNode, rootNode, instanceLocation);

        // Get the config.
        SchemaValidatorsConfig config = this.validationContext.getConfig();

        // When walk is called in series of nested call we don't want to load the collectors every time. Leave to the API to decide when to call collectors.
        if (config.doLoadCollectors()) {
            // Get the collector context.
            CollectorContext collectorContext = executionContext.getCollectorContext();

            // Load all the data from collectors into the context.
            collectorContext.loadCollectors();
        }
        // Collect errors and collector context into validation result.
        return new ValidationResult(errors, executionContext);
    }

    /**
     * Deprecated. Initialize the CollectorContext externally and call loadCollectors when done.
     *
     * @param node JsonNode
     * @return ValidationResult
     */
    @Deprecated
    public ValidationResult validateAndCollect(JsonNode node) {
        return validateAndCollect(createExecutionContext(), node, node, atRoot());
    }

    /************************ END OF VALIDATE METHODS **********************************/

    /*********************** START OF WALK METHODS **********************************/

    /**
     * Walk the JSON node.
     * 
     * @param executionContext the execution context
     * @param node             the input
     * @param validate         true to validate the input against the schema
     * @param executionCustomizer the customizer
     *
     * @return the validation result
     */
    public ValidationResult walk(ExecutionContext executionContext, JsonNode node, boolean validate,
            ExecutionContextCustomizer executionCustomizer) {
        return walkAtNodeInternal(executionContext, node, node, atRoot(), validate, OutputFormat.RESULT,
                executionCustomizer);
    }

    /**
     * Walk the JSON node.
     * 
     * @param <T>         the result type
     * @param executionContext the execution context
     * @param node             the input
     * @param outputFormat     the output format
     * @param validate         true to validate the input against the schema
     * @param executionCustomizer the customizer
     *
     * @return the validation result
     */
    public <T> T walk(ExecutionContext executionContext, JsonNode node, OutputFormat<T> outputFormat, boolean validate,
            ExecutionContextCustomizer executionCustomizer) {
        return walkAtNodeInternal(executionContext, node, node, atRoot(), validate, outputFormat, executionCustomizer);
    }

    /**
     * Walk the JSON node.
     * 
     * @param executionContext the execution context
     * @param node             the input
     * @param validate         true to validate the input against the schema
     * @param executionCustomizer the customizer
     *
     * @return the validation result
     */
    public ValidationResult walk(ExecutionContext executionContext, JsonNode node, boolean validate,
            Consumer<ExecutionContext> executionCustomizer) {
        return walkAtNodeInternal(executionContext, node, node, atRoot(), validate, OutputFormat.RESULT,
                executionCustomizer);
    }

    /**
     * Walk the JSON node.
     * 
     * @param <T>         the result type
     * @param executionContext the execution context
     * @param node             the input
     * @param outputFormat     the output format
     * @param validate         true to validate the input against the schema
     * @param executionCustomizer the customizer
     *
     * @return the validation result
     */
    public <T> T walk(ExecutionContext executionContext, JsonNode node, OutputFormat<T> outputFormat, boolean validate,
            Consumer<ExecutionContext> executionCustomizer) {
        return walkAtNodeInternal(executionContext, node, node, atRoot(), validate, outputFormat, executionCustomizer);
    }

    /**
     * Walk the JSON node.
     * 
     * @param executionContext the execution context
     * @param node             the input
     * @param validate         true to validate the input against the schema
     *
     * @return the validation result
     */
    public ValidationResult walk(ExecutionContext executionContext, JsonNode node, boolean validate) {
        return walkAtNodeInternal(executionContext, node, node, atRoot(), validate, OutputFormat.RESULT,
                (ExecutionContextCustomizer) null);
    }

    /**
     * Walk the input.
     * 
     * @param executionContext the execution context
     * @param input            the input
     * @param inputFormat      the input format
     * @param validate         true to validate the input against the schema
     * @return the validation result
     */
    public ValidationResult walk(ExecutionContext executionContext, String input, InputFormat inputFormat,
            boolean validate) {
        JsonNode node = deserialize(input, inputFormat);
        return walkAtNodeInternal(executionContext, node, node, atRoot(), validate, OutputFormat.RESULT,
                (ExecutionContextCustomizer) null);
    }

    /**
     * Walk the input.
     * 
     * @param <T>         the result type
     * @param executionContext the execution context
     * @param input            the input
     * @param inputFormat      the input format
     * @param outputFormat     the output format
     * @param validate         true to validate the input against the schema
     * @return the validation result
     */
    public <T> T walk(ExecutionContext executionContext, String input, InputFormat inputFormat,
            OutputFormat<T> outputFormat, boolean validate) {
        JsonNode node = deserialize(input, inputFormat);
        return walkAtNodeInternal(executionContext, node, node, atRoot(), validate, outputFormat,
                (ExecutionContextCustomizer) null);
    }

    /**
     * Walk the input.
     * 
     * @param executionContext the execution context
     * @param input            the input
     * @param inputFormat      the input format
     * @param validate         true to validate the input against the schema
     * @param executionCustomizer the customizer
     * @return the validation result
     */
    public ValidationResult walk(ExecutionContext executionContext, String input, InputFormat inputFormat,
            boolean validate, ExecutionContextCustomizer executionCustomizer) {
        JsonNode node = deserialize(input, inputFormat);
        return walkAtNodeInternal(executionContext, node, node, atRoot(), validate, OutputFormat.RESULT, executionCustomizer);
    }

    /**
     * Walk the input.
     * 
     * @param <T>         the result type
     * @param executionContext the execution context
     * @param input            the input
     * @param inputFormat      the input format
     * @param outputFormat     the output format
     * @param validate         true to validate the input against the schema
     * @param executionCustomizer the customizer
     * @return the validation result
     */
    public <T> T walk(ExecutionContext executionContext, String input, InputFormat inputFormat,
            OutputFormat<T> outputFormat, boolean validate, ExecutionContextCustomizer executionCustomizer) {
        JsonNode node = deserialize(input, inputFormat);
        return walkAtNodeInternal(executionContext, node, node, atRoot(), validate, outputFormat, executionCustomizer);
    }

    /**
     * Walk the JSON node.
     * 
     * @param node     the input
     * @param validate true to validate the input against the schema
     * @return the validation result
     */
    public ValidationResult walk(JsonNode node, boolean validate) {
        return walk(createExecutionContext(), node, validate);
    }

    /**
     * Walk the JSON node.
     * 
     * @param <T>         the result type
     * @param node     the input
     * @param validate true to validate the input against the schema
     * @param outputFormat the output format
     * @return the validation result
     */
    public <T> T walk(JsonNode node, OutputFormat<T> outputFormat, boolean validate) {
        return walk(createExecutionContext(), node, outputFormat, validate, (ExecutionContextCustomizer) null);
    }

    /**
     * Walk the input.
     * 
     * @param input       the input
     * @param inputFormat the input format
     * @param validate    true to validate the input against the schema
     * @return the validation result
     */
    public ValidationResult walk(String input, InputFormat inputFormat, boolean validate) {
        return walk(createExecutionContext(), deserialize(input, inputFormat), validate);
    }

    /**
     * Walk the input.
     * 
     * @param input       the input
     * @param inputFormat the input format
     * @param validate    true to validate the input against the schema
     * @param executionCustomizer the customizer
     * @return the validation result
     */
    public ValidationResult walk(String input, InputFormat inputFormat, boolean validate,
            ExecutionContextCustomizer executionCustomizer) {
        return walk(createExecutionContext(), deserialize(input, inputFormat), validate, executionCustomizer);
    }

    /**
     * Walk the input.
     * 
     * @param input       the input
     * @param inputFormat the input format
     * @param validate    true to validate the input against the schema
     * @param executionCustomizer the customizer
     * @return the validation result
     */
    public ValidationResult walk(String input, InputFormat inputFormat, boolean validate,
            Consumer<ExecutionContext> executionCustomizer) {
        return walk(createExecutionContext(), deserialize(input, inputFormat), validate, executionCustomizer);
    }

    /**
     * Walk at the node.
     * 
     * @param executionContext the execution content
     * @param node             the current node
     * @param rootNode         the root node
     * @param instanceLocation the instance location
     * @param validate         true to validate the input against the schema
     * @return the validation result
     */
    public ValidationResult walkAtNode(ExecutionContext executionContext, JsonNode node, JsonNode rootNode,
            JsonNodePath instanceLocation, boolean validate) {
        return walkAtNodeInternal(executionContext, node, rootNode, instanceLocation, validate, OutputFormat.RESULT,
                (ExecutionContextCustomizer) null);
    }

    private <T> T walkAtNodeInternal(ExecutionContext executionContext, JsonNode node, JsonNode rootNode,
            JsonNodePath instanceLocation, boolean validate, OutputFormat<T> format, Consumer<ExecutionContext> executionCustomizer) {
        return walkAtNodeInternal(executionContext, node, rootNode, instanceLocation, validate, format,
                (executeContext, validationContext) -> executionCustomizer.accept(executeContext));
    }

    private <T> T walkAtNodeInternal(ExecutionContext executionContext, JsonNode node, JsonNode rootNode,
            JsonNodePath instanceLocation, boolean validate, OutputFormat<T> format,
            ExecutionContextCustomizer executionCustomizer) {
        if (executionCustomizer != null) {
            executionCustomizer.customize(executionContext, this.validationContext);
        }
        // Walk through the schema.
        Set<ValidationMessage> errors = walk(executionContext, node, rootNode, instanceLocation, validate);

        // Get the config.
        SchemaValidatorsConfig config = this.validationContext.getConfig();
        // When walk is called in series of nested call we don't want to load the collectors every time. Leave to the API to decide when to call collectors.
        /* When doLoadCollectors is removed after the deprecation period the following block should be removed */
        if (config.doLoadCollectors()) {
            // Get the collector context.
            CollectorContext collectorContext = executionContext.getCollectorContext();

            // Load all the data from collectors into the context.
            collectorContext.loadCollectors();
        }
        return format.format(this, errors, executionContext, this.validationContext);
    }

    @Override
    public Set<ValidationMessage> walk(ExecutionContext executionContext, JsonNode node, JsonNode rootNode,
            JsonNodePath instanceLocation, boolean shouldValidateSchema) {
        Set<ValidationMessage> errors = new LinkedHashSet<>();
        // Walk through all the JSONWalker's.
        for (JsonValidator validator : getValidators()) {
            JsonNodePath evaluationPathWithKeyword = validator.getEvaluationPath();
            try {
                // Call all the pre-walk listeners. If at least one of the pre walk listeners
                // returns SKIP, then skip the walk.
                if (this.validationContext.getConfig().getKeywordWalkListenerRunner().runPreWalkListeners(executionContext,
                        evaluationPathWithKeyword.getName(-1), node, rootNode, instanceLocation,
                        this, validator)) {
                    Set<ValidationMessage> results = null;
                    try {
                        results = validator.walk(executionContext, node, rootNode, instanceLocation, shouldValidateSchema);
                    } finally {
                        if (results != null && !results.isEmpty()) {
                            errors.addAll(results);
                        }
                    }
                }
            } finally {
                // Call all the post-walk listeners.
                this.validationContext.getConfig().getKeywordWalkListenerRunner().runPostWalkListeners(executionContext,
                        evaluationPathWithKeyword.getName(-1), node, rootNode, instanceLocation,
                        this, validator, errors);
            }
        }
        return errors;
    }

    /************************ END OF WALK METHODS **********************************/
    @Override
    public String toString() {
        return "\"" + getEvaluationPath() + "\" : " + getSchemaNode().toString();
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
            for (final JsonValidator validator : getValidators()) {
                validator.preloadJsonSchema();
            }
            /*
             * This is only set to true after the preload as it may throw an exception for
             * instance if the remote host is unavailable and we may want to be able to try
             * again.
             */
            this.validatorsLoaded = true;
        }
    }

    public boolean isRecursiveAnchor() {
        return this.recursiveAnchor;
    }

    /**
     * Creates an execution context.
     * 
     * @return the execution context
     */
    public ExecutionContext createExecutionContext() {
        SchemaValidatorsConfig config = validationContext.getConfig();
        // Copy execution config defaults from validation config
        ExecutionConfig executionConfig = new ExecutionConfig();
        executionConfig.setLocale(config.getLocale());
        executionConfig.setFormatAssertionsEnabled(config.getFormatAssertionsEnabled());
        executionConfig.setFailFast(config.isFailFast());

        ExecutionContext executionContext = new ExecutionContext(executionConfig);
        if(config.getExecutionContextCustomizer() != null) {
            config.getExecutionContextCustomizer().customize(executionContext, validationContext);
        }
        return executionContext;
    }
}
