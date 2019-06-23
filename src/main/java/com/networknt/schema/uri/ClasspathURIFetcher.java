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
import java.net.URI;
import java.util.Arrays;
import java.util.List;

/**
 * An {@link URIFetcher} capable of loading resources from the classpath.
 *
 * @author <a href="mailto:kenneth.waldenstroem@gmail.com">Kenneth Waldenstrom</a>
 */
public class ClasspathURIFetcher implements URIFetcher {
  public static final List<String> SUPPORTED_SCHEMES = Arrays.asList("classpath", "resource");

  /**
   * {@inheritDoc}
   */
  @Override
  public InputStream fetch(final URI uri) throws IOException {
    String path = uri.getPath();
    if (path.startsWith("/")) {
      path = path.substring(1);
    }
    
    // It's possible that the classpath URI is in relation to a p	articular class' class loader.
    Class<?> classHost;
    final String className = uri.getAuthority();
    try {
      if (className != null && className.length() > 0) {
        classHost = Class.forName(className);
      }
      else {
        classHost = null;
      }
    }
    catch (ClassNotFoundException e) {
      classHost = null;
      throw new IOException("Class not found: " + e.toString());
    }
    
    InputStream stream;
    if (classHost != null) {
      stream = classHost.getClassLoader().getResourceAsStream(path);
    }
    else {
      stream = Thread.currentThread().getContextClassLoader().getResourceAsStream(path);
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
