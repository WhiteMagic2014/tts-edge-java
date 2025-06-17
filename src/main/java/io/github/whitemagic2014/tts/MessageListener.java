/*
 * Copyright (c) 2025 by XUANWU INFORMATION TECHNOLOGY CO.
 *             All rights reserved
 */

package io.github.whitemagic2014.tts;

import com.alibaba.fastjson.JSONObject;
import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.concurrent.CountDownLatch;

/**
 * @author huangtianpei
 * @date 2025-06-17
 */
public class MessageListener {

    private static final byte[] HEAD = new byte[]{0x50, 0x61, 0x74, 0x68, 0x3a, 0x61, 0x75, 0x64, 0x69, 0x6f, 0x0d, 0x0a};

    private String storage;
    private String filename;
    private Boolean findHeadHook;
    private SubMaker subMaker;

    /**
     * When a complete session ends, this sessionLatch will become 0.
     */
    private CountDownLatch sessionLatch = new CountDownLatch(1);

    public MessageListener(String storage, String filename, Boolean findHeadHook, boolean enableVttFile, boolean overwrite) {
        this.storage = storage;
        this.filename = filename;
        this.findHeadHook = findHeadHook;
        if (enableVttFile) {
            this.subMaker = new SubMaker(storage + File.separator + filename);
        }
        if (overwrite) {
            File voiceFile = new File(storage + File.separator + filename);
            File subFile = new File(storage + File.separator + filename + ".vtt");
            if (voiceFile.exists()) {
                voiceFile.delete();
            }
            if (subFile.exists()) {
                subFile.delete();
            }
        }
    }

    public void onMessage(String message) {
        if (message.contains("Path:turn.end")) {
            if (subMaker != null) {
                subMaker.generateSubs(10);
            }
            sessionLatch.countDown();
        } else if (message.contains("\"Type\": \"WordBoundary\"")) {
            JSONObject json = JSONObject.parseObject(message.substring(message.indexOf("{")));
            JSONObject item = json.getJSONArray("Metadata").getJSONObject(0).getJSONObject("Data");
            if (subMaker != null) {
                subMaker.createSub(item.getDouble("Offset"), item.getDouble("Duration"), item.getJSONObject("text").getString("Text"));
            }
        }
    }

    public void onMessage(ByteBuffer originBytes) {
        if (findHeadHook) {
            findHeadHook(originBytes);
        } else {
            fixHeadHook(originBytes);
        }
    }

    public void finishBlocking() throws InterruptedException {
        this.sessionLatch.await();
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
            try (FileOutputStream fos = new FileOutputStream(storage + File.separator + filename, true)) {
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
        try (FileOutputStream fos = new FileOutputStream(storage + File.separator + filename, true)) {
            fos.write(voiceBytesRemoveHead);
            fos.flush();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }


}
