/*
 * Copyright (c) 2016 Network New Technologies Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.networknt.schema.uri;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLStreamHandler;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * An {@link URLStreamHandler} capable of loading resources from the classpath.
 *
 * @author <a href="mailto:kenneth.waldenstroem@gmail.com">Kenneth Waldenstrom</a>
 */
class ClasspathURLStreamHandler extends URLStreamHandler {
    public static final Set<String> SUPPORTED_SCHEMES = Collections.unmodifiableSet(new HashSet<String>(
            Arrays.asList("classpath", "resource")));

    @Override
    protected URLConnection openConnection(final URL pURL) throws IOException {
        return new ClassPathURLConnection(pURL);
    }

    class ClassPathURLConnection extends URLConnection {

        private Class<?> mHost = null;

        protected ClassPathURLConnection(URL pURL) {
            super(pURL);
        }

        @Override
        public final void connect() throws IOException {
            String className = url.getHost();
            try {
                if (className != null && className.length() > 0) {
                    mHost = Class.forName(className);
                }
                connected = true;
            } catch (ClassNotFoundException e) {
                throw new IOException("Class not found: " + e.toString());
            }
        }

        @Override
        public final InputStream getInputStream() throws IOException {
            if (!connected) {
                connect();
            }
            return getResourceAsStream(url);
        }

        private InputStream getResourceAsStream(URL pURL) throws IOException {
            String path = pURL.getPath();

            if (path.startsWith("/")) {
                path = path.substring(1);
            }

            InputStream stream = null;
            if (mHost != null) {
                stream = mHost.getClassLoader().getResourceAsStream(path);
            } else {
                ClassLoader contextClassLoader = Thread.currentThread().getContextClassLoader();
                if (contextClassLoader != null) {
                    stream = contextClassLoader.getResourceAsStream(path);
                }
                if (stream == null) {
                    stream = getClass().getClassLoader().getResourceAsStream(path);
                }
                if (stream == null) {
                    stream = ClassLoader.getSystemResourceAsStream(path);
                }
            }
            if (stream == null) {
                throw new IOException("Resource " + path + " not found in classpath.");
            }
            return stream;
        }
    }


}
