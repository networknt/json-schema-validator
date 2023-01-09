/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.networknt.schema.format;

import com.fasterxml.jackson.databind.JsonNode;
import com.networknt.schema.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 *
 * <p>This class provides methods to validate a duration format .
 *
 * @version $Revision$
 * @since Validator 1.4
 */
public class DurationValidator extends BaseJsonValidator implements JsonValidator {
    private static final Logger logger = LoggerFactory.getLogger(DurationValidator.class);

    private static final String DURATION_REGEX = "^(-?)P(?=\\d|T\\d)(?:(\\d+)Y)?(?:(\\d+)M)?(?:(\\d+)([DW]))?(?:T(?:(\\d+)H)?(?:(\\d+)M)?(?:(\\d+(?:\\.\\d+)?)S)?)?$";
    private static final Pattern DURATION_PATTERN = Pattern.compile(DURATION_REGEX);
    private final String formatName;



    /**
     * <p>Checks if a field has a valid duration.</p>
     *
     * @param duration The value validation is being performed on.  A <code>null</code>
     *              value is considered invalid.
     * @return true if the duration valid.
     */
    public boolean isValid(String duration) {
        if (duration == null) {
            return false;
        }

        Matcher durationMatcher = DURATION_PATTERN.matcher(duration);
        if (!durationMatcher.matches()) {
            return false;
        }

        return true;
    }

    public DurationValidator(String schemaPath, JsonNode schemaNode, JsonSchema parentSchema, ValidationContext validationContext, String formatName, ValidatorTypeCode type) {
        super(schemaPath, schemaNode, parentSchema, type, validationContext);
        this.formatName = formatName;
        this.validationContext = validationContext;
        parseErrorCode(getValidatorType().getErrorCodeKey());
    }

    @Override
    public Set<ValidationMessage> validate(JsonNode node, JsonNode rootNode, String at) {
        debug(logger, node, rootNode, at);

        Set<ValidationMessage> errors = new LinkedHashSet<ValidationMessage>();

        JsonType nodeType = TypeFactory.getValueNodeType(node, this.validationContext.getConfig());
        if (nodeType != JsonType.STRING) {
            return errors;
        }
        if (!isValid(node.textValue())) {
            errors.add(buildValidationMessage(at, node.textValue(), formatName));
        }
        return Collections.unmodifiableSet(errors);

    }
}