package com.networknt.schema.url;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLStreamHandler;

/**
 * An {@link URLStreamHandler} capable of loading resources from the classpath.
 *
 * @author <a href="mailto:kenneth.waldenstroem@gmail.com">Kenneth Waldenstrom</a>
 */
class ClasspathURLStreamHandler extends URLStreamHandler {
  private final static String CLASSPATH_PREFIX = "classpath:";
  private final static String RESOURCE_PREFIX = "resource:";

  boolean supports(final String pURL) {
    return pURL.startsWith(CLASSPATH_PREFIX) || pURL.startsWith(RESOURCE_PREFIX);
  }

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
      }
      catch (ClassNotFoundException e) {
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

      InputStream stream;
      if (mHost != null) {
        stream = mHost.getClassLoader().getResourceAsStream(path);
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


}
