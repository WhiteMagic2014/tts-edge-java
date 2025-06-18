package io.github.whitemagic2014.tts;

import io.github.whitemagic2014.tts.bean.Voice;
import org.apache.commons.lang3.tuple.Pair;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class TTSTest {

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
     * 测试单内容场景能否转换成功
     */
    @Test
    void should_convert_to_mp3_file_success_with_multi_content() {
        String voiceName = "zh-CN-XiaoyiNeural";
        Optional<Voice> voiceOptional = TTSVoice.provides()
                .stream()
                .filter(v -> voiceName.equals(v.getShortName()))
                .findFirst();
        if (!voiceOptional.isPresent()) {
            throw new IllegalStateException("voice not found：" + voiceName);
        }
        Voice voice = voiceOptional.get();
        List<Pair<String, String>> contentAndFilePairList = new ArrayList<>();
        contentAndFilePairList.add(Pair.of("hello tts, 你好，有什么可以帮助你的吗", "1.test-tts"));
        contentAndFilePairList.add(Pair.of("hello multi tts, 你好，有什么可以帮助我的吗", "2.test-tts"));
        new TTS(voice)
                .findHeadHook()
                .isRateLimited(true)
                .overwrite(true)
                .parallelThreadSize(2)
                .contentAndFilePairList(contentAndFilePairList)
                .formatMp3()
                .trans();
    }
}
