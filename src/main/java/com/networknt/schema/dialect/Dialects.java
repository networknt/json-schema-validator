/*
 * Copyright (c) 2025 the original author or authors.
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

package com.networknt.schema.dialect;

/**
 * The dialects.
 */
public class Dialects {
    /**
     * Draft 4.
     * 
     * @return the Draft 4 dialect
     */
    public static Dialect getDraft4() {
        return Draft4.getInstance();
    }

    /**
     * Draft 6.
     * 
     * @return the Draft 6 dialect
     */
    public static Dialect getDraft6() {
        return Draft6.getInstance();
    }

    /**
     * Draft 7.
     * 
     * @return the Draft 7 dialect
     */
    public static Dialect getDraft7() {
        return Draft7.getInstance();
    }

    /**
     * Draft 2019-09.
     * 
     * @return the Draft 2019-09 dialect
     */
    public static Dialect getDraft201909() {
        return Draft201909.getInstance();
    }

    /**
     * Draft 2020-12.
     * 
     * @return the Draft 2020-12 dialect
     */
    public static Dialect getDraft202012() {
        return Draft202012.getInstance();
    }

    /**
     * OpenAPI 3.0.
     * 
     * @return the OpenAPI 3.0 dialect
     */
    public static Dialect getOpenApi30() {
        return OpenApi30.getInstance();
    }

    /**
     * OpenAPI 3.1.
     * 
     * @return the OpenAPI 3.1 dialect
     */
    public static Dialect getOpenApi31() {
        return OpenApi31.getInstance();
    }
}
