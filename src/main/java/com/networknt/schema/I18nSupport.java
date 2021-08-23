package com.networknt.schema;

import java.util.ResourceBundle;

/**
 * Created by leaves chen leaves615@gmail.com on 2021/8/23.
 *
 * @Author leaves chen leaves615@gmail.com
 */
public class I18nSupport {
    private static final String BASE_NAME = "jsv-messages";
    private static ResourceBundle bundle = ResourceBundle.getBundle(BASE_NAME);

    public static String getString(String key) {
        return bundle.getString(key);
    }
}
