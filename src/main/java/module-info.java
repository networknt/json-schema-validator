module com.networknt.schema {
    requires org.apache.commons.lang3;
    requires org.jruby.jcodings;
    requires org.jruby.joni;
    requires org.slf4j;
    requires transitive com.fasterxml.jackson.databind;

    opens com.networknt.schema to com.fasterxml.jackson.databind;

    exports com.networknt.schema;
    exports com.networknt.schema.format;
    exports com.networknt.schema.uri;
    exports com.networknt.schema.urn;
    exports com.networknt.schema.walk;
}
