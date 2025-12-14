package com.networknt.schema;

import java.io.InputStream;
import java.util.List;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import com.fasterxml.jackson.annotation.JsonInclude;
import tools.jackson.databind.DeserializationFeature;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.SerializationFeature;
import tools.jackson.databind.node.BinaryNode;

/**
 *
 * created at 07.02.2023
 *
 * @author k-oliver
 * @since 1.0.77
 */
class Issue650Test {

    /**
     * Test using a Java model with a byte[] property which jackson converts to a BASE64 encoded string automatically. Then convert into
     * a jackson tree. The resulting node is of type {@link BinaryNode}. This test checks if validation handles the {@link BinaryNode} as string
     * when validating.
     *
     * @throws Exception
     * @since 1.0.77
     */
    @Test
    void testBinaryNode() throws Exception {
        final ObjectMapper mapper = new ObjectMapper().rebuild()
                .changeDefaultPropertyInclusion(incl -> incl.withValueInclusion(JsonInclude.Include.NON_EMPTY))
                .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
                .configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false)
                .build();

        // schema with data property of type string:
        InputStream schemaInputStream = getClass().getResourceAsStream("/draft7/issue650.json");
        Schema schema = SchemaRegistry.withDefaultDialect(SpecificationVersion.DRAFT_7).getSchema(schemaInputStream);

        // create model first:
        Issue650Test.Model model = new Issue650Test.Model();
        model.setData("content".getBytes("UTF-8"));
        // now convert to tree. The resulting type of the data property is BinaryNode now:
        JsonNode node = mapper.valueToTree(model);

        // validate:
        List<Error> errors = schema.validate(node);

        // check result:
        Assertions.assertTrue(errors.isEmpty());
    }

    /**
     * created at 07.02.2023
     *
     * @author Oliver Kelling
     * @since 1.0.77
     */
    private static class Model {
        private byte[] data;


        /**
         * @return the data
         * @since 1.0.77
         */
        @SuppressWarnings("unused") // called by jackson
        public byte[] getData() {
            return this.data;
        }


        /**
         * @param data the data to set
         * @since 1.0.77
         */
        public void setData(byte[] data) {
            this.data = data;
        }

    }
}
