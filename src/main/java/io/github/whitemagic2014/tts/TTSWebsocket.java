package io.github.whitemagic2014.tts;

import org.java_websocket.client.WebSocketClient;
import org.java_websocket.drafts.Draft_6455;
import org.java_websocket.handshake.ServerHandshake;

import java.net.URI;
import java.net.URISyntaxException;
import java.nio.ByteBuffer;
import java.util.Map;

public class TTSWebsocket extends WebSocketClient {

    private MessageListener messageListener;

    public TTSWebsocket(String serverUri, Map<String, String> httpHeaders, int connectTimeout) throws URISyntaxException, InterruptedException {
        super(new URI(serverUri), new Draft_6455(), httpHeaders, connectTimeout);
        super.connectBlocking();
    }

    @Override
    public void onOpen(ServerHandshake handshakedata) {

    }

    @Override
    public void onMessage(String message) {
        this.messageListener.onMessage(message);
    }

    @Override
    public void onMessage(ByteBuffer originBytes) {
        this.messageListener.onMessage(originBytes);
    }

    @Override
    public void onClose(int code, String reason, boolean remote) {
        System.out.println("TTSWebsocket closed:" + reason);
    }

    @Override
    public void onError(Exception ex) {
        ex.printStackTrace();
    }

    public void startBlocking() {
        try {
            this.messageListener.startBlocking();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public void openSession(MessageListener messageListener) {
        this.messageListener = messageListener;
    }
}
