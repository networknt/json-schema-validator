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
import org.junit.Test;

/**
 * Created by stevehu on 2016-12-20.
 */
public class SelfRefTest extends BaseJsonSchemaValidatorTest {
    @Test
    public void testSelfRef() throws Exception {
        JsonNode node = getJsonNodeFromClasspath("selfref.json");
        System.out.println("node = " + node);
    }
}
