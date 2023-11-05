package com.networknt.schema;

import org.junit.jupiter.api.Test;


/**
 * This class test {@link NotAllowedValidator}, above mentioned validator check that
 */
class NotAllowedValidatorTest extends AbstractJsonSchemaTest {

    @Override
    protected String getDataTestFolder() {
        return "/data/notAllowedValidation/";
    }


    /**
     * This test case checks that NotAllowedValidator is working with latest code and able to identify field.
     */
    @Test
    void testNotAllowedValidatorWorks() {
        assertValidatorType("notAllowedJson.json", ValidatorTypeCode.NOT_ALLOWED);
    }
}
