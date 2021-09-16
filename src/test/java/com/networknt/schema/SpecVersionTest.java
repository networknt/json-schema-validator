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

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.EnumSet;
import java.util.Set;

public class SpecVersionTest {
    @Test
    public void testGetVersionValue() {
        SpecVersion ds = new SpecVersion();
        Set versionFlags = EnumSet.of(
                SpecVersion.VersionFlag.V4,
                SpecVersion.VersionFlag.V201909);
        Assertions.assertEquals(ds.getVersionValue(versionFlags), 9); // 0001|1000
    }

    @Test
    public void testGetVersionFlags() {
        SpecVersion ds = new SpecVersion();

        long numericVersionCode = SpecVersion.VersionFlag.V201909.getVersionFlagValue()
                | SpecVersion.VersionFlag.V6.getVersionFlagValue()
                | SpecVersion.VersionFlag.V7.getVersionFlagValue();  // 14

        Set versionFlags = ds.getVersionFlags(numericVersionCode);

        assert !versionFlags.contains(SpecVersion.VersionFlag.V4);
        assert versionFlags.contains(SpecVersion.VersionFlag.V6);
        assert versionFlags.contains(SpecVersion.VersionFlag.V7);
        assert versionFlags.contains(SpecVersion.VersionFlag.V201909);

    }

    @Test
    public void testAllVersionValue() {
        long numericVersionCode =
                SpecVersion.VersionFlag.V201909.getVersionFlagValue()
                        | SpecVersion.VersionFlag.V4.getVersionFlagValue()
                        | SpecVersion.VersionFlag.V6.getVersionFlagValue()
                        | SpecVersion.VersionFlag.V7.getVersionFlagValue();  // 15
        Assertions.assertEquals(numericVersionCode, 15);

    }
}
