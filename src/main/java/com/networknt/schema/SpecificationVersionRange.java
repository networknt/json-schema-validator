package com.networknt.schema;

import java.util.Arrays;
import java.util.EnumSet;

/**
 * SpecificationVersionRange.
 */
public enum SpecificationVersionRange {
    NONE(new SpecificationVersion[] { }),
    ALL_VERSIONS(new SpecificationVersion[] { SpecificationVersion.DRAFT_4, SpecificationVersion.DRAFT_6, SpecificationVersion.DRAFT_7, SpecificationVersion.DRAFT_2019_09, SpecificationVersion.DRAFT_2020_12 }),
    MIN_DRAFT_6(new SpecificationVersion[] { SpecificationVersion.DRAFT_6, SpecificationVersion.DRAFT_7, SpecificationVersion.DRAFT_2019_09, SpecificationVersion.DRAFT_2020_12 }),
    DRAFT_6_TO_DRAFT_7(new SpecificationVersion[] { SpecificationVersion.DRAFT_6, SpecificationVersion.DRAFT_7 }),
    MIN_DRAFT_7(new SpecificationVersion[] { SpecificationVersion.DRAFT_7, SpecificationVersion.DRAFT_2019_09, SpecificationVersion.DRAFT_2020_12 }),
    MAX_DRAFT_7(new SpecificationVersion[] { SpecificationVersion.DRAFT_4, SpecificationVersion.DRAFT_6, SpecificationVersion.DRAFT_7 }),
    MAX_DRAFT_2019_09(new SpecificationVersion[] { SpecificationVersion.DRAFT_4, SpecificationVersion.DRAFT_6, SpecificationVersion.DRAFT_7, SpecificationVersion.DRAFT_2019_09 }),
    MIN_DRAFT_2019_09(new SpecificationVersion[] { SpecificationVersion.DRAFT_2019_09, SpecificationVersion.DRAFT_2020_12 }),
    MIN_DRAFT_2020_12(new SpecificationVersion[] { SpecificationVersion.DRAFT_2020_12 }),
    DRAFT_2019_09(new SpecificationVersion[] { SpecificationVersion.DRAFT_2019_09 }),
    DRAFT_7(new SpecificationVersion[] { SpecificationVersion.DRAFT_7 });

    private final EnumSet<SpecificationVersion> versions;

    SpecificationVersionRange(SpecificationVersion[] versionFlags) {
        this.versions = EnumSet.noneOf(SpecificationVersion.class);
	      this.versions.addAll(Arrays.asList(versionFlags));
    }

    public EnumSet<SpecificationVersion> getVersions() {
        return this.versions;
    }
}