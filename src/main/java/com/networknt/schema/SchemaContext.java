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

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import com.fasterxml.jackson.databind.JsonNode;
import com.networknt.schema.dialect.Dialect;
import com.networknt.schema.keyword.KeywordValidator;
import com.networknt.schema.path.NodePath;

/**
 * The schema context associated with a schema and all its validators.
 */
public class SchemaContext {
    private final Dialect dialect;
    private final SchemaRegistry schemaRegistry;
    private final ConcurrentMap<String, Schema> schemaReferences;
    private final ConcurrentMap<String, Schema> schemaResources;
    private final ConcurrentMap<String, Schema> dynamicAnchors;
    
    /**
     * When set to true, support for discriminators is enabled for validations of
     * oneOf, anyOf and allOf as described on <a href=
     * "https://github.com/OAI/OpenAPI-Specification/blob/master/versions/3.0.3.md#discriminatorObject">GitHub</a>.
     *
     * When enabled, the validation of <code>anyOf</code> and <code>allOf</code> in
     * polymorphism will respect OpenAPI 3 style discriminators as described in the
     * <a href=
     * "https://github.com/OAI/OpenAPI-Specification/blob/master/versions/3.0.3.md#discriminatorObject">OpenAPI
     * 3.0.3 spec</a>. The presence of a discriminator configuration on the schema
     * will lead to the following changes in the behavior:
     * <ul>
     * <li>for <code>oneOf</code> the spec is unfortunately very vague. Whether
     * <code>oneOf</code> semantics should be affected by discriminators or not is
     * not even 100% clear within the members of the OAS steering committee.
     * Therefore <code>oneOf</code> at the moment ignores discriminators</li>
     * <li>for <code>anyOf</code> the validation will choose one of the candidate
     * schemas for validation based on the discriminator property value and will
     * pass validation when this specific schema passes. This is in particular
     * useful when the payload could match multiple candidates in the
     * <code>anyOf</code> list and could lead to ambiguity. Example: type B has all
     * mandatory properties of A and adds more mandatory ones. Whether the payload
     * is an A or B is determined via the discriminator property name. A payload
     * indicating it is an instance of B then requires passing the validation of B
     * and passing the validation of A would not be sufficient anymore.</li>
     * <li>for <code>allOf</code> use cases with discriminators defined on the
     * copied-in parent type, it is possible to automatically validate against a
     * subtype. Example: some schema specifies that there is a field of type A. A
     * carries a discriminator field and B inherits from A. Then B is automatically
     * a candidate for validation as well and will be chosen in case the
     * discriminator property matches</li>
     * </ul>
     * 
     * @param openAPI3StyleDiscriminators whether discriminators should be used.
     *                                    Defaults to <code>false</code>
     * @since 1.0.51
     */
    private final boolean discriminatorKeywordEnabled;

    /**
     * When a field is set as nullable in the OpenAPI specification, the schema
     * validator validates that it is nullable however continues with validation
     * against the nullable field
     * <p>
     * If handleNullableField is set to true && incoming field is nullable && value
     * is field: null --> succeed If handleNullableField is set to false && incoming
     * field is nullable && value is field: null --> it is up to the type validator
     * using the SchemaValidator to handle it.
     */
    private final boolean nullableKeywordEnabled;

    public SchemaContext(Dialect dialect,
                             SchemaRegistry schemaRegistry) {
        this(dialect, schemaRegistry, new ConcurrentHashMap<>(), new ConcurrentHashMap<>(), new ConcurrentHashMap<>());
    }

    public SchemaContext(Dialect dialect, SchemaRegistry schemaRegistry,
            ConcurrentMap<String, Schema> schemaReferences,
            ConcurrentMap<String, Schema> schemaResources, ConcurrentMap<String, Schema> dynamicAnchors) {
        if (dialect == null) {
            throw new IllegalArgumentException("Dialect must not be null");
        }
        if (schemaRegistry == null) {
            throw new IllegalArgumentException("SchemaRegistry must not be null");
        }
        this.dialect = dialect;
        this.schemaRegistry = schemaRegistry;
        this.schemaReferences = schemaReferences;
        this.schemaResources = schemaResources;
        this.dynamicAnchors = dynamicAnchors;

        if (dialect.getKeywords().containsKey("discriminator")) {
            this.discriminatorKeywordEnabled = true;
        } else {
            this.discriminatorKeywordEnabled = false;
        }

        if (dialect.getKeywords().containsKey("nullable")) {
            this.nullableKeywordEnabled = true;
        } else {
            this.nullableKeywordEnabled = false;
        }
    }

    public Schema newSchema(SchemaLocation schemaLocation, NodePath evaluationPath, JsonNode schemaNode, Schema parentSchema) {
        return getSchemaRegistry().create(this, schemaLocation, evaluationPath, schemaNode, parentSchema);
    }

    public KeywordValidator newValidator(SchemaLocation schemaLocation, NodePath evaluationPath,
            String keyword /* keyword */, JsonNode schemaNode, Schema parentSchema) {
        return this.dialect.newValidator(this, schemaLocation, evaluationPath, keyword, schemaNode, parentSchema);
    }

    public String resolveSchemaId(JsonNode schemaNode) {
        return this.dialect.readId(schemaNode);
    }

    public SchemaRegistry getSchemaRegistry() {
        return this.schemaRegistry;
    }

    public SchemaRegistryConfig getSchemaRegistryConfig() {
        return this.schemaRegistry.getSchemaRegistryConfig();
    }

    /**
     * Gets the schema references identified by the ref uri.
     *
     * @return the schema references
     */
    public ConcurrentMap<String, Schema> getSchemaReferences() {
        return this.schemaReferences;
    }

    /**
     * Gets the schema resources identified by id.
     *
     * @return the schema resources
     */
    public ConcurrentMap<String, Schema> getSchemaResources() {
        return this.schemaResources;
    }

    /**
     * Gets the dynamic anchors.
     *
     * @return the dynamic anchors
     */
    public ConcurrentMap<String, Schema> getDynamicAnchors() {
        return this.dynamicAnchors;
    }

    public Dialect getDialect() {
        return this.dialect;
    }

    public boolean isDiscriminatorKeywordEnabled() {
        return discriminatorKeywordEnabled;
    }

    public boolean isNullableKeywordEnabled() {
        return nullableKeywordEnabled;
    }
}
