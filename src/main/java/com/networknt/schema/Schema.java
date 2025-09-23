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

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.function.Consumer;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.networknt.schema.Specification.Version;
import com.networknt.schema.keyword.DiscriminatorValidator;
import com.networknt.schema.keyword.KeywordValidator;
import com.networknt.schema.keyword.TypeValidator;
import com.networknt.schema.keyword.ValidatorTypeCode;
import com.networknt.schema.utils.JsonNodes;

/**
 * Used for creating a schema with validators for validating inputs. This is
 * created using SchemaRegistry#withDefaultDialect(Version, Consumer) and should
 * be cached for performance.
 * <p>
 * This is the core of json constraint implementation. It parses json constraint
 * file and generates JsonValidators. The class is thread safe, once it is
 * constructed, it can be used to validate multiple json data concurrently.
 * <p>
 * JsonSchema instances are thread-safe provided its configuration is not
 * modified.
 */
public class Schema implements Validator {
    private static final long V201909_VALUE = Version.DRAFT_2019_09.getOrder();
    private final String id;

    /**
     * The validators sorted and indexed by evaluation path.
     */
    private List<KeywordValidator> validators = null;
    private boolean validatorsLoaded = false;
    private boolean recursiveAnchor = false;
    private TypeValidator typeValidator = null;

    protected final JsonNode schemaNode;
    protected final Schema parentSchema;
    protected final SchemaLocation schemaLocation;
    protected final SchemaContext schemaContext;
    protected final boolean suppressSubSchemaRetrieval;

    protected final JsonNodePath evaluationPath;
    protected final Schema evaluationParentSchema;
    
    public JsonNode getSchemaNode() {
        return this.schemaNode;
    }
    
    public SchemaLocation getSchemaLocation() {
        return this.schemaLocation;
    }
    
    public Schema getParentSchema() {
        return parentSchema;
    }

    public boolean isSuppressSubSchemaRetrieval() {
        return suppressSubSchemaRetrieval;
    }

    public JsonNodePath getEvaluationPath() {
        return evaluationPath;
    }

    public Schema getEvaluationParentSchema() {
        if (this.evaluationParentSchema != null) {
            return this.evaluationParentSchema;
        }
        return getParentSchema();
    }

    protected Schema fetchSubSchemaNode(SchemaContext schemaContext) {
        return this.suppressSubSchemaRetrieval ? null : obtainSubSchemaNode(this.schemaNode, schemaContext);
    }
    
    private static Schema obtainSubSchemaNode(final JsonNode schemaNode, final SchemaContext schemaContext) {
        final JsonNode node = schemaNode.get("id");

        if (node == null) {
            return null;
        }

        if (node.equals(schemaNode.get("$schema"))) {
            return null;
        }

        final String text = node.textValue();
        if (text == null) {
            return null;
        }
        final SchemaLocation schemaLocation = SchemaLocation.of(node.textValue());
        return schemaContext.getSchemaRegistry().getSchema(schemaLocation);
    }
    public static class JsonNodePathLegacy {
        private static final JsonNodePath INSTANCE = new JsonNodePath(PathType.LEGACY);
        public static JsonNodePath getInstance() {
            return INSTANCE;
        }
    }

    public static class JsonNodePathJsonPointer {
        private static final JsonNodePath INSTANCE = new JsonNodePath(PathType.JSON_POINTER);
        public static JsonNodePath getInstance() {
            return INSTANCE;
        }
    }

    public static class JsonNodePathJsonPath {
        private static final JsonNodePath INSTANCE = new JsonNodePath(PathType.JSON_PATH);
        public static JsonNodePath getInstance() {
            return INSTANCE;
        }
    }

    public void validate(ExecutionContext executionContext, JsonNode node) {
        validate(executionContext, node, node, atRoot());
    }

    /**
     * Get the root path.
     *
     * @return The path.
     */
    protected JsonNodePath atRoot() {
        if (this.schemaContext.getSchemaRegistryConfig().getPathType().equals(PathType.JSON_POINTER)) {
            return JsonNodePathJsonPointer.getInstance();
        } else if (this.schemaContext.getSchemaRegistryConfig().getPathType().equals(PathType.LEGACY)) {
            return JsonNodePathLegacy.getInstance();
        } else if (this.schemaContext.getSchemaRegistryConfig().getPathType().equals(PathType.JSON_PATH)) {
            return JsonNodePathJsonPath.getInstance();
        }
        return new JsonNodePath(this.schemaContext.getSchemaRegistryConfig().getPathType());
    }    

    static Schema from(SchemaContext schemaContext, SchemaLocation schemaLocation, JsonNodePath evaluationPath, JsonNode schemaNode, Schema parent, boolean suppressSubSchemaRetrieval) {
        return new Schema(schemaContext, schemaLocation, evaluationPath, schemaNode, parent, suppressSubSchemaRetrieval);
    }

    private boolean hasNoFragment(SchemaLocation schemaLocation) {
        JsonNodePath fragment = this.schemaLocation.getFragment();
        return fragment == null || (fragment.getParent() == null && fragment.getNameCount() == 0);
    }

    private static SchemaLocation resolve(SchemaLocation schemaLocation, JsonNode schemaNode, boolean rootSchema,
            SchemaContext schemaContext) {
        String id = schemaContext.resolveSchemaId(schemaNode);
        if (id != null) {
            String resolve = id;
            int fragment = id.indexOf('#');
            // Check if there is a non-empty fragment
            if (fragment != -1 && !(fragment + 1 >= id.length())) {
                // strip the fragment when resolving
                resolve = id.substring(0, fragment);
            }
            SchemaLocation result = !"".equals(resolve) ? schemaLocation.resolve(resolve) : schemaLocation;
            SchemaIdValidator validator = schemaContext.getSchemaRegistryConfig().getSchemaIdValidator();
            if (validator != null) {
                if (!validator.validate(id, rootSchema, schemaLocation, result, schemaContext)) {
                    SchemaLocation idSchemaLocation = schemaLocation.append(schemaContext.getDialect().getIdKeyword());
                    Error error = Error.builder()
                            .messageKey(ValidatorTypeCode.ID.getValue()).keyword(ValidatorTypeCode.ID.getValue())
                            .instanceLocation(idSchemaLocation.getFragment())
                            .arguments(id, schemaContext.getDialect().getIdKeyword(), idSchemaLocation)
                            .schemaLocation(idSchemaLocation)
                            .schemaNode(schemaNode)
                            .messageFormatter(args -> schemaContext.getSchemaRegistryConfig().getMessageSource().getMessage(
                                    ValidatorTypeCode.ID.getValue(), schemaContext.getSchemaRegistryConfig().getLocale(), args))
                            .build();
                    throw new InvalidSchemaException(error);
                }
            }
            return result;
        } else {
            return schemaLocation;
        }
    }

    private Schema(SchemaContext schemaContext, SchemaLocation schemaLocation, JsonNodePath evaluationPath,
            JsonNode schemaNode, Schema parent, boolean suppressSubSchemaRetrieval) {
        /*
        super(resolve(schemaLocation, schemaNode, parent == null, schemaContext), evaluationPath, schemaNode, parent,
                null, null, schemaContext, suppressSubSchemaRetrieval);
        */
        this.schemaContext = schemaContext;
        this.schemaLocation = resolve(schemaLocation, schemaNode, parent == null, schemaContext);
        this.schemaNode = schemaNode;
        this.parentSchema = parent;
        this.suppressSubSchemaRetrieval = suppressSubSchemaRetrieval;
        this.evaluationPath = evaluationPath;
        this.evaluationParentSchema = null;
        
        String id = this.schemaContext.resolveSchemaId(this.schemaNode);
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
            this.schemaContext.getSchemaResources().putIfAbsent(result != null ? result.toString() : id, this);
        } else {
            if (hasNoFragment(schemaLocation)) {
                // No $id but there is no fragment and is thus a schema resource
                this.id = schemaLocation.getAbsoluteIri() != null ? schemaLocation.getAbsoluteIri().toString() : "";
                this.schemaContext.getSchemaResources()
                        .putIfAbsent(schemaLocation != null ? schemaLocation.toString() : this.id, this);
            } else {
                this.id = null;
            }
        }
        String anchor = this.schemaContext.getDialect().readAnchor(this.schemaNode);
        if (anchor != null) {
            String absoluteIri = this.schemaLocation.getAbsoluteIri() != null
                    ? this.schemaLocation.getAbsoluteIri().toString()
                    : "";
            this.schemaContext.getSchemaResources()
                    .putIfAbsent(absoluteIri + "#" + anchor, this);
        }
        String dynamicAnchor = this.schemaContext.getDialect().readDynamicAnchor(schemaNode);
        if (dynamicAnchor != null) {
            String absoluteIri = this.schemaLocation.getAbsoluteIri() != null
                    ? this.schemaLocation.getAbsoluteIri().toString()
                    : "";
            this.schemaContext.getDynamicAnchors()
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
     * @param schemaContext the schema context
     * @param parentSchema the parent schema
     * @param schemaLocation the schema location
     * @param evaluationPath the evaluation path
     * @param evaluationParentSchema the evaluation parent schema
     * @param errorMessage the error message
     */
    protected Schema(
            /* Below from JsonSchema */
            List<KeywordValidator> validators,
            boolean validatorsLoaded,
            boolean recursiveAnchor,
            TypeValidator typeValidator,
            String id,            
            /* Below from BaseJsonValidator */
            boolean suppressSubSchemaRetrieval,
            JsonNode schemaNode,
            SchemaContext schemaContext,
            Schema parentSchema,
            SchemaLocation schemaLocation,
            JsonNodePath evaluationPath,
            Schema evaluationParentSchema,
            Map<String, String> errorMessage) {
//        super(suppressSubSchemaRetrieval, schemaNode, schemaContext, errorMessageType, keyword,
//                parentSchema, schemaLocation, evaluationPath, evaluationParentSchema, errorMessage);
        this.validators = validators;
        this.validatorsLoaded = validatorsLoaded;
        this.recursiveAnchor = recursiveAnchor;
        this.typeValidator = typeValidator;
        this.id = id;
        
        this.schemaContext = schemaContext;
        this.schemaLocation = schemaLocation;
        this.schemaNode = schemaNode;
        this.parentSchema = parentSchema;
        this.suppressSubSchemaRetrieval = suppressSubSchemaRetrieval;
        this.evaluationPath = evaluationPath;
        this.evaluationParentSchema = evaluationParentSchema;
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
    public Schema fromRef(Schema refEvaluationParentSchema, JsonNodePath refEvaluationPath) {
        SchemaContext schemaContext = new SchemaContext(this.getSchemaContext().getDialect(),
                this.getSchemaContext().getSchemaRegistry(),
                refEvaluationParentSchema.getSchemaContext().getSchemaReferences(),
                refEvaluationParentSchema.getSchemaContext().getSchemaResources(),
                refEvaluationParentSchema.getSchemaContext().getDynamicAnchors());
        JsonNodePath evaluationPath = refEvaluationPath;
        Schema evaluationParentSchema = refEvaluationParentSchema;
        // Validator state is reset due to the changes in evaluation path
        boolean validatorsLoaded = false;
        TypeValidator typeValidator = null;
        List<KeywordValidator> validators = null;

        return new Schema(
                /* Below from JsonSchema */
                validators,
                validatorsLoaded,
                recursiveAnchor,
                typeValidator,
                id,            
                /* Below from BaseJsonValidator */
                suppressSubSchemaRetrieval,
                schemaNode,
                schemaContext,
                parentSchema, schemaLocation, evaluationPath,
                evaluationParentSchema, /* errorMessage */ null);
    }

//    public Schema withConfig(SchemaValidatorsConfig config) {
//        if (this.getValidationContext().getMetaSchema().getKeywords().containsKey("discriminator")
//                && !config.isDiscriminatorKeywordEnabled()) {
//            config = SchemaValidatorsConfig.builder(config)
//                    .discriminatorKeywordEnabled(true)
//                    .nullableKeywordEnabled(true)
//                    .build();
//        }
//        if (!this.getValidationContext().getSchemaRegistryConfig().equals(config)) {
//            ValidationContext schemaContext = new ValidationContext(this.getValidationContext().getMetaSchema(),
//                    this.getValidationContext().getSchemaRegistry(), config,
//                    this.getValidationContext().getSchemaReferences(),
//                    this.getValidationContext().getSchemaResources(),
//                    this.getValidationContext().getDynamicAnchors());
//            boolean validatorsLoaded = false;
//            TypeValidator typeValidator = null;
//            List<KeywordValidator> validators = null;
//            return new Schema(
//                    /* Below from JsonSchema */
//                    validators,
//                    validatorsLoaded,
//                    recursiveAnchor,
//                    typeValidator,
//                    id,            
//                    /* Below from BaseJsonValidator */
//                    suppressSubSchemaRetrieval,
//                    schemaNode,
//                    schemaContext,
//                    parentSchema,
//                    schemaLocation,
//                    evaluationPath,
//                    evaluationParentSchema,
//                    /* errorMessage */ null);
//
//        }
//        return this;
//    }

    public SchemaContext getSchemaContext() {
        return this.schemaContext;
    }

    /**
     * Find the schema node for $ref attribute.
     *
     * @param ref String
     * @return JsonNode
     */
    public JsonNode getRefSchemaNode(String ref) {
        Schema schema = findSchemaResourceRoot();
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

    public Schema getRefSchema(JsonNodePath fragment) {
        if (PathType.JSON_POINTER.equals(fragment.getPathType())) {
            // Json Pointer
            return getSubSchema(fragment);
        } else {
            // Anchor
            String base = this.getSchemaLocation().getAbsoluteIri() != null ? this.schemaLocation.getAbsoluteIri().toString() : "";
            String anchor = base + "#" + fragment;
            Schema result = this.schemaContext.getSchemaResources().get(anchor);
            if (result == null) {
                result  = this.schemaContext.getDynamicAnchors().get(anchor);
            }
            if (result == null) {
                throw new SchemaException("Unable to find anchor "+anchor);
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
    public Schema getSubSchema(JsonNodePath fragment) {
        Schema document = findSchemaResourceRoot(); 
        Schema parent = document; 
        Schema subSchema = null;
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
                 * The parent schema context is used to create as there can be changes in
                 * $schema is later drafts which means the schema context can change.
                 */
                // This may need a redesign see Issue 939 and 940
                String id = parent.getSchemaContext().resolveSchemaId(subSchemaNode);
//                if (!("definitions".equals(segment.toString()) || "$defs".equals(segment.toString())
//                        )) {
                if (id != null || x == nameCount - 1) {
                    subSchema = parent.getSchemaContext().newSchema(schemaLocation, evaluationPath, subSchemaNode,
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
                Schema found = document.findSchemaResourceRoot().fetchSubSchemaNode(this.schemaContext);
                if (found != null) {
                    found = found.getSubSchema(fragment);
                }
                if (found == null) {
                    Error error = Error.builder()
                            .keyword(ValidatorTypeCode.REF.getValue()).messageKey("internal.unresolvedRef")
                            .message("Reference {0} cannot be resolved")
                            .instanceLocation(schemaLocation.getFragment())
                            .schemaLocation(schemaLocation)
                            .evaluationPath(evaluationPath)
                            .arguments(fragment).build();
                    throw new InvalidSchemaRefException(error);
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

    public Schema findLexicalRoot() {
        Schema ancestor = this;
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
    public Schema findSchemaResourceRoot() {
        Schema ancestor = this;
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

    public Schema findAncestor() {
        Schema ancestor = this;
        if (this.getParentSchema() != null) {
            ancestor = this.getParentSchema().findAncestor();
        }
        return ancestor;
    }

    private JsonNode handleNullNode(String ref, Schema schema) {
        Schema subSchema = schema.fetchSubSchemaNode(this.schemaContext);
        if (subSchema != null) {
            return subSchema.getRefSchemaNode(ref);
        }
        return null;
    }
    
    /**
     * Please note that the key in {@link #validators} map is the evaluation path.
     */
    private List<KeywordValidator> read(JsonNode schemaNode) {
        List<KeywordValidator> validators;
        if (schemaNode.isBoolean()) {
            validators = new ArrayList<>(1);
            if (schemaNode.booleanValue()) {
                JsonNodePath path = getEvaluationPath().append("true");
                KeywordValidator validator = this.schemaContext.newValidator(getSchemaLocation().append("true"), path,
                        "true", schemaNode, this);
                validators.add(validator);
            } else {
                JsonNodePath path = getEvaluationPath().append("false");
                KeywordValidator validator = this.schemaContext.newValidator(getSchemaLocation().append("false"),
                        path, "false", schemaNode, this);
                validators.add(validator);
            }
        } else {
            KeywordValidator refValidator = null;

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
                        Error error = Error.builder().keyword("$recursiveAnchor")
                                .messageKey("internal.invalidRecursiveAnchor")
                                .message(
                                        "The value of a $recursiveAnchor must be a Boolean literal but is {0}")
                                .instanceLocation(path)
                                .evaluationPath(path)
                                .schemaLocation(schemaPath)
                                .arguments(nodeToUse.getNodeType().toString())
                                .build();
                        throw new SchemaException(error);
                    }
                    this.recursiveAnchor = nodeToUse.booleanValue();
                }

                KeywordValidator validator = this.schemaContext.newValidator(schemaPath, path,
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
        return this.schemaContext
            .activeDialect()
            .map(Version::getOrder)
            .orElse(Integer.MAX_VALUE);
    }

    /**
     * A comparator that sorts validators, such that 'properties' comes before 'required',
     * so that we can apply default values before validating required.
     */
    private static final Comparator<KeywordValidator> VALIDATOR_SORT = (lhs, rhs) -> {
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
    public void validate(ExecutionContext executionContext, JsonNode jsonNode, JsonNode rootNode, JsonNodePath instanceLocation) {
        if (this.schemaContext.isDiscriminatorKeywordEnabled()) {
            ObjectNode discriminator = (ObjectNode) schemaNode.get("discriminator");
            if (null != discriminator && null != executionContext.getCurrentDiscriminatorContext()) {
                executionContext.getCurrentDiscriminatorContext().registerDiscriminator(schemaLocation,
                        discriminator);
            }
        }
        int currentErrors = executionContext.getErrors().size();
        for (KeywordValidator v : getValidators()) {
            v.validate(executionContext, jsonNode, rootNode, instanceLocation);
        }

        if (this.schemaContext.isDiscriminatorKeywordEnabled()) {
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
                    DiscriminatorValidator.checkDiscriminatorMatch(discriminatorContext, discriminatorToUse, discriminatorPropertyValue,
                            this);
                }
            }
        }

        if (executionContext.getErrors().size() > currentErrors) {
            // Failed with assertion set result and drop all annotations from this schema
            // and all subschemas
            executionContext.getResults().setResult(instanceLocation, getSchemaLocation(), getEvaluationPath(), false);
        }
    }

    /**
     * Validate the given root JsonNode, starting at the root of the data path.
     * <p>
     * Note that since Draft 2019-09 by default format generates only annotations
     * and not assertions.
     * <p>
     * Use {@link ExecutionConfig.Builder#formatAssertionsEnabled(Boolean)} to override
     * the default.
     * 
     * @param rootNode the root node
     * @return A list of Error if there is any validation error, or an
     *         empty list if there is no error.
     */
    public List<Error> validate(JsonNode rootNode) {
        return validate(rootNode, OutputFormat.DEFAULT);
    }

    /**
     * Validate the given root JsonNode, starting at the root of the data path.
     * <p>
     * Note that since Draft 2019-09 by default format generates only annotations
     * and not assertions.
     * <p>
     * Use {@link ExecutionConfig.Builder#formatAssertionsEnabled(Boolean)} to override
     * the default.
     *
     * @param rootNode            the root node
     * @param executionCustomizer the execution customizer
     * @return the assertions
     */
    public List<Error> validate(JsonNode rootNode, ExecutionContextCustomizer executionCustomizer) {
        return validate(rootNode, OutputFormat.DEFAULT, executionCustomizer);
    }

    /**
     * Validate the given root JsonNode, starting at the root of the data path.
     * <p>
     * Note that since Draft 2019-09 by default format generates only annotations
     * and not assertions.
     * <p>
     * Use {@link ExecutionConfig.Builder#formatAssertionsEnabled(Boolean)} to override
     * the default.
     * 
     * @param rootNode            the root node
     * @param executionCustomizer the execution customizer
     * @return the assertions
     */
    public List<Error> validate(JsonNode rootNode, Consumer<ExecutionContext> executionCustomizer) {
        return validate(rootNode, OutputFormat.DEFAULT, executionCustomizer);
    }

    /**
     * Validates the given root JsonNode, starting at the root of the data path. The
     * output will be formatted using the formatter specified.
     * <p>
     * Note that since Draft 2019-09 by default format generates only annotations
     * and not assertions.
     * <p>
     * Use {@link ExecutionConfig.Builder#formatAssertionsEnabled(Boolean)} to override
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
     * Use {@link ExecutionConfig.Builder#formatAssertionsEnabled(Boolean)} to override
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
     * Use {@link ExecutionConfig.Builder#formatAssertionsEnabled(Boolean)} to override
     * the default.
     * 
     * @param <T>                 the result type
     * @param rootNode            the root node
     * @param format              the formatter
     * @param executionCustomizer the execution customizer
     * @return the result
     */
    public <T> T validate(JsonNode rootNode, OutputFormat<T> format, Consumer<ExecutionContext> executionCustomizer) {
        return validate(createExecutionContext(), rootNode, format, (executionContext, schemaContext) -> executionCustomizer.accept(executionContext));
    }

    /**
     * Validate the given input string using the input format, starting at the root
     * of the data path.
     * <p>
     * Note that since Draft 2019-09 by default format generates only annotations
     * and not assertions.
     * <p>
     * Use {@link ExecutionConfig.Builder#formatAssertionsEnabled(Boolean)} to override
     * the default.
     * 
     * @param input       the input
     * @param inputFormat the inputFormat
     * @return A list of Error if there is any validation error, or an
     *         empty list if there is no error.
     */
    public List<Error> validate(String input, InputFormat inputFormat) {
        return validate(deserialize(input, inputFormat), OutputFormat.DEFAULT);
    }

    /**
     * Validate the given input string using the input format, starting at the root
     * of the data path.
     * <p>
     * Note that since Draft 2019-09 by default format generates only annotations
     * and not assertions.
     * <p>
     * Use {@link ExecutionConfig.Builder#formatAssertionsEnabled(Boolean)} to override
     * the default.
     *
     * @param input               the input
     * @param inputFormat         the inputFormat
     * @param executionCustomizer the execution customizer
     * @return the assertions
     */
    public List<Error> validate(String input, InputFormat inputFormat, ExecutionContextCustomizer executionCustomizer) {
        return validate(deserialize(input, inputFormat), OutputFormat.DEFAULT, executionCustomizer);
    }

    /**
     * Validate the given input string using the input format, starting at the root
     * of the data path.
     * <p>
     * Note that since Draft 2019-09 by default format generates only annotations
     * and not assertions.
     * <p>
     * Use {@link ExecutionConfig.Builder#formatAssertionsEnabled(Boolean)} to override
     * the default.
     * 
     * @param input               the input
     * @param inputFormat         the inputFormat
     * @param executionCustomizer the execution customizer
     * @return the assertions
     */
    public List<Error> validate(String input, InputFormat inputFormat, Consumer<ExecutionContext> executionCustomizer) {
        return validate(deserialize(input, inputFormat), OutputFormat.DEFAULT, executionCustomizer);
    }

    /**
     * Validates the given input string using the input format, starting at the root
     * of the data path. The output will be formatted using the formatter specified.
     * <p>
     * Note that since Draft 2019-09 by default format generates only annotations
     * and not assertions.
     * <p>
     * Use {@link ExecutionConfig.Builder#formatAssertionsEnabled(Boolean)} to override
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
     * Use {@link ExecutionConfig.Builder#formatAssertionsEnabled(Boolean)} to override
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
     * Use {@link ExecutionConfig.Builder#formatAssertionsEnabled(Boolean)} to override
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
        return validate(createExecutionContext(), deserialize(input, inputFormat), format, (executionContext, schemaContext) -> executionCustomizer.accept(executionContext));
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
        format.customize(executionContext, this.schemaContext);
        if (executionCustomizer != null) {
            executionCustomizer.customize(executionContext, this.schemaContext);
        }
        try {
            validate(executionContext, node);
        } catch (FailFastAssertionException e) {
            executionContext.setErrors(e.getErrors());
        }
        return format.format(this, executionContext, this.schemaContext);
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
            return this.getSchemaContext().getSchemaRegistry().readTree(input, inputFormat);
        } catch (IOException e) {
            throw new IllegalArgumentException("Invalid input", e);
        }
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
     * @param node     the input
     * @param validate true to validate the input against the schema
     * @param executionCustomizer the customizer
     * @return the validation result
     */
    public ValidationResult walk(JsonNode node, boolean validate, ExecutionContextCustomizer executionCustomizer) {
        return walk(createExecutionContext(), node, validate, executionCustomizer);
    }

    /**
     * Walk the JSON node.
     * 
     * @param node     the input
     * @param validate true to validate the input against the schema
     * @param executionCustomizer the customizer
     * @return the validation result
     */
    public ValidationResult walk(JsonNode node, boolean validate, Consumer<ExecutionContext> executionCustomizer) {
        return walk(createExecutionContext(), node, validate, executionCustomizer);
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
                (executeContext, schemaContext) -> executionCustomizer.accept(executeContext));
    }

    private <T> T walkAtNodeInternal(ExecutionContext executionContext, JsonNode node, JsonNode rootNode,
            JsonNodePath instanceLocation, boolean validate, OutputFormat<T> format,
            ExecutionContextCustomizer executionCustomizer) {
        if (executionCustomizer != null) {
            executionCustomizer.customize(executionContext, this.schemaContext);
        }
        // Walk through the schema.
        walk(executionContext, node, rootNode, instanceLocation, validate);
        return format.format(this, executionContext, this.schemaContext);
    }

    @Override
    public void walk(ExecutionContext executionContext, JsonNode node, JsonNode rootNode,
            JsonNodePath instanceLocation, boolean shouldValidateSchema) {
        // Walk through all the JSONWalker's.
        int currentErrors = executionContext.getErrors().size();
        for (KeywordValidator validator : getValidators()) {
            JsonNodePath evaluationPathWithKeyword = validator.getEvaluationPath();
            try {
                // Call all the pre-walk listeners. If at least one of the pre walk listeners
                // returns SKIP, then skip the walk.
                if (executionContext.getWalkConfig().getKeywordWalkListenerRunner().runPreWalkListeners(executionContext,
                        evaluationPathWithKeyword.getName(-1), node, rootNode, instanceLocation,
                        this, validator)) {
                    validator.walk(executionContext, node, rootNode, instanceLocation, shouldValidateSchema);
                }
            } finally {
                // Call all the post-walk listeners.
                executionContext.getWalkConfig().getKeywordWalkListenerRunner().runPostWalkListeners(executionContext,
                        evaluationPathWithKeyword.getName(-1), node, rootNode, instanceLocation,
                        this, validator,
                        executionContext.getErrors().subList(currentErrors, executionContext.getErrors().size()));
            }
        }
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

    public List<KeywordValidator> getValidators() {
        if (this.validators == null) {
            this.validators = Collections.unmodifiableList(read(getSchemaNode()));
        }
        return this.validators;
    }

    /**
     * Initializes the validators' {@link com.networknt.schema.Schema} instances.
     * For avoiding issues with concurrency, in 1.0.49 the {@link com.networknt.schema.Schema} instances affiliated with
     * validators were modified to no more preload the schema and lazy loading is used instead.
     * <p>This comes with the issue that this way you cannot rely on validating important schema features, in particular
     * <code>$ref</code> resolution at instantiation from {@link com.networknt.schema.SchemaRegistry}.</p>
     * <p>By calling <code>initializeValidators</code> you can enforce preloading of the {@link com.networknt.schema.Schema}
     * instances of the validators.</p>
     */
    public void initializeValidators() {
        if (!this.validatorsLoaded) {
            for (final KeywordValidator validator : getValidators()) {
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
        SchemaRegistryConfig config = schemaContext.getSchemaRegistryConfig();
        // Copy execution config defaults from validation config
		ExecutionConfig executionConfig = ExecutionConfig.builder()
				.locale(config.getLocale())
				.formatAssertionsEnabled(config.getFormatAssertionsEnabled())
				.failFast(config.isFailFast()).build();
        ExecutionContext executionContext = new ExecutionContext(executionConfig);
        if(config.getExecutionContextCustomizer() != null) {
            config.getExecutionContextCustomizer().customize(executionContext, schemaContext);
        }
        return executionContext;
    }
}
