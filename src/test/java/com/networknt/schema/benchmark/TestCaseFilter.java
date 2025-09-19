package com.networknt.schema.benchmark;

import java.nio.file.Path;
import java.util.function.Predicate;

public class TestCaseFilter {
    public static Predicate<? super Path> requiredType() {
        return optionalType().negate();
    }

    public static Predicate<? super Path> optionalType() {
        return path -> {
            int count = path.getNameCount();
            for (int x = count - 2; x > 0; x--) {
                if (path.getName(x).toString().equals("optional")) {
                    return true;
                }
            }
            return false;
        };
    }
}
