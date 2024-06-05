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
    /**
     * Flag to check if walking is enabled.
     */
    private boolean isWalkEnabled = false;

    /**
     * Flag to check if validation is enabled while walking.
     */
    private boolean isValidationEnabled = false;

    /**
     * Constructor for validation state.
     */
    public ValidatorState() {
    }

    /**
     * Constructor for validation state.
     * 
     * @param walkEnabled       whether walk is enabled
     * @param validationEnabled whether validation is enabled
     */
    public ValidatorState(boolean walkEnabled, boolean validationEnabled) {
        this.isWalkEnabled = walkEnabled;
        this.isValidationEnabled = validationEnabled;
    }

    public boolean isWalkEnabled() {
        return isWalkEnabled;
    }

    public void setWalkEnabled(boolean isWalkEnabled) {
        this.isWalkEnabled = isWalkEnabled;
    }

    public boolean isValidationEnabled() {
        return isValidationEnabled;
    }

    public void setValidationEnabled(boolean isValidationEnabled) {
        this.isValidationEnabled = isValidationEnabled;
    }

}
