package io.github.whitemagic2014.tts;

import io.github.whitemagic2014.tts.bean.Voice;
import lombok.Data;
import lombok.experimental.Accessors;
import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Data
@Accessors(chain = true, fluent = true)
public class TTS {

    private static final String EDGE_URL = "wss://speech.platform.bing.com/consumer/speech/synthesize/readaloud/edge/v1?TrustedClientToken=6A5AA1D4EAFF4E9FB37E23D68491D6F4";
    private static final  String EDGE_UA = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/99.0.4844.74 Safari/537.36 Edg/99.0.1150.55";
    private static final  String EDGE_ORIGIN = "chrome-extension://jdiccldimpdaibmpdkjnbmckianbfold";
    private static final  String VOICES_LIST_URL = "https://speech.platform.bing.com/consumer/speech/synthesize/readaloud/voices/list?trustedclienttoken=6A5AA1D4EAFF4E9FB37E23D68491D6F4";

    private final Voice voice;

    private Map<String, String> headers;
    private String content;
    private Boolean findHeadHook = false;
    private String format = "audio-24khz-48kbitrate-mono-mp3";
    private String voicePitch = "+0Hz";
    private String voiceRate = "+0%";
    private String voiceVolume = "+0%";
    private String storage = "./storage";
    private String fileName;
    private int connectTimeout = 0;
    private Boolean overwrite = true;

    /**
     * Set to true to resolve the rate limiting issue in certain regions.
     */
    private Boolean isRateLimited = false;

    /**
     * Whether to enable VTT file support, default false
     */
    private boolean enableVttFile = false;

    public TTS(Voice voice, String content) {
        this.voice = voice;
        this.content = content;
        this.headers = new HashMap<>();
        this.headers.put("Origin", EDGE_ORIGIN);
        this.headers.put("Pragma", "no-cache");
        this.headers.put("Cache-Control", "no-cache");
        this.headers.put("User-Agent", EDGE_UA);
    }

    public TTS formatMp3() {
        this.format = "audio-24khz-48kbitrate-mono-mp3";
        return this;
    }

    public TTS formatOpus() {
        this.format = "webm-24khz-16bit-mono-opus";
        return this;
    }

    /**
     * This hook is more generic as it searches for the file header marker in the given file header and removes it. However, it may have lower efficiency.
     */
    public TTS findHeadHook() {
        this.findHeadHook = true;
        return this;
    }

    /**
     * default
     * This hook directly specifies the file header marker, which makes it faster. However, if the format changes, it may become unusable.
     */
    public TTS fixHeadHook() {
        this.findHeadHook = false;
        return this;
    }

    public String trans() {
        if (voice == null) {
            throw new RuntimeException("please set voice");
        }
        this.content = removeIncompatibleCharacters(content);
        if (StringUtils.isBlank(this.content)) {
            throw new RuntimeException("invalid content");
        }

        File storageFolder = new File(storage);
        if (!storageFolder.exists()) {
            storageFolder.mkdirs();
        }

        String dateStr = dateToString(new Date());
        String reqId = uuid();
        String audioFormat = mkAudioFormat(dateStr, format);
        String ssml = mkssml(voice.getLocale(), voice.getName(), voicePitch, voiceRate, voiceVolume, content);
        String ssmlHeadersPlusData = ssmlHeadersPlusData(reqId, dateStr, ssml);

        String fName = StringUtils.isBlank(fileName) ? reqId : fileName;
        if ("audio-24khz-48kbitrate-mono-mp3".equals(format)) {
            fName += ".mp3";
        } else if ("webm-24khz-16bit-mono-opus".equals(format)) {
            fName += ".opus";
        }
        if (overwrite) {
            File voiceFile = new File(storage + File.separator + fName);
            File subFile = new File(storage + File.separator + fName + ".vtt");
            if (voiceFile.exists()) {
                voiceFile.delete();
            }
            if (subFile.exists()) {
                subFile.delete();
            }
        }
        String requestEdgeUrl = EDGE_URL;
        if (isRateLimited) {
            requestEdgeUrl += createSecMSGEC();
        }
        try {
            TTSWebsocket client = new TTSWebsocket(requestEdgeUrl, headers, connectTimeout, storage, fName, findHeadHook, enableVttFile);
            client.connectBlocking();
            client.send(audioFormat);
            client.send(ssmlHeadersPlusData);
            client.finishBlocking();
            return fName;
        } catch (URISyntaxException | InterruptedException e) {
            throw new IllegalStateException(e);
        }
    }

    private static String mkAudioFormat(String dateStr, String format) {
        return "X-Timestamp:" + dateStr + "\r\n" +
                "Content-Type:application/json; charset=utf-8\r\n" +
                "Path:speech.config\r\n\r\n" +
                "{\"context\":{\"synthesis\":{\"audio\":{\"metadataoptions\":{\"sentenceBoundaryEnabled\":\"false\",\"wordBoundaryEnabled\":\"true\"},\"outputFormat\":\"" + format + "\"}}}}\n";
    }


    private static String mkssml(String locate, String voiceName, String voicePitch, String voiceRate, String voiceVolume, String content) {
        return "<speak version='1.0' xmlns='http://www.w3.org/2001/10/synthesis' xml:lang='" + locate + "'>" +
                "<voice name='" + voiceName + "'><prosody pitch='" + voicePitch + "' rate='" + voiceRate + "' volume='" + voiceVolume + "'>" +
                content + "</prosody></voice></speak>";
    }


    private static String ssmlHeadersPlusData(String requestId, String timestamp, String ssml) {
        return "X-RequestId:" + requestId + "\r\n" +
                "Content-Type:application/ssml+xml\r\n" +
                "X-Timestamp:" + timestamp + "Z\r\n" +
                "Path:ssml\r\n\r\n" + ssml;
    }


    private static String dateToString(Date date) {
        return new SimpleDateFormat("EEE MMM dd yyyy HH:mm:ss 'GMT'Z (zzzz)").format(date);
    }

    public static String createSecMSGEC() {
        try {
            String SEC_MS_GEC_Version = "1-130.0.2849.68";
            long ticks = (long) (Math.floor((System.currentTimeMillis() / 1000.0) + 11644473600L) * 10000000);
            long roundedTicks = ticks - (ticks % 3000000000L);
            String str = roundedTicks + "6A5AA1D4EAFF4E9FB37E23D68491D6F4";
            MessageDigest sha256 = MessageDigest.getInstance("SHA-256");
            byte[] hash = sha256.digest(str.getBytes(StandardCharsets.UTF_8));
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) {
                    hexString.append('0');
                }
                hexString.append(hex);
            }
            String SEC_MS_GEC = hexString.toString().toUpperCase();
            return String.format("&Sec-MS-GEC=%s&Sec-MS-GEC-Version=%s", SEC_MS_GEC, SEC_MS_GEC_Version);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }

    private static String uuid() {
        return UUID.randomUUID().toString().replace("-", "");
    }

    private static String removeIncompatibleCharacters(String input) {
        if (StringUtils.isBlank(input)) {
            return null;
        }

        /*
         *Define the special characters that need to be escaped and their corresponding escape sequences (using XML/HTML as examples).
         */
        final Map<Character, String> escapeMap = new HashMap<Character, String>() {{
            put('<', "&lt;");   // Escape the less-than sign (use &lt;)
            put('>', "&gt;");   // Escape the greater-than sign (use &gt;)
            put('&', "&amp;");  // Escape the ampersand (use &amp;)
            put('"', "&quot;"); // Escape the quotation mark (use &quot;)
            put('\'', "&apos;");// Escape the apostrophe (use &apos;)
        }};

        StringBuilder output = new StringBuilder();
        for (int i = 0; i < input.length(); i++) {
            char c = input.charAt(i);
            int code = (int) c;

            boolean isControlChar = (0 <= code && code <= 8) ||(11 <= code && code <= 12) ||(14 <= code && code <= 31);

            if (isControlChar) {
                output.append(' ');
            } else if (escapeMap.containsKey(c)) {
                output.append(escapeMap.get(c));
            } else {
                output.append(c);
            }
        }
        return output.toString();
    }
}
