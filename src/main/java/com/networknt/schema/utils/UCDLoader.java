package com.networknt.schema.utils;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.util.BitSet;
import java.util.function.Function;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.networknt.schema.format.IdnHostnameFormat;

public class UCDLoader {
    private static final Logger logger = LoggerFactory.getLogger(UCDLoader.class);

    static void loadMapping(String filename, Function<String, BitSet> selector) {
        try (
            InputStream is = IdnHostnameFormat.class.getResourceAsStream(filename);
            LineNumberReader rd = new LineNumberReader(new InputStreamReader(is))
        ) {
            rd.lines().forEach(line -> {
                if (!line.isEmpty() && '#' != line.charAt(0)) {
                    String[] s = line.split("\\s*[;#]\\s*", 3);

                    BitSet bs = selector.apply(s[1]);
                    if (null != bs) {
                        String[] n = s[0].split("\\.\\.");
                        switch (n.length) {
                            case 2: bs.set(Integer.parseUnsignedInt(n[0], 16), 1 + Integer.parseUnsignedInt(n[1], 16)); break;
                            case 1: bs.set(Integer.parseUnsignedInt(n[0], 16)); break;
                            default: throw new IllegalStateException("Unable to parse integer range on line " + rd.getLineNumber());
                        }
                    }
                }
            });
        } catch (IllegalStateException | IOException e) {
            logger.error("unable to load Unicode data from file '{}': {}", filename, e.getMessage());
        }
    }

}
