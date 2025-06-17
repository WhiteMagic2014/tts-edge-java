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
import java.util.concurrent.CountDownLatch;

public class TTSWebsocket extends WebSocketClient {

    /**
     * When a complete session ends, this sessionLatch will become 0.
     */
    private CountDownLatch sessionLatch;

    private MessageListener messageListener;

    public TTSWebsocket(String serverUri, Map<String, String> httpHeaders, int connectTimeout) throws URISyntaxException {
        super(new URI(serverUri), new Draft_6455(), httpHeaders, connectTimeout);
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
        sessionLatch.countDown();
    }

    @Override
    public void onError(Exception ex) {
        ex.printStackTrace();
    }

    public void finishBlocking() throws InterruptedException {
        this.sessionLatch.await();
    }

    public void openSession(MessageListener messageListener) {
        this.messageListener = messageListener;
        this.sessionLatch = new CountDownLatch(1);
    }
}
