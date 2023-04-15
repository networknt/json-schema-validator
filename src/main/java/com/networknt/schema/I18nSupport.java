package com.networknt.schema;

import java.util.MissingResourceException;
import java.util.ResourceBundle;

/**
 * Created by leaves chen leaves615@gmail.com on 2021/8/23.
 *
 * @author leaves chen leaves615@gmail.com
 */
public class I18nSupport {

    private static final String BASE_NAME = "jsv-messages";
    private static final ResourceBundle bundle;

    static {
        ResourceBundle tmpBundle = null;
        try {
            tmpBundle = ResourceBundle.getBundle(BASE_NAME);
        } catch (MissingResourceException mre) {
            // Need to avoid by all means that we fail loading ValidatorTypeCode with a
            // "java.lang.NoClassDefFoundError: Could not initialize class com.networknt.schema.ValidatorTypeCode"
            // due to the fact that a ResourceBundle is incomplete
            mre.printStackTrace();
        }
        bundle = tmpBundle;
    }

    public static String getString(String key) {
        String retval = null;
        try {
            retval = bundle.getString(key);
        } catch (MissingResourceException mre) {
            // Need to avoid by all means that we fail loading ValidatorTypeCode with a
            // "java.lang.NoClassDefFoundError: Could not initialize class com.networknt.schema.ValidatorTypeCode"
            // due to the fact that a ResourceBundle is incomplete
            mre.printStackTrace();
        }
        return retval;
    }
    
}
