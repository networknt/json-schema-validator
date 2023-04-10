/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
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
 * Validates that a string property conforms to RFC 3339's understanding of
 * duration as defined in ISO 8601:1988. This understanding is captured in
 * <a href="https://www.rfc-editor.org/rfc/rfc3339.html#appendix-A">appendix
 * A of RFC 3339</a>.
 * <p>
 * This validator will enforce the strict definition of RFC 3339 unless
 * {@code SchemaValidatorsConfig.isStrict()} return {@literal false}.
 * <p>
 * JSON Schema Draft 2019-09 and later uses RFC 3339 to define dates and times.
 * RFC 3339 bases its definition of duration of what is in the 1988 version of
 * ISO 1801, which is over 35 years old and has undergone many changes with
 * updates in 1991, 2000, 2004, 2019 and an amendment in 2022.
 * <p>
 * There are notable differences between the current version of ISO 8601 and
 * RFC 3339:
 * <ul>
 *   <li>ISO 8601-2:2019 permits negative durations</li>
 *   <li>
 *   ISO 8601-2:2019 permits combining weeks with other terms (e.g. {@literal
 *   P1Y13W})
 *   </li>
 * </ul>
 * <p>
 * There are notable differences in how RFC 3339 defines a duration compared
 * with how the Java Date/Time API defines it:
 * <ul>
 *   <li>
 *   {@link java.time.Duration} accepts fractional seconds; RFC 3339 does not
 *   </li>
 *   <li>
 *   {@link java.time.Period} does not accept a time component while RFC 3339
 *   accepts both a date and time component
 *   </li>
 *   <li>
 *   {@link java.time.Duration} accepts days but not years, months or weeks
 *   </li>
 * </ul>
 */
public class DurationValidator extends BaseJsonValidator {
    private static final Logger logger = LoggerFactory.getLogger(DurationValidator.class);

    private static final Pattern STRICT = Pattern.compile("^(?:P\\d+W)|(?:P(?:\\d+Y)?(?:\\d+M)?(?:\\d+D)?(?:T(?:\\d+H)?(?:\\d+M)?(?:\\d+S)?)?)$");
    private static final Pattern LAX = Pattern.compile("^(?:[-+]?)P(?:[-+]?[0-9]+Y)?(?:[-+]?[0-9]+M)?(?:[-+]?[0-9]+W)?(?:[-+]?[0-9]+D)?(?:T(?:[-+]?[0-9]+H)?(?:[-+]?[0-9]+M)?(?:[-+]?[0-9]+(?:[.,][0-9]{0,9})?S)?)?$");

    private final String formatName;
    private final boolean strict;

    public DurationValidator(String schemaPath, JsonNode schemaNode, JsonSchema parentSchema, ValidationContext validationContext, String formatName, ValidatorTypeCode type) {
        super(schemaPath, schemaNode, parentSchema, type, validationContext);
        this.formatName = formatName;
        this.validationContext = validationContext;
        this.strict = validationContext.getConfig().isStrict("duration");
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

    /**
     * Checks if a field has a valid duration.
     * A {@literal null} value is considered valid.
     *
     * @param duration The value to test.
     * @return true if the duration valid.
     */
    private boolean isValid(String duration) {
        if (null == duration) {
            return true;
        }

        if (duration.endsWith("P") || duration.endsWith("T")) {
            return false;
        }

        Pattern pattern = strict ? STRICT : LAX;
        Matcher matcher = pattern.matcher(duration);
        return matcher.matches();
    }

}
