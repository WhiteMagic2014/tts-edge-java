package io.github.whitemagic2014;

import io.github.whitemagic2014.tts.TTS;
import io.github.whitemagic2014.tts.TTSVoice;
import io.github.whitemagic2014.tts.bean.Voice;

import java.util.stream.Collectors;

public class TestTTS {

    public static void main(String[] args) {
        Voice voice = TTSVoice.provides().stream().filter(v -> v.getShortName().equals("zh-CN-XiaoyiNeural")).collect(Collectors.toList()).get(0);
        String content = "你好，有什么可以帮助你的吗";
        new TTS(voice, content)
//                .voicePitch()
//                .voiceRate()
//                .voiceVolume()
//                .storage()  // the output file storage ,default is ./storage
                .trans();
    }

}
