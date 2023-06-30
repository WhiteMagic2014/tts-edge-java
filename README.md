# tts-edge-java

java sdk for Edge Read Aloud

## Setup

### maven

```
<dependency>
  <groupId>io.github.whitemagic2014</groupId>
  <artifactId>tts-edge-java</artifactId>
  <version>version</version>
</dependency>
```

### gradle

```
implementation group: 'io.github.whitemagic2014', name: 'tts-edge-java', version: 'version'

short
implementation 'io.github.whitemagic2014:tts-edge-java:version'
```

## demo

```
public static void main(String[] args) {
    // Voice can be found in file "voicesList.json" 
    Voice voice = TTSVoice.provides().stream().filter(v -> v.getShortName().equals("zh-CN-XiaoyiNeural")).collect(Collectors.toList()).get(0);
    String content = "你好，有什么可以帮助你的吗";
    String fileName = new TTS(voice, content)
//                .voicePitch()
//                .voiceRate()
//                .voiceVolume()
//                .storage()  // the output file storage ,default is ./storage
            .trans();
    // you can find the voice file in storage folder        
}
```

## Version

### 1.0.1

- Optimize: Now, the TTS.trans function will return the name of the voice file.

### 1.0.0

- Edge Read Aloud Text To Speech

## License

This project is an open-sourced software licensed under the [MIT license](LICENSE).