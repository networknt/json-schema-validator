package com.networknt.schema;

import org.junit.Assert;
import org.junit.Test;

import java.util.EnumSet;
import java.util.Set;

public class SpecVersionTest {
    @Test
    public void testGetVersionValue() {
        SpecVersion ds = new SpecVersion();
        Set versionFlags = EnumSet.of(
                SpecVersion.VersionFlag.V4,
                SpecVersion.VersionFlag.V201909);
        Assert.assertEquals(ds.getVersionValue(versionFlags), 9); // 0001|1000
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
        Assert.assertEquals(numericVersionCode, 15);

    }
}
