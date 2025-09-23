/*
 * Copyright (c) 2024 the original author or authors.
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

import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import com.networknt.schema.annotation.Annotation;
import com.networknt.schema.annotation.Annotations;

/**
 * AnnotationsTest.
 */
class AnnotationsTest {
    @Test
    void put() {
        Annotations annotations = new Annotations();
        Annotation annotation = new Annotation("unevaluatedProperties",
                new NodePath(PathType.JSON_POINTER), SchemaLocation.of(""), new NodePath(PathType.JSON_POINTER),
                "test");
        annotations.put(annotation);
        assertTrue(annotations.asMap().get(annotation.getInstanceLocation()).contains(annotation));
    }
}
