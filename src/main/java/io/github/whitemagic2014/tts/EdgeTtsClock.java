package io.github.whitemagic2014.tts;

import java.net.HttpURLConnection;
import java.net.URL;
import java.time.Instant;
import java.time.format.DateTimeFormatter;

public class EdgeTtsClock {
    private static long skewTicks = 0;
    private static boolean isSynced = false;

    public static synchronized void sync() {
        if (isSynced) return;
        try {
            URL url = new URL("https://speech.platform.bing.com/consumer/speech/synthesize/readaloud/voices/list?trustedclienttoken=6A5AA1D4EAFF4E9FB37E23D68491D6F4");
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setConnectTimeout(3000);

            String dateHeader = conn.getHeaderField("Date");
            if (dateHeader != null) {
                // Parse RFC-1123 "Date" header to milliseconds
                long serverTime = DateTimeFormatter.RFC_1123_DATE_TIME.parse(dateHeader, Instant::from).toEpochMilli();
                long localTime = System.currentTimeMillis();

                // Calculate delta in 100-nanosecond ticks
                skewTicks = (serverTime - localTime) * 10000L;
                isSynced = true;
            }
        } catch (Exception e) {
            // Failsafe: if the network drops, default to 0 skew
            System.err.println("Warning: Failed to sync Edge TTS clock skew.");
        }
    }

    public static long getAdjustedTicks() {
        if (!isSynced) sync();

        long epoch1601 = Instant.parse("1601-01-01T00:00:00Z").toEpochMilli();
        long localTicks = (System.currentTimeMillis() - epoch1601) * 10000L;

        return localTicks + skewTicks;
    }
}