package com.networknt.schema;

import java.util.Arrays;
import java.util.EnumSet;

/**
 * SpecificationVersionRange.
 */
public enum SpecificationVersionRange {
    None(new SpecificationVersion[] { }),
    AllVersions(new SpecificationVersion[] { SpecificationVersion.DRAFT_4, SpecificationVersion.DRAFT_6, SpecificationVersion.DRAFT_7, SpecificationVersion.DRAFT_2019_09, SpecificationVersion.DRAFT_2020_12 }),
    MinV6(new SpecificationVersion[] { SpecificationVersion.DRAFT_6, SpecificationVersion.DRAFT_7, SpecificationVersion.DRAFT_2019_09, SpecificationVersion.DRAFT_2020_12 }),
    MinV6MaxV7(new SpecificationVersion[] { SpecificationVersion.DRAFT_6, SpecificationVersion.DRAFT_7 }),
    MinV7(new SpecificationVersion[] { SpecificationVersion.DRAFT_7, SpecificationVersion.DRAFT_2019_09, SpecificationVersion.DRAFT_2020_12 }),
    MaxV7(new SpecificationVersion[] { SpecificationVersion.DRAFT_4, SpecificationVersion.DRAFT_6, SpecificationVersion.DRAFT_7 }),
    MaxV201909(new SpecificationVersion[] { SpecificationVersion.DRAFT_4, SpecificationVersion.DRAFT_6, SpecificationVersion.DRAFT_7, SpecificationVersion.DRAFT_2019_09 }),
    MinV201909(new SpecificationVersion[] { SpecificationVersion.DRAFT_2019_09, SpecificationVersion.DRAFT_2020_12 }),
    MinV202012(new SpecificationVersion[] { SpecificationVersion.DRAFT_2020_12 }),
    V201909(new SpecificationVersion[] { SpecificationVersion.DRAFT_2019_09 }),
    V7(new SpecificationVersion[] { SpecificationVersion.DRAFT_7 });

    private final EnumSet<SpecificationVersion> versions;

    SpecificationVersionRange(SpecificationVersion[] versionFlags) {
        this.versions = EnumSet.noneOf(SpecificationVersion.class);
	      this.versions.addAll(Arrays.asList(versionFlags));
    }

    public EnumSet<SpecificationVersion> getVersions() {
        return this.versions;
    }
}