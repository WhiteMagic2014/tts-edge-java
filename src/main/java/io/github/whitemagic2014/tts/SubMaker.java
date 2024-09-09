package io.github.whitemagic2014.tts;

import org.apache.commons.text.StringEscapeUtils;

import java.io.*;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class SubMaker {

    private String fullFilePath;

    private Double lastoffset = 0d;

    private Double note = 0d;

    private Boolean head = true;

    public SubMaker(String fullFilePath) {
        this.fullFilePath = fullFilePath.concat(".vtt");
        File oldFile = new File(this.fullFilePath);
        String lastNote = "NOTE0";
        if (oldFile.exists()) {
            head = false;
            try (BufferedReader br = new BufferedReader(new FileReader(oldFile))) {
                String line = "";
                while ((line = br.readLine()) != null) {
                    if (line.startsWith("NOTE")) {
                        lastNote = line;
                    }
                }
                lastoffset = Double.parseDouble(lastNote.replace("NOTE", ""));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private List<Double[]> offset = new ArrayList<>();
    private List<String> subs = new ArrayList<>();

    public void createSub(Double start, Double duration, String text) {
        note = lastoffset + start + duration;
        offset.add(new Double[]{lastoffset + start, lastoffset + start + duration});
        subs.add(text);
    }

    public String generateSubs(int wordsInCue) {
        if (subs.size() != offset.size()) {
            throw new IllegalArgumentException("subs and offset are not of the same length");
        }
        if (wordsInCue <= 0) {
            throw new IllegalArgumentException("wordsInCue must be greater than 0");
        }
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(fullFilePath, true))) {
            StringBuilder data = new StringBuilder();
            if (head) {
                bw.write("WEBVTT\r\n\r\n");
            }
            bw.write("NOTE" + note + "\r\n\r\n");
            bw.newLine();
            int subStateCount = 0;
            double subStateStart = -1.0;
            StringBuilder subStateSubs = new StringBuilder();
            for (int idx = 0; idx < offset.size(); idx++) {
                Double[] currentOffset = offset.get(idx);
                double startTime = currentOffset[0];
                double endTime = currentOffset[1];
                String subsText = StringEscapeUtils.unescapeHtml4(subs.get(idx));

                if (subStateSubs.length() > 0) {
                    subStateSubs.append(" ");
                }
                subStateSubs.append(subsText);

                if (subStateStart == -1.0) {
                    subStateStart = startTime;
                }
                subStateCount++;

                if (subStateCount == wordsInCue || idx == offset.size() - 1) {
                    List<String> splitSubs = splitStringEvery(subStateSubs.toString(), 79);
                    for (int i = 0; i < splitSubs.size() - 1; i++) {
                        String sub = splitSubs.get(i);
                        boolean splitAtWord = true;
                        if (sub.endsWith(" ")) {
                            splitSubs.set(i, sub.substring(0, sub.length() - 1));
                            splitAtWord = false;
                        }
                        if (sub.startsWith(" ")) {
                            splitSubs.set(i, sub.substring(1));
                            splitAtWord = false;
                        }
                        if (splitAtWord) {
                            splitSubs.set(i, sub + "-");
                        }
                    }
                    data.append(formatter(subStateStart, endTime, String.join("\r\n", splitSubs)));
                    bw.write(formatter(subStateStart, endTime, String.join("\r\n", splitSubs)));
                    bw.newLine();

                    subStateCount = 0;
                    subStateStart = -1;
                    subStateSubs = new StringBuilder();
                }
            }
            return data.toString();

        } catch (Exception e) {
            return null;
        }
    }

    private String formatter(double startTime, double endTime, String subdata) {
        return String.format("%s --> %s\r\n%s\r\n\r\n", mktimestamp(startTime), mktimestamp(endTime), StringEscapeUtils.escapeHtml4(subdata));
    }

    private String mktimestamp(double timeUnit) {
        int hour = (int) Math.floor(timeUnit / Math.pow(10, 7) / 3600);
        int minute = (int) Math.floor((timeUnit / Math.pow(10, 7) / 60) % 60);
        double seconds = (timeUnit / Math.pow(10, 7)) % 60;

        BigDecimal secondsBD = new BigDecimal(seconds).setScale(3, BigDecimal.ROUND_HALF_UP);

        return String.format("%02d:%02d:%06.3f", hour, minute, secondsBD.doubleValue());
    }

    private List<String> splitStringEvery(String s, int interval) {
        List<String> result = new ArrayList<>();
        for (int i = 0; i < s.length(); i += interval) {
            result.add(s.substring(i, Math.min(i + interval, s.length())));
        }
        return result;
    }

}
