package io.github.whitemagic2014.tts;

import io.github.whitemagic2014.tts.bean.Voice;
import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.net.URISyntaxException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class TTS {

    private String EDGE_URL = "wss://speech.platform.bing.com/consumer/speech/synthesize/readaloud/edge/v1?TrustedClientToken=6A5AA1D4EAFF4E9FB37E23D68491D6F4";
    private String EDGE_UA = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/99.0.4844.74 Safari/537.36 Edg/99.0.1150.55";
    private String EDGE_ORIGIN = "chrome-extension://jdiccldimpdaibmpdkjnbmckianbfold";
    private String voicesListUrl = "https://speech.platform.bing.com/consumer/speech/synthesize/readaloud/voices/list?trustedclienttoken=6A5AA1D4EAFF4E9FB37E23D68491D6F4";

    private Map<String, String> headers;

    private Voice voice;
    private String content;
    private Boolean findHeadHook = false;
    private String format = "audio-24khz-48kbitrate-mono-mp3";
    private String voicePitch = "+0Hz";
    private String voiceRate = "+0%";
    private String voiceVolume = "+0%";
    private String storage = "./storage";
    private String fileName;
    private int connectTimeout = 0;
    private Boolean overwrite;


    public TTS voicePitch(String voicePitch) {
        this.voicePitch = voicePitch;
        return this;
    }

    public TTS voiceRate(String voiceRate) {
        this.voiceRate = voiceRate;
        return this;
    }

    public TTS voiceVolume(String voiceVolume) {
        this.voiceVolume = voiceVolume;
        return this;
    }

    public TTS fileName(String fileName) {
        this.fileName = fileName;
        return this;
    }

    public TTS formatMp3() {
        this.format = "audio-24khz-48kbitrate-mono-mp3";
        return this;
    }

    public TTS formatOpus() {
        this.format = "webm-24khz-16bit-mono-opus";
        return this;
    }

    public TTS connectTimeout(int connectTimeout) {
        this.connectTimeout = connectTimeout;
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

    public TTS storage(String storage) {
        this.storage = storage;
        return this;
    }


    public TTS(Voice voice, String content) {
        this.voice = voice;
        this.content = content;
    }

    public TTS headers(Map<String, String> headers) {
        this.headers = headers;
        return this;
    }

    public TTS overwrite(Boolean overwrite) {
        this.overwrite = overwrite;
        return this;
    }

    public String trans() {
        if (voice == null) {
            throw new RuntimeException("please set voice");
        }
        String str = removeIncompatibleCharacters(content);
        if (StringUtils.isBlank(str)) {
            throw new RuntimeException("invalid content");
        }

        File storageFolder = new File(storage);
        if (!storageFolder.exists()) {
            storageFolder.mkdirs();
        }

        String dateStr = dateToString(new Date());
        String reqId = uuid();

        String audioFormat = mkAudioFormat(dateStr);
        String ssml = mkssml(voice.getLocale(), voice.getName());
        String ssmlHeadersPlusData = ssmlHeadersPlusData(reqId, dateStr, ssml);

        if (headers == null) {
            headers = new HashMap<>();
            headers.put("Origin", EDGE_ORIGIN);
            headers.put("Pragma", "no-cache");
            headers.put("Cache-Control", "no-cache");
            headers.put("User-Agent", EDGE_UA);
        }
        String fName = StringUtils.isBlank(fileName) ? reqId : fileName;
        if (format.equals("audio-24khz-48kbitrate-mono-mp3")) {
            fName += ".mp3";
        } else if (format.equals("webm-24khz-16bit-mono-opus")) {
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
        try {
            TTSWebsocket client = new TTSWebsocket(EDGE_URL, headers, connectTimeout, storage, fName, findHeadHook);
            client.connect();
            while (!client.isOpen()) {
                // wait open
                Thread.sleep(100);
            }
            client.send(audioFormat);
            client.send(ssmlHeadersPlusData);
            while (client.isOpen()) {
                // wait close
                Thread.sleep(100);
            }
            return fName;
        } catch (URISyntaxException | InterruptedException e) {
            e.printStackTrace();
            return null;
        }
    }


    private String mkAudioFormat(String dateStr) {
        return "X-Timestamp:" + dateStr + "\r\n" +
                "Content-Type:application/json; charset=utf-8\r\n" +
                "Path:speech.config\r\n\r\n" +
                "{\"context\":{\"synthesis\":{\"audio\":{\"metadataoptions\":{\"sentenceBoundaryEnabled\":\"false\",\"wordBoundaryEnabled\":\"true\"},\"outputFormat\":\"" + format + "\"}}}}\n";
    }


    private String mkssml(String locate, String voiceName) {
        return "<speak version='1.0' xmlns='http://www.w3.org/2001/10/synthesis' xml:lang='" + locate + "'>" +
                "<voice name='" + voiceName + "'><prosody pitch='" + voicePitch + "' rate='" + voiceRate + "' volume='" + voiceVolume + "'>" +
                content + "</prosody></voice></speak>";
    }


    private String ssmlHeadersPlusData(String requestId, String timestamp, String ssml) {
        return "X-RequestId:" + requestId + "\r\n" +
                "Content-Type:application/ssml+xml\r\n" +
                "X-Timestamp:" + timestamp + "Z\r\n" +
                "Path:ssml\r\n\r\n" + ssml;
    }


    private String dateToString(Date date) {
        SimpleDateFormat sdf = new SimpleDateFormat("EEE MMM dd yyyy HH:mm:ss 'GMT'Z (zzzz)");
        return sdf.format(date);
    }


    private String uuid() {
        return UUID.randomUUID().toString().replace("-", "");
    }

    private String removeIncompatibleCharacters(String input) {
        if (StringUtils.isBlank(input)) {
            return null;
        }
        StringBuilder output = new StringBuilder();
        for (int i = 0; i < input.length(); i++) {
            char c = input.charAt(i);
            int code = (int) c;
            if ((0 <= code && code <= 8) || (11 <= code && code <= 12) || (14 <= code && code <= 31)) {
                output.append(" ");
            } else {
                output.append(c);
            }
        }
        return output.toString();
    }

}
