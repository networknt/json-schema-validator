package com.networknt.schema;

import org.junit.jupiter.api.Test;

import com.networknt.schema.keyword.NotAllowedValidator;
import com.networknt.schema.keyword.Keywords;


/**
 * This class test {@link NotAllowedValidator},
 * above-mentioned validator check that schema defined json should not be there in JSON object
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
        assertValidatorType("notAllowedJson.json", Keywords.NOT_ALLOWED);
    }
}
