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

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * A URIFactory that uses {@link #URL(String)} and {@link #URL(URL, String) for creating {@link URI}s.
 */
public final class URLFactory implements URIFactory {
  // These supported schemes are defined in {@link #URL(String, String, int, String)}.
  public static final Set<String> SUPPORTED_SCHEMES = Collections.unmodifiableSet(new HashSet<>(
    Arrays.asList("http", "https", "ftp", "file", "jar")));
  
  /**
   * {@inheritDoc}
   */
  @Override
  public URI create(final String uri)
  {
    try {
      return new URL(uri).toURI();
    } catch (MalformedURLException | URISyntaxException e) {
      throw new IllegalArgumentException("Unable to create URI.", e);
    }
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public URI create(final URI baseURI, final String segment)
  {
    try {
      return new URL(baseURI.toURL(), segment).toURI();
    } catch (MalformedURLException | URISyntaxException e) {
      throw new IllegalArgumentException("Unable to create URI.", e);
    }
  }
}
