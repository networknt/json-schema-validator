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

import java.net.URI;

/**
 * The URIFactory interface defines how {@link URI}s are able to be combined and created.
 */
public interface URIFactory {
  /**
   * @param uri Some uri string.
   * @return The converted {@link URI}.
   * @throws IllegalArgumentException if there was a problem creating the {@link URI} with the given data.
   */
  URI create(String uri);
  
  /**
   * @param baseURI The base {@link URI}.
   * @param segment The segment to add to the base {@link URI}. 
   * @return The combined {@link URI}.
   * @throws IllegalArgumentException if there was a problem creating the {@link URI} with the given data.
   */
  URI create(URI baseURI, String segment);
}
