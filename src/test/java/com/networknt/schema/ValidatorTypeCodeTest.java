package com.networknt.schema;

import static org.junit.Assert.assertEquals;
import org.junit.Test;

public class ValidatorTypeCodeTest {

    @Test
    public void testFromValueString() {
        assertEquals(ValidatorTypeCode.ADDITIONAL_PROPERTIES, ValidatorTypeCode.fromValue("additionalProperties"));
    }

    @Test
    public void testFromValueAll() {
        for (ValidatorTypeCode code : ValidatorTypeCode.values()) {
            assertEquals(code, ValidatorTypeCode.fromValue(code.getValue()));
        }
    }

    @Test(expected = IllegalArgumentException.class)
    public void testFromValueMissing() {
        assertEquals(ValidatorTypeCode.ADDITIONAL_PROPERTIES, ValidatorTypeCode.fromValue("missing"));
    }

}
