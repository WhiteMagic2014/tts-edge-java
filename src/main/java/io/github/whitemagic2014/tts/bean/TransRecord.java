package io.github.whitemagic2014.tts.bean;

public class TransRecord {

    private String content;

    private String filename;

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getFilename() {
        return filename;
    }

    public void setFilename(String filename) {
        this.filename = filename;
    }

    @Override
    public String toString() {
        return "TransRecord{" +
                "content='" + content + '\'' +
                ", filename='" + filename + '\'' +
                '}';
    }
}
