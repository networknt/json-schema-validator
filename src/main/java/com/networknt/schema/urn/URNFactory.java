package com.networknt.schema.urn;

import java.net.URI;

public interface URNFactory
{
  /**
   * @param urn Some urn string.
   * @return The converted {@link URI}.
   * @throws IllegalArgumentException if there was a problem creating the {@link URI} with the given data.
   */
  URI create(String urn);
}
