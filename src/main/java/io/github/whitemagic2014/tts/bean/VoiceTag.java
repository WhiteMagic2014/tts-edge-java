package io.github.whitemagic2014.tts.bean;

import java.io.Serializable;
import java.util.List;

public class VoiceTag implements Serializable {
    private static final long serialVersionUID = 1L;
    private List<String> ContentCategories;
    private List<String> VoicePersonalities;

    public List<String> getContentCategories() {
        return ContentCategories;
    }

    public void setContentCategories(List<String> contentCategories) {
        ContentCategories = contentCategories;
    }

    public List<String> getVoicePersonalities() {
        return VoicePersonalities;
    }

    public void setVoicePersonalities(List<String> voicePersonalities) {
        VoicePersonalities = voicePersonalities;
    }

    @Override
    public String toString() {
        return "VoiceTag{" +
                "ContentCategories=" + ContentCategories +
                ", VoicePersonalities=" + VoicePersonalities +
                '}';
    }
}
