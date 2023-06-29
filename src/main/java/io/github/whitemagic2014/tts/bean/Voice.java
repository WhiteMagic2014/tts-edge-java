package io.github.whitemagic2014.tts.bean;

import java.io.Serializable;

public class Voice implements Serializable {

    private static final long serialVersionUID = 1L;

    private String Name;
    private String ShortName;
    private String Gender;
    private String Locale;
    private String SuggestedCodec;
    private String FriendlyName;
    private String Status;
    private VoiceTag VoiceTag;

    public String getName() {
        return Name;
    }

    public void setName(String name) {
        Name = name;
    }

    public String getShortName() {
        return ShortName;
    }

    public void setShortName(String shortName) {
        ShortName = shortName;
    }

    public String getGender() {
        return Gender;
    }

    public void setGender(String gender) {
        Gender = gender;
    }

    public String getLocale() {
        return Locale;
    }

    public void setLocale(String locale) {
        Locale = locale;
    }

    public String getSuggestedCodec() {
        return SuggestedCodec;
    }

    public void setSuggestedCodec(String suggestedCodec) {
        SuggestedCodec = suggestedCodec;
    }

    public String getFriendlyName() {
        return FriendlyName;
    }

    public void setFriendlyName(String friendlyName) {
        FriendlyName = friendlyName;
    }

    public String getStatus() {
        return Status;
    }

    public void setStatus(String status) {
        Status = status;
    }

    public io.github.whitemagic2014.tts.bean.VoiceTag getVoiceTag() {
        return VoiceTag;
    }

    public void setVoiceTag(io.github.whitemagic2014.tts.bean.VoiceTag voiceTag) {
        VoiceTag = voiceTag;
    }

    @Override
    public String toString() {
        return "Voice{" +
                "Name='" + Name + '\'' +
                ", ShortName='" + ShortName + '\'' +
                ", Gender='" + Gender + '\'' +
                ", Locale='" + Locale + '\'' +
                ", SuggestedCodec='" + SuggestedCodec + '\'' +
                ", FriendlyName='" + FriendlyName + '\'' +
                ", Status='" + Status + '\'' +
                ", VoiceTag=" + VoiceTag +
                '}';
    }
}
