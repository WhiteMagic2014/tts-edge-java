package io.github.whitemagic2014.tts;

import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;

import java.io.File;
import java.io.FileOutputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.ByteBuffer;
import java.util.Map;

public class TTSWebsocket extends WebSocketClient {

    private String storage;
    private String fileName;


    public String getFileName() {
        return fileName;
    }

    public TTSWebsocket(String serverUri, Map<String, String> httpHeaders, String storage, String requestId) throws URISyntaxException {
        super(new URI(serverUri), httpHeaders);
        this.storage = storage;
        this.fileName = requestId + ".mp3";
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
        try (FileOutputStream fos = new FileOutputStream(storage + File.separator + fileName, true)) {
            fos.write(originBytes.array());
            fos.flush();
        } catch (Exception e) {
            e.printStackTrace();
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
