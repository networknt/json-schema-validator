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
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.util.Arrays;
import java.util.Collection;
import java.util.Set;

/**
 * Created by steve on 22/10/16.
 */
@RunWith(Parameterized.class)
public class PatternPropertiesValidatorTest extends BaseJsonSchemaValidatorTest {
    @Parameterized.Parameters
    public static Collection<?> parameters() {
      return Arrays.asList(new Object[][] {
         { SpecVersion.VersionFlag.V4 },
         { SpecVersion.VersionFlag.V6 },
         { SpecVersion.VersionFlag.V7 },
         { SpecVersion.VersionFlag.V201909 }
      });
    }
    
    public PatternPropertiesValidatorTest(final SpecVersion.VersionFlag specVersion) {
        super(specVersion);
    }
        
    @Test(expected=JsonSchemaException.class)
    public void testInvalidPatternPropertiesValidator() throws Exception {
        JsonSchemaFactory factory = JsonSchemaFactory.getInstance(this.specVersion);
        JsonSchema schema = factory.getSchema("{\"patternProperties\":6}");

        JsonNode node = getJsonNodeFromStringContent("");
        Set<ValidationMessage> errors = schema.validate(node);
        Assert.assertEquals(errors.size(), 0);
    }
}
