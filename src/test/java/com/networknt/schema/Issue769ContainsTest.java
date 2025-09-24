package com.networknt.schema;

import org.junit.jupiter.api.Test;

import com.networknt.schema.keyword.ContainsValidator;
import com.networknt.schema.keyword.KeywordType;

/**
 * <p>Test class for issue <a href="https://github.com/networknt/json-schema-validator/issues/769">#769</a></p>
 * <p>This test class asserts that correct messages are returned for contains, minContains et maxContains keywords</p>
 * <p>Tested class: {@link ContainsValidator}</p>
 *
 * @author vwuilbea
 */
class Issue769ContainsTest extends AbstractJsonSchemaTest {

    @Override
    protected String getDataTestFolder() {
        return "/data/contains/issue769/";
    }

    @Test
    void shouldReturnMinContainsKeyword() {
        assertValidatorType("min-contains.json", KeywordType.MIN_CONTAINS);
    }

    @Test
    void shouldReturnContainsKeywordForMinContainsV7() {
        assertValidatorType("min-contains-v7.json", KeywordType.CONTAINS);
    }

    @Test
    void shouldReturnMaxContainsKeyword() {
        assertValidatorType("max-contains.json", KeywordType.MAX_CONTAINS);
    }

    @Test
    void shouldReturnContainsKeywordForMaxContainsV7() {
        assertValidatorType("max-contains-v7.json", KeywordType.CONTAINS);
    }

}
