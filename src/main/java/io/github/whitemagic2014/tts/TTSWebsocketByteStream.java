package io.github.whitemagic2014.tts;

import org.java_websocket.client.WebSocketClient;
import org.java_websocket.drafts.Draft_6455;
import org.java_websocket.handshake.ServerHandshake;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.ByteBuffer;
import java.util.Map;
import java.util.concurrent.CountDownLatch;

public class TTSWebsocketByteStream extends WebSocketClient {

    private ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

    private CountDownLatch closeLatch = new CountDownLatch(1);

    public TTSWebsocketByteStream(String serverUri, Map<String, String> httpHeaders, int connectTimeout) throws URISyntaxException, InterruptedException {
        super(new URI(serverUri), new Draft_6455(), httpHeaders, connectTimeout);
        super.connectBlocking();
    }


    @Override
    public void onOpen(ServerHandshake serverHandshake) {
    }

    @Override
    public void onMessage(String message) {
        if (message.contains("Path:turn.end")) {
            close();
        }
    }

    @Override
    public void onMessage(ByteBuffer originBytes) {
        byte[] bytes = new byte[originBytes.remaining()];
        originBytes.get(bytes);
        try {
            outputStream.write(bytes);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public ByteArrayOutputStream getOutputStream() {
        return outputStream;
    }

    @Override
    public void onClose(int code, String reason, boolean remote) {
        System.out.println("TTSWebsocket closed:" + reason);
        closeLatch.countDown();
    }

    public void startBlocking() throws InterruptedException  {
        closeLatch.await();
    }

    @Override
    public void onError(Exception e) {
        e.printStackTrace();
        closeLatch.countDown();
    }
}
