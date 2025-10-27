package io.github.whitemagic2014.tts;

import io.github.whitemagic2014.tts.bean.TransRecord;
import io.github.whitemagic2014.tts.bean.Voice;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class TTSTest {


    @Test
    void convert_to_mp3_with_byte_stream() {
        String voiceName = "zh-CN-XiaoyiNeural";
        Optional<Voice> voiceOptional = TTSVoice.provides()
                .stream()
                .filter(v -> voiceName.equals(v.getShortName()))
                .findFirst();
        if (!voiceOptional.isPresent()) {
            throw new IllegalStateException("voice not found：" + voiceName);
        }
        Voice voice = voiceOptional.get();
        String content = "你好，有什么可以帮助你的吗，今天的天气很不错呢";

        TTS tts = new TTS(voice, content)
                .isRateLimited(true) // Set to true to resolve the rate limiting issue in certain regions.
                .formatMp3();  // default mp3.
//                .formatOpus() // or opus
//                .voicePitch()
//                .voiceRate()
//                .voiceVolume()
//                .connectTimeout(0) // set connect timeout

        ByteArrayOutputStream stream = tts.transToAudioStream();
        // Write to file to test if the stream is correct.
        try (FileOutputStream fileOutputStream = new FileOutputStream("./storage/test.mp3")) {
            stream.writeTo(fileOutputStream);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 测试单内容场景能否转换成功
     */
    @Test
    void should_convert_to_mp3_file_success_with_single_content() {
        String voiceName = "zh-CN-XiaoyiNeural";
        Optional<Voice> voiceOptional = TTSVoice.provides()
                .stream()
                .filter(v -> voiceName.equals(v.getShortName()))
                .findFirst();
        if (!voiceOptional.isPresent()) {
            throw new IllegalStateException("voice not found：" + voiceName);
        }
        Voice voice = voiceOptional.get();
        String filename = voiceName + "-" + "test-tts";
        String content = "你好，有什么可以帮助你的吗，今天的天气很不错呢";
        TTS tts = new TTS(voice, content)
                .findHeadHook()
                .isRateLimited(true) // Set to true to resolve the rate limiting issue in certain regions.
                .fileName(filename)// You can customize the file name; if omitted, a random file name will be generated.
                .overwrite(true) // When the specified file name is the same, it will either overwrite or append to the file.
                .formatMp3();  // default mp3.
//                .formatOpus() // or opus
//                .voicePitch()
//                .voiceRate()
//                .voiceVolume()
//                .storage()  // the output file storage ,default is ./storage
//                .connectTimeout(0) // set connect timeout
        tts.trans();
        // you can find the voice file in storage folder
    }

    /**
     * 测试批量内容场景能否转换成功
     */
    @Test
    void should_convert_to_mp3_file_success_with_multi_content() throws IOException {
        String voiceName = "zh-CN-XiaoyiNeural";
        Optional<Voice> voiceOptional = TTSVoice.provides()
                .stream()
                .filter(v -> voiceName.equals(v.getShortName()))
                .findFirst();
        if (!voiceOptional.isPresent()) {
            throw new IllegalStateException("voice not found：" + voiceName);
        }
        Voice voice = voiceOptional.get();
        List<TransRecord> recordList = new ArrayList<>();
        String store = "./storage";

        // create batch task
        for (int i = 0; i < 100; i++) {
            TransRecord record = new TransRecord();
            record.setContent(i + ", hello tts, 你好，有什么可以帮助你的吗");
            record.setFilename(i + ".test-tts");
            recordList.add(record);
            Files.deleteIfExists(Paths.get(buildFilename(store, record)));
        }
        new TTS(voice)
                .findHeadHook()
                .isRateLimited(true)
                .overwrite(true)
                .batch(recordList) // set batch task
                .parallel(12) // set up 12 parallel threads
                .storage(store)
                .formatMp3()
                .batchTrans(); // trans
        for (TransRecord record : recordList) {
            Path path = Paths.get(buildFilename(store, record));
            Assertions.assertTrue(Files.exists(path), "file not found in " + path.toString());
        }
    }

    private static String buildFilename(String store, TransRecord record) {
        return store + File.separator + record.getFilename() + ".mp3";
    }
}
