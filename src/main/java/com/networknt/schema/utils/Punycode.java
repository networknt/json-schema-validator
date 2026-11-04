package com.networknt.schema.utils;

/**
 * Punycode (RFC 3492) for IDNA2008 A-labels.
 * <p>
 * {@link java.net.IDN} implements IDNA2003, which maps or rejects code points that IDNA2008
 * allows, such as U+00DF (sharp s), U+03C2 (final sigma), U+200C (ZWNJ) and U+200D (ZWJ). Decoding
 * the Punycode directly leaves those code points for the RFC 5892 rules to judge.
 */
public final class Punycode {
    private static final int BASE = 36;
    private static final int TMIN = 1;
    private static final int TMAX = 26;
    private static final int SKEW = 38;
    private static final int DAMP = 700;
    private static final int INITIAL_BIAS = 72;
    private static final int INITIAL_N = 0x80;
    private static final char DELIMITER = '-';

    private Punycode() {
    }

    /**
     * Decodes a Punycode string (an A-label without its {@code xn--} prefix).
     *
     * @param input the Punycode string
     * @return the decoded string, or {@code null} if the input is not valid Punycode
     */
    public static String decode(String input) {
        int b = input.lastIndexOf(DELIMITER);
        if (b < 0) b = 0;
        int[] output = new int[input.length()];
        int length = 0;
        for (int j = 0; j < b; j++) {
            char c = input.charAt(j);
            if (c >= 0x80) return null;
            output[length++] = c;
        }
        int n = INITIAL_N;
        int bias = INITIAL_BIAS;
        int i = 0;
        for (int in = b > 0 ? b + 1 : 0; in < input.length(); ) {
            int oldi = i;
            int w = 1;
            for (int k = BASE; ; k += BASE) {
                if (in >= input.length()) return null;
                int digit = digit(input.charAt(in++));
                if (digit < 0) return null;
                if (digit > (Integer.MAX_VALUE - i) / w) return null;
                i += digit * w;
                int t = k <= bias ? TMIN : k >= bias + TMAX ? TMAX : k - bias;
                if (digit < t) break;
                if (w > Integer.MAX_VALUE / (BASE - t)) return null;
                w *= BASE - t;
            }
            bias = adapt(i - oldi, length + 1, oldi == 0);
            if (i / (length + 1) > Integer.MAX_VALUE - n) return null;
            n += i / (length + 1);
            i %= length + 1;
            if (n > Character.MAX_CODE_POINT || (n >= Character.MIN_SURROGATE && n <= Character.MAX_SURROGATE)) {
                return null;
            }
            if (length == output.length) {
                output = java.util.Arrays.copyOf(output, output.length * 2);
            }
            System.arraycopy(output, i, output, i + 1, length - i);
            output[i++] = n;
            length++;
        }
        return new String(output, 0, length);
    }

    /**
     * Encodes a string as Punycode (without the {@code xn--} prefix).
     *
     * @param input the string to encode
     * @return the Punycode string
     */
    public static String encode(String input) {
        int[] codePoints = input.codePoints().toArray();
        StringBuilder output = new StringBuilder();
        for (int c : codePoints) {
            if (c < 0x80) output.append((char) c);
        }
        int b = output.length();
        int h = b;
        if (b > 0) output.append(DELIMITER);
        int n = INITIAL_N;
        int delta = 0;
        int bias = INITIAL_BIAS;
        while (h < codePoints.length) {
            int m = Integer.MAX_VALUE;
            for (int c : codePoints) {
                if (c >= n && c < m) m = c;
            }
            delta += (m - n) * (h + 1);
            n = m;
            for (int c : codePoints) {
                if (c < n) delta++;
                if (c == n) {
                    int q = delta;
                    for (int k = BASE; ; k += BASE) {
                        int t = k <= bias ? TMIN : k >= bias + TMAX ? TMAX : k - bias;
                        if (q < t) break;
                        output.append(encodeDigit(t + (q - t) % (BASE - t)));
                        q = (q - t) / (BASE - t);
                    }
                    output.append(encodeDigit(q));
                    bias = adapt(delta, h + 1, h == b);
                    delta = 0;
                    h++;
                }
            }
            delta++;
            n++;
        }
        return output.toString();
    }

    private static int adapt(int delta, int numPoints, boolean firstTime) {
        delta = firstTime ? delta / DAMP : delta / 2;
        delta += delta / numPoints;
        int k = 0;
        while (delta > ((BASE - TMIN) * TMAX) / 2) {
            delta /= BASE - TMIN;
            k += BASE;
        }
        return k + (BASE - TMIN + 1) * delta / (delta + SKEW);
    }

    private static int digit(char c) {
        if (c >= '0' && c <= '9') return c - '0' + 26;
        if (c >= 'a' && c <= 'z') return c - 'a';
        if (c >= 'A' && c <= 'Z') return c - 'A';
        return -1;
    }

    private static char encodeDigit(int d) {
        return (char) (d < 26 ? 'a' + d : '0' + d - 26);
    }
}
