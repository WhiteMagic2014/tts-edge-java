package io.github.whitemagic2014.tts;

import com.alibaba.fastjson.JSONObject;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.drafts.Draft_6455;
import org.java_websocket.handshake.ServerHandshake;

import java.io.File;
import java.io.FileOutputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.Map;

public class TTSWebsocket extends WebSocketClient {

    private static final byte[] HEAD = new byte[]{0x50, 0x61, 0x74, 0x68, 0x3a, 0x61, 0x75, 0x64, 0x69, 0x6f, 0x0d, 0x0a};

    private String storage;
    private String fileName;
    private Boolean findHeadHook;
    private SubMaker subMaker;

    public TTSWebsocket(String serverUri, Map<String, String> httpHeaders, int connectTimeout, String storage, String fileName,
                        Boolean findHeadHook, boolean enableVttFile) throws URISyntaxException {
        super(new URI(serverUri), new Draft_6455(), httpHeaders, connectTimeout);
        this.storage = storage;
        this.fileName = fileName;
        this.findHeadHook = findHeadHook;
        if (enableVttFile) {
            this.subMaker = new SubMaker(storage + File.separator + fileName);
        }
    }

    @Override
    public void onOpen(ServerHandshake handshakedata) { }

    @Override
    public void onMessage(String message) {
        if (message.contains("Path:turn.end")) {
            if (subMaker != null) {
                subMaker.generateSubs(10);
            }
            close();
        } else if (message.contains("\"Type\": \"WordBoundary\"")) {
            JSONObject json = JSONObject.parseObject(message.substring(message.indexOf("{")));
            JSONObject item = json.getJSONArray("Metadata").getJSONObject(0).getJSONObject("Data");
            if (subMaker != null) {
                subMaker.createSub(item.getDouble("Offset"), item.getDouble("Duration"), item.getJSONObject("text").getString("Text"));
            }
        }
    }

    @Override
    public void onMessage(ByteBuffer originBytes) {
        if (findHeadHook) {
            findHeadHook(originBytes);
        } else {
            fixHeadHook(originBytes);
        }
    }

    /**
     * This implementation method is more generic as it searches for the file header marker in the given file header and removes it. However, it may have lower efficiency.
     *
     * @param originBytes
     */
    private void findHeadHook(ByteBuffer originBytes) {
        byte[] origin = originBytes.array();
        int headIndex = -1;
        for (int i = 0; i < origin.length - HEAD.length; i++) {
            boolean match = true;
            for (int j = 0; j < HEAD.length; j++) {
                if (origin[i + j] != HEAD[j]) {
                    match = false;
                    break;
                }
            }
            if (match) {
                headIndex = i;
                break;
            }
        }
        if (headIndex != -1) {
            byte[] voiceBytesRemoveHead = Arrays.copyOfRange(origin, headIndex + HEAD.length, origin.length);
            try (FileOutputStream fos = new FileOutputStream(storage + File.separator + fileName, true)) {
                fos.write(voiceBytesRemoveHead);
                fos.flush();
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }

    /**
     * This method directly specifies the file header marker, which makes it faster. However, if the format changes, it may become unusable.
     *
     * @param originBytes
     */
    public void fixHeadHook(ByteBuffer originBytes) {
        String str = new String(originBytes.array());
        byte[] origin = originBytes.array();
        int skip;
        if (str.contains("Content-Type")) {
            if (str.contains("audio/mpeg")) {
                skip = 130;
            } else if (str.contains("codec=opus")) {
                skip = 142;
            } else {
                skip = 0;
            }
        } else {
            skip = 105;
        }
        byte[] voiceBytesRemoveHead = Arrays.copyOfRange(origin, skip, origin.length);
        try (FileOutputStream fos = new FileOutputStream(storage + File.separator + fileName, true)) {
            fos.write(voiceBytesRemoveHead);
            fos.flush();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    @Override
    public void onClose(int code, String reason, boolean remote) {
    }

    @Override
    public void onError(Exception ex) {
        ex.printStackTrace();
    }

    public String getFileName() {
        return fileName;
    }
}
