# tts-edge-java

java sdk for Edge Read Aloud

[click me have a try](https://server.whitemagic2014.com/tts/)

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
                .findHeadHook()
                .fileName("file name")// You can customize the file name; if omitted, a random file name will be generated.
                .overwrite(false) // When the specified file name is the same, it will either overwrite or append to the file.
                .formatMp3()  // default mp3.
//                .formatOpus() // or opus
//                .voicePitch()
//                .voiceRate()
//                .voiceVolume()
//                .storage()  // the output file storage ,default is ./storage
//                .connectTimeout(0) // set connect timeout
                .trans();
        // you can find the voice file in storage folder
}
```

## Version

### 1.2.4

- Optimize: The default value for the `overWrite` parameter is now true.

### 1.2.3

- Optimize: A new parameter, overWrite, has been added. When the same file name is provided, if overWrite = true, it
  will overwrite the original audio file and VTT subtitle file. If overWrite = false, it will continue to append to the
  original audio file and VTT subtitle file.

### 1.2.2

- Optimize: TTS can now set connectTimeout.

### 1.2.1

- New: In this update, 17 new supported voices have been added, as follows:

```
    en-US-AvaMultilingualNeural
    en-US-AndrewMultilingualNeural
    en-US-EmmaMultilingualNeural
    en-US-BrianMultilingualNeural
    en-US-AvaNeural
    en-US-AndrewNeural
    en-US-EmmaNeural
    en-US-BrianNeural
    fr-CA-ThierryNeural
    fr-FR-VivienneMultilingualNeural
    fr-FR-RemyMultilingualNeural
    de-DE-SeraphinaMultilingualNeural
    de-DE-FlorianMultilingualNeural
    it-IT-GiuseppeNeural
    ko-KR-HyunsuNeural
    pt-BR-ThalitaNeural
    es-ES-XimenaNeural
```

### 1.2.0

- Optimize: Now You can customize the file name; if omitted, a random file name will be generated.
- New:  Now, while generating audio, a VTT subtitle file with the same name will be
  created.[issue:3](https://github.com/WhiteMagic2014/tts-edge-java/issues/3)

### 1.1.1

- Optimize: Fix high CPU usage while waiting for a response

### 1.1.0

- Optimize: Now, the TTS.trans function offers the choice of receiving the output file in two formats, MP3 or opus.
- Optimize: Add two methods to parse voice file

### 1.0.1

- Optimize: Now, the TTS.trans function will return the name of the voice file.

### 1.0.0

- Edge Read Aloud Text To Speech

## License

This project is an open-sourced software licensed under the [MIT license](LICENSE).
