package io.github.whitemagic2014.tts;

import io.github.whitemagic2014.tts.bean.Voice;
import org.junit.jupiter.api.Test;

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
        System.out.println(false);
    }
}
