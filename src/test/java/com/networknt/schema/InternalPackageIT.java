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

import static org.junit.jupiter.api.Assertions.*;
import java.io.File;
import java.lang.reflect.Method;
import java.nio.file.Path;
import java.util.Optional;
import java.util.Set;
import java.util.jar.JarFile;
import org.junit.jupiter.api.Test;

/** Runs after packaging in Maven verify, including the normal CI verify command. */
class InternalPackageIT {
    @Test
    void packagedArtifactKeepsMetadataInternal() throws Exception {
        File artifact = new File(System.getProperty("countArtifact"));
        assertTrue(artifact.isFile(), "packaged jar is missing");
        try (JarFile jar = new JarFile(artifact)) {
            assertNotNull(jar.getJarEntry("com/networknt/schema/internal/CountTokens.class"));
            assertNull(jar.getJarEntry("com/networknt/schema/serialization/CountTokenNode.class"));
            String exports = jar.getManifest().getMainAttributes().getValue("Export-Package");
            if (exports != null) {
                assertFalse(exports.contains("com.networknt.schema.internal"), "internal OSGi export");
            }
        }
        Class<?> finderType;
        try {
            finderType = Class.forName("java.lang.module.ModuleFinder");
        } catch (ClassNotFoundException java8) {
            return; // Java 8 has no JPMS; the jar/OSGi assertions above still apply.
        }
        Object finder = finderType.getMethod("of", Path[].class).invoke(null, (Object) new Path[] { artifact.toPath() });
        Optional<?> reference = (Optional<?>) finderType.getMethod("find", String.class).invoke(finder, "com.networknt.schema");
        assertTrue(reference.isPresent());
        Object descriptor = Class.forName("java.lang.module.ModuleReference").getMethod("descriptor").invoke(reference.get());
        Class<?> descriptorType = Class.forName("java.lang.module.ModuleDescriptor");
        assertFalse((Boolean) descriptorType.getMethod("isAutomatic").invoke(descriptor), "explicit module descriptor missing");
        Set<?> packages = (Set<?>) descriptorType.getMethod("packages").invoke(descriptor);
        assertTrue(packages.contains("com.networknt.schema.internal"), "internal package has no module owner");
        Method source = Class.forName("java.lang.module.ModuleDescriptor$Exports").getMethod("source");
        for (Object exported : (Set<?>) descriptorType.getMethod("exports").invoke(descriptor)) {
            assertFalse("com.networknt.schema.internal".equals(source.invoke(exported)), "internal JPMS export");
        }
    }
}
