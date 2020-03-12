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

import java.util.HashMap;
import java.util.Map;

public class ThreadInfo {

    private static ThreadLocal<Map<String, Object>> threadLocal = new ThreadLocal<Map<String, Object>>() {
        protected java.util.Map<String, Object> initialValue() {
            return new HashMap<String, Object>();
        }

        ;
    };

    public static Object get(String key) {
        return threadLocal.get().get(key);
    }

    public static void set(String key, Object value) {
        Map<String, Object> threadLocalMap = threadLocal.get();
        threadLocalMap.put(key, value);
    }

    public static void remove(String key) {
        Map<String, Object> threadLocalMap = threadLocal.get();
        threadLocalMap.remove(key);
    }

}
