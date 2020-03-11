package com.networknt.schema;

import java.util.EnumSet;
import java.util.Set;

public class SpecVersion {

    public enum VersionFlag {

        V4(1 << 0),
        V6(1 << 1),
        V7(1 << 2),
        V201909(1 << 3);


        private final long versionFlagValue;

        VersionFlag(long versionFlagValue) {
            this.versionFlagValue = versionFlagValue;
        }

        public long getVersionFlagValue() {
            return versionFlagValue;
        }
    }


    /**
     * Translates a numeric version code into a Set of VersionFlag enums
     *
     * @param versionValue long
     * @return EnumSet representing a version
     */
    public EnumSet<VersionFlag> getVersionFlags(long versionValue) {
        EnumSet versionFlags = EnumSet.noneOf(VersionFlag.class);
        for (VersionFlag flag : VersionFlag.values()) {
            long flagValue = flag.versionFlagValue;
            if ((flagValue & versionValue) == flagValue) {
                versionFlags.add(flag);
            }
        }
        return versionFlags;
    }


    /**
     * Translates a set of VersionFlag enums into a long version code
     *
     * @param flags set of versionFlags
     * @return numeric representation of the spec version
     */
    public long getVersionValue(Set<VersionFlag> flags) {
        long value = 0;
        for (VersionFlag flag : flags) {
            value = value | flag.versionFlagValue;
        }
        return value;
    }
}
