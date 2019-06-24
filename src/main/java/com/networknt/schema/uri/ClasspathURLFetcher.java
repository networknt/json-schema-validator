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
import java.net.URL;
import java.util.Collections;
import java.util.Set;

/**
 * A URIfetcher that uses {@link URL#openStream()} for fetching and assumes given {@link URI}s
 * are actualy {@link URL}s.
 */
public final class ClasspathURLFetcher implements URIFetcher {
  // This fetcher handles the {@link URL}s created with the {@link ClasspathURIFactory}.
  public static final Set<String> SUPPORTED_SCHEMES = Collections.unmodifiableSet(ClasspathURLFactory.SUPPORTED_SCHEMES);
  
  /**
   * {@inheritDoc}
   */
  @Override
  public InputStream fetch(final URI uri) throws IOException {
    return ClasspathURLFactory.convert(uri).openStream();
  }
}
