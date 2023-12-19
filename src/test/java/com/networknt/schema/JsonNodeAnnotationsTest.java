/*
 * Copyright (c) 2023 the original author or authors.
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

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Test;

import com.networknt.schema.annotation.JsonNodeAnnotation;
import com.networknt.schema.annotation.JsonNodeAnnotationPredicate;
import com.networknt.schema.annotation.JsonNodeAnnotations;

class JsonNodeAnnotationsTest {

    @Test
    void filter() {
        JsonNodePath instanceRoot = new JsonNodePath(PathType.JSON_POINTER);
        JsonNodePath instance = instanceRoot.append("foo");

        JsonNodePath evaluationRoot = new JsonNodePath(PathType.JSON_POINTER).append("properties").append("foo");

        JsonNodeAnnotations annotations = new JsonNodeAnnotations();

        annotations.put(JsonNodeAnnotation.builder().keyword("title").instanceLocation(instance)
                .evaluationPath(evaluationRoot.append("$ref").append("title")).value("Title in reference target")
                .build());
        annotations.put(JsonNodeAnnotation.builder().keyword("title").instanceLocation(instance)
                .evaluationPath(evaluationRoot.append("title")).value("Title adjacent to reference").build());
        annotations.put(JsonNodeAnnotation.builder().keyword("description").instanceLocation(instance)
                .evaluationPath(evaluationRoot.append("$ref").append("description")).value("Even more text").build());
        annotations.put(JsonNodeAnnotation.builder().keyword("description").instanceLocation(instance)
                .evaluationPath(evaluationRoot.append("description")).value("Lots of text").build());
        // 1 instance
        assertEquals(1, annotations.asMap().size());

        // 2 keywords
        assertEquals(2, annotations.asMap().get(instance).size());

        List<JsonNodeAnnotation> all = annotations.stream().toList();
        assertEquals(4, all.size());

        List<JsonNodeAnnotation> titles = annotations.stream()
                .filter(filter -> filter.keyword(keyword -> "title".equals(keyword))).toList();
        assertEquals(2, titles.size());
        
        List<JsonNodeAnnotation> allTitlesResult = all.stream()
                .filter(JsonNodeAnnotationPredicate.builder().keyword(keyword -> "title".equals(keyword)).build())
                .collect(Collectors.toList());
        assertEquals(2, allTitlesResult.size());

    }
}
