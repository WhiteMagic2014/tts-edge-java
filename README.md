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


```

## Version


### 1.3.2

- Optimize: The method name ```finishBlocking``` has been changed to ```startBlocking```. This method is used to initiate the blocking process and wait for the task to complete.
- Fix BUG: Resolved an issue where the ```enableVttFile``` setting was incorrectly set to ```true``` by default and was not updated properly.
- New: Introduced a new method ```transToAudioStream``` to support direct return of audio streams. [Issue#17](https://github.com/WhiteMagic2014/tts-edge-java/issues/17) [Issue#16](https://github.com/WhiteMagic2014/tts-edge-java/issues/16)

### 1.3.1

- Optimize: Optimize parallel tasks [PR#15](https://github.com/WhiteMagic2014/tts-edge-java/pull/15)

### 1.3.0

- New: Now supports batch conversion of multiple text tasks within a single TTS transaction.
- Optimize: A new parameter, `enableVttFile`, has been added. This parameter determines whether VTT file support is enabled, with the default setting being `false`
- Optimize: Refactored some of the code.
- Update: Upgraded some dependency packages.
- Thanks to user [lifeopsgo](https://github.com/lifeopsgo) for making nearly all the contributions to this version update.[PR#13](https://github.com/WhiteMagic2014/tts-edge-java/pull/13)

### 1.2.6

- Resolve Conversion Issues Caused by Special Characters.[PR#9](https://github.com/WhiteMagic2014/tts-edge-java/pull/9)

### 1.2.5

- Optimize: Setting the parameter `isRateLimited` to true can resolve rate limiting issues in certain regions.
- Thanks to user [PoliceZ](https://github.com/PoliceZ), the answer in
  the [issue](https://github.com/Mai-Onsyn/VeloVoice/issues/9) was helpful to me.

### 1.2.4

- Optimize: The default value for the `overWrite` parameter is now true.

### 1.2.3

- Optimize: A new parameter, `overWrite`, has been added. When the same file name is provided, if overWrite = `true`, it
  will overwrite the original audio file and VTT subtitle file. If overWrite = `false`, it will continue to append to the
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

- Optimize: Fix high CPU usage while waiting for a response. [PR#2](https://github.com/WhiteMagic2014/tts-edge-java/pull/2)

### 1.1.0

- Optimize: Now, the TTS.trans function offers the choice of receiving the output file in two formats, MP3 or opus.
- Optimize: Add two methods to parse voice file

### 1.0.1

- Optimize: Now, the TTS.trans function will return the name of the voice file.

### 1.0.0

- Edge Read Aloud Text To Speech

## License

This project is an open-sourced software licensed under the [MIT license](LICENSE).
