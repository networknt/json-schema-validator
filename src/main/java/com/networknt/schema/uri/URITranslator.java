package com.networknt.schema.uri;

import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

@FunctionalInterface
public interface URITranslator {
    static final URITranslator NOOP = original -> original;

    /**
     * Translates one URI into another.
     * @param original the URI to translate
     * @return the translated URI or the original URI if it did not match
     *         the conditions triggering translation
     */
    URI translate(URI original);

    /**
     * Creates a simple mapping from one URI to another.
     * @param source the URI to match
     * @param target the URI to return when matched
     * @return a new URITranslator
     */
    static URITranslator map(String source, String target) {
        return map(URI.create(source), URI.create(target));
    }

    /**
     * Creates a simple mapping from one URI to another.
     * @param source the URI to match
     * @param target the URI to return when matched
     * @return a new URITranslator
     */
    static URITranslator map(URI source, URI target) {
        return original -> Objects.equals(source, original) ? target : original;
    }

    /**
     * Creates a map-based mapping from one URI to another.
     * @param uriMappings the mappings to build
     * @return a new URITranslator
     */
    static URITranslator map(Map<String, String> uriMappings) {
        return new MappingURITranslator(uriMappings);
    }

    /**
     * Creates a CompositeURITranslator.
     * @param uriTranslators the translators to combine
     * @return a new CompositeURITranslator
     */
    static CompositeURITranslator combine(URITranslator... uriTranslators) {
        return new CompositeURITranslator(uriTranslators);
    }

    /**
     * Creates a mapping from one URI to another by replacing the beginning of the URI.
     * <p>
     * For example, replace http with https.
     * 
     * @param source the search string
     * @param target the replacement string
     * @return a new URITranslator
     */
    static URITranslator prefix(String source, String target) {
        return new PrefixReplacer(source, target);
    }

    /**
     * Creates a CompositeURITranslator.
     * @param uriTranslators the translators to combine
     * @return a new CompositeURITranslator
     */
    static CompositeURITranslator combine(Collection<? extends URITranslator> uriTranslators) {
        return new CompositeURITranslator(uriTranslators);
    }

    class CompositeURITranslator extends ArrayList<URITranslator> implements URITranslator {
        private static final long serialVersionUID = 1L;

        public CompositeURITranslator() {
            super();
        }

        public CompositeURITranslator(URITranslator...translators) {
            this(Arrays.asList(translators));
        }

        public CompositeURITranslator(Collection<? extends URITranslator> c) {
            super(c);
        }

        @Override
        public URI translate(URI original) {
            URI result = original;
            for (URITranslator translator: this) {
                result = translator.translate(result);
            }
            return result;
        }

        public CompositeURITranslator with(URITranslator translator) {
            if (null != translator) {
                return new CompositeURITranslator(this, translator);
            }
            return this;
        }
    }

    /**
     * Provides support for legacy map-based translations
     */
    class MappingURITranslator implements URITranslator {
        private final Map<URI, URI> mappings;

        public MappingURITranslator(Map<String, String> uriMappings) {
            this.mappings = new HashMap<>();
            if (null != uriMappings) {
                uriMappings.forEach((k, v) -> this.mappings.put(URI.create(k), URI.create(v)));
            }
        }

        @Override
        public URI translate(URI original) {
            return this.mappings.getOrDefault(original, original);
        }

    }

    /**
     * Replaces the beginning of a URI
     */
    class PrefixReplacer implements URITranslator {
        private final String src;
        private final String tgt;

        public PrefixReplacer(String src, String tgt) {
            this.src = src;
            this.tgt = tgt;
        }

        @Override
        public URI translate(URI original) {
            if (null != original) {
                String o = original.toString();
                if (o.startsWith(src)) {
                    o = tgt + o.substring(src.length());
                    return URI.create(o);
                }
            }

            return original;
        }

    }
}
