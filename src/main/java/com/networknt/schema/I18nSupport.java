package com.networknt.schema;

import java.util.Locale;
import java.util.ResourceBundle;

/**
 * Created by leaves chen leaves615@gmail.com on 2021/8/23.
 *
 * @author leaves chen leaves615@gmail.com
 */
@Deprecated
public class I18nSupport {

    public static final String DEFAULT_BUNDLE_BASE_NAME = "jsv-messages";
    public static final Locale DEFAULT_LOCALE = Locale.getDefault();
    public static final ResourceBundle DEFAULT_RESOURCE_BUNDLE = ResourceBundle.getBundle(DEFAULT_BUNDLE_BASE_NAME, DEFAULT_LOCALE);

}
