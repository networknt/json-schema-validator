/*
 * Copyright (c) 2020 Network New Technologies Inc.
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

public class ValidatorState {

    public static final String VALIDATOR_STATE_KEY = "com.networknt.schema.ValidatorState";

    /**
     * Flag set when a node has matched Works in conjunction with the next flag:
     * isComplexValidator, to be used for complex validators such as oneOf, for ex
     */
    private boolean matchedNode = true;

    /**
     * Flag set if complex validators such as oneOf, for ex, neeed to have their
     * properties validated. The PropertiesValidator is not aware generally of a
     * complex validator is being validated or a simple poperty tree
     */
    private boolean isComplexValidator = false;

    /**
     * Flag to check if walking is enabled.
     */
    private boolean isWalkEnabled = false;

    /**
     * Flag to check if validation is enabled while walking.
     */
    private boolean isValidationEnabledWhileWalking = false;

    public void setMatchedNode(boolean matchedNode) {
        this.matchedNode = matchedNode;
    }

    public boolean hasMatchedNode() {
        return matchedNode;
    }

    public boolean isComplexValidator() {
        return isComplexValidator;
    }

    public void setComplexValidator(boolean isComplexValidator) {
        this.isComplexValidator = isComplexValidator;
    }

    public boolean isWalkEnabled() {
        return isWalkEnabled;
    }

    public void setWalkEnabled(boolean isWalkEnabled) {
        this.isWalkEnabled = isWalkEnabled;
    }

    public boolean isValidationEnabledWhileWalking() {
        return isValidationEnabledWhileWalking;
    }

    public void setValidationEnabledWhileWalking(boolean isValidationEnabledWhileWalking) {
        this.isValidationEnabledWhileWalking = isValidationEnabledWhileWalking;
    }

}
