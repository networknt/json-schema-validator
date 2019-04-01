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

package com.networknt.schema.url;

import java.net.MalformedURLException;
import java.net.URL;

/**
 * A factory for creating {@link URL}'s. This factory creates {@link URL}'s that in additional to the standard {@link URL}'s
 * capability of loading resources using http, https, file, etc. also makes it possible to load resources from
 * the applications classpath. To load a resource from classpath, the url must be prefixed either with <i>classpath:</i>
 * or <i>resource:</i>
 *
 * To ensure that we support classpath resources, this class should be used instead of <code>new URL(pURL)</code>
 *
 * @author <a href="mailto:kenneth.waldenstroem@gmail.com">Kenneth Waldenstrom</a>
 */
public class URLFactory {
  private static final ClasspathURLStreamHandler sClasspathURLStreamHandler = new ClasspathURLStreamHandler();

  /**
   * Creates an {@link URL} based on the provided string
   * @param pURL the url
   * @return a {@link URL}
   * @throws MalformedURLException if the url is not a proper URL
   */
  public static URL toURL(final String pURL) throws MalformedURLException {
    return new URL(null, pURL, sClasspathURLStreamHandler.supports(pURL) ? sClasspathURLStreamHandler : null);
  }
}