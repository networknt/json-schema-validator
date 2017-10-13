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