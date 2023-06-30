package io.github.whitemagic2014.tts;

import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;

import java.io.File;
import java.io.FileOutputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.Map;

public class TTSWebsocket extends WebSocketClient {

    private String storage;
    private String fileName;
    private Boolean findHeadHook;

    public String getFileName() {
        return fileName;
    }

    public TTSWebsocket(String serverUri, Map<String, String> httpHeaders, String storage, String fileName, Boolean findHeadHook) throws URISyntaxException {
        super(new URI(serverUri), httpHeaders);
        this.storage = storage;
        this.fileName = fileName;
        this.findHeadHook = findHeadHook;
    }

    @Override
    public void onOpen(ServerHandshake handshakedata) {

    }

    @Override
    public void onMessage(String message) {
        if (message.contains("Path:turn.end")) {
            close();
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

    private static byte[] head = new byte[]{0x50, 0x61, 0x74, 0x68, 0x3a, 0x61, 0x75, 0x64, 0x69, 0x6f, 0x0d, 0x0a};

    /**
     * This implementation method is more generic as it searches for the file header marker in the given file header and removes it. However, it may have lower efficiency.
     *
     * @param originBytes
     */
    private void findHeadHook(ByteBuffer originBytes) {
        byte[] origin = originBytes.array();
        int headIndex = -1;
        for (int i = 0; i < origin.length - head.length; i++) {
            boolean match = true;
            for (int j = 0; j < head.length; j++) {
                if (origin[i + j] != head[j]) {
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
            byte[] voiceBytesRemoveHead = Arrays.copyOfRange(origin, headIndex + head.length, origin.length);
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
}
