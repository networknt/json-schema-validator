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

package com.networknt.schema;

import java.net.MalformedURLException;
import java.net.URL;

import org.junit.Test;

import com.fasterxml.jackson.databind.JsonNode;
import com.networknt.schema.url.URLFactory;

/**
 * Created by stevehu on 2016-12-20.
 */
public class SelfRefTest extends BaseJsonSchemaValidatorTest {
    @Test
    public void urlBehavior() throws MalformedURLException
    {
      final URL rootUrl = new URL("http://localhost:1234");
      final URL rootIdUrl = new URL("http://localhost:1234/asdfasdf");
      final URL relUrl = new URL(rootUrl, "foo/");
      final URL domainUrl = new URL(relUrl, "/asdf");
      
      final URL fooUrl = new URL(relUrl, "foo.json");
      final URL barUrl = new URL(fooUrl, "bar.json");
      final URL subUrl = new URL(fooUrl, "../bar");

      System.out.println(rootUrl);
      System.out.println(rootIdUrl);
      System.out.println(relUrl);
      System.out.println(domainUrl);
      System.out.println(fooUrl);
      System.out.println(barUrl);
      System.out.println(subUrl);
      
      System.out.println(URLFactory.toURL("classpath:/tests/schema.json"));
      System.out.println(URLFactory.toURL("resource:/tests/schema.json"));
    }
    @Test
    public void urlBehavior2() throws MalformedURLException
    {
      System.out.println(isAbsolute("http://localhost:1234"));
      System.out.println(isAbsolute("/asdfasdf"));
      System.out.println(isAbsolute("asdfasdf.json"));
    }
    
    private static boolean isAbsolute(String url)
    {
      try {
        new URL(url);
        return true;
      } catch (MalformedURLException e) {
        return false;
      }
    }
}
