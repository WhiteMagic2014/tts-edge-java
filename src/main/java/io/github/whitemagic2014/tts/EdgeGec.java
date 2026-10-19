package io.github.whitemagic2014.tts;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;

public class EdgeGec {

    private static final int[] INITIAL = {
            0x6A09E667, 0xBB67AE85, 0x3C6EF372, 0xA54FF53A, 0x510E527F, 0x9B05688C, 0x1F83D9AB, 0x5BE0CD19
    };

    private static final int[] RESUME_STATE = {
            0xFD9FBEA2, 0x91243CE7, 0xBE499094, 0xCD2E1E0A, 0x6C006F49, 0x04BD35AB, 0xFEB0A7E4, 0x03EFFBD2
    };

    private static final int[] SCHEDULE = {
            0xEB24BF9E, 0x1558691F, 0xA0C09EE2, 0x0CFA32BA, 0xA622EE8C, 0xD1DD9491, 0xC0DD25A0,
            0xF9E70AB6, 0xF0C31573, 0x7058CE4E, 0x7188E606, 0x00E5AB33, 0xD258B060, 0x72766564
    };

    private static final int[] ROUNDS = {
            0x428A2F98, 0x71374491, 0xB5C0FBCF, 0xE9B5DBA5, 0x3956C25B, 0x59F111F1, 0x923F82A4, 0xAB1C5ED5,
            0xD807AA98, 0x12835B01, 0x243185BE, 0x550C7DC3, 0x72BE5D74, 0x80DEB1FE, 0x9BDC06A7, 0xC19BF174,
            0xE49B69C1, 0xEFBE4786, 0x0FC19DC6, 0x240CA1CC, 0x2DE92C6F, 0x4A7484AA, 0x5CB0A9DC, 0x76F988DA,
            0x983E5152, 0xA831C66D, 0xB00327C8, 0xBF597FC7, 0xC6E00BF3, 0xD5A79147, 0x06CA6351, 0x14292967,
            0x27B70A85, 0x2E1B2138, 0x4D2C6DFC, 0x53380D13, 0x650A7354, 0x766A0ABB, 0x81C2C92E, 0x92722C85,
            0xA2BFE8A1, 0xA81A664B, 0xC24B8B70, 0xC76C51A3, 0xD192E819, 0xD6990624, 0xF40E3585, 0x106AA070,
            0x19A4C116, 0x1E376C08, 0x2748774C, 0x34B0BCB5, 0x391C0CB3, 0x4ED8AA4A, 0x5B9CCA4F, 0x682E6FF3,
            0x748F82EE, 0x78A5636F, 0x84C87814, 0x8CC70208, 0x90BEFFFA, 0xA4506CEB, 0xBEF9A3F7, 0xC67178F2
    };

    private static final int[][] COEFFICIENTS = {
            {0xE82E91B1, 0xB32700D0}, {0x44DE24C6, 0x88DFF51D}, {0x3BEB65BC, 0x29514038}, {0xC89C5933, 0xB328CBDF},
            {0x8D1EC211, 0xD405B4D5}, {0x314EF5D5, 0x26E55F3C}, {0xE92491B1, 0x607801C6}, {0xF0A5B9D5, 0x82B2C1FB},
            {0x06064CF3, 0xFA51A3DA}, {0xDA79FA60, 0x207CC464}, {0xBED20560, 0x3C3C74F3}, {0x89F9C9F8, 0xD2D14B61},
            {0x35FA38C6, 0xBB04A2C1}, {0xCB49E3C6, 0xC5626167}
    };

    private static int ror(int x, int n) {
        return (x >>> n) | (x << (32 - n));
    }

    private static int swap(int x) {
        return ror(x, 16);
    }

    private static int s0(int x) {
        return ror(x, 7) ^ ror(x, 18) ^ (x >>> 3);
    }

    private static int s1(int x) {
        return ror(x, 17) ^ ror(x, 19) ^ (x >>> 10);
    }

    private static int readBE(byte[] p, int off) {
        return ((p[off] & 0xFF) << 24) | ((p[off + 1] & 0xFF) << 16) | ((p[off + 2] & 0xFF) << 8) | (p[off + 3] & 0xFF);
    }

    private static int readLE(byte[] p, int off) {
        return ((p[off + 3] & 0xFF) << 24) | ((p[off + 2] & 0xFF) << 16) | ((p[off + 1] & 0xFF) << 8) | (p[off] & 0xFF);
    }

    private static void expand(int[] w, int first) {
        for (int i = first; i < 64; i++) {
            w[i] = s1(w[i - 2]) + w[i - 7] + s0(w[i - 15]) + w[i - 16];
        }
    }

    private static void compress(int[] state, int[] w, int first) {
        int a = state[0], b = state[1], c = state[2], d = state[3];
        int e = state[4], f = state[5], g = state[6], h = state[7];

        for (int i = first; i < 64; i++) {
            int k = (first == 14 && i == 14) ? 0xC9DC06A7 : ROUNDS[i];
            int t1 = h + (ror(e, 6) ^ ror(e, 11) ^ ror(e, 25)) + (g ^ (e & (f ^ g))) + k + w[i];
            int t2 = (ror(a, 2) ^ ror(a, 13) ^ ror(a, 22)) + ((a & b) | (c & (a | b)));
            h = g; g = f; f = e; e = d + t1; d = c; c = b; b = a; a = t1 + t2;
        }

        int[] work = {a, b, c, d, e, f, g, h};
        for (int i = 0; i < 8; i++) {
            state[i] = (first == 14 ? INITIAL[i] : state[i]) + work[i];
        }
    }

    public static String generateToken(long fileTime) {
        // Round down to the nearest 5-minute block
        fileTime -= fileTime % 3000000000L;

        // Build data prefix without risking locale-specific commas like %d can inject
        String dataStr = fileTime + "6A5AA1D4EAFF4E9FB37E23D68491D6F4";
        byte[] bytes = dataStr.getBytes(StandardCharsets.US_ASCII);
        int len = bytes.length;

        int[] w = new int[64];
        w[14] = ((bytes[0] & 0xFF) << 16) | ((bytes[1] & 0xFF) << 8) | (bytes[2] & 0xFF);
        w[15] = readBE(bytes, 3);

        for (int i = 16; i < 30; i++) {
            int v = SCHEDULE[i - 16];
            if (i - 2 >= 14) v += s1(w[i - 2]) ^ (i == 16 ? 0x000B92C0 : 0);
            if (i - 7 >= 14) v += w[i - 7];
            if (i - 15 >= 14) v += s0(w[i - 15]) ^ (i == 29 ? 0x059C0B80 : 0);
            w[i] = v;
        }
        w[30] = s1(w[28]) + w[23] + s0(w[15]) + w[14] + 0xC9DC06A7 - ROUNDS[14];
        expand(w, 31);

        int[] state = Arrays.copyOf(RESUME_STATE, 8);
        compress(state, w, 14);

        byte[] padded = new byte[128];
        System.arraycopy(bytes, 7, padded, 0, len - 7);
        padded[len - 7] = (byte) 0x80;

        int total = ((len + 57 + 9 + 63) / 64) * 64 - 64;
        long bits = (len + 57L) * 8L;
        for (int i = 0; i < 8; i++) {
            padded[total - 1 - i] = (byte) (bits >>> (i * 8));
        }

        for (int off = 0; off < total; off += 64) {
            for (int i = 0; i < 16; i++) {
                w[i] = readBE(padded, off + i * 4);
            }
            expand(w, 16);
            compress(state, w, 0);
        }

        byte[] digest = new byte[32];
        for (int i = 0; i < 32; i++) {
            digest[i] = (byte) (state[i / 4] >>> (24 - 8 * (i % 4)));
        }

        int ka = readLE(digest, 0) | 1;
        int kb = readLE(digest, 4) | 1;
        int[] factors = {ka * 0x82893933, kb * 0x82893933};
        int[] terms = new int[32];
        int[] offsets = new int[32];
        int count = 14;

        for (int i = 0; i < 14; i++) {
            terms[i] = COEFFICIENTS[i][0] * factors[i % 2];
            offsets[i] = COEFFICIENTS[i][1];
        }

        byte[] input = new byte[72];
        System.arraycopy(bytes, 0, input, 1, len);
        int words = ((len + 1 + 7) / 8) * 2;

        for (int i = 0; i < words; i++) {
            int word = readLE(input, i * 4);
            terms[count] = word * ((i % 2 != 0) ? kb : ka);
            offsets[count] = swap(word) * ((i % 2 != 0) ? 0xC31713DB : 0xEF0569FB);
            if (i == 0) {
                terms[count] += 0xF2F90B1A * factors[0];
                offsets[count] += 0x0B1A0000;
            }
            count++;
        }

        int swapAcc = 0, swapLast = 0, s = 0, acc = 0;
        for (int i = 0; i < count; i += 2) {
            int x = terms[i], y = terms[i + 1];

            int t1 = swap(offsets[i] + x);
            int q1 = t1 * 0x76F50F34;
            swapAcc += swap(q1) * 0xC3EFEA97 + q1;

            int t2 = swap(offsets[i + 1] + y);
            int q2 = t2 * 0x1881CE3C;
            swapLast = swap(q2) * 0x35BD1EC9 + q2;
            swapAcc += swapLast;

            s = s * ka + x;
            s = swap(s) * 0xCF98B111;
            s = swap(s) * 0x87085B9F;
            s = swap(s) * 0x12CEB96D;
            s = swap(s) * 0x257E1D83;
            acc += s;

            int t = s * kb + y;
            t = swap(t) * 0xA27416F5;
            t = swap(t) * 0xD38396FF;
            s = swap(swap(t) * 0x7C932B89) * 0xBFA49F69;
            acc += s;
        }

        int[] cs = {swapLast ^ s, swapAcc ^ acc};
        StringBuilder result = new StringBuilder(81);
        char[] hexArray = "0123456789ABCDEF".toCharArray();

        for (int i = 0; i < 8; i++) {
            int v = (cs[i / 4] >>> (8 * (i % 4))) & 0xFF;
            result.append(hexArray[v >>> 4]).append(hexArray[v & 0x0F]);
        }
        result.append('-');
        for (byte b : digest) {
            int v = b & 0xFF;
            result.append(hexArray[v >>> 4]).append(hexArray[v & 0x0F]);
        }

        return result.toString();
    }
}