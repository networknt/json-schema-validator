package com.networknt.schema;

import java.util.ArrayList;
import java.util.List;

public abstract class JsonSchemaVersion {
    protected static String URI;
    protected static String ID;
    public static final List<Format> BUILTIN_FORMATS = new ArrayList<Format>(JsonMetaSchema.COMMON_BUILTIN_FORMATS);
    static {
        // add version specific formats here.
        //BUILTIN_FORMATS.add(pattern("phone", "^\\+(?:[0-9] ?){6,14}[0-9]$"));
    }
    public abstract JsonMetaSchema getInstance();
}
