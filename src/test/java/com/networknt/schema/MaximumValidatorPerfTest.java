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

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@Disabled
class MaximumValidatorPerfTest {
    MaximumValidatorTest test = new MaximumValidatorTest();

    @Test
    void testTime() throws InvocationTargetException, IllegalAccessException {
        String[] testMethodsToBeExecuted = {"testMaximumDoubleValue"};
        List<Method> testMethods = getTestMethods(testMethodsToBeExecuted);
        long start = System.currentTimeMillis();
        executeTests(testMethods, 200000);
        long end = System.currentTimeMillis();
        System.out.println("time to execute all tests using:" + (end - start) + "ms");
    }

    void executeTests(List<Method> methods, int executeTimes) throws InvocationTargetException, IllegalAccessException {

        for (int i = 0; i < executeTimes; i++) {
            for (Method testMethod : methods) {
                testMethod.invoke(test);
            }
        }

    }

    List<Method> getTestMethods(String[] methodNames) {
        Method[] methods = test.getClass().getMethods();
        List<Method> testMethods = new ArrayList<Method>();
        if (methodNames.length > 0) {
            for (String name : methodNames) {
                Collection<Method> listOfMethodNames = new ArrayList<Method>();
                for (Method testMethod : methods) {
                    if (testMethod.getName().equals(name)) {
                        listOfMethodNames.add(testMethod);
                    }
                }
                testMethods.addAll(listOfMethodNames);
            }
            return testMethods;
        }
        for (Method m : methods) {
            Annotation[] annotations = m.getDeclaredAnnotations();
            boolean isTestMethod = false;
            for (Annotation annotation : annotations) {
                if (annotation.annotationType() == Test.class) {
                    isTestMethod = true;
                }
            }
            if (isTestMethod) {
                //filter out incompatible test cases.
                if (!m.getName().equals("doubleValueCoarsing") && !m.getName().equals("negativeDoubleOverflowTest"))
                    testMethods.add(m);
            }
        }
        return testMethods;
    }
}
